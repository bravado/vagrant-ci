package br.com.voicetechnology.ng.ipx.rule.implement.webservices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.VoiceException;
import br.com.voicetechnology.ng.ipx.commons.exception.authentication.InvalidLoginException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.SessionManagerException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.QuotaException;
import br.com.voicetechnology.ng.ipx.commons.file.FileUtils;
import br.com.voicetechnology.ng.ipx.commons.file.TempFileManager;
import br.com.voicetechnology.ng.ipx.commons.security.crypt.Crypt;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GatewayDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserterminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.RecordFileDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.RouteruleDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ServiceclassDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipsessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ivr.IVRDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.ContactDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.ContactphonesDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.FileinfoDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.GroupfileDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RoleDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserfileDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVR;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Gateway;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuserterminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Presence;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Routerule;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contact;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contactphones;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Groupfile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Preference;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Role;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CalllogInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ContactsInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.DIDInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.FileInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.GatewayInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.GroupInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.IVRInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.LoginInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.NewsInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.NightmodeGroup;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PBXDialPlanInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PBXInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PbxuserInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PhoneInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PresenceInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.RecordFileInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.RoleInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.RouteRuleInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SimpleContactInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.TerminalInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserCentrexInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserForwardsInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserSessionInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UsergroupInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.pojo.ws.WebServiceConstantValues;
import br.com.voicetechnology.ng.ipx.rule.implement.AboutMeManager;
import br.com.voicetechnology.ng.ipx.rule.implement.AddressManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CallBackManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CallLogManager;
import br.com.voicetechnology.ng.ipx.rule.implement.ContactManager;
import br.com.voicetechnology.ng.ipx.rule.implement.DialPlanManager;
import br.com.voicetechnology.ng.ipx.rule.implement.FileinfoManager;
import br.com.voicetechnology.ng.ipx.rule.implement.GatewayManager;
import br.com.voicetechnology.ng.ipx.rule.implement.GroupManager;
import br.com.voicetechnology.ng.ipx.rule.implement.NewsManager;
import br.com.voicetechnology.ng.ipx.rule.implement.PbxManager;
import br.com.voicetechnology.ng.ipx.rule.implement.PbxuserManager;
import br.com.voicetechnology.ng.ipx.rule.implement.RoleManager;
import br.com.voicetechnology.ng.ipx.rule.implement.RouteRuleManager;
import br.com.voicetechnology.ng.ipx.rule.implement.SessionManager;
import br.com.voicetechnology.ng.ipx.rule.implement.TerminalManager;
import br.com.voicetechnology.ng.ipx.rule.implement.UserCentrexManager;
import br.com.voicetechnology.ng.ipx.rule.implement.ivr.IVRManager;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class WebServicesManager extends Manager {
	public static final int RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FIRST = 1;
	public static final int RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_SECOND = 2;
	public static final int RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_THIRD = 3;
	public static final int RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FOURTH = 4;
	public static final int RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FIFTH = 5;
	public static final int RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_SIXTH = 6;
	public static final int RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_SEVENTH = 7;
	public static final int RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_EIGTH = 8;
	public static final int RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_NINETH = 9;

	// Manager
	private TerminalManager terminalManager;
	private PbxuserManager pbxuserManager;
	private CallBackManager callbackManager;
	private PbxManager pbxManager;
	private AddressManager addressManager;
	private NewsManager newsManager;
	private GroupManager groupManager;
	private SessionManager sessionManager;
	private ContactManager contactManager;
	private AboutMeManager aboutMeManager;
	private CallLogManager callLogManager;
	private FileinfoManager fileInfoManager;
	private IVRManager ivrManager;
	private GatewayManager gatewayManager;
	private RouteRuleManager routeRuleManager;
	private UserCentrexManager userCentrexManager;
	private RoleManager roleManager;
	private DialPlanManager dPlanManager;

	// DAO
	private AddressDAO addDAO;
	private ServiceclassDAO scDAO;
	private PbxuserDAO puDAO;
	private TerminalDAO tDAO;
	private GroupDAO gDAO;
	private RoleDAO rDAO;
	private CalllogDAO cLogDAO;
	private GroupfileDAO groupFileDAO;
	private UserfileDAO userFileDAO;
	private FileinfoDAO fileInfoDAO;
	private ContactDAO contactDAO;
	private ContactphonesDAO contactPhonesDAO;
	private IVRDAO ivrDAO;
	private PbxDAO pbxDAO;
	private GatewayDAO gatewayDAO;
	private UserDAO userDAO;
	private DomainDAO domainDAO;
	private PbxuserterminalDAO pbxuserTerminalDAO;
	private RouteruleDAO routeRuleDAO;
	private SipsessionlogDAO sipSessionlogDAO;
    private ConfigDAO configDAO;  // tveiga basix store 
    private PbxpreferenceDAO preferenceDAO; // tveiga basix store
    private ForwardDAO forwardDAO; // tveiga basix store 
    private RecordFileDAO recordfileDAO;
    
	public WebServicesManager(String loggerPath) throws DAOException {
		super(loggerPath);
		terminalManager = new TerminalManager(loggerPath);
		pbxuserManager = new PbxuserManager(loggerPath);
		callbackManager = new CallBackManager(loggerPath);
		pbxManager = new PbxManager(loggerPath);
		addressManager = new AddressManager(loggerPath);
		newsManager = new NewsManager(loggerPath);
		groupManager = new GroupManager(loggerPath);
		sessionManager = new SessionManager(loggerPath);
		contactManager = new ContactManager(loggerPath);
		aboutMeManager = new AboutMeManager(loggerPath);
		callLogManager = new CallLogManager(loggerPath);
		fileInfoManager = new FileinfoManager(loggerPath);
		ivrManager = new IVRManager(loggerPath);
		gatewayManager = new GatewayManager(loggerPath);
		routeRuleManager = new RouteRuleManager(loggerPath);
		userCentrexManager = new UserCentrexManager(loggerPath);
		roleManager = new RoleManager(loggerPath);
		dPlanManager = new DialPlanManager(loggerPath);

		addDAO = dao.getDAO(AddressDAO.class);
		scDAO = dao.getDAO(ServiceclassDAO.class);
		puDAO = dao.getDAO(PbxuserDAO.class);
		tDAO = dao.getDAO(TerminalDAO.class);
		gDAO = dao.getDAO(GroupDAO.class);
		rDAO = dao.getDAO(RoleDAO.class);
		cLogDAO = dao.getDAO(CalllogDAO.class);
		groupFileDAO = dao.getDAO(GroupfileDAO.class);
		userFileDAO = dao.getDAO(UserfileDAO.class);
		fileInfoDAO = dao.getDAO(FileinfoDAO.class);
		contactDAO = dao.getDAO(ContactDAO.class);
		contactPhonesDAO = dao.getDAO(ContactphonesDAO.class);
		ivrDAO = dao.getDAO(IVRDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		gatewayDAO = dao.getDAO(GatewayDAO.class);
		userDAO = dao.getDAO(UserDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
		pbxuserTerminalDAO = dao.getDAO(PbxuserterminalDAO.class);
		routeRuleDAO = dao.getDAO(RouteruleDAO.class);
		sipSessionlogDAO = dao.getDAO(SipsessionlogDAO.class);
		configDAO = dao.getDAO(ConfigDAO.class); // tveiga basix store
		preferenceDAO = dao.getDAO(PbxpreferenceDAO.class); // tveiga basix store
		forwardDAO = dao.getDAO(ForwardDAO.class); // tveiga basix store
		recordfileDAO = dao.getDAO(RecordFileDAO.class);
	}

	public void makeCall(String from, String to) throws Exception {
		SipAddressParser sipFrom = new SipAddressParser(from);
		Long fromKey = pbxuserManager.getPbxuserKeyByAddressAndDomain(sipFrom
				.getExtension(), sipFrom.getDomain());
		callbackManager.createClickToTalk(fromKey, to);
	}

	public void InactivateAllUserDIDExceptDefault(String username,
			String domainName) throws DAOException, ValidateObjectException,
			DeleteDependenceException, ValidationException {
		Domain domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Pbxuser pu = puDAO.getPbxuserByUsernameAndDomain(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		List<Long> didKeyList = addDAO.getDIDKeyExceptDefaultListByPbxuser(pu
				.getKey());

		addressManager.changeDIDStatus(didKeyList, Address.DEFINE_DELETED);
	}

	public void associateClassOfServiceToUser(String classOfServiceName,
			String username, String domainName) throws DAOException,
			ValidateObjectException, ValidationException {
		Serviceclass serviceClass = scDAO.getServiceClassByNameAndDomainName(
				classOfServiceName, domainName);
		if (serviceClass == null)
			throw new ValidationException(
					"Serviceclass is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_SERVICECLASS);

		Pbxuser pbxuser = getPbxuser(username, domainName);
		if (pbxuser == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		pbxuser.setServiceclassKey(serviceClass.getKey());
		puDAO.save(pbxuser);
	}

	public void createNews(NewsInfo newsInfo) throws DAOException,
			ValidateObjectException, ValidationException {
		Domain domain = getDomainByName(newsInfo.getDomainName());
		if (domain == null)
			throw new ValidationException(
					"Cannot create this news because the domain is invalid!!!",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		newsInfo.setDomainKey(domain.getKey());

		if (newsInfo.getGroupName() != null
				&& newsInfo.getGroupName().length() > 0) {
			Group group = gDAO.getGroupByAddressAndDomain(newsInfo
					.getGroupName(), domain.getDomain());
			if (group == null)
				throw new ValidationException(
						"Cannot create this news because the Group is invalid!!!",
						WebServiceConstantValues.RESULT_CODE_INVALID_GROUP);

			newsInfo.setGroupKey(group.getKey());
		}
		newsManager.save(newsInfo);
	}

	public void createTerminal(TerminalInfo terminalInfo, String domainName,
			String associationUsername) throws DAOException,
			ValidationException, ValidateObjectException {
		Domain domain = getDomainByName(domainName);
		if (domain == null)
			throw new ValidationException(
					"Cannot create this terminal because the domain is invalid!!!",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		if (terminalInfo.getUsername() == null
				&& terminalInfo.getPassword() == null)
			createTerminalInformations(terminalInfo, domainName);
		else {
			Pbx pbx = pbxManager.getPbxByDomainName(domainName);
			if (pbx != null
					&& pbx.getPbxPreferences() != null
					&& pbx.getPbxPreferences().getTerminalType().equals(
							Pbxpreference.TERMINAL_TYPE_AUTOMATIC))
				throw new ValidationException(
						"Cannot create numeric terminal on this Pbx because it doesn't support this configuration, only automatic terminal in this Pbx!!!",
						WebServiceConstantValues.RESULT_CODE_TERMINAL_INVALID_CONFIGURATION);
		}
		Serviceclass sc = scDAO.getDefaultServiceclass(domain.getKey());
		if (sc == null)
			throw new ValidationException(
					"Cannot create this terminal because the serviceClass Default doesn't exist!!!",
					WebServiceConstantValues.RESULT_CODE_INVALID_SERVICECLASS);

		if (associationUsername != null && associationUsername.length() > 0)
			createPbxuserTerminal(terminalInfo, associationUsername, domainName);

		terminalInfo.setDomainKey(domain.getKey());
		terminalInfo.setServiceclassKey(sc.getKey());
		terminalManager.save(terminalInfo);
	}

	public void associateUserToTerminal(String username, String terminalName,
			String domainName) throws DAOException, ValidationException,
			ValidateObjectException {
		Terminal terminal = getTerminalByTypeName(terminalName, domainName);
		if (terminal == null)
			throw new ValidationException(
					"Terminal is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_TERMINAL);

		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		TerminalInfo terminalInfo = terminalManager
				.getTerminalInfoByKey(terminal.getKey());
		createPbxuserTerminal(terminalInfo, username, domainName);

		terminalManager.save(terminalInfo);
	}

	public void createPBX(PBXInfo pbxInfo, String didTrunkLine)
			throws ValidateObjectException, ValidationException, DAOException,
			DeleteDependenceException, IOException {
		Domain domain = pbxManager.getRootDomain();
		if (domain == null)
			throw new ValidationException(
					"Cannot createPBX because domain root is invalid!!!",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Address address = addDAO.getAddress(didTrunkLine, domain.getKey());
		if (address == null)
			throw new ValidationException(
					"Cannot createPBX because did is invalid!!!",
					WebServiceConstantValues.RESULT_CODE_INVALID_DID);

		List<Long> didKeyList = new ArrayList<Long>();
		didKeyList.add(address.getKey());
		pbxInfo.setDIDInKeyList(didKeyList);
		pbxInfo.setDefaultaddressKey(address.getKey());

		pbxManager.savePBX(pbxInfo);
	}

	public void createDID(DIDInfo didInfo) throws ValidationException,
			DAOException, ValidateObjectException {
		Domain domain = getDomainByName(didInfo.getDomainName());
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		didInfo.setDomainKey(domain.getKey());
		if (didInfo.isTrunkLine()) {
			Pbx pbx = pbxManager.getPbxByDomainName(didInfo.getDomainName());
			if (pbx != null) {
				didInfo.setPbxKey(pbx.getKey());
				resetTrunkLine(pbx.getKey());
			}
		}
		if (didInfo.getSipID() != null && didInfo.getSipID().length() > 0) {
			Address address = addressManager.getAddress(didInfo.getSipID(),
					didInfo.getDomainName());
			if (address != null) {
				didInfo.setPbxuserKey(address.getPbxuserKey());
				didInfo.setGroupKey(address.getGroupKey());
			}
		}
		addressManager.save(didInfo);
	}

	public void createDID(DIDInfo didInfo, String anotherDIDAssociation)
			throws Exception {
		Address add = addDAO.getDIDAddress(anotherDIDAssociation);
		if (add == null)
			throw new ValidationException(
					"Cannot createDID because anotherDIDAssociation doesn't exist or is invalid!!!",
					WebServiceConstantValues.RESULT_CODE_INVALID_DID);

		didInfo.setPbxuserKey(add.getPbxuserKey());
		didInfo.setGroupKey(add.getGroupKey());
		this.createDID(didInfo);
	}

	public void updateDIDInfoEditSipID(DIDInfo didInfo) throws Exception {
		Address addressDID = addressManager.getAddress(
				didInfo.getAddressName(), didInfo.getDomainName());
		if (addressDID == null)
			throw new ValidationException("DID is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DID);

		Address addressSipID = addressManager.getAddress(didInfo.getSipID(),
				didInfo.getDomainName());
		if (addressSipID == null)
			throw new ValidationException(
					"Address is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_ADDRESS);

		addressDID.setPbxuserKey(addressSipID.getPbxuserKey());
		addressDID.setGroupKey(addressSipID.getGroupKey());

		didInfo = new DIDInfo(addressDID, didInfo.getSipID(), false);
		addressManager.save(didInfo);
	}

	public void updateDIDInfoEditNumber(DIDInfo didInfo, String newNumber)
			throws Exception {
		Address addressDID = addressManager.getAddress(
				didInfo.getAddressName(), didInfo.getDomainName());
		if (addressDID == null)
			throw new ValidationException("DID is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DID);

		addressDID.setAddress(newNumber);
		didInfo = new DIDInfo(addressDID, null, didInfo.getDomainName());

		addressManager.save(didInfo);
	}

	public void updateDIDInfoEditDomain(DIDInfo didInfo, String newDomainName)
			throws Exception {
		Domain domain = pbxManager.getDomain(newDomainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Address addressDID = addDAO.getDIDAddress(didInfo.getAddressName());
		if (addressDID == null)
			throw new ValidationException("DID is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DID);

		addressDID.setDomainKey(domain.getKey());
		didInfo = new DIDInfo(addressDID, null, newDomainName);

		addressManager.save(didInfo);
	}

	public void setDIDToTrunkLine(DIDInfo didInfo, String domainName)
			throws Exception {
		Pbx pbx = pbxManager.getPbxByDomainName(domainName);
		if (pbx == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Address addressDID = addressManager.getAddress(
				didInfo.getAddressName(), domainName);
		if (addressDID == null)
			throw new ValidationException("DID is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DID);

		if (addressDID.getPbxuserKey() != null
				|| addressDID.getGroupKey() != null)
			throw new ValidationException(
					"Cannot execute this update because the address is a!!!",
					WebServiceConstantValues.RESULT_CODE_DID_ALREADY_ASSOCIATED_TO_SOMEONE);

		resetTrunkLine(pbx.getKey());

		addressDID.setPbxKey(pbx.getKey());
		didInfo = new DIDInfo(addressDID, null, domainName);

		addressManager.save(didInfo);
	}

	public void createUser(PbxuserInfo pbxuserInfo, String domainName,
			String roleName, String serviceClassName, boolean isAdminUser)
			throws Exception {
		Serviceclass serviceclass = null;

		Domain domain = pbxManager.getDomain(domainName);
		if (domain == null || domain.getDomain().length() == 0
				|| domain.getDomain().equals(""))
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		if (serviceClassName != null && serviceClassName.length() > 0)
			serviceclass = scDAO.getServiceClassByNameAndDomainName(
					serviceClassName, domainName);
		else
			serviceclass = scDAO.getServiceClassByNameAndDomainName(
					Serviceclass.SERVICECLASS_NAME, domainName);
		if (serviceclass == null)
			throw new ValidationException(
					"Class of Service is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_SERVICECLASS);

		Pbx pbx = pbxManager.getPbxByDomainName(domainName);

		//inicio --> dnakamashi - adicionada validação de quota de Pbxuser - version 3.0.5 RC6.5.1
		Long userAmount = puDAO.countUsers(domain.getKey());
		if(userAmount >= pbx.getMaxUser())
			throw new QuotaException("User quota exceeded for this pbx!", QuotaException.Type.PBXUSER);
		//fim --> dnakamashi - adicionada validação de quota de Pbxuser - version 3.0.5 RC6.5.1
		
		
		Role role = null;
		if (isAdminUser)
			role = rDAO.getAdminRole();
		else if (roleName != null && roleName.length() > 0)
			role = rDAO.getRoleByName(roleName);
		else
			role = rDAO.getSimpleRole();
		if (role == null)
			throw new ValidationException("Role is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_ROLE);

		if (!isAdminUser && role.getAdmin() == Role.ROLE_ADMIN_ON)
			throw new ValidationException(
					"Cannot create this user because the configured role is invalid to Simple Users",
					WebServiceConstantValues.RESULT_CODE_ROLE_NOT_PERMITTED);

		pbxuserInfo.setDomainKey(domain.getKey());
		pbxuserInfo.setPbxKey(pbx.getKey());
		List<Long> roleKeyList = new ArrayList<Long>();
		roleKeyList.add(role.getKey());
		pbxuserInfo.setRoleKeyList(roleKeyList);
		pbxuserInfo.setServiceclassKey(serviceclass.getKey());

		pbxuserManager.save(pbxuserInfo);
	}

	public void createGroup(GroupInfo groupInfo, String domainName)
			throws Exception {
		Pbx pbx = pbxManager.getPbxByDomainName(domainName);
		if (pbx == null)
			throw new ValidationException(
					"Cannot execute this update because the domainName is invalid!!!",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);
		groupInfo.setPbxKey(pbx.getKey());

		groupManager.save(groupInfo);
	}

	public void addExtension(String domainName, String extension,
			Integer associationType, String associationAddress)
			throws Exception {
		switch (associationType) {
		case WebServiceConstantValues.OPERATION_ADD_EXTENSION_TO_USER:
			addExtensionToUser(associationAddress, domainName, extension);
			break;
		case WebServiceConstantValues.OPERATION_ADD_EXTENSION_TO_GROUP:
			addExtensionToGroup(associationAddress, domainName, extension);
			break;
		case WebServiceConstantValues.OPERATION_ADD_EXTENSION_TO_TERMINAL:
			addExtensionToTerminal(associationAddress, extension, domainName);
			break;
		default:
			break;
		}
	}

	public void addUserToGroup(String groupName, String domainName,
			String username, Integer priority, boolean isAdminUser)
			throws Exception {
		Pbxuser pbxuser = getPbxuser(username, domainName);
		if (pbxuser == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		Group group = gDAO.getGroupByAddressAndDomain(groupName, domainName);
		if (group == null)
			throw new ValidationException("Group is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_GROUP);

		GroupInfo groupInfo = groupManager.getGroupInfoByKey(group.getKey());
		List<UsergroupInfo> usergroupList = groupManager.getUsers(group
				.getKey());
		usergroupList.add(createUsergroupInfo(pbxuser.getKey(), priority,
				isAdminUser, username, false));
		groupInfo.setUsersInGroup(usergroupList);

		groupManager.save(groupInfo);
	}

	public void configureForward(UserForwardsInfo info, String address,
			String domainName, Integer forwardType) throws DAOException,
			ValidationException, ValidateObjectException {
		Address add = addDAO.getAddress(address, domainName);
		if (add == null)
			throw new ValidationException("DID is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DID);

		info = setUserForwardConfigurations(add.getPbxuserKey(), info,
				forwardType);
		pbxuserManager.updateForwardSettings(info);
	}

	public String[] getActiveDomainList() throws DAOException,
			ValidationException {
		List<Duo<Long, String>> domainList = pbxManager.getActiveDomainList();
		if (domainList == null || domainList.size() == 0)
			throw new ValidationException("There is no domain on System",
					WebServiceConstantValues.RESULT_CODE_NO_DOMAIN_EXIST);

		String[] domainsNameArray = new String[domainList.size()];
		for (int i = 0; i < domainList.size(); i++)
			domainsNameArray[i] = domainList.get(i).getSecond();
		return domainsNameArray;
	}

	public void changeUserPassword(String domainName, String username,
			String newPassword) throws ValidationException, DAOException,
			ValidateObjectException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		PbxuserInfo puInfo = pbxuserManager.getPbxuserInfoByKey(pu.getKey(),
				false);
		puInfo.setPassword(Crypt.encrypt(username, domainName, newPassword));

		pbxuserManager.save(puInfo);
	}

	public PbxuserInfo validateUser(LoginInfo loginInfo) throws DAOException,
			ValidateObjectException, InvalidLoginException, ValidationException {
		UserSessionInfo userSessionInfo = sessionManager
				.createUserSession(loginInfo);
		if (userSessionInfo == null || userSessionInfo.getPbxuser() == null)
			throw new ValidationException(
					"Cannot execute this method because userSessionInfo is invalid!!!",
					WebServiceConstantValues.RESULT_CODE_LOGIN_INVALID_SESSION);

		PbxuserInfo info = pbxuserManager.getPbxuserInfoByKey(userSessionInfo
				.getPbxuser().getKey(), false);
		return info;
	}

	public PbxuserInfo getUniqueUser(String domainName, String username)
			throws DAOException, ValidationException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		PbxuserInfo info = pbxuserManager.getPbxuserInfoByKey(pu.getKey(),
				false);
		return info;
	}

	public void deleteMissedCalls(String domainName, String username)
			throws DAOException, ValidationException, ValidateObjectException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		pbxuserManager.clearMissedCalls(pu.getKey());
	}

	public void saveNumberInAddressBook(ContactsInfo contactInfo,
			String domainName, String username) throws DAOException,
			ValidationException, ValidateObjectException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		contactInfo.setUserKey(pu.getUserKey());
		contactInfo.setDomainKey(pu.getUser().getDomainKey());

		contactManager.saveContact(contactInfo);
	}

	public void changeUserStatus(PresenceInfo presenceInfo, String domainName,
			String username) throws DAOException, ValidationException,
			ValidateObjectException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		presenceInfo.setKey(pu.getPresenceKey());
		aboutMeManager.saveOrUpdatePresence(presenceInfo);
	}

	public ReportResult findContacts(Report<ContactsInfo> report,
			String domainName, String username) throws DAOException,
			ValidationException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		ContactsInfo contactsInfo = report.getInfo();
		contactsInfo.setDomainKey(pu.getUser().getDomainKey());
		contactsInfo.setUserKey(pu.getUserKey());

		ReportResult reportResult = contactManager.findContacts(report);
		return reportResult;
	}

	public ReportResult findCalllogs(Report<CalllogInfo> report,
			String domainName, String username) throws DAOException,
			ValidationException, ValidateObjectException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		CalllogInfo calllogInfo = report.getInfo();
		calllogInfo.setPbxuserKey(pu.getKey());
		calllogInfo.setDomainKey(pu.getUser().getDomainKey());

		ReportResult reportResult = callLogManager.findCalllogs(report);
		return reportResult;
	}

	public void recordVoiceMail(String domainName, String username,
			String destinationNumber) throws Exception {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		Address add = addDAO.getAddress(destinationNumber, domainName);
		if (add == null)
			throw new ValidationException(
					"Destination number address is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_ADDRESS);

		callbackManager.createClickToMessage(pu.getKey(), destinationNumber);
	}

	public void deleteVoiceMail(FileInfo fileInfo, String domainName,
			String username, Long callLogKey) throws Exception {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		Long fileKey = getFileinfoKey(pu, fileInfo, callLogKey);
		if (fileKey == null)
			throw new ValidationException("File is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_FILE);

		fileInfoManager.deleteFileinfo(fileKey);
	}

	public String getUrlForDownloadFile(FileInfo fileInfo, String domainName,
			String username, Long callLogKey) throws Exception {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		Long fileKey = getFileinfoKey(pu, fileInfo, callLogKey);
		if (fileKey == null)
			throw new ValidationException("File is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_FILE);

		File file = new File(fileInfoManager.getDownloadableFile(fileKey));
		TempFileManager tmpManager = (TempFileManager) FileUtils
				.getTempFileManager(FileUtils.TEMP_FILEMANAGER_TYPE_WEBSERVICES);
		File copied = tmpManager.copyFile(file);

		String url = IPXProperties
				.getProperty(IPXPropertiesType.WEBSERVICES_BASIX_URL)
				+ File.separator + "tmp" + File.separator + copied.getName();
		return url;
	}

	public List<SimpleContactInfo> searchInAddressBookByParameter(
			String domainName, String username, String searchWord)
			throws DAOException, ValidationException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		List<SimpleContactInfo> simpleContactInfoList = new ArrayList<SimpleContactInfo>();
		// 1
		String lastDialedCall = cLogDAO.getListByCalledNumber(pu.getKey(),
				searchWord);
		if (lastDialedCall != null && lastDialedCall.length() > 0)
			simpleContactInfoList.add(new SimpleContactInfo(lastDialedCall,
					RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FIRST));

		// 2
		simpleContactInfoList.addAll(removeDuplicateEntries(
				simpleContactInfoList, puDAO
						.getUsersAndPbxusersWithPresenceByDomain(pu.getUser()
								.getDomainKey(), searchWord,
								User.Fields.USERNAME,
								RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_SECOND)));

		// 3
		simpleContactInfoList.addAll(removeDuplicateEntries(
				simpleContactInfoList, puDAO
						.getUsersAndPbxusersWithPresenceByDomain(pu.getUser()
								.getDomainKey(), makeFinalLike(searchWord),
								User.Fields.USERNAME,
								RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_THIRD)));

		// 4
		simpleContactInfoList.addAll(removeDuplicateEntries(
				simpleContactInfoList, puDAO
						.getUsersAndPbxusersWithPresenceByDomain(pu.getUser()
								.getDomainKey(), makeFinalLike(searchWord),
								User.Fields.NAME,
								RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FOURTH)));

		// 4
		List<Contact> contactList = contactDAO.getAllContacts(pu.getUserKey(),
				pu.getUser().getDomainKey(), makeFinalLike(searchWord));
		simpleContactInfoList.addAll(makeSimpleContactInfoForContact(
				contactList, simpleContactInfoList,
				RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FOURTH));

		// 5
		simpleContactInfoList.addAll(removeDuplicateEntries(
				simpleContactInfoList, puDAO
						.getUsersAndPbxusersWithPresenceByDomain(pu.getUser()
								.getDomainKey(), makeLike(" " + searchWord),
								User.Fields.NAME,
								RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FIFTH)));

		// 5
		contactList = contactDAO.getAllContacts(pu.getUserKey(), pu.getUser()
				.getDomainKey(), makeLike(" " + searchWord));
		simpleContactInfoList.addAll(makeSimpleContactInfoForContact(
				contactList, simpleContactInfoList,
				RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FIFTH));

		// 6
		simpleContactInfoList.addAll(removeDuplicateEntries(
				simpleContactInfoList, puDAO
						.getUsersAndPbxusersWithPresenceByDomain(pu.getUser()
								.getDomainKey(), makeLike(searchWord),
								User.Fields.USERNAME,
								RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_SIXTH)));
		simpleContactInfoList.addAll(removeDuplicateEntries(
				simpleContactInfoList, puDAO
						.getUsersAndPbxusersWithPresenceByDomain(pu.getUser()
								.getDomainKey(), makeLike(searchWord),
								User.Fields.NAME,
								RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_SIXTH)));

		// 7
		contactList = contactDAO.getAllContacts(pu.getUserKey(), pu.getUser()
				.getDomainKey(), makeLike(searchWord));
		simpleContactInfoList.addAll(makeSimpleContactInfoForContact(
				contactList, simpleContactInfoList,
				RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_SEVENTH));

		Pbx pbx = pbxManager.getPbxByDomainName(domainName);
		if (pbx == null)
			throw new ValidationException("Pbx is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		// 8
		List<Group> groupList = gDAO.getGroupsInPBX(pbx.getKey(), searchWord);
		simpleContactInfoList
				.addAll(makeSimpleContactInfoForGroup(groupList,
						simpleContactInfoList,
						RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_EIGTH));

		// 8
		groupList = gDAO
				.getGroupsInPBX(pbx.getKey(), makeFinalLike(searchWord));
		simpleContactInfoList
				.addAll(makeSimpleContactInfoForGroup(groupList,
						simpleContactInfoList,
						RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_EIGTH));

		// 9
		List<IVR> ivrList = ivrDAO.getIVRListByDomain(pu.getUser().getDomain()
				.getDomain(), searchWord);
		simpleContactInfoList.addAll(makeSimpleContactForIVR(ivrList,
				simpleContactInfoList,
				RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_NINETH));

		// 9
		ivrList = ivrDAO.getIVRListByDomain(pu.getUser().getDomain()
				.getDomain(), makeFinalLike(searchWord));
		simpleContactInfoList.addAll(makeSimpleContactForIVR(ivrList,
				simpleContactInfoList,
				RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_NINETH));

		return simpleContactInfoList;
	}

	public SimpleContactInfo getSimpleContactInfo(String domainName,
			String username, Long contactKey, Long pbxuserKey, Long groupKey,
			Long ivrKey) throws DAOException, ValidationException,
			QuotaException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		if (contactKey != null) {
			ContactsInfo info = contactManager.getContactInfo(contactKey);
			if (info == null)
				throw new ValidationException(
						"Contact is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_CONTACT);

			List<PhoneInfo> phones = new ArrayList<PhoneInfo>();
			for (Contactphones contactPhones : info.getContactphonesList())
				phones.add(new PhoneInfo(contactPhones.getType(), contactPhones
						.getPhone()));
			return new SimpleContactInfo(info.getContact(), phones,
					RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FIRST);
		} else if (pbxuserKey != null) {
			PbxuserInfo puInfo = pbxuserManager.getPbxuserInfoByKey(pbxuserKey,
					false);
			if (puInfo == null)
				throw new ValidationException(
						"Requested User is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_USER);

			List<PhoneInfo> phoneList = makePhoneList(
					puInfo.getExtensionList(), puInfo.getDIDList());
			return new SimpleContactInfo(puInfo.getPbxuser(), puInfo
					.getPresence(), phoneList);
		} else if (groupKey != null) {
			GroupInfo groupInfo = groupManager.getGroupInfoByKey(groupKey);
			if (groupInfo == null)
				throw new ValidationException(
						"Group is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_GROUP);

			List<PhoneInfo> phoneList = makePhoneList(groupInfo
					.getExtensionList(), groupInfo.getDIDList());
			Presence groupPresence = new Presence();
			groupPresence.setState(Presence.STATE_ONLINE);
			return new SimpleContactInfo(groupInfo.getGroup(), groupPresence,
					phoneList, RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FIRST);
		} else if (ivrKey != null) {
			IVRInfo ivrInfo = ivrManager.getIVRInfoByKey(ivrKey);
			if (ivrInfo == null)
				throw new ValidationException(
						"IVR is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_IVR);

			List<PhoneInfo> phoneList = makePhoneList(ivrInfo
					.getExtensionList(), ivrInfo.getDIDList());
			return new SimpleContactInfo(ivrInfo.getIVR(), phoneList,
					RELEVANCE_ORDER_SEARCH_ADDRESSBOOK_FIRST);
		}
		return null;
	}

	public void changePBXConfigurations(PBXInfo pbxInfo) throws DAOException,
			ValidateObjectException, DeleteDependenceException, IOException,
			ValidationException {
		Pbx pbx = pbxManager.getPbxByDomainName(pbxInfo.getName());
		if (pbx == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		PBXInfo pbxInfoSaved = pbxManager.getPBXInfo(pbx.getKey());
		pbxInfoSaved
				.setMaxIVRApplications(pbxInfo.getMaxIVRApplications() != null ? pbxInfo
						.getMaxIVRApplications()
						: pbxInfoSaved.getMaxIVRApplications());
		pbxInfoSaved.setMaxUsers(pbxInfo.getMaxUsers() != null ? pbxInfo
				.getMaxUsers() : pbxInfoSaved.getMaxUsers());
		pbxInfoSaved.setMaxQuota(pbxInfo.getMaxQuota() != null ? pbxInfo
				.getMaxQuota() : pbxInfoSaved.getMaxQuota());
		pbxInfoSaved
				.setKoushiKubunEnable(pbxInfo.getKoushiKubunEnable() != null ? pbxInfo
						.getKoushiKubunEnable()
						: pbxInfoSaved.getKoushiKubunEnable());

		pbxManager.savePBX(pbxInfoSaved);
	}

	public void setDefaultOperatorForDomain(String domainName,
			String associationAddress) throws DAOException,
			ValidationException, ValidateObjectException {
		Pbx pbx = pbxManager.getPbxByDomainName(domainName);
		if (pbx == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Address add = null;
		Pbxuser pu = puDAO.getPbxuserByUsernameAndDomain(associationAddress,
				domainName);
		if (pu != null)
			add = addDAO.getSipIDByPbxuser(pu.getKey());
		else {
			Group group = gDAO.getGroupByAddressAndDomain(associationAddress,
					domainName);
			if (group != null)
				add = addDAO.getSipIDByGroup(group.getKey());
		}
		if (add == null)
			throw new ValidationException(
					"Address groupname or username is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_ADDRESS);

		pbx.setDefaultaddressKey(add.getKey());
		pbxDAO.save(pbx);
	}

	public void setDefaultDIDToUser(String domainName, String username,
			String didNumber) throws DAOException, ValidationException,
			ValidateObjectException {
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		PbxuserInfo puInfo = pbxuserManager.getPbxuserInfoByKey(pu.getKey(),
				false);
		boolean isPermit = verifyDIDnumber(puInfo.getDIDList(), didNumber);
		if (!isPermit)
			throw new ValidationException(
					"DID doesn't associated to user will be done this setting",
					WebServiceConstantValues.RESULT_CODE_DID_NOT_ASSOCIATED_TO_USER);

		Address add = addDAO.getDIDAddress(didNumber);
		pu.setDefaultDIDKey(add.getKey());
		puDAO.save(pu);
	}

	public void createGateway(GatewayInfo gatewayInfo)
			throws ValidateObjectException, DeleteDependenceException,
			SessionManagerException, VoiceException {
		Domain domain = pbxManager.getRootDomain();
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		gatewayInfo.setDomainKey(domain.getKey());
		gatewayInfo.setPassword(Crypt.encrypt(gatewayInfo.getName(), domain
				.getDomain(), gatewayInfo.getPassword()));
		gatewayManager.saveGateway(gatewayInfo);
	}

	public void createRouteRole(RouteRuleInfo routeRuleInfo, String gatewayName)
			throws Exception {
		Domain domain = pbxManager.getRootDomain();
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		User user = userDAO.getUserByUsernameAndDomain(gatewayName, domain
				.getDomain());
		if (user == null)
			throw new ValidationException(
					"Gateway user is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		Gateway gateway = gatewayDAO.getGatewayByUserKey(user.getKey());
		if (gateway == null)
			throw new ValidationException(
					"Gateway is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_GATEWAY);

		routeRuleInfo.setGatewayKey(gateway.getKey());
		routeRuleManager.saveRouteRule(routeRuleInfo);
	}
   
	public void delete(String[] params, int classID, boolean isForced) throws DAOException,
	ValidateObjectException, DeleteDependenceException,
	ValidationException, IOException
	{
		switch (classID) {

		case Address.classID:
			deleteDID(params[0], isForced);
			break;
		case Pbx.classID:
			deletePBX(params[0]);
			break;
		case Group.classID:
			deleteGroup(params[1], params[0], false);
			break;
		case Terminal.classID:
			deleteTerminal(params[1], params[0]);
			break;
		case Pbxuser.classID:
			//tveiga , ezaghi - adicionando delete forced - inicio - basix 3.05
			if(isForced)
			   deletePbxuser(params[1], params[0], isForced);
			else
			   deletePbxuser(params[1], params[0]);
			break;
			//tveiga , ezaghi - adicionando delete forced - fim - basix 3.05
		case Gateway.classID:
			deleteGateway(params[0]);
			break;
		case Routerule.classID:
			deleteRouteRule(params[0]);
			break;
		case User.classID:
			deleteUserCentrex(params[0]);
			break;
		case Role.classID:
			deleteRole(params[0]);
			break;
		default:
			break;
		}
	}

	private void deleteDID(String didNumber, boolean isForced) throws DAOException,
			ValidationException, ValidateObjectException {
		Address add = addDAO.getDIDAddress(didNumber);
		//tveiga - nao era possivel deletar um did inativo pelo web service , agora funciona - inicio
		// klabunde - trocado o status de inactive para deleted
		if (add == null)
		{
		  add = addDAO.getDIDAddress(didNumber, Address.DEFINE_DELETED);
		  if (add == null)
			throw new ValidationException("DID is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DID);
		}
		//tveiga - nao era possivel deletar um did inativo pelo web service , agora funciona - fim
		try {
			addressManager.deleteDID(add.getKey(), isForced);
		} catch (Exception e) {
			if (e instanceof DeleteDependenceException)
				throw new ValidationException(e.getMessage(),
						WebServiceConstantValues.RESULT_CODE_DID_IS_TRUNK_LINE,
						e);
		}
	}

	private void deleteGroup(String groupName, String domain, Boolean isForced) throws DAOException, ValidationException, ValidateObjectException {
		Group group = gDAO.getGroupByAddressAndDomain(groupName, domain);
		if (group == null)
			throw new ValidationException("Group is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_GROUP);

		List<Long> groupKeys = new ArrayList<Long>();
		groupKeys.add(group.getKey());
		try {
			groupManager.deleteGroups(groupKeys, isForced);//dnakamashi - ezaghi - vmartinez - deleção forçada de grupo - version 3.0.5 RC6.4
		} catch (DeleteDependenceException e) {
			DeleteDependenceException del = (DeleteDependenceException) e;
			if (del.getDependenceClass().equals(Usergroup.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_GROUP_HAS_LOGGED_USER);
			else if (del.getDependenceClass().equals(Forward.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_GROUP_IS_FORWARD_DESTINATION);
			else if (del.getDependenceClass().equals(NightmodeGroup.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_GROUP_IS_NIGHTMODE_FROM_OTHER_GROUP);
			else if (del.getDependenceClass().equals(IVR.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_GROUP_IS_IVR_FORWARD_DESTINATION);
			else if (del.getDependenceClass().equals(Pbx.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_GROUP_IS_NIGHTMODE_OR_DEFAULTOPERATOR_FROM_PBX);
		}
	}

	private void deletePBX(String domainName) throws DAOException,
			ValidationException, ValidateObjectException, IOException {
		Pbx pbx = pbxDAO.getPbxByDomain(domainName);
		if (pbx == null)
			throw new ValidationException("Pbx is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_PBX);

		List<Long> pbxKeys = new ArrayList<Long>();
		pbxKeys.add(pbx.getKey());
		try {
			pbxManager.deletePBX(pbxKeys);
		}catch (IOException e){
			throw new ValidationException(
					e.getMessage(),
					WebServiceConstantValues.RESULT_CODE_ERROR_DELETE_DOMAINFOLDER);
		}catch (DeleteDependenceException e) {
			DeleteDependenceException del = (DeleteDependenceException) e;
			if (del.getDependenceClass().equals(Activecall.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_PBX_HAS_ACTIVECALLS);
		}
	}

	private Routerule getRouteRule(String param) throws DAOException {
		Routerule routeRule = null;
		routeRule = routeRuleDAO.getRouteRuleByPattern(param);
		if (routeRule == null)
			routeRule = routeRuleDAO.getRouteRuleByPriority(Integer
					.valueOf(param));
		return routeRule;
	}

	private void deleteTerminal(String terminalName, String domainName)
			throws DAOException, ValidationException, ValidateObjectException {
		Terminal terminal = getTerminalByTypeName(terminalName, domainName);
		if (terminal == null)
			throw new ValidationException(
					"Terminal is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_TERMINAL);

		List<Long> terminalKeys = new ArrayList<Long>();
		terminalKeys.add(terminal.getKey());
		try {
			terminalManager.deleteTerminals(terminalKeys);
		} catch (DeleteDependenceException e) {
			DeleteDependenceException del = (DeleteDependenceException) e;
			if (del.getDependenceClass().equals(Forward.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_TERMINAL_IS_FORWARD_DESTINATION);
			else if (del.getDependenceClass().equals(NightmodeGroup.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_TERMINAL_IS_NIGHTMODE_DESTINATION);
			else if (del.getDependenceClass().equals(IVR.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_TERMINAL_IS_IVR_FORWARD_DESTINATION);
		}
	}

	private void deletePbxuser(String username, String domainName)
			throws DAOException, ValidationException, ValidateObjectException {
		Pbxuser pu = puDAO.getPbxuserByUsernameAndDomain(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		List<Long> puKeys = new ArrayList<Long>();
		puKeys.add(pu.getKey());
		try {
			pbxuserManager.deletePbxusers(puKeys, false);
		} catch (DeleteDependenceException e) {
			DeleteDependenceException del = (DeleteDependenceException) e;
			if (del.getDependenceClass().equals(Forward.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_FORWARD_DESTINATION);
			else if (del.getDependenceClass().equals(NightmodeGroup.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_NIGHTMODE_GROUP_DESTINATION);
			else if (del.getDependenceClass().equals(IVR.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_IVR_FORWARD_DESTINATION);
			else if (del.getDependenceClass().equals(Pbx.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_NIGHTMODE_OR_DEFAULTOPERATOR_FROM_PBX);
			else if (del.getDependenceClass().equals(Activecall.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_HAS_ACTIVECALL);
			else if (del.getDependenceClass().equals(Group.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_GROUP_MEMBER);
			else if (del.getDependenceClass().equals(Sessionlog.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_HAS_WEB_ACTIVE_SESSION);
		}
	}

	//tveiga , ezaghi - adicionando delete forced - inicio - basix 3.05
	private void deletePbxuser(String username, String domainName, boolean isForced) throws DAOException, ValidationException, ValidateObjectException 
	{
		Pbxuser pu = puDAO.getPbxuserByUsernameAndDomain(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		List<Long> puKeys = new ArrayList<Long>();
		puKeys.add(pu.getKey());
		try {
			pbxuserManager.deletePbxusers(puKeys, isForced);
		} catch (DeleteDependenceException e) {
			DeleteDependenceException del = (DeleteDependenceException) e;
			if (del.getDependenceClass().equals(Forward.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_FORWARD_DESTINATION);
			else if (del.getDependenceClass().equals(NightmodeGroup.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_NIGHTMODE_GROUP_DESTINATION);
			else if (del.getDependenceClass().equals(IVR.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_IVR_FORWARD_DESTINATION);
			else if (del.getDependenceClass().equals(Pbx.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_NIGHTMODE_OR_DEFAULTOPERATOR_FROM_PBX);
			else if (del.getDependenceClass().equals(Activecall.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_HAS_ACTIVECALL);
			else if (del.getDependenceClass().equals(Group.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_IS_GROUP_MEMBER);
			else if (del.getDependenceClass().equals(Sessionlog.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USER_HAS_WEB_ACTIVE_SESSION);
		}
	}
	//tveiga , ezaghi - adicionando delete forced - fim - basix 3.05
	
	private void deleteGateway(String gatewayName) throws DAOException,
			ValidationException, ValidateObjectException {
		Gateway gateway = gatewayDAO.getGatewayByName(gatewayName);
		if (gateway == null)
			throw new ValidationException(
					"Gateway is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_GATEWAY);

		List<Long> gatewayKeys = new ArrayList<Long>();
		gatewayKeys.add(gateway.getKey());
		try {
			gatewayManager.deleteGateways(gatewayKeys);
		} catch (DeleteDependenceException e) {
			DeleteDependenceException del = (DeleteDependenceException) e;
			if (del.getDependenceClass().equals(Routerule.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_GATEWAY_HAS_ROUTERULE_ASSOCIATED);
		}
	}

	private void deleteRouteRule(String param) throws DAOException,
			ValidationException {
		Routerule routeRule = getRouteRule(param);
		if (routeRule == null)
			throw new ValidationException(
					"Routerule is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_ROUTERULE);

		List<Long> routeRuleKeys = new ArrayList<Long>();
		routeRuleKeys.add(routeRule.getKey());
		routeRuleManager.deleteRouteRules(routeRuleKeys);
	}

	private void deleteUserCentrex(String username) throws DAOException,
			ValidationException, ValidateObjectException {
		Domain domain = pbxManager.getRootDomain();
		if (domain == null)
			throw new ValidationException(
					"Root Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		User user = userDAO.getUserCentrexAdmin(username, domain.getDomain());
		if (user == null)
			throw new ValidationException(
					"User centrex is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USERCENTREX);

		List<Long> ucKeys = new ArrayList<Long>();
		ucKeys.add(user.getKey());
		try {
			userCentrexManager.deleteUserCentrex(ucKeys);
		} catch (DeleteDependenceException e) {
			DeleteDependenceException del = (DeleteDependenceException) e;
			if (del.getDependenceClass().equals(Sessionlog.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_USERCENTREX_HAS_WEB_ACTIVE_SESSION);
		}
	}

	private void deleteRole(String roleName) throws DAOException,
			ValidationException, ValidateObjectException {
		Role role = rDAO.getRoleByName(roleName);
		if (role == null)
			throw new ValidationException("Role is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_ROLE);

		List<Long> roleKeys = new ArrayList<Long>();
		roleKeys.add(role.getKey());
		try {
			roleManager.deleteRole(roleKeys);
		} catch (DeleteDependenceException e) {
			DeleteDependenceException del = (DeleteDependenceException) e;
			if (del.getDependenceClass().equals(User.class))
				throw new ValidationException(
						del.getMessage(),
						WebServiceConstantValues.RESULT_CODE_ROLE_IS_ASSOCIATED_TO_USERS);
		}
	}

	public List getObjectList(String domainName, int classID)
			throws DAOException, ValidationException {
		List objectList = null;
		Domain domain = null;
		if (domainName == null)
			domain = pbxManager.getRootDomain();
		else
			domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		switch (classID) {
		case Address.classID:
			DIDInfo didInfo = null;
			objectList = new ArrayList<DIDInfo>();
			List<Address> addressList = addDAO.getDIDsInDomain(domain.getKey());

			for (Address address : addressList) {
				didInfo = addressManager.getDIDInfoByKey(address.getKey());
				objectList.add(didInfo);
			}
			break;

		case Pbxuser.classID:
			PbxuserInfo puInfo = null;
			objectList = new ArrayList<PbxuserInfo>();
			List<Pbxuser> puList = puDAO.getPbxuserAndUsersInDomain(domain
					.getKey());

			for (Pbxuser pbxuser : puList) {
				puInfo = pbxuserManager.getPbxuserInfoByKey(pbxuser.getKey(),
						false);
				objectList.add(puInfo);
			}
			break;

		case Group.classID:
			Pbx pbx = pbxDAO.getPbxByDomain(domain.getKey());
			GroupInfo groupInfo = null;
			objectList = new ArrayList<GroupInfo>();
			List<Group> groupList = gDAO.getGroupsInPBX(pbx.getKey());
			if (groupList == null || groupList.size() == 0)
				throw new ValidationException(
						"There is no Group on this Domain",
						WebServiceConstantValues.RESULT_CODE_NO_GROUP_IN_DOMAIN);

			for (Group group : groupList) {
				groupInfo = groupManager.getGroupInfoByKey(group.getKey());
				objectList.add(groupInfo);
			}
			break;

		case Terminal.classID:
			TerminalInfo terminalInfo = null;
			objectList = new ArrayList<TerminalInfo>();
			List<Terminal> terminalList = tDAO.getTerminalListByDomain(domain
					.getKey());
			if (terminalList == null || terminalList.size() == 0)
				throw new ValidationException(
						"There is no Terminal on this Domain",
						WebServiceConstantValues.RESULT_CODE_NO_TERMINAL_IN_DOMAIN);

			for (Terminal terminal : terminalList) {
				terminalInfo = terminalManager.getTerminalInfoByKey(terminal
						.getKey());
				objectList.add(terminalInfo);
			}
			break;

		case Routerule.classID:
			RouteRuleInfo routeRuleInfo = null;
			objectList = new ArrayList<Routerule>();
			List<Routerule> routeRuleList = routeRuleDAO
					.getRouteListByPriority();

			for (Routerule routeRule : routeRuleList) {
				routeRuleInfo = routeRuleManager.getRouteRuleInfo(routeRule
						.getKey());
				objectList.add(routeRuleInfo);
			}
			break;

		case Gateway.classID:
			GatewayInfo gatewayInfo = null;
			objectList = new ArrayList<Gateway>();
			List<Duo<Long, String>> gatewayList = gatewayDAO.getGatewayList();

			for (Duo<Long, String> duo : gatewayList) {
				gatewayInfo = gatewayManager.getGatewayInfo(duo.getFirst());
				objectList.add(gatewayInfo);
			}
			break;

		case User.classID:
			UserCentrexInfo userCentrexInfo = null;
			objectList = new ArrayList<UserCentrexInfo>();
			List<User> userCentrexList = userDAO.getUserCentrexList();

			for (User userCentrex : userCentrexList) {
				userCentrexInfo = userCentrexManager
						.getUserCentrexInfo(userCentrex.getKey());
				objectList.add(userCentrexInfo);
			}
			break;

		case Role.classID:
			RoleInfo roleInfo = null;
			objectList = new ArrayList<RoleInfo>();
			List<Role> roleList = rDAO.getRoleListMinusDefaultAndRootRole();

			for (Role role : roleList) {
				roleInfo = roleManager.getRoleInfoByKey(role.getKey(), true);
				objectList.add(roleInfo);
			}
			break;
		default:
			return null;
		}
		return objectList;
	}

	public void createUserCentrex(UserCentrexInfo userCentrexInfo)
			throws Exception {
		Domain domain = pbxManager.getRootDomain();
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		userCentrexInfo.setItensPerPage(Preference.ITENS_PER_PAGE_DEFAULT);
		userCentrexInfo.setDomainKey(domain.getKey());
		userCentrexInfo.setPassword(Crypt.encrypt(userCentrexInfo.getName(),
				domain.getDomain(), userCentrexInfo.getPassword()));
		if (userCentrexInfo.getLocale().equals(null)
				|| userCentrexInfo.getLocale().equals(""))
			userCentrexInfo.setLocale(Preference.LOCALE_DEFAULT);
		userCentrexManager.saveUserCentrex(userCentrexInfo);
	}

	public void createRole(RoleInfo roleInfo) throws Exception {
		roleInfo.setCentrexAdmin(false);
		roleInfo.setRoleAdmin(false);
		roleInfo.setRoleDefault(false);

		roleManager.save(roleInfo);
	}
	
	public RecordFileInfo getCallIdInformations(String callId) throws DAOException,
		ValidationException {
		RecordFileInfo recordfileinfo = recordfileDAO.getRecordFileInfobySipCallId(callId);
		if (recordfileinfo == null)
			throw new ValidationException("SIPCallID is invalid or doesn't exist",
			WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);
		return recordfileinfo;
	}
	
	
	public PBXInfo getPBXInformations(String domainName) throws DAOException,
			ValidationException {
		Pbx pbx = pbxDAO.getPbxByDomain(domainName);
		if (pbx == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		return pbxManager.getPBXInfo(pbx.getKey());
	}

	/*
	 * ##################################################################
	 * ############# METODOS PARA MIGRACAO DO SISTEMA FLIP ##############
	 * ##################################################################
	 */

	public void manageActiveInactive(String domainName, String address,
			boolean isActivate, Integer classID) throws DAOException,
			ValidateObjectException, DeleteDependenceException,
			ValidationException {
		switch (classID) {
		case User.classID:
			activeDesactiveUser(domainName, address, isActivate);
			break;
		case Terminal.classID:
			activeDesactiveTerminal(domainName, address, isActivate);
			break;
		default:
			break;
		}
	}

	private void activeDesactiveUser(String domainName, String username,
			boolean isActivate) throws DAOException, ValidateObjectException,
			DeleteDependenceException, ValidationException {
		Domain domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Pbxuser pu = puDAO.getPbxuserByUsernameAndDomain(username, domainName,
				isActivate ? Pbxuser.DEFINE_INACTIVE : Pbxuser.DEFINE_ACTIVE);
		if (pu == null) {
			pu = puDAO.getPbxuserByUsernameAndDomain(username, domainName,
					isActivate ? Pbxuser.DEFINE_ACTIVE
							: Pbxuser.DEFINE_INACTIVE);
			if (pu == null)
				throw new ValidationException(
						"User is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_USER);
			throw new ValidationException(
					"User is already "
							+ (isActivate ? "activated" : "desactivated"),
					WebServiceConstantValues.RESULT_CODE_OBJECT_ALREADY_ACTIVATED_OR_DESACTIVATED);
		}
		pu.getUser().setActive(
				isActivate ? Pbxuser.DEFINE_ACTIVE : Pbxuser.DEFINE_INACTIVE);
		userDAO.save(pu.getUser());

		pu.setActive(isActivate ? Pbxuser.DEFINE_ACTIVE
				: Pbxuser.DEFINE_INACTIVE);
		puDAO.save(pu);

		if (!isActivate)
			pbxuserManager.deleteSessionAndDependences(pu, false);
	}

	private void activeDesactiveTerminal(String domainName,
			String terminalName, boolean isActivate) throws DAOException,
			ValidateObjectException, DeleteDependenceException,
			ValidationException {
		Domain domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Terminal terminal = getTerminalByTypeName(terminalName, domainName,
				isActivate ? Terminal.DEFINE_INACTIVE : Terminal.DEFINE_ACTIVE);
		if (terminal == null) {
			terminal = getTerminalByTypeName(terminalName, domainName,
					isActivate ? Terminal.DEFINE_ACTIVE
							: Terminal.DEFINE_INACTIVE);
			if (terminal == null)
				throw new ValidationException(
						"Terminal is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_TERMINAL);
			throw new ValidationException(
					"Terminal is already "
							+ (isActivate ? "activated" : "desactivated"),
					WebServiceConstantValues.RESULT_CODE_OBJECT_ALREADY_ACTIVATED_OR_DESACTIVATED);
		}
		Integer status = isActivate ? Terminal.DEFINE_ACTIVE
				: Terminal.DEFINE_INACTIVE;

		terminal.setActive(status);
		tDAO.save(terminal);

		terminal.getPbxuser().setActive(status);
		puDAO.save(terminal.getPbxuser());

		terminal.getPbxuser().getUser().setActive(status);
		userDAO.save(terminal.getPbxuser().getUser());

		if (!isActivate)
			pbxuserManager.deleteSessionAndDependences(terminal.getPbxuser(), false);
	}

	public void manageStatusListForSomeRelatedToUser(String domainName,
			String username, boolean isActivate, Integer classID)
			throws DAOException, ValidateObjectException, ValidationException {
		Domain domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Pbxuser pu = null;

		if (isActivate)
			pu = puDAO.getPbxuserByUsernameAndDomain(username, domainName);
		else
			pu = puDAO.getActiveOrInactivePbxuserByUsernameAndDomain(username,
					domainName);

		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		Integer status = isActivate ? Terminal.DEFINE_ACTIVE
				: Terminal.DEFINE_INACTIVE;
		switch (classID) {
		case Terminal.classID:
			List<Terminal> terminalList = tDAO
					.getTerminalListAssociatedWithPbxuser(pu.getKey(),
							isActivate ? Terminal.DEFINE_INACTIVE
									: Terminal.DEFINE_ACTIVE);
			for (Terminal terminal : terminalList) {
				terminal.setActive(status);
				tDAO.save(terminal);

				terminal.getPbxuser().setActive(status);
				puDAO.save(terminal.getPbxuser());

				terminal.getPbxuser().getUser().setActive(status);
				userDAO.save(terminal.getPbxuser().getUser());
			}
			break;
		case Address.classID:
			List<Address> didList = addDAO.getDIDListByPbxuser(pu.getKey(),
					isActivate ? Terminal.DEFINE_INACTIVE
							: Terminal.DEFINE_ACTIVE);
			for (Address did : didList) {
				did.setActive(status);
				addDAO.save(did);
			}
			break;
		}
	}

	public void manageStatusForSomeRelatedToUser(String domainName,
			String address, String username, boolean isActivate, Integer classID)
			throws DAOException, ValidateObjectException,
			DeleteDependenceException, ValidationException {
		Domain domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Pbxuser pu = null;

		if (isActivate)
			pu = puDAO.getPbxuserByUsernameAndDomain(username, domainName);
		else
			pu = puDAO.getActiveOrInactivePbxuserByUsernameAndDomain(username,
					domainName);

		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		Integer status = isActivate ? Terminal.DEFINE_ACTIVE
				: Terminal.DEFINE_INACTIVE;
		switch (classID) {
		case Address.classID:
			Address did = addDAO.getDIDAddress(address,
					isActivate ? Address.DEFINE_DELETED
							: Address.DEFINE_ACTIVE);
			if (did == null) {
				did = addDAO.getDIDAddress(address,
						isActivate ? Address.DEFINE_ACTIVE
								: Address.DEFINE_DELETED);
				if (did == null)
					throw new ValidationException(
							"DID is invalid or doesn't exist",
							WebServiceConstantValues.RESULT_CODE_INVALID_DID);
				throw new ValidationException(
						"DID is already "
								+ (isActivate ? "activated" : "desactivated"),
						WebServiceConstantValues.RESULT_CODE_OBJECT_ALREADY_ACTIVATED_OR_DESACTIVATED);
			}
			did.setActive(status);
			addDAO.save(did);
			break;
		case Terminal.classID:
			Terminal terminal = getTerminalByTypeName(address, domainName,
					isActivate ? Terminal.DEFINE_INACTIVE
							: Terminal.DEFINE_ACTIVE);
			if (terminal == null) {
				terminal = getTerminalByTypeName(address, domainName,
						isActivate ? Terminal.DEFINE_ACTIVE
								: Terminal.DEFINE_INACTIVE);
				if (terminal == null)
					throw new ValidationException(
							"Terminal is invalid or doesn't exist",
							WebServiceConstantValues.RESULT_CODE_INVALID_TERMINAL);
				throw new ValidationException(
						"Terminal is already "
								+ (isActivate ? "activated" : "desactivated"),
						WebServiceConstantValues.RESULT_CODE_OBJECT_ALREADY_ACTIVATED_OR_DESACTIVATED);
			}
			Pbxuserterminal pt = pbxuserTerminalDAO
					.getPbxuserterminalByPbxuserKeyAndTerminalPbxuserKey(pu
							.getKey(), terminal.getPbxuserKey());
			if (pt == null)
				throw new ValidationException(
						"Terminal not associated to user",
						WebServiceConstantValues.RESULT_CODE_TERMINAL_NOT_ASSOCIATED_TO_USER);

			terminal.setActive(status);
			tDAO.save(terminal);

			terminal.getPbxuser().setActive(status);
			puDAO.save(terminal.getPbxuser());

			terminal.getPbxuser().getUser().setActive(status);
			userDAO.save(terminal.getPbxuser().getUser());

			pbxuserManager.deleteSessionAndDependences(terminal.getPbxuser(), false);
			break;
		}
	}

	public void releaseUserTerminal(String domainName, String terminalName,
			String username) throws DAOException, ValidationException {
		Domain domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Pbxuser pu = puDAO.getActiveOrInactivePbxuserByUsernameAndDomain(
				username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		List<Pbxuserterminal> ptList = new ArrayList<Pbxuserterminal>();
		if (terminalName != null && terminalName.length() > 0) {
			Terminal terminal = getTerminalByTypeName(terminalName, domainName);
			if (terminal == null)
				throw new ValidationException(
						"Terminal is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_TERMINAL);

			Pbxuserterminal pt = pbxuserTerminalDAO
					.getPbxuserterminalByPbxuserKeyAndTerminalPbxuserKey(pu
							.getKey(), terminal.getPbxuserKey());
			if (pt == null)
				throw new ValidationException(
						"Terminal not associated to user",
						WebServiceConstantValues.RESULT_CODE_TERMINAL_NOT_ASSOCIATED_TO_USER);

			ptList.add(pt);
		} else
			ptList = pbxuserTerminalDAO
					.getPbxuserterminalByPbxuser(pu.getKey());

		for (Pbxuserterminal pt : ptList)
			pbxuserTerminalDAO.remove(pt);
	}

	public void excludeUserDID(String domainName, String didNumber,
			String username) throws DAOException, ValidateObjectException,
			DeleteDependenceException, ValidationException {
		Domain domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		// Este método tenta retornar um Pbxuser ativado, se esse não existir
		// ele procura por um desativado
		Pbxuser pu = puDAO.getActiveOrInactivePbxuserByUsernameAndDomain(
				username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		List<Address> didList = new ArrayList<Address>();
		if (didNumber != null && didNumber.length() > 0) {
			Address did = addDAO.getDIDAddress(didNumber);
			if (did == null)
				throw new ValidationException(
						"DID is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_DID);
			didList.add(did);
		} else
			didList = addDAO.getDIDListByPbxuser(pu.getKey());

		List<Long> didKeyList = new ArrayList<Long>();
		for (Address did : didList)
			didKeyList.add(did.getKey());

		addressManager.deleteDID(didKeyList);
	}

	public void verifySipSession(String domainName, String address,
			Integer classID) throws DAOException, ValidationException {
		Domain domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Pbxuser pbxuser = null;
		switch (classID) {
		case Pbxuser.classID:
			pbxuser = puDAO.getPbxuserByUsernameAndDomain(address, domainName);
			if (pbxuser == null)
				throw new ValidationException(
						"User is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_USER);

			if (!hasSipSession(pbxuser.getKey()))
				throw new ValidationException(
						"Terminal or User not logged",
						WebServiceConstantValues.RESULT_CODE_TERMINAL_OR_USER_NOT_LOGGED);
			break;

		case Terminal.classID:
			Terminal terminal = getTerminalByTypeName(address, domainName);
			if (terminal == null)
				throw new ValidationException(
						"Terminal is invalid or doesn't exist",
						WebServiceConstantValues.RESULT_CODE_INVALID_TERMINAL);
			pbxuser = terminal.getPbxuser();

			if (!hasSipSession(pbxuser.getKey()))
				throw new ValidationException(
						"Terminal or User not logged",
						WebServiceConstantValues.RESULT_CODE_TERMINAL_OR_USER_NOT_LOGGED);
			break;
		}
	}

	public List<TerminalInfo> getUserTerminalList(String domainName,
			String username) throws DAOException, ValidationException {
		Domain domain = domainDAO.getDomain(domainName);
		if (domain == null)
			throw new ValidationException("Domain is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_DOMAIN);

		Pbxuser pu = puDAO.getPbxuserByUsernameAndDomain(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		List<TerminalInfo> terminalInfoList = new ArrayList<TerminalInfo>();
		List<Terminal> terminalList = tDAO
				.getTerminalListAssociatedWithPbxuser(pu.getKey());
		for (Terminal t : terminalList) {
			TerminalInfo info = terminalManager
					.getTerminalInfoByKey(t.getKey());
			terminalInfoList.add(info);
		}
		return terminalInfoList;
	}

	/*
	 * ##########################################################################
	 * ## ### INICIO DOS METODOS QUE SERVEM DE AUXILIO AOS PRINCIPAIS DESTA
	 * CLASSE ###
	 * ###############################################################
	 * #############
	 */

	private boolean hasSipSession(Long pbxuserKey) throws DAOException {
		List<Sipsessionlog> sipSessionList = sipSessionlogDAO
				.getActiveSipsessionlogListByPbxuser(pbxuserKey);
		if (sipSessionList == null || sipSessionList.size() == 0)
			return false;
		return true;
	}

	private List<SimpleContactInfo> removeDuplicateEntries(
			List<SimpleContactInfo> currentList, List<SimpleContactInfo> tmpList) {
		List<SimpleContactInfo> noDuplicateList = new ArrayList<SimpleContactInfo>();
		for (int i = 0; i < tmpList.size(); i++) {
			boolean isAllowAdd = true;
			for (int j = 0; j < currentList.size(); j++)
				if (tmpList.get(i).getName() != null
						&& currentList.get(j).getName() != null
						&& tmpList.get(i).getType().equals(
								currentList.get(j).getType())
						&& tmpList.get(i).getName().equals(
								currentList.get(j).getName()))
					if (tmpList.get(i).getPhoneList().get(0).getPhone()
							.equals(
									currentList.get(j).getPhoneList().get(0)
											.getPhone())) {
						isAllowAdd = false;
						break;
					}
			if (isAllowAdd)
				noDuplicateList.add(tmpList.get(i));
		}
		return noDuplicateList;
	}

	private boolean verifyDIDnumber(List<Duo<Long, String>> didList,
			String didNumber) {
		for (Duo<Long, String> didDuo : didList)
			if (didDuo.getSecond().equals(didNumber))
				return true;
		return false;
	}

	private UsergroupInfo createUsergroupInfo(Long pbxuserKey,
			Integer priority, boolean isAdminUser, String username,
			boolean isLogged) {
		UsergroupInfo usergroupInfo = new UsergroupInfo();

		usergroupInfo.setPbxuserKey(pbxuserKey);
		usergroupInfo.setPriority(priority);
		usergroupInfo.setAdmin(isAdminUser);
		usergroupInfo.setUsername(username);
		usergroupInfo.setLogged(isLogged);
		return usergroupInfo;
	}

	private void addExtensionToUser(String username, String domainName,
			String extension) throws Exception {
		verifyNewExtension(extension, domainName);
		Pbxuser pu = getPbxuser(username, domainName);
		if (pu == null)
			throw new ValidationException("User is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_USER);

		PbxuserInfo puInfo = pbxuserManager.getPbxuserInfoByKey(pu.getKey(),
				false);
		puInfo.getExtensionList().add(createExtensionAddress(extension));

		pbxuserManager.save(puInfo);
	}

	private void addExtensionToGroup(String groupName, String domainName,
			String extension) throws Exception {
		verifyNewExtension(extension, domainName);
		Group group = gDAO.getGroupByAddressAndDomain(groupName, domainName);
		if (group == null)
			throw new ValidationException("Group is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_GROUP);

		GroupInfo groupInfo = groupManager.getGroupInfoByKey(group.getKey());
		groupInfo.getExtensionList().add(createExtensionAddress(extension));

		groupManager.save(groupInfo);
	}

	private void addExtensionToTerminal(String terminalName, String extension,
			String domainName) throws DAOException, ValidationException,
			ValidateObjectException {
		verifyNewExtension(extension, domainName);
		Terminal terminal = getTerminalByTypeName(terminalName, domainName);
		if (terminal == null)
			throw new ValidationException(
					"Terminal is invalid or doesn't exist",
					WebServiceConstantValues.RESULT_CODE_INVALID_TERMINAL);

		TerminalInfo tInfo = terminalManager.getTerminalInfoByKey(terminal
				.getKey());
		tInfo.setExtension(extension);

		terminalManager.save(tInfo);
	}

	private void verifyNewExtension(String extension, String domainName)
			throws DAOException, ValidationException {
		Address add = addDAO.getAddress(extension, domainName);
		if (add != null)
			throw new ValidationException(
					"Cannot add this Extension because it's already exist!!!",
					WebServiceConstantValues.RESULT_CODE_EXTENSION_ALREADY_EXIST_ON_SYSTEM);
	}

	private Address createExtensionAddress(String extension) {
		Address add = new Address();
		add.setAddress(extension);
		return add;
	}

	private UserForwardsInfo setUserForwardConfigurations(Long pbxuserKey,
			UserForwardsInfo info, Integer forwardType) throws DAOException,
			ValidationException {
		UserForwardsInfo databaseInfo = pbxuserManager.getForwardSettings(
				pbxuserKey, true);
		switch (forwardType) {
		case Forward.BUSY_MODE:
			databaseInfo.setForwardBusyEnable(info.getForwardBusyEnable());
			databaseInfo.setForwardBusyTarget(info.getForwardBusyTarget());
			databaseInfo
					.setForwardBusyTargetKey(info.getForwardBusyTargetKey());
			if (databaseInfo.getForwardBusyEnable()
					&& (databaseInfo.getForwardBusyTarget() == null && databaseInfo
							.getForwardBusyTargetKey() == null))
				throw new ValidationException(
						"Cannot active Forward because !!!",
						CallStateEvent.NOT_FOUND);
			break;
		case Forward.NOANSWER_MODE:
			databaseInfo.setForwardNoAnswerEnable(info
					.getForwardNoAnswerEnable());
			databaseInfo.setForwardNoAnswerTarget(info
					.getForwardNoAnswerTarget());
			databaseInfo.setForwardNoAnswerTargetKey(info
					.getForwardNoAnswerTargetKey());
			if (databaseInfo.getForwardNoAnswerEnable()
					&& (databaseInfo.getForwardNoAnswerTarget() == null && databaseInfo
							.getForwardNoAnswerTargetKey() == null))
				throw new ValidationException(
						"Cannot active Forward because !!!",
						CallStateEvent.NOT_FOUND);
			break;
		case Forward.ALWAYS_MODE:
			databaseInfo.setForwardAlwaysEnable(info.getForwardAlwaysEnable());
			databaseInfo.setForwardAlwaysTarget(info.getForwardAlwaysTarget());
			databaseInfo.setForwardAlwaysTargetKey(info
					.getForwardAlwaysTargetKey());
			if (databaseInfo.getForwardAlwaysEnable()
					&& (databaseInfo.getForwardAlwaysTarget() == null && databaseInfo
							.getForwardAlwaysTargetKey() == null))
				throw new ValidationException(
						"Cannot active Forward because !!!",
						CallStateEvent.NOT_FOUND);
			break;
		case Forward.CALL_FAILURE_MODE:
			databaseInfo.setForwardCallFailureEnable(info
					.getForwardCallFailureEnable());
			databaseInfo.setForwardCallFailureTarget(info
					.getForwardCallFailureTarget());
			databaseInfo.setForwardCallFailureTargetKey(info
					.getForwardCallFailureTargetKey());
			if (databaseInfo.getForwardCallFailureEnable()
					&& (databaseInfo.getForwardCallFailureTarget() == null && databaseInfo
							.getForwardCallFailureTargetKey() == null))
				throw new ValidationException(
						"Cannot active Forward because !!!",
						CallStateEvent.NOT_FOUND);
			break;
		default:
			break;
		}
		return databaseInfo;
	}

	private void createPbxuserTerminal(TerminalInfo terminalInfo,
			String associationUsername, String domainName) throws DAOException,
			ValidationException {
		Pbxuser pu = getPbxuser(associationUsername, domainName);
		List<Pbxuserterminal> ptList = new ArrayList<Pbxuserterminal>();

		Pbxuserterminal pt = new Pbxuserterminal();
		pt.setPbxuserKey(pu.getKey());
		ptList.add(pt);

		terminalInfo.setPbxuserListInTerminal(ptList);
	}

	private void createTerminalInformations(TerminalInfo terminalInfo,
			String domainName) {
		TerminalInfo terminalInfoTmp = terminalManager
				.createTerminalInfo(terminalInfo.getMACAddress());
		terminalInfo.setUsername(terminalInfoTmp.getUsername());
		terminalInfo.setPassword(Crypt.encrypt(terminalInfoTmp.getUsername(),
				domainName, terminalInfoTmp.getPassword()));
	}

	private Domain getDomainByName(String domainName) throws DAOException {
		Domain domain = null;
		if (domainName != null && domainName.length() > 0)
			domain = pbxManager.getDomain(domainName);
		else
			domain = pbxManager.getRootDomain();
		return domain;
	}

	private void resetTrunkLine(Long pbxKey) throws DAOException,
			ValidateObjectException {
		Address add = addDAO.getDefaultAddress(pbxKey);
		if (add != null) {
			add.setPbxKey(null);
			addDAO.save(add);
		}
	}

	private List<SimpleContactInfo> makeSimpleContactInfoForContact(
			List<Contact> contactList,
			List<SimpleContactInfo> simpleContactInfoList,
			Integer parameterOrder) throws DAOException {
		List<SimpleContactInfo> tmpList = new ArrayList<SimpleContactInfo>();
		for (Contact contact : contactList) {
			List<PhoneInfo> phonesList = new ArrayList<PhoneInfo>();
			List<Contactphones> contactPhonesList = contactPhonesDAO
					.getContactphonesListByContact(contact.getKey());
			for (Contactphones contactPhones : contactPhonesList)
				phonesList.add(new PhoneInfo(contactPhones.getType(),
						contactPhones.getPhone(), contactPhones.getPrefix()));
			tmpList.add(new SimpleContactInfo(contact, phonesList,
					parameterOrder));
		}
		return removeDuplicateEntries(simpleContactInfoList, tmpList);
	}

	private List<SimpleContactInfo> makeSimpleContactForIVR(List<IVR> ivrList,
			List<SimpleContactInfo> simpleContactInfoList,
			Integer parameterOrder) throws DAOException {
		List<SimpleContactInfo> tmpList = new ArrayList<SimpleContactInfo>();
		for (IVR ivr : ivrList) {
			ivr.setAddressList(addDAO.getAddressFullListByPbxuser(ivr
					.getPbxuserKey()));
			List<PhoneInfo> phones = new ArrayList<PhoneInfo>();
			for (Address address : ivr.getAddressList())
				phones.add(new PhoneInfo(address.getType(), address
						.getAddress()));
			tmpList.add(new SimpleContactInfo(ivr, phones, parameterOrder));
		}
		return removeDuplicateEntries(simpleContactInfoList, tmpList);
	}

	private List<SimpleContactInfo> makeSimpleContactInfoForGroup(
			List<Group> groupList,
			List<SimpleContactInfo> simpleContactInfoList,
			Integer parameterOrder) throws DAOException {
		List<SimpleContactInfo> tmpList = new ArrayList<SimpleContactInfo>();
		Presence groupPresence = new Presence();
		groupPresence.setState(Presence.STATE_ONLINE);
		for (Group g : groupList) {
			g.setAddressList(addDAO.getAddressFullListByGroup(g.getKey()));
			List<PhoneInfo> phones = new ArrayList<PhoneInfo>();
			for (Address address : g.getAddressList())
				phones.add(new PhoneInfo(address.getType(), address
						.getAddress()));
			tmpList.add(new SimpleContactInfo(g, groupPresence, phones,
					parameterOrder));
		}
		return removeDuplicateEntries(simpleContactInfoList, tmpList);
	}

	private List<PhoneInfo> makePhoneList(List<Address> extensionList,
			List<Duo<Long, String>> didList) {
		List<PhoneInfo> phoneList = new ArrayList<PhoneInfo>();
		for (Address add : extensionList)
			phoneList.add(new PhoneInfo(add.getType(), add.getAddress()));
		for (Duo<Long, String> list : didList)
			phoneList.add(new PhoneInfo(Address.TYPE_DID, list.getSecond()));
		return phoneList;
	}

	private Pbxuser getPbxuser(String username, String domainName)
			throws DAOException, ValidationException {
		return puDAO.getPbxuserByUsernameAndDomain(username, domainName);
	}

	private Long getFileinfoKey(Pbxuser pbxuser, FileInfo fileInfo,
			Long callLogKey) throws DAOException {
		Long fileKey = null;
		if (callLogKey != null) {
			Calllog callLog = cLogDAO.getByKey(callLogKey);
			if (callLog != null) {
				Groupfile gf = groupFileDAO.getGroupfileByCalllog(callLog
						.getKey());
				if (gf != null)
					fileKey = gf.getFileinfoKey();
				else
					fileKey = userFileDAO
							.getUserFileByCallLog(callLog.getKey())
							.getFileinfoKey();
			}
		} else if (fileInfo.getKey() != null)
			fileKey = fileInfo.getKey();
		else if (fileInfo.getName() != null) {
			Fileinfo file = fileInfoDAO.getFileinfoByNameAndAssociatedUser(
					fileInfo.getName(), pbxuser.getUserKey(), pbxuser.getUser()
							.getDomainKey());
			if (file != null)
				fileKey = file.getKey();
		}
		return fileKey;
	}

	private String makeLike(String parameter) {
		return "*" + parameter + "*";
	}

	private String makeFinalLike(String parameter) {
		return parameter + "*";
	}

	private Terminal getTerminalByTypeName(String terminalName,
			String domainName, Integer status) throws DAOException {
		return getTerminal(terminalName, domainName, status);
	}

	private Terminal getTerminalByTypeName(String terminalName,
			String domainName) throws DAOException {
		return getTerminal(terminalName, domainName, Terminal.DEFINE_ACTIVE);
	}

	private Terminal getTerminal(String terminalName, String domainName,
			Integer status) throws DAOException {
		Terminal terminal = status != null ? tDAO
				.getTerminalByMacAddressAndDomain(terminalName, domainName,
						status) : tDAO.getTerminalByMacAddressAndDomain(
				terminalName, domainName);
		if (terminal == null)
			terminal = status != null ? tDAO.getTerminalByAddressAndDomain(
					terminalName, domainName, status) : tDAO
					.getTerminalByAddressAndDomain(terminalName, domainName);
		return terminal;
	}

	// inicio tveiga basix store
	public void allowFunctionCTIToDomain(String domainName, boolean enable) throws DAOException, ValidationException, ValidateObjectException 
	{   
		Pbxpreference pbxPreference = getPbxpreferenceByDomain(domainName);
		
		if(enable)
			 pbxPreference.setCtiIntegrationEnable(Pbxpreference.CTIINTEGRATION_ENABLE_ON);
		else
			 pbxPreference.setCtiIntegrationEnable(Pbxpreference.CTIINTEGRATION_ENABLE_OFF); 
		
		 preferenceDAO.save(pbxPreference);
	}

	public void allowFunctionForwardToGroup(String domainName, String groupName, boolean enable) throws DAOException, ValidateObjectException
	{
		Group group = gDAO.getGroupByGroupNameAndDomain(groupName, domainName);
		   
	       if(group == null)
			   throw new DAOException("group "+ groupName + "not found to at " + domainName);
		   
	       Config config = group.getConfig();
		
		if(config == null)
			throw new DAOException("configuration not found to "+ groupName + " at " + domainName);
         
		if(enable)
		  config.setAllowedGroupForward(Config.ALLOWED_GROUPFORWARD);
		else
		  config.setAllowedGroupForward(Config.NOT_ALLOWED_GROUPFORWARD);
		
		configDAO.save(config);
	}

	public void allowFunctionIVRToDomain(String domainName, boolean enable) throws DAOException, ValidationException, ValidateObjectException 
	{
		Pbxpreference pbxPreference = getPbxpreferenceByDomain(domainName);
		 
		if(enable)
			 pbxPreference.setAllowedIVR(Pbxpreference.ALLOWED_IVR_ON);
		else
			 pbxPreference.setAllowedIVR(Pbxpreference.ALLOWED_IVR_OFF); 
		
		 preferenceDAO.save(pbxPreference);
	}

	public void allowFunctionKoushikubunToUser(String domainName, String userName, boolean enable) throws DAOException, ValidationException, ValidateObjectException 
	{
        Config config = getConfigByDomainNameAndUserName(domainName, userName);
		
		if(enable)
			config.setAllowedKoushiKubun(Config.ALLOWED_KOUSHIKUBUN); 
		else
			config.setAllowedKoushiKubun(Config.NOT_ALLOWED_KOUSHIKUBUN); 
		
		configDAO.save(config);
	}

	public void allowFunctionVoiceMailToGroup(String domainName, String groupName, boolean enable) throws DAOException, ValidateObjectException 
	{
       Group group = gDAO.getGroupByGroupNameAndDomain(groupName, domainName);
	   
       if(group == null)
		   throw new DAOException("group "+ groupName + " not found to at " + domainName);
	   
       Config config = group.getConfig();
	   
       if(config == null)
			throw new DAOException("configuration not found to "+ groupName + " at " + domainName);

		if(enable)
			config.setAllowedVoiceMail(Config.ALLOWED_VOICEMAIL);
		else
		{
			disableForward(config.getKey());
			config.setAllowedVoiceMail(Config.NOT_ALLOWED_VOICEMAIL);	
		}
		configDAO.save(config);
	}

	public void allowFunctionVoiceMailToUser(String domainName,	String userName, boolean enable) throws DAOException, ValidationException, ValidateObjectException 
	{
		Config config = getConfigByDomainNameAndUserName(domainName, userName);
		
		if(enable)
		  config.setAllowedVoiceMail(Config.ALLOWED_VOICEMAIL);
		else
		{
		    disableForward(config.getKey());
		    config.setAllowedVoiceMail(Config.NOT_ALLOWED_VOICEMAIL);
		}
		configDAO.save(config);
	}
	
	private void disableForward(Long configKey) throws DAOException, ValidateObjectException
	{
		//List<Forward> forwardList = forwardDAO.getForwardListByConfig(configKey);	
		
		Config config = configDAO.getConfigWithForwardListByConfigkey(configKey); 
		List<Forward> forwardList = config.getForwardList();
	
		for(Forward forward : forwardList)
		{
			if(forward.getStatus() == Forward.STATUS_ON && forward.getAddressKey() != null)
			{
				Pbxuser pbxUser = puDAO.getPbxuserByAddressKey(forward.getAddressKey());
				
				if(pbxUser.getUser().getAgentUser() == User.TYPE_VOICEMAIL)
				{
					forward.setAddressKey(null);
					forward.setStatus(Forward.STATUS_OFF);
					forwardDAO.save(forward);
				}
			}
		}
	}
	
   private Pbxpreference getPbxpreferenceByDomain(String domainName) throws DAOException, ValidationException, ValidateObjectException
   {
		
		Pbx pbx = pbxDAO.getPbxByDomain(domainName);

		if(pbx == null)
			throw new DAOException("pbx not found at " + domainName);

		Pbxpreference pbxPreference = pbx.getPbxPreferences();
		
		if(pbxPreference == null)
			throw new DAOException("preference not found to " + pbx.getKey() + " at " + domainName);
        
		return pbxPreference;
		
	}
   
   private Config getConfigByDomainNameAndUserName(String domainName, String userName) throws DAOException, ValidationException, ValidateObjectException
	{
		Config config = null; 
		Pbxuser pbxUser = getPbxuser(userName, domainName);
       
		if(pbxUser == null)
			throw new DAOException("pbxuser not found at " + domainName);
			
		config = pbxUser.getConfig();
		
		if(config == null)
			throw new DAOException("configuration not found to "+ userName + " at " + domainName);

		return config;
	 }	
// fim tveiga basix store
   
   public void updateDialPlan(DialPlan dPlan, String domain) throws DAOException, ValidateObjectException, ValidationException
   {
	   int type = dPlan.getType();
	   if(type > DialPlan.TYPE_DEFAULTOPERATOR || type < DialPlan.TYPE_USERSPEEDDIAL)
		   throw new ValidationException(	"Dial Plan type doesn't exisit!!",	WebServiceConstantValues.RESULT_CODE_DIALPLAN_INVALID_TYPE);

	   PBXDialPlanInfo info = dPlanManager.getPBXDialPlanByDomain(domain);
	   info.setNewDialPlan(dPlan);
	   
	   Pbx pbx = pbxManager.getPbxByDomain(domain);
	   info.setPbx(pbx);
	   
	   dPlanManager.updatePBXDialPlan(info, null);
   }
   
   public void updatePBXDialPlan(PBXDialPlanInfo info, String domain)  throws DAOException, ValidateObjectException
   {
	   Pbx pbx = pbxManager.getPbxByDomain(domain);
	   info.setPbx(pbx);
	   dPlanManager.updatePBXDialPlan(info, null);
   }
   
   public List<String> getGroupRingTargets(String address, String domain) throws DAOException
   {
	   return gDAO.getGroupRingTargets(address, domain);
   }
   
   public List<String> getGroupHuntTargets(String address, String domain, int algorithmType) throws DAOException
   {
	   return gDAO.getGroupHuntTargets(address, domain, algorithmType);
   }

   public List<String> getGroupACDTargets(String address, String domain, int algorithmType) throws DAOException
   {
	   return gDAO.getGroupACDTargets(address, domain, algorithmType);
   }
   
   public LinkedList<Usergroup> getGroupACDCallCenterTargets(String address, String domain, int algorithmType) throws DAOException
   {
	   return gDAO.getGroupACDCallCenterTargets(address, domain, algorithmType);
   }
   
   public Group getGroupByAddressAndDomain(String address, String domain) throws DAOException
   {
	   return gDAO.getGroupByAddressAndDomain(address, domain);
   }
   
   public void changeDIDStatus(String did, int status) throws DAOException, ValidateObjectException, DeleteDependenceException
   {
	   Address add = addressManager.getDID(did);
	   addressManager.changeDIDStatus(add.getKey(), status);
   }
   
}