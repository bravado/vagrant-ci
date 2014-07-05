package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.jms.tools.JMSNotificationTools;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.BlockDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PresenceDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Presence;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.facets.user.PresenceFacet;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ACDInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.AboutMeInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ConfigViewInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PresenceInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserForwardsInfo;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class AboutMeManager extends Manager
{
	private PbxuserManager puManager;
	private GroupManager groupManager;
	private AddressDAO addDAO;
	private BlockDAO blockDAO;
	private PbxuserDAO pbxuserDAO;
	private ConfigDAO configDAO;
	private PbxDAO pbxDAO;
	private PresenceDAO presenceDAO;
	private ForwardDAO forwardDAO;
	private GroupDAO groupDAO;
		
	public AboutMeManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
	
		puManager = new PbxuserManager(loggerPath);
		groupManager = new GroupManager(loggerPath);
		addDAO = dao.getDAO(AddressDAO.class);
		pbxuserDAO = dao.getDAO(PbxuserDAO.class);
		blockDAO = dao.getDAO(BlockDAO.class);
		configDAO = dao.getDAO(ConfigDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		presenceDAO = dao.getDAO(PresenceDAO.class);
		forwardDAO = dao.getDAO(ForwardDAO.class);
		groupDAO = dao.getDAO(GroupDAO.class);
	}

	public void updateAboutInfo(AboutMeInfo aboutMeInfo) throws DAOException, ValidateObjectException
	{
		if(aboutMeInfo.getPresenceKey() != null)
			saveOrUpdatePresence(aboutMeInfo);
		else if(aboutMeInfo.isDefaultDIDKeyUpdated())
			updateDefaultDIDKey(aboutMeInfo);
		else if(aboutMeInfo.getVoiceMailUpdated())
			updateVoiceMailEnable(aboutMeInfo);
		else if(aboutMeInfo.getForwardAlwaysUpdated())
			updateForwardAways(aboutMeInfo);
		else if(aboutMeInfo.getForwardBusyUpdated())
			updateForwardBusy(aboutMeInfo);
		else if(aboutMeInfo.getForwardNoAnswerUpdated())
			updateForwardNoAnswer(aboutMeInfo);
		else if(aboutMeInfo.getForwardCallFailureUpdated())
			updateForwardCallFailure(aboutMeInfo);
		else if(aboutMeInfo.getPbxNightModeDestination() != null)
			updateNightModeEnable(aboutMeInfo);
		else if(aboutMeInfo.getConfigKey() != null)
			updateDNDSatus(aboutMeInfo);
	}
	
	public AboutMeInfo getAboutInfo(Long pbxuserKey) throws DAOException
	{					

		AboutMeInfo aboutMeInfo = new AboutMeInfo();
		
		UserForwardsInfo userInfo =  puManager.getForwardSettings(pbxuserKey, false);
		aboutMeInfo.setForwardAlways(userInfo.getForwardAlways());
		if(userInfo.getForwardAlwaysTargetKey() != null)
			aboutMeInfo.setForwardAlwaysTarget(addDAO.getByKey(userInfo.getForwardAlwaysTargetKey()).getAddress());

		aboutMeInfo.setForwardBusy(userInfo.getForwardBusy());
		if(userInfo.getForwardBusyTargetKey() != null)
			aboutMeInfo.setForwardBusyTarget(addDAO.getByKey(userInfo.getForwardBusyTargetKey()).getAddress());

		aboutMeInfo.setForwardNoAnswer(userInfo.getForwardNoAnswer());
		if(userInfo.getForwardNoAnswerTargetKey() != null)
			aboutMeInfo.setForwardNoAnswerTarget(addDAO.getByKey(userInfo.getForwardNoAnswerTargetKey()).getAddress());

		aboutMeInfo.setForwardCallFailure(userInfo.getForwardCallFailure());
		if(userInfo.getForwardCallFailureTargetKey() != null)
			aboutMeInfo.setForwardCallFailureTarget(addDAO.getByKey(userInfo.getForwardCallFailureTargetKey()).getAddress());

		List<Address> extensionsAddressList = addDAO.getExtensionListByPbxuser(pbxuserKey);
		List<String> extensions = new ArrayList<String>();
		for(Address ext : extensionsAddressList)
			extensions.add(ext.getAddress());

		List<Address> extensionsTerminalAssociateds = addDAO.getTerminalExtensionListAssociatedByPbxuser(pbxuserKey);
		for(Address ad : extensionsTerminalAssociateds)
			extensions.add(ad.getAddress());

		Pbxuser pu = pbxuserDAO.getPbxuserFull(pbxuserKey);		

		PresenceInfo presenceInfo = puManager.getPresenceInfo(pbxuserKey);	

		aboutMeInfo.setDefaultDIDKey(pu.getDefaultDIDKey());
		aboutMeInfo.setPresenceInfo(presenceInfo);
		aboutMeInfo.setExtensionList(extensions);
		boolean anonymous = pu.getIsAnonymous() == Pbxuser.ANONYMOUS_ON ? true : false;
		aboutMeInfo.setIsAnonymous(anonymous);

		
		aboutMeInfo.setDIDList(puManager.getPbxuserDIDList(pbxuserKey));

		Config conf = pu.getConfig();

		boolean dndStatus = conf.getDndStatus()== Config.DND_ON ? true : false;
		aboutMeInfo.setDNDStatus(dndStatus);
		aboutMeInfo.setConfigKey(conf.getKey());
		ConfigViewInfo confInfo = puManager.getPbxuserConfigurations(pbxuserKey);
		aboutMeInfo.setDID(confInfo.getDID());
		aboutMeInfo.setPbxNightMode(confInfo.getPbxNightMode());
		aboutMeInfo.setPbxNightModeDestination(confInfo.getPbxNightModeDestination());
		aboutMeInfo.setVoiceMailEnabled(confInfo.getVoiceMailEnable());
		aboutMeInfo.setPbxuserKey(pbxuserKey);

		ACDInfo acdInfo = groupManager.getACDInfo(pbxuserKey);
		if(acdInfo != null)
		{
			aboutMeInfo.setGroups(acdInfo.getGroups());
			aboutMeInfo.setLoginDate(acdInfo.getLoginDate());
			aboutMeInfo.setLogged(acdInfo.getLogged());
		}

		List<Group> groupList = groupDAO.getGroupListByPbxuser(pbxuserKey);
		if(groupList != null && groupList.size() > 0)
			for(Group g : groupList)
				aboutMeInfo.addNightmodeStatusGroupAndName(new Duo<Integer, String>(g.getNightmodeStatus(), g.getName()));
		
		return aboutMeInfo;
	}

	public void updateDefaultDIDKey(AboutMeInfo aboutMeInfo) throws DAOException, ValidateObjectException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserFull(aboutMeInfo.getPbxuserKey());	
	
		pu.setDefaultDIDKey(aboutMeInfo.getDefaultDIDKey());
		pu.setIsAnonymous(aboutMeInfo.isAnonymous() ? Pbxuser.ANONYMOUS_ON : Pbxuser.ANONYMOUS_OFF);
		pbxuserDAO.save(pu);
	}
	
	public void updateDNDSatus(AboutMeInfo aboutMeInfo) throws DAOException, ValidateObjectException
	{
		Config conf = configDAO.getByKey(aboutMeInfo.getConfigKey());
		conf.setDndStatus(aboutMeInfo.getDNDStatus() ? Config.DND_ON : Config.DND_OFF);
		
		configDAO.save(conf);
	}
	
	public void updateVoiceMailEnable(AboutMeInfo aboutMeInfo) throws DAOException, ValidateObjectException
	{
		Config conf = configDAO.getByKey(aboutMeInfo.getConfigKey());
		conf.setDisableVoicemail(aboutMeInfo.getVoiceMailEnabled() ? Config.VOICEMAIL_OFF : Config.VOICEMAIL_ON);
		configDAO.save(conf);

		if(!aboutMeInfo.getVoiceMailEnabled())
		{
			Pbxuser pu = pbxuserDAO.getPbxuserByConfig(aboutMeInfo.getConfigKey());
			Address voicemailAddress = addDAO.getAddress(User.VOICEMAIL_NAME, pu.getUser().getDomainKey());
			List<Forward> forwardList = forwardDAO.getForwardListByConfig(aboutMeInfo.getConfigKey());
			for(Forward f : forwardList)
				if(f.getAddressKey() != null && voicemailAddress != null && f.getAddressKey().equals(voicemailAddress.getKey()))
				{
					f.setStatus(Forward.STATUS_OFF);
					f.setAddressKey(null);
					f.setTarget(null);
					forwardDAO.save(f);
				}
		}
	}
	
	public void updateForwardAways(AboutMeInfo aboutMeInfo) throws DAOException, ValidateObjectException
	{
		UserForwardsInfo userInfo =  puManager.getForwardSettings(aboutMeInfo.getPbxuserKey(), true);
		
		userInfo.setForwardAlwaysTargetKey(aboutMeInfo.getForwardAlwaysTargetKey());
		userInfo.setForwardAlwaysEnable(aboutMeInfo.getForwardAlwaysEnable());
		userInfo.setForwardAlwaysTarget(aboutMeInfo.getForwardAlwaysTarget());
		userInfo.setConfigKey(aboutMeInfo.getConfigKey());
		userInfo.setPbxuserKey(aboutMeInfo.getPbxuserKey());
		userInfo.setDomainKey(aboutMeInfo.getDomainKey());
		
		puManager.updateForwardSettings(userInfo);
	}
	
	public void updateForwardBusy(AboutMeInfo aboutMeInfo) throws DAOException, ValidateObjectException
	{
		UserForwardsInfo userInfo =  puManager.getForwardSettings(aboutMeInfo.getPbxuserKey(), true);
		
		userInfo.setForwardBusyTargetKey(aboutMeInfo.getForwardBusyTargetKey());
		userInfo.setForwardBusyEnable(aboutMeInfo.getForwardBusyEnable());
		userInfo.setForwardBusyTarget(aboutMeInfo.getForwardBusyTarget());
		userInfo.setConfigKey(aboutMeInfo.getConfigKey());
		userInfo.setPbxuserKey(aboutMeInfo.getPbxuserKey());
		
		puManager.updateForwardSettings(userInfo);
	}
	
	public void updateForwardNoAnswer(AboutMeInfo aboutMeInfo) throws DAOException, ValidateObjectException
	{
		UserForwardsInfo userInfo =  puManager.getForwardSettings(aboutMeInfo.getPbxuserKey(), true);
		
		userInfo.setForwardNoAnswerTargetKey(aboutMeInfo.getForwardNoAnswerTargetKey());
		userInfo.setForwardNoAnswerEnable(aboutMeInfo.getForwardNoAnswerEnable());
		userInfo.setForwardNoAnswerTarget(aboutMeInfo.getForwardNoAnswerTarget());
		userInfo.setConfigKey(aboutMeInfo.getConfigKey());
		userInfo.setPbxuserKey(aboutMeInfo.getPbxuserKey());
		
		puManager.updateForwardSettings(userInfo);
	}
	
	public void updateForwardCallFailure(AboutMeInfo aboutMeInfo) throws DAOException, ValidateObjectException
	{
		UserForwardsInfo userInfo =  puManager.getForwardSettings(aboutMeInfo.getPbxuserKey(), true);
		
		userInfo.setForwardCallFailureTargetKey(aboutMeInfo.getForwardCallFailureTargetKey());
		userInfo.setForwardCallFailureEnable(aboutMeInfo.getForwardCallFailureEnable());
		userInfo.setForwardCallFailureTarget(aboutMeInfo.getForwardCallFailureTarget());
		userInfo.setConfigKey(aboutMeInfo.getConfigKey());
		userInfo.setPbxuserKey(aboutMeInfo.getPbxuserKey());
		
		puManager.updateForwardSettings(userInfo);
	}
	
	public void updateNightModeEnable(AboutMeInfo aboutMeInfo) throws DAOException, ValidateObjectException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserFull(aboutMeInfo.getPbxuserKey());
		
		Pbx pbx = pbxDAO.getPbxByDomain(pu.getUser().getDomainKey());
		Integer nightMode = aboutMeInfo.getPbxNightMode() ? Pbx.NIGHTMODE_ON : Pbx.NIGHTMODE_OFF;
		pbx.setNightMode(nightMode);
		
		pbxDAO.save(pbx);
	
	}
	
	public void saveOrUpdatePresence(PresenceFacet presenceInfo) throws DAOException, ValidateObjectException 
	{
		Presence presence = presenceInfo.getPresence();
		if(presence.getState() == Presence.STATE_OFFLINE)
		{
			presence.setState(Presence.STATE_ONLINE);
			logger.warn("Cannot save presence with status Offline! Changing it to Online");
		}
		presenceDAO.save(presence);
	}
	
}
