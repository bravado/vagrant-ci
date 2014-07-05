package br.com.voicetechnology.ng.ipx.rule.implement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.inconsistency.InconsistencyType;
import br.com.voicetechnology.ng.ipx.commons.exception.inconsistency.ObjectInconsistencyException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.jms.tools.JMSNotificationTools;
import br.com.voicetechnology.ng.ipx.commons.security.crypt.Crypt;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.ActivecallDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.BlockDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigblockDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CostCenterDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.DialPlanDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.KoushikubunDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.NightModeSchedulerDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ServiceclassDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipsessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SpeeddialDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ivr.IVRDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ivr.IVROptionDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.FileinfoDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.GroupfileDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RoleDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserfileDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserroleDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVR;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVROption;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Configblock;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CostCenter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Koushikubun;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.NightModeScheduler;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Groupfile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userfile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userrole;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.DIDInPBXInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.DIDInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.IVRInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PBXDialPlanInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PBXInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PbxuserInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.implement.ivr.IVRManager;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class PbxManager extends Manager
{
	private PbxDAO pbxDAO;
	private DomainDAO domainDAO;
	private AddressDAO addDAO;
	private PbxuserDAO pbxuserDAO;
	private FileinfoDAO fileDAO;
	private IVRDAO ivrDAO;
	private ActivecallDAO acDAO;
	private SpeeddialDAO sdDAO;
	private UserDAO userDAO;
	private UserfileDAO ufDAO;
	private CalllogDAO clDAO;
	private ConfigDAO configDAO;
	private ServiceclassDAO serviceClassDAO;
	private BlockDAO blockDAO;
	private ConfigblockDAO configBlockDAO;
	private RoleDAO roleDAO;
	private ReportDAO<Pbx, PBXInfo> reportDomainDAO;
	private PbxpreferenceDAO pbxPreferencesDAO;
	private GroupDAO groupDAO;
	private GroupfileDAO groupFileDAO;
	private TerminalDAO terminalDAO;
	private ForwardDAO forwardDAO;
	private IVROptionDAO ivrOptionDAO;
	private SipsessionlogDAO sipSessionDAO;
	private UserroleDAO userRoleDAO;
	private KoushikubunDAO koushikubunDAO;
	private DialPlanDAO dPlanDAO;
	private DialPlanManager dPlanManager;
	private NightModeSchedulerDAO nightModeSchedulerDAO;
	private CostCenterDAO costCenterDAO;
	private ServiceclassDAO scDAO;
	IVRManager ivrManager;		
	
	public PbxManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		pbxDAO = dao.getDAO(PbxDAO.class);		
		domainDAO = dao.getDAO(DomainDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		pbxuserDAO = dao.getDAO(PbxuserDAO.class);
		fileDAO = dao.getDAO(FileinfoDAO.class);
		ivrDAO = dao.getDAO(IVRDAO.class);
		acDAO = dao.getDAO(ActivecallDAO.class);
		sdDAO = dao.getDAO(SpeeddialDAO.class);	
		userDAO = dao.getDAO(UserDAO.class);
		ufDAO = dao.getDAO(UserfileDAO.class);
		clDAO = dao.getDAO(CalllogDAO.class);
		configDAO = dao.getDAO(ConfigDAO.class);
		serviceClassDAO = dao.getDAO(ServiceclassDAO.class);
		blockDAO = dao.getDAO(BlockDAO.class);
		configBlockDAO = dao.getDAO(ConfigblockDAO.class);
		roleDAO = dao.getDAO(RoleDAO.class);
		reportDomainDAO = dao.getDAO(PbxDAO.class);
		pbxPreferencesDAO = dao.getDAO(PbxpreferenceDAO.class);
		groupDAO = dao.getDAO(GroupDAO.class);
		groupFileDAO = dao.getDAO(GroupfileDAO.class);
		terminalDAO = dao.getDAO(TerminalDAO.class);
		forwardDAO = dao.getDAO(ForwardDAO.class);
		ivrOptionDAO = dao.getDAO(IVROptionDAO.class);
		sipSessionDAO = dao.getDAO(SipsessionlogDAO.class);
		userRoleDAO = dao.getDAO(UserroleDAO.class);
		koushikubunDAO = dao.getDAO(KoushikubunDAO.class);
		dPlanDAO = dao.getDAO(DialPlanDAO.class);
		dPlanManager = new DialPlanManager(loggerPath);
		nightModeSchedulerDAO = dao.getDAO(NightModeSchedulerDAO.class);
		costCenterDAO = dao.getDAO(CostCenterDAO.class);
		scDAO = dao.getDAO(ServiceclassDAO.class);
		ivrManager = new IVRManager(logger.getName());
	}
	
	public ReportResult findPbxs(Report<PBXInfo> info) throws DAOException
	{
		Long size = reportDomainDAO.getReportCount(info);
		List<Pbx> pbxList = reportDomainDAO.getReportList(info);
		List<PBXInfo> pbxInfoList = new ArrayList<PBXInfo>(pbxList.size());
		for(Pbx pbx : pbxList)
			pbxInfoList.add(new PBXInfo(pbx, pbx.getDomain()));
		return new ReportResult<PBXInfo>(pbxInfoList, size);
	}
	
	//Metodo usado para a edicao do PBX na parte do WCA/PBX Config
	public PBXInfo getPBXInfoByKey(Long pbxKey) throws DAOException
	{
		return getPBXInfoByKey(pbxKey, true);
	}
	
	//Metodo usado para a edicao do PBX na parte do WCA/PBX Config
	public PBXInfo getPBXInfoByKey(Long pbxKey, boolean getContext) throws DAOException
	{
		Pbx pbx = pbxDAO.getPbxFull(pbxKey);
		Long domainKey = pbx.getUser().getDomainKey();
		Address voiceMailExtension = addDAO.getVoicemailAddress(domainKey);
		Address recordBoxExtension = addDAO.getRecordFileBoxAddress(domainKey);
		Address defaultAddress = addDAO.getDefaultAddress(pbx.getKey());
		Long userAmount = pbxuserDAO.countUsers(domainKey);
		Long ivrAmount = ivrDAO.countIVRs(domainKey);
		Long didAmount = addDAO.countDIDs(domainKey, false);
		Long didsBlockedAmount = addDAO.countDIDs(domainKey, true);
		Long recordCallUsersAmount = pbxuserDAO.countPbxuserRecordCall(domainKey);
		Double usedQuota = fileDAO.countUsedQuota(domainKey);
		Domain domain = domainDAO.getByKey(domainKey);
		
		Long musicFileKey = null;
		Long parkMusicFileKey = null;
		String farmIP = null;
		List<User> musicUsersList = userDAO.getUsersInDomain(domainKey, User.TYPE_MUSIC_SERVER);
		if(musicUsersList.size() > 0)
		{
			farmIP = musicUsersList.get(0).getFarmIP();
			List<Fileinfo> musicFiles = fileDAO.getFilesByUser(musicUsersList.get(0).getKey());
			if(musicFiles.size() > 0)
				musicFileKey = musicFiles.get(0).getKey();
		}
		musicUsersList = userDAO.getUsersInDomain(domainKey, User.TYPE_PARK_SERVER);
		if(musicUsersList.size() > 0)
		{
			List<Fileinfo> parkMusicFiles = fileDAO.getFilesByUser(musicUsersList.get(0).getKey());
			if(parkMusicFiles.size() > 0)
				parkMusicFileKey = parkMusicFiles.get(0).getKey();
		}
		PBXInfo pbxInfo = new PBXInfo(pbx, defaultAddress.getKey(), musicFileKey, parkMusicFileKey, voiceMailExtension != null ? voiceMailExtension.getAddress() : null, userAmount, ivrAmount, didAmount, didsBlockedAmount, usedQuota.floatValue(), pbx.getPbxPreferences(), domain, farmIP,  recordBoxExtension != null ? recordBoxExtension.getAddress() : null);
		pbxInfo.setRecordCallUsersAmount(recordCallUsersAmount);
		pbxInfo.setNightmodeSchedulerList(nightModeSchedulerDAO.getNightModeSchedulerByPbx(pbxKey));
				
		if(getContext)
			getContext(pbxInfo, domainKey);
		
		return pbxInfo;
	}
	
	public void getContext(PBXInfo pbxInfo, Long domainKey) throws DAOException
	{
		pbxInfo.addGroupNameSipIDList(addDAO.getGroupKeyAndSipIDByDomain(domainKey, false));
		pbxInfo.addUsernameSipIDList(addDAO.getPbxuserKeyAndSipIDByDomain(domainKey, true));
		pbxInfo.addDIDList(addDAO.getAvailableDIDList(domainKey));
		pbxInfo.addFileList(fileDAO.getFilesInDomain(domainKey, Fileinfo.TYPE_SIMPLEFILE));
	}
	
	
	public PBXInfo getContext(PBXInfo pbxInfo, Integer lastIndex, Long domainKey) throws DAOException
	{
		pbxInfo.setListsLastIndex(lastIndex);
		int maxLength = PBXInfo.lIST_MAX_LENGTH;
		List<Duo<Long, String>> usernameSipIDList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> groupNameSipIDList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> fileList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> ivrList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> sipTrunkList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> didList = new ArrayList<Duo<Long,String>>();
		
		if(!pbxInfo.isUsernameSipIDListLoaded())
		{
			usernameSipIDList = addDAO.getUsernameSIPIDList(domainKey, false, pbxInfo.getListsLastIndex(), maxLength);
			
			if(usernameSipIDList.size() < maxLength)
			{
				pbxInfo.setUsernameSipIDListLoaded(true);
				pbxInfo.setListsLastIndex(0);
			}
			else
			{
				pbxInfo.setListsLastIndex(pbxInfo.getListsLastIndex() + usernameSipIDList.size());
			}
			
			maxLength -= usernameSipIDList.size();
		}
		if(!pbxInfo.isSipTrunkListLoaded())
		{
			sipTrunkList = addDAO.getSipTrunkSIPIDList(domainKey, pbxInfo.getListsLastIndex(), maxLength);
			
			if(sipTrunkList.size() < maxLength)
			{
				pbxInfo.setSipTrunkListLoaded(true);
				pbxInfo.setListsLastIndex(0);
			}
			else
			{
				pbxInfo.setListsLastIndex(pbxInfo.getListsLastIndex() + sipTrunkList.size());
			}
			
			maxLength -= sipTrunkList.size();
		}
		if(!pbxInfo.isGroupSipIDListLoaded())
		{
			groupNameSipIDList = addDAO.getGroupNameSipIDList(domainKey, false, pbxInfo.getListsLastIndex(), maxLength);
			
			if(groupNameSipIDList.size() < maxLength)
			{
				pbxInfo.setGroupSipIDListLoaded(true);
				pbxInfo.setListsLastIndex(0);
			}
			else
			{
				pbxInfo.setListsLastIndex(pbxInfo.getListsLastIndex() + groupNameSipIDList.size());
			}
			
			maxLength -= groupNameSipIDList.size();
		}
		if(!pbxInfo.isDidListLoaded())
		{
			didList = addDAO.getAvailableDIDList(domainKey, pbxInfo.getListsLastIndex(), maxLength);
			
			if(didList.size() < maxLength)
			{
				pbxInfo.setDidListLoaded(true);
				pbxInfo.setListsLastIndex(0);
			}
			else
			{
				pbxInfo.setListsLastIndex(pbxInfo.getListsLastIndex() + didList.size());
			}
			
			maxLength -= didList.size();
		}
		if(!pbxInfo.isFileListLoaded())
		{
			fileList = fileDAO.getFilesInDomain(domainKey, Fileinfo.TYPE_SIMPLEFILE, pbxInfo.getListsLastIndex(), maxLength);
			
			if(fileList.size() < maxLength)
			{
				pbxInfo.setFileListLoaded(true);
				pbxInfo.setListsLastIndex(0);
			}
			else
			{
				pbxInfo.setListsLastIndex(pbxInfo.getListsLastIndex() + fileList.size());
			}
			
			maxLength -= fileList.size();
		}
		// tveiga basix 3.06 add lista de ivr na tela de config do pbx - inicio 
		if(!pbxInfo.isIvrListLoaded())
		{
			ivrList = ivrDAO.getIVRKeyAndNameList(domainKey, pbxInfo.getListsLastIndex(), maxLength);
			
			if(ivrList.size() < maxLength)
			{
				pbxInfo.setIvrListLoaded(true);
				pbxInfo.setListsLastIndex(0);
			}
			else
			{
				pbxInfo.setListsLastIndex(pbxInfo.getListsLastIndex() + ivrList.size());
			}
			
			maxLength -= ivrList.size();
		}
		// tveiga basix 3.06 add lista de ivr na tela de config do pbx - fim 
		pbxInfo.addDIDList(didList);
		pbxInfo.addGroupNameSipIDList(groupNameSipIDList);
		pbxInfo.addUsernameSipIDList(usernameSipIDList);
		pbxInfo.addFileList(fileList);
		pbxInfo.addIvrList(ivrList);
		pbxInfo.addSipTrunkNameSipIDList(sipTrunkList);
		// tveiga basix 3.06 add lista de ivr na tela de config do pbx - 
		
		return pbxInfo;
			
	}
	
	public void update(PBXInfo pbxInfo) throws DAOException, ValidateObjectException, ObjectInconsistencyException
	{
		Pbx pbx = pbxInfo.getPbx();
		Pbxpreference pbxPreferences = pbxInfo.getPbxpreferences();	
		
		if(pbx.getKey() == null)
			throw new NullPointerException("Cannot update a pbx without key!");
		
		Domain domain = domainDAO.getDomainByPbx(pbx.getKey());		
		DialPlan dialPlan = dPlanDAO.getDialPlanByTypeAndDomain(DialPlan.TYPE_DEFAULTOPERATOR, domain.getKey());
		dPlanManager.validateDialPlanNumber(dialPlan, pbx.getOperator());
		
		Pbx oldPbx = pbxDAO.getByKey(pbx.getKey());
		oldPbx.setOperator(pbx.getOperator());
		oldPbx.setDefaultaddressKey(pbx.getDefaultaddressKey());
		// jfarah - 3.1.0 - conserva o estado do nightmode caso o nightmodescheduler esteja ativo. 
		if(pbxPreferences.getNightModeScheduler().equals(Pbxpreference.NIGHTMODE_SCHEDULER_OFF))
			oldPbx.setNightMode(pbx.getNightMode());
		oldPbx.setMaxMessageTime(pbx.getMaxMessageTime());
		oldPbx.setNightmodeaddressKey(pbx.getNightmodeaddressKey());
		oldPbx.getPbxPreferences().setVmLocale(pbxPreferences.getVmLocale() != null ? pbxPreferences.getVmLocale() : Pbxpreference.DEFAULT_LOCALE) ;
		pbx = oldPbx;
		pbxDAO.save(pbx);
		Pbxpreference oldPreference = pbxPreferencesDAO.getByKey(pbx.getPbxPreferencesKey());
		oldPreference.setNightModeScheduler(pbxPreferences.getNightModeScheduler());
		oldPreference.setPrefix(pbxPreferences.getPrefix());
		pbxPreferencesDAO.save(oldPreference);
		
		Long defaultAddressKey = pbxInfo.getDefaultaddressKey();
		Address newDefaultAddress = addDAO.getByKey(defaultAddressKey);
		
		if(newDefaultAddress == null || !newDefaultAddress.getActive().equals(Address.DEFINE_ACTIVE))
		{
			StringBuilder message = new StringBuilder("DID ");
			if(newDefaultAddress!=null)
				message.append(newDefaultAddress.getAddress());
			message.append("can't set as a default address, because it not actived in the system ");
			throw new ObjectInconsistencyException(message.toString(),Address.class,newDefaultAddress.getAddress(), InconsistencyType.NOT_ACTIVED);
		}		
		
		Address defaultAddress = addDAO.getDefaultAddress(pbx.getKey());
		
		if(!domain.getKey().equals(newDefaultAddress.getDomainKey()))
		{
			Domain currentDomain = domainDAO.getByKey(newDefaultAddress.getDomainKey());
			StringBuilder message = new StringBuilder("DID ");
			message.append(newDefaultAddress.getAddress());
			message.append(" can't set as a default address, because it was moved from  domain ");
			message.append(domain.getDomain());
			message.append(" to Domain");
			message.append(currentDomain.getDomain());
			throw new ObjectInconsistencyException(message.toString(),Address.class,newDefaultAddress.getAddress(), InconsistencyType.DOMAIN_CHANGED);
		}
		
		//caso o tronco chave tenha sido modificado
		if(defaultAddress.getKey().longValue() != defaultAddressKey.longValue())
		{
			//remova a associacao de tronco chave do anterior
			defaultAddress.setPbxKey(null);
			addDAO.save(defaultAddress);
			//associe novo tronco chave
			Address address = addDAO.getByKey(defaultAddressKey);
			address.setPbxKey(pbx.getKey());			
			
			List<Pbxuser> pbxuserList = pbxuserDAO.getPbxuserListByDefaultDID(defaultAddress.getKey());
			for(Pbxuser pu : pbxuserList)
			{
				pu.setDefaultDIDKey(defaultAddressKey);
				pbxuserDAO.save(pu);
			}
			addDAO.save(address);
		}
		
		String voiceMailExtension = pbxInfo.getVoiceMailExtension();
		String recordBoxExtension = pbxInfo.getRecordBoxExtension();			
		updateRecordBoxAndVoicemail(voiceMailExtension, recordBoxExtension, domain.getKey());
		
		User musicServer = userDAO.getUsersInDomain(domain.getKey(), User.TYPE_MUSIC_SERVER).get(0);
		List<Userfile> ufList = ufDAO.getUserFileListByUser(musicServer.getKey());
		for(Userfile userfile : ufList)
			ufDAO.remove(userfile);
		if(pbxInfo.getMusicFileKey() != null)
		{
			Userfile uf = new Userfile();
			uf.setFileinfoKey(pbxInfo.getMusicFileKey());
			uf.setUserKey(musicServer.getKey());
			uf.setLastAccess(Calendar.getInstance());
			ufDAO.save(uf);
		}
		User parkServer = userDAO.getUsersInDomain(domain.getKey(), User.TYPE_PARK_SERVER).get(0);
		List<Userfile> ufParkList = ufDAO.getUserFileListByUser(parkServer.getKey());
		for(Userfile userfile : ufParkList)
			ufDAO.remove(userfile);
		if(pbxInfo.getParkFileKey() != null)
		{
			Userfile uf = new Userfile();
			uf.setFileinfoKey(pbxInfo.getParkFileKey());
			uf.setUserKey(parkServer.getKey());
			uf.setLastAccess(Calendar.getInstance());
			ufDAO.save(uf);
		}
	}
	
	private void updateRecordBoxAndVoicemail(String voiceMailExtension, String recordBoxExtension, Long domainKey) throws DAOException, ValidateObjectException
	{		
		DialPlan dialPlan = dPlanDAO.getDialPlanByTypeAndDomain(DialPlan.TYPE_EXTENSION, domainKey);
		
		if(voiceMailExtension != null && recordBoxExtension != null)
		{
			dPlanManager.validateDialPlanNumber(dialPlan, voiceMailExtension);
			dPlanManager.validateDialPlanNumber(dialPlan, recordBoxExtension);
			
			Address voiceMailAddress = addDAO.getVoicemailAddress(domainKey);
			Address recordBoxAddress = addDAO.getRecordFileBoxAddress(domainKey);
			
			if(voiceMailAddress != null && recordBoxAddress != null)
			{
				String oldVoicemail = voiceMailAddress.getAddress();
				String oldRecordBox = recordBoxAddress.getAddress();
				
				if(voiceMailExtension.equals(oldRecordBox) || recordBoxExtension.equals(oldVoicemail))
				{					
					voiceMailAddress.setActive(Address.DEFINE_DELETED);
					recordBoxAddress.setActive(Address.DEFINE_DELETED);
					addDAO.save(recordBoxAddress);
					addDAO.save(voiceMailAddress);
				}				
				recordBoxAddress.setAddress(recordBoxExtension);
				recordBoxAddress.setActive(Address.DEFINE_ACTIVE);
				addDAO.save(recordBoxAddress);

				voiceMailAddress.setAddress(voiceMailExtension);
				voiceMailAddress.setActive(Address.DEFINE_ACTIVE);
				addDAO.save(voiceMailAddress);
			}			
		}
		else if(voiceMailExtension != null)
		{			
			dPlanManager.validateDialPlanNumber(dialPlan, voiceMailExtension);		
			
			Address voiceMailAddress = addDAO.getVoicemailAddress(domainKey);
			if(voiceMailAddress != null)
			{
				voiceMailAddress.setAddress(voiceMailExtension);
				addDAO.save(voiceMailAddress);
			}
		}
		else if(recordBoxExtension != null)
		{
			dPlanManager.validateDialPlanNumber(dialPlan, recordBoxExtension);
			
			Address recordBoxAddress = addDAO.getRecordFileBoxAddress(domainKey);
			if(recordBoxAddress != null)
			{
				recordBoxAddress.setAddress(recordBoxExtension);			
				addDAO.save(recordBoxAddress);
			}
		}
		
	}
	
	//Início - vmartinez - version 3.0.5 - correção do bug #6150 - ao delatar um pbx, vai deletar a pasta física também.
	public void deletePBX(List<Long> pbxKeys) throws DAOException, ValidateObjectException, DeleteDependenceException, IOException
	{
		for(Long pbxKey : pbxKeys)
			this.deletePBX(pbxKey);
	}

	private void deletePBX(Long pbxKey) throws DAOException, ValidateObjectException, DeleteDependenceException, IOException
	{
		Pbx pbx = pbxDAO.getPbxFull(pbxKey);
		Domain domain = domainDAO.getDomainByPbx(pbx.getKey());

		List<Activecall> activeCallList = acDAO.getActivecallListByPBX(pbxKey);
		if(activeCallList != null && activeCallList.size() > 0)
			throw new DeleteDependenceException("Cannot delete PBX " + domain.getDomain() + ": " + activeCallList.size() + " active call(s) current in this momment!!!", Activecall.class, activeCallList.size(), domain);

		manageKoushiKubunConfiguration(Pbxpreference.KOUSHIKUBUN_ENABLE_OFF, domain.getKey());
		deleteGroups(pbx.getKey());
		deletePbxusers(domain.getKey());		
		deleteServiceClass(pbx.getKey());
		deleteIVRs(domain.getDomain());
		setAvaialableDIDs(domain.getKey(), domain.getRootKey());
		deleteFiles(domain.getKey());
		deleteTerminals(domain.getKey());
		deleteMediaAgents(domain.getKey());
		deleteFolderDomain(domain.getDomain());
		deleteNightmodeSchedulers(pbx.getKey());

		User agentUser = pbx.getUser();
		agentUser.setActive(User.DEFINE_DELETED);
		userDAO.save(agentUser);

		PBXDialPlanInfo pbxDialPlan = dPlanManager.getPBXDialPlan(pbxKey);
		dPlanDAO.remove(pbxDialPlan.getDefaultOperatorDialPlan());
		dPlanDAO.remove(pbxDialPlan.getExtensionDialPlan());
		dPlanDAO.remove(pbxDialPlan.getParkDialPlan());
		dPlanDAO.remove(pbxDialPlan.getSpeedDialDialPlan());
		dPlanDAO.remove(pbxDialPlan.getPublicSpeedDialDialPlan());
		
		Pbxpreference pbxPreferences = pbx.getPbxPreferences();
		pbxPreferencesDAO.remove(pbxPreferences);
		//Fim - vmartinez - version 3.0.5 - correção do bug #6150 - ao delatar um pbx, vai deletar a pasta física também.
		pbx.setActive(Pbx.DEFINE_DELETED);
		pbxDAO.save(pbx);

		domain.setActive(Domain.DEFINE_DELETED);
		domainDAO.save(domain);

		if(pbxPreferences.getAcdGroupEnable() == User.DEFINE_ACTIVE)
			JMSNotificationTools.getInstance().sendRemoveAcdGroupSchedulerMessage(domain.getKey());
	}
	
	private void deleteTerminals(Long domainKey) throws DAOException, ValidateObjectException
	{
		List<Terminal> terminalList = terminalDAO.getTerminalListByDomain(domainKey);
		if(terminalList != null)
			for(Terminal t : terminalList)
			{
				t.setActive(Terminal.DEFINE_DELETED);
				terminalDAO.save(t);

				Pbxuser pu = t.getPbxuser();
				if(pu != null)
				{
					pu.setActive(Pbxuser.DEFINE_DELETED);
					pbxuserDAO.save(pu);

					User u = pu.getUser();
					if(u != null)
					{
						u.setActive(User.DEFINE_DELETED);
						userDAO.save(u);
					}
				}
			}
	}
	
	private void deleteFiles(Long domainKey) throws DAOException, ValidateObjectException
	{
		List<Fileinfo> fileList = fileDAO.getFilesInDomain(domainKey);
		for(Fileinfo file : fileList)
		{
			List<Userfile> userFileList = ufDAO.getUserFileListByFile(file.getKey());
			for(Userfile uf : userFileList)
				ufDAO.remove(uf);
			
			List<Groupfile> groupFileList = groupFileDAO.getGroupFileListByFile(file.getKey());
			for(Groupfile gf : groupFileList)
				groupFileDAO.remove(gf);
			
			file.setActive(Fileinfo.DEFINE_DELETED);
			fileDAO.save(file);
		}
	}
	
	private void setAvaialableDIDs(Long domainKey, Long domainRootKey) throws DAOException, ValidateObjectException
	{
		List<Address> addressList = addDAO.getDIDsInDomainWithoutActive(domainKey);
		for(Address add : addressList)
		{
			add.setPbxuserKey(null);
			add.setGroupKey(null);
			add.setPbxKey(null);
			add.setDomainKey(domainRootKey);
			addDAO.save(add);
		}
	}
	
	private void deleteIVRs(String domainName) throws DAOException, ValidateObjectException
	{
		List<IVR> ivrList = ivrDAO.getIVRListByDomain(domainName);
		for(IVR ivr : ivrList)
		{
			ivr.setActive(IVR.DEFINE_DELETED);
			ivrDAO.save(ivr);
		}	
	}
	
	private void deleteServiceClass(Long pbxKey) throws DAOException, ValidateObjectException
	{
		List<Serviceclass> serviceClassList = serviceClassDAO.getServiceClassInPBX(pbxKey, true);
		for(Serviceclass sc : serviceClassList)
		{
			sc.setActive(Serviceclass.DEFINE_DELETED);
			serviceClassDAO.save(sc);
		}
	}
	//inicio --> dnakamashi - 5570 - 3.0.5
	private void deletePbxusers(Long domainKey) throws DAOException, ValidateObjectException
	{
		List<Pbxuser> pbxuserList = pbxuserDAO.getPbxuserAndUsersInDomain(domainKey);
		deleteUsers(pbxuserList);		
	}
	
	private void deleteNightmodeSchedulers(Long pbxKey) throws DAOException, ValidateObjectException
	{
		List<NightModeScheduler> nmslist = nightModeSchedulerDAO.getNightModeSchedulerByPbx(pbxKey);
		for(NightModeScheduler nms : nmslist)
			nightModeSchedulerDAO.remove(nms);
	}
	
	private void deleteMediaAgents(Long domainKey) throws DAOException, ValidateObjectException
	{
		List<Pbxuser> mediaAgentsList = pbxuserDAO.getMediaAgentsInDomain(domainKey);		
		deleteUsers(mediaAgentsList);		
	}
	
	private void deleteUsers(List<Pbxuser> userList) throws DAOException, ValidateObjectException
	{
		for(Pbxuser pu : userList)
		{
			pu.setActive(Pbxuser.DEFINE_DELETED);
			pbxuserDAO.save(pu);
			
			User u = pu.getUser();
			removeUserRoleDependences(u.getKey());
			u.setActive(User.DEFINE_DELETED);
			userDAO.save(u);
		}
	}
//fim --> dnakamashi - 5570 - 3.0.5
	private void removeUserRoleDependences(Long userKey) throws DAOException
	{
		List<Userrole> urList = userRoleDAO.getUserroleListByUser(userKey);
		for(Userrole ur : urList)
			userRoleDAO.remove(ur);
	}
	
	private void deleteGroups(Long pbxKey) throws DAOException, ValidateObjectException
	{
		List<Group> groupList = groupDAO.getGroupsInPBX(pbxKey);
		for(Group g : groupList)
		{
			g.setActive(Group.DEFINE_DELETED);
			groupDAO.save(g);
		}
	}
	
	//Metodo usado para edicao de PBX na parte do WBA/Config
	public PBXInfo getPBXInfo(Long pbxKey) throws DAOException
	{
		Pbx pbx = pbxDAO.getPbxFull(pbxKey);
		Domain domain = domainDAO.getDomainByPbx(pbxKey);
		pbx.setDomain(domain);
		Address address = addDAO.getDefaultAddress(pbxKey);
		Pbxuser pbxuserVoiceMail = pbxuserDAO.getPbxuserByAddressAndDomainKey(User.VOICEMAIL_NAME, domain.getKey());

		//setagem de todas as configuracoes que estao em uso no momento e podem ser configuradas aumentando/diminuindo seu valor.
		Long amountUsedUser = pbxuserDAO.countUsers(domain.getKey());
		Long amountUsedIVR = ivrDAO.countIVRs(domain.getKey());
		//Long amountPAsOnline = groupDAO.countPAsOnline(pbx.getKey());
		Double amountUsedQuota = fileDAO.countUsedQuota(domain.getKey());
		Integer amountUsedMaxConcurrentCalls = getMaxConcurrentCallsValue(pbx.getKey());
		Integer amountUsedMaxSipSession = getMaxSipSessionsValue(domain.getKey());

		String voiceMailExtension = null;
		if(pbxuserVoiceMail != null)
		{
			List<Address> addressList = addDAO.getExtensionListByPbxuser(pbxuserVoiceMail.getKey());
			voiceMailExtension = addressList != null && addressList.size() > 0 ? addressList.get(0).getAddress() : null;
		}	
		List<Duo<Long, String>> didOutList = addDAO.getDIDListByRootDomain(domain.getRootKey());
		
		setPBXDialPlan(pbx);
		
		PBXInfo pbxInfo = new PBXInfo(pbx, address.getKey(), voiceMailExtension, pbx.getUser().getFarmIP(), didOutList, amountUsedUser, amountUsedIVR, amountUsedQuota.floatValue(), amountUsedMaxConcurrentCalls, amountUsedMaxSipSession);
	    //pbxInfo.setAmountPAsOnline(amountPAsOnline);
		
		List addressList = ivrDAO.getAddressByIVRNameAndDomainName(domain.getDomain(), User.RECORDFILEBOX_NAME);
		if(addressList.size() > 0)
		{
			Address recordFileBoxAddress = getRecordFileBoxAddress(addressList);		
			String recordFileBoxExtension = recordFileBoxAddress != null ? recordFileBoxAddress.getAddress() : null;
			pbxInfo.setRecordBoxExtension(recordFileBoxExtension);
		}
		
		makeDIDInPBXList(domain.getKey(), 0, pbxInfo);
		
		loadMediaRelayIPList(pbxInfo);
		loadAudioEstablishmentTypeList(pbxInfo);
		
		return pbxInfo;
	}

	private void loadMediaRelayIPList(PBXInfo pbxInfo)
	{
		List<Duo<String, String>> mediaRelayIPList = new ArrayList<Duo<String,String>>(); 
		for(String mediaIp: Arrays.asList(IPXProperties.getProperty(IPXPropertiesType.RTPPROXY_SERVER_LIST).split(",")) )
		{
			mediaRelayIPList.add(new Duo<String,String>(mediaIp.trim(), mediaIp.trim()));
		}
		
		pbxInfo.setMediaRelayIPList(mediaRelayIPList);
	}
	
	private void loadAudioEstablishmentTypeList(PBXInfo pbxInfo)
	{
		int defaultAudioEstablishmentType = Integer.parseInt(IPXProperties.getProperty(IPXPropertiesType.DEFAULT_AUDIO_ESTABLISHMENT_TYPE));
		List<Duo<Integer, String>> audioEstablishmetnTypeList = new ArrayList<Duo<Integer, String>>();
		
		switch(defaultAudioEstablishmentType)
		{
			case Pbxpreference.AUDIO_ESTABLISHMENT_RELAY:
				audioEstablishmetnTypeList.add(new Duo<Integer, String>(Pbxpreference.AUDIO_ESTABLISHMENT_RELAY,"RELAY"));
				audioEstablishmetnTypeList.add(new Duo<Integer, String>(Pbxpreference.AUDIO_ESTABLISHMENT_AUTO, "AUTO"));
				audioEstablishmetnTypeList.add(new Duo<Integer, String>(Pbxpreference.AUDIO_ESTABLISHMENT_PEER_TO_PEER, "Peer To Peer"));
				break;
			case Pbxpreference.AUDIO_ESTABLISHMENT_AUTO:
				audioEstablishmetnTypeList.add(new Duo<Integer, String>(Pbxpreference.AUDIO_ESTABLISHMENT_AUTO, "AUTO"));
				audioEstablishmetnTypeList.add(new Duo<Integer, String>(Pbxpreference.AUDIO_ESTABLISHMENT_RELAY,"RELAY"));
				audioEstablishmetnTypeList.add(new Duo<Integer, String>(Pbxpreference.AUDIO_ESTABLISHMENT_PEER_TO_PEER, "Peer To Peer"));
				break;
			case Pbxpreference.AUDIO_ESTABLISHMENT_PEER_TO_PEER:
				audioEstablishmetnTypeList.add(new Duo<Integer, String>(Pbxpreference.AUDIO_ESTABLISHMENT_PEER_TO_PEER, "Peer To Peer"));
				audioEstablishmetnTypeList.add(new Duo<Integer, String>(Pbxpreference.AUDIO_ESTABLISHMENT_RELAY,"RELAY"));
				audioEstablishmetnTypeList.add(new Duo<Integer, String>(Pbxpreference.AUDIO_ESTABLISHMENT_AUTO, "AUTO"));
				break;
		}
		pbxInfo.setAudioEstablishmentTypeList(audioEstablishmetnTypeList);
	}
	
	private void setPBXDialPlan(Pbx pbx) throws DAOException
	{
		PBXDialPlanInfo pbxdiaDialPlanInfo = dPlanManager.getPBXDialPlan(pbx.getKey());
		pbx.getPbxPreferences().setExtensionDialPlan(pbxdiaDialPlanInfo.getExtensionDialPlan());
		pbx.getPbxPreferences().setDefaultOperatorDialPlan(pbxdiaDialPlanInfo.getDefaultOperatorDialPlan());
		pbx.getPbxPreferences().setParkDialPlan(pbxdiaDialPlanInfo.getParkDialPlan());
		pbx.getPbxPreferences().setUserSpeedDialDialPlan(pbxdiaDialPlanInfo.getSpeedDialDialPlan());
		pbx.getPbxPreferences().setPublicSpeedDialDialPlan(pbxdiaDialPlanInfo.getPublicSpeedDialDialPlan());
	}
	
	public DIDInPBXInfo getDIDInPBX(Long domainKey, Integer lastIndex) throws DAOException
	{
		//List<Address> didFullList = addDAO.getDIDsInDomain(domainKey);
		//List<Address> didList = addDAO.getPartialDIDsInDomain(domainKey, lastIndex, DIDInPBXInfo.RESULTS_PER_CONSULT);
		DIDInPBXInfo info = new DIDInPBXInfo();
		makeDIDInPBXList(domainKey, lastIndex, info);
//		for(Address add : didList)
//		{
//			if(add.getPbxKey() != null)
//				info.addDIDInPBX(new DIDInfo(add, add.getAddress(), true));
//			else
//				info.addDIDInPBX(new DIDInfo(add, add.getAddress(), false));
//		}
//		info.setHaveMoreDID(lastIndex + DIDInPBXInfo.RESULTS_PER_CONSULT >= didFullList.size() ? false : true);
		return info;
	}
	
	private void makeDIDInPBXList(Long domainKey, Integer lastIndex, PBXInfo info) throws DAOException
	{
		Long didAmount = addDAO.getCountDIDsInDomain(domainKey);
		List<Address> didList = addDAO.getPartialDIDsInDomain(domainKey, lastIndex, DIDInPBXInfo.RESULTS_PER_CONSULT);
		for(Address add : didList)
		{
			if(add.getPbxKey() != null)
				info.addDIDInPBX(new DIDInfo(add, add.getAddress(), true));
			else
				info.addDIDInPBX(new DIDInfo(add, add.getAddress(), false));
		}
		info.setHaveMoreDID((lastIndex + DIDInPBXInfo.RESULTS_PER_CONSULT) >= didAmount.intValue() ? false : true);
	}

	private Integer getMaxSipSessionsValue(Long domainKey) throws DAOException
	{
		Integer maxSipSessions = 0;
		List<Pbxuser> pbxuserList = pbxuserDAO.getPbxuserAndUsersInDomain(domainKey);
		for(Pbxuser pu : pbxuserList)
		{
			List<Sipsessionlog> pbxuserSipSessionList = sipSessionDAO.getActiveSipsessionlogListByPbxuser(pu.getKey());
			List<Sipsessionlog> terminalSipSessionList = sipSessionDAO.getActiveSipSessionLogListByTerminalOfPbxuser(pu.getKey());
			if((pbxuserSipSessionList.size() + terminalSipSessionList.size()) > maxSipSessions)
				maxSipSessions = pbxuserSipSessionList.size() + terminalSipSessionList.size();
		}
		return maxSipSessions;
	}
	
	private Integer getMaxConcurrentCallsValue(Long pbxKey) throws DAOException
	{
		Integer maxConcurrentCalls = 0;
		List<Serviceclass> scList = serviceClassDAO.getServiceClassInPBX(pbxKey, false);
		for(Serviceclass sc : scList)
		{
			Config config = configDAO.getByKey(sc.getConfigKey());
			if(config.getMaxConcurrentCalls() > maxConcurrentCalls)
				maxConcurrentCalls = config.getMaxConcurrentCalls();
		}
		return maxConcurrentCalls;
	}

	public void savePBX(PBXInfo pbxInfo) throws DAOException, ValidateObjectException, IOException, DeleteDependenceException, ValidationException
	{
		boolean edit = pbxInfo.getKey() != null;
		Domain domain = pbxInfo.getDomain();
		Pbx pbx = pbxInfo.getPbx();
		
		if(pbxInfo.isAllOptionsFeaturesEnable())
			pbxInfo.getPbxpreferences().setAllowedIVR(Pbxpreference.ALLOWED_IVR_ON);
		else
			pbxInfo.getPbxpreferences().setAllowedIVR(Pbxpreference.ALLOWED_IVR_OFF);		
			
		pbx.setPbxPreferences(pbxInfo.getPbxpreferences());

		saveDomain(domain);
		Long agentUserKey = createAgentUser(domain, pbxInfo.getFarmIP(), edit);
		savePBX(pbxInfo, agentUserKey); 
		savePBXPreferences(pbxInfo, pbxInfo.getPbxpreferences(), edit);
 
		//TODO flag colocada para quando o dominio possuir milhares de DIDs e caso nao se tenha alterado a lista nao precisar salva-los novamente
		if(pbxInfo.isDIDListChange())
			saveDIDList(pbxInfo.getDIDInKeyList(), domain.getKey());

		saveTrunkLine(pbxInfo.getDefaultaddressKey(), pbx.getKey(), domain.getKey(), edit);

		if(edit)
		{
			updateMediaAgentsFarmIPs(pbxInfo.getFarmIP(), domain.getKey());
			manageKoushiKubunConfiguration(pbxInfo.getKoushiKubunEnable(), domain.getKey());
			manageIVRConfiguration(pbxInfo.getIVREnable(), domain.getDomain());
		    //validação de agents de mídia, configurados via WBA - inicio
			Long serviceClassDefault = ((Serviceclass)serviceClassDAO.getDefaultServiceclass(domain.getKey())).getKey();
			manageCallCenterConfiguration(pbxInfo, domain.getKey(), serviceClassDefault);
			manageVoicemailConfiguration(pbxInfo.getVMEnable(), domain.getKey());
			//validação de agents de mídia, configurados via WBA - fim
		} else
		{
			List<Duo<Long, Long>> servicesClassKeyList = createServiceClassFull(pbx.getKey(), pbxInfo.getMaxConcurrentCalls());
			createUsers(pbxInfo, domain.getKey(), servicesClassKeyList.get(0).getFirst(), servicesClassKeyList.get(0).getSecond());
			setNightModeAndOperator(pbxInfo.getUsernameAdministrator(), pbx, domain.getKey());
			createFolderDomain(domain.getDomain());
		}
		//if(pbxInfo.getVMEnable() == Pbxpreferences.VM_ENABLE_ON && pbxInfo.getVoiceMailExtension() != null && pbxInfo.getVoiceMailExtension().length() > 0)
		setExtensionToVoicemail(pbxInfo.getVMEnable(), pbxInfo.getVoiceMailExtension(), domain, edit);		
		
		manageRecordFileBox(pbxInfo);	
		manageIVRPreProcessCall(pbxInfo);
	}
	
	private void validatePbxPreference(Pbxpreference newPreference, Pbxpreference oldPreference, PBXInfo pbxInfo) throws DAOException, ValidateObjectException
	{				
		//Se esta passando o IVR enable para OFF
		if(newPreference.getIvrEnable() == Pbxpreference.ALLOWED_IVR_OFF && oldPreference.getIvrEnable() == Pbxpreference.ALLOWED_IVR_ON)
		{
			List ivrList = ivrDAO.getIVRKeyAndNameList(pbxInfo.getDomain().getKey());		
			
			if(ivrList.size() > 0)
				throw new ValidateObjectException("Its impossible disabled IVR, because a IVR has already been created in the domain.", IVR.class, pbxInfo, ValidateType.DEPENDENCE);
		}
		
		if(newPreference.getAcdGroupEnable() == Pbxpreference.ACDGROUP_ENABLE_OFF && oldPreference.getAcdGroupEnable() == Pbxpreference.ACDGROUP_ENABLE_ON)
		{
			List groups = groupDAO.getGroupsInPBXByGroupType(pbxInfo.getKey(), Group.ACDCALLCENTER_GROUP);
			
			if(groups.size() > 0)
				throw new ValidateObjectException("Its impossible disabled Call Center, because a Call Center group has already been created in the domain.", Group.class, pbxInfo, ValidateType.DEPENDENCE);
		}
		
		List<CostCenter> costCenters = costCenterDAO.getByDomain(pbxInfo.getDomainKey());		
		
		if(newPreference.getCostCenterEnable() == Pbxpreference.COSTCENTER_OFF)
		{
			if(costCenters.size() > 0)
				throw new ValidateObjectException("Its impossible disabled CostCenter, because a CostCenter has already been created in the domain.", "costcenterenable",CostCenter.class, pbxInfo, ValidateType.DEPENDENCE);
		}else
		{
			if(oldPreference.getCostCenterEnable() == Pbxpreference.COSTCENTER_ON)
			{
				if(oldPreference.getCostCenterCodeDigits() > newPreference.getCostCenterCodeDigits())
					throw new ValidateObjectException("Cannot execute this method because new cost center digits value configured is lower than already configured!!!", "costCenterLength", newPreference.getClass(), newPreference.getCostCenterCodeDigits(), ValidateType.INVALID);
			}
			
			for(CostCenter costCenter : costCenters){
				if(costCenter.getCode().length() != newPreference.getCostCenterCodeDigits())
					throw new ValidateObjectException("Cannot execute this method because there already are cost centers codes with different sizes !!!", "costCenterLength", newPreference.getClass(), newPreference.getCostCenterCodeDigits(), ValidateType.LENGTH);
			}
		}
	}
	
	//inicio --> dnakamashi - Caixa de  Gravação- 3.0.5
	private void manageRecordFileBox(PBXInfo pbxInfo) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		IVR ivr = ivrDAO.getIVRByNameAndDomainName(pbxInfo.getDomain().getDomain(), User.RECORDFILEBOX_NAME);		
		if(pbxInfo.isRecordBoxEnable() && ivr == null)//Cria uma nova Caixa de Gravação		
			saveRecordFileBox(pbxInfo.getRecordBoxExtension(), pbxInfo);
		else if(!pbxInfo.isRecordBoxEnable() && ivr != null)//Deleta uma Caixa de Gravação
			deleteRecordFileBox(pbxInfo.getRecordBoxExtension(), pbxInfo, ivr.getKey());
		else if(pbxInfo.isRecordBoxEnable() && ivr != null)//Edita uma Caixa de Gravação
			editRecordFileBox(pbxInfo.getRecordBoxExtension(), ivr.getPbxuserKey());
	}
	
	private void saveRecordFileBox(String extension, PBXInfo pbxInfo) throws DAOException, ValidateObjectException
	{
		IVRManager manager = new IVRManager(logger.getName());		
		Domain domain = pbxInfo.getDomain();
		IVRInfo ivrInfo = new IVRInfo();
		ivrInfo.setDomainKey(domain.getKey());
		ivrInfo.getIVR().setType(IVR.TYPE_RECORDFILE);
		Long serviceclassKey = scDAO.getDefaultServiceclass(domain.getKey()).getKey(); //RecordBox deve ter valor default de classOfService. IVR é configurável.
		ivrInfo.setServiceClassKey(serviceclassKey);
		
		Address add = new Address();
		add.setAddress(extension);
		ivrInfo.getIVR().getPbxuser().getAddressList().add(add);
		ivrInfo.getIVR().getPbxuser().getUser().setUsername(User.RECORDFILEBOX_NAME);			
		manager.save(ivrInfo);				
	}
	
	private void manageIVRPreProcessCall(PBXInfo pbxInfo) throws DAOException, ValidateObjectException, DeleteDependenceException
	{		
		Domain domain = pbxInfo.getDomain();
		IVR ivr = ivrDAO.getIVRByNameAndDomainName(domain.getDomain(), User.IVR_PREPROCESSCALL);
		if (ivr == null)
		{
			if (pbxInfo.getEletronicLockEnable() == Pbxpreference.ELETRONICLOCK_ENABLE_ON || pbxInfo.getCostCenterEnable() != Pbxpreference.COSTCENTER_OFF)
				createIVRPreProcessCall(domain.getKey());
		}else
		{
			if (pbxInfo.getEletronicLockEnable() == Pbxpreference.ELETRONICLOCK_ENABLE_OFF && pbxInfo.getCostCenterEnable() == Pbxpreference.COSTCENTER_OFF)
				ivrManager.deleteIVR(ivr.getKey());
		}
	}

	private void createIVRPreProcessCall(Long domainKey) throws DAOException, ValidateObjectException{
		IVRInfo ivrInfo = new IVRInfo();
		ivrInfo.setDomainKey(domainKey);
		ivrInfo.getIVR().setType(IVR.TYPE_PREPROCESSCALL);
		ivrInfo.getIVR().getPbxuser().getUser().setUsername(User.IVR_PREPROCESSCALL);
		ivrManager.save(ivrInfo, true);
	}
	
	private void deleteRecordFileBox(String extension, PBXInfo pbxinInfo, Long ivrKey) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		IVRManager ivrManager = new IVRManager(logger.getName());		
		ivrManager.deleteIVR(ivrKey);
	}
	
	private void editRecordFileBox(String extension, Long pbxuserKey) throws DAOException, ValidateObjectException
	{
		List<Address> addressList = addDAO.getAddressFullListByPbxuser(pbxuserKey);
		if(addressList.size() <= 2 )
		{
			Address add = getRecordFileBoxAddress(addressList);
			add.setAddress(extension);
			addDAO.save(add);			
		}
		
	}	
	
	private Address getRecordFileBoxAddress(List<Address> addressList)
	{
		for(Address address : addressList)
		{			
			if(!(address.getAddress().equals(User.RECORDFILEBOX_NAME)))			
				return address;				
		}		
		return null;
	}
	//fim --> dnakamashi - Caixa de  Gravação- 3.0.5
	
	public void manageIVRConfiguration(Integer ivrEnable, String domainName) throws DAOException, ValidateObjectException
	{
		List<IVR> ivrList = ivrDAO.getIVRListByDomain(domainName);
		Integer active = ivrEnable.equals(Pbxpreference.IVR_ENABLE_OFF) ? IVR.DEFINE_DELETED : IVR.DEFINE_ACTIVE;
		for(IVR ivr : ivrList)
		{
			ivr.setActive(active);
			ivrDAO.save(ivr);
		}
	}
	
	private void manageCallCenterConfiguration(PBXInfo pbxInfo, Long domainKey, Long serviceClassDefault) throws DAOException, ValidateObjectException
	{
		manageAgentConfiguration(pbxInfo, domainKey, serviceClassDefault, User.TYPE_ACD_GROUP);
	}
	
	private void manageVoicemailConfiguration(Integer enable, Long domainKey) throws DAOException, ValidateObjectException
	{
		manageAgentConfiguration(enable, domainKey, User.TYPE_VOICEMAIL);
	}
	
	private void manageAgentConfiguration(PBXInfo pbxInfo, Long domainKey, Long serviceClassDefault, Integer type) throws DAOException, ValidateObjectException
	{
		Integer enable = pbxInfo.getAcdGroupEnable();
		List <Pbxuser> userList = pbxuserDAO.getCallCenterAgentToConfiguration(domainKey);
		
		if(enable == User.DEFINE_DELETED)
			JMSNotificationTools.getInstance().sendRemoveAcdGroupSchedulerMessage(domainKey);

		if (userList.isEmpty() && type == User.TYPE_ACD_GROUP && enable == User.DEFINE_ACTIVE)
		{
			PbxuserManager puManager = new PbxuserManager(logger.getName());
			List<Long> roleDefaultList = getRoleList(false);

			PbxuserInfo puInfo = new PbxuserInfo();
			puInfo.setDomainKey(domainKey);
			puInfo.setPbxKey(pbxInfo.getPbx().getKey());

			puInfo.setUsername(User.ACDGROUP_NAME);
			puInfo.setName(User.ACDGROUP_NAME);
			puInfo.setPassword(generateAgentPassword(User.ACDGROUP_NAME, pbxInfo.getDomain().getDomain()));
			puInfo.setServiceclassKey(serviceClassDefault);
			puInfo.setRoleKeyList(roleDefaultList);
			puInfo.setFarmIP(pbxInfo.getFarmIP());
			puInfo.setVoiceMailEnable(false);

			puManager.save(puInfo);
		}
		
		else
		{
			for(Pbxuser pu : userList)
			{
				pu.setActive(enable);
				pbxuserDAO.save(pu);
				
				User u = pu.getUser();
				u.setActive(enable);
				userDAO.save(u);
			}
		}
	}
	
	private void manageAgentConfiguration(Integer enable, Long domainKey, Integer type) throws DAOException, ValidateObjectException
	{
		List <Pbxuser> userList = pbxuserDAO.getMediaAgentToConfiguration(domainKey, type);
		
		for(Pbxuser pu : userList)
		{
			pu.setActive(enable);
			pbxuserDAO.save(pu);
			
			User u = pu.getUser();
			u.setActive(enable);
			userDAO.save(u);
		}
	}
	
	//inicio --> dnakamashi - bug #5489 - 3.0.2 RC Patch 4 
	private void manageKoushiKubunConfiguration(Integer koushikubunEnable, Long domainKey) throws DAOException, ValidateObjectException
	{
		List<Koushikubun> koushikubunList = koushikubunDAO.getListByDomain(domainKey);
		
		if(koushikubunEnable.equals(Pbxpreference.KOUSHIKUBUN_ENABLE_OFF))
			changeKoushiKubunActiveConfiguration(koushikubunList, Koushikubun.DEFINE_ACTIVE, Koushikubun.DEFINE_INACTIVE);		
		else
			changeKoushiKubunActiveConfiguration(koushikubunList, Koushikubun.DEFINE_INACTIVE, Koushikubun.DEFINE_ACTIVE);
	}
	
	private void changeKoushiKubunActiveConfiguration(List<Koushikubun> koushikubunList, Integer actualConfiguration, Integer newConfiguration) throws DAOException, ValidateObjectException
	{
		for(Koushikubun koushikubun : koushikubunList)
		{
			if(koushikubun.getActive() == actualConfiguration)
			{	
				koushikubun.setActive(newConfiguration);
				koushikubunDAO.save(koushikubun);
			}	
		}
	}
