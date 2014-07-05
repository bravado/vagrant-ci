package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.InvalidOwnerException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.LoopDetected;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.SameFromAndToException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.SelectedANINotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.UserNotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.UserWithoutSipSessionlogException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.DefaultUserNotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.DestinationCallBlockedException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.DestinationFilterForwardException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.DestinationInDNDException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.ForwardAlwaysException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.InvalidDestinationUser;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.InvalidForwardException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.MaxConcurrentCallsException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.MaxForwardsException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.NightModeTargetNotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.OriginationCallBlockedException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.OriginationFilterForwardException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.PermissionDeniedException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.ActivecallDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CostCenterDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.DialPlanDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.FilterDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GatewayDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ParkDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserterminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PhoneDialPlanDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.RouteruleDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ServiceclassDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipTrunkAddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipTrunkDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipTrunkRouteruleDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipsessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SpeeddialDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.UserspeeddialDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.ContactDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.SessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.CallInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Leg;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Target;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ForwardMode;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.RouteType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CostCenter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Filter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Gateway;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuserterminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Presence;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Routerule;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.SipTrunk;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.SipTrunkRouterule;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Speeddial;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Userspeeddial;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contact;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.PhoneDialPlanResponse;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ActivecallInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PBXDialPlanInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.TraceCallInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.call.localesettings.CallLocaleSettings;
import br.com.voicetechnology.ng.ipx.rule.call.localesettings.CallLocaleSettings.DisplaySettings;
import br.com.voicetechnology.ng.ipx.rule.call.localesettings.CallLocaleSettingsFactory;
import br.com.voicetechnology.ng.ipx.rule.call.validationcall.ClickToMessageValidator;
import br.com.voicetechnology.ng.ipx.rule.call.validationcall.RouteInfoValidator;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class CallManager extends Manager
{
	protected PbxDAO pbxDAO;
	protected SpeeddialDAO sdDAO;
	protected UserspeeddialDAO usdDAO;
	protected AddressDAO addDAO;
	protected GroupDAO gDAO;
	protected PbxuserDAO puDAO;
	protected UserDAO uDAO;
	protected DomainDAO dmDAO;
	protected ForwardDAO fDAO;
	protected FilterDAO filterDAO;
	protected RouteruleDAO rrDAO;
	protected GatewayDAO gatDAO;
	protected ActivecallDAO acDAO;
	protected TerminalDAO tDAO;
	protected PbxuserterminalDAO ptDAO;
	protected ContactDAO contactDAO;
	protected ConfigDAO confDAO;
	protected ServiceclassDAO scDAO;
	protected SessionlogDAO slDAO;
	protected SipsessionlogDAO sslDAO;
	protected AddressManager addManager;
	protected ParkDAO parkDAO;
	protected DialPlanDAO dPlanDAO;
	protected PbxpreferenceDAO pbxPreferenceDAO;
	protected SipTrunkDAO siptrunkDAO;
	protected DialPlanManager dPlanManager;
	protected PhoneDialPlanDAO phoneDialPlanDAO;
	protected PhoneDialPlanManager phoneDialPlanManager;
	protected CostCenterDAO costCenterDAO;
	private SipTrunkRouteruleDAO siptrunkRouteRuleDAO;
	private SipTrunkAddressDAO siptrunkAddressDAO;

	public final String PSTN_DIGITS = "\\d{6,}";
	
	private final int FROM_DISPLAY = 0;
	private final int MY_DISPLAY = 1;

	public CallManager(String loggerPath) throws DAOException
	{
		super(loggerPath);

		sdDAO = dao.getDAO(SpeeddialDAO.class);
		usdDAO = dao.getDAO(UserspeeddialDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		gDAO = dao.getDAO(GroupDAO.class);
		puDAO = dao.getDAO(PbxuserDAO.class);
		dmDAO = dao.getDAO(DomainDAO.class);
		fDAO = dao.getDAO(ForwardDAO.class);
		filterDAO = dao.getDAO(FilterDAO.class);
		rrDAO = dao.getDAO(RouteruleDAO.class);
		gatDAO = dao.getDAO(GatewayDAO.class);
		acDAO = dao.getDAO(ActivecallDAO.class);
		tDAO = dao.getDAO(TerminalDAO.class);
		ptDAO = dao.getDAO(PbxuserterminalDAO.class);
		contactDAO = dao.getDAO(ContactDAO.class);
		uDAO = dao.getDAO(UserDAO.class);
		confDAO = dao.getDAO(ConfigDAO.class);
		scDAO = dao.getDAO(ServiceclassDAO.class);
		slDAO = dao.getDAO(SessionlogDAO.class);
		sslDAO = dao.getDAO(SipsessionlogDAO.class);
		parkDAO = dao.getDAO(ParkDAO.class);
		dPlanDAO = dao.getDAO(DialPlanDAO.class);
		pbxPreferenceDAO = dao.getDAO(PbxpreferenceDAO.class);
		siptrunkDAO = dao.getDAO(SipTrunkDAO.class);
		phoneDialPlanDAO = dao.getDAO(PhoneDialPlanDAO.class);
		costCenterDAO = dao.getDAO(CostCenterDAO.class);
		addManager = new AddressManager(loggerPath);
		dPlanManager = new DialPlanManager(loggerPath);
		phoneDialPlanManager = new PhoneDialPlanManager(logger);
		siptrunkRouteRuleDAO = dao.getDAO(SipTrunkRouteruleDAO.class); 
		siptrunkAddressDAO = dao.getDAO(SipTrunkAddressDAO.class);
	}

	// getCallInfo
	public CallInfo getCallInfo(SipAddressParser sipFrom, SipAddressParser sipToOriginal, SipAddressParser requestUser, String domain, String display) throws Exception
	{
		/**
		 * Detecta se foi originado pelo callcenter baseado no IP que originou a chamada, 
		 * que esta no fromDomain, antes de ser alterado no metodo makeCorrectFromAndToInfo.
		 */
		boolean isFromCallCenter = sipFrom.getDomain().equals(IPXProperties.getProperty(IPXPropertiesType.MEDIA_APP_IP));
		String ivrCallID = makeCorrectFromAndToInfo(sipFrom, sipToOriginal, domain);
		String locale = pbxDAO.getLocaleByDomain(domain);
		CallLocaleSettings settings = CallLocaleSettingsFactory.getCallLocaleSettings(locale);
		CallInfo callInfo = settings.getCallInfo(sipFrom, sipToOriginal, requestUser, domain, display);
		callInfo.setIsFromCallCenter(isFromCallCenter);
		
		callInfo.setIvrCallID(ivrCallID);
		
		return callInfo;
	}

	private String makeCorrectFromAndToInfo(SipAddressParser sipFrom, SipAddressParser sipToOriginal, String domain) throws InvalidDestinationUser
	{
        makeCorrectSipFrom(sipFrom, domain);	//this method is invoked to set correct domain in mediaApplication calls.
        String ivrCallID = makeCorrectSipTo(sipToOriginal);		//this method is invoked to remove callID from IVR agents.
        String fromExtension = sipFrom.getExtension();
        String toExtension = sipToOriginal.getExtension();
        int addressLength = Integer.valueOf(IPXProperties.getProperty(IPXPropertiesType.CENTREX_ADDRESS_LENGTH));
    	String matcher = new StringBuilder("\\d{").append(addressLength).append(",}").toString();
    	if(fromExtension.matches(matcher) && toExtension.matches(matcher) && fromExtension.equals(toExtension))
    		throw new InvalidDestinationUser("Origination and destination users are the same.", CallStateEvent.PERMISSION_DENIED);
    	
    	return ivrCallID;
	}
	
	private void makeCorrectSipFrom(SipAddressParser sipFrom, String domain)
	{
		// TODO alteracao feita para suportar chamada feita pelo MS
		String mediaIP = IPXProperties.getProperty(IPXPropertiesType.MEDIA_APP_IP);
		if(mediaIP != null)
		{
			String[] tmp = mediaIP.split(",");
			for(String ip: tmp)
				if(sipFrom.getDomain().equals(ip.trim()))
					sipFrom.setDomain(domain);
		}
	}

	private String makeCorrectSipTo(SipAddressParser sipToOriginal)
	{
		// Remove CALLID## da chamada feita pelo IVR, utilizado para referenciar qual chamada esta sendo transferida.
        String[] extension = sipToOriginal.getExtension().split("##");
        if(extension.length == 2)
        {
        	sipToOriginal.setExtension(extension[1]);
        	if(extension[0] != null && extension[0].length() == 0)
        		return null;
        	return extension[0];
        }
        
        return null;
	}
	
	public SipAddressParser isSpeeddial(SipAddressParser from, SipAddressParser to, String domain, boolean isFromTerminal) throws DAOException
	{
		String extension = to.getExtension();
		String destination = null;
		
		PBXDialPlanInfo pbxDialPlan = dPlanManager.getPBXDialPlanByDomain(domain);
		DialPlan speedDialDialPlan = pbxDialPlan.getSpeedDialDialPlan();
		DialPlan publicSpeedDialDialPlan = pbxDialPlan.getPublicSpeedDialDialPlan();
		
		if(extension.startsWith("*")) // if starts with * its a command.
			destination = to.getExtension();
		else if(isNumber(extension) && extension.length() <= DialPlan.MAX_DIGITS)
		{
			if(Integer.valueOf(extension) >= publicSpeedDialDialPlan.getStart() && Integer.valueOf(extension) <= publicSpeedDialDialPlan.getEnd())
			{
				Speeddial sd = sdDAO.getSpeeddial(extension, domain);
				destination = sd != null ? sd.getDestination() : null;
			}
			else	if(Integer.valueOf(extension) >= speedDialDialPlan.getStart() && Integer.valueOf(extension) <= speedDialDialPlan.getEnd())
			{
				String fromExtension = from.getExtension();
				if(isFromTerminal)
				{
					Pbxuser pbxUser = puDAO.getPbxuserByTerminal(fromExtension, domain);
					if(pbxUser != null)
						fromExtension  = pbxUser.getUser().getUsername();
				}
				Userspeeddial usd = usdDAO.getUserspeeddial(fromExtension, extension, domain);
				destination = usd != null ? usd.getDestination() : null;
			}
		}
		if(destination != null)
			to = new SipAddressParser(destination, domain);
		return to;
	}
	
	
	
	protected boolean isCommandConfig(SipAddressParser address) throws DAOException
	{			
		return address.getExtension().matches("\\*\\d{2}[\\.|\\-|\\w]{0,30}");
	}

	protected boolean isNumber(String string)
    {
    	return string.matches("(\\d[- \\.]?)+");
    }
	
	public RouteInfo getRouteInfo(CallInfo callInfo, ValidationModes validationModes) throws Exception
	{
		boolean isEnabledLoopDetection = Boolean.valueOf(IPXProperties.getProperty(IPXPropertiesType.PBX_LOOP_DETECTION));
		return getRouteInfo(callInfo, isEnabledLoopDetection, validationModes);		
	}
	
	// getRouteInfo																	// Esse valor deve ser encapsulado na CallInfo
	public RouteInfo getRouteInfo(CallInfo callInfo,  boolean isEnableLoopDetection, ValidationModes validationModes) throws Exception
	{
		Pbx pbx = getPbxFull(callInfo.getDomain());
		Long domainKey = pbx.getUser().getDomainKey();
		Address originationAddress = getAddress(callInfo.getFrom().getExtension(), domainKey, callInfo.isFromTerminal());
		
		//dnakamashi - bug #5861 - version 3.0.6
		Leg fromLeg = makeLeg(pbx, originationAddress, callInfo.getFrom().clone(), callInfo.getOriginalFrom(), callInfo.getRequestUser(), true, callInfo.isFromTerminal(), false, isEnableLoopDetection, false);
		
		Leg toLeg = null;
		if(callInfo.isParkPosition())
			toLeg = makeParkLeg(fromLeg, callInfo, pbx, domainKey);
		else if(callInfo.isSpyCommand())
			toLeg = makeSpyLeg(callInfo);
		else
			toLeg = callInfo.isCommandConfig() ? makeCommandConfigLeg(callInfo.getTo()) : makeToLeg(fromLeg, callInfo, pbx, domainKey , isEnableLoopDetection, validationModes);
		
		RouteInfo routeInfo = new RouteInfo(callInfo, fromLeg, toLeg);
		
		routeInfo = verifyGroupNightMode(routeInfo, false);		
		
		routeInfo.setAccountID(pbx.getAccountId());
		
		boolean isPbxVMEnable = isPBXVoicemailEnable(pbx);
		routeInfo.setPbxVmEnable(isPbxVMEnable);
		
		return routeInfo;
	}
	
	//inicio --> dnakamashi - bug #4323 - version 3.0.6
	private boolean isSelectANIFunc(SipAddressParser address)
	{
		if(address.getExtension().matches("\\*\\d{1,}\\*\\d{6,}"))
			return true;
		return false;
	}
	
	//Seta o display escolhido e seta o To correto na CallInfo 
	private void prepareForSelectANIFunc(Pbx pbx, Leg fromLeg, CallInfo callInfo) throws Exception
	{
		String address = callInfo.getTo().getExtension().substring(1);
		String didAddress = address;
		String didNumber = address ;
		int didIndex = 0;
		
		didAddress = didAddress.substring(address.indexOf('*') + 1, address.length());
		callInfo.getTo().setExtension(didAddress);
		
		
		didNumber = didNumber.substring(0, didNumber.indexOf('*'));
		didIndex = Integer.parseInt(didNumber);		
		
		String domain = fromLeg.getUser().getDomain().getDomain();
		String outgoingAddress = getOutgoingAddress(domain, fromLeg);		
		if(didIndex == 1)
		{			
			fromLeg.setDisplay(outgoingAddress);
			fromLeg.getSipAddress().setExtension(outgoingAddress);
		}			
		else	if(didIndex == 2) //ANONYMOUS
		{		
			fromLeg.setDisplay(Pbxuser.ANONYMOUS);
			fromLeg.getSipAddress().setExtension(outgoingAddress);
		}
		else
		{				
			selectANI(pbx, fromLeg, didIndex);
		}		
	}
	
	
	//Ordem dos DIDs: userDidList, ivrDidList, trunkLine, groupDidList
	private void selectANI(Pbx pbx, Leg fromLeg, int didIndex) throws Exception
	{
		//jfarah - 3.1.0 - Adicionada a lista de dids de grupo cujo usuario esta associado.
		List<Address> didList = new ArrayList<Address>();
		didList.addAll(addDAO.getDIDListByPbxuser(fromLeg.getPbxuser().getKey())); // userDidList
		
		if(IsDIDInList(didIndex, didList))
		{
			Address did = (Address) didList.get(didIndex - 3);
			fromLeg.setDisplay(did.getAddress());
			fromLeg.getSipAddress().setExtension(did.getAddress());
		}
		else
		{
			/*
			 * Caso o did escolhido não seja os DIDs do usuário, então o trunk line, os DIDs dos IVRs
			 * e os DIDs relacionados com os Grupos dos quais o usuario eh membro do domínio são carregados
			 */
			didList.addAll(addDAO.getIvrDIDListByPbxuser(fromLeg.getPbxuser().getKey())); // IvrDidList
			didList.add(addDAO.getDefaultAddress(pbx.getKey())); // trunkLine			
			didList.addAll(getGroupDIDListByPbxuser(fromLeg.getPbxuser().getKey())); // groupDidList			 
			
			//resgata od dids relacionados aos usuarios Tronco do domínio
			List<Pbxuser> sipTrunkList = puDAO.getSipTrunkUsers(fromLeg.getUser().getDomainKey());
			for(Pbxuser sipTrunk: sipTrunkList)
			{
				List<Address> sipTrunkAddressList = addDAO.getDIDListByPbxuser(sipTrunk.getKey());
				if(sipTrunkList.size() > 0)
					didList.addAll(sipTrunkAddressList);
			}
			
			//resgata od dids relacionados aos grupos Tronco do domínio
			List<Group> groupTrunkList = gDAO.getGroupsInPBXByGroupType(pbx.getKey(), Group.SIPTRUNK_GROUP);
			for(Group trunkGroup: groupTrunkList)
			{
				List<Address> didEachGroupList = addDAO.getDIDListByGroup(trunkGroup.getKey());
				if(didEachGroupList.size() > 0)
					didList.addAll(didEachGroupList);
			}
			
			if(IsDIDInList(didIndex, didList))
			{
				Address did = (Address) didList.get(didIndex - 3);
				fromLeg.setDisplay(did.getAddress());
				fromLeg.getSipAddress().setExtension(did.getAddress());
			}
			else
			{
				StringBuilder sb = new StringBuilder("The DID on the position : ");
				sb.append(didIndex);
				sb.append(" does'n exist!!");
				throw new SelectedANINotFoundException(sb.toString());
			}
		}
	}
	
	/**
	 * Return the DID address list of all Groups which the user <code>pbxuserKey</code> is related of.
	 * @param pbxuserKey
	 * @return
	 * @throws DAOException
	 */
	private List<Address> getGroupDIDListByPbxuser(Long pbxuserKey) throws DAOException
	{
		List<Address> addressList = new ArrayList<Address>();
		List<Group> groupList = gDAO.getGroupListByPbxuser(pbxuserKey);
		for(Group group: groupList)
		{
			addressList.addAll(addDAO.getDIDListByGroup(group.getKey()));
		}
		
		return addressList;		
	}
	
	private boolean IsDIDInList(int index, List<Address> didList)
	{
		return (index - 2) <= didList.size() && (index - 3) >= 0 ? true : false; 
	}
	//fim --> dnakamashi - bug #4323 - version 3.0.6

	private Leg makeToLeg(Leg fromLeg, CallInfo callInfo, Pbx pbx, Long domainKey, boolean isEnableLoopDetection, ValidationModes validationModes) throws Exception
	{	
		//inicio --> dnakamashi - bug #4323 - version 3.0.6
		boolean isDisplayReady = false;
		if(isSelectANIFunc(callInfo.getTo()))
		{
			prepareForSelectANIFunc(pbx, fromLeg, callInfo);
			isDisplayReady = true;
		}
		//fim --> dnakamashi - bug #4323 - version 3.0.6
		
		processCostCenter(callInfo, fromLeg, validationModes);
		
		//inicio --> dnakamashi - PhonedialPlan - version 3.2.8 - 16/Jun/2011
		if(fromLeg.getRouteType() == RouteType.STATION)
		{
			String newToExtension = getPhoneDialPlanResult(fromLeg.getPbxuser(), callInfo.getTo().getExtension(), callInfo.getDomain());
			callInfo.getTo().setExtension(newToExtension);
		}
		//fim --> dnakamashi - PhonedialPlan - version 3.2.8 - 16/Jun/2011
		
		Leg toLeg = null;
		
		boolean isInSameDomain = false;
		if(pbx.getFailureForward() != null && (fromLeg.getRouteType().equals(RouteType.PSTN) || fromLeg.getRouteType().equals(RouteType.ON_NET)))
			toLeg = makePBXFailureLeg(pbx, callInfo.getTo(), callInfo.getRequestUser());
		else
		{		
			boolean isIVRPreProcessCall = isToIVRPreProcessCall(callInfo, fromLeg, validationModes);
			
			Address address = getAddress(callInfo.getTo().getExtension(), domainKey, callInfo.isToTerminal());
			if(address != null && fromLeg.getUser() != null && !fromLeg.getUser().getAgentUser().equals(User.TYPE_PBX))
				isInSameDomain = address.getDomainKey().intValue() == fromLeg.getUser().getDomainKey().intValue();			
			
			toLeg = makeLeg(pbx, address, callInfo.getTo(), callInfo.getOriginalTo(), callInfo.getRequestUser(), false, callInfo.isToTerminal(), isInSameDomain, isEnableLoopDetection, isIVRPreProcessCall);
		}
		
		//dnakamashi - bug #4323 - version 3.0.6
		if(!isDisplayReady)
			makeDisplay(pbx, callInfo, fromLeg, toLeg);
		
		return toLeg;
	}	
	
	private String getPhoneDialPlanResult(Pbxuser pbxuser, String address, String domain) throws DAOException
	{			
		if(pbxuser != null)
		{
			List<PhoneDialPlan> dialPlans = phoneDialPlanManager.getPhoneDialPlansByPbxuser(pbxuser.getKey());
			for(PhoneDialPlan dialPlan : dialPlans)
			{
				PhoneDialPlanResponse resp = dialPlan.exec(address);
				if(resp.isReady())
					return resp.getResult();
			}
		}
		
		PhoneDialPlanResponse resp = phoneDialPlanManager.executePhoneDialPlan(address, domain);
		return resp.getResult();		
	}
	
	private Leg makeParkLeg(Leg fromLeg, CallInfo callInfo, Pbx pbx, Long domainKey) throws Exception
	{
		Leg toLeg = null;
		User park = uDAO.getParkServerByDomain(callInfo.getDomain());
		if(park != null)
			toLeg = makeLeg(getAddress(park.getUsername(), domainKey, callInfo.isToTerminal()), callInfo.getTo(), callInfo.getOriginalTo(), RouteType.STATION, false, false);
		else
			throw new UserNotFoundException(new StringBuilder("Destination address: ").append(callInfo.getOriginalTo()).append(" not found").toString(), false);
		makeDisplay(pbx, callInfo, fromLeg, toLeg);
		return toLeg;
	}
	
	private boolean isStation(Leg leg)
	{
		return leg.isPbxuser() || leg.isGroup() || leg.isTerminal();
	}
	
	private boolean isExternalCall(Leg leg)
	{
		return leg.getRouteType().equals(RouteType.PSTN) || leg.getRouteType().equals(RouteType.ON_NET);
	}

	private Leg makeLeg(Pbx pbx, Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddres, SipAddressParser requestAddress, boolean isFrom, boolean isTerminal, boolean isInSameDomain, boolean isEnableLoopDetection, boolean isIVRPreProcessCall) throws Exception
	{
		Leg leg = null;
		
		if(isIVRPreProcessCall)
			leg = makeIVRPreProcessCallLeg(sipAddress, originalSipAddres, isFrom, isTerminal);
		else if(address == null)
			if(sipAddress.getExtension().length() == 0)
				throw new UserNotFoundException(new StringBuilder("Destination address: ").append(sipAddress.getAddress()).append(" not found").toString(), isFrom);
			else if(isOperator(pbx, sipAddress, isFrom))
				leg =  makeOperatorLeg(pbx, sipAddress, originalSipAddres, isFrom, isTerminal);
			else if(isPSTN(sipAddress, requestAddress))
				leg = makePSTNLeg(address, sipAddress, requestAddress, isFrom, pbx.getDomainKey());
			else if(isCommandConfig(sipAddress))
				leg = makeCommandConfigLeg(sipAddress);
			else
				throw new UserNotFoundException(new StringBuilder("Destination address: ").append(sipAddress.getAddress()).append(" not found").toString(), isFrom);
		else if(isDID(address))
			if(isSameDomain(pbx, address))
			{
				if(!isFrom || !isEnableLoopDetection)
					leg = makeStationLeg(pbx, address, sipAddress, originalSipAddres, isFrom, isTerminal, isInSameDomain);
				else
					throw new LoopDetected("The from address can not be a DID, probably this is a loop configured in PSTN Gateway!");
			} else
				leg = makeOnNetLeg(address, sipAddress, originalSipAddres, isFrom);
		else
			leg = makeStationLeg(pbx, address, sipAddress, originalSipAddres, isFrom, isTerminal, false);
		return leg;
	}

	private boolean isToIVRPreProcessCall(CallInfo callInfo, Leg fromLeg, ValidationModes validationModes) throws DAOException 
	{
		SipAddressParser sipAddress = callInfo.getTo();		
				
		 if(isStationToPSTN(callInfo, fromLeg, validationModes) || isStationToVoiceMail(fromLeg, sipAddress))
		 {				
			Pbxuser pu = fromLeg.getPbxuser();
			if(pu == null)
				return false;
			Config config = pu.getConfig();
			Pbxpreference preference = pbxPreferenceDAO.getByDomainKey(pu.getUser().getDomainKey());		
			
			if(preference.getEletronicLockEnable() == Pbxpreference.ELETRONICLOCK_ENABLE_ON && config.isEletronicLockOn())		
				return true;
		 }
		
		return false;
	}
	
	private void processCostCenter(CallInfo callInfo, Leg fromLeg, ValidationModes validationModes) throws Exception
	{
		 if(!isStationToPSTN(callInfo, fromLeg, validationModes))
			 return;

		Pbxuser pu = fromLeg.getPbxuser();		
		Config config = pu.getConfig();
		Pbxpreference preference = pbxPreferenceDAO.getByDomainKey(pu.getUser().getDomainKey());

		if(preference.isCostCenterEnable() && config.isCostCenterOn())
		{
			List<CostCenter> ccs = costCenterDAO.getByDomain(pu.getUser().getDomainKey());
			
			if(ccs.isEmpty())
				return;
			
			int costCenterDigits = preference.getCostCenterCodeDigits();
			String address = callInfo.getTo().getExtension();
			String costCenterCode = address.substring(0, costCenterDigits);

			CostCenter cc = costCenterDAO.getByDomainAndCode(pu.getUser().getDomain().getDomain(), costCenterCode);

			if(cc == null)
				throw new PermissionDeniedException("Invalid CostCenter", false, CallStateEvent.CALL_REJECTED);

			callInfo.setCostCenter(cc);
			callInfo.getTo().setExtension(address.substring(costCenterDigits));
		}		
	}	
	
	private boolean isStationToPSTN(CallInfo callInfo, Leg fromLeg, ValidationModes validationModes) throws DAOException
	{
		SipAddressParser sipAddress = callInfo.getTo();
		SipAddressParser requestAddress = callInfo.getRequestUser();

		if(fromLeg.getPbxuser() == null)
			return false;
		
		 if(isToExtension(sipAddress, requestAddress) || fromLeg.getRouteType() != RouteType.STATION || validationModes != ValidationModes.FULL_MODE)
			 return false;
		 
		 return true;
	}
	
	private boolean isStationToVoiceMail(Leg fromLeg, SipAddressParser sipAddress) throws DAOException
	{		
		if(fromLeg.getPbxuser() == null)
			return false;
		
		if((fromLeg.getRouteType() == RouteType.STATION) && isToVoicemail(sipAddress.getExtension(), sipAddress.getDomain()))
			return true;
		return false;
	}
	
	private boolean isToExtension(SipAddressParser sipAddress, SipAddressParser requestAddress) throws DAOException
	{
		if(!isPSTN(sipAddress, requestAddress))
			 return true;
		return false;
	}
	public Pbxuser getPbxuserOrTerminalByAddressAndDomain(String address, String domain) throws DAOException
	{
		Pbxuser pu = puDAO.getPbxuserWithConfigByAddressAndDomain(address, domain, null);
		if(pu == null)
			return null;
		
		if(pu.isTerminal())
		{
			Pbxuser associatedPbxuser = puDAO.getPbxuserWithConfigByTerminal(address, domain);			
			pu = associatedPbxuser != null ? associatedPbxuser : pu;
		}
		
		return pu;
	}

	private boolean isOperator(Pbx pbx, SipAddressParser sipAddress, boolean isFrom)
	{
		return sipAddress.getExtension().equals(pbx.getOperator());
	}
	
	private boolean isPSTN(SipAddressParser sipAddress, SipAddressParser requestAddress) throws DAOException
	{
		return sipAddress.getExtension().matches(PSTN_DIGITS) || 
		getPSTNUser(requestAddress) != null || 
		getPSTNUser(sipAddress) != null;
	}
	
	private String getIMGOutgoingCallPrefix()
	{
		return IPXProperties.getProperty(IPXPropertiesType.IMG_OUTGOING_CALL_PREFIX);
	}
	
	private boolean isAutoCompleteAreaCodeEnable()
	{
		return Boolean.parseBoolean(IPXProperties.getProperty(IPXPropertiesType.AUTOCOMPLETE_AREACODE));
	}
	
	private boolean isSameDomain(Pbx pbx, Address address)
	{
		return address.getDomainKey().longValue() == pbx.getUser().getDomainKey().longValue();
	}

	private boolean isNightModeOn(Pbx pbx)
	{
		return pbx.getNightMode().intValue() == Pbx.NIGHTMODE_ON;
	}

	private boolean isNightModeOn(Group group)
	{
		return group.getNightmodeStatus().intValue() == Group.NIGHTMODE_ON;
	}
	private boolean isToDefaultOperator(Pbx pbx, SipAddressParser sipAddress, Address address)
	{
		return sipAddress.getExtension().equals(pbx.getOperator()) || address.getPbxKey() != null || (address.getPbxuserKey() == null && address.getGroupKey() == null);
	}

	private Leg makePBXFailureLeg(Pbx pbx, SipAddressParser sipTo, SipAddressParser requestUser) throws Exception
	{
		sipTo.setExtension(pbx.getFailureForward());
		return makePSTNLeg(null, sipTo, requestUser, false, pbx.getDomainKey());
	}
	private Leg makeIVRPreProcessCallLeg(SipAddressParser sipAddress, SipAddressParser originalSipAddres, boolean isFrom, boolean isTerminal) throws DAOException, NightModeTargetNotFoundException, DefaultUserNotFoundException, MaxForwardsException{
		Address address = new Address();
		address.setDomain(dmDAO.getDomain(sipAddress.getDomain()));
		address.setAddress(User.IVR_PREPROCESSCALL);		
		address.setType(Address.TYPE_EXTENSION);
		Pbxuser pu = getPbxuserByAddressAndDomain(User.IVR_PREPROCESSCALL, address.getDomain().getDomain());
		address.setPbxuser(pu);
		Leg leg = makeStationLeg(address, sipAddress.clone(), originalSipAddres, isFrom, isTerminal, true);		
		return leg;
	}
	private Leg makeOperatorLeg(Pbx pbx, SipAddressParser sipAddress, SipAddressParser originalSipAddres, boolean isFrom, boolean isTerminal) throws DAOException, NightModeTargetNotFoundException, DefaultUserNotFoundException, MaxForwardsException
	{
		Address address = addDAO.getByKey(pbx.getDefaultaddressKey());
		address.setDomain(dmDAO.getByKey(address.getDomainKey()));
		return makeStationLeg(pbx, address, sipAddress, originalSipAddres, isFrom, isTerminal, false);
	}
	
	private Leg makePSTNLeg(Address address, SipAddressParser sipAddress, SipAddressParser requestUser, boolean isFrom, Long domainKey) throws Exception
    {
        User gwUser = null;
        SipTrunk siptrunk = null;
        if (!isFrom)
        {
            siptrunk = getSipTrunk(sipAddress.getExtension(), domainKey);
            if(siptrunk != null)
                gwUser = siptrunk.getPbxuser().getUser();
            else
                gwUser = getGatewayUser(sipAddress.getExtension(), domainKey);
            
        } else if(requestUser != null)
            gwUser = getPSTNUser(requestUser);    // PSTN Incoming, pega o user atrav�s do User e Domain contido no ProxyAuthorization Header.

        // No caso de click to call a perna from eh PSTN mas nao foi gerada por 
        // um invite sip, portanto requestuser = null, entao nao e possivel resgatar 
        // o gw atraves do requestuser, entao resgata o gw pela regra de roteamento
        //
        // Se requestUser nao eh nulo mas nao foi possivel encontrar o gateway user
        // a partir dele, tenta pela extension
        //
        if (gwUser == null)
                gwUser = getGatewayUser(sipAddress.getExtension(), domainKey);
                
        if(gwUser == null)
            throw new UserNotFoundException("Gateway not Found!!!", isFrom);
        
        sipAddress.setDomain(gwUser.getDomain().getDomain());
        Leg leg = makeLeg(gwUser, address, sipAddress, requestUser, RouteType.PSTN, isFrom, false);
        if(siptrunk != null)
        	leg.setSiptrunk(siptrunk);
        
        return leg;
    }


    private SipTrunk getSipTrunk(String number, Long domainKey) throws Exception
    {        
        List<SipTrunkRouterule> siptrunkRouteList = siptrunkRouteRuleDAO.getSipTrunkRouteruleOrderByPriority(domainKey);
        
        SipTrunk siptrunk = null;
        for(int i = 0; i < siptrunkRouteList.size() && siptrunk == null; i++)
        {
            SipTrunkRouterule rr = siptrunkRouteList.get(i);
            if(number.matches(rr.getRegex()))
            {
                siptrunk = rr.getSipTrunk();
                siptrunk.setAddressList(siptrunkAddressDAO.getSipTrunkAddressDidListIn(siptrunk.getKey()));
            }
        }
        
        return siptrunk;
    }

	private Leg makeCommandConfigLeg(SipAddressParser sipAddress) throws DAOException, NightModeTargetNotFoundException
	{
		return makeLeg(sipAddress, sipAddress.clone(), RouteType.COMMAND_CONFIG, false, false);
	}
	
	private Leg makeUnknowLeg(Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddres, boolean isFrom) throws DAOException, NightModeTargetNotFoundException, MaxForwardsException
	{
		address = addDAO.getAddress(sipAddress.getExtension(), sipAddress.getDomain());
		if(address != null)
			if(address.getPbxuserKey() != null)
				return makeLeg(address, sipAddress, originalSipAddres, RouteType.UNDEFINED, isFrom, false);
			else if(address.getGroupKey() != null)
				return makeGroupLeg(address, sipAddress, originalSipAddres, isFrom, false);
		return makeLeg(null, address, sipAddress, originalSipAddres, RouteType.UNDEFINED, isFrom, false);
	}

	private Leg makeOnNetLeg(Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddres ,boolean isFrom) throws DAOException, NightModeTargetNotFoundException
	{
		sipAddress.setDomain(address.getDomain().getDomain());
		Pbx pbxOnnet = pbxDAO.getPbxByDomain(address.getDomain().getKey());
		User userPBX = uDAO.getByKey(pbxOnnet.getUserKey());
		userPBX.setDomain(address.getDomain());
		Leg leg = makeLeg(userPBX, address, sipAddress, originalSipAddres, RouteType.ON_NET, isFrom, false);
		leg.setAllowedVideoCall(true);
		return leg;
	}

	private Leg makeStationLeg(Pbx pbx, Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddres, boolean isFrom, boolean isTerminal, boolean isInSameDomain) throws DAOException, NightModeTargetNotFoundException, DefaultUserNotFoundException, MaxForwardsException
	{
		sipAddress.setDomain(address.getDomain().getDomain());
		if(!isFrom && address != null && address.getGroupKey() != null)
			return makeGroupLeg(address, sipAddress, originalSipAddres, isFrom, false);
		else if(!isFrom && isNightModeOn(pbx) && isDID(address) && !isInSameDomain)
			return makeNightModeLeg(pbx, sipAddress, originalSipAddres, isTerminal);
		else if(!isFrom && isToDefaultOperator(pbx, sipAddress, address))
			return makeDefaultLeg(pbx, sipAddress, originalSipAddres, isTerminal);
		else
		{
			Leg leg = makeStationLeg(address, sipAddress, originalSipAddres, isFrom, isTerminal, false);
			//dsakuma: configurar video on off
			
			if (leg.getPbxuser() != null)
			{
				Integer isAllowedVideo = leg.getPbxuser().getConfig().getAllowedVideoCall();
				Integer isPbxAllowedVideo = pbx.getPbxPreferences().getVideoCallEnable();

				if (isAllowedVideo == Config.ALLOWED_VIDEOCALL && isPbxAllowedVideo == Pbxpreference.VIDEOCALL_ENABLE_ON)
					leg.setAllowedVideoCall(true);
			}
			//dsakuma: fim configurar video on off
			return leg; 
		}
	}

	private Leg makeNightModeLeg(Pbx pbx, SipAddressParser sipAddress, SipAddressParser originalSipAddres, boolean isTerminal) throws NightModeTargetNotFoundException, DAOException, MaxForwardsException
	{
		logger.debug("PBX in NightMode...");
		Address nightAddress = pbx.getNightmodeaddress();
		if(pbx.getNightmodeaddressKey() == null)
			throw new NightModeTargetNotFoundException("Night mode user not defined!");
		Leg leg = makeStationLeg(nightAddress, sipAddress, originalSipAddres, false, isTerminal, false);
		leg.setNightmodeLeg(true); //bug 7202
		return leg;
	}

	private Leg makeDefaultLeg(Pbx pbx, SipAddressParser sipAddress, SipAddressParser originalSipAddres, boolean isTerminal) throws DefaultUserNotFoundException, DAOException, NightModeTargetNotFoundException, MaxForwardsException
	{
		Address defaultAddress = pbx.getDefaultaddress();
		if(pbx.getDefaultaddressKey() == null)
			throw new DefaultUserNotFoundException("Default user not defined!");

		return makeStationLeg(defaultAddress, sipAddress, originalSipAddres, false, isTerminal, false);
	}

	private Leg makeStationLeg(Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddress, boolean isFrom, boolean isTerminal, boolean ignoreNigthMode) throws DAOException, NightModeTargetNotFoundException, MaxForwardsException
	{
		if(address.getDomain() == null)
			address.setDomain(dmDAO.getByKey(address.getDomainKey()));
		sipAddress.setDomain(address.getDomain().getDomain());
		if(isDID(address) && address.getPbxKey() == null)
			address = address.getPbxuserKey() != null ? addDAO.getSipIDByPbxuser(address.getPbxuserKey()) : addDAO.getSipIDByGroup(address.getGroupKey());
			
		sipAddress.setExtension(address.getAddress());
		
		Leg leg = null;
		if(address.getGroupKey() != null)
			leg = makeGroupLeg(address, sipAddress, originalSipAddress, isFrom, ignoreNigthMode);
		else if(address.getPbxuserKey() != null)
		{			
			leg = makeLeg(address, sipAddress, originalSipAddress, RouteType.STATION, isFrom, isTerminal);			
			
			SipTrunk siptrunk = siptrunkDAO.getSipTrunksByPbxuserKey(address.getPbxuserKey());
			if(siptrunk != null)
			{
				siptrunk.setPbxuser(leg.getPbxuser());
				leg.getPbxuser().setAddressList(addDAO.getDIDListByPbxuser(leg.getPbxuser().getKey()));
				
				if(leg.getPbxuser().getDefaultDIDKey() != null)
					leg.getPbxuser().setDefaultDID(addDAO.getByKey(leg.getPbxuser().getDefaultDIDKey()));
				leg.setSiptrunk(siptrunk);
			}
		}
		else
			leg = makeUnknowLeg(address, sipAddress, originalSipAddress, isFrom);
		
		return leg;
	}
	
	private Leg makeStationLeg(Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddress, Pbxuser pu, boolean isFrom, boolean isTerminal, boolean ignoreNigthMode) throws DAOException, NightModeTargetNotFoundException, MaxForwardsException
	{
		if(address.getDomain() == null)
			address.setDomain(dmDAO.getByKey(address.getDomainKey()));
		sipAddress.setDomain(address.getDomain().getDomain());
		if(isDID(address) && address.getPbxKey() == null)
			address = address.getPbxuserKey() != null ? addDAO.getSipIDByPbxuser(address.getPbxuserKey()) : addDAO.getSipIDByGroup(address.getGroupKey());
			
		sipAddress.setExtension(address.getAddress());
		
		Leg leg = null;
		
		leg = makeLeg(address, sipAddress, originalSipAddress, pu, RouteType.STATION, isFrom, isTerminal);		
		
		return leg;
	}

	private Leg makeLeg(SipAddressParser sipAddress, SipAddressParser originalSipAddress, RouteType routeType, boolean isFrom, boolean isTerminal) throws DAOException, NightModeTargetNotFoundException
	{
		return new Leg(sipAddress, originalSipAddress, routeType, isFrom);
	}
	
	/**
	 * Geracao de Leg SPY usada em chamadas de monitoracao de PAs em callcenter.
	 */
	private Leg makeSpyLeg(CallInfo callInfo) throws DAOException, NightModeTargetNotFoundException
	{
		User user = uDAO.getUserByUsernameAndDomain(User.ACDGROUP_NAME, callInfo.getDomain());
		return new Leg(user, callInfo.getTo(), callInfo.getOriginalTo(), RouteType.SPY_COMMAND, false);		
	}
	
	private Leg makeLeg(Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddress, Pbxuser pu, RouteType routeType, boolean isFrom, boolean isTerminal) throws DAOException, NightModeTargetNotFoundException
	{
		return new Leg(pu, sipAddress, originalSipAddress, routeType, isFrom, isTerminal);
	}
	
	private Leg makeLeg(Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddress, RouteType routeType, boolean isFrom, boolean isTerminal) throws DAOException, NightModeTargetNotFoundException
	{
		return new Leg(puDAO.getPbxuserWithConfigByKey(address.getPbxuserKey()), sipAddress, originalSipAddress, routeType, isFrom, isTerminal);
	}

	private Leg makeLeg(User user, Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddress, RouteType routeType, boolean isFrom, boolean isTerminal) throws DAOException, NightModeTargetNotFoundException
	{
		if(routeType.equals(RouteType.STATION) && address != null && address.getPbxuserKey() != null)
			return makeLeg(address, sipAddress, originalSipAddress, routeType, isFrom, isTerminal);
		else
			return new Leg(user, sipAddress, originalSipAddress, routeType, isFrom);
	}

	private Leg makeGroupLeg(Address address, SipAddressParser sipAddress, SipAddressParser originalSipAddress, boolean isFrom, boolean ignoreNigthMode) throws DAOException, NightModeTargetNotFoundException, MaxForwardsException
	{
		Group group = gDAO.getGroupFull(address.getGroupKey());
		List<Usergroup> userGroup = gDAO.getUsersInGroup(address.getGroupKey());
		group.setUsergroupList(userGroup);
		
		if(isNightModeOn(group) && !ignoreNigthMode)
		{
			if(group.getNightmodeaddressKey() != null)
			{
				Address nightAddress = addDAO.getByKey(group.getNightmodeaddressKey());
				if(nightAddress.getGroupKey() != null)
				{
					Group groupNighmodeDestination = gDAO.getGroupFull(nightAddress.getGroupKey());
					if(groupNighmodeDestination != null && isNightModeOn(groupNighmodeDestination) && groupNighmodeDestination.getNightmodeaddressKey() != null)
						throw new MaxForwardsException("Cannot complete this call because was exceeded max number redirections!!!", CallStateEvent.UNAVALIABLE);
					sipAddress.setExtension(nightAddress.getAddress());
					return new Leg(groupNighmodeDestination, sipAddress, originalSipAddress, RouteType.STATION, isFrom);
				} else
				{
					Pbxuser pbxuser = puDAO.getPbxuserAndUser(nightAddress.getPbxuserKey());
					// se o nightmode estiver habilitado para o VoiceMail, essa leg deve ser gerada com o tratamento tryForward
					// caso contrario a chamada para o VoiceMail não será tratada como uma chamda pra deixar recada mas sim
					// para acessar a a caixa de mensagens do usuario que efetuou a ligação.
					// TODO - Remover esse trecho quando o forwardAlways de grupo estiver implementado.
					if(pbxuser.getUser().getAgentUser().intValue() == User.TYPE_VOICEMAIL)
						return new Leg(group, sipAddress, originalSipAddress, RouteType.STATION, isFrom);
					else
					{
						sipAddress.setExtension(nightAddress.getAddress());
						return new Leg(pbxuser, sipAddress, originalSipAddress, RouteType.STATION, isFrom, false);
					}
				}
			} else
				throw new NightModeTargetNotFoundException("Group night mode user not defined!");
		} else
		{
			Leg groupLeg = new Leg(group, sipAddress, originalSipAddress, RouteType.STATION, isFrom);
			if(groupLeg.getGroup().getGroupType().equals(Group.RING_GROUP))
				groupLeg.setIsGroupRecordable(isGroupRecordableCall(groupLeg.getGroup()));
			return groupLeg;
		}
	}

	//jfarah - 3.1.0 GAMBI: gravacao de chamada de grupo.
	private boolean isGroupRecordableCall(Group ringGroup) throws DAOException
	{
		List<Usergroup> userGroupList = ringGroup.getUsergroupList();
		for(Usergroup userGroup: userGroupList)
		{
			Pbxuser pbxUser = userGroup.getPbxuser();
			Config config = confDAO.getByKey(pbxUser.getConfigKey());
			if(config.getAllowedRecordCall().equals(Config.ALLOWEDRECORDCALL_ON))
				return true;
		}
		
		return false;
	}
	
	
	// TODO - Remover esse metodo quando o forwardAlways de grupo for estiver implementado.
	private RouteInfo verifyGroupNightMode(RouteInfo route, boolean ignoreNigthMode) throws Exception
	{		
		if(route.getToLeg().getGroup() != null)
		{
			Group group = route.getToLeg().getGroup(); 
			
			if(isNightModeOn(group) && !ignoreNigthMode && group.getNightmodeaddressKey() != null)
			{
				Address nightAddress = addDAO.getByKey(group.getNightmodeaddressKey());
				
				if(nightAddress.getPbxuserKey() != null)
				{
					Pbxuser pbxuser = puDAO.getPbxuserAndUser(nightAddress.getPbxuserKey());
					if(pbxuser.getUser().getAgentUser().intValue() == User.TYPE_VOICEMAIL)
						return forwardToVoicemail(route);
				}
			}
		}
		
		return route;
	}

	//display
	private void makeDisplay(Pbx pbx, CallInfo callInfo, Leg fromLeg, Leg toLeg) throws DAOException
	{
		CallLocaleSettings settings = CallLocaleSettingsFactory.getCallLocaleSettings(pbx.getPbxPreferences().getLocale());
		DisplaySettings displaySettings = fromLeg.isStation() && toLeg.isStation() ? DisplaySettings.SHOW : settings.getToDisplaySettings(callInfo.getOriginalTo(), isHideAni(fromLeg, callInfo));
		
		//dnakamashi --> bug #5394 - version 3.0.5 RC6.6
		if( callInfo.getDisplay() != null &&
			((fromLeg.getRouteType().equals(RouteType.PSTN) && toLeg.getRouteType().equals(RouteType.PSTN)) ||
			(fromLeg.getRouteType().equals(RouteType.ON_NET ) && toLeg.getRouteType().equals(RouteType.STATION) && displaySettings .equals(DisplaySettings.SHOW))))
		{
			fromLeg.setDisplay(callInfo.getDisplay());
		} else
		{
			makeDisplay(pbx, fromLeg, toLeg, false, displaySettings);
			makeDisplay(pbx, toLeg, fromLeg, true, displaySettings);
		}
	}

	private boolean isHideAni(Leg fromLeg, CallInfo callInfo)
	{
		boolean anonymousDisplay = callInfo.getDisplay() != null && callInfo.getDisplay().equals(Pbxuser.ANONYMOUS);
		boolean anonymousConfig = fromLeg.isPbxuser() && fromLeg.getPbxuser().getIsAnonymous().intValue() == Pbxuser.ANONYMOUS_ON;
		return (anonymousDisplay || anonymousConfig) && !callInfo.isToVoiceMail();
	}
	
	protected void makeDisplay(Pbx pbx, Leg leg, Leg partnerLeg, boolean isLegTo, DisplaySettings displaySettings) throws DAOException
	{
		if(isLegTo)
			if(displaySettings.equals(DisplaySettings.HIDE))
				makeAnonymousDisplay(pbx, partnerLeg, leg);
			else if(isExternalCall(leg))			
				makeOutgoingDisplay(pbx, partnerLeg, leg);
			else
				makeDisplay(leg, partnerLeg, displaySettings);
		else
			makeDisplay(leg, partnerLeg, displaySettings);
	}
	
	private void makeDisplay(Leg fromLeg, Leg toLeg, DisplaySettings displaySettings) throws DAOException
	{
//		Address address = fromLeg.getRouteType() != RouteType.PSTN ? address = addDAO.getAddress(fromLeg.getSipAddress().getExtension(), fromLeg.getSipAddress().getDomain()) : null;
		if(fromLeg.isFrom())
			if(fromLeg.isPbxuser())
				if(fromLeg.getPbxuser().getCustomID() != null)
					fromLeg.setDisplay(fromLeg.getPbxuser().getCustomID());
				else
					fromLeg.setDisplay(makeDisplayA(fromLeg, toLeg, displaySettings));		
			else if(fromLeg.isPSTN() || fromLeg.getGroup() != null)
				fromLeg.setDisplay(makeDisplayA(fromLeg, toLeg, displaySettings));
	}

	private void makeAnonymousDisplay(Pbx pbx, Leg fromLeg, Leg toLeg) throws DAOException
	{
		fromLeg.setDisplay(Pbxuser.ANONYMOUS);
		if(isStation(toLeg))
			fromLeg.getSipAddress().setExtension(Pbxuser.ANONYMOUS);
		else if(isExternalCall(toLeg))
			makeOutgoingDisplay(pbx, fromLeg, toLeg);
	}
	
	private void makeOutgoingDisplay(Pbx pbx, Leg fromLeg, Leg toLeg) throws DAOException
	{
		String address = null;
		List<Address> didList = new ArrayList<Address>();
		if(fromLeg.getGroup() != null)
		{
			address = getOutgoingAddress(pbx.getAddress(), fromLeg.getGroup());
			didList = addDAO.getDIDListByGroup(fromLeg.getGroup().getKey());
			didList.add(pbx.getAddress());
		}
		else
		{
			address = getOutgoingAddress(pbx.getAddress(), fromLeg);
			if(fromLeg.isPbxuser())
			{
				didList = addDAO.getDIDListByPbxuser(fromLeg.getPbxuser().getKey());
				didList.add(pbx.getAddress());				
			}
			
		}
		
		address = getSuitableOutgoingAddress(didList, toLeg.getSipAddress().getAddress(), address, fromLeg);//dnakamashi - bug #6652 - version 3.0.5 RC 6.6
		
		// Se o destino for um tronco gateway, entao o display deve ser um dos dids associados ao mesmo e nao o DID de saida do usuario.
		if(toLeg.getSiptrunk() != null && toLeg.getRouteType().equals(RouteType.PSTN))
			address = getSipTrunkOutgoingAddress(toLeg, address); 
		
		fromLeg.getSipAddress().setExtension(address);
		if(!fromLeg.getDisplay().equals(Pbxuser.ANONYMOUS))
			fromLeg.setDisplay(address);
	}
	
	private String getSipTrunkOutgoingAddress(Leg toLeg, String currentDisplay) throws DAOException
	{
		String display = currentDisplay;
		
		if(toLeg.getSiptrunk() != null && toLeg.getSiptrunk().getAddressList() != null)
		{
			List<Address> adrList = toLeg.getSiptrunk().getAddressList();
			boolean exist = false;
			for (Address adr : adrList)
			{
				String add = adr.getAddress();
				if (add.equals(currentDisplay))
				{
					exist = true;
					break;
				}
			}
			
			if(!exist && toLeg.getSiptrunk().getPbxuser().getDefaultDIDKey() != null)
			{				
				Address tmp = toLeg.getSiptrunk().getPbxuser().getDefaultDID();
				if(tmp == null)
					tmp = addDAO.getByKey(toLeg.getSiptrunk().getPbxuser().getDefaultDIDKey());
				display = tmp != null ? tmp.getAddress():display;
			}
		}
		
		return display;
	}
	
	protected String getOutgoingAddress(String domain, Leg fromLeg) throws DAOException
	{
		Address pbxAdd = addDAO.getDefaultAddressByDomain(domain);
		return getOutgoingAddress(pbxAdd, fromLeg);
	}
		
	public String getOutgoingAddress(Long domainKey, Leg fromLeg) throws DAOException
	{
		Address pbxAdd = addDAO.getDefaultAddressByDomainKey(domainKey);
		return getOutgoingAddress(pbxAdd, fromLeg);
	}
	
	private String getOutgoingAddress(Address pbxAddress, Group group) throws DAOException
	{
		Address outAddress = getOutgoingAddress(pbxAddress, addDAO.getDIDListByGroup(group.getKey()));

		return outAddress != null ? outAddress.getAddress() : "";
	}
	
	private String getOutgoingAddress(Address pbxAddress, Leg fromLeg) throws DAOException
	{
		Address outAddress = null;
		Pbxuser pu = fromLeg.getPbxuser();
		if(pu == null)
//			outAddress = pbx.getAddress(); // Getting trunk line.
			return fromLeg.getOriginalSipAddress().getExtension();
		else if(pu.getDefaultDIDKey() != null)
			outAddress = addDAO.getByKey(pu.getDefaultDIDKey()); // Get user default DID.
		else
			outAddress = getOutgoingAddress(pbxAddress, addDAO.getDIDListByPbxuser(pu.getKey()));
	
		return outAddress != null ? outAddress.getAddress() : "";
	}
	
	private Address getOutgoingAddress(Address pbxAddress, List<Address> addressList)
	{
		if(addressList.size() > 0)
			return addressList.get(0); // Getting any DID associated  with user.
		
		return pbxAddress; // Getting trunk line.
	}
	
	private String makeDisplayA(Leg fromLeg, Leg toLeg, DisplaySettings displaySettings) throws DAOException
	{
		String display = null;
		String ani = fromLeg != null ? fromLeg.getSipAddress().getExtension() : null;

		if(fromLeg != null || (ani != null && ani.length() > 0))
		{
			if(fromLeg.getRouteType().equals(RouteType.STATION))
			{				
				if(fromLeg.isGroup())
				{
					display = makeStringDisplay(fromLeg.getGroup().getName(), fromLeg.getSipAddress().getExtension());
				} else
				{
					if(fromLeg.isPbxuser() && displaySettings.equals(DisplaySettings.HIDE) && toLeg.getRouteType() != RouteType.STATION)
						display = Pbxuser.ANONYMOUS;
					else
					{
						String name = fromLeg.getPbxuser().getUser().getName() != null && fromLeg.getPbxuser().getUser().getName().length() > 0 ? fromLeg.getPbxuser().getUser().getName() : fromLeg.getPbxuser().getUser().getKanjiName(); 
						display = makeStringDisplay(name, fromLeg.getSipAddress().getExtension());
					}
				}
			} else if(fromLeg.getRouteType().equals(RouteType.ON_NET) || fromLeg.getRouteType().equals(RouteType.PSTN))
			{
				Contact contact = null;
				if(toLeg.isPbxuser())
				{
					String address = getPSTNNumber(fromLeg.getSipAddress().getExtension());
					contact = contactDAO.getContactByUserAndAddress(toLeg.getPbxuser().getUserKey(), address);
					if(contact == null)
						contact = contactDAO.getPublicContact(toLeg.getUser().getDomainKey(), address);
				}
				//inicio --> dnakamashi - bug #6832 - version 3.1
				else if(toLeg.isGroup())
				{
					String address = getPSTNNumber(fromLeg.getSipAddress().getExtension());
					contact = contactDAO.getPublicContactByPbxKey(toLeg.getGroup().getPbxKey(), address);
				}
				//fim --> dnakamashi - bug #6832 - version 3.1
				display = contact != null ? makeStringDisplay(contact.getName() != null && contact.getName().length() > 0 ? contact.getName() : contact.getKanjiName(), ani) : fromLeg.getSipAddress().getExtension();
			}
		} else
			display = Pbxuser.ANONYMOUS;

		return display;
	}

	public String getPSTNNumber(String number)
	{
		String length = IPXProperties.getProperty(IPXPropertiesType.CENTREX_ADDRESS_LENGTH);
		if(length == null)
			return number;
		try
		{
			int sizeNumber = Integer.valueOf(length);
			if(number.length() - sizeNumber >= 0)
				return number.substring(number.length() - sizeNumber, number.length());
		}catch(Exception e)
		{
			logger.error("Error when making display.", e);
		}
		return number;
	}
	
	private String makeStringDisplay(String... args)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < args.length - 1; i++)
			if(args[i] != null)
				sb.append(args[i] + " ");
		sb.append("(" + args[args.length - 1] + ")");
		return sb.toString();
	}
	
	//utils
	private Pbx getPbxFull(String domain) throws DAOException
	{
		Pbx pbx = pbxDAO.getPbxFullByDomain(domain);
		if(pbx.getNightmodeaddressKey() != null)
			pbx.setNightmodeaddress(addDAO.getByKey(pbx.getNightmodeaddressKey()));
		return pbx;
	}

	private Address getAddress(String address, Long domainKey, boolean isTerminal) throws DAOException
	{
		Address add;
//		String address = sipAddress.getExtension(); // getRealAddress(sipAddress.getExtension());
		if(isTerminal)
			add = getPbxuserSipIDInTerminal(address, domainKey);
		else
		{
			add = addDAO.getAddress(address, domainKey);
			if(add == null)
				add = addDAO.getDIDAddress(address);
		}
		return add;
	}

	private Address getPbxuserSipIDInTerminal(String address, Long domainKey) throws DAOException
	{
		Terminal t = tDAO.getTerminalByAddress(address, domainKey);
		List<Pbxuserterminal> ptList = ptDAO.getPbxuserterminalByTerminal(t.getKey());
		if(ptList.size() == 0)
			return addDAO.getAddress(address, domainKey);
		Address add = addDAO.getSipIDByPbxuser(ptList.get(0).getPbxuserKey());
		add.setDomain(dmDAO.getByKey(add.getDomainKey()));
		return add;
	}

	private boolean isDID(Address add)
	{
		return add != null && (add.getType().intValue() == Address.TYPE_DID);
	}

	// tryForward
	public RouteInfo tryForward(RouteInfo routeInfoOriginal, ForwardMode forwardMode) throws Exception
	{
		RouteInfo routeInfo = null;
		Leg toLeg = routeInfoOriginal.getToLeg();
		if(forwardMode.equals(ForwardMode.VOICEMAIL_COMMAND))
			routeInfo = forwardToVoicemailCommand(routeInfoOriginal);
		else if(forwardMode.equals(ForwardMode.VOICEMAIL))
			routeInfo = forwardToVoicemail(routeInfoOriginal);
		else if(forwardMode.equals(ForwardMode.MUSIC))
			routeInfo = forwardToMusic(routeInfoOriginal);
		else
			routeInfo = forwardByForwardMode(toLeg, routeInfoOriginal, forwardMode);
		return routeInfo;
	}

	private RouteInfo forwardToVoicemailCommand(RouteInfo routeInfoOriginal) throws Exception
	{
		SipAddressParser sipAddress = routeInfoOriginal.getToLeg().getSipAddress();
		SipAddressParser originalSipAddress = routeInfoOriginal.getCallInfo().getOriginalTo();
		Address add = addDAO.getAddress(sipAddress.getExtension(), routeInfoOriginal.getCallInfo().getDomain());
		Leg toLeg = null;
		if(routeInfoOriginal.getToLeg().getRouteType().equals(RouteType.COMMAND_CONFIG))
		{
			SipAddressParser address = routeInfoOriginal.getToLeg().getOriginalSipAddress();
			Pbxuser pu = puDAO.getPbxuserByAddressAndDomain(address.getExtension(), address.getDomain());
			if(pu != null && pu.getUser().getAgentUser().equals(User.TYPE_TERMINAL))
			{
				pu = tDAO.getAssociatedPbxuserByTerminalPbxuserKey(pu.getKey());
				if(pu != null)
					toLeg = makeStationLeg(add, sipAddress, originalSipAddress, pu, false, false, true);
				else
					toLeg = makeStationLeg(add, sipAddress, originalSipAddress, false, false, true);
			}else
				 toLeg = makeStationLeg(add, sipAddress, originalSipAddress, false, false, true);
		}else
		 toLeg = makeStationLeg(add, sipAddress, originalSipAddress, false, false, true);
		RouteInfo routeInfo = new RouteInfo(routeInfoOriginal.getCallInfo(), routeInfoOriginal.getFromLeg(), toLeg);
		new ClickToMessageValidator().validate(routeInfo.getFromLeg(), toLeg);
		return forwardToVoicemail(routeInfo);
	}
	
	private RouteInfo forwardToVoicemail(RouteInfo routeInfoOriginal) throws Exception
	{
		//inicio --> dnakamashi - Validação feita para o Basix Store - version 3.0.3
		 if(!routeInfoOriginal.getToLeg().isVoicemailAllowed())
			 throw new InvalidForwardException("Voicemail is off!", CallStateEvent.PERMISSION_DENIED, routeInfoOriginal);
		//fim --> dnakamashi - Validação feita para o Basix Store - version 3.0.3
		 
			RouteInfo baseRouteInfo = routeInfoOriginal.hasPreviousRouteInfo() ? routeInfoOriginal.getPreviousRouteInfo() : routeInfoOriginal;
			Pbx pbx = getPbxFull(baseRouteInfo.getCallInfo().getDomain());
			boolean isPbxVMEnable = isPBXVoicemailEnable(pbx);
			baseRouteInfo.setPbxVmEnable(isPbxVMEnable);
			if(!isVoicemailEnable(baseRouteInfo))
			{			
				int callState = getVoicemailCallState(routeInfoOriginal);
				throw new InvalidForwardException("Voicemail is off!", callState);
			}
			Address add = addDAO.getVoicemailInternalAddress(baseRouteInfo.getCallInfo().getDomain());
			return tryForward(add.getAddress(), baseRouteInfo, true);
		 
	}
	
	private int getVoicemailCallState(RouteInfo routeInfoVoicemail)
	{
		return getVoicemailCallState(routeInfoVoicemail.getToLeg());
	}
	
	private int getVoicemailCallState(Leg toLeg)
	{
		boolean haveSipSession = true;
		if(toLeg.getPbxuser() != null)
		{
			try {
				int numberSipSession = sslDAO.getHowManyActiveSipSessionByPbxuser(toLeg.getPbxuser().getKey());
				if(numberSipSession==0)
					haveSipSession = false;
			} catch (DAOException e) {
				return CallStateEvent.UNAVALIABLE;
			}
		}
		return haveSipSession ? CallStateEvent.CALL_UNANSWERED :  CallStateEvent.UNAVALIABLE;
	}

	private boolean isVoicemailEnable(RouteInfo routeinfo) throws InvalidForwardException
	{
		int disableVoicemail = Config.VOICEMAIL_OFF;
		Leg toLeg = routeinfo.getToLeg();
		
		if(routeinfo.isPbxVmEnable())
		{	
			if(toLeg.isVoicemail())
			{
				int callState = getVoicemailCallState(toLeg);
				throw new InvalidForwardException("Already tried to forward to voicemail!!!", callState);
			}
			
			if(toLeg.isGroup())
				disableVoicemail = toLeg.getGroup().getConfig().getDisableVoicemail();
			else if(toLeg.isPbxuser())
				disableVoicemail = toLeg.getPbxuser().getConfig().getDisableVoicemail();
		}
		return disableVoicemail == Config.VOICEMAIL_ON;
	}
	
	private RouteInfo forwardByForwardMode(Leg toLeg, RouteInfo routeInfoOriginal, ForwardMode forwardMode) throws Exception
	{
		SipAddressParser originalSipAddress = routeInfoOriginal.getToLeg().getOriginalSipAddress();
		Group g = gDAO.getGroupByAddressAndDomain(originalSipAddress.getExtension(), originalSipAddress.getDomain());
		if(g != null)
		{
			RouteInfo newRouteInfo = getOriginalRouteInfo(routeInfoOriginal);
			if(g.getNightmodeStatus().equals(Group.NIGHTMODE_ON))			
				return forwardToVoicemail(newRouteInfo);
		}
		
		Forward f = null;
		Config config = null;
		if(toLeg.isPbxuser())
		{
			config = toLeg.getPbxuser().getConfig();
		} else if(toLeg.isGroup())
		{
			config = toLeg.getGroup().getConfig();
		} else if( toLeg.isSipTrunk()) //desvio de siptrunk
		{
			config = confDAO.getConfigByPbxuser(toLeg.getSiptrunk().getPbxuserKey());			
		}
		
		if(config != null)
			f = fDAO.getForwardByConfig(config.getKey(), forwardMode.getValue());
		if(f == null)
		{			
			int callState = getVoicemailCallState(toLeg);
			throw new InvalidForwardException("Invalid forward, please check mode and user!", callState);
		}
		if(f.getStatus().intValue() == Forward.STATUS_OFF)
			return forwardToVoicemail(routeInfoOriginal);
		
		String to = f.getAddressKey() != null ? addDAO.getByKey(f.getAddressKey()).getAddress() : f.getTarget();
		//boolean isFromForward = Integer.valueOf(IPXProperties.getProperty(IPXPropertiesType.PBX_FORWARD_DISPLAY_MODE)) == FROM_DISPLAY;
		boolean isFromForward = config.getForwardType().intValue() == Config.FORWARD_TYPE_FROM;
		return tryForward(to, routeInfoOriginal, isFromForward);
	}

	private RouteInfo forwardToMusic(RouteInfo previousRouteInfo) throws Exception
	{
		if(previousRouteInfo.getFromLeg().getRouteType() != RouteType.STATION)
			previousRouteInfo.getCallInfo().setFrom(previousRouteInfo.getCallInfo().getTo());
		return tryForward(User.MUSICSERVER_NAME, previousRouteInfo, true);
	}
	
	private RouteInfo getOriginalRouteInfo(RouteInfo routeInfoOriginal) throws Exception
	{
		//dnakamashi --> bug CDR( call to Group --> desvio para PSTN - dialed number ficava o nome do grupo) 
		//Agora o clone do objeto é passado, pois antes a originalSipAddress(routeinfoOriginal) sofria alterações
		SipAddressParser originalSipAddress = routeInfoOriginal.getToLeg().getOriginalSipAddress().clone();
		
		Address add = addDAO.getAddress(originalSipAddress.getExtension(), originalSipAddress.getDomain());
		Leg newToLeg = makeStationLeg(add, originalSipAddress, originalSipAddress, false, false, true);
		CallInfo newCallInfo = getCallInfo(routeInfoOriginal.getFromLeg().getSipAddress(), newToLeg.getSipAddress(), routeInfoOriginal.getFromLeg().getSipAddress(), newToLeg.getSipAddress().getDomain(), routeInfoOriginal.getCallInfo().getDisplay());
		RouteInfo newRouteInfo = new RouteInfo(newCallInfo, routeInfoOriginal.getFromLeg(), newToLeg);
		return newRouteInfo;
	}

	// validate
	public RouteInfo validateRouteInfo(RouteInfo routeInfo, boolean enableForward, ValidationModes mode) throws Exception
	{
		try
		{
			new RouteInfoValidator().validate(routeInfo, mode);
		} catch (ForwardAlwaysException e)
		{
			if(enableForward)
				try
				{
					routeInfo = tryForward(routeInfo, ForwardMode.ALWAYS);
				}catch (InvalidForwardException fae) {
					SipAddressParser originalSipAddress = routeInfo.getToLeg().getOriginalSipAddress();
					Group g = gDAO.getGroupByAddressAndDomain(originalSipAddress.getExtension(), originalSipAddress.getDomain());
					if(g != null)
					{
						throw fae;
					} else
						routeInfo = tryForward(routeInfo, ForwardMode.VOICEMAIL);
					
				}catch(ValidationException fae)
				{
					routeInfo = tryForward(routeInfo, ForwardMode.VOICEMAIL);
				}
			else
				throw e;
		} catch (DestinationFilterForwardException e)
		{
			if(enableForward)
				try
				{
					routeInfo = tryForward((Filter) e.getObject(), routeInfo);
				}catch(ValidationException fae)
				{
					routeInfo = tryForward(routeInfo, ForwardMode.VOICEMAIL);
				}
			else
				throw e;
		} catch (MaxConcurrentCallsException e)
		{
			e.setObj(routeInfo);
			if(!mode.equals(ValidationModes.RETURN_CALL_MODE) && !mode.equals(ValidationModes.CALLBACK_MODE) && !e.isFrom())
			{
				Forward busy = fDAO.getForwardByConfig(routeInfo.getToLeg().getPbxuser().getConfigKey(), Forward.BUSY_MODE);
				if(busy.getAddressKey() != null)
					busy.setAddress(addDAO.getByKey(busy.getAddressKey()));
				if(busy.getStatus().intValue() == Forward.STATUS_ON)
					try
					{
						routeInfo = tryForward(routeInfo, busy);
					} catch(OriginationCallBlockedException be)
			        {
			            routeInfo = tryForward(routeInfo, ForwardMode.VOICEMAIL);
			        }
				else if(isForwardToVoicemail(routeInfo.getToLeg()))
					routeInfo = tryForward(routeInfo, ForwardMode.VOICEMAIL);
				else
					throw e;
			} else
				throw e;
		} catch(MaxForwardsException e)
		{
			routeInfo = tryForward(routeInfo, ForwardMode.VOICEMAIL);
		}catch(ValidationException ve)
		{
			ve.setObj(routeInfo);
			throw ve;
		}			
		return routeInfo;
	}

	private boolean isForwardToVoicemail(Leg leg)
	{
		return (leg.isGroup() && leg.getGroup().getConfig().getDisableVoicemail().intValue() == Config.VOICEMAIL_ON) || (leg.isPbxuser() && leg.getPbxuser().getConfig().getDisableVoicemail().intValue() == Config.VOICEMAIL_ON);
	}

	private RouteInfo tryForward(Filter filter, RouteInfo routeInfo) throws Exception
	{
		String address = filter.getAddressKey() != null ? filter.getAddress().getAddress() : filter.getTarget();
		
		//inicio --> dnakamashi - bug #5705 - version 3.0.5
		Config config  = routeInfo.getToLeg().isPbxuser() ? routeInfo.getToLeg().getPbxuser().getConfig() : routeInfo.getToLeg().isGroup() ? routeInfo.getToLeg().getGroup().getConfig() : null;
		boolean isFromForward = config != null ? config.getForwardType().equals(Config.FORWARD_TYPE_FROM) : true;
		return tryForward(address, routeInfo, isFromForward);
		//fim --> dnakamashi - bug #5705 - version 3.0.5
	}

	private RouteInfo tryForward(RouteInfo routeInfo, Forward forward) throws Exception
	{
		String address = forward.getAddressKey() != null ? forward.getAddress().getAddress() : forward.getTarget();
		Config config  = routeInfo.getToLeg().isPbxuser() ? routeInfo.getToLeg().getPbxuser().getConfig() : routeInfo.getToLeg().isGroup() ? routeInfo.getToLeg().getGroup().getConfig() : null;
		//boolean isFromForward = routeInfo.getToLeg().getUser().getPbxuser().getConfig() Integer.valueOf(IPXProperties.getProperty(IPXPropertiesType.PBX_FORWARD_DISPLAY_MODE)) == FROM_DISPLAY;		
		boolean isFromForward = config.getForwardType().equals(Config.FORWARD_TYPE_FROM);
		return tryForward(address, routeInfo, isFromForward);
	}
	
	private RouteInfo tryForward(String address, RouteInfo previousRouteInfo, boolean isFromForward) throws Exception
	{
		CallInfo callInfo = previousRouteInfo.getCallInfo();
		SipAddressParser to = new SipAddressParser(address, callInfo.getDomain());
		
		boolean isToVoicemail = isToVoicemail(to.getExtension(), previousRouteInfo.getCallInfo().getDomain());
		
		//inicio --> dnakamashi - Validação feita para o Basix Store - version 3.0.3
		if(previousRouteInfo.getToLeg().isGroup() && !previousRouteInfo.getToLeg().isGroupForwardAllowed() && !isToVoicemail)
			throw new InvalidForwardException("Forward is not allowed!", CallStateEvent.UNAVALIABLE, previousRouteInfo);
		//fim --> dnakamashi - Validação feita para o Basix Store - version 3.0.3
		
		isFromForward = isFromForward || isToVoicemail;
		
		SipAddressParser previousFrom = isHideAni(previousRouteInfo.getFromLeg(), callInfo) ? callInfo.getOriginalFrom().clone() : callInfo.getFrom().clone();		
				
		//dnakamashi --> bug #5394 - version 3.0.5 RC6.6
		SipAddressParser from = null; //isFromForward && !previousRouteInfo.getFromLeg().isPSTN() ? previousFrom : getMyAddress(previousRouteInfo);
		if(isFromForward)
		{
			from = previousFrom;
			
			// bug 5394 - Verifica se a chamada eh um pstnIncoming com desvio onnet
			// dessa forma a chamada deve ser myDisplay, para que o outro dominio 
			// consiga processar a chamada como ON_NET incoming, porem o campo display 
			// do header FROM deve conservar o numero da PSTN, para que o destino tenha 
			// como display a PSTN.
			if(previousRouteInfo.getFromLeg().isPSTN() && to.getExtension().matches(PSTN_DIGITS))
			{
				Address toAddress = getAddress(to.getExtension(), previousRouteInfo.getToLeg().getUser().getDomainKey(), false);
				if(toAddress != null && 
						toAddress.getDomainKey().intValue() != previousRouteInfo.getToLeg().getUser().getDomainKey().intValue()) // ON_NET
				{
					from = getMyAddress(previousRouteInfo);
				}
			}
			
		} else
			from = getMyAddress(previousRouteInfo);
		
		
		String display = verifyCorrectForwardDisplay(previousRouteInfo);
		String ivrCallID = callInfo.getIvrCallID();
		callInfo = getCallInfo(from, to, callInfo.getRequestUser(), callInfo.getDomain(), display);
		callInfo.setIvrCallID(ivrCallID);
		RouteInfo routeInfo = getRouteInfo(callInfo, ValidationModes.FORWARD_MODE);
		
		//inicio --> dnakamashi - bug #5394 - version 3.0.5 RC6.6
		if(previousRouteInfo.getFromLeg().isPSTN() && isFromForward && routeInfo.getToLeg().getRouteType() == RouteType.ON_NET)
		{
			routeInfo.getFromLeg().setDisplay(previousRouteInfo.getFromLeg().getSipAddress().getExtension());
		//fim --> dnakamashi - bug #5394 - version 3.0.5 RC6.6
		} else if(previousRouteInfo.getToLeg().getRouteType().equals(RouteType.PSTN) && !routeInfo.getToLeg().getRouteType().equals(RouteType.PSTN))
		{ // GAMBI: Cenario: chamadas para SipTrunk as Gateway com desvio para Siptrunk As Gateway ou TrunkGroup:
			// chamadas para SiPTrunk as Gateway com desvio para SiptTrunk as Gateway ou TrunkGroup nao conserva o display como DID, 
			// pois o novo destino nao eh mais PSTN. A solucao definitiva seria fazer como que desvio seja PSTN (para siptrunk as gateway e trunkgroup)
			// pois a ToLeg acaba virando station.
			routeInfo.getFromLeg().setDisplay(previousRouteInfo.getFromLeg().getDisplay());
		}
		
		routeInfo.setPreviousRouteInfo(previousRouteInfo);
		try
		{
			//inicio --> dnakamashi - Validação feita para o Basix Store - version 3.0.3			
			if(routeInfo.getToLeg().isVoicemail() && !routeInfo.getPreviousRouteInfo().getToLeg().isVoicemailAllowed())
				 throw new InvalidForwardException("Voicemail is off!", CallStateEvent.PERMISSION_DENIED, previousRouteInfo);			
			//fim --> dnakamashi - Validação feita para o Basix Store - version 3.0.3						
				
			new RouteInfoValidator().validate(routeInfo, ValidationModes.FORWARD_MODE);
		} catch (ForwardAlwaysException e)
		{
			throw new ForwardAlwaysException("Previous forward was executed! No more forwards left!", CallStateEvent.CALL_UNANSWERED, e.getObject());
		} catch (DestinationFilterForwardException e)
		{
			throw new DestinationFilterForwardException("Previous forward was executed! No more forwards left!", CallStateEvent.CALL_UNANSWERED, e.getObject());
		} catch(MaxConcurrentCallsException e)
		{
			e.setObj(routeInfo);
			throw e;
		}
		return routeInfo;
	}
	
	private String verifyCorrectForwardDisplay(RouteInfo previousRouteInfo) throws DAOException
	{
		String display = previousRouteInfo.getCallInfo().getDisplay();
		Config config  = previousRouteInfo.getToLeg().isPbxuser() ? previousRouteInfo.getToLeg().getPbxuser().getConfig() : previousRouteInfo.getToLeg().isGroup() ? previousRouteInfo.getToLeg().getGroup().getConfig() : null;
		if(config != null && config.getForwardType().equals(Config.FORWARD_TYPE_TO))
		{
   		 	Pbx pbx = pbxDAO.getPbxFullByDomain(previousRouteInfo.getCallInfo().getDomain());
   		 	if(pbx==null)
   		 		return display;
			if(previousRouteInfo.getToLeg().getGroup() != null)
				display= getOutgoingAddress(pbx.getAddress(),previousRouteInfo.getToLeg().getGroup());			
			else if(previousRouteInfo.getToLeg().getPbxuser()!= null && previousRouteInfo.getToLeg().getPbxuser().getIsAnonymous().equals(Pbxuser.ANONYMOUS_OFF))
				display= getOutgoingAddress(pbx.getAddress(),previousRouteInfo.getToLeg());	
		}
		return display;
	}

	private boolean isToVoicemail(String to, String domain) throws DAOException
	{
		Address toAddress = addDAO.getAddress(to, domain);
		if(toAddress != null && toAddress.getPbxuserKey() != null)
		{
			Pbxuser toPbxuser = puDAO.getPbxuserAndUser(toAddress.getPbxuserKey());
			if(toPbxuser != null)
				return toPbxuser.getUser().getAgentUser() == User.TYPE_VOICEMAIL;
		}
		
		return false;
	}
	
	private SipAddressParser getMyAddress(RouteInfo routeInfo)
	{
		if(routeInfo.getToLeg() != null && routeInfo.getToLeg().getUser() != null)
			return new SipAddressParser(routeInfo.getToLeg().getUser().getUsername(), routeInfo.getCallInfo().getDomain());
		else if(routeInfo.getToLeg() != null && routeInfo.getToLeg().getGroup() != null)
				return new SipAddressParser(routeInfo.getToLeg().getGroup().getName(), routeInfo.getCallInfo().getDomain());
		else
			return routeInfo.getCallInfo().getTo();			
	}
	
	public void checkFilters(Leg fromLeg, Leg toLeg, ValidationModes mode) throws DAOException, ValidationException
	{
		if(toLeg.isTerminal())
		{
			Terminal terminal = tDAO.getTerminalByAddressAndDomain(toLeg.getPbxuser().getUser().getUsername(), toLeg.getPbxuser().getUser().getDomain().getDomain());
			if(terminal != null)
				return;			
		}
	
		int precenseStatus = toLeg.getPbxuser().getPresence().getState();
		String address = fromLeg.getSipAddress().getExtension();
		List<Filter> fList = filterDAO.getActiveFilterListByConfig(toLeg.getPbxuser().getConfigKey());
		List<Address> addList = null;
		if(!fromLeg.isPSTN())
		{
//			if(mode == ValidationModes.CALLBACK_MODE)
			addList = fromLeg.isGroup() ? addDAO.getAddressListByGroup(fromLeg.getGroup().getKey()) : addDAO.getAddressListByPbxuser(fromLeg.getPbxuser().getKey());
//			else
//				addList = toLeg.isGroup() ? addDAO.getAddressListByGroup(fromLeg.getGroup().getKey()) : addDAO.getAddressListByPbxuser(fromLeg.getPbxuser().getKey());
		}
		for(Filter f : fList)
		{
			boolean reject = false;
			if(f.getPresence().intValue() == precenseStatus || f.getPresence().intValue() == Presence.STATE_ANY)
			{
				boolean matches = f.getPatternType().intValue() == Filter.TYPE_MATCH;
				reject = !matches;
				if(fromLeg.isPSTN())
				{
					if(f.getRegex() != null)
						reject = address.matches(f.getRegex()) == matches;
					else
						reject = true;
				} else
				{
					String regEx = f.getRegex();
					for(int j = 0; j < addList.size(); j++)
						if(addList.get(j).getAddress().matches(regEx))
							reject = matches;
				}
			}
			if(reject)
				if(f.getAction().intValue() == Filter.ACTION_BLOCK)
					throw new DestinationCallBlockedException("Destination address blocked to origination call by user filter!", CallStateEvent.CALL_REJECTED, f);
				else if(f.getAction().intValue() == Filter.ACTION_FORWARD)
					if(mode == ValidationModes.CALLBACK_MODE)
						throw new OriginationFilterForwardException("Origination in forward always by user filter!", CallStateEvent.CALL_FORWARDED, f);
					else
						throw new DestinationFilterForwardException("Destination in forward always by user filter!", CallStateEvent.CALL_FORWARDED, f);
		}
	}

	public List<Target> getTargets(Leg fromLeg, Leg toLeg, String locale, SipAddressParser transferRequestUsername, String domain) throws Exception
	{
		List<Target> targets = null;
		if(toLeg.getRouteType().equals(RouteType.STATION))
			if(toLeg.isGroup())
				targets = getGroupTargets(fromLeg.getSipAddress().getExtension(), toLeg,transferRequestUsername);
			else
				targets = getStationTargets(toLeg);
		else if(toLeg.getRouteType().equals(RouteType.PSTN))
			targets = getPSTNTargets(fromLeg, toLeg, locale, domain);
		else if(toLeg.getRouteType().equals(RouteType.ON_NET))
			targets = getOnNetTargets(toLeg);
		else if(toLeg.getRouteType().equals(RouteType.SPY_COMMAND))
			targets = getCallCenterTarget(domain);
			
		return targets;
	}

	public Integer setTimeout(Leg toLeg) throws DAOException
	{
		int timeout = -1;
		try
		{
			if(toLeg.isGroup())
			{
				// O timeout dos grupos CallCenter são gerenciados pelo pelo ACDGroupServer, ACDGroupHandler e ACDGroupScheduler - rribeiro
				if(toLeg.getGroup().getGroupType() == Group.ACDCALLCENTER_GROUP || toLeg.getRouteType().equals(RouteType.SPY_COMMAND))
					timeout = -1;
				else
					timeout = toLeg.getGroup().getConfig().getTimeoutcall();
			}
			else if(toLeg.isPbxuser())
			{
				Forward no = fDAO.getForwardByConfig(toLeg.getPbxuser().getConfigKey(), Forward.NOANSWER_MODE);
				Config config = toLeg.getPbxuser().getConfig();
				if(no != null && no.getStatus().intValue() == Forward.STATUS_ON && config.getTimeoutcall() != null)
					timeout = config.getTimeoutcall();
				else
					timeout = toLeg.getPbxuser().getServiceclass().getConfig().getTimeoutcall();
			} else if(toLeg.getRouteType().equals(RouteType.COMMAND_CONFIG))
			{
				
				timeout = -1;
				
			} else if(toLeg.isPSTN()) // ActiveCall presa em estado Dialing.
			{
				String pstn_timeout = IPXProperties.getProperty(IPXPropertiesType.PSTN_TIMEOUT_REQUEST);
				if(pstn_timeout != null && pstn_timeout.length() > 0)
				{
					try
					{
						timeout = Integer.parseInt(pstn_timeout);
					} catch (Throwable t)
					{
						if(logger.isDebugEnabled())
							logger.debug(new StringBuilder("Error while converting PSTN timeout ").append(pstn_timeout));
					}
				}
			}
		} catch(Throwable t)
		{
			logger.debug("Error to set Timeout, nothing to do...");
		}
		return timeout;
	}

	private List<Target> getStationTargets(Leg toLeg) throws Exception
	{
		List<Target> targetList = new ArrayList<Target>();
		Pbxuser pu = toLeg.getPbxuser();
		Target target = new Target(pu.getUser().getUsername(), toLeg.getSipAddress().getDomain());
		targetList.add(target);
		return targetList;
	}

	private List<Target> getGroupTargets(String from, Leg toLeg, SipAddressParser transferRequestUser) throws DAOException
	{
		Group group = toLeg.getGroup();
		List<Target> targetList = new ArrayList<Target>();
		String to = toLeg.getGroup().getName();
		String domain = toLeg.getSipAddress().getDomain();
		int groupType = group.getGroupType().intValue();
		String transferRequestUsername = transferRequestUser != null ? transferRequestUser.getExtension() : null;
		if(groupType == Group.HUNT_GROUP)
			targetList = getHuntTargets(from, to, domain, false, group.getAlgorithmType(), transferRequestUsername);
		else if(groupType == Group.ACDHUNT_GROUP)
			targetList = getHuntTargets(from, to, domain, true, group.getAlgorithmType(), transferRequestUsername);
		else if(groupType == Group.ACDCALLCENTER_GROUP)
			targetList = getCallCenterTarget(domain);
		else if(groupType == Group.RING_GROUP)
			targetList = getRingTargets(from, to, domain, transferRequestUsername);
		else if(groupType == Group.SIPTRUNK_GROUP)
			targetList = getSipTrunkGroupTargets(from, to, domain, group.getAlgorithmType(), transferRequestUsername);
		
		return targetList;
	}

	private List<Target> getSipTrunkGroupTargets(String from, String to, String domain, Integer algorithmType, String transferRequestUsername) throws DAOException 
	{
		List<Target> targetList = new ArrayList<Target>();
		List<String> usernameList = gDAO.getGroupSipTrunkTargets(to, domain, algorithmType);
		for(String username : usernameList)
			if(from == null || !from.equals(username) || !(transferRequestUsername!=null && transferRequestUsername.equals(username)))
				targetList.add(new Target(username, domain));
		return targetList;
	}

	private List<Target> getRingTargets(String from, String to, String domain, String transferRequestUsername) throws DAOException
	{
		List<Target> targetList = new ArrayList<Target>();
		List<String> tmp = new ArrayList<String>();
		List<String> usernameList = gDAO.getGroupRingTargets(to, domain);
	
		for(String username : usernameList)		
			if(from == null || !from.equals(username) || !(transferRequestUsername!=null && transferRequestUsername.equals(username)))
				tmp.add(username);
		
		targetList.add(new Target(tmp, domain));
		return targetList;
	}

	private List<Target> getHuntTargets(String from, String to, String domain, boolean isACD, Integer algorithmType, String transferRequestUsername) throws DAOException
	{
		List<Target> targetList = new ArrayList<Target>();
		List<String> usernameList = isACD ? gDAO.getGroupACDTargets(to, domain, algorithmType) : gDAO.getGroupHuntTargets(to, domain, algorithmType);
		for(String username : usernameList)
			if(from == null || !from.equals(username) || !(transferRequestUsername!=null && transferRequestUsername.equals(username)))
				targetList.add(new Target(username, domain));
		return targetList;
	}
	
	private List<Target> getCallCenterTarget(String domain) throws DAOException
	{
		List<Target> targetList = new ArrayList<Target>();
		String username = User.ACDGROUP_NAME;
		targetList.add(new Target(username, domain));
		return targetList;
	}
	

	private List<Target> getPSTNTargets(Leg fromLeg, Leg toLeg, String locale, String domain) throws Exception
	{
		List<Target> tList = new ArrayList<Target>();
		Target t = null;
		Address add = null;
		if(fromLeg.isPbxuser())
			add = getAddress(toLeg.getSipAddress().getExtension(), fromLeg.getPbxuser().getUser().getDomainKey(), false);
		if(add != null)
			t = new Target(add.getAddress(), add.getDomain().getDomain());
		else
		{
			User u = toLeg.getUser();
			if(u != null)
			{
				t = getGatewayTarget(fromLeg, u, locale, domain);
//				t = getGatewayTarget(fromLeg, gatDAO.getGatewayByUserKey(u.getKey()), locale, domain);
				
			}
		}
		if(t != null)
			tList.add(t);
		return tList;
	}
	
	private Target getGatewayTarget(Leg fromLeg, User u, String locale, String domain) throws DAOException
	{
		if(u.getAgentUser() == User.TYPE_SIPTRUNK)
		{
			return new Target(u.getUsername(), u.getDomain().getDomain());
		} else
		{
			Gateway gw = gatDAO.getGatewayByUserKey(u.getKey());
			
			// jfarah - 3.2 bug 7094
			String domainPrefix = null;
			if(domain != null)
				domainPrefix = pbxDAO.getPbxByDomain(domain).getPbxPreferences().getPrefix();
			return new Target(u.getUsername(), u.getDomain().getDomain(), getPrefix(fromLeg, gw.getPrefix(), domainPrefix, locale));
		}
	}
	
	private String getPrefix(Leg fromLeg, String gatewayPrefix, String domainPrefix, String locale)
	{
		CallLocaleSettings settings = CallLocaleSettingsFactory.getCallLocaleSettings(locale);
		String prefix = settings.makePrefix(gatewayPrefix, domainPrefix, fromLeg.getDisplay().equals(Pbxuser.ANONYMOUS));
		return prefix;
	}
	
	private List<Target> getOnNetTargets(Leg toLeg)
	{
		List<Target> tList = new ArrayList<Target>();
		Target target = new Target(toLeg.getSipAddress().getExtension(), toLeg.getSipAddress().getDomain());
		tList.add(target);
		return tList;
	}

	private User getGatewayUser(String number, Long domainKey) throws Exception
    {
        User u = null;
        List<Routerule> routeList = rrDAO.getRouteListByPriority();
        Long gatewayKey = null;
        for(int i = 0; i < routeList.size() && gatewayKey == null; i++)
        {
            Routerule rr = routeList.get(i);
            if(number.matches(rr.getRegex()))
                gatewayKey = rr.getGatewayKey();
        }
        
        if(gatewayKey != null)
            u = uDAO.getUserByGatewayKey(gatewayKey);

        return u;
    }
	
	/**
	 * Metodo que retorna um gateway ou um siptrunk (as gateway) baseado no address passado. Esse address deve ser o username do gatewaay ou do siptrunk.
	 */
	private User getPSTNUser(SipAddressParser requestUser) throws DAOException 
	{
		if(requestUser == null)
			return null;
		User u = uDAO.getUserByUsernameAndDomain(requestUser.getExtension(), requestUser.getDomain());
		
		if(u != null && (u.getAgentUser().intValue() == User.TYPE_GATEWAY ||
						u.getAgentUser().intValue() == User.TYPE_SIPTRUNK))
		{
			return u;
		}
		
		return null;
	}
	
	public Activecall getActivecallToCapture(String extensionToCapture, String capturer, String domain) throws DAOException
	{
		Activecall activecall = null;
        if(extensionToCapture.length() > 0)	// obter o activecall em ringing a mais tempo no grupo do fromcall, obter pbxcallid desse activecall
        	activecall = getActivecall(extensionToCapture, domain, Activecall.STATE_ALERTING);
        else	// obter o activecall da chamada que esta tocando a mais tempo nos grupos de quem solicitou a captura
        	activecall = getActivecallInGroup(capturer, domain, Activecall.STATE_ALERTING);
        
        // Valida se o usu�rio n�o est� capturando uma chamada dele mesmo, caso seja retorna null.
        return validateActiveCall(activecall, capturer, domain); 
	}
	
	private Activecall validateActiveCall(Activecall activecall, String capturer, String domain) throws DAOException
	{
        if(activecall != null)
        	if(acDAO.getActivecallByCallIDAddressAndDomain(activecall.getCallID(), capturer, domain) != null)
        		return null;
        return activecall;
	}
	
	public Activecall getActivecall(Long activecallKey) throws DAOException
	{
		return acDAO.getByKey(activecallKey);
	}
	
	public Activecall getActivecall(String address, String domain) throws DAOException
	{
		Pbxuser pu = puDAO.getPbxuserByTerminal(address, domain);
		if(pu != null)
			address = pu.getUser().getUsername();
		return acDAO.getActiveCallByAddress(address, domain);
	}
	
	public List<Activecall> getActiveCallListByPbxuser(Long pbxuserKey) throws DAOException{
		return acDAO.getActiveCallListByPbxuser(pbxuserKey);
	}
	
	public Activecall getActivecall(String address, String domain, int state) throws DAOException
	{
		Pbxuser pu = puDAO.getPbxuserByTerminal(address, domain);
		if(pu != null)
			address = pu.getUser().getUsername();
		return acDAO.getActiveCallByAddressAndState(address, domain, state);
	}

	public Activecall getActivecall(String address, String domain, String callID) throws DAOException
	{
		return acDAO.getActiveCallByAddress(address, domain, callID);
	}
	
	public Activecall getActivecall(Long pbxuserKey, int state) throws DAOException
	{			
		if (pbxuserKey != null)
			return acDAO.getActiveCallByPbxuser(getPbxuserKey(pbxuserKey), state);
				
		return null;		
	}	
	
	public Activecall getActivecall(Long pbxuserKey, String callId) throws DAOException
	{			
		if (pbxuserKey != null)
			return acDAO.getActiveCallByPbxuser(getPbxuserKey(pbxuserKey), callId);
		
		return null;		
	}

	/**
	 * Se o Pbxuser passado � terminal retorna a chave do Pbxuser Termnial, se n�o, retorna a chave do Pbxuser passado. 
	 */
	private Long getPbxuserKey(Long pbxuserKey) throws DAOException
	{
		Pbxuser pbxuser = puDAO.getByKey(pbxuserKey);
		
		if( pbxuser.getUser().getAgentUser() == User.TYPE_TERMINAL)
		{
			Pbxuser terminal = tDAO.getAssociatedPbxuserByTerminalPbxuserKey(pbxuser.getKey());
			if (terminal != null)
				return terminal.getKey();
		}
		
		return pbxuser.getKey();
	}
	
	public Activecall getActiveCallByAddress(String address, String domain, String callId) throws DAOException
	{
		return acDAO.getActiveCallByAddress(address, domain, callId);
	}
	
	public Activecall getActivecall(String address, String domain, int state, String callId) throws DAOException
	{
		return acDAO.getActiveCallByAddressAndState(address, domain, state, callId);
	}
	
	public Activecall getActivecallInGroup(String address, String domain, int state) throws DAOException
	{
		Pbxuser pu = puDAO.getPbxuserByTerminal(address, domain);
		if(pu != null)
			address = pu.getUser().getUsername();
		return acDAO.getActiveCallInGroupByAddressAndState(address, domain, state);
	}
	
	public List<Target> getTargetList(Leg toLeg, List<Target> targetList) throws DAOException
	{
		if(targetList != null && targetList.size() > 0)
		{
			if(toLeg.isGroup())
				return getGroupTargetList(toLeg.getGroup().getGroupType().intValue(), targetList);
			else if(toLeg.getRouteType().equals(RouteType.SPY_COMMAND))
				return getCallCenterTargetList(targetList);
			else
				return getPbxuserTargetList(targetList);
		}
		return new ArrayList<Target>();
	}

	private List<Target> getPbxuserTargetList(List<Target> originalTargets)
	{
		List<Target> nextTargets = new ArrayList<Target>();
		nextTargets.addAll(originalTargets);
		originalTargets.clear();
		return nextTargets;
	}

	private List<Target> getGroupTargetList(int groupType, List<Target> originalTargets) throws DAOException
	{
		List<Target> nextTargets = new ArrayList<Target>();
		if(groupType == Group.RING_GROUP)
		{
			for(int i = 0; originalTargets.size() > 0; i++)
			{
				Target target = (Target) originalTargets.remove(0);
				while (target.getUsernameList().size() > 0)
				{
					String username = target.getUsernameList().remove(0);
					if(isReady(username, target.getDomain(), groupType))
						nextTargets.add(new Target(username, target.getDomain()));
				}
			}
			originalTargets.clear();
			
		} else if(groupType == Group.ACDCALLCENTER_GROUP)
		{
			Target target = (Target) originalTargets.remove(0);
			nextTargets.add(target);
		} else
		{
			Target target = (Target) originalTargets.remove(0);
			if(isReady(target.getUsername(), target.getDomain(), groupType))
				nextTargets.add(target);
		}
		return nextTargets;
	}
	
	private List<Target> getCallCenterTargetList(List<Target> targetList)
	{
		List<Target> tmp = new ArrayList<Target>();
		tmp.add(targetList.remove(0));
		return tmp;
	}
	
	private boolean isReady(String username, String domain, int grouptype) throws DAOException
	{		
		Pbxuser pu = puDAO.getPbxuserByAddressAndDomain(username, domain);
		Long actual = acDAO.howManyActiveCallByPbxuser(pu.getKey());
		Forward f = fDAO.getForwardByConfig(pu.getConfigKey(), Forward.ALWAYS_MODE);
		boolean activeCallValidation = false;
		
		if(grouptype == Group.SIPTRUNK_GROUP)
		{
			SipTrunk siptrunk = siptrunkDAO.getSipTrunksByPbxuserKey(pu.getKey());
			activeCallValidation = actual < siptrunk.getCalls();		
		}
		else
		{
			activeCallValidation = actual.longValue() == 0L;
		}
		
		return  activeCallValidation && f.getStatus().intValue() == Forward.STATUS_OFF;
	}

	public void saveActivecall(Activecall ac) throws DAOException, ValidateObjectException
	{
		if(ac.getAddress() != null && ac.getAddress().getPbxuserKey() != null)
		{
			Pbxuser tu = puDAO.getPbxuserAndUser(ac.getAddress().getPbxuserKey());
			if(tu != null && tu.getUser().getAgentUser() == User.TYPE_TERMINAL)
			{
				Pbxuser pu = tDAO.getAssociatedPbxuserByTerminalPbxuserKey(tu.getKey());
				if(pu != null)
				{
					Address sipID = addDAO.getSipIDByPbxuser(pu.getKey());
					if(sipID != null)
						ac.setAddress(sipID);
				}
			}
		}
		acDAO.save(ac);
	}

	public void deleteActivecalls(List<Long> activecallKeyList) throws DAOException
	{
		for(Long key : activecallKeyList)
			removeActivecall(key);
	}

	public void removeActivecall(Long acKey) throws DAOException
	{
		Activecall ac = acDAO.getByKey(acKey);
		if(ac != null)
			acDAO.remove(ac);
	}

	public void removeActivecallListByCallID(String callID) throws DAOException, ValidateObjectException
	{
		List<Activecall> list = acDAO.getActivecallListByCallID(callID);
		for(Activecall ac : list)
			acDAO.remove(ac);
	}

	public ReportResult<ActivecallInfo> findActivecalls(Report<ActivecallInfo> report) throws DAOException
	{
		ReportDAO<Activecall, ActivecallInfo> callReport = dao.getReportDAO(ActivecallDAO.class);
		Long size = callReport.getReportCount(report);
		List<Activecall> callList = callReport.getReportList(report);
		List<ActivecallInfo> callInfoList = new ArrayList<ActivecallInfo>();
		Long domainKey = report.getInfo().getDomainKey();
		Domain domain = domainKey != null ? dmDAO.getByKey(domainKey) : dmDAO.getRootDomain();
		List<Duo<Long, String>> domainList = dmDAO.getDomainsByRootDomain(domain.getRootKey() != null ? domain.getRootKey() : domain.getKey());
		for(Activecall call : callList)
		{
			ActivecallInfo info = new ActivecallInfo(call, call.getAddress().getAddress(), domainList, call.getAddress().getDomain().getDomain());
			callInfoList.add(info);
		}
		if(callInfoList.size() == 0)
		{
			ActivecallInfo info = new ActivecallInfo(domainList);
			callInfoList.add(info);
		}
		return new ReportResult<ActivecallInfo>(callInfoList, size);
	}
	
	//trace call
	public RouteInfo simulateTraceCall(TraceCallInfo traceCallInfo) throws Exception
	{
		String [] add = traceCallInfo.getFrom().split("@");
		String domain = add.length > 1 ? add[1] : null;
		
		SipAddressParser sipFrom = new SipAddressParser(traceCallInfo.getFrom());
		SipAddressParser sipTo = new SipAddressParser(traceCallInfo.getTo());

		if(sipFrom.getAddress().equals(sipTo.getAddress()))
			throw new SameFromAndToException("From and To user are the same", TraceCallInfo.STATE_SAME_FROM_AND_TO);

		Address fromAdd = getAddressBySipAddressParser(sipFrom);
		Address toAdd = getAddressBySipAddressParser(sipTo);

		if (fromAdd != null)
		{

			if(fromAdd.getPbxKey() != null) // from eh trunkLine
			{
				 if(toAdd == null) // to eh PSTN
					 throw new InvalidOwnerException("From is a trunk line", true);
				 else // Quando a origem eh trunkline a chamada deve ser processada no domain da perna to.
					 sipFrom.setDomain(toAdd.getDomain().getDomain());
				 
			} else if(fromAdd.getType() == Address.TYPE_DID)
			{

				if(toAdd != null && (toAdd.getType() == Address.TYPE_EXTENSION || toAdd.getType() == Address.TYPE_SIPID))
				{
					sipFrom.setDomain(fromAdd.getDomain().getDomain());
					sipTo.setDomain(toAdd.getDomain().getDomain());
				}
			}

			if(fromAdd.getType() != Address.TYPE_DID || (toAdd != null && toAdd.getType() == Address.TYPE_DID))
			{  
				if(sipFrom.getDomain() == null)
					sipFrom.setDomain(fromAdd.getDomain().getDomain());

				if(sipTo.getDomain() == null)
				{	  
					if(toAdd != null)
						sipTo.setDomain(toAdd.getDomain().getDomain());
					else
						sipTo.setDomain(fromAdd.getDomain().getDomain());
				}
			}

		}else
		{
			if (toAdd != null)
			{
				if(sipFrom.getDomain() == null)	
					sipFrom.setDomain(toAdd.getDomain().getDomain());

				sipTo.setDomain(toAdd.getDomain().getDomain());
			}else
				throw new InvalidOwnerException("From and To not Found", true);
		}

		if(sipFrom.getDomain() == null)
			sipFrom.setDomain(fromAdd.getDomain().getDomain());

		CallInfo callInfo = getCallInfo(sipFrom, sipTo, sipFrom, (domain == null ? sipFrom.getDomain() : domain), sipFrom.getExtension());
		RouteInfo routeInfo = getRouteInfo(callInfo, false, ValidationModes.NO_VALIDATION); 

		//		if (routeInfo.getToLeg().getRouteType() == RouteType.ON_NET/* && tempAddressTo.getDomain().equals(routeInfo.getToLeg().getUser().getDomain().getDomain())*/)
		//		{
		//			sipFrom.setExtension(routeInfo.getFromLeg().getDisplay());
		//			callInfo = getCallInfo(sipFrom, sipTo, sipFrom, routeInfo.getToLeg().getUser().getDomain().getDomain(), sipFrom.getExtension());
		//			routeInfo = getRouteInfo(callInfo, false); 
		//		}

		return routeInfo;
	}

	private Address getAddressBySipAddressParser(SipAddressParser sipAddressParser) throws DAOException
	{
		Address address = null;
		 
		if (sipAddressParser != null)
		{	
			if (sipAddressParser.getDomain() != null)
			   address = addDAO.getAddress(sipAddressParser.getExtension(), sipAddressParser.getDomain());
	        
		    if(address == null)
	    	   address = addDAO.getDIDAddress(sipAddressParser.getExtension());
		}
		return address;
	}
	
    private void  validateAddress (Address fromAdd, Address toAdd) throws InvalidOwnerException
    {
//    	if(address.getPbxuserKey() == null && address.getGroupKey() == null)
//    		throw new InvalidOwnerException("Did associated to default operator", true);
    	
    	if(fromAdd.getPbxKey() != null && toAdd == null)
    			throw new InvalidOwnerException("From is a trunk line", true);
    	
    }
    
    public int validateTracecallRouteInfo(RouteInfo routeInfo) throws Exception 
	{
		int callState = TraceCallInfo.STATE_SUCCESS;
		try
		{
			RouteInfo newRoute = validateRouteInfo(routeInfo, true, ValidationModes.FULL_MODE);
			if(!newRoute.equals(routeInfo))			
				routeInfo.setPreviousRouteInfo(newRoute);
		} catch(UserWithoutSipSessionlogException e) 
		{
			callState = TraceCallInfo.STATE_FROM_WITHOUT_SIPSESSION;
		} catch(PermissionDeniedException e) 
		{
			if(e.isAni())
				callState = TraceCallInfo.STATE_FROM_PERMISSION_DENIED;
			else
				callState = TraceCallInfo.STATE_TO_PERMISSION_DENIED;
		} catch(InvalidDestinationUser e) 
		{
			callState = TraceCallInfo.STATE_INVALID_DESTINATION;
		} catch(OriginationCallBlockedException e) 
		{
			callState = TraceCallInfo.STATE_FROM_BLOCKED;
		} catch(DestinationCallBlockedException e) 
		{
			callState = TraceCallInfo.STATE_TO_BLOCKED;
		} catch(DestinationInDNDException e) 
		{
			callState = TraceCallInfo.STATE_TO_DND;
		} catch(MaxConcurrentCallsException e) 
		{
			callState = TraceCallInfo.STATE_TO_MAX_CC;
		} catch(ForwardAlwaysException e) 
		{
			callState = TraceCallInfo.STATE_TO_FORWARD_ALWAYS;
		} catch(OriginationFilterForwardException e) 
		{
			callState = TraceCallInfo.STATE_FROM_FILTERED;
		} catch(DestinationFilterForwardException e) 
		{
			callState = TraceCallInfo.STATE_TO_FILTERED;
		} catch(InvalidOwnerException e) 
		{
			callState = TraceCallInfo.STATE_INVALID_OWNER;
		}catch(LoopDetected e) 
		{
			callState =  TraceCallInfo.STATE_LOOP_DETECTED;
		}
		catch(UserNotFoundException e) 
		{
			callState = e.isFrom() ? TraceCallInfo.STATE_FROM_NOT_EXIST : TraceCallInfo.STATE_TO_NOT_EXIST;
		}
		catch(InvalidForwardException e) 
		{
			callState = TraceCallInfo.STATE_INVALID_FORWARD;
		}
		
		return callState;
	}

	public TraceCallInfo createTraceCallInfo(RouteInfo routeInfo, int callStatus) 
	{
		String from = routeInfo.getFromLeg().getSipAddress().getAddress();
		String to = null;
		String forwardTo = null;
		if(routeInfo.hasPreviousRouteInfo())
		{
			to = routeInfo.getPreviousRouteInfo().getCallInfo().getTo().getAddress();
			forwardTo = routeInfo.getCallInfo().getTo().getAddress();
		} else
			to = routeInfo.getCallInfo().getTo().getAddress();

		Integer routeTypeFrom = getRouteType(routeInfo.getFromLeg().getRouteType(), false);
		Integer routeTypeTo = getRouteType(routeInfo.getToLeg().getRouteType(), routeInfo.getToLeg().isGroup());
		List<String> targetList = new ArrayList<String>();
		
		if(callStatus != TraceCallInfo.STATE_SUCCESS)
			return new TraceCallInfo(from, to, routeTypeFrom, routeTypeTo, callStatus, forwardTo, targetList);	
		  
		if(routeTypeTo != TraceCallInfo.ROUTE_COMMAND)
		{	
			for(Target t : routeInfo.getTargetList())
				for(Sipsessionlog ssl : t.getSipsessionlogList())
					targetList.add(ssl.getContact());
			if(targetList.size() == 0)
				callStatus = TraceCallInfo.STATE_TO_NOT_FOUND;
		}
		return new TraceCallInfo(from, to, routeTypeFrom, routeTypeTo, callStatus, forwardTo, targetList);
	}

	private Integer getRouteType(RouteType type, boolean isGroup)
	{
		switch(type)
		{
			case STATION:
				return isGroup ? TraceCallInfo.ROUTE_GROUP : TraceCallInfo.ROUTE_STATION;
			case PSTN:
				return TraceCallInfo.ROUTE_PSTN;
			case PBX_FAILURE:
				return TraceCallInfo.ROUTE_PBX_FAILURE;
			case ON_NET:
				return TraceCallInfo.ROUTE_ONNET;
			case COMMAND_CONFIG:
				return TraceCallInfo.ROUTE_COMMAND;
			case UNDEFINED:
				return TraceCallInfo.ROUTE_UNKNOW;
			default:
				return TraceCallInfo.ROUTE_UNKNOW;		
		}
	}

	public RouteInfo makeCorrectRouteInfo(RouteInfo routeInfo) 
	{
		RouteInfo tmp = routeInfo;
		if(tmp.hasPreviousRouteInfo())
		{
			tmp.getPreviousRouteInfo().setPreviousRouteInfo(tmp);
			tmp = routeInfo.getPreviousRouteInfo();
			routeInfo.setPreviousRouteInfo(null);
		}
		return tmp;
	}
	
	/*
	 * ###############################################################################################
	 * 
	 * M�todos criados para realizar a l�gica da cria��o do display para o Call Status do WCC
	 * 
	 * ###############################################################################################
	 */
	
	public Pbxuser getPbxuserByAddressAndDomain(String address, String domain) throws DAOException
	{
		return puDAO.getPbxuserByAddressAndDomain(address, domain);
	}
	
	public Group getGroupByNameAndDomain(String groupName, String domain) throws DAOException
	{
		return gDAO.getGroupByAddressAndDomain(groupName, domain);
	}
	
	public Contact getContactByUserAndPhoneAndDomain(String username, String phone, String domain) throws DAOException
	{
		return contactDAO.getContactByUserAndPhoneAndDomain(username, phone, domain);
	}

	public Contact getPublicContactByPhoneAndDomain(String phone, String domain) throws DAOException
	{
		return contactDAO.getPublicContactByPhoneAndDomain(phone, domain);
	}

	public Serviceclass getServiceClassByKey(Long serviceclassKey) throws DAOException
	{
		return scDAO.getByKey(serviceclassKey);
	}

	public boolean isUserSessionActive(String username, String domain) throws DAOException
	{
		User user  = uDAO.getUserByUsernameAndDomain(username, domain);
		List<Sessionlog> sessionLogList = null;

		if(user != null)
		{
			sessionLogList = slDAO.getOpenedSessionlogListByUser(user.getKey());
			return sessionLogList.size()>0;
		}
		return false;	
	}
	
	public String makeOrignNumber(RouteInfo routeInfo, boolean isFrom) throws DAOException
	{
		CallLocaleSettings settings = CallLocaleSettingsFactory.getCallLocaleSettings(routeInfo.getCallInfo().getLocale());
		return settings.makeOrignNumber(routeInfo, isFrom);
	}
	
	public SipAddressParser verifyCorrectToAddress(RouteInfo routeInfo, SipAddressParser sipTo) throws DAOException
	{
		SipAddressParser toAddress = sipTo;
		Config config  = routeInfo.hasPreviousRouteInfo() ?( routeInfo.getPreviousRouteInfo().getToLeg().isPbxuser() ? routeInfo.getPreviousRouteInfo().getToLeg().getPbxuser().getConfig() : routeInfo.getPreviousRouteInfo().getToLeg().isGroup() ? routeInfo.getPreviousRouteInfo().getToLeg().getGroup().getConfig() : null):null;
		RouteInfo previousRoute = routeInfo.getPreviousRouteInfo();
		//jluchetta - Mudança no if para verifcar quando for from display
		if(config != null && config.getForwardType().equals(Config.FORWARD_TYPE_FROM) )
		{
	         if(previousRoute != null )
	         {
	        	 if((routeInfo.getToLeg().getRouteType().equals(RouteType.PSTN) || routeInfo.getToLeg().getRouteType().equals(RouteType.ON_NET)) && !previousRoute.getToLeg().getRouteType().equals(RouteType.PSTN))	        	
	        	 {
	        		 String requestURI = routeInfo.getToLeg().getSipAddress().getAddress();
	        		 toAddress = getOutgoingAddressForward(previousRoute, requestURI);
	        	 }else if(previousRoute.getToLeg().getGroup() != null)
	        		  toAddress = new SipAddressParser(previousRoute.getCallInfo().getTo().getExtension(),previousRoute.getCallInfo().getTo().getDomain());
	        	else
	        		  toAddress = new SipAddressParser(previousRoute.getCallInfo().getOriginalTo().getExtension(),previousRoute.getCallInfo().getOriginalTo().getDomain());
	         }
		}
		
		return toAddress;
	}
	
	//jluchetta - Método com a lógica para verificar o to address de uma routeInfo
	private SipAddressParser getOutgoingAddressForward(RouteInfo previousRoute, String requestURI) throws DAOException
	{
		Address originalTo = addDAO.getAddress(previousRoute.getToLeg().getOriginalSipAddress().getExtension(), previousRoute.getToLeg().getOriginalSipAddress().getDomain());
		boolean didDialed = originalTo != null ? originalTo.getType().equals(Address.TYPE_DID) : false;
		if(!didDialed)
		{
			originalTo = addDAO.getAddress(previousRoute.getToLeg().getSipAddress().getExtension(), previousRoute.getToLeg().getSipAddress().getDomain());
			didDialed = originalTo != null ? originalTo.getType().equals(Address.TYPE_DID) : false;
		}
		String outAdd = originalTo.getAddress();
		String domainOriginalTO = previousRoute.getToLeg().getOriginalSipAddress().getDomain();
		if(didDialed)
		{
			List<Address> didList = new ArrayList<Address>();
			Pbx pbx = null;
			
			if(previousRoute.getToLeg().isGroup())
			{
				didList = addDAO.getDIDListByGroup(previousRoute.getToLeg().getGroup().getKey());
				pbx = getPbxFull(previousRoute.getCallInfo().getDomain());
			} else if(previousRoute.getToLeg().isPbxuser())
			{
				didList = addDAO.getDIDListByPbxuser(previousRoute.getToLeg().getPbxuser().getKey());
				pbx = getPbxFull(previousRoute.getCallInfo().getDomain());
			}
			
			if(pbx != null)
				didList.add(pbx.getAddress());
			
			outAdd = getSuitableOutgoingAddress(didList, requestURI, outAdd, previousRoute.getToLeg());//dnakamashi - bug #6652 - version 3.0.5 RC 6.6
			return new SipAddressParser(outAdd,domainOriginalTO);
		}

		Pbx pbx = pbxDAO.getPbxFullByDomain(domainOriginalTO);
		if(previousRoute.getToLeg().getGroup() != null)
			outAdd = getOutgoingAddress(pbx.getAddress(),previousRoute.getToLeg().getGroup());
		else       		
			outAdd = getOutgoingAddress(pbx.getAddress(), previousRoute.getToLeg());

		return new SipAddressParser(outAdd,domainOriginalTO);
	}

	//inicio --> dnakamashi - bug #6652 - version 3.0.5 RC 6.6
	public String getSuitableOutgoingAddress(List<Address> didList, String requestURI, String outAdd, Leg leg)
	{
		if(didList.size() == 0)
			return outAdd;
	
		String suitableOutgoingAddress = outAdd;
		
		try
		{
			Long userDefaultDidKey = leg.getPbxuser() != null ? leg.getPbxuser().getDefaultDIDKey() : null;
			
			boolean enableGetSuitableOutgoingAddress = Boolean.parseBoolean(IPXProperties.getProperty(IPXPropertiesType.IS_ENABLE_CHOOSE_OUTGOING_DISPLAY));
			
			if(enableGetSuitableOutgoingAddress && 
					(userDefaultDidKey == null || // did de saida automatico
					leg.getGroup() != null))      // grupo nao seleciona o did de saida
			{		
				String internationalPrefix = IPXProperties.getProperty(IPXPropertiesType.INTERNATIONAL_PREFIX);
				if(requestURI.startsWith(internationalPrefix))
					requestURI = requestURI.substring(internationalPrefix.length());		
				
				int bigger_hit = 0;
				int hits = 0;
				
				for(Address did : didList)
				{
					hits = compareAddress(requestURI, did.getAddress());
					
					if(hits > bigger_hit || (hits == bigger_hit && isDefaultDid(did.getKey(), userDefaultDidKey)))
					{
						bigger_hit = hits;
						suitableOutgoingAddress = did.getAddress();
					}
				}
			}
		}catch(Throwable t)
		{
			logger.error("Error while getting suitableOutgoingAddress.", t);
		}
		
		return suitableOutgoingAddress;		
	}
	
	private boolean isDefaultDid(Long addressKey, Long defaultDidKey)
	{
		if(addressKey != null && defaultDidKey != null)
			return addressKey.intValue() == defaultDidKey.intValue();
		return false;
	}
	
	public int compareAddress(String firstDid, String secondDid)
	{
		int hits;
		for(hits = 0; (hits < firstDid.length() && hits < secondDid.length()); hits++)
		{
			if(firstDid.charAt(hits) != secondDid.charAt(hits))
				return hits;
		}
		
		return hits;
	}
	//fim --> dnakamashi - bug #6652 - version 3.0.5 RC 6.6
	
	public SipAddressParser verifyCorrectFromAddress(RouteInfo routeInfo, SipAddressParser originalFrom)
	{
		SipAddressParser fromAddress = originalFrom;
		boolean hasPreviousRoute = routeInfo.hasPreviousRouteInfo();
		Config config  = hasPreviousRoute ?( routeInfo.getPreviousRouteInfo().getToLeg().isPbxuser() ? routeInfo.getPreviousRouteInfo().getToLeg().getPbxuser().getConfig() : routeInfo.getPreviousRouteInfo().getToLeg().isGroup() ? routeInfo.getPreviousRouteInfo().getToLeg().getGroup().getConfig() : null):null;
		if(hasPreviousRoute && config != null && config.equals(Config.FORWARD_TYPE_TO))
			fromAddress = routeInfo.getFromLeg().getOriginalSipAddress();
			 
		//inicio --> jfarah - dnakamashi - version 3.0.5 RC6.6
		//Correção do Callog do seguinte cenário: PSTN --liga--> user@domainA(fromDisplay) --desvia--> user@domainB
		//Antes o campo Address do Callog do user@domainB estava ficando sempre como se o user@domainA estivesse em MyDisplay.
		//Bug relacionado: #5394(3.0.5 RC6.6)
		if(routeInfo.getFromLeg().getRouteType().equals(RouteType.ON_NET))
			fromAddress = new SipAddressParser(routeInfo.getFromLeg().getDisplay(), originalFrom.getDomain());
		//fim --> jfarah - dnakamashi - alteração feita para que o calllog
		
		return fromAddress;
	}
	
    public RouteInfo verifyCorrectRouteInfo(Long ownerKey, RouteInfo routeInfo)
    {
        if(routeInfo.hasPreviousRouteInfo()&&routeInfo.getPreviousRouteInfo().getToLeg().isPbxuser() && routeInfo.getFromLeg().isPbxuser())
        {
            boolean isOwner = routeInfo.getPreviousRouteInfo().getFromLeg().isPbxuser() && ownerKey.equals(routeInfo.getPreviousRouteInfo().getFromLeg().getPbxuser().getKey());
        	Config config  = routeInfo.getPreviousRouteInfo().getToLeg().isPbxuser() ? routeInfo.getPreviousRouteInfo().getToLeg().getPbxuser().getConfig() : routeInfo.getPreviousRouteInfo().getToLeg().isGroup() ? routeInfo.getPreviousRouteInfo().getToLeg().getGroup().getConfig() : null;
            if(config.getForwardType().equals(Config.FORWARD_TYPE_TO))
            {
            	//jluchetta - Correção para resgatar previous route, remoção de parte da condição do if (BUG ID: 5213)
                if(routeInfo.getFromLeg().getPbxuser().getKey().equals(routeInfo.getPreviousRouteInfo().getToLeg().getPbxuser().getKey()))
                    return routeInfo.getPreviousRouteInfo();
             
            }else if(isOwner || (!isOwner && routeInfo.getFromLeg().getPbxuser().getKey().equals(routeInfo.getPreviousRouteInfo().getToLeg().getPbxuser().getKey())))//from display
                return routeInfo.getPreviousRouteInfo();              
        }           
        return routeInfo;
    }
    
    private boolean isPBXVoicemailEnable(Pbx pbx)
    {
    	return pbx.getPbxPreferences().getVmEnable().equals(Pbxpreference.VM_ENABLE_ON);
    }
    
    public boolean verifyReverseReturnCall(SipAddressParser from, SipAddressParser to, SipAddressParser returnAddress) throws DAOException
    {
    	  Address fromAddress= addManager.getAddressWithPbxuserAndConfig(from.getExtension());
		   if(fromAddress==null || (fromAddress!=null && !fromAddress.getType().equals(Address.TYPE_DID)))
		       fromAddress= addManager.getAddressWithPbxuserAndConfig(from.getExtension(), from.getDomain());
		    
		       
		   Address toAddress= addManager.getAddressWithPbxuserAndConfig(to.getExtension());
		   if(toAddress==null || (toAddress!=null && !toAddress.getType().equals(Address.TYPE_DID)))			  
		       toAddress= addManager.getAddressWithPbxuserAndConfig(to.getExtension(), to.getDomain());
		   
		   boolean isReverse = false;
		   Address returnAdd = addManager.getAddressWithPbxuserAndConfig(returnAddress.getExtension(), returnAddress.getDomain());
		   Pbxuser pbxUserFrom = fromAddress!=null ? fromAddress.getPbxuser() : null;
		   Pbxuser pbxUserReturn = returnAdd != null ? returnAdd.getPbxuser() : null;
		   Pbxuser pbxUserTo = toAddress != null ? toAddress.getPbxuser() : null;
		   boolean isFromPark = pbxUserFrom!= null && pbxUserReturn != null && pbxUserFrom.getKey().equals(pbxUserReturn.getKey());
		   boolean isOnNetFromPBX = fromAddress != null && fromAddress.getPbxKey() != null && pbxUserReturn != null && pbxUserTo != null && !pbxUserTo.getKey().equals(pbxUserReturn.getKey());
		   boolean isOnnetToPBX  = toAddress != null && toAddress.getPbxKey() != null && pbxUserReturn != null && pbxUserFrom != null && pbxUserFrom.getKey().equals(pbxUserReturn.getKey());
		   Domain rootDomain = dmDAO.getRootDomain();
		   
		   boolean isToPSTN = rootDomain.getDomain().equals(to.getDomain());
		   
		   if(isFromPark ||isOnnetToPBX  || isOnNetFromPBX || isToPSTN)
		       isReverse = true;
		 
		   return isReverse;
    }
}