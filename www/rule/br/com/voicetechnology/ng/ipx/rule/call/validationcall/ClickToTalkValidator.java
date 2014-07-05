package br.com.voicetechnology.ng.ipx.rule.call.validationcall;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.NoTargetsWereFound;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.ForwardAlwaysException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.OriginationInDNDException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Leg;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Callback;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.rule.implement.CallBackManager;

public class ClickToTalkValidator extends ValidatorBase
{

	private CallBackManager cbManager;
	
	public ClickToTalkValidator() throws DAOException
	{
		super();
		cbManager = new CallBackManager("EJB");
	}

	public void validate(Leg fromLeg, Leg toLeg) throws ValidationException, DAOException
	{
		if(!checkSipSessionlog(fromLeg.getPbxuser().getKey()))
			throw new NoTargetsWereFound("Origination isn't registered!!", CallStateEvent.UNAVALIABLE);

		Config fromConfig = configManager.getConfigByKey(fromLeg.getPbxuser().getConfigKey());

		if(fromConfig.getDndStatus().intValue() == Config.DND_ON)
			throw new OriginationInDNDException("Origination cannot use dnd on to use callback!", CallStateEvent.PERMISSION_DENIED);

		if(cbManager.getCallbackByFromAndTo(fromLeg.getSipAddress().getSipAddress(), toLeg.getSipAddress().getSipAddress()) != null)
			throw new ValidationException(Callback.CALL_BACK_DUPLICATED, CallStateEvent.PERMISSION_DENIED);

		if(checkForwardToExternal(fromLeg.getPbxuser().getConfigKey(), Forward.ALWAYS_MODE))
			throw new ForwardAlwaysException(Callback.CALL_BACK_ORIGINATION_FORWARD_ALWAYS, CallStateEvent.PERMISSION_DENIED);

		if(toLeg.isPbxuser() && !checkSipSessionlog(toLeg.getPbxuser().getKey()))
			if(!checkForward(toLeg.getPbxuser().getConfigKey(), Forward.ALWAYS_MODE))
				if(toLeg.getPbxuser().getConfig().getDisableVoicemail() == Config.VOICEMAIL_OFF)				
					throw new NoTargetsWereFound("Destination isn't registered!!", CallStateEvent.UNAVALIABLE);
		
		checkFilters(toLeg, fromLeg, ValidationModes.CALLBACK_MODE);
	}
	
    protected boolean checkForwardToExternal(Long configKey, int forwardMode) throws DAOException
    {
    	Forward forward = configManager.getForwardByUserAndMode(configKey, forwardMode);
        return forward != null && forward.getStatus() == Forward.STATUS_ON && forward.getAddressKey() != null;
    }
}