//	fim --> dnakamashi - 3.0.2 RC Patch 4 - bug #5489

	private void savePBXPreferences(PBXInfo pbxInfo, Pbxpreference pbxPreferences, boolean isEditing) throws DAOException, ValidateObjectException
	{
		
		if(pbxPreferences.getKey() != null)
		{			
			Pbxpreference pbxPreferencesTmp = pbxPreferencesDAO.getByKey(pbxPreferences.getKey());			
			validatePbxPreference(pbxPreferences, pbxPreferencesTmp, pbxInfo);			
			pbxPreferencesTmp.setIvrEnable(pbxPreferences.getIvrEnable());
			pbxPreferencesTmp.setLocale(pbxPreferences.getLocale());
			pbxPreferencesTmp.setMaxConcurrentCalls(pbxPreferences.getMaxConcurrentCalls());		
			pbxPreferencesTmp.setTerminalType(pbxPreferences.getTerminalType());
			pbxPreferencesTmp.setVmEnable(pbxPreferences.getVmEnable());
			pbxPreferencesTmp.setVmLocale(pbxPreferences.getVmLocale() != null ? pbxPreferences.getVmLocale() : Pbxpreference.DEFAULT_LOCALE);
			pbxPreferencesTmp.setMaxSipsessions(pbxPreferences.getMaxSipsessions());
			pbxPreferencesTmp.setCtiIntegrationEnable(pbxPreferences.getCtiIntegrationEnable());
			pbxPreferencesTmp.setKoushiKubunEnable(pbxPreferences.getKoushiKubunEnable());
			pbxPreferencesTmp.setBlindTransferOnHookEnable(pbxPreferences.getBlindTransferOnHookEnable());
			pbxPreferencesTmp.setScreenPopUpEnable(pbxPreferences.getScreenPopUpEnable());
			pbxPreferencesTmp.setVideoCallEnable(pbxPreferences.getVideoCallEnable());
			pbxPreferencesTmp.setAllowedIVR(pbxPreferences.getAllowedIVR());			
			pbxPreferencesTmp.setCostCenterCodeDigits(pbxPreferences.getCostCenterCodeDigits());
			pbxPreferencesTmp.setRecordCallEnable(pbxPreferences.getRecordCallEnable());
			pbxPreferencesTmp.setCostCenterEnable(pbxPreferences.getCostCenterEnable());
			pbxPreferencesTmp.setPrefix(pbxPreferences.getPrefix());
			pbxPreferencesTmp.setRecordFileBoxEnable(pbxPreferences.getRecordFileBoxEnable() != null ? pbxPreferences.getRecordFileBoxEnable() : Pbxpreference.RECORDFILEBOX_ENABLE_OFF);
			pbxPreferencesTmp.setEletronicLockEnable(pbxPreferences.getEletronicLockEnable() != null ? pbxPreferences.getEletronicLockEnable() : Pbxpreference.ELETRONICLOCK_ENABLE_OFF);
			pbxPreferencesTmp.setoptionsFeaturesEnable(pbxPreferences.getOptionsFeaturesEnable() != null ? pbxPreferences.getOptionsFeaturesEnable() : Pbxpreference.OPTIONS_FEATURES_ENABLE_OFF);
			pbxPreferencesTmp.setAcdGroupEnable(pbxPreferences.getAcdGroupEnable());
			pbxPreferencesTmp.setCtiServerEnable(pbxPreferences.getCtiServerEnable() != null ? pbxPreferences.getCtiServerEnable() : Pbxpreference.CTISERVER_ENABLE_OFF);
			pbxPreferencesTmp.setCtiServerAddress(pbxPreferences.getCtiServerAddress());
			pbxPreferencesTmp.setMediaRelayIP(pbxPreferences.getMediaRelayIP());
			pbxPreferencesTmp.setAudioEstablishmentType(pbxPreferences.getAudioEstablishmentType());
			pbxPreferencesDAO.save(pbxPreferencesTmp);
		} else
		{
			pbxPreferences.setPbxKey(pbxInfo.getPbx().getKey());
			if(pbxPreferences.getVmLocale() == null)
				pbxPreferences.setVmLocale(Pbxpreference.DEFAULT_LOCALE);
			if(pbxPreferences.getNightModeScheduler() == null)
				pbxPreferences.setNightModeScheduler(Pbxpreference.NIGHTMODE_SCHEDULER_OFF);
			pbxPreferencesDAO.save(pbxPreferences);
		}
		
		saveDialPlan(pbxPreferences, isEditing, pbxInfo.getPbx().getKey());
	}
	
	private void saveDialPlan(Pbxpreference pbxpreference, boolean isEditing, Long pbxKey) throws DAOException, ValidateObjectException
	{		
		DialPlan extensionDialPlanNew = pbxpreference.getExtensionDialPlan();
		DialPlan speedDialDialPlanNew = pbxpreference.getUserSpeedDialDialPlan();
		DialPlan publicSpeedDialDialPlanNew = pbxpreference.getPublicSpeedDialDialPlan();
		DialPlan defaultOperatorDialPlanNew = pbxpreference.getDefaultOperatorDialPlan();
		DialPlan parkDialPlanNew = pbxpreference.getParkDialPlan();
		
		PBXDialPlanInfo newPBXDialPlan = new PBXDialPlanInfo(speedDialDialPlanNew, publicSpeedDialDialPlanNew, parkDialPlanNew, defaultOperatorDialPlanNew, extensionDialPlanNew, pbxKey);		
		
		if(isEditing)
			dPlanManager.updatePBXDialPlan(newPBXDialPlan, pbxpreference.getKey());
		else
			dPlanManager.savePBXDialPlan(newPBXDialPlan, pbxKey, pbxpreference.getKey());
	}	

	
	//Início - vmartinez - version 3.0.5 - correção do bug #6150 - ao delatar um pbx, vai deletar a pasta física também.
	private void deleteFolderDomain(String domainName) throws IOException
    {
		String physDomain = System.getProperty("centrex.files.root") + File.separator + System.getProperty("centrex.files.base") + File.separator + convertDomainName(domainName);
    	File dirDomain  = new File(physDomain);
    	boolean success = deleteSubdirectory(dirDomain);
    	if(!success)
			throw new IOException("Could not delete domain dir " + physDomain);
    	
    }
    
    private boolean deleteSubdirectory(File dir)
    {
    	if (dir.isDirectory()) 
    	{
    		String[] subDirs = dir.list();
    		for (int i=0; i<subDirs.length; i++) 
    		{
	 
   		boolean success = deleteSubdirectory(new File(dir, subDirs[i]));
	    		if (!success) 
	    			return false;
    		}
    	}

		return dir.delete();    		
    }

	private void createFolderDomain(String domainName) throws IOException
	{
		boolean success = true;
		File file = null;
		String physDomain = System.getProperty("centrex.files.root") + File.separator + System.getProperty("centrex.files.base") + File.separator + convertDomainName(domainName);
		String[] dirs = new String[]
        {
			physDomain,
			physDomain + File.separator + Fileinfo.Constants.FILE_DIR,
			physDomain + File.separator + Fileinfo.Constants.VM_DIR,
			physDomain + File.separator + Fileinfo.Constants.VM_MESSAGE_DIR
		};
		//Fim - vmartinez - version 3.0.5 - correção do bug #6150 - ao delatar um pbx, vai deletar a pasta física também.
		for(int i = 0; i < dirs.length && success; i++)
		{
			file = new File(dirs[i]);
			success = file.mkdir();
		}
		if(!success)
			throw new IOException("Could not create dir " + file.getAbsoluteFile());
	}

	private String convertDomainName(String domainName)
	{
		return domainName.replaceAll("\\.", "_");
	}
	
	private List<Duo<Long, Long>> createServiceClassFull(Long pbxKey, Integer maxConcurrentCalls) throws DAOException, ValidateObjectException
	{
		List<Duo<Long, Long>> tmp = createClassOfService(Serviceclass.SERVICECLASS_NAME, maxConcurrentCalls < Config.DEFAULT_MAXCONCURRENTCALLS_SERVICECLASS ? maxConcurrentCalls : Config.DEFAULT_MAXCONCURRENTCALLS_SERVICECLASS, Serviceclass.SC_DEFAULT_OFF, pbxKey);
		Long configCommonKey = tmp.get(0).getFirst();
		Long serviceClassCommonKey = tmp.get(0).getSecond();
		Long blockIncomingKey1 = createBlock(Serviceclass.SERVICECLASS_NAME, pbxKey, Block.TYPE_INCOMING);
		Long blockOutgoingKey1 = createBlock(Serviceclass.SERVICECLASS_NAME, pbxKey, Block.TYPE_OUTGOING);
		createConfigBlock(configCommonKey, blockIncomingKey1, blockOutgoingKey1);

		tmp = createClassOfService(Serviceclass.SERVICECLASSDEFAULT_NAME, Config.DEFAULT_MAXCONCURRENTCALLS_SERVICECLASSDEFAULT, Serviceclass.SC_DEFAULT_ON, pbxKey);
		Long configDefaultKey = tmp.get(0).getFirst();
		Long serviceClassDefaultKey = tmp.get(0).getSecond();
		Long blockIncomingKey2 = createBlock(Serviceclass.SERVICECLASSDEFAULT_NAME, pbxKey, Block.TYPE_INCOMING);
		Long blockOutgoingKey2 = createBlock(Serviceclass.SERVICECLASSDEFAULT_NAME, pbxKey, Block.TYPE_OUTGOING);
		createConfigBlock(configDefaultKey, blockIncomingKey2, blockOutgoingKey2);
		List<Duo<Long, Long>> list = new ArrayList<Duo<Long,Long>>();
		list.add(new Duo<Long, Long>(serviceClassCommonKey, serviceClassDefaultKey));
		return list;
	}

	private void updateMediaAgentsFarmIPs(String farmIP, Long domainKey) throws DAOException, ValidateObjectException
	{
		List<User> userList = userDAO.getMediaAgentsList(domainKey);
		for(User user : userList)
		{
			user.setFarmIP(farmIP);
			userDAO.save(user);
		}
	}

	private void setExtensionToVoicemail(Integer vmEnable, String extension, Domain domain, boolean edit) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		Pbxuser pbxuserVoiceMail = pbxuserDAO.getPbxuserByAddressAndDomainKey(User.VOICEMAIL_NAME, domain.getKey());
		if(vmEnable == Pbxpreference.VM_ENABLE_ON && pbxuserVoiceMail == null) //caso o dominio nao tenha usuario voicemail e queira ter, ele sera criado agora
		{
			createVoiceMailUser(domain);
			setVoicemailEnableToUsers(domain.getKey());
			pbxuserVoiceMail = pbxuserDAO.getPbxuserByAddressAndDomain(User.VOICEMAIL_NAME, domain.getDomain());
		} else if(vmEnable == Pbxpreference.VM_ENABLE_OFF && pbxuserVoiceMail != null) //caso de um pbx que tinha voicemail e agora nao quer mais ter
		{
			PbxuserManager puManager = new PbxuserManager(logger.getName());
			puManager.deletePbxuser(pbxuserVoiceMail.getKey(), false);
			removeAllVoicemailAddress(pbxuserVoiceMail.getKey(), domain.getDomain());
		}
		Address address = null;
		if(edit && vmEnable == Pbxpreference.VM_ENABLE_ON)
		{
			List<Address> extensionList = addDAO.getExtensionListByPbxuser(pbxuserVoiceMail.getKey());
			if(extensionList != null && extensionList.size() == 1)
			{
				address = extensionList.get(0);
				if(!address.getAddress().equals(extension))
				{
					addDAO.remove(address);
					address = null;
				}
			}
		}
		if(address == null)
			address = new Address();

		if(vmEnable == Pbxpreference.VM_ENABLE_ON)
		{
			address.setAddress(extension);
			address.setDomainKey(domain.getKey());
			address.setPbxuserKey(pbxuserVoiceMail.getKey());
			address.setStaticaddress(Address.STATIC_ON);
			address.setType(Address.TYPE_EXTENSION);
			address.setActive(Address.DEFINE_ACTIVE);
			addDAO.save(address);
		}
	}
	
	private void setVoicemailEnableToUsers(Long domainKey) throws DAOException, ValidateObjectException
	{
		List<Config> configList = configDAO.getConfigListByUsersInDomain(domainKey);
		if(configList != null && configList.size() > 0)
			for(Config conf : configList)
			{
				conf.setDisableVoicemail(Config.VOICEMAIL_ON);
				configDAO.save(conf);
			}
	}
	
	private void removeAllVoicemailAddress(Long pbxuserVoicemailKey, String domainName) throws DAOException, ValidateObjectException
	{
		Address sipID = addDAO.getSipIDByPbxuser(pbxuserVoicemailKey);
		Long sipIDKey = sipID.getKey();

		//verifica a existencia de forwards setados para o address que esta sendo removido.
		List<Forward> forwardList = forwardDAO.getForwardListByAddress(sipIDKey);
		for(Forward f : forwardList)
		{
			f.setAddressKey(null);
			f.setStatus(Forward.STATUS_OFF);
			forwardDAO.save(f);
		}

		//verifica a existencia de nightmode de grupos relacionados com o address que esta sendo removido.
		List<Group> groupList = groupDAO.getGroupListByNightmodeAddress(sipIDKey);
		for(Group g : groupList)
		{
			g.setNightmodeaddressKey(null);
			g.setNightmodeStatus(Group.NIGHTMODE_OFF);
			groupDAO.save(g);
		}

		//verifica a existencia de forwards de IVR para o address que esta sendo removido
		//TODO Verificar se pode deixar este campo na tabela IVR vazio para ver se nao ir� quebrar no PBX.
		List<IVR> ivrList = ivrDAO.getIVRsByAddress(sipIDKey);
		for(IVR ivr : ivrList)
		{
			ivr.setSalutationForwardAddressKey(null);
			ivr.setEotForwardAddressKey(null);
			ivrDAO.save(ivr);
		}

		List<IVROption> ivrOptionList = ivrOptionDAO.getIVROptionListByAddress(sipIDKey);
		for(IVROption ivrOption : ivrOptionList)
			ivrOptionDAO.remove(ivrOption);

		Pbx pbx = pbxDAO.getPbxByDomain(sipID.getDomainKey());
		//verifica se o address que esta sendo removido eh nightmode do pbx
		if(pbx.getNightmodeaddressKey()!= null && pbx.getNightmodeaddressKey().longValue() == sipIDKey.longValue())
		{
			pbx.setNightmodeaddressKey(null);
			pbx.setNightMode(Pbx.NIGHTMODE_OFF);
			pbxDAO.save(pbx);
		}	

//		//verifica se o address que esta sendo removido eh default operator do pbx		
//		if(pbx.getDefaultaddressKey().longValue() == sipIDKey.longValue())
//		{
//			Pbxuser defaultOperatorUser = pbxuserDAO.getPbxuserByUsernameAndDomain(User.OPERATOR_NAME, domainName);
//			Address add = null;
//			if(defaultOperatorUser != null)
//				add = addDAO.getAddress(User.OPERATOR_NAME, domainName);
//			pbx.setDefaultaddressKey(add.getKey());
//			pbxDAO.save(pbx);
//		}

		//verifica se o address que esta sendo removido esta em ligacao no momento(possui active call)
		List<Activecall> activeCallList = acDAO.getActiveCallListByPbxuser(sipID.getPbxuserKey());
		for(Activecall ac : activeCallList)
			acDAO.remove(ac);

		addDAO.remove(sipID);

		List<Address> addList = addDAO.getExtensionListByPbxuser(pbxuserVoicemailKey);
		for(Address add : addList)
			addDAO.remove(add);
		addList = addDAO.getDIDListByPbxuserWithoutActive(pbxuserVoicemailKey);
		for(Address add : addList)
		{
			add.setPbxuserKey(null);
			add.setGroupKey(null);
			addDAO.save(add);
		}
	}

	private void createVoiceMailUser(Domain domain) throws DAOException, ValidateObjectException
	{
		PbxuserManager puManager = new PbxuserManager(logger.getName());
		PbxuserInfo puInfo = new PbxuserInfo();
		puInfo.setDomainKey(domain.getKey());
		puInfo.setUsername(User.VOICEMAIL_NAME);
		puInfo.setName(User.VOICEMAIL_NAME);
		//TODO ver como vai ficar a senha do voicemail, se vai ser um algoritmo que sempre gere uma randomica
		puInfo.setPassword(Crypt.encrypt(User.VOICEMAIL_NAME, domain.getDomain(), User.VOICEMAIL_PASS));
		puInfo.setServiceclassKey(serviceClassDAO.getDefaultServiceclass(domain.getKey()).getKey());
		puInfo.setRoleKeyList(getRoleList(false));
		puInfo.setFarmIP(userDAO.getUserByUsernameAndDomain(User.MUSICSERVER_NAME, domain.getDomain()).getFarmIP());
		puInfo.setPbxKey(pbxDAO.getPbxByDomain(domain.getKey()).getKey());
		puManager.save(puInfo);
	}
	
	private void setNightModeAndOperator(String usernameAdmnistrator, Pbx pbx, Long domainKey) throws DAOException, ValidateObjectException
	{
		String username = usernameAdmnistrator != null && usernameAdmnistrator.length() > 0 ? usernameAdmnistrator : User.ADMINISTRATOR_NAME; 
		Address addressAdministrator = addDAO.getAddress(username, domainKey);
		pbx.setNightmodeaddressKey(addressAdministrator.getKey());
		pbx.setDefaultaddressKey(addressAdministrator.getKey());
		pbxDAO.save(pbx);
	}
	
	private List<Long> getRoleList(boolean admin) throws DAOException
	{
		List<Long> roleList = new ArrayList<Long>();
		if(admin)
			roleList.add(roleDAO.getAdminRole().getKey());
		else
			roleList.add(roleDAO.getDefaultRole().getKey());
		return roleList;
	}
	
	private String generateAgentPassword(String agentName, String domainName) throws DAOException
	{
		Domain rootDomain = domainDAO.getRootDomain();
		StringBuilder pass = new StringBuilder();
		pass.append(agentName);
		pass.append(domainName);
		pass.append(rootDomain.getDomain());
		return Crypt.encrypt(agentName, domainName, pass.toString());
	}
	
	private void createUsers(PBXInfo pbxInfo, Long domainKey, Long serviceClass, Long serviceClassDefault) throws DAOException, ValidateObjectException
	{
		PbxuserManager puManager = new PbxuserManager(logger.getName());
		List<Long> roleAdminList = getRoleList(true);
		List<Long> roleDefaultList = getRoleList(false);
		int i = 0;
		int finalValue = 5;
		if(pbxInfo.getVMEnable() == Pbxpreference.VM_ENABLE_OFF)
			i = 1;
		if(pbxInfo.getAcdGroupEnable() == Pbxpreference.ACDGROUP_ENABLE_OFF)
			finalValue = 4;
		for(; i < finalValue; i++)
		{
			PbxuserInfo puInfo = new PbxuserInfo();
			puInfo.setDomainKey(domainKey);
			puInfo.setPbxKey(pbxInfo.getPbx().getKey());
			switch(i)
			{
				case 0: // voicemail
					puInfo.setUsername(User.VOICEMAIL_NAME);
					puInfo.setName(User.VOICEMAIL_NAME);
					puInfo.setPassword(generateAgentPassword(User.VOICEMAIL_NAME, pbxInfo.getDomain().getDomain()));
					puInfo.setServiceclassKey(serviceClassDefault);
					puInfo.setRoleKeyList(roleDefaultList);
					puInfo.setFarmIP(pbxInfo.getFarmIP());
					puInfo.setVoiceMailEnable(false);
					break;

				case 1: // administrator
					String username = pbxInfo.getUsernameAdministrator() != null && pbxInfo.getUsernameAdministrator().length() > 0 ? pbxInfo.getUsernameAdministrator() : User.ADMINISTRATOR_NAME; 
					puInfo.setUsername(username);
					puInfo.setName(username);
					puInfo.setPassword(Crypt.encrypt(username, pbxInfo.getDomain().getDomain(), pbxInfo.getPasswordAdministrator()));
					puInfo.setServiceclassKey(serviceClass);
					puInfo.setRoleKeyList(roleAdminList);
					puInfo.setPin(Crypt.encrypt(Pbxuser.PIN_DEFAULT));
					puInfo.setLocale(pbxInfo.getVMLocale());
					break;

				case 2: //musicServer
					puInfo.setUsername(User.MUSICSERVER_NAME);
					puInfo.setName(User.MUSICSERVER_NAME);
					puInfo.setPassword(generateAgentPassword(User.MUSICSERVER_NAME, pbxInfo.getDomain().getDomain()));
					puInfo.setServiceclassKey(serviceClassDefault);
					puInfo.setRoleKeyList(roleDefaultList);
					puInfo.setFarmIP(pbxInfo.getFarmIP());
					puInfo.setVoiceMailEnable(false);
					break;

				case 3: //parkServer
					puInfo.setUsername(User.PARKSERVER_NAME);
					puInfo.setName(User.PARKSERVER_NAME);
					puInfo.setPassword(generateAgentPassword(User.PARKSERVER_NAME, pbxInfo.getDomain().getDomain()));
					puInfo.setServiceclassKey(serviceClassDefault);
					puInfo.setRoleKeyList(roleDefaultList);
					puInfo.setFarmIP(pbxInfo.getFarmIP());
					puInfo.setVoiceMailEnable(false);
					break;
					
				case 4: //acdGroupServer
					puInfo.setUsername(User.ACDGROUP_NAME);
					puInfo.setName(User.ACDGROUP_NAME);
					puInfo.setPassword(generateAgentPassword(User.ACDGROUP_NAME, pbxInfo.getDomain().getDomain()));
					puInfo.setServiceclassKey(serviceClassDefault);
					puInfo.setRoleKeyList(roleDefaultList);
					puInfo.setFarmIP(pbxInfo.getFarmIP());
					puInfo.setVoiceMailEnable(false);
					break;
					
			}
			puManager.save(puInfo);
		}
	}
	
	private void createConfigBlock(Long configKey, Long blockIncomingKey, Long blockOutgoingKey) throws DAOException, ValidateObjectException
	{
		Configblock configBlock = new Configblock();
		configBlock.setConfigKey(configKey);
		configBlock.setBlockKey(blockIncomingKey);
		configBlockDAO.save(configBlock);
		
		configBlock = new Configblock();
		configBlock.setConfigKey(configKey);
		configBlock.setBlockKey(blockOutgoingKey);
		configBlockDAO.save(configBlock);
	}
	
	private List<Duo<Long, Long>> createClassOfService(String name, Integer defaultMaxConcurrentCalls, Integer scDefault, Long pbxKey) throws DAOException, ValidateObjectException
	{
		Long configKey = createConfig(Config.DEFAULT_TIMEOUT, defaultMaxConcurrentCalls);
		Serviceclass serviceClass = new Serviceclass();
		serviceClass.setName(name);
		serviceClass.setPbxKey(pbxKey);
		serviceClass.setScDefault(scDefault);
		serviceClass.setConfigKey(configKey);
		serviceClass.setActive(Serviceclass.DEFINE_ACTIVE);
		serviceClass.setScreenPopUpType(Serviceclass.SCREENPOPUP_TYPE_WINDOW);
		serviceClass.setKeepDestinationNumber(Serviceclass.KEEP_DEST_NUMBER_OFF);
		serviceClassDAO.save(serviceClass);
		List<Duo<Long, Long>> list = new ArrayList<Duo<Long,Long>>();
		list.add(new Duo<Long, Long>(configKey, serviceClass.getKey()));
		return list;
	}
	
	private Long createBlock(String name, Long pbxKey, Integer type) throws DAOException, ValidateObjectException
	{
		Block block = new Block();
		block.setName(name);
		block.setPbxKey(pbxKey);
		block.setBlockNOID(Block.TYPE_BLOCKNOID_OFF);
		block.setStatus(Block.STATUS_ACTIVE);
		block.setBlockType(type);
		block.setActive(Block.DEFINE_ACTIVE);
		blockDAO.save(block);
		return block.getKey();
	}
	
	private Long createConfig(Integer timeoutCall, Integer maxConcurrentCalls) throws DAOException, ValidateObjectException
	{
		Config config = new Config();
		config.setDisableVoicemail(Config.VOICEMAIL_OFF);
		config.setEmailNotify(Config.EMAILNOTIFY_OFF);
		config.setDndStatus(Config.DND_OFF);
		config.setMaxConcurrentCalls(maxConcurrentCalls);
		config.setTimeoutcall(timeoutCall);
		config.setAttachFile(Config.ATTACH_FILE_OFF);
		config.setForwardType(Config.FORWARD_TYPE_DEFAULT);
		config.setActive(Config.DEFINE_ACTIVE);
		config.setAllowedGroupForward(Config.NOT_ALLOWED_GROUPFORWARD);
		config.setAllowedKoushiKubun(Config.NOT_ALLOWED_KOUSHIKUBUN);
		config.setAllowedVoiceMail(Config.NOT_ALLOWED_VOICEMAIL);
		configDAO.save(config);
		return config.getKey();
	}

	private void saveDIDList(List<Long> didList, Long domainKey) throws DAOException, ValidateObjectException
	{
		List<Address> didOldList = addDAO.getDIDsInDomain(domainKey);
		for(int i = 0; i < didOldList.size(); i++)
		{
			boolean tmp = false;
			for(int j = 0; j < didList.size(); j++)
			{
				if(didOldList.get(i).getKey().equals(didList.get(j)))
				{
					tmp = true;
					break;
				}
			}
			if(!tmp)
			{
				Address add = didOldList.get(i);
				add.setDomainKey(domainDAO.getRootDomain().getKey());
				add.setPbxuserKey(null);
				add.setGroupKey(null);
				add.setPbxKey(null);
				addDAO.save(add);
			}
		}
		for(Long didKey : didList)
		{
			Address address = addDAO.getByKey(didKey);
			if(address != null)
			{
				if(!address.getDomainKey().equals(domainKey))
				{
					address.setGroupKey(null);
					address.setPbxuserKey(null);
				}
				address.setPbxKey(null);
				address.setDomainKey(domainKey);
				addDAO.save(address);
			}
		}
	}

	private void saveTrunkLine(Long addressKey, Long pbxKey, Long domainKey, boolean edit) throws DAOException, ValidateObjectException
	{
		if(edit)
		{
			Address address = addDAO.getDefaultAddress(pbxKey);
			if(address != null)
			{
				address.setPbxKey(null);
				address.setPbxuser(null);
				address.setGroupKey(null);
				addDAO.save(address);
			}	
		}
		Address address = addDAO.getByKey(addressKey);
		if(address != null)
		{
			address.setPbxKey(pbxKey);
			address.setPbxuserKey(null);
			address.setGroupKey(null);
			address.setDomainKey(domainKey);
			addDAO.save(address);
		}
	}
	
	private void savePBX(PBXInfo pbxInfo, Long agentUserKey) throws DAOException, ValidateObjectException, ValidationException
	{
		Pbx pbx = pbxInfo.getPbx();
		Long validate = pbxDAO.validateAccoundID(pbx.getAccountId(), pbx.getKey() != null ? pbx.getKey() : null);
		if(validate > 0)
			throw new ValidateObjectException("Account Id already in use! Please choose another one.", "accountId", Pbx.class, pbx, ValidateType.DUPLICATED);
		
		validateSettings(pbxInfo);
		
		if(pbx.getKey() != null)
		{
			Pbx pbxTmp = pbxDAO.getByKey(pbx.getKey());
			pbxTmp.setAccountId(pbx.getAccountId());
			pbxTmp.setOperator(pbx.getOperator());
			pbxTmp.setFailureForward(pbx.getFailureForward());
			pbxTmp.setMaxUser(pbx.getMaxUser());
			pbxTmp.setMaxIVRApplication(pbx.getMaxIVRApplication());
			pbxTmp.setMaxRecordCallUsers(pbx.getMaxRecordCallUsers());
			pbxTmp.setMaxVideoCallUsers(pbx.getMaxVideoCallUsers());
			pbxTmp.setQuota(pbx.getQuota());
			pbxTmp.setMaxMessageTime(pbx.getMaxMessageTime());
			pbxTmp.setOutboundProxyPort(pbx.getOutboundProxyPort());
			pbxTmp.setActive(Pbx.DEFINE_ACTIVE);
			pbxTmp.setPbxPreferences(null);
			pbxTmp.setPbxPreferencesKey(null);
			pbxTmp.setMaxPAsOnlineNumber(pbx.getMaxPAsOnlineNumber());
			
			validateRecordingCall(pbxInfo);
			validateVideoCall(pbxInfo);
			pbxDAO.save(pbxTmp);			
		} else
		{	
			pbx.setUserKey(agentUserKey);
			pbx.setActive(Pbx.DEFINE_ACTIVE);
			pbx.setNightMode(Pbx.NIGHTMODE_OFF);
			pbxDAO.save(pbx);
		}
	}

	private void validateRecordingCall(PBXInfo info) throws DAOException, ValidateObjectException
	{
		if(info.getRecordCallEnable() == Pbxpreference.RECORDCALL_ENABLE_ON)
		{
			if(info.getMaxRecordCallUsers() == 0)
				info.setRecordCallEnable(Pbxpreference.RECORDCALL_ENABLE_OFF);
			else
			{
				Long count =  pbxuserDAO.countPbxuserRecordCall(info.getDomainKey());
				if(count > info.getMaxRecordCallUsers())
					throw new ValidateObjectException("Record Call Max users Error: There is already a number of users larger than the configured!", Pbx.class, info.getMaxRecordCallUsers(), ValidateType.MIN_NUMBER);
			}
		}
		else if(info.getRecordCallEnable() == Pbxpreference.RECORDCALL_ENABLE_OFF)
		{
			Long count =  pbxuserDAO.countPbxuserRecordCall(info.getDomainKey());
			if(count > 0)
				throw new ValidateObjectException("Record Call Max users Error: There are users using this feature in the domain!", Pbx.class, info.getMaxRecordCallUsers(), ValidateType.NUMBER);
			
//			disableAllRecordCallUsers(info.getKey());
//			info.setMaxRecordCallUsers(0);
		}
	}
	
	private void validateVideoCall(PBXInfo info) throws DAOException, ValidateObjectException
	{
		if(info.getVideoCallEnable() == Pbxpreference.VIDEOCALL_ENABLE_ON)
		{
			if(info.getMaxVideoCallUsers() == 0)
				info.setVideoCallEnable(Pbxpreference.VIDEOCALL_ENABLE_OFF);
			else
			{
				Long count =  pbxuserDAO.countPbxuserVideoCall(info.getDomainKey());
				if(count > info.getMaxVideoCallUsers())
					throw new ValidateObjectException("Video Call Max users Error: There is already a number of users larger than the configured!", Pbx.class, info.getMaxVideoCallUsers(), ValidateType.MIN_NUMBER_VIDEO);
			}
		}
		else if(info.getVideoCallEnable() == Pbxpreference.VIDEOCALL_ENABLE_OFF)
		{
			Long count =  pbxuserDAO.countPbxuserVideoCall(info.getDomainKey());
			if(count > 0)
				throw new ValidateObjectException("Video Call Max users Error: There are users using this feature in the domain!", Pbx.class, info.getMaxVideoCallUsers(), ValidateType.NUMBER_VIDEO);
		}
	}
	
	private void disableAllRecordCallUsers(Long pbxKey) throws DAOException, ValidateObjectException
	{
		List<Pbxuser> list = pbxuserDAO.getPbxusersByAllowedRecordCall(pbxKey, Config.ALLOWEDRECORDCALL_ON, null);
		
		for(Pbxuser pu : list)
		{
			Config config = pu.getConfig();
			config.setAllowedRecordCall(Config.ALLOWEDRECORDCALL_OFF);
			configDAO.save(config);
		}		
	}
	
	private void validateSettings(PBXInfo pbxInfo) throws ValidationException, NumberFormatException, ValidateObjectException, DAOException
	{			
		dPlanManager.validateDialPlan(pbxInfo.getExtensionDialPlan());
		
		dPlanManager.validateDialPlanNumber(pbxInfo.getDefaultOperatorDialPlan(), pbxInfo.getOperator());		
		if(pbxInfo.getVMEnable() == Pbxpreference.VM_ENABLE_ON)
			dPlanManager.validateDialPlanNumber(pbxInfo.getExtensionDialPlan(), pbxInfo.getVoiceMailExtension());
		if(pbxInfo.getRecordFileBoxEnable() == Pbxpreference.RECORDFILEBOX_ENABLE_ON)
			dPlanManager.validateDialPlanNumber(pbxInfo.getExtensionDialPlan(), pbxInfo.getRecordBoxExtension());		
		
		if(pbxInfo.getPbx().getKey() != null)
		{
			Pbx pbxNew = pbxInfo.getPbx();
			if(pbxNew.getMaxIVRApplication() < pbxInfo.getAmountUsedIVR())
				throw new ValidationException("Cannot execute this method because new maxIVRApplications value configured is lower than already configured!!!", CallStateEvent.NOT_FOUND);

			if(pbxNew.getMaxUser() < pbxInfo.getAmountUsedUser())
				throw new ValidationException("Cannot execute this method because new maxUsers value configured is lower than already configured!!!", CallStateEvent.NOT_FOUND);

			if(pbxNew.getQuota() < pbxInfo.getAmountUsedQuota())
				throw new ValidationException("Cannot execute this method because new Quota value configured is lower than already configured!!!", CallStateEvent.NOT_FOUND);
		}
	}

	private Long createAgentUser(Domain domain, String farmIP, boolean edit) throws DAOException, ValidateObjectException
	{
		User user;
		if(edit)
		{
			user = userDAO.getUserByUsernameAndDomain(User.AGENTUSER_NAME, domain.getDomain());
			if(!user.getFarmIP().equals(farmIP))
				JMSNotificationTools.getInstance().sendRemoveAcdGroupSchedulerMessage(domain.getKey());
		}
		else
			user = new User();
		user.setUsername(User.AGENTUSER_NAME);
		user.setPassword(Crypt.encrypt(User.AGENTUSER_NAME, domain.getDomain(), User.AGENTUSER_PASS));
		user.setAgentUser(User.TYPE_PBX);
		user.setDomain(null);
		user.setDomainKey(domain.getKey());
		user.setFarmIP(farmIP);
		user.setActive(User.DEFINE_ACTIVE);
		userDAO.save(user);
		return user.getKey();
	}
	
	private void saveDomain(Domain domain) throws DAOException, ValidateObjectException
	{
		Domain rootDomain = domainDAO.getRootDomain();
		domain.setRootKey(rootDomain.getKey());
		domain.setActive(Domain.DEFINE_ACTIVE);		
		domainDAO.save(domain);
	}
	
	public List<PBXInfo> getAllPbxInfoList() throws DAOException 
	{
		List<PBXInfo> pbxInfoList = new ArrayList<PBXInfo>();
		List<Pbx> pbxList;
		pbxList = pbxDAO.getAllPbxList();
		for(Pbx pbx : pbxList)
		{
			PBXInfo pbxInfo = getPBXInfoByKey(pbx.getKey());
			pbxInfoList.add(pbxInfo);
		}
    	return pbxInfoList;
	}
	
	public List<Pbx> getAllPbxWithPbxPreference() throws DAOException
	{
		return pbxDAO.getAllPbxWithPbxPreference();
	}

	public List<Pbx> getPbxListByFarmIP(String farmIP) throws DAOException
	{
		return pbxDAO.getPbxListByFarmIP(farmIP);
	}

	public void clearPBX(Long pbxKey) throws DAOException, ValidateObjectException
	{
		//limpa as active calls que podem ter ficado gravadas
		//por causa de uma interrupção brusca no serviço
		List<Activecall> acList = acDAO.getActivecallListByPBX(pbxKey);
		for(Activecall ac : acList)
			acDAO.remove(ac);
		
		//Fecha calllogs abertos
		for(Calllog log:clDAO.getCalllogListByPbxKey(pbxKey))
		{
			log.setUntildate(log.getFromdate());
			clDAO.save(log);
		}
	}

	public List<Duo<Long, String>> getDomainsByRootDomain(Long domainRootKey) throws DAOException
	{
		List<Duo<Long, String>> domainList = domainDAO.getDomainsByRootDomain(domainRootKey);
		if(domainList != null && domainList.size() > 0)
			return domainList;
		else
			return null;
	}
	
	public List<Duo<Domain, Pbx>> getDomainAndPbxByDomainKey(Long domainKey) throws DAOException
	{
		Domain domain = domainDAO.getByKey(domainKey);
		Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
		List<Duo<Domain, Pbx>> list = new ArrayList<Duo<Domain, Pbx>>();
		if(pbx != null)
			list.add(new Duo(domain, pbx));
		return list;
	}
	
	public Domain getRootDomain(Long domainKey) throws DAOException
	{
		return domainDAO.getByKey(domainKey);
	}
	
	public Domain getRootDomain() throws DAOException
	{
		return domainDAO.getRootDomain();
	}
	
	public Pbx getPbxByDomainName(String domainName) throws DAOException
	{
		return pbxDAO.getPbxByDomain(domainName);
	}
	
	public void getPBXCentrexContext(PBXInfo pbxInfo, Long domainRootKey) throws DAOException, ValidationException
	{
		List<Duo<Long, String>> addressAvailableList = addDAO.getDIDListByRootDomain(domainRootKey);
		if(addressAvailableList == null || addressAvailableList.size() == 0)
			throw new ValidationException("No DID Availables ", 0);
		pbxInfo.addDIDOutList(addDAO.getDIDListByRootDomain(domainRootKey));
		pbxInfo.setPbxPreferences(new Pbxpreference());
		createDefaultPBXDialPlan(pbxInfo);
		
		loadMediaRelayIPList(pbxInfo);
		loadAudioEstablishmentTypeList(pbxInfo);
	}
	
	private void createDefaultPBXDialPlan(PBXInfo info)
	{
		DialPlan defaultOperatorDialPlan = new DialPlan(DialPlan.DEFAULTOPERATOR_DEFAULT_START, DialPlan.DEFAULTOPERATOR_DEFAULT_END, DialPlan.TYPE_DEFAULTOPERATOR);
		DialPlan extensionDialPlan = new DialPlan(DialPlan.EXTENSION_DEFAULT_START, DialPlan.EXTENSION_DEFAULT_END, DialPlan.TYPE_EXTENSION);
		DialPlan publicSpeedDialDialPlan = new DialPlan(DialPlan.PUBLICSPEEDDIAL_DEFAULT_START, DialPlan.PUBLICSPEEDDIAL_DEFAULT_END, DialPlan.TYPE_PUBLICSPEEDDIAL);
		DialPlan userSpeedDialDialPlan = new DialPlan(DialPlan.USERSPEEDDIAL_DEFAULT_START, DialPlan.USERSPEEDDIAL_DEFAULT_END, DialPlan.TYPE_USERSPEEDDIAL);
		DialPlan parkDialPlan = new DialPlan(DialPlan.PARK_DEFAULT_START, DialPlan.PARK_DEFAULT_END, DialPlan.TYPE_PARK);
		
		info.getPbxpreferences().setDefaultOperatorDialPlan(defaultOperatorDialPlan);
		info.getPbxpreferences().setExtensionDialPlan(extensionDialPlan);
		info.getPbxpreferences().setParkDialPlan(parkDialPlan);
		info.getPbxpreferences().setUserSpeedDialDialPlan(userSpeedDialDialPlan);
		info.getPbxpreferences().setPublicSpeedDialDialPlan(publicSpeedDialDialPlan);
	}

	public void changeNightModeStatus(Long pbxKey, int status) throws DAOException, ValidateObjectException
	{
		Pbx pbx = pbxDAO.getByKey(pbxKey);
		pbx.setNightMode(status);
		pbxDAO.save(pbx);
	}
	
	public Pbx getPbxByDomain(String domainName) throws DAOException
	{
		return pbxDAO.getPbxByDomain(domainName);
	}
	
	public Domain getDomain(String domainName) throws DAOException 
	{
		return domainDAO.getDomain(domainName);
	}

	public List<Duo<Long, String>> getActiveDomainList() throws DAOException
	{
		return domainDAO.getActiveDomainList();
	}
	
	public Domain getDomainByPbx(Long pbxKey) throws DAOException
	{
		return domainDAO.getDomainByPbx(pbxKey);
	}

	public Pbxpreference getPbxPreference(String domain) throws DAOException
	{	
		return pbxPreferencesDAO.getByDomain(domain);
	}	
	
	public Pbxpreference getPbxPreference(Long pbxKey) throws DAOException
	{	
		return pbxPreferencesDAO.getByPbxKey(pbxKey);
	}	
}