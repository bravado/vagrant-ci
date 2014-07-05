package br.com.voicetechnology.ng.ipx.rule.implement;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.command.CommandConfigException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.WebAdminException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.QuotaException;
import br.com.voicetechnology.ng.ipx.commons.jms.tools.JMSNotificationTools;
import br.com.voicetechnology.ng.ipx.commons.security.crypt.Crypt;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.BlockDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallbackDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallfilterDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.FilterDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.KoushikubunDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserterminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PhoneDialPlanGroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PresenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ServiceclassDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipsessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.UsergroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ivr.IVRDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.CategoryDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.ContactDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.ContactphonesDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.EventsubscriptionDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.GroupfileDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RoleDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.SessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserfileDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserroleDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ForwardMode;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.Permissions;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVR;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Callback;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Callfilter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Filter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Koushikubun;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuserterminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlanGroup;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Presence;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Category;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contact;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contactphones;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Eventsubscription;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Permission;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Preference;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Role;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userrole;
import br.com.voicetechnology.ng.ipx.pojo.facets.call.CallbackFacet;
import br.com.voicetechnology.ng.ipx.pojo.facets.config.SipIDListFacet;
import br.com.voicetechnology.ng.ipx.pojo.facets.group.NightmodeGroupFacet;
import br.com.voicetechnology.ng.ipx.pojo.facets.user.PresenceFacet;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CallInformationsInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CallbackInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CalllogInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ConfigViewInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.EasyCallInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.KoushikubunInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.LoginInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ManagementInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.NightmodeGroup;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PbxuserInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PersonalDataUserInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PhoneInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PresenceInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SimpleContactInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserForwardsInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.pojo.report.Report.Operator;
import br.com.voicetechnology.ng.ipx.pojo.report.Report.Order;
import br.com.voicetechnology.ng.ipx.pojo.webreport.wca.view.CalllogWebReport;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class PbxuserManager extends Manager
{
	private AddressManager addManager;
	private BlockManager blockManager;
	private SipsessionlogDAO sslDAO;
	private PbxuserDAO pbxuserDAO;
	private UserDAO userDAO;
	private AddressDAO addDAO;
	private ConfigDAO configDAO;
	private ServiceclassDAO serviceclassDAO;
	private RoleDAO roleDAO;
	private PbxDAO pbxDAO;
	private BlockDAO blockDAO;
	private ForwardDAO forwardDAO;
	private GroupDAO groupDAO;
	private PresenceDAO presenceDAO;
	private PreferenceDAO preferenceDAO;
	private UserroleDAO userroleDAO;
	private UsergroupDAO usergroupDAO;
	private CallfilterDAO callfilterDAO;
	private CategoryDAO categoryDAO;
	private PbxuserterminalDAO pbxuserterminalDAO;
	private SessionlogDAO slogDAO;
	private CalllogDAO calllogDAO;
	private UserfileDAO userFileDAO;
	private CallbackDAO callbackDAO;
	private ContactDAO contactDAO;
	private IVRDAO ivrDAO;
	private DomainDAO domainDAO;
	private ContactphonesDAO contactPhonesDAO;
	private FilterDAO filterDAO;
	private TerminalDAO terminalDAO;
	private EventsubscriptionDAO eventSubscriptionDAO;
	private GroupfileDAO groupfileDAO;
	private KoushikubunDAO koushiKubunDAO;
	private KoushiKubunManager koushiKubunManager; //  issue 5293 tveiga rc12 patch 5
	private PbxpreferenceDAO pbxPreferenceDAO;
	private CallfilterDAO cfDAO;
	private PhoneDialPlanGroupDAO phoneDialPlanGroupDAO;
	
	public PbxuserManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		addManager = new AddressManager(logger);
		blockManager = new BlockManager(logger);
		koushiKubunManager = new KoushiKubunManager(logger.getName()); // issue 5293 tveiga rc12 patch 5
		sslDAO = dao.getDAO(SipsessionlogDAO.class);
		pbxuserDAO = dao.getDAO(PbxuserDAO.class);
		userDAO = dao.getDAO(UserDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		configDAO = dao.getDAO(ConfigDAO.class);
		serviceclassDAO = dao.getDAO(ServiceclassDAO.class);
		roleDAO = dao.getDAO(RoleDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		blockDAO = dao.getDAO(BlockDAO.class);
		forwardDAO = dao.getDAO(ForwardDAO.class);
		groupDAO = dao.getDAO(GroupDAO.class);
		presenceDAO = dao.getDAO(PresenceDAO.class);
		preferenceDAO = dao.getDAO(PreferenceDAO.class);
		userroleDAO = dao.getDAO(UserroleDAO.class);
		usergroupDAO = dao.getDAO(UsergroupDAO.class);
		callfilterDAO = dao.getDAO(CallfilterDAO.class);
		categoryDAO = dao.getDAO(CategoryDAO.class);
		pbxuserterminalDAO = dao.getDAO(PbxuserterminalDAO.class);
		slogDAO = dao.getDAO(SessionlogDAO.class);
		calllogDAO = dao.getDAO(CalllogDAO.class);
		userFileDAO = dao.getDAO(UserfileDAO.class);
		callbackDAO = dao.getDAO(CallbackDAO.class);
		contactDAO = dao.getDAO(ContactDAO.class);
		ivrDAO = dao.getDAO(IVRDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
		contactPhonesDAO = dao.getDAO(ContactphonesDAO.class);
		filterDAO = dao.getDAO(FilterDAO.class);
		terminalDAO = dao.getDAO(TerminalDAO.class);
		eventSubscriptionDAO = dao.getDAO(EventsubscriptionDAO.class);
		groupfileDAO = dao.getDAO(GroupfileDAO.class);
		koushiKubunDAO = dao.getDAO(KoushikubunDAO.class);
		pbxPreferenceDAO = dao.getDAO(PbxpreferenceDAO.class);
		cfDAO = dao.getDAO(CallfilterDAO.class);
		phoneDialPlanGroupDAO = dao.getDAO(PhoneDialPlanGroupDAO.class);
	}
	
	public ReportResult<PbxuserInfo> find(Report<PbxuserInfo> report) throws DAOException
	{
		ReportDAO<Pbxuser, PbxuserInfo> puReport = dao.getReportDAO(PbxuserDAO.class);
		Long size = puReport.getReportCount(report);
		List<Pbxuser> puList = puReport.getReportList(report);
		List<PbxuserInfo> puInfoList = getPbxuserInfoList(puList);
		return new ReportResult<PbxuserInfo>(puInfoList, size);
	}
	
	public List<PbxuserInfo> getPbxuserInfoList(List<Pbxuser> puList) throws DAOException{
		List<PbxuserInfo> puInfoList = new ArrayList<PbxuserInfo>();
		for(Pbxuser pu : puList)
		{
			puInfoList.add(getInfo(pu));
		}
		return puInfoList;
	}

	
	public PbxuserInfo getInfo(Pbxuser pu) throws DAOException{
		List<Address> extensionList = addDAO.getExtensionListByPbxuser(pu.getKey());
		extensionList.addAll(addDAO.getTerminalExtensionListAssociatedByPbxuser(pu.getKey()));
		PbxuserInfo info = new PbxuserInfo(pu);
		info.setExtensionList(extensionList);
		//tveiga issue 6111 - inicio 
		List<Address> didList = addDAO.getDIDListByPbxuser(pu.getKey());
		List<Duo<Long, String>> didTempList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> addressTempList = new ArrayList<Duo<Long,String>>();
        for(Address address : didList)
        {
        	didTempList.add(new Duo<Long, String>(address.getKey(), address.getAddress()));
        }
        
        for(Address address : extensionList)
        {
        	addressTempList.add(new Duo<Long, String>(address.getKey(), address.getAddress()));
        }
        
        info.setDIDList(didTempList);
        info.setAddressList(addressTempList);
        if(pu != null && pu.getDefaultDIDKey() != null)
        {
        	Address defaultaddres = addDAO.getByKey(pu.getDefaultDIDKey());
            info.setDefaultDIDANI(defaultaddres != null ?defaultaddres.getAddress() : null);	
        }
        //tveiga issue 6111 - fim 
		Long sipSessions = sslDAO.getHowManySipSessionByPbxuser(pu.getKey());
		if(sipSessions.longValue() == 0L)
		{
			List<Sipsessionlog> sipTerminalList = sslDAO.getActiveSipSessionLogListByTerminalOfPbxuser(pu.getKey());
			info.setOffline(sipTerminalList == null || sipTerminalList.size() == 0);
		} else
			info.setOffline(false);
		return info;
	}
	
	//Retorna uma lista de DIDs. Ordem dos dids: pbxuser, ivr, trunk, group
	public List<Duo<Long, String>> getPbxuserDIDList(Long pbxuserKey) throws DAOException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserFull(pbxuserKey);
		Block block = blockDAO.getBlockWithItens(pu.getConfigKey(), Block.TYPE_INCOMING);		
		
		List<Duo<Long, String>> outputDIDList = new ArrayList<Duo<Long,String>>();
		List<Address> didList = new ArrayList<Address>();
		
		didList.addAll(addDAO.getDIDListByPbxuser(pbxuserKey));
		didList.addAll(addDAO.getIvrDIDListByPbxuser(pbxuserKey));
		
		Address trunkDID = null;
		if(block != null)
		{
			trunkDID = addDAO.getDefaultAddress(block.getPbxKey());
			didList.add(trunkDID);		
		}
		
		List<Group> gList = groupDAO.getGroupListByPbxuser(pbxuserKey);
		for(Group g: gList)
		{
			List<Address> didEachGroupList = addDAO.getDIDListByGroup(g.getKey());
			for(Address address: didEachGroupList)
				didList.add(address);
		}
		
		for(Address did : didList)
			outputDIDList.add(new Duo<Long, String>(did.getKey(), did.getAddress()));

		//resgata od dids relacionados aos usuarios Tronco do domínio
		List<Pbxuser> sipTrunkList = pbxuserDAO.getSipTrunkUsers(pu.getUser().getDomainKey());
		for(Pbxuser sipTrunk: sipTrunkList)
		{
			List<Address> tmp = addDAO.getDIDListByPbxuser(sipTrunk.getKey());
			for(Address did : tmp)
				outputDIDList.add(new Duo<Long, String>(did.getKey(), did.getAddress()));
		}
		
		//resgata od dids relacionados aos grupos Tronco do domínio
		List<Group> groupTrunkList = groupDAO.getGroupsInPBX(pbxDAO.getPbxByDomain(pu.getUser().getDomainKey()).getKey());
		for(Group trunkGroup: groupTrunkList)
		{
			List<Address> didEachGroupList = addDAO.getDIDListByGroup(trunkGroup.getKey());
			for(Address address: didEachGroupList)
				outputDIDList.add(new Duo<Long, String>(address.getKey(), address.getAddress()));
		}
		
		return outputDIDList;
	}
	public PbxuserInfo getPbxuserInfoByKey(Long pbxuserKey, boolean isViewUser) throws DAOException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserFull(pbxuserKey);
		Block block = blockDAO.getBlockWithItens(pu.getConfigKey(), Block.TYPE_INCOMING);
		Forward always = forwardDAO.getForwardByConfig(pu.getConfigKey(), Forward.ALWAYS_MODE);
		Forward busy = forwardDAO.getForwardByConfig(pu.getConfigKey(), Forward.BUSY_MODE);
		Forward noAnswer = forwardDAO.getForwardByConfig(pu.getConfigKey(), Forward.NOANSWER_MODE);
		Forward callFailure = forwardDAO.getForwardByConfig(pu.getConfigKey(), Forward.CALL_FAILURE_MODE);
		KoushikubunInfo kkinfo = koushiKubunManager.getKoushikubunInfo(pbxuserKey); // issue 5293 tveiga rc12 patch 5
		PbxuserInfo info = new PbxuserInfo(pu, block, always, busy, noAnswer, callFailure);
		Integer noAnswerTimeout = Integer.valueOf(IPXProperties.getProperty(IPXPropertiesType.NOANSWER_TIMEOUT));
		info.setDefaultNoAnswerTimeout(noAnswerTimeout);
		  
		// seta o koushikubuninfo issue 5293 tveiga rc12 patch 5
		if (kkinfo != null)
			info.setKoushikubunInfo(kkinfo);
		
		//seta no info o defaultDIDKey do usu�rio
		if(pu.getDefaultDIDKey() != null)
		{
			info.setDefaultDIDKey(pu.getDefaultDIDKey());
			Address defaultDIDAddress = addDAO.getByKey(pu.getDefaultDIDKey());
			info.setDefaultDIDANI(defaultDIDAddress != null ? defaultDIDAddress.getAddress() : null);
		} else if(pu.getDefaultDIDKey() == null && pu.getIsAnonymous().equals(Pbxuser.ANONYMOUS_ON) ? true : false)
		{
			info.setDefaultDIDKey(null);
			info.setIsAnonymous(true);
		} else
		{
			info.setDefaultDIDKey(null);
			info.setIsAnonymous(false);
		}

		// Dependence Lists
		pu.setAddressList(addDAO.getExtensionListByPbxuser(pbxuserKey));
		for(Address ext : pu.getAddressList())
			info.addExtension(new Duo<Long, String>(ext.getKey(), ext.getAddress()));

		if(isViewUser)
		{
			List<Address> terminalExtensionsList = addDAO.getTerminalExtensionListAssociatedByPbxuser(pbxuserKey);
			for(Address ext : terminalExtensionsList)
				info.addExtension(new Duo<Long, String>(ext.getKey(), ext.getAddress()));
		}

		List<Role> puRoleList = roleDAO.getRoleListByPbxuser(pbxuserKey);
		for(Role role : puRoleList)
			info.addRoleKey(role.getKey());

		List<Group> gList = groupDAO.getGroupListByPbxuser(pbxuserKey);
		for(Group g : gList)
			info.addGroup(new Duo<Long, String>(g.getKey(), g.getName()));

		List<Sipsessionlog> sipSessionLogList = sslDAO.getSipsessionlogListByPbxuser(pbxuserKey);
		if(sipSessionLogList != null && sipSessionLogList.size() > 0)
			info.setOffline(false);
		else
			info.setOffline(true);

		try 
		{
			getPbxuserInfoContext(info, pu.getUser().getDomainKey(), false);
		} catch(QuotaException e) 
		{
			//Excecao nunca pega pois quota nao eh validada neste escopo
			logger.error(e.getMessage());
		}
		
		Callfilter callFilter = cfDAO.getCallFilterByPbxuser(pbxuserKey);
		List<Filter> filterList = cfDAO.getFilterByCallFilter(callFilter.getKey());
		
		callFilter.setFilterList(filterList);
		
		info.setCallFilter(callFilter);
		
		return info;
	}
	
	public void getPbxuserInfoContext(PbxuserInfo info, Long domainKey, boolean validateQuota) throws DAOException, QuotaException
	{
		if(validateQuota)
		{
			Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
			Long userAmount = pbxuserDAO.countUsers(domainKey);
			if(userAmount >= pbx.getMaxUser())
				throw new QuotaException("User quota exceeded for this pbx!", QuotaException.Type.PBXUSER);
		}
		if(info.getPbxuserKey() == null)
		{
			Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
			info.setPbxKey(pbx.getKey());
		}
		makeDIDListRelatedToUser(info, info.getPbxuserKey() == null ? true : false, domainKey);

		// Dependence Lists
		List<Duo<Long, String>> roleList = roleDAO.getRoleKeyAndNameListMinusDefaultRole();
		for(int i = 0; i < roleList.size(); i++)
			info.addRole(new Duo<Long, String>(roleList.get(i).getFirst(), roleList.get(i).getSecond()));	
		Long pbxKey = pbxDAO.getPbxByDomain(domainKey).getKey();
		info.setPbxKey(pbxKey);
		info.addClassOfService(serviceclassDAO.getServiceclassKeyAndName(pbxKey));
		info.addClassOfService(serviceclassDAO.getCentrexServiceclassKeyAndName());
		Integer noAnswerTimeout = Integer.valueOf(IPXProperties.getProperty(IPXPropertiesType.NOANSWER_TIMEOUT));
		info.setDefaultNoAnswerTimeout(noAnswerTimeout);
		  
		if(info.getKoushikubunInfo()==null)
			info.setKoushikubunInfo(new KoushikubunInfo());
		
		List<PhoneDialPlanGroup> phoneDialPlanGroups = phoneDialPlanGroupDAO.getByDomainKey(domainKey);
		for(PhoneDialPlanGroup phoneDialPlanGroup : phoneDialPlanGroups){
			info.addPhoneDialPlanGroup(new Duo<Long, String>(phoneDialPlanGroup.getKey(), phoneDialPlanGroup.getName()));
		}
		
		if(info.getServiceclassKey() == null)
		{
			Serviceclass sc = serviceclassDAO.getCentrexDefaultServiceclass();
			if(sc!= null)
				info.setServiceclassKey(sc.getKey());
		}
		
	}
	
	private void makeDIDListRelatedToUser(PbxuserInfo info, boolean isNewUser , Long domainKey) throws DAOException
	{
		//resgata somente o did tronco-chave do PBX
		if(info.getPbxKey() != null)
		{
			Address trunkDID = addDAO.getDefaultAddress(info.getPbxKey());
			info.setDomainTrunkLine(trunkDID.getKey());
			info.addDID(new Duo<Long, String>(trunkDID.getKey(), trunkDID.getAddress()));
		}
		if(isNewUser)
		{
			//resgata od dids relacionados ao IVR do domínio
			List<Address> ivrDidList = addDAO.getIvrDIDListByDomain(domainKey);
			for(Address didIvr : ivrDidList)
			{
				info.addDID(new Duo<Long, String>(didIvr.getKey(), didIvr.getAddress()));
			}
			return;
		}
		//resgata os DIDs relacionados com o usuario
		addPbxuserDIDList(info, info.getPbxuserKey());

		//resgata os dids relacionados com os grupos em que o usuario pertence
		if(info.getGroupList() != null && info.getGroupList().size() > 0)
		{
			List<Address> didGroupList = new ArrayList<Address>();
			for(Duo<Long, String> gDuo : info.getGroupList())
			{
				List<Address> didEachGroupList = addDAO.getDIDListByGroup(gDuo.getFirst());
				for(Address address: didEachGroupList)
					didGroupList.add(address);
			}
			for(Address didGroup : didGroupList)
				info.addDID(new Duo<Long, String>(didGroup.getKey(), didGroup.getAddress()));
		}
		//resgata od dids relacionados ao IVR do domínio
		List<Address> ivrDidList = addDAO.getIvrDIDListByPbxuser(info.getPbxuserKey());
		for(Address didIvr : ivrDidList)
		{
			info.addDID(new Duo<Long, String>(didIvr.getKey(), didIvr.getAddress()));
		}
		
		//resgata od dids relacionados aos usuarios Tronco do domínio
		List<Pbxuser> sipTrunkList = pbxuserDAO.getSipTrunkUsers(domainKey);
		for(Pbxuser sipTrunk: sipTrunkList)
		{
			addPbxuserDIDList(info, sipTrunk.getKey());
		}
		
		//resgata od dids relacionados aos grupos Tronco do domínio
		List<Group> groupTrunkList = groupDAO.getGroupsInPBXByGroupType(info.getPbxKey(), Group.SIPTRUNK_GROUP);
		for(Group trunkGroup: groupTrunkList)
		{
			List<Address> didEachGroupList = addDAO.getDIDListByGroup(trunkGroup.getKey());
			for(Address address: didEachGroupList)
				info.addDID(new Duo<Long, String>(address.getKey(), address.getAddress()));
		}
	}
	
	private void addPbxuserDIDList(PbxuserInfo info, Long pbxuserKey) throws DAOException
	{
		//resgata os DIDs relacionados com o usuario
		List<Address> didList = addDAO.getDIDListByPbxuser(pbxuserKey);
		for(Address did : didList)
			info.addDID(new Duo<Long, String>(did.getKey(), did.getAddress()));
	}
	
    public Pbxuser getPbxuserAndUser(Long pbxuserKey) throws DAOException
    {
    	Pbxuser pbxUser = pbxuserDAO.getPbxuserAndUser(pbxuserKey);
    	pbxUser.setAddressList(addDAO.getExtensionListByPbxuser(pbxuserKey));
    	return pbxUser;
    }

	protected void setSipIDList(SipIDListFacet info, Long domainKey, boolean voicemail, String... excludeUserSipID) throws DAOException
	{
		info.addUsernameSipIDList(addDAO.getPbxuserKeyAndSipIDByDomain(domainKey, voicemail, excludeUserSipID));
		info.addGroupNameSipIDList(addDAO.getGroupKeyAndSipIDByDomain(domainKey, false));
		info.addTerminalNameSipIDList(addDAO.getTerminalPbxuserKeyAndTerminalNAmeByDomain(domainKey, excludeUserSipID));
		info.addIVRSipIDList(addDAO.getIVRPbxuserKeyAndIVRNameByDomain(domainKey, excludeUserSipID));
	}

	public void deletePbxusers(List<Long> pbxuserKeyList, boolean isForced) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		for(Long key : pbxuserKeyList)
			deletePbxuser(key , isForced);
	}
	
	public void deletePbxuser(Long pbxuserKey, boolean isForced) throws DAOException, br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException, DeleteDependenceException
	{
		//Pbxuser, Address, Presence, Pbxuserterminal
		Pbxuser pu = deletePbxuserAndDependences(pbxuserKey, isForced);

		//User, Role, GroupIn, Preference
		deleteUserAndDependences(pu, isForced);
		
		//Config, Block, Blockitem, Configblock, Forward
		deleteConfigAndDependences(pu.getConfig());
		
		//Web SessionLog, SipSessionlog
		deleteSessionAndDependences(pu, isForced);
	}

	private Pbxuser deletePbxuserAndDependences(Long pbxuserKey , boolean isForced) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserFull(pbxuserKey);
		pu.setPresenceKey(null);

		pu.setDefaultDIDKey(null);
		pu.setActive(Pbxuser.DEFINE_DELETED);

		List<Koushikubun> koushiKubunList = koushiKubunDAO.getKoushiKubunListByPbxuserKey(pu.getKey());
		for(Koushikubun koushikubun : koushiKubunList)
			koushiKubunDAO.remove(koushikubun);

		pbxuserDAO.save(pu);
		presenceDAO.remove(pu.getPresence());

		if(!pu.getUser().getUsername().equals(User.VOICEMAIL_NAME))
			addManager.removeAllAddress(pbxuserKey, false, isForced);

		List<Pbxuserterminal> ptList = pbxuserterminalDAO.getPbxuserterminalByPbxuser(pu.getKey());
		for(Pbxuserterminal pt : ptList)
			pbxuserterminalDAO.remove(pt);

		List<Calllog> callLogList = calllogDAO.getCallLogListByAnotherPbxuser(pbxuserKey);
		for(Calllog cl : callLogList)
		{
			cl.setAnotherPbxuserKey(null);
			calllogDAO.save(cl);
		}
		return pu;
	}

	private void deleteUserAndDependences(Pbxuser pbxuser, boolean isForced) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		pbxuser.getUser().setActive(User.DEFINE_DELETED);
		userDAO.save(pbxuser.getUser());
		preferenceDAO.remove(pbxuser.getUser().getPreference());	
		
		List<Userrole> urList = userroleDAO.getUserroleListByUser(pbxuser.getUser().getKey());
		for(Userrole ur : urList)
			userroleDAO.remove(ur);
		
		List<Usergroup> ugList = usergroupDAO.getUsergroupListByPbxuser(pbxuser.getKey());
		//tveiga adicionar forced para web service - basix 3.05 - inicio
		//TODO: verificar qual o procedimento caso seja administrador de grupo e de pbx   
		if(ugList.size() > 0)
			if(isForced)
			   for(Usergroup userGroup : ugList)
			   {
			      usergroupDAO.remove(userGroup);
			    //dsakuma/rribeiro - Notificação JMS para AcdCallCenter acerca de grupo deletado do domínio - início
					JMSNotificationTools.getInstance().sendRemoveUsergroupJMSMessage(pbxuser.getKey(), pbxuser.getUser().getDomainKey(), 
							userGroup.getGroup().getKey());
				//dsakuma/rribeiro - Notificação JMS para AcdCallCenter acerca de grupo deletado do domínio- fim
			   }
			else
			   throw new DeleteDependenceException("User is group member! Please try again later.", Group.class, (long) ugList.size(), pbxuser.getUser());
		//tveiga adicionar forced para web service - basix 3.05 - fim
	}
	
	private void deleteConfigAndDependences(Config config) throws DAOException, ValidateObjectException
	{
		config.setActive(Config.DEFINE_DELETED);
		configDAO.save(config);
		
		blockManager.deleteBlock(config.getKey(), Block.TYPE_INCOMING);
		blockManager.deleteConfigblock(config.getKey());
		
		List<Forward> fList = forwardDAO.getForwardListByConfig(config.getKey());
		for(Forward f : fList)
			forwardDAO.remove(f);
	}

	public void deleteSessionAndDependences(Pbxuser pu, boolean isForced) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		List<Sessionlog> openSessionlogList = slogDAO.getSessionlogListByPbxuser(pu.getKey(), true, false);
		//tveiga adicionar forced para web service - basix 3.05 - inicio
		if(openSessionlogList != null && openSessionlogList.size() > 0)
			if(isForced)
			  for(Sessionlog sessionLog : openSessionlogList)
				  slogDAO.remove(sessionLog);
		    else	
			  throw new DeleteDependenceException("Cannot delete " + pu.getUser().getUsername() + ": " + openSessionlogList.size() + " active web session(s) points to this user", Sessionlog.class, (long) openSessionlogList.size(), pu.getUser());
		//tveiga adicionar forced para web service - basix 3.05 - fim
		
		List<Sipsessionlog> sslList = sslDAO.getSipsessionlogListByPbxuser(pu.getKey());
		for(Sipsessionlog ssl : sslList)
		{
			Sessionlog slog = ssl.getSessionlog();
			if(slog.getSessionEnd() == null)
			{
				slog.setSessionEnd(Calendar.getInstance());
				slogDAO.save(slog);
			}
			removeEventSubscriptionListAssociatedsBySipSessionlog(ssl.getKey());
			sslDAO.remove(ssl);
		}
	}
	
	private void removeEventSubscriptionListAssociatedsBySipSessionlog(Long sipSessionLogKey) throws DAOException
	{
		List<Eventsubscription> eventList = eventSubscriptionDAO.getBySipsessionlog(sipSessionLogKey);
		for(Eventsubscription ev : eventList)
			eventSubscriptionDAO.remove(ev);
	}	
	
	public void save(PbxuserInfo puInfo) throws DAOException, ValidateObjectException
	{
		save(puInfo, false);
	}
	
	public void save(PbxuserInfo puInfo, boolean isFromWeb) throws DAOException, ValidateObjectException
	{
		final Long key = puInfo.getKey();
		Pbxuser pu = puInfo.getPbxuser();
		Block blockIn = puInfo.getIncomingBlock();
		
		validatePbxuser(pu); // tveiga issue 5912 3.0.6 
		
		if(pu.isRecordCallEnable())
			validateMaxRecordCallUsers(puInfo.getDomainKey(), key);

		if(pu.getConfig().getAllowedVideoCall() == Config.ALLOWED_VIDEOCALL)
			validateMaxVideoCallUsers(puInfo.getDomainKey(), key);
		
		manageOptionsFeatures(puInfo);
		
		pu.getConfig().setAllowedKoushiKubun(puInfo.getAllowedKoushiKubun());
		pu.getConfig().setAllowedVoiceMail(puInfo.getAllowedVoicemail());
		pu.getConfig().setAllowedGroupForward(Config.NOT_ALLOWED_GROUPFORWARD);		
		
		if(pu.getKey() == null)
			blockManager.saveBlock(blockIn);
		savePbxuser(pu, isFromWeb);
		
		// inicio issue 5293 tveiga rc12 patch 4 
		if(puInfo.getKoushikubunInfo() != null)
		{
			puInfo.getKoushikubunInfo().setPbxuserKey(pu.getKey());
			puInfo.getKoushikubunInfo().setDomainkey(puInfo.getDomainKey());//dnakamashi - correção do bug #6831 - 3.0.5RC6.5.1
			koushiKubunManager.saveKoushikubunInfo(puInfo.getKoushikubunInfo());
		}
        // fim issue 5293 tveiga rc12 patch 4 

		if(key == null)
			blockManager.saveConfigblock(blockIn.getKey(), pu.getConfigKey());

		//TODO: Trazer o userType de cima 
		saveForwardList(User.TYPE_PBXUSER, pu.getKey(), puInfo.getDomainKey(), pu.getConfigKey(), puInfo.getForwardAlways(), puInfo.getForwardBusy(), puInfo.getForwardNoAnswer(), puInfo.getForwardCallFailure());

		if(pu.getConfig() != null && pu.getConfig().getDisableVoicemail() == Config.VOICEMAIL_OFF)
			deleteVoicemailRelationships(pu.getConfigKey(), pu.getUser().getDomainKey());

		saveAddressList(pu);
		saveUserroleList(pu.getUserKey(), puInfo.getRoleKeyList());
	}

	private void validateMaxRecordCallUsers(Long domainKey, Long pbxuserKey) throws DAOException, ValidateObjectException 
	{	
		Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
		List<Pbxuser> list = pbxuserDAO.getPbxusersByAllowedRecordCall(pbx.getKey(), Config.ALLOWEDRECORDCALL_ON, pbxuserKey);
		if(pbx.getMaxRecordCallUsers() <= list.size())
			throw new ValidateObjectException("Max Record Call Users!", User.class, pbx.getMaxIVRApplication(), ValidateType.MAX_NUMBER);
	}
	
	private void validateMaxVideoCallUsers(Long domainKey, Long pbxuserKey) throws DAOException, ValidateObjectException 
	{	
		Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
		List<Pbxuser> list = pbxuserDAO.getPbxusersByAllowedVideoCall(pbx.getKey(), Config.ALLOWED_VIDEOCALL, pbxuserKey);
		if(pbx.getMaxVideoCallUsers() <= list.size())
			throw new ValidateObjectException("Max Video Call Users!", User.class, pbx.getMaxIVRApplication(), ValidateType.MAX_NUMBER_VIDEO);
	}

	private void createBlock(Block block) throws DAOException, ValidateObjectException
	{
		block.setActive(Block.DEFINE_ACTIVE);
		blockManager.saveBlock(block);
	}
	
	protected void saveAddressList(Pbxuser pu) throws DAOException, ValidateObjectException
	{
		addManager.saveAddressList(pu);		
	}

	protected void savePbxuser(Pbxuser pu, boolean isFromWeb) throws ValidateObjectException, DAOException
	{
		validatePbxuser(pu);
		if(pu.getUser().getUsername().equals(User.MUSICSERVER_NAME))
			pu.getUser().setAgentUser(User.TYPE_MUSIC_SERVER);
		else if(pu.getUser().getUsername().equals(User.VOICEMAIL_NAME))
			pu.getUser().setAgentUser(User.TYPE_VOICEMAIL);
		else if(pu.getUser().getUsername().equals(User.PARKSERVER_NAME))
			pu.getUser().setAgentUser(User.TYPE_PARK_SERVER);
		else if(pu.getUser().getUsername().equals(User.ACDGROUP_NAME))
			pu.getUser().setAgentUser(User.TYPE_ACD_GROUP);
		userDAO.save(pu.getUser());
		pu.setUserKey(pu.getUser().getKey());
		pu.getUser().getPreference().setUserKey(pu.getUser().getKey());
		if(pu.getKey() != null)
		{
			Preference preferenceOld = preferenceDAO.getPreferenceByPbxuser(pu.getKey());
			preferenceOld.setLocale(pu.getUser().getPreference().getLocale());
			preferenceOld.setItensPerPage(pu.getUser().getPreference().getItensPerPage());
			preferenceOld.setClickToCallConfirmation(pu.getUser().getPreference().getClickToCallConfirmation());
			preferenceDAO.save(preferenceOld);
		} else
		{
			Preference preference = pu.getUser().getPreference();
			preference.setLastCalllogView(Calendar.getInstance());
			preferenceDAO.save(preference);
		}
		// caso seja adica�o de um novo usuario, seta timeout padrao
		if(pu.getConfig().getTimeoutcall() == null )
			pu.getConfig().setTimeoutcall(Config.DEFAULT_TIMEOUT);

		if(pu.getConfig().getAttachFile() == null)
			pu.getConfig().setAttachFile(Config.ATTACH_FILE_OFF);
		if(pu.getConfig().getForwardType() == null)
			pu.getConfig().setForwardType(Config.FORWARD_TYPE_DEFAULT);

		configDAO.save(pu.getConfig());
		pu.setConfigKey(pu.getConfig().getKey());

		//SAVE
		if(pu.getKey() == null)
		{
			// setagem dos valores padr�es para anonymous e default DID
			pu.setIsAnonymous(Pbxuser.ANONYMOUS_OFF);
			if(!isFromWeb)
				pu.setDefaultDIDKey(null);

            Callfilter callfilter = new Callfilter();
            callfilter.setConfigKey(pu.getConfigKey());
            callfilter.setActive(new Integer(0));
            callfilterDAO.save(callfilter);

    		Category category = new Category();
            category.setName(Category.DEFINE_NAME_DEFAULT);
            category.setDescription(Category.DEFINE_NAME_DEFAULT);
            category.setType(new Integer(Category.TYPE_GENERAL));    		
			category.setUserKey(pu.getUserKey());
			categoryDAO.save(category);
			pu.getUser().setCategory(category);

			presenceDAO.save(pu.getPresence());
		}
		pbxuserDAO.save(pu);
		pu.setPresenceKey(pu.getPresence().getKey());
	}
	
	//inicio --> dnakamashi - OptionsFeatures- 3.0.5
	private void manageOptionsFeatures(PbxuserInfo puInfo) throws DAOException
	{
		Pbxpreference pbxPreference = pbxPreferenceDAO.getByDomainKey(puInfo.getUser().getDomainKey());		
		
		if(puInfo.getPbxuser().getKey() == null)
		{
			if(pbxPreference.getOptionsFeaturesEnable() == Pbxpreference.OPTIONS_FEATURES_ENABLE_ON)
			{
				puInfo.setAllowedKoushiKubun(Config.ALLOWED_KOUSHIKUBUN);
				puInfo.setAllowedVoicemail(Config.ALLOWED_VOICEMAIL);
			}
			else
			{
				puInfo.setAllowedKoushiKubun(Config.NOT_ALLOWED_KOUSHIKUBUN);
				puInfo.setAllowedVoicemail(Config.NOT_ALLOWED_VOICEMAIL);
			}	
		}				
	}
	//fim --> dnakamashi - OptionsFeatures- 3.0.5
	
	protected void saveForwardList(Integer userType, Long puKey, Long domainKey, Long configKey, Forward... forwardList) throws DAOException, ValidateObjectException
	{
		for(Forward f : forwardList)
		{
			Forward tmp = forwardDAO.getForwardByConfig(configKey, f.getForwardMode());
			if(tmp == null)
			{
				tmp = f;
				tmp.setConfigKey(configKey);
			}else
			{
				tmp.setAddressKey(f.getAddressKey());
				tmp.setStatus(f.getStatus());
				tmp.setTarget(f.getTarget());
			}
			forwardDAO.save(tmp);
		}
		
		
	}
	
	protected void saveUserroleList(Long uKey, List<Long> roleKeyList) throws DAOException, ValidateObjectException
	{
		List<Userrole> urList = userroleDAO.getUserroleListByUser(uKey);
		if(urList.size() > 0)
			for(Userrole ur : urList)
				userroleDAO.remove(ur);
		for(Long rKey : roleKeyList)
		{
			//TODO if colocado pois o javascript de role atualmente est� em um padr�o ruim de l�gica de Role. 
			//Ap�s a entrega da vers�o esta l�gica deve ser refeita e este if deve sumir
			if(rKey != null)
			{
				Userrole ur = new Userrole();
				ur.setRoleKey(rKey);
				ur.setUserKey(uKey);
				userroleDAO.save(ur);
			}	
		}
	}
	
	private void validatePbxuser(Pbxuser pu) throws ValidateObjectException
	{
		if(!pu.getUser().getUsername().matches(getSipIDRegex()))
			throw new ValidateObjectException("Please check username, it must be compilant with sipID rules!", User.class, pu.getUser(), ValidateType.INVALID);
	}

	public Pbxuser getPbxuserFullByKey(Long pbxuserKey) throws DAOException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserFull(pbxuserKey); 
		if(pu == null)
		{
			pu = pbxuserDAO.getByKey(pbxuserKey);
			pu.setConfig(configDAO.getByKey(pu.getConfigKey()));
		}
		return pu;
	}

	public PresenceInfo getPresenceInfo(Long pbxuserKey) throws DAOException
	{
		Pbxuser pu = pbxuserDAO.getByKey(pbxuserKey);
		Presence presence = presenceDAO.getByKey(pu.getPresenceKey());
		Integer status = presence.getState();		
		List<Sipsessionlog> sipList = sslDAO.getActiveSipsessionlogListByPbxuser(pu.getKey());
		if(sipList.size() == 0)
			sipList = sslDAO.getActiveSipSessionLogListByTerminalOfPbxuser(pu.getKey());
		if(sipList.size() == 0)
			status = Presence.STATE_OFFLINE;
		return new PresenceInfo(presence, status);
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

	public ConfigViewInfo getPbxuserConfigurations(Long pbxuserKey) throws DAOException
	{
		Pbxuser pbxuser = pbxuserDAO.getPbxuserFull(pbxuserKey);
		Forward always = forwardDAO.getForwardByConfig(pbxuser.getConfigKey(), Forward.ALWAYS_MODE);
		if(always.getAddressKey() != null)
			always.setAddress(addDAO.getByKey(always.getAddressKey()));
			
		Forward busy = forwardDAO.getForwardByConfig(pbxuser.getConfigKey(), Forward.BUSY_MODE);
		if(busy.getAddressKey() != null)
			busy.setAddress(addDAO.getByKey(busy.getAddressKey()));
			
		Forward no = forwardDAO.getForwardByConfig(pbxuser.getConfigKey(), Forward.NOANSWER_MODE);
		if(no.getAddressKey() != null)
			no.setAddress(addDAO.getByKey(no.getAddressKey()));

		Pbx pbx = pbxDAO.getPbxByDomain(pbxuser.getUser().getDomainKey());
		if(pbx.getNightmodeaddressKey() != null)
			pbx.setNightmodeaddress(addDAO.getByKey(pbx.getNightmodeaddressKey()));
		
		String did;
		if(pbxuser.getDefaultDIDKey() != null)
			did = addDAO.getByKey(pbxuser.getDefaultDIDKey()).getAddress();
		else if(pbxuser.getIsAnonymous() == Pbxuser.ANONYMOUS_ON)
			did = Pbxuser.ANONYMOUS;
		else
		{
			List<Address> pbxuserDIDList = addDAO.getDIDListByPbxuser(pbxuserKey);
			if(pbxuserDIDList != null && pbxuserDIDList.size() > 0)
				did = pbxuserDIDList.get(0).getAddress();
			else
				did = addDAO.getDefaultAddress(pbx.getKey()).getAddress();
		}

		List<Group> groupList = groupDAO.getGroupListByPbxuser(pbxuserKey);
		List<NightmodeGroupFacet> nightmodeGroupList = new ArrayList<NightmodeGroupFacet>(groupList.size());
		for (Group group : groupList) 
		{
			String nightmodeTarget = null;
			if(group.getNightmodeaddressKey() != null)
				nightmodeTarget = (addDAO.getByKey(group.getNightmodeaddressKey())).getAddress();
			nightmodeGroupList.add(new NightmodeGroup(group.getName(), group.getNightmodeStatus() == Group.NIGHTMODE_ON, nightmodeTarget));
		}
		return new ConfigViewInfo(pbxuser.getConfig(), always, no, busy, pbx, did,nightmodeGroupList);
	}

	public CallInformationsInfo getPbxuserCallInformations(Long pbxuserKey) throws DAOException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserAndUser(pbxuserKey);
		Preference preference = preferenceDAO.getPreferenceByUser(pu.getUserKey());
		CalllogInfo info = new CalllogInfo();
		info.setPbxuserKey(pbxuserKey);
		info.setType(CallType.CALL_RECEIVED);
		info.setStart(preference.getLastCalllogView());
		info.setDomainKey(pu.getUser().getDomainKey());

		//info.setStatus(CallStateEvent.CALL_UNANSWERED);
		info.setStatuses(new Integer[]{CallStateEvent.CALL_BLOCKED, 	CallStateEvent.CALL_BLOCKED, 	CallStateEvent.FAILED,
									   CallStateEvent.NOT_FOUND, 		CallStateEvent.PERMISSION_DENIED, 	CallStateEvent.CALL_REJECTED,
									   CallStateEvent.CALL_UNANSWERED, 	CallStateEvent.UNAVALIABLE, 		CallStateEvent.CALL_FORWARDED});
		Report<CalllogInfo> missedReport = new CalllogWebReport();
		missedReport.setInfo(info);
		missedReport.setOperator(Operator.AND);
		missedReport.setOrder(Order.ASC);
		Long missedCalls = calllogDAO.getReportCount(missedReport);

//		info.setStatus(CallStateEvent.CALL_FORWARDED);
//		Report<CalllogInfo> forwardedReport = new Report<CalllogInfo>(info, null, null);
//		Integer forwardedCalls = calllogDAO.getReportCount(forwardedReport);
// rc 12 patch 4 issue 5191 thiago veiga inicio
		Long unreadedUserMessages = userFileDAO.countNewMessagesByUser(pu.getUserKey(), false);
		Long unreadedGruopMessages = groupfileDAO.countNewMessagesByGroupAdmin(pu.getKey(), false);
		Long readedUserMessages = userFileDAO.countOldMessagesByUser(pu.getUserKey(), false);
		Long readedGroupMessages = groupfileDAO.countOldMessagesByGroupAdmin(pu.getKey(), false);
//rc 12 patch 4 issue 5191 thiago veiga fim 	
		List<Callback> callbackList = callbackDAO.getCallbackListFromPbxuser(pbxuserKey);
		List<CallbackFacet> callbackInfoList = new ArrayList<CallbackFacet>(callbackList.size());
		for (Callback callback : callbackList)
		{
			String from = new SipAddressParser(callback.getSipFrom()).getExtension();
			String to = new SipAddressParser(callback.getSipTo()).getExtension();
			callbackInfoList.add(new CallbackInfo(callback, from, to));
		}
		return new CallInformationsInfo(missedCalls, readedUserMessages,readedGroupMessages , unreadedUserMessages, unreadedGruopMessages , preference.getLastCalllogView(), callbackInfoList);
	}

	public CallInformationsInfo getReadAndUnreadMessages(Long userKey, boolean onlyNotYetNotified) throws Exception
	{
		Long unreadedMessages = userFileDAO.countNewMessagesByUser(userKey, onlyNotYetNotified);
		
		Long readedMessages = userFileDAO.countOldMessagesByUser(userKey, onlyNotYetNotified);
		return new CallInformationsInfo(null, readedMessages,null, unreadedMessages,null, null, null);//rc 12 patch 4 5191 thiago veiga
	}

	public void clearMissedCalls(Long pbxuserKey) throws DAOException, ValidateObjectException
	{
		Preference preference = preferenceDAO.getPreferenceByPbxuser(pbxuserKey);
		preference.setLastCalllogView(Calendar.getInstance());
		preferenceDAO.save(preference);

		deleteMissedCalls(pbxuserKey);
	}

	private void deleteMissedCalls(Long pbxuserKey) throws DAOException, ValidateObjectException
	{
		int[] statusList = new int[]{CallStateEvent.CALL_BLOCKED, CallStateEvent.CALL_DISCONNECTED, CallStateEvent.CALL_FORWARDED, 
									 CallStateEvent.CALL_UNANSWERED, CallStateEvent.DESTINATION_BUSY, CallStateEvent.UNAVALIABLE, 
									 CallStateEvent.NOT_FOUND, CallStateEvent.MAX_CONCURRENT_CALLS_EXCEEDED, CallStateEvent.CALL_REJECTED};
		List<Calllog> callLogList = calllogDAO.getActiveMissedCallsByPbxuser(pbxuserKey, statusList);
		for(Calllog clog : callLogList)
		{
			clog.setActive(Calllog.DEFINE_DELETED);
			calllogDAO.save(clog);
		}
	}

	public void deleteMissedCall(Long callLogKey) throws DAOException, ValidateObjectException
	{
		Calllog clog = calllogDAO.getByKey(callLogKey);
		if(clog != null)
		{
			clog.setActive(Calllog.DEFINE_DELETED);
			calllogDAO.save(clog);
		}
	}

	public List<SimpleContactInfo> getMostCalleds(Long pbxuserKey, Integer maxContacts) throws DAOException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserAndUser(pbxuserKey);
		List<Address> addressList = addDAO.getAddressListByPbxuser(pu.getKey());
		List<String> calledList = calllogDAO.getMostDialedCalls(pbxuserKey, false, addressList, maxContacts);
		List<SimpleContactInfo> contactInfoList = new ArrayList<SimpleContactInfo>(calledList.size());
		for (String called : calledList) 
		{
			SimpleContactInfo info = null;
			SipAddressParser parser = new SipAddressParser(called);
			String address = parser.getExtension();
			String domain = parser.getDomain();
			//Caso seja external
			if(address.matches(Address.EXTERNAL_REGEX) || address.matches("\\d+"))
			{
				Contact contact = contactDAO.getContactByUserAndAddress(pu.getUserKey(), address);
				if(contact != null)
					info = new SimpleContactInfo(contact, address);
				else
				{
					Contact publicContact = contactDAO.getPublicContact(pu.getUser().getDomainKey(), address);
					if(publicContact != null)
						info = new SimpleContactInfo(publicContact, address);
					else
					{
						Address add = addManager.getAddress(address, domain);
						if(add != null)
						{
							if(add.getPbxuserKey() != null)
							{
								Pbxuser calledPbxuser = pbxuserDAO.getPbxuserByAddressAndDomain(address, domain);
								if(calledPbxuser != null)
								{
									if(calledPbxuser.getPresenceKey() != null)
									{
										Presence presence = getPresenceByPbxuser(calledPbxuser);
										info = new SimpleContactInfo(calledPbxuser, presence, address);
									} else
										info = new SimpleContactInfo(calledPbxuser, new Presence(), address);
								} else
									info = new SimpleContactInfo(address);
							} else if(add.getGroupKey() != null)
							{
								Group group = groupDAO.getGroupByAddressAndDomain(address, domain);
								if(group != null)
									info = new SimpleContactInfo(group, null, address);
								else
									info = new SimpleContactInfo(address);
							} else
								info = new SimpleContactInfo(address);
						} else
							info = new SimpleContactInfo(address);
					}
				}
			} else
			{
				Pbxuser calledPbxuser = pbxuserDAO.getPbxuserByAddressAndDomain(address, domain);
				if(calledPbxuser != null)
				{
					if(calledPbxuser.getPresenceKey() != null)
					{	
						Presence presence = getPresenceByPbxuser(calledPbxuser);
						info = new SimpleContactInfo(calledPbxuser, presence, address);
					} else
						info = new SimpleContactInfo(calledPbxuser, new Presence(), address);
				} else
				{
					Group group = groupDAO.getGroupByAddressAndDomain(address, domain);
					if(group != null)
						info = new SimpleContactInfo(group, null, address);
					else
						info = new SimpleContactInfo(address);
				}
			}
			contactInfoList.add(info);
		}
		return contactInfoList;
	}

	private Presence getPresenceByPbxuser(Pbxuser pu) throws DAOException
	{
		Presence p = clonePresence(presenceDAO.getByKey(pu.getPresenceKey()));
		if(sslDAO.getHowManySipSessionByPbxuser(pu.getKey()).intValue() == 0)
		{
			List<Sipsessionlog> sipSessionLogList = sslDAO.getActiveSipSessionLogListByTerminalOfPbxuser(pu.getKey());
			if(sipSessionLogList == null || sipSessionLogList.size() == 0)
				p.setState(Presence.STATE_OFFLINE);
		}
		return p;
	}

	private Presence clonePresence(Presence newData)
	{
		Presence source = new Presence();
		source.setComment(newData.getComment());
		source.setKey(newData.getKey());
		source.setState(newData.getState());
		source.setLastChange(newData.getLastChange());
		return source;
	}
	
	public void updatePbxuserSettings(PbxuserInfo pbxuserInfo) throws DAOException, ValidateObjectException 
	{
		Pbxuser pbxuser = pbxuserInfo.getPbxuser();
		User user = pbxuserInfo.getUser();
		Config config = pbxuserInfo.getConfig();
		Preference preference = pbxuserInfo.getPreference();
		if(pbxuser.getKey() == null)
			throw new IllegalArgumentException("Cannot update a pbxuser settings without a key!");
		if(user == null || user.getKey() == null)
			throw new IllegalArgumentException("Cannot update a pbxuser settings without a saved user!");
		if(config == null || config.getKey() == null)
			throw new IllegalArgumentException("Cannot update a pbxuser settings without a saved config!");
		if(preference == null || preference.getKey() == null)
			throw new IllegalArgumentException("Cannot update a pbxuser settings without a saved preference!");
		if(!user.getPassword().matches(Crypt.MD5_REGEX))
			throw new IllegalArgumentException("Cannot update a pbxuser settings without an encrypted password!");
		if(!pbxuser.getPin().matches(Crypt.MD5_REGEX))
			throw new IllegalArgumentException("Cannot update a pbxuser settings without an encrypted pin!");
		
		validatePbxuser(pbxuser);
		//setagem do pbxuser
		Pbxuser pbxuserOld = pbxuserDAO.getPbxuserFull(pbxuser.getKey());
		pbxuserOld.setPin(pbxuser.getPin());
		if(pbxuser.getIsAnonymous().equals(Pbxuser.ANONYMOUS_ON))
		{	
			pbxuserOld.setDefaultDIDKey(null);
			pbxuserOld.setIsAnonymous(Pbxuser.ANONYMOUS_ON);
		} else if(pbxuser.getIsAnonymous().equals(Pbxuser.ANONYMOUS_OFF) && pbxuser.getDefaultDIDKey() == null)
		{
			pbxuserOld.setDefaultDIDKey(null);
			pbxuserOld.setIsAnonymous(Pbxuser.ANONYMOUS_OFF);
		} else if(pbxuser.getDefaultDIDKey() != null)
		{
			pbxuserOld.setDefaultDIDKey(pbxuser.getDefaultDIDKey());
			pbxuserOld.setIsAnonymous(Pbxuser.ANONYMOUS_OFF);
		}
		pbxuserDAO.save(pbxuserOld);

		//setagem do user
		User userOld = pbxuserOld.getUser();
		userOld.setName(user.getName());
		userOld.setKanjiName(user.getKanjiName());
		userOld.setEmail(user.getEmail());
		userOld.setPassword(user.getPassword());
		userDAO.save(userOld);

		//setagem da preference
		Preference preferenceOld = userOld.getPreference();
		preferenceOld.setItensPerPage(preference.getItensPerPage());
		preferenceOld.setLocale(preference.getLocale());
		preferenceOld.setClickToCallConfirmation(preference.getClickToCallConfirmation());
		preferenceDAO.save(preferenceOld);

		//setagem da config
		Config configOld = pbxuserOld.getConfig();
		if(configOld.getDisableVoicemail() == Config.VOICEMAIL_ON && config.getDisableVoicemail() == Config.VOICEMAIL_OFF)
			deleteVoicemailRelationships(configOld.getKey(), pbxuserOld.getUser().getDomainKey());
		configOld.setEmailNotify(config.getEmailNotify());
		configOld.setDisableVoicemail(config.getDisableVoicemail());
		configOld.setAttachFile(config.getAttachFile());
		configOld.setEletronicLockStatus(config.getEletronicLockStatus());		
		configDAO.save(configOld);

	}
	
	private void deleteVoicemailRelationships(Long configKey, Long domainKey) throws DAOException, ValidateObjectException 
	{
		Address voicemailAddress = addDAO.getAddress(User.VOICEMAIL_NAME, domainKey);
		if(voicemailAddress != null)
		{
			List<Forward> forwardList = forwardDAO.getForwardListByConfig(configKey);
			for(Forward f : forwardList)
				if(f.getAddressKey() != null && f.getAddressKey().equals(voicemailAddress.getKey()))
				{
					f.setAddressKey(null);
					f.setStatus(Forward.STATUS_OFF);
					forwardDAO.save(f);
				}

			List<Filter> filterList = filterDAO.getFilterListByConfig(configKey);
			for(Filter filter : filterList)
				if(filter.getTarget() != null && filter.getTarget().toLowerCase().equals(User.VOICEMAIL_NAME.toLowerCase()))
					filterDAO.remove(filter);
		}
	}

	public UserForwardsInfo getForwardSettings(Long pbxuserKey, boolean loadSipIDList) throws DAOException
	{
		Pbxuser pbxuser 	= pbxuserDAO.getPbxuserAndUser(pbxuserKey);
		Forward always 		= forwardDAO.getForwardByConfig(pbxuser.getConfigKey(), Forward.ALWAYS_MODE);
		Forward no 			= forwardDAO.getForwardByConfig(pbxuser.getConfigKey(), Forward.NOANSWER_MODE);
		Forward busy 		= forwardDAO.getForwardByConfig(pbxuser.getConfigKey(), Forward.BUSY_MODE);
		Forward callFailure = forwardDAO.getForwardByConfig(pbxuser.getConfigKey(), Forward.CALL_FAILURE_MODE);
		Config config 		= configDAO.getConfigByPbxuser(pbxuser.getKey());
		Integer timeout 	= config != null ? config.getTimeoutcall() : null;
		Integer forwardDisplay = config != null ? config.getForwardType() : null;
		UserForwardsInfo info = new UserForwardsInfo(always, no, busy, callFailure, timeout, forwardDisplay);
		Integer noAnswerTimeout = Integer.valueOf(IPXProperties.getProperty(IPXPropertiesType.NOANSWER_TIMEOUT));
		info.setDefaultNoAnswerTimeout(noAnswerTimeout);
		
		if(loadSipIDList)
		{
			String[] tmp = getTerminalAssociatedWithPbxuser(pbxuser.getKey());
			if(tmp != null && tmp.length > 0)
			{
				tmp[tmp.length - 1] = pbxuser.getUser().getUsername();
				setSipIDList(info, pbxuser.getUser().getDomainKey(), true, tmp);
			} else
				setSipIDList(info, pbxuser.getUser().getDomainKey(), true, pbxuser.getUser().getUsername());
		}
		return info;
	}

	private String[] getTerminalAssociatedWithPbxuser(Long pbxuserKey) throws DAOException
	{
		List<Terminal> terminalList = terminalDAO.getTerminalListAssociatedWithPbxuser(pbxuserKey);
		if(terminalList != null && terminalList.size() > 0)
		{
			String[] tmp = new String[terminalList.size() + 1];
			for(int i = 0; i < terminalList.size(); i++)
				tmp[i] = terminalList.get(i).getPbxuser().getUser().getUsername();
			return tmp;
		}
		return null;
	}

	public void updateForwardSettings(UserForwardsInfo info) throws DAOException, ValidateObjectException
	{
		Forward forwardAlways = forwardDAO.getForwardByConfig(info.getConfigKey(), Forward.ALWAYS_MODE);
		forwardAlways.setAddressKey(info.getForwardAlwaysTargetKey());
		forwardAlways.setTarget(info.getForwardAlwaysTarget());
		forwardAlways.setStatus(info.getForwardAlwaysEnable() ? Forward.STATUS_ON : Forward.STATUS_OFF);
		forwardDAO.save(forwardAlways);

		Forward forwardNoAnswer = forwardDAO.getForwardByConfig(info.getConfigKey(), Forward.NOANSWER_MODE);
		forwardNoAnswer.setAddressKey(info.getForwardNoAnswerTargetKey());
		forwardNoAnswer.setTarget(info.getForwardNoAnswerTarget());
		forwardNoAnswer.setStatus(info.getForwardNoAnswerEnable() ? Forward.STATUS_ON : Forward.STATUS_OFF);
		forwardDAO.save(forwardNoAnswer);
		
		Forward forwardBusy = forwardDAO.getForwardByConfig(info.getConfigKey(), Forward.BUSY_MODE);
		forwardBusy.setAddressKey(info.getForwardBusyTargetKey());
		forwardBusy.setTarget(info.getForwardBusyTarget());
		forwardBusy.setStatus(info.getForwardBusyEnable() ? Forward.STATUS_ON : Forward.STATUS_OFF);
		forwardDAO.save(forwardBusy);

		Forward forwardCallFailure = forwardDAO.getForwardByConfig(info.getConfigKey(), Forward.CALL_FAILURE_MODE);
		forwardCallFailure.setAddressKey(info.getForwardCallFailureTargetKey());
		forwardCallFailure.setTarget(info.getForwardCallFailureTarget());
		forwardCallFailure.setStatus(info.getForwardCallFailureEnable() ? Forward.STATUS_ON : Forward.STATUS_OFF);
		forwardDAO.save(forwardCallFailure);
		
		Config config = configDAO.getConfigByPbxuser(info.getPbxuserKey());
		config.setForwardType(info.getForwardDisplay() ? Config.FORWARD_TYPE_FROM : Config.FORWARD_TYPE_TO);
		config.setTimeoutcall(info.getTimeoutCall());
		configDAO.save(config);
	}
	
	public PersonalDataUserInfo getPersonalDataUserInfo(Long pbxuserKey) throws DAOException
	{
		Pbxuser pbxuser = pbxuserDAO.getPbxuserFull(pbxuserKey);
		Preference preference = preferenceDAO.getPreferenceByPbxuser(pbxuserKey);
		List<Role> roleList = roleDAO.getRoleListByPbxuser(pbxuserKey);
		boolean isAdmin = false;
		boolean canViewAccount = false;
		for(Role role : roleList)
		{
			if(role.getAdmin() == Role.ROLE_ADMIN_ON)
			{
				isAdmin = true;
				canViewAccount = true;
			}		
			
			if(role.getName().equals(Role.ROLE_NAME_VIEWACCDETAILS))
				canViewAccount = true;
		}
		return new PersonalDataUserInfo(pbxuser.getUser(), preference, isAdmin, canViewAccount);
	}
	
	@SuppressWarnings("unchecked")
	public EasyCallInfo getAllPossibleContacts(Long pbxuserKey, HashMap<String, String> findStatusMap) throws DAOException 
	{
		boolean hasMoreResult = false;
		List<SimpleContactInfo> contacts = new ArrayList<SimpleContactInfo>();
		List<SimpleContactInfo> calls = new ArrayList<SimpleContactInfo>();
		Integer resultLength = 0;
		Pbxuser pbxUser = pbxuserDAO.getPbxuserAndUser(pbxuserKey);
		Domain domain = domainDAO.getByKey(pbxUser.getUser().getDomainKey());
		Integer minLimit = 0;
		Integer maxLimit = EasyCallInfo.CONTACTS_PER_CONSULT;
		// pega lista de calllogs
		minLimit = getLastIndex(findStatusMap, EasyCallInfo.FIND_CALLOG);
		List<String> callLogs = calllogDAO.getCallLogNumbersExcludingUsersAndContacts(pbxuserKey, pbxUser.getUserKey(), domain.getKey(), minLimit, minLimit + maxLimit);
		for(String call : callLogs)
			calls.add(new SimpleContactInfo(call));

		resultLength += callLogs.size();
		maxLimit = maxLimit - callLogs.size();
		
		EasyCallInfo easyCallInfo = new EasyCallInfo(calls, hasMoreResult);
		
		easyCallInfo.addFindStatus(EasyCallInfo.FIND_CALLOG, minLimit+callLogs.size());
		
		if(resultLength>=EasyCallInfo.CONTACTS_PER_CONSULT)
		{
			hasMoreResult = true;
			easyCallInfo.setHaveMoreResult(hasMoreResult);
		    return easyCallInfo;
		}
		
		minLimit = getLastIndex(findStatusMap, EasyCallInfo.FIND_USER);
		List<Integer> limits = new ArrayList<Integer>();
		limits.add(minLimit);
		limits.add(minLimit + maxLimit);

		List<SimpleContactInfo> userList = pbxuserDAO.getUsersAndPbxusersWithPresenceByDomain(pbxUser.getUser().getDomainKey(), limits);

		contacts.addAll(userList);
		
		resultLength += userList.size();
		maxLimit = maxLimit - userList.size();
		easyCallInfo.addFindStatus(EasyCallInfo.FIND_USER, limits.get(1));
		
		if(resultLength>=EasyCallInfo.CONTACTS_PER_CONSULT)
		{
			calls.addAll(contacts);
			easyCallInfo.setContactsList(calls);
			hasMoreResult = true;	
			easyCallInfo.setHaveMoreResult(hasMoreResult);
			return easyCallInfo;
		}
		
		// pega lista de grupos
		Presence groupPresence = new Presence();
		groupPresence.setState(Presence.STATE_ONLINE);

		minLimit = getLastIndex(findStatusMap, EasyCallInfo.FIND_GROUP);
		maxLimit = minLimit+EasyCallInfo.CONTACTS_PER_CONSULT;
		List<Group> groups = groupDAO.getGroupsInPBX(pbxDAO.getPbxByDomain(domain.getKey()).getKey(), minLimit, minLimit + maxLimit);
		for(Group g :  groups)
		{
			g.setAddressList(addDAO.getAddressFullListByGroup(g.getKey()));
			List<PhoneInfo> phones = new ArrayList<PhoneInfo>();
			for(Address address : g.getAddressList())
				phones.add(new PhoneInfo(address.getType(), address.getAddress()));
			contacts.add(new SimpleContactInfo(g, groupPresence, phones));
		}
		
		resultLength += groups.size();
		maxLimit = maxLimit - groups.size();
		
		easyCallInfo.addFindStatus(EasyCallInfo.FIND_GROUP, minLimit+groups.size());
		
		if(resultLength>=EasyCallInfo.CONTACTS_PER_CONSULT)
		{
			calls.addAll(contacts);
			easyCallInfo.setContactsList(calls);
			hasMoreResult = true;
			easyCallInfo.setHaveMoreResult(hasMoreResult);
			return easyCallInfo;
		}
	
		// pega lista de ivrs
		minLimit = getLastIndex(findStatusMap, EasyCallInfo.FIND_IVR);
		maxLimit = minLimit+EasyCallInfo.CONTACTS_PER_CONSULT;
		List<IVR> ivrs = ivrDAO.getIVRListByDomain(domain.getDomain(), minLimit, minLimit + maxLimit);
		for(IVR ivr : ivrs)
		{
			ivr.setAddressList(addDAO.getAddressFullListByPbxuser(ivr.getPbxuserKey()));
			List<PhoneInfo> phones = new ArrayList<PhoneInfo>();
			for(Address address : ivr.getAddressList())
				phones.add(new PhoneInfo(address.getType(), address.getAddress()));
			contacts.add(new SimpleContactInfo(ivr, phones));
		}
		
		resultLength += ivrs.size();
		maxLimit = maxLimit - ivrs.size();
		
		
		easyCallInfo.addFindStatus(EasyCallInfo.FIND_IVR, minLimit+ivrs.size());
		
		if(resultLength>=EasyCallInfo.CONTACTS_PER_CONSULT)
		{
			calls.addAll(contacts);
		    easyCallInfo.setContactsList(calls);
		 	hasMoreResult = true;	
			easyCallInfo.setHaveMoreResult(hasMoreResult);
			return easyCallInfo;
		}
	
		// pega todos os contatos.
		minLimit = getLastIndex(findStatusMap, EasyCallInfo.FIND_CONTACT);
		maxLimit = minLimit+EasyCallInfo.CONTACTS_PER_CONSULT;
		List<Contact> contactList = contactDAO.getAllContacts(pbxUser.getUser().getKey(), pbxUser.getUser().getDomainKey(), minLimit, minLimit + maxLimit);
		String phone = null;
		for(Contact contact : contactList)
		{
			List<PhoneInfo> phones = new ArrayList<PhoneInfo>();
			List<Contactphones> contactPhonesList = contactPhonesDAO.getContactphonesListByContact(contact.getKey());
			for(Contactphones contactphones : contactPhonesList)
			{
				//dnakamashi - bug #5702 -- prefixo adicionado ao telefone
				phone = contactphones.getPrefix() != null ? contactphones.getPrefix() + contactphones.getPhone() : contactphones.getPhone();
				phones.add(new PhoneInfo(contactphones.getType(), phone));
			}	
			contacts.add(new SimpleContactInfo(contact, phones));
		}
		//if(contactList.size() >= EasyCallInfo.CONTACTS_PER_CONSULT)
		//	hasMoreResult = true;
		hasMoreResult = false;
		calls.addAll(contacts);
		easyCallInfo.setHaveMoreResult(hasMoreResult);
		easyCallInfo.setContactsList(calls);
		easyCallInfo.addFindStatus(EasyCallInfo.FIND_CONTACT, minLimit+contactList.size());
		return easyCallInfo;
	}

	private Integer getLastIndex(HashMap<String, String> findStatusMap, String key)
	{
		return findStatusMap != null && findStatusMap.get(key) != null ? Integer.parseInt(findStatusMap.get(key)) : 0;
	}
	
	public LoginInfo validateUserAndDomain(LoginInfo loginInfo) throws DAOException 
	{
	
		LoginInfo newLoginInfo = null;
		User u = userDAO.getUserByUsernameAndDomain(loginInfo.getUsername(), loginInfo.getDomain());
	
		if(u != null)
			newLoginInfo = new LoginInfo(u.getUsername(), u.getDomain().getDomain(), u.getPassword(), u.getDomainKey());

		return newLoginInfo;
	}
	
	public LoginInfo validateUserbyEmail(LoginInfo loginInfo) throws DAOException 
	{
		LoginInfo newLoginInfo = null;
		User u = userDAO.getUserByEmail(loginInfo.getUsername() + "@" + loginInfo.getDomain());
		
		if(u != null)
			newLoginInfo = new LoginInfo(u.getUsername(), u.getDomain().getDomain(), u.getPassword(), u.getDomainKey());

		return newLoginInfo;
	}

	public ManagementInfo getManagementInformations(Long pbxuserKey) throws DAOException
	{
		PermissionManager permManager = new PermissionManager(logger.getName());
		Pbxuser pu = pbxuserDAO.getPbxuserFull(pbxuserKey);
		Config c = pu.getConfig();

		Integer voicemailUserEnable = c.getDisableVoicemail() == Config.VOICEMAIL_ON ? ManagementInfo.VM_USER_ENABLE_ON : ManagementInfo.VM_USER_ENABLE_OFF;
		Integer isNightmodeActiveEnable = permManager.checkPermission(pu.getUserKey(), Permissions.CONFIG_NIGHTMODE_USER) ? ManagementInfo.USER_PBXNIGHTMODE_ACTIVE_ON : ManagementInfo.USER_PBXNIGHTMODE_ACTIVE_OFF;
		Integer isSystemAdministrator = permManager.checkPermission(pu.getUserKey(), Permission.BASIX_WCA_ADMIN) ? ManagementInfo.USER_SYSTEMADMINISTRATOR_ON : ManagementInfo.USER_SYSTEMADMINISTRATOR_OFF;

		return new ManagementInfo(isNightmodeActiveEnable, isSystemAdministrator, voicemailUserEnable);
	}

	public Long getPbxuserKeyByAddressAndDomain(String extension, String domain) throws DAOException
	{
		Address add = addDAO.getAddress(extension, domain);
		return add != null ? add.getPbxuserKey() : null;
	}
	
	public Pbxuser getPbxuserByAddressAndDomain(String extension, String domain) throws DAOException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserByAddressAndDomain(extension, domain);
		return pu;
	}	
	
	public Pbxuser getPbxUserWithExtensionList(String username, String domain) throws DAOException
	{
		Pbxuser pu = getPbxuserByAddressAndDomain(username, domain);
		pu.setAddressList(addDAO.getExtensionListByPbxuser(pu.getKey()));
		return pu;
	}
	
	public Pbxuser getPbxuserByTerminal(String terminal, String domain) throws DAOException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserByTerminal(terminal, domain);
		return pu;
	}
	
	public Long getPbxuserKeyByAddressAndDomainKey(String extension, Long domainKey) throws DAOException
	{
		Address add = addDAO.getAddress(extension, domainKey);
		return add != null ? add.getPbxuserKey() : null;
	}
	
	public List<Pbxuser> getPbxusersAndTerminalsWithConfigByDomain(Long domainKey) throws DAOException{
		return pbxuserDAO.getPbxusersWithConfigByDomain(domainKey, User.TYPE_PBXUSER, User.TYPE_TERMINAL);
	}
	
	public boolean verifyForwardEnable(Long pbxuserKey, ForwardMode forwardMode) throws DAOException
	{
		Pbxuser pu = pbxuserDAO.getByKey(pbxuserKey);
		Forward forward = forwardDAO.getForwardByConfig(pu.getConfigKey(),forwardMode.getValue());		
		return forward.getStatus() == Forward.STATUS_ON;
	}
	
	public Pbxuser getPbxuserByConfig(Long configKey) throws DAOException
	{
		return pbxuserDAO.getPbxuserByConfig(configKey);
	}
	
	public Pbxuser getPbxuserByPresence(Long presenceKey) throws DAOException
	{
		return pbxuserDAO.getPbxuserByPresence(presenceKey);
	}
	
	
	public User getUserWithpreference(Long userKey) throws DAOException
	{
		return userDAO.getUserWithPreference(userKey);
	}

	private String[] getExcludesSipID(Long pbxuserKey) throws DAOException
	{
		String[] tmp = null;
		
		if(pbxuserKey != null)
		{
			Pbxuser pbxuser = pbxuserDAO.getPbxuserAndUser(pbxuserKey);
			tmp = getTerminalAssociatedWithPbxuser(pbxuser.getKey());
			if(tmp != null)
				tmp[tmp.length - 1] = pbxuser.getUser().getUsername();
			else if( pbxuser.getUser().getUsername() != null)
			{
				tmp = new String[1];
				tmp[0] = pbxuser.getUser().getUsername();
			}	
		}
		
		return tmp;
	}
	
	public UserForwardsInfo getUserForwardSipIDLists(UserForwardsInfo info, Integer lastIndex, Long pbxuserKey)throws WebAdminException, RemoteException, DAOException
	{
		int maxLength = UserForwardsInfo.LISTS_MAX_LENGTH;	
		String[] tmp = getExcludesSipID(pbxuserKey);			
				
		List<Duo<Long, String>> usernameSipIDList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> groupNameSipIDList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> terminalNameSipIDList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> IVRSipIDList = new ArrayList<Duo<Long,String>>();
		List<Duo<Long, String>> sipTrunkSipIDList = new ArrayList<Duo<Long,String>>();
		
		info.setListLastIndex(lastIndex);
		
		if(!info.isUsernameListLoaded())
		{
			if(tmp != null)
				usernameSipIDList = addDAO.getUsernameSIPIDList(info.getDomainKey(), true, info.getListLastIndex(), maxLength, tmp);
			else
				usernameSipIDList = addDAO.getUsernameSIPIDList(info.getDomainKey(), true, info.getListLastIndex(), maxLength);
					
			if(usernameSipIDList.size() < maxLength)
			{
				info.setUsernameListLoaded(true);
				info.setListLastIndex(0);
			}
			else
				info.setListLastIndex(info.getListLastIndex() + usernameSipIDList.size());							
			
			maxLength -= usernameSipIDList.size();			
		}
		
		if(!info.isSipTrunkListLoaded())
		{
			if(tmp != null)
				sipTrunkSipIDList = addDAO.getSipTrunkSIPIDList(info.getDomainKey(), info.getListLastIndex(), maxLength, tmp);
			else
				sipTrunkSipIDList = addDAO.getSipTrunkSIPIDList(info.getDomainKey(), info.getListLastIndex(), maxLength);
					
			if(sipTrunkSipIDList.size() < maxLength)
			{
				info.setSipTrunkListLoaded(true);
				info.setListLastIndex(0);
			}
			else
				info.setListLastIndex(info.getListLastIndex() + sipTrunkSipIDList.size());							
			
			maxLength -= sipTrunkSipIDList.size();			
		}
		
		if(!info.isGroupListLoaded() && maxLength > 0)
		{			
			groupNameSipIDList = addDAO.getGroupNameSipIDList(info.getDomainKey(), false, info.getListLastIndex(), maxLength);
			
				
					
			if(groupNameSipIDList.size() < maxLength)
			{
				info.setGroupListLoaded(true);
				info.setListLastIndex(0);
			}
			else
				info.setListLastIndex(info.getListLastIndex() + groupNameSipIDList.size());
			
			maxLength -= groupNameSipIDList.size();			
		}
		
		if(!info.isTerminalListLoaded() && maxLength > 0)
		{
			if(tmp != null)
				terminalNameSipIDList = addDAO.getTerminalNameSipIDList(info.getDomainKey(), info.getListLastIndex(), maxLength, tmp);				
			else
				terminalNameSipIDList = addDAO.getTerminalNameSipIDList(info.getDomainKey(), info.getListLastIndex(), maxLength);
		
			if(terminalNameSipIDList.size() < maxLength)
			{
				info.setTerminalListLoaded(true);
				info.setListLastIndex(0);
			}
			else
				info.setListLastIndex(info.getListLastIndex() + terminalNameSipIDList.size());
			
			maxLength -= terminalNameSipIDList.size();
		}
		
		if(!info.isIvrListLoaded() && maxLength > 0)
		{
			IVRSipIDList = addDAO.getIVRSipIDList(info.getDomainKey(), info.getListLastIndex(), maxLength);
			
			if(IVRSipIDList.size() < maxLength)
				info.setIvrListLoaded(true);
			else
				info.setListLastIndex(info.getListLastIndex() + IVRSipIDList.size());
		}		
	
		info.addUsernameSipIDList(usernameSipIDList);
		info.addGroupNameSipIDList(groupNameSipIDList);
		info.addTerminalNameSipIDList(terminalNameSipIDList);
		info.addIVRSipIDList(IVRSipIDList);	
		info.addSipTrunkNameSipIDList(sipTrunkSipIDList);
		
		return info;			
	}
	
	
	public List<Duo<Long, String>> getUsernameSipIDList(Long pbxuserKey, Integer lastIndex) throws DAOException
	{
		int listLength = 4000;
		List<Duo<Long, String>> usernameSipIDList = new ArrayList<Duo<Long,String>>();
		Pbxuser pbxuser = pbxuserDAO.getPbxuserAndUser(pbxuserKey);
		
		String[] tmp = getTerminalAssociatedWithPbxuser(pbxuser.getKey());
		if(tmp != null && tmp.length > 0)
		{
			tmp[tmp.length - 1] = pbxuser.getUser().getUsername();
			usernameSipIDList = addDAO.getUsernameSIPIDList(pbxuser.getUser().getDomainKey(), true, lastIndex, listLength, tmp);
		}	
		else
			usernameSipIDList = addDAO.getUsernameSIPIDList(pbxuser.getUser().getDomainKey(), true, lastIndex, listLength, pbxuser.getUser().getUsername());
		
		return usernameSipIDList;
	}
	
	//inicio --> dnakamashi - Caixa de  Gravação- 3.0.5
	public Preference getPreferenceByUsernameAndDomain(String username, String domain) throws DAOException
	{
		Preference pref = preferenceDAO.getPreferenceByUsernameAndDomain(username, domain);
		return pref;		
	}
	//fim --> dnakamashi - Caixa de  Gravação- 3.0.5

	//tveiga - issue 4329 - inicio 
	public PbxuserInfo isGroupAdministrator(Long key) throws DAOException {
		PbxuserInfo info = new PbxuserInfo();
		boolean isGroupAdmin = pbxuserDAO.isGroupAdministrator(key);
		info.setGroupAdministrator(isGroupAdmin);
		info.setKey(isGroupAdmin ? key : null);
		return info;
	}
	//tveiga issue 4329 - fim 
	
	public List<Long> getGroupkeyListOfGroupAdministrator(Long key) throws DAOException 
	{
		return pbxuserDAO.getGroupkeyListOfGroupAdministrator(key);
	}

	public void changePbxuserRecordCallEnable(boolean isEnable, Long pbxuserKey, Long domainKey) throws DAOException, ValidateObjectException 
	{
		if(isEnable)
			validateMaxRecordCallUsers(domainKey, pbxuserKey);
		
		Config config = configDAO.getConfigByPbxuser(pbxuserKey);
		
		config.setAllowedRecordCall(isEnable ? Config.ALLOWEDRECORDCALL_ON : Config.ALLOWEDRECORDCALL_OFF);
		
		configDAO.save(config);
	}
	
	public void changePbxuserVideoCallEnable(boolean isEnable, Long pbxuserKey, Long domainKey) throws DAOException, ValidateObjectException 
	{
		if(isEnable)
			validateMaxVideoCallUsers(domainKey, pbxuserKey);
		
		Config config = configDAO.getConfigByPbxuser(pbxuserKey);
		
		config.setAllowedVideoCall(isEnable ? Config.ALLOWED_VIDEOCALL : Config.NOT_ALLOWED_VIDEOCALL);
		
		configDAO.save(config);
	}
	
