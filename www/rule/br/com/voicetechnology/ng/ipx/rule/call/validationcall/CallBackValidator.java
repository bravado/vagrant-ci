package br.com.voicetechnology.ng.ipx.rule.call.validationcall;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.ForwardAlwaysException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.OriginationInDNDException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.command.CallBackException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Leg;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Callback;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.rule.implement.CallBackManager;

public class CallBackValidator extends ValidatorBase 
{
	private CallBackManager cbManager;
	
	public CallBackValidator() throws DAOException 
	{
		super();
		cbManager = new CallBackManager("EJB");
	}
	
	public void validate(Callback callback) throws CallBackException, DAOException, ValidateObjectException, ValidationException
	{
		Config fromConfig = configManager.getConfigByKey(callback.getPbxuserFrom().getConfigKey());

		if(fromConfig.getDndStatus().intValue() == Config.DND_ON)
			throw new OriginationInDNDException("Origination cannot use dnd on to use callback!", CallStateEvent.PERMISSION_DENIED);

		if(cbManager.getCallbackByFromAndTo(callback.getSipFrom(),	callback.getSipTo()) != null)
			throw new ValidationException(Callback.CALL_BACK_DUPLICATED, CallStateEvent.PERMISSION_DENIED);

		if(checkForward(callback.getPbxuserFrom().getConfigKey(), Forward.ALWAYS_MODE))
			throw new ForwardAlwaysException(Callback.CALL_BACK_ORIGINATION_FORWARD_ALWAYS, CallStateEvent.PERMISSION_DENIED);
		
		Config configFrom = configManager.getConfigByKey(callback.getPbxuserFrom().getConfigKey());
		SipAddressParser to = new SipAddressParser(callback.getSipTo());
		Leg toLeg = new Leg(to, to.clone(), null, false);
		if(checkBlock(configFrom, toLeg, Block.TYPE_OUTGOING, null))
			throw new ValidationException(Callback.CALL_BACK_ORIGINATION_OUTGOING_BLOCK, CallStateEvent.PERMISSION_DENIED);
		
		SipAddressParser from = new SipAddressParser(callback.getSipFrom()); 
		Leg fromLeg = new Leg(from, from.clone(), null, false);
		
		if(callback.getPbxuserTo() != null)
		{
			Config configTo = configManager.getConfigByKey(callback.getPbxuserTo().getConfigKey());
			if(checkBlock(configTo, toLeg, Block.TYPE_INCOMING, fromLeg))
				throw new ValidationException(Callback.CALL_BACK_DESTINATION_INCOMING_BLOCK, CallStateEvent.PERMISSION_DENIED);
		} else if(callback.getType() == Callback.TYPE_CALL_BACK)	//se é callback é obrigatório que o destino seja um pbxuser
			throw new ValidationException(Callback.CALL_BACK_DESTINATION_NOTEXIST, CallStateEvent.PERMISSION_DENIED);
	}
}