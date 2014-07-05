package br.com.voicetechnology.ng.ipx.rule.implement;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.UserWithoutSipSessionlogException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.command.LoginACDException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.command.PauseException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.jms.tools.JMSNotificationTools;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.ActivecallDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallCenterConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupPauseDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PauseDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ServiceclassDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipsessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.UsergroupDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.FileinfoDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.GroupfileDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcenter.values.PauseEvent;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.db.PersistentObject;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterConfig;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.GroupPause;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pause;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Groupfile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.facets.group.UserInGroupFacet;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.Info;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ACDInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.GroupInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserInACDGroupsInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UsergroupInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.implement.voicemail.VoicemailManager;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class GroupManager extends Manager
{
	private AddressManager addManager;
	private AddressDAO addDAO;
	private GroupDAO groupDAO;
	private ForwardDAO forwardDAO;
	private ConfigDAO configDAO;
	private PbxuserDAO pbxuserDAO;
	private DomainDAO domainDAO;
	private UsergroupDAO ugDAO;
	private SipsessionlogDAO sipSessionLogDAO;
	private FileinfoDAO fileDAO;
	private GroupfileDAO gfDAO;
	private VoicemailManager voicemailManager;
	private PbxpreferenceDAO pbxPreferenceDAO;
	private ServiceclassDAO serviceclassDAO;
	private PbxDAO pbxDAO;
	private CallCenterConfigDAO callCenterConfigDAO;
	private NightModeSchedulerManager nightModeSchedulerManager;
	private PauseDAO pauseDAO;
	private GroupPauseDAO groupPauseDAO;
	private ActivecallDAO aDAO;

	public GroupManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		addManager = new AddressManager(logger);
		ugDAO = dao.getDAO(UsergroupDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		groupDAO = dao.getDAO(GroupDAO.class);
		forwardDAO = dao.getDAO(ForwardDAO.class);
		configDAO = dao.getDAO(ConfigDAO.class);
		pbxuserDAO = dao.getDAO(PbxuserDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
		ugDAO = dao.getDAO(UsergroupDAO.class);
		sipSessionLogDAO = dao.getDAO(SipsessionlogDAO.class);
		fileDAO = dao.getDAO(FileinfoDAO.class);
		gfDAO = dao.getDAO(GroupfileDAO.class);
		voicemailManager = new VoicemailManager(logger.getName());
		pbxPreferenceDAO = dao.getDAO(PbxpreferenceDAO.class);
		serviceclassDAO = dao.getDAO(ServiceclassDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		callCenterConfigDAO = dao.getDAO(CallCenterConfigDAO.class);
		nightModeSchedulerManager = new NightModeSchedulerManager(logger);
		pauseDAO = dao.getDAO(PauseDAO.class);
		groupPauseDAO = dao.getDAO(GroupPauseDAO.class); 
		aDAO = dao.getDAO(ActivecallDAO.class);
	}

	public <T extends Info> ReportResult<T> findGroups(Report<GroupInfo> report) throws DAOException
	{
		ReportDAO<Group, GroupInfo> gReport = dao.getReportDAO(GroupDAO.class);
		Long size = gReport.getReportCount(report);
		List<Group> gList = gReport.getReportList(report);
		List<GroupInfo> gInfoList = new ArrayList<GroupInfo>(gList.size());
		Domain domain = domainDAO.getDomainByPbx(report.getInfo().getPbxKey());
		List<Duo<Long, String>> fileList = fileDAO.listSimpleFiles(domain.getKey());
		for(Group g : gList)
		{
			GroupInfo gInfo = new GroupInfo(g);
			List<Address> addressList = addDAO.getExtensionListByGroup(g.getKey());
			List<Duo<Long, String>> addressDuoList = new ArrayList<Duo<Long,String>>(addressList.size());
			for (Address address : addressList) 
				addressDuoList.add(new Duo<Long, String>(address.getKey(), address.getAddress()));
			gInfo.addExtensionList(addressDuoList);
			Groupfile gfDefault = gfDAO.getSalutationByGroupKey(g.getKey(), Groupfile.DEFAULT_FILE);
			if(gfDefault != null)
				g.setDefaultSalutationFileKey(gfDefault.getFileinfoKey());
			Groupfile gfNightMode = gfDAO.getSalutationByGroupKey(g.getKey(), Groupfile.NIGHTMODE_FILE);
			if(gfNightMode != null)
				g.setNightModeSalutationFileKey(gfNightMode.getFileinfoKey());
			gInfo.addFileList(fileList);
			gInfoList.add(gInfo);
			
			if(g.getServiceclassKey() != null)
				gInfo.setServiceclass(serviceclassDAO.getByKey(g.getServiceclassKey()));
		}
		
		return (ReportResult<T>) new ReportResult<GroupInfo>(gInfoList, size);
	}
	
	public ReportResult<UserInACDGroupsInfo> findUsersInACDGroups(Report<UserInACDGroupsInfo> userACDGroupReport) throws DAOException 
	{
		ReportDAO<Usergroup, UserInACDGroupsInfo> report = dao.getReportDAO(UsergroupDAO.class);

		Long size = report.getReportCount(userACDGroupReport);
		
		List<Usergroup> ugList = report.getReportList(userACDGroupReport);
		
		List<UserInACDGroupsInfo> userACDInfoList = new ArrayList<UserInACDGroupsInfo>();
		List<String> usuariosUsados = new ArrayList<String>();

		for(Usergroup ug : ugList) //1. Trecho para Agrupar grupos aos seus Usuarios
		{
			Pbxuser pu = ug.getPbxuser();

			boolean logged = ug.getLoginState() == Usergroup.LOGIN_ON;

			Calendar lastLoginChange = ug.getLoginChange();

			List<String> groups = new ArrayList<String>();

			for (Usergroup tmpUG : ugList) //1.1 Percorre lista para Buscar proximos registro do usuario pu 
			{
				if((pu.getUser().getUsername().equals(tmpUG.getPbxuser().getUser().getUsername())) 
						&& !usuariosUsados.contains(tmpUG.getPbxuser().getUser().getName())){

					groups.add(tmpUG.getGroup().getName());

					if((lastLoginChange != null || tmpUG.getLoginChange() != null ) && (lastLoginChange.after(tmpUG.getLoginChange()))){
						lastLoginChange = tmpUG.getLoginChange();
					}
				}
			}

			usuariosUsados.add(ug.getPbxuser().getUser().getName());//1.2 Utiliza um List utilitário para dizer quais usuarios já foram usados

			if(groups.size() == 0) //1.3 Se o List group for == 0, quer dizer que o pu já foi processado anteriormente
			{
				continue;
			}

			UserInACDGroupsInfo userACDInfo = new UserInACDGroupsInfo(pu, groups, logged, lastLoginChange);

			Long sipSessions = sipSessionLogDAO.getHowManySipSessionByPbxuser(pu.getKey());

			if(sipSessions.longValue() == 0L)
			{
				List<Sipsessionlog> sipTerminalList = sipSessionLogDAO.getActiveSipSessionLogListByTerminalOfPbxuser(pu.getKey());
				userACDInfo.setOffline(sipTerminalList == null || sipTerminalList.size() == 0);
			} else
				userACDInfo.setOffline(false);

			userACDInfoList.add(userACDInfo);
		}
		//1.4 Verifica se a Ordenação é feita por LOGINCHANGE, pois a ordenação não é possivel ser feita via Query
		if(userACDGroupReport.getOrderBy() != null && userACDGroupReport.getOrderBy()[0].toString().equals(Usergroup.Fields.LOGINCHANGE.toString()))
		{

			if(userACDGroupReport.getOrder().toString().equals("ASC"))
			{
				userACDInfoList = this.organizeListByLoginState(userACDInfoList, "ASC");
			}else{
				userACDInfoList = this.organizeListByLoginState(userACDInfoList, "DESC");
			}
		}
		return new ReportResult<UserInACDGroupsInfo>(userACDInfoList, size);
	}
	
	
	public List<UserInACDGroupsInfo> organizeListByLoginState(List<UserInACDGroupsInfo> userACDInfoList, String orderBy){ // Metodo Para Organizar Lista de <UserInACDGroupsInfo> , possue um comparator para fazer a ordenação por Data.
		
		//1. Cria Comparator para Ordenação da Lista por Data
		class OrganizeLoginChange implements Comparator<UserInACDGroupsInfo>{

			String orderBy = "ASC";
			
			//1.1 Necessário saber o Tipo de Ordenação ASC/DESC
			public OrganizeLoginChange(String orderBy) {
				this.orderBy = orderBy;
			}
			
			//1.2 Compara a data de 2 formas diferentes, por ASC ou DESC
			public int compare(UserInACDGroupsInfo o1, UserInACDGroupsInfo o2) {
				
				if(orderBy.equals("ASC")){
					
					if(((o1.getLastLoginChange() != null && o2.getLastLoginChange() != null) && (o1.getLastLoginChange().after(o2.getLastLoginChange())))){
						return 1;
					}else if ((o1.getLastLoginChange() != null && o2.getLastLoginChange() != null) && (o1.getLastLoginChange().before(o2.getLastLoginChange()))){
						return -1;
					}else{
						return 1;
					}
					
				}else if(orderBy.equals("DESC")){
					
					if(((o1.getLastLoginChange() != null && o2.getLastLoginChange() != null) && (o1.getLastLoginChange().after(o2.getLastLoginChange())))){
						return -1;
					}else if (((o1.getLastLoginChange() != null && o2.getLastLoginChange() != null) && (o1.getLastLoginChange().before(o2.getLastLoginChange())))){
						return 1;
					}else{
						return 1;
					}
				}
				return 0;
			}
		}
		
		//2. Cria Comparator, passando o OrderBy(ASC/DESC)
		OrganizeLoginChange organizeList = new OrganizeLoginChange(orderBy);
		
		//3. Ordena a Lista
		Collections.sort(userACDInfoList,organizeList);
		
		//4. Retorna Lista
		return userACDInfoList;
	}

	public GroupInfo getGroupInfoByKey(Long groupKey) throws DAOException
	{
		Group g = groupDAO.getGroupFull(groupKey);	//group, config
		GroupInfo info = null;
		
		//inicio --> dnakamashi - bug #6109 - version 3.0.5
		Forward fNoAnswer = forwardDAO.getForwardByConfig(g.getConfigKey(), Forward.NOANSWER_MODE);
		Forward fAlways = forwardDAO.getForwardByConfig(g.getConfigKey(), Forward.ALWAYS_MODE);
		
		if (g.getGroupType() == Group.ACDCALLCENTER_GROUP)
		{
			Forward fMaxWaitingTime = forwardDAO.getForwardByConfig(g.getConfigKey(), Forward.MAX_WAITING_TIME);
			Forward fGroupEmpty = forwardDAO.getForwardByConfig(g.getConfigKey(), Forward.GROUP_EMPTY);
			Forward fGroupUnavailable = forwardDAO.getForwardByConfig(g.getConfigKey(), Forward.GROUP_UNAVAILABLE);
			info = new GroupInfo(g, fNoAnswer, fAlways, fMaxWaitingTime, fGroupEmpty, fGroupUnavailable);
		}
		else
			info = new GroupInfo(g, fNoAnswer, fAlways); 
		//fim --> dnakamashi - bug #6109 - version 3.0.5
		
		Groupfile gfDefault = gfDAO.getSalutationByGroupKey(g.getKey(), Groupfile.DEFAULT_FILE);
		if(gfDefault != null)
			g.setDefaultSalutationFileKey(gfDefault.getFileinfoKey());
		Groupfile gfNightMode = gfDAO.getSalutationByGroupKey(g.getKey(), Groupfile.NIGHTMODE_FILE);
		if(gfNightMode != null)
			g.setNightModeSalutationFileKey(gfNightMode.getFileinfoKey());
		
		//TODO analisar neste ponto a quest�o de que uma query foi acrescida.
		Long domainKey = domainDAO.getDomainByPbx(g.getPbxKey()).getKey();
		
		List<Usergroup> inList = groupDAO.getUsersInGroup(groupKey);
		for(Usergroup ug : inList)
			info.addUserInGroup(new UsergroupInfo(ug, ug.getPbxuser().getUser().getUsername(), ug.getPbxuser().getUser().getName()));

		info.addUserOutGroup(groupDAO.getUsersOutGroup(groupKey, domainKey, User.TYPE_PBXUSER));
		info.setSiptrunkOutList(groupDAO.getUsersOutGroup(groupKey, domainKey, User.TYPE_SIPTRUNK));
		List<Duo<Long, String>> fileList = fileDAO.listSimpleFiles(domainKey);
		
		info.addFileList(fileList);
		
		List<Address> extList = addDAO.getExtensionListByGroup(groupKey);
		g.setAddressList(extList);
		for(Address ext : extList)
			info.addExtension(new Duo<Long, String>(ext.getKey(), ext.getAddress()));

		List<Address> didList = addDAO.getDIDListByGroup(groupKey);
		for(Address did : didList)
			info.addDID(new Duo<Long, String>(did.getKey(), did.getAddress()));

		info.addUsernameSipIDList(addDAO.getPbxuserKeyAndSipIDByDomain(domainKey, true));
		info.addGroupNameSipIDList(addDAO.getGroupKeyAndSipIDByDomain(domainKey, false, g.getName()));
		info.addTerminalNameSipIDList(addDAO.getTerminalPbxuserKeyAndTerminalNAmeByDomain(domainKey));
		info.addIVRSipIDList(addDAO.getIVRPbxuserKeyAndIVRNameByDomain(domainKey));
		info.setGroupRingMaxUsers(Integer.parseInt(IPXProperties.getProperty(IPXPropertiesType.GROUP_RING_MAX_USERS)));
		
		//Issue: ???? Eduardo Zaghi <Inicio>
		info.addClassOfService(serviceclassDAO.getServiceclassKeyAndName(g.getPbxKey()));
		info.addClassOfService(serviceclassDAO.getCentrexServiceclassKeyAndName());
		//Issue: ???? Eduardo Zaghi <Fim>
		
		if (g.getGroupType() == Group.ACDCALLCENTER_GROUP)
		{
			CallCenterConfig callCenterConfig = callCenterConfigDAO.getByKey(g.getCallcenterConfigKey());
			info.setAllNightModeSchedulers(nightModeSchedulerManager.getNightModeSchedulerListByGroupKey(groupKey));
			if (callCenterConfig != null)
			{				
				g.setCallcenterConfigKey(callCenterConfig.getKey());
				g.setCallCenterConfig(callCenterConfig);

				Groupfile gfCallCenterSalutation = gfDAO.getSalutationByGroupKey(g.getKey(), Groupfile.CALLCENTER_SALUTATION_FILE);
				if(gfCallCenterSalutation != null)
					g.getCallCenterConfig().setCallCenterSalutationFileKey(gfCallCenterSalutation.getFileinfoKey());
				Groupfile gfCallCenterEndFile = gfDAO.getSalutationByGroupKey(g.getKey(), Groupfile.CALLCENTER_END_FILE);
				if(gfCallCenterEndFile != null)
					g.getCallCenterConfig().setCallCenterEndFileKey(gfCallCenterEndFile.getFileinfoKey());
				Groupfile gfCallCenterGroupEmptyFile = gfDAO.getSalutationByGroupKey(g.getKey(), Groupfile.CALLCENTER_GROUP_EMPTY_FILE);
				if(gfCallCenterGroupEmptyFile != null)
					g.getCallCenterConfig().setCallCenterGroupEmptyFileKey(gfCallCenterGroupEmptyFile.getFileinfoKey());
				Groupfile gfCallCenterGroupFullFile = gfDAO.getSalutationByGroupKey(g.getKey(), Groupfile.CALLCENTER_GROUP_FULL_FILE);
				if(gfCallCenterGroupFullFile != null)
					g.getCallCenterConfig().setCallCenterGroupFullFileKey(gfCallCenterGroupFullFile.getFileinfoKey());
				
				callCenterConfig.setConfigkey(g.getConfigKey());
				info.setTimeoutqueue(callCenterConfig.getTimeoutqueue());
				info.setQueueSize(callCenterConfig.getQueueSize());
				info.setAllowedQueue(callCenterConfig.getAllowedQueue());
				info.setQueueSizeType(callCenterConfig.getQueueSizeType());
				info.setAllowedWorkingHours(callCenterConfig.getAllowedWorkingHours());
				info.setCallcenterConfigKey(callCenterConfig.getKey());
				//CallCenter monitoring - inicio
				info.setEstablishedTimeouts(g.getCallCenterConfig().getEstablishedTimeouts());
				info.setQueueCallsLimit(g.getCallCenterConfig().getQueueCallsLimit());
				info.setEstablishedServiceLevelTimes(g.getCallCenterConfig().getEstablishedServiceLevelTimes());
				info.setMoreTimeQueueTimeouts(g.getCallCenterConfig().getMoreTimeQueueTimeouts());
				info.setAbandonedCallsInQueueLimit(g.getCallCenterConfig().getAbandonedCallsInQueueLimit());
				info.setAbandonedServiceLevelTimes(g.getCallCenterConfig().getAbandonedServiceLevelTimes());
				info.setTsfTimeThreshold(g.getCallCenterConfig().getTsfTimeThreshold());
				info.setTsfLimits(g.getCallCenterConfig().getTsfLimits());
				//CallCenter monitoring - fim
				
				g.setPauseMap(buildPausesMap(domainKey));
				g.setGroupPauseList(groupPauseDAO.getGroupPauseListByGroupKey(g.getKey()));
			}
		}
		
		return info;
	}
	
	private HashMap<Long, Pause> buildPausesMap(Long key) throws DAOException
	{
		List<Pause> pauseList = pauseDAO.getPauseByDomainKey(key);
		HashMap<Long, Pause> options = new HashMap<Long, Pause>();
		for(Pause opt : pauseList)
			options.put(opt.getKey(), opt);
		return options;
	}
	
	public GroupInfo getGroupInfoContext(Long pbxKey) throws DAOException
	{
		Group g = new Group();
       
		GroupInfo groupInfo = new GroupInfo(g);
		//TODO analisar neste ponto a quest�o de que uma query foi acrescida.
		Long domainKey = domainDAO.getDomainByPbx(pbxKey).getKey();
	//	groupInfo.addUsernameSipIDList(addDAO.getPbxuserKeyAndSipIDByDomain(domainKey, true));
	//	groupInfo.addGroupNameSipIDList(addDAO.getGroupKeyAndSipIDByDomain(domainKey, false));
	//	groupInfo.addTerminalNameSipIDList(addDAO.getTerminalPbxuserKeyAndTerminalNAmeByDomain(domainKey));
	//	groupInfo.addIVRSipIDList(addDAO.getIVRPbxuserKeyAndIVRNameByDomain(domainKey));
		List<Duo<Long, String>> fileList = fileDAO.listSimpleFiles(domainKey);
		groupInfo.addFileList(fileList);
		
		groupInfo.setDefaultSalutationFileKey(null);			
	    groupInfo.setNightModeSalutationFileKey(null);
	    
		//List<Duo<Long, String>> userList = addDAO.getPbxuserKeyAndSipIDByDomain(domainKey, false);
		//TODO fazer m�todo getGroupByKey utilizar este m�todo e a partir da� retirar esse "-1L".
		groupInfo.addUserOutGroup(groupDAO.getUsersOutGroup(-1L, domainKey, User.TYPE_PBXUSER));
		groupInfo.setSiptrunkOutList(groupDAO.getUsersOutGroup(-1L, domainKey, User.TYPE_SIPTRUNK));
		groupInfo.setGroupRingMaxUsers(Integer.parseInt(IPXProperties.getProperty(IPXPropertiesType.GROUP_RING_MAX_USERS)));
		return groupInfo;
	}

	//inicio --> dnakamashi - OptionsFeatures- 3.0.5
	private void manageOptionsFeatures(GroupInfo info) throws DAOException
	{
		Group g = info.getGroup();
		Pbxpreference pbxPreference = pbxPreferenceDAO.getByPbxKey(g.getPbxKey());		
		g.setDomainKey(pbxPreference.getPbx().getDomainKey());
		
		if(g.getKey() == null)
		{
			if(pbxPreference.getOptionsFeaturesEnable() == Pbxpreference.OPTIONS_FEATURES_ENABLE_ON)
			{
				info.setAllowedGroupForward(Config.ALLOWED_GROUPFORWARD);
				info.setAllowedVoicemail(Config.ALLOWED_VOICEMAIL);
			}			
			else
			{
				info.setAllowedGroupForward(Config.NOT_ALLOWED_GROUPFORWARD);
				info.setAllowedVoicemail(Config.NOT_ALLOWED_VOICEMAIL);
			}				
		}						
	}
	//fim --> dnakamashi - OptionsFeatures- 3.0.5	
	
	public void save(GroupInfo info) throws DAOException, ValidateObjectException, DeleteDependenceException, IOException, ParseException
	{		
		Group g = info.getGroup();		
		validateGroup(g);
		g.getConfig().setKey(g.getConfigKey());		
		
		manageOptionsFeatures(info);
		
		g.getConfig().setAllowedGroupForward(info.getAllowedGroupForward());
		g.getConfig().setAllowedVoiceMail(info.getAllowedVoicemail());		
		g.getConfig().setAllowedKoushiKubun(Config.NOT_ALLOWED_KOUSHIKUBUN);
		configDAO.save(g.getConfig());
		g.setConfigKey(g.getConfig().getKey());
		g.setCallcenterConfigKey(info.getCallcenterConfigKey());
		g.setActive(Group.DEFINE_ACTIVE);
		boolean isHuntGroup = g.getGroupType() != Group.RING_GROUP ? true : false;
		Calendar calendar = Calendar.getInstance();
		
		if(g.getGroupType() == Group.ACDCALLCENTER_GROUP)
		{
			g.getCallCenterConfig().setTimeoutqueue(info.getTimeoutqueue());
			g.getCallCenterConfig().setQueueSize(info.getQueueSize());
			g.getCallCenterConfig().setAllowedQueue(info.getAllowedQueue());
			g.getCallCenterConfig().setQueueSizeType(info.getQueueSizeType());
			g.getCallCenterConfig().setAllowedWorkingHours(info.getAllowedWorkingHours());
			g.getCallCenterConfig().setKey(info.getCallcenterConfigKey());
			g.getCallCenterConfig().setGroupkey(info.getGroup().getKey());
			g.getCallCenterConfig().setCallCenterSalutationFileKey(info.getCallCenterSalutationFileKey());
			g.getCallCenterConfig().setCallCenterEndFileKey(info.getCallCenterEndFileKey());
			g.getCallCenterConfig().setCallCenterGroupEmptyFileKey(info.getCallCenterGroupEmptyFileKey());
			g.getCallCenterConfig().setCallCenterGroupFullFileKey(info.getCallCenterGroupFullFileKey());
			//CallCenter monitoring - inicio
			g.getCallCenterConfig().setEstablishedTimeouts(info.getEstablishedTimeouts());
			g.getCallCenterConfig().setQueueCallsLimit(info.getQueueCallsLimit());
			g.getCallCenterConfig().setEstablishedServiceLevelTimes(info.getEstablishedServiceLevelTimes());
			g.getCallCenterConfig().setMoreTimeQueueTimeouts(info.getMoreTimeQueueTimeouts());
			g.getCallCenterConfig().setAbandonedCallsInQueueLimit(info.getAbandonedCallsInQueueLimit());
			g.getCallCenterConfig().setAbandonedServiceLevelTimes(info.getAbandonedServiceLevelTimes());
			g.getCallCenterConfig().setTsfTimeThreshold(info.getTsfTimeThreshold());
			g.getCallCenterConfig().setTsfLimits(info.getTsfLimits());
			//CallCenter monitoring - fim

			callCenterConfigDAO.save(g.getCallCenterConfig());

			g.setCallcenterConfigKey(g.getCallCenterConfig().getKey());
		}
		//dsakuma-rribeiro: inicio notificacao da mudanca do tipo de grupo para o Call Center - INICIO
		boolean edit = g.getKey() != null;
		if (edit)
		{
			Integer oldType = groupDAO.getGroupTypeByKey(g.getKey());

			if (oldType != Group.ACDCALLCENTER_GROUP && g.getGroupType() == Group.ACDCALLCENTER_GROUP)
				JMSNotificationTools.getInstance().sendChangeToCallCenterTypeJMSMessage(g.getDomainKey(), g.getName(), g.getDomainName());

			else if (oldType == Group.ACDCALLCENTER_GROUP && g.getGroupType() != Group.ACDCALLCENTER_GROUP)
			{
				/*StringBuilder key = new StringBuilder();
				key.append(g.getName());
				key.append("@");
				key.append(g.getDomainName());*/
				//JMSNotificationTools.getInstance().sendRemoveGroupJMSMessage(key.toString(), g.getDomainKey() );
				JMSNotificationTools.getInstance().sendRemoveGroupJMSMessage(g.getKey(), g.getDomainKey());

				CallCenterConfig callCenterConfig = callCenterConfigDAO.getCallCenterConfigByGroupkey(g.getKey());
				callCenterConfigDAO.remove(callCenterConfig);
				g.setCallcenterConfigKey(null);
			}
		}
        //dsakuma-rribeiro: inicio notificacao da mudanca do tipo de grupo para o Call Center - FIM		
		
		groupDAO.save(g);
		nightModeSchedulerManager.saveNigthModeScheduler(info);
		
		if (g.getGroupType() == Group.ACDCALLCENTER_GROUP)
		{
			g.getCallCenterConfig().setGroupkey(g.getKey());
			callCenterConfigDAO.save(g.getCallCenterConfig());
			savePauses(g);
		}
		
		info.getForwardNoAnswer().setConfigKey(g.getConfigKey());
		info.getForwardNoAnswer().setForwardMode(Forward.NOANSWER_MODE);
		forwardDAO.save(info.getForwardNoAnswer());
		
		info.getForwardAlways().setConfigKey(g.getConfigKey());
		info.getForwardAlways().setForwardMode(Forward.ALWAYS_MODE);
		forwardDAO.save(info.getForwardAlways());

		if (g.getGroupType() == Group.ACDCALLCENTER_GROUP)
		{
			info.getForwardMaxWaitingTime().setConfigKey(g.getConfigKey());
			info.getForwardMaxWaitingTime().setForwardMode(Forward.MAX_WAITING_TIME);
			forwardDAO.save(info.getForwardMaxWaitingTime());

			info.getForwardGroupEmpty().setConfigKey(g.getConfigKey());
			info.getForwardGroupEmpty().setForwardMode(Forward.GROUP_EMPTY);
			forwardDAO.save(info.getForwardGroupEmpty());

			info.getForwardGroupUnavailable().setConfigKey(g.getConfigKey());
			info.getForwardGroupUnavailable().setForwardMode(Forward.GROUP_UNAVAILABLE);
			forwardDAO.save(info.getForwardGroupUnavailable());
		}
		updateGroupSalutation(g.getKey(), g.getNightmodeSalutationFileKey(), Groupfile.NIGHTMODE_FILE);
		updateGroupSalutation(g.getKey(), g.getDefaultSalutationFileKey(), Groupfile.DEFAULT_FILE);
		if (g.getGroupType() == Group.ACDCALLCENTER_GROUP)
		{
			updateGroupSalutation(g.getKey(), g.getCallCenterConfig().getCallCenterSalutationFileKey(), Groupfile.CALLCENTER_SALUTATION_FILE);
			updateGroupSalutation(g.getKey(), g.getCallCenterConfig().getCallCenterEndFileKey(), Groupfile.CALLCENTER_END_FILE);
			updateGroupSalutation(g.getKey(), g.getCallCenterConfig().getCallCenterGroupEmptyFileKey(), Groupfile.CALLCENTER_GROUP_EMPTY_FILE);
			updateGroupSalutation(g.getKey(), g.getCallCenterConfig().getCallCenterGroupFullFileKey(), Groupfile.CALLCENTER_GROUP_FULL_FILE);
		}
		StringBuilder groupname = new StringBuilder().append(g.getName()).append("@").append(g.getDomainName());
		
		List<Address> groupAddressList = addDAO.getAddressFullListByGroup(g.getKey());
		
		List<UsergroupInfo> oldList = getUsers(g.getKey());
		List<UsergroupInfo> newList = info.getUsersInGroup();		
		
		Collections.sort(newList, new Comparator<UserInGroupFacet>()
		{
			public int compare(UserInGroupFacet o1, UserInGroupFacet o2) 
			{
				return o1.getUsername().compareTo(o2.getUsername());
			}
		});
		Collections.sort(oldList, new Comparator<UserInGroupFacet>()
		{
			public int compare(UserInGroupFacet o1, UserInGroupFacet o2) 
			{
				return o1.getUsername().compareTo(o2.getUsername());
			}
		});
				
//		//dsakuma-rribeiro: Validação do número de PAs - INICIO
//		if(g.getGroupType() == Group.ACDCALLCENTER_GROUP)
//		{
//			int w = 0, y =0;
//			List<Long> pasAddedInGroup = new ArrayList<Long>();
//			List<Long> pasRemovedInGroup = new ArrayList<Long>();
//
//			while (w < newList.size() && y < oldList.size())
//			{
//				UserInGroupFacet newUG = newList.get(w);
//				UserInGroupFacet oldUG = oldList.get(y);
//
//				int compare = newUG.getUsername().compareTo(oldUG.getUsername());
//
//				if(compare == 0)
//				{	
//					w++;  y++;
//				} 
//				else if(compare < 0)
//				{
//					pasAddedInGroup.add(0, newList.get(w).getPbxuserKey());
//					w++;
//				}
//				else
//				{
//					pasRemovedInGroup.add(0, oldList.get(y).getPbxuserKey());
//					y++;
//				}
//			}
//
//			if(w < newList.size())
//				while(w < newList.size())
//				{
//					pasAddedInGroup.add(0, newList.get(w).getPbxuserKey());
//					w++;
//				}
//			else if (y < oldList.size())
//				while(y < oldList.size())
//				{
//					pasRemovedInGroup.add(0, oldList.get(y).getPbxuserKey());
//					y++;
//				}
//
//			List<Long> pasAssociatedToGroupAmount = new ArrayList<Long>();
//			pasAssociatedToGroupAmount = groupDAO.getPAsByPbxkey(g.getPbxKey());
//
//			//Deletar usuarios da lista removed 
//			w=0;
//			while(pasRemovedInGroup.size()>0)
//			{
//				if(pasAssociatedToGroupAmount.get(w).equals(pasRemovedInGroup.get(0)) )
//				{
//					pasAssociatedToGroupAmount.remove(w);
//					pasRemovedInGroup.remove(0);
//					w=0;
//				}
//				else
//					w++;
//			}
//			//Inserir da lista pasAddedInGroup
//			while(pasAddedInGroup.size()>0)
//			{
//				pasAssociatedToGroupAmount.add(pasAssociatedToGroupAmount.size(), pasAddedInGroup.get(0));
//				pasAddedInGroup.remove(0);
//			}
//
//			//Apagando membros repetidos da pasAssociatedToGroupAmount
//			y=0;
//			List<Long> paAmount = new ArrayList<Long>();
//			while(pasAssociatedToGroupAmount.size() > 0)
//			{
//				if(!paAmount.contains(pasAssociatedToGroupAmount.get(0)) ){
//					paAmount.add(y, pasAssociatedToGroupAmount.get(0));
//					y++;
//				}
//				pasAssociatedToGroupAmount.remove(0);
//			}
//
//			//Fazendo a validação do numero maximo de PAs por dominio
//			Integer maxPa = pbxDAO.getMaxPAsOnlineNumber(g.getPbxKey());
//			if (paAmount.size() > maxPa)
//				throw new ValidateObjectException("It's not possible save ACD group, because the Max PAs number was reached!!", Group.class, g, ValidateType.MAX_NUMBER);
//		}
//		//dsakuma-rribeiro: Validação do número de PAs - FIM
		
		int i = 0, j = 0;
		while(i < newList.size() && j < oldList.size())
		{
			UserInGroupFacet newUG = newList.get(i);
			UserInGroupFacet oldUG = oldList.get(j);
			int compare = newUG.getUsername().compareTo(oldUG.getUsername());
			if(compare == 0)
			{
				Usergroup ug = oldUG.getUsergroup();
				ug.setPriority(newUG.getPriority());
				ug.setGroupAdmin(newUG.getAdmin() ? Usergroup.ADMIN_ON : Usergroup.ADMIN_OFF);
				if(isHuntGroup && ug.getLastCall() == null && ug.getStartLastCall() == null)
				{
					ug.setLastCall(calendar);
					ug.setStartLastCall(calendar);
				}
				//inicio-->vmartinez e andre bug #6126 - 3.0.5
				setUserGroupACDState(ug, g.getGroupType());
				//fim-->vmartinez e andre bug #6126 - 3.0.5
				
				i++;  j++;
				ugDAO.save(ug);
				
			} else if(compare < 0)
			{
				Usergroup ug = fillUsergroup(newUG, g.getKey());
				if(isHuntGroup && ug.getLastCall() == null && ug.getStartLastCall() == null)
				{
					ug.setLastCall(calendar);
					ug.setStartLastCall(calendar);
				}
				//inicio-->vmartinez e andre bug #6126 - 3.0.5 
				setUserGroupACDState(ug, g.getGroupType());
				//fim-->vmartinez e andre bug #6126 - 3.0.5
				ugDAO.save(ug);
				i++;
			}else
			{	
				// bug id 5888
				// default did do usuario deve ser alterado,
				// caso usuario tenha como default did o did do grupo
				changeUserDefaultdDID(oldUG.getUsergroup(), groupAddressList);
				ugDAO.remove(oldUG.getUsergroup());
				sendRemoveUsergroupJMSMessage(oldUG.getUsergroup().getPbxuserKey(), g.getDomainKey(), g.getKey(), g.getGroupType());	
				j++;
			}
		}
		if(i < newList.size())
			while(i < newList.size())
			{
				Usergroup ug = fillUsergroup(newList.get(i++), g.getKey());
				if(isHuntGroup && ug.getLastCall() == null && ug.getStartLastCall() == null)
				{
					ug.setLastCall(calendar);
					ug.setStartLastCall(calendar);
				}
				//inicio-->vmartinez e andre bug #6126 - 3.0.5
				setUserGroupACDState(ug, g.getGroupType());
				//fim-->vmartinez e andre bug #6126 - 3.0.5
				ugDAO.save(ug);
			}	
		else if (j < oldList.size())
			while(j < oldList.size())
			{
				//rribeiro/dsakuma -> inicio alerta de remoção de usuário de grupo acd call center  
				ugDAO.remove(oldList.get(j).getUsergroup());
				sendRemoveUsergroupJMSMessage(oldList.get(j).getUsergroup().getPbxuserKey(), g.getDomainKey(), g.getKey(), g.getGroupType());
				j++;
				//rribeiro/dsakuma -> fim alerta de remoção de usuário de grupo acd call center
			}

		addManager.saveAddressList(g);
		
		//dsakuma e rribeiro - Notificação para ACD CallCenter após alterações nas configurações do grupo - iniciojeryes
		
		if(g.getGroupType() == Group.ACDCALLCENTER_GROUP)
		{
			CallCenterConfig cfg = g.getCallCenterConfig();
			JMSNotificationTools.getInstance().sendGroupConfigJMSMessage(info.getTimeoutqueue(), info.getQueueSize(), 
				cfg.getCallCenterSalutationFileKey(), cfg.getCallCenterEndFileKey(), g.getDomainKey(), 
				cfg.getCallCenterGroupEmptyFileKey(), cfg.getCallCenterGroupFullFileKey(), 
				cfg.getAllowedQueue(), cfg.getQueueSizeType(), cfg.getAllowedWorkingHours(), 
				g.getMaxDistributionTime().intValue(), g.getConfig().getTimeoutcall().intValue(), g.getDomainName(), g.getGroupType(), 
				groupname.toString(), cfg.getEstablishedTimeouts(), cfg.getQueueCallsLimit(), cfg.getEstablishedServiceLevelTimes(), cfg.getMoreTimeQueueTimeouts(), 
				cfg.getAbandonedCallsInQueueLimit(), cfg.getAbandonedServiceLevelTimes(), cfg.getTsfTimeThreshold(), cfg.getTsfLimits(), cfg.getSurveyBargein(), cfg.getSurveyActive(), cfg.getSurveyTimeout(), cfg.getSurveyRetries(), 
				cfg.getSurveySalutationFileKey(), cfg.getSurveyInputErrorFileKey(), cfg.getSurveyEndFileKey(), cfg.getSurveyRankingValues(), g.getAlgorithmType());
			JMSNotificationTools.getInstance().sendPauseRefresh(g.getKey(), groupname.toString(), g.getDomainKey());
			JMSNotificationTools.getInstance().sendGroupForwardRefresh(g.getKey(), groupname.toString(), g.getDomainKey());
		}
		//dsakuma e rribeiro - Notificação para ACD CallCenter após alterações nas configurações do grupo - fim
	}

	private void sendRemoveUsergroupJMSMessage (Long puKey, Long domainKey, Long groupKey, Integer groupType)
	{
		if(groupType == Group.ACDCALLCENTER_GROUP)
			JMSNotificationTools.getInstance().sendRemoveUsergroupJMSMessage(puKey, domainKey, groupKey);
	}
	
	private void savePauses(Group g) throws DAOException, ValidateObjectException
	{
		List<GroupPause> gpList = new ArrayList<GroupPause>(g.getGroupPauseList());
		
		for (GroupPause option : gpList) 
		{  
			if(option.getPauseKey() != null)
				groupPauseDAO.save(option);
			else if(option.getPauseKey() == null && option.getKey() != null)
				groupPauseDAO.remove(option);
		}	
	}
	
	private void changeUserDefaultdDID(Usergroup userGroup, List<Address> groupAddressList) throws DAOException, ValidateObjectException
	{
	    // bug 5888
		// default did do usuario deve ser alterado,
		// caso usuario tenha como default did o did do grupo
		Long userDefaultDid = userGroup.getPbxuser().getDefaultDIDKey();
		if(userDefaultDid != null)
		{
			boolean isDefaultDidAGroupDid = false;
			for(Address address: groupAddressList)
			{
				if(userDefaultDid.equals(address.getKey()))
				{
					isDefaultDidAGroupDid = true;
					break;
				}
			}
				
			if(isDefaultDidAGroupDid)
			{
				userGroup.getPbxuser().setDefaultDIDKey(null);
				pbxuserDAO.save(userGroup.getPbxuser());
			}
		}
	}
	
	//inicio-->vmartinez e andre bug #6126 - 3.0.5 - seta o estado do novo  membro do grupo de acordo com o estado dele nos outros grupos
	private void setUserGroupACDState(Usergroup ug, Integer groupType) throws DAOException 
	{
		List<Usergroup> usergroupList = new ArrayList<Usergroup>();
		usergroupList = ugDAO.getUsergroupInACDByPbxuser(ug.getPbxuserKey());
		if (usergroupList.size()>0)
		{
			ug.setLoginState(usergroupList.get(0).getLoginState());
			ug.setLoginChange(Calendar.getInstance());
			if(ug.getAvailableState() != null && ug.getAvailableState()==Usergroup.AVAILABLE_STATE)
				ug.setAvailableState(Usergroup.AVAILABLE_STATE);
			else
				ug.setAvailableState(Usergroup.UNAVAILABLE_STATE);
		}
		else if(groupType == Group.ACDCALLCENTER_GROUP)
		{
			ug.setAvailableState(Usergroup.UNAVAILABLE_STATE);
		}
	}
	//fim-->vmartinez e andre bug #6126 - 3.0.5

	public List<UsergroupInfo> getUsers(Long groupKey) throws DAOException
	{
		List<Usergroup> userGroupList = ugDAO.getUsergroupListWithUserByGroup(groupKey);
		return getUsergroupInfoList(userGroupList);
	}
	
	public List<UsergroupInfo> getUsers(Long domainKey, Long groupKey, Long pbxuserKey) throws DAOException
	{
		List<Usergroup> userGroupList = ugDAO.getUsergroupListByGroupKeyAndDomainKeyAndPbxuserKey(domainKey, groupKey, pbxuserKey);
		for(Usergroup ug : userGroupList)
		{
			ug.getPbxuser().setAddressList(addDAO.getExtensionListByPbxuser(ug.getPbxuserKey()));
		}
		return getUsergroupInfoList(userGroupList);
	}
	
	public List<UsergroupInfo> getUsers(String domain, Long groupKey, Long pbxuserKey) throws DAOException
	{
		List<Usergroup> userGroupList = ugDAO.getUsergroupListByGroupKeyAndDomainAndPbxuserKey(domain, groupKey, pbxuserKey);
		for(Usergroup ug : userGroupList)
		{
			ug.getPbxuser().setAddressList(addDAO.getExtensionListByPbxuser(ug.getPbxuserKey()));
		}
		return getUsergroupInfoList(userGroupList);
	}
	
	public List<Pbxuser> getPbxuserListByGroupKeyAndDomain(Long groupKey, Long domainKey, String domain) throws DAOException
	{
		return ugDAO.getPbxuserListByGroupKeyAndDomain(groupKey, domainKey, domain); 
	}
	
	public List<UsergroupInfo> getUsergroupInfoList(List<Usergroup> userGroupList)
	{
		List<UsergroupInfo> result = new ArrayList<UsergroupInfo>(userGroupList.size());
		for (Usergroup ug : userGroupList) 
			result.add(new UsergroupInfo(ug, ug.getPbxuser().getUser().getUsername(), ug.getPbxuser().getUser().getName()));
		return result;
	}
	
	private void updateGroupSalutation(Long gKey, Long fileInfoKey, Integer useType) throws DAOException, DeleteDependenceException, ValidateObjectException, IOException
	{
		if(fileInfoKey!=null)
			voicemailManager.saveGroupSalutation(gKey, fileInfoKey, useType);
		else
			voicemailManager.deleteSalutationGroup(gKey, useType);
	}
	
	private Usergroup fillUsergroup(UserInGroupFacet info, Long groupKey)
	{
		Usergroup ug = info.getUsergroup();
		ug.setKey(null);
		ug.setGroupKey(groupKey);
		//ug.setLastCall(Calendar.getInstance());
		//ug.setStartLastCall(Calendar.getInstance());
		return ug;
	}

	private void validateGroup(Group g) throws ValidateObjectException
	{
		if(!g.getName().matches(getSipIDRegex()))
			throw new ValidateObjectException("Please check group name, it must be compilant with sipID rules!", Group.class, g, ValidateType.INVALID);
	}
//inicio --> dnakamashi - ezaghi - vmartinez - deleção forçada de grupo - version 3.0.5 RC6.4
	public void deleteGroups(List<Long> groupKeyList, Boolean isForced) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		for(Long key: groupKeyList)
			deleteGroup(key,isForced);
	}
	
	public void deleteGroup(Long key, boolean isForced) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		//Group, Config, Forward, Usergroup
		Group g = deleteGroupAndDependences(key, isForced);
		
		//dsakuma/rribeiro - Notificação JMS para AcdCallCenter acerca de grupo deletado - início
		
		if(g.getGroupType() == Group.ACDCALLCENTER_GROUP)
		{
			//StringBuilder keyAcd = new StringBuilder(g.getName()).append("@").append(g.getDomain().getDomain());
			JMSNotificationTools.getInstance().sendRemoveGroupJMSMessage(g.getKey(), g.getDomainKey());
		}
		//dsakuma/rribeiro - Notificação JMS para AcdCallCenter acerca de grupo deletado - fim
		
		//Extension, SipID, DID
		addManager.removeAllAddress(g.getKey(), true, isForced);
	}
	//fim --> dnakamashi - ezaghi - vmartinez - deleção forçada de grupo - version 3.0.5 RC6.4
	
	private Group deleteGroupAndDependences(Long key, boolean isForced) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		Group g = groupDAO.getGroupFullWithDomain(key);
		g.setActive(PersistentObject.DEFINE_DELETED);
		g.setNightmodeaddressKey(null);
		groupDAO.save(g);
		g.getConfig().setActive(PersistentObject.DEFINE_DELETED);
		configDAO.save(g.getConfig());
		if (g.getGroupType() == Group.ACDCALLCENTER_GROUP)
		{
			CallCenterConfig callCenterConfig = callCenterConfigDAO.getByKey(g.getCallcenterConfigKey());
			callCenterConfigDAO.remove(callCenterConfig);
		}

		voicemailManager.deleteSalutationGroup(key, Groupfile.DEFAULT_FILE);
		voicemailManager.deleteSalutationGroup(key, Groupfile.NIGHTMODE_FILE);
		voicemailManager.deleteSalutationGroup(key, Groupfile.CALLCENTER_SALUTATION_FILE);
		voicemailManager.deleteSalutationGroup(key, Groupfile.CALLCENTER_END_FILE);

		ForwardDAO fDAO = dao.getDAO(ForwardDAO.class);
		List<Forward> forwards = fDAO.getForwardListByConfig(g.getConfigKey());
		for(Forward forward : forwards)
			fDAO.remove(forward);
		
		for(Usergroup ug : ugDAO.getUsergroupListByGroup(g.getKey()))
		{	
			if((g.getGroupType().equals(Group.ACDHUNT_GROUP) || g.getGroupType().equals(Group.ACDCALLCENTER_GROUP)) 
					&& ug.getLoginState().equals(Usergroup.LOGIN_ON) && !isForced)
				throw new DeleteDependenceException("Cannot delete " + g.getName() + " because there is logged users at this group!!!", Usergroup.class, g);
			ugDAO.remove(ug);
		}
		return g;
	}
	
	public void executeLoginACD(SipAddressParser from) throws DAOException, LoginACDException, ValidateObjectException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserByAddressAndDomain(from.getExtension(), from.getDomain());
		if(aDAO.howManyActiveCallByPbxuser(pu.getKey()) > 1 ? true : false)	throw new LoginACDException("PA has active call. Ignoring change login state");
		List<Usergroup> ugList = ugDAO.getUsergroupInACDByPbxuser(pu.getKey());
		if(ugList.size() == 0)
			throw new LoginACDException("No groups found, cannot execute login action!!!");	
		else if (ugList.get(0).getLoginState() == Usergroup.LOGIN_ON)
		{
			throw new LoginACDException("It's not possible Login in ACD group, because PA already logged!!");
		}
		else
		{
			Long pbxKey = ugList.get(0).getGroup().getPbxKey();
			List<Long> paAmount = groupDAO.getPAsAmmountByPbxkey(pbxKey);
			Integer maxPa = pbxDAO.getMaxPAsOnlineNumber(pbxKey);
			Integer count = paAmount.size() + 1;
			boolean needNotify = false;

			
			for (Usergroup tmp : ugList)
			{
				if(tmp.getGroup().getGroupType().equals(Group.ACDCALLCENTER_GROUP))
				{
					if (count < maxPa)
					{
						needNotify = true;
						tmp.setPbxuser(pu);
						setACDStatus(tmp, Usergroup.LOGIN_ON);
					}
					else
						throw new LoginACDException("It's not possible Login in ACD group, because the Max PAs number was reached!!");
				}
				else
				{
					tmp.setPbxuser(pu);
					setACDStatus(tmp, Usergroup.LOGIN_ON);
				}
			}
			
			if(needNotify)
				JMSNotificationTools.getInstance().sendLoginJMSMessage(Usergroup.LOGIN_ON, pu.getKey(), pu.getUser().getDomainKey(), pu.getUsername());
		}
	}
	
	public void executeLogoutACD(SipAddressParser from) throws DAOException, ValidateObjectException
	{
        Pbxuser pu = pbxuserDAO.getPbxuserByAddressAndDomain(from.getExtension(), from.getDomain());
        if(aDAO.howManyActiveCallByPbxuser(pu.getKey()) > 1 ? true : false)	throw new LoginACDException("PA has active call. Ignoring change login state");
        List<Usergroup> ugList = ugDAO.getUsergroupInACDByPbxuser(pu.getKey());
        if(ugList.size() == 0)
        	throw new LoginACDException("No groups found, cannot execute logout action!!!");

        boolean needNotify = false;
        
        if (ugList.get(0).getLoginState() == Usergroup.LOGIN_OFF)
        	throw new LoginACDException("It's not possible Logoff in ACD group, because PA already logged off!!");
        	
        for (Usergroup tmp : ugList)
        {
			if(tmp.getGroup().getGroupType().equals(Group.ACDCALLCENTER_GROUP))
				needNotify = true;
        	tmp.setPbxuser(pu);
        	setACDStatus(tmp, Usergroup.LOGIN_OFF);
        }
        
		if(needNotify)
			JMSNotificationTools.getInstance().sendLoginJMSMessage(Usergroup.LOGIN_OFF, pu.getKey(), pu.getUser().getDomainKey(), pu.getUsername());
	}
	
	private void setACDStatus(Usergroup ug, Integer status) throws DAOException, ValidateObjectException
	{
			ug.setLoginState(status);
			ug.setLoginChange(Calendar.getInstance());
			ugDAO.save(ug);

			//Tirar a PA de pausa caso ela esteja se deslogando
			if(status == Usergroup.LOGIN_OFF && isInPauseState(ug))
			{
				if(ug.getPbxuser() == null)
					ug.setPbxuser(pbxuserDAO.getPbxuserAndUser(ug.getPbxuserKey()));
    			executePause(ug.getPbxuser().getUser().getUsername(), ug.getPbxuser().getUser().getDomain().getDomain(), "");
			}
	}

	public void changeACDLogin(List<Long> pbxuserKeyList, boolean logged) throws DAOException, ValidateObjectException, UserWithoutSipSessionlogException 
	{
		for (Long key : pbxuserKeyList) 
			changeACDLogin(key, logged);
	}
	
	public void changeACDLogin(Long pbxuserKey, boolean logged) throws DAOException, ValidateObjectException, UserWithoutSipSessionlogException
	{
		Pbxuser pu = pbxuserDAO.getByKey(pbxuserKey);
		List<Usergroup> ugList = ugDAO.getUsergroupInACDByPbxuser(pbxuserKey);
		if (ugList.get(0).getLoginState().equals(logged == true ? 1 : 0)) return; //Ignoring if Login status is same
		if(aDAO.howManyActiveCallByPbxuser(pbxuserKey) > 0 ? true : false)	throw new LoginACDException("PA has active call. Ignoring change login state");
		
		if(logged)
		{
			List<Sipsessionlog> sipSessionList = sipSessionLogDAO.getActiveSipsessionlogListByPbxuser(pbxuserKey);
			if(sipSessionList == null || sipSessionList.size() == 0)
				sipSessionList = sipSessionLogDAO.getActiveSipSessionLogListByTerminalOfPbxuser(pbxuserKey);
			if(sipSessionList == null || sipSessionList.size() == 0)
				throw new UserWithoutSipSessionlogException("", CallStateEvent.PERMISSION_DENIED);
		}
		Integer loginStatus = logged ? Usergroup.LOGIN_ON : Usergroup.LOGIN_OFF;
		boolean needNotify = false;
		
		if(ugList.size() >0)
		{
			Long pbxKey = ugList.get(0).getGroup().getPbxKey();
			List<Long> paAmount = groupDAO.getPAsAmmountByPbxkey(pbxKey);
			Integer maxPa = pbxDAO.getMaxPAsOnlineNumber(pbxKey);
			Integer count = paAmount.size();
			
			for (Usergroup tmp : ugList)
			{
				if(tmp.getGroup().getGroupType().equals(Group.ACDCALLCENTER_GROUP))
				{
					needNotify = true;
					if(loginStatus.equals(Usergroup.LOGIN_OFF))
						setACDStatus(tmp, loginStatus);
					else
					{
						if (count < maxPa)
						{
							tmp.setPbxuser(pu);
							setACDStatus(tmp, loginStatus);
							count += 1;
						}
						else
							throw new LoginACDException("It's not possible Login in ACD group, because the Max PAs number was reached!!");
					}
				}
				else
				{
					if(loginStatus.equals(Usergroup.LOGIN_OFF))
						setACDStatus(tmp, loginStatus);
					else
					{
						tmp.setPbxuser(pu);
						setACDStatus(tmp, loginStatus);
					}
				}
			}	
			if(needNotify)
				JMSNotificationTools.getInstance().sendLoginJMSMessage(loginStatus, pbxuserKey, pu.getUser().getDomainKey(), pu.getUsername());
		}
	}
	
	public Usergroup changeUsergroupAvailableState(Long usergroupKey, Integer available) throws DAOException, ValidateObjectException, UserWithoutSipSessionlogException
	{
		Usergroup ug = getUsergroupFullByUsergroupKey(usergroupKey);
		Long pbxuserKey = ug.getPbxuserKey();
		if(aDAO.howManyActiveCallByPbxuser(pbxuserKey) > 0 ? true : false)	throw new LoginACDException("PA has active call. Ignoring change login state");
		
		if (ug.getAvailableState().equals(available)) return ug;//Ignoring, available state is same
			
		if(available.equals(Usergroup.AVAILABLE_STATE))
		{
			List<Sipsessionlog> sipSessionList = sipSessionLogDAO.getActiveSipsessionlogListByPbxuser(pbxuserKey);
			if(sipSessionList == null || sipSessionList.size() == 0)
				sipSessionList = sipSessionLogDAO.getActiveSipSessionLogListByTerminalOfPbxuser(pbxuserKey);
			if(sipSessionList == null || sipSessionList.size() == 0)
				throw new UserWithoutSipSessionlogException("", CallStateEvent.PERMISSION_DENIED);
		}

		if(ug.getGroup().getGroupType().equals(Group.ACDCALLCENTER_GROUP))
		{
			Long pbxKey = ug.getGroup().getPbxKey();
			List<Long> paAmount = groupDAO.getPAsAmmountByPbxkey(pbxKey);
			Integer maxPa = pbxDAO.getMaxPAsOnlineNumber(pbxKey);
			
			if (!(paAmount.size() < maxPa))
				throw new LoginACDException("It's not possible Login in ACD group, because the Max PAs number was reached!!");
		}

		//ug.setLoginState(available);
		ug.setLoginChange(Calendar.getInstance());
		ug.setAvailableState(available);
		ugDAO.save(ug);

		//dsakuma/rribeiro - Notificação JMS para AcdCallCenter Available State - início
		if (ug.getGroup().getGroupType() == Group.ACDCALLCENTER_GROUP)
			JMSNotificationTools.getInstance().sendChangeAvailableStateMessage(usergroupKey, available, ug.getPbxuser().getUser().getDomainKey());
		//dsakuma/rribeiro - Notificação JMS para AcdCallCenter Available State - fim
		return ug;
	}
	
	public void changeUsergroupLoggedState(Long pbxuserKey, boolean available) throws DAOException, ValidateObjectException, UserWithoutSipSessionlogException
	{
		changeACDLogin(pbxuserKey, available);
	}
	
	public Usergroup getUsergroupFullByUsergroupKey(Long usergroupKey) throws DAOException
	{
		return ugDAO.getUsergroupFullByUsergroupKey(usergroupKey);
	}

	public ACDInfo getACDInfo(Long pbxuserKey) throws DAOException
	{
		ACDInfo info = null;
		List<Usergroup> ugList = ugDAO.getUsergroupInACDByPbxuser(pbxuserKey);
		if(ugList.size() > 0)
		{
			Usergroup firstUserGroup = ugList.get(0);
			boolean logged = firstUserGroup.getLoginState() == Usergroup.LOGIN_ON;
			Calendar lastLoginChange = firstUserGroup.getLoginChange();
			List<String> groups = new ArrayList<String>(ugList.size());
			for (Usergroup userGroup : ugList) 
				groups.add(userGroup.getGroup().getName());
			info = new ACDInfo(pbxuserKey, logged, groups, lastLoginChange);
		} 
		return info;
	}
	
	public Group getGroupByNameAndAssociatedUser(String groupName, String username, String domainName) throws DAOException
	{
		Pbxuser  pbxuser = pbxuserDAO.getPbxuserByTerminal(username, domainName);
		if(pbxuser != null && pbxuser.getUser() != null)
			username = pbxuser.getUser().getUsername();
		return groupDAO.getGroupByNameAndAssociatedUser(groupName, username, domainName);
	}

	public GroupInfo getGroupInfoServiceClassByPbxKey(Long key) throws DAOException{
		
		Forward fAlways = new Forward();
		fAlways.setForwardMode(Forward.ALWAYS_MODE);
		fAlways.setStatus(Forward.STATUS_OFF);
		
		Forward fNoAnswer = new Forward();
		fNoAnswer.setForwardMode(Forward.NOANSWER_MODE);
		fNoAnswer.setStatus(Forward.STATUS_OFF);
		
		Config config = new Config();
		config.setActive(PersistentObject.DEFINE_ACTIVE);
		config.setDisableVoicemail(Config.VOICEMAIL_OFF);
		config.setEmailNotify(Config.EMAILNOTIFY_OFF);
		config.setDndStatus(Config.DND_OFF);
		config.setAttachFile(Config.ATTACH_FILE_OFF);
		config.setForwardType(Config.FORWARD_TYPE_DEFAULT);		
		config.setAllowedKoushiKubun(Config.NOT_ALLOWED_KOUSHIKUBUN);
		
		Group g = new Group();
		g.setConfig(config);
		g.setNightmodeStatus(Group.NIGHTMODE_OFF);
		
		GroupInfo info = new GroupInfo(g,fNoAnswer,fAlways);
		info.addClassOfService(serviceclassDAO.getServiceclassKeyAndName(key));
		
		return info;
	}
	
	public Group getGroupAndServiceClassByDid(String did) throws DAOException
	{
		Group group = groupDAO.getGroupAndServiceClassByDID(did);
		return group;
	}

	public void executePause(SipAddressParser sipFrom, String pauseCode) throws DAOException, ValidateObjectException
	{
		executePause(sipFrom.getExtension(), sipFrom.getDomain(), pauseCode);
	}
	
	public void executePause(String user, String domain, String strPauseCode) throws DAOException, ValidateObjectException 
	{
		Pbxuser pu = pbxuserDAO.getPbxuserByAddressAndDomain(user, domain);
		List<Usergroup> ugList = ugDAO.getUsergroupInACDByPbxuserAndGroupAddress(pu.getKey(), null);
		if(ugList.size() == 0)
			throw new PauseException("No groups found, cannot execute pause action!!!");		
		if(hasActiveCall(pu.getKey()))
			throw new PauseException("User has active call, cannot execute pause action!!!");	
		boolean pauseExecuted = false;
		boolean needNotify = false;
		Integer groupPosition = null;
		Integer pauseCode = null;
		List<String> groupList = new ArrayList<String>();
		
		
		for (Usergroup tmp : ugList)
		{
			if(tmp.getGroup().getGroupType().equals(Group.ACDCALLCENTER_GROUP))
			{
				tmp.setPbxuser(pu);
				PauseEvent pauseEvent = getPauseEvent(strPauseCode, tmp.getGroupKey());
				
				if(pauseEvent != null)
				{
					pauseCode = pauseEvent.getPauseCode();
					groupList.add(tmp.getGroup().getKey().toString());
					groupPosition = pauseEvent.getGroupPosition();
					needNotify = true;
					setPauseStatus(tmp, pauseEvent);
					pauseExecuted = true;
				}
			}
		}
		
        String groupListStr = groupList.toString().replace("[", "").replace("]", "").replace(" ", ""); //transforma lista de grupos em string
		
		if(needNotify)
			JMSNotificationTools.getInstance().sendPauseJMSMessage(pauseCode, groupPosition, pu.getKey(), pu.getUser().getDomainKey(), groupListStr); 
		
		if(!pauseExecuted)
			throw new PauseException("Pause code not found, cannot execute pause action!!!");
	}
	
	private void setPauseStatus(Usergroup ug, PauseEvent pauseEvent) throws DAOException, ValidateObjectException
	{
		if(ug.getPauseCause() != null && ug.getPauseCause().equals(pauseEvent.getPauseCode()))
			throw new PauseException("User already is in pause or same state!");
		
		ug.setPauseCause(pauseEvent.getPauseCode());
		ugDAO.save(ug);
	}
	
	private boolean isInPauseState(Usergroup ug)
	{
		if(ug.getPauseCause() != null && !ug.getPauseCause().equals(PauseEvent.PAUSE_RETURN_CODE))
			return true;
		else
			return false;
	}
	
	private PauseEvent getPauseEvent(String pauseCode, Long groupKey) throws DAOException 
	{
		PauseEvent pauseEvent = null;
		if(pauseCode.length() > 0)//pauseCode - Pause IN
		{
			Integer code = new Integer(pauseCode);
			List <GroupPause> options = groupPauseDAO.getGroupPauseListActiveByGroupKey(groupKey);
			for(GroupPause tmp : options)
			{
				if(tmp.getPause().getPauseCode().equals(code))
				{
					pauseEvent = new PauseEvent(code, tmp.getGroupPosition());
					break;
				}
			}
		}else //Pause RETURN
			pauseEvent = new PauseEvent(PauseEvent.PAUSE_RETURN_CODE, PauseEvent.GROUPPOSITON_RETURN_CODE);

		return pauseEvent;
	}

	public Group getGroupByKey(Long groupKey) throws DAOException
	{
		return groupDAO.getByKey(groupKey);
	}
	
	public List<Long> getGroupkeyListByAddressAndDomainKey(String address, Long  domainKey) throws DAOException
	{
		return groupDAO.getGroupkeyListByAddressAndDomainKey(address, domainKey);
	}
	
	public void changeACDLoginStatus(String domain, String user, boolean logged) throws DAOException, UserWithoutSipSessionlogException, ValidateObjectException
	{
		Pbxuser pu = pbxuserDAO.getPbxuserByAddressAndDomain(user, domain);
		changeACDLogin(pu.getKey(), logged);
	}
	
	private boolean hasActiveCall(Long pbxuserKey) throws DAOException
	{
		Long count = aDAO.howManyActiveCallByPbxuser(pbxuserKey);
		return count >1;
	}
}