//rribeiro - integração do CallMe no WCC - inicio - 3.1.4
	public PbxuserInfo getPBXuserInfoByUsernameDomain(String username,String domain) throws DAOException, ValidateObjectException{
		Pbxuser pbxuser = pbxuserDAO.getPbxuserByUsernameAndDomain(username, domain);
		if (pbxuser == null)
			return null;
		String locale = pbxuser.getUser().getPreference().getLocale();
		PbxuserInfo info = new PbxuserInfo(pbxuser);
		info.setLocale(locale);
		return info;
	}
	//rribeiro - integração do CallMe no WCC - fim - 3.1.4

	public void manageCostCenterUsers(Long[] checkeds, Long[] uncheckeds) throws DAOException
	{
		pbxuserDAO.updateCostCenters(checkeds, uncheckeds);
	}

	public void changeEletronicLockStatus(SipAddressParser from, int status, String pin) throws DAOException, ValidateObjectException
	{
		 Pbxuser pu = pbxuserDAO.getPbxuserByAddressAndDomain(from.getExtension(), from.getDomain());
	        if(pu == null || !pu.getPin().equals(pin))
	        	throw new CommandConfigException("Command:changeEletronicLockStatus Error. Please check your extension or pin!!!");
	        
	     Config config = configDAO.getConfigByPbxuser(pu.getKey());
	     if(status != Config.ELETRONICLOCK_OFF && status != Config.ELETRONICLOCK_ON)
	    	 throw new CommandConfigException("Command:changeEletronicLockStatus Error. Error in Status!!");
	     
	     config.setEletronicLockStatus(status);
	     configDAO.save(config);	
	}
}