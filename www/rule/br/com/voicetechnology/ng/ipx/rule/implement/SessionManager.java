package br.com.voicetechnology.ng.ipx.rule.implement;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import br.com.voicetechnology.ng.ipx.commons.exception.VoiceException;
import br.com.voicetechnology.ng.ipx.commons.exception.authentication.InvalidLoginException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.UserWithoutSipSessionlogException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.PbxAlreadyRegistered;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.SessionManagerException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.SipsessionRegisterDenied;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.jms.tools.JMSNotificationTools;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.ActivecallDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GatewayDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserterminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipsessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.EventsubscriptionDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.PermissionDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RoleDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.SessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserfileDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Target;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.mwi.MWIEventInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.RouteType;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuserterminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Eventsubscription;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.LoginInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SipsessionlogInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserSessionInfo;
import br.com.voicetechnology.ng.ipx.pojo.registrar.UserRegisterExpiresInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.pojo.ws.WebServiceConstantValues;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class SessionManager extends Manager
{
	private int MAX_SEQUENCE_VALUE = 10000;
	private int MIN_SEQUENCE_VALUE = 1;
	
	private SipsessionlogDAO sslDAO;
	private SessionlogDAO slDAO;
	private PbxDAO pbxDAO;
	private UserDAO userDAO;
	private PbxuserDAO puDAO;
	private PbxuserterminalDAO ptDAO;
	private DomainDAO domainDAO;
	private RoleDAO roleDAO;
	private PreferenceDAO preferenceDAO;
	private EventsubscriptionDAO evtSubscriptionDAO;
	private ActivecallDAO activeCallDAO;
	private GatewayDAO gtDAO;
	private PbxpreferenceDAO pbxPrefDAO;
	private UserfileDAO ufDAO;
	private TerminalDAO tDAO;
	private PermissionDAO permissionDAO;
	private AddressDAO addressDAO;

	public SessionManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		sslDAO = dao.getDAO(SipsessionlogDAO.class);
		slDAO = dao.getDAO(SessionlogDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		puDAO = dao.getDAO(PbxuserDAO.class);
		userDAO = dao.getDAO(UserDAO.class);
		ptDAO = dao.getDAO(PbxuserterminalDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
		roleDAO = dao.getDAO(RoleDAO.class);
		preferenceDAO = dao.getDAO(PreferenceDAO.class);
		evtSubscriptionDAO = dao.getDAO(EventsubscriptionDAO.class);
		activeCallDAO = dao.getDAO(ActivecallDAO.class);
		pbxPrefDAO = dao.getDAO(PbxpreferenceDAO.class);
		ufDAO = dao.getDAO(UserfileDAO.class);
		tDAO = dao.getDAO(TerminalDAO.class);
		gtDAO = dao.getDAO(GatewayDAO.class);
		permissionDAO = dao.getDAO(PermissionDAO.class);
		addressDAO = dao.getDAO(AddressDAO.class);
		
	}

	public boolean hasSipsessionlogByPbxuser(Long pbxuserKey) throws DAOException
	{
		//if(isTerminal)
		//{
		List<Pbxuserterminal> ptList = ptDAO.getPbxuserterminalByPbxuser(pbxuserKey);
		for(Pbxuserterminal pt : ptList)
			if(sslDAO.getHowManySipSessionByPbxuser(pt.getTerminal().getPbxuserKey()).longValue() > 0L)
				return true;
		//}
		return sslDAO.getHowManySipSessionByPbxuser(pbxuserKey).longValue() > 0L;
	}
	
	public void getContactList(RouteInfo routeInfo, List<Target> targetList) throws DAOException
	{
		if(targetList == null || targetList.size() == 0)
			return ;
		
		RouteType route = routeInfo.getToLeg().getRouteType();
		
		if(route.equals(RouteType.STATION) && !routeInfo.getToLeg().isMediaLeg())
			makeStationTargets(targetList);
		else
		{
			if(routeInfo.getToLeg().isMediaLeg())
				makeMediaTargets(targetList);
			else
				makeOutgoingTargets(route, routeInfo.getToLeg().getSipAddress().getExtension(), targetList);
			
			if(route.equals(RouteType.PSTN) || route.equals(RouteType.PBX_FAILURE) || routeInfo.getToLeg().isMediaLeg())
				feedOriginalTargetList(routeInfo.getTargetList(), targetList);
		}
	}	
	
	private void feedOriginalTargetList(List<Target> originalTargetList, List<Target> targetList)
	{
		if(targetList.size() < 1)
			return ;
		
		List<Target> subList = targetList.subList(1, targetList.size());
		originalTargetList.addAll(0, subList);
		subList.clear();
	}
	
	private void makeStationTargets(List<Target> targetList) throws DAOException
	{
		for (int i = 0; i < targetList.size(); i++)
		{
			Target t = targetList.get(i);
			List<Sipsessionlog> sslList = lookupPbxuser(t.getUsername(), t.getDomain());
			
			if(sslList.size() > 0)
				targetList.get(i).addSipsessionlogList(sslList);
			else
				targetList.remove(i--);
		}
	}
	
	private void makeOutgoingTargets(RouteType route, String toExtension, List<Target> targetList) throws DAOException
	{
		Target t = targetList.size() == 0 ? null : targetList.remove(0);
		
		if(t.isLoad())
			targetList.add(0, t);
		else if(route.equals(RouteType.PSTN) || route.equals(RouteType.PBX_FAILURE))
			targetList.addAll(lookupGateway(toExtension, t));
		else if(route.equals(RouteType.ON_NET))
			targetList.add(lookupPBX(toExtension, t));
	}
	
	public void makeMediaTargets(List<Target> targetList) throws DAOException
	{
		Target t = targetList.size() == 0 ? null : targetList.remove(0);
			
		if(t.isLoad())
			targetList.add(0, t);
		else
			targetList.addAll(lookupMedia(t));
	}	
	
	private List<Target> lookupGateway(String toExtension, Target t) throws DAOException
	{
		List<String> contactList = sslDAO.getContactListUsingLoadBalance(t.getUsername(), t.getDomain());
		if(contactList.size() == 0)
			return Collections.emptyList();
		
		if(t.getPrefix() != null && t.getPrefix().length() > 0)
			toExtension = t.getPrefix() + toExtension;
		
		return makeTargetList(contactList, toExtension, t);
	}
	
	private List<Target> lookupMedia(Target t) throws DAOException
	{
		List<String> contactList = sslDAO.getContactListUsingLoadBalance(t.getUsername(), t.getDomain());
		if(contactList.size() == 0)
			return Collections.emptyList();
		return makeTargetList(contactList, null, t);
	}
	
	private Target lookupPBX(String toExtension, Target t) throws DAOException
	{
		Pbx pbx = pbxDAO.getPbxFullByDomain(t.getDomain());
		List<Sipsessionlog> sslList = sslDAO.getActiveSipsessionByUsernameAndDomain(pbx.getUser().getUsername(), t.getDomain());
		if(sslList.size() == 0)
			return t;
		makeContact(sslList, toExtension);
		t.addSipsessionlogList(sslList);
		return t;
	}
	
	private void makeContact(List<Sipsessionlog> sslList, String toExtension)
	{
		Sipsessionlog sslPBX = sslList.get(0);
		sslList.clear();
		SipAddressParser sipPBX = new SipAddressParser(sslPBX.getContact());
		Sipsessionlog ssl = new Sipsessionlog();
		ssl.setContact(makeANI(toExtension, sipPBX.getDomain(), sipPBX.getPort()));
		ssl.setOriginalContact(sslPBX.getOriginalContact());
		sslList.add(ssl);
	}

	private String makeANI(String toExtension, String domain, String port)
	{
		return new SipAddressParser(toExtension, domain, port).toString();
	}
	
	private List<Sipsessionlog> lookupPbxuser(String username, String domain) throws DAOException
	{
		List<Sipsessionlog> sslList = sslDAO.getActiveSipsessionByUsernameAndDomain(username, domain);
		Pbxuser pu = puDAO.getPbxuserByAddressAndDomain(username, domain);
		if(pu.getUser().getAgentUser().intValue() == User.TYPE_TERMINAL)
			return sslList;
		List<Pbxuserterminal> ptList = ptDAO.getPbxuserterminalByPbxuser(pu.getKey());
		for(Pbxuserterminal pt : ptList)
			sslList.addAll(sslDAO.getActiveSipsessionlogListByPbxuser(pt.getTerminal().getPbxuserKey()));
		return sslList;
	}

	//IVR
	public Target lookupSipSession(String address, String domain) throws DAOException
	{
		return lookupPBX(address, new Target(address, domain));
	}
	
	private List<Target> makeTargetList(List<String> contactList, String toExtension, Target t)
	{
		List<Target> targetList = new ArrayList<Target>();
		for(String contact : contactList)
		{
			Sipsessionlog tmp = new Sipsessionlog();
			SipAddressParser sipTo = new SipAddressParser(contact);
			tmp.setOriginalContact(contact);
			toExtension = toExtension != null ? toExtension : sipTo.getExtension();
			tmp.setContact(makeANI(toExtension, sipTo.getDomain(), sipTo.getPort()));			
			targetList.add(new Target(t.getUsername(), t.getDomain(), t.getPrefix(), tmp));
		}
		
		return targetList;
	}
	
	/**
	 * Seleciona as sessoes web abertas e verfica
	 * se devem ser expiradas (fechadas)
	 * @throws DAOException
	 * @throws ValidateObjectException 
	 */
	public long closeExpiredSessions(int timeout) throws DAOException, ValidateObjectException
	{
		long closedWebSessions = 0;
		try
		{
			logger.debug("Closing expired Web sessions");
			long expired = Integer.parseInt(IPXProperties.getProperty(IPXPropertiesType.WEB_SESSION_TIMEOUT)) * 1000L;
			closedWebSessions = slDAO.updateClosedUserSessionlog(expired);
			logger.debug(new StringBuilder("Closed ").append(closedWebSessions).append("expired Web sessions"));
			
		} catch(Exception e)
		{
			logger.error("Fail closing expired Web sessions", e);
		}
		return closedWebSessions;
	}
	
	/**
	 * Seleciona as sessoes sip abertas e verfica
	 * se devem ser expiradas (fechadas)
	 * @throws DAOException
	 * @throws ValidateObjectException 
	 * @throws DeleteDependenceException 
	 */
	public long closeExpiredSipSessions() throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		long closedSipSessions = 0;

		//jluchetta: correção do problema de insconsistencia entre sip session e session log, exceção tratada na fachada
		closedSipSessions += closeSession();
		logger.debug(new StringBuilder("Closed ").append(closedSipSessions).append("expired Sip sessions"));
		
		return closedSipSessions;
	}
	
	private long closeSession() throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		long timeout = Integer.parseInt(IPXProperties.getProperty(IPXPropertiesType.SIP_SESSION_TIMEOUT)) * 1000L;
		StringBuilder query = sslDAO.getOpenedSipsessionlogList(timeout);

		return closeSession(query);		
	}
	
	private long closeSession(StringBuilder query) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		//jluchetta alteração para passar uma data comum para todas as queries 
		//q fazem os procedimentos de fechar as sessões sips e seus dependentes
		Long now = System.currentTimeMillis();
		logger.debug("Closing expired sip sessions");
		
		//jluchetta: alteração para generalizar a query deleta os events subscriptions -versão 3.0.4
		String queryAllClosed = query.toString();
		String queryNotClosed = query.append(" AND sl.DTM_SESSIONEND is null ").toString();
		
		evtSubscriptionDAO.removeBySipsessionlog(queryAllClosed, now);
		slDAO.updateClosedSessionlog(queryNotClosed, now);
		
		return sslDAO.removeSipSessionLog(queryNotClosed, now);
	}
	
	/*jluchetta: Método não verifica mais se passou do número máximo de sip session, ele simplismente atualiza uma sip sesssion ou cria, 
	as outras verificações são feitas pelo método verifyRegisterExpire que é executado antes deste método (Bug ID 5682 -  3.0.2-RC12_patch4)*/
	public void registerSipUser(String username, String domain, String contact, String originalContact, String userAgent, int expires, Long userKey) throws VoiceException, DAOException, ValidateObjectException
	{
		
		Sipsessionlog ssl  = sslDAO.getSipsessionlogByUserKeyAndContact(userKey,contact);
	 	
		boolean createNewSipsession = true;	
		if(ssl != null)
		{
			
			//inicio --> dnakamashi - bug #7379 - version 3.2.8 - 6/Jun/2011
			//Quando um REGISTER chega com um original contact diferente do antigo, a sipsession não era atualizada 
			//com o original contact novo			
			if(originalContact != null && !ssl.getOriginalContact().equals(originalContact))
				ssl.setOriginalContact(originalContact);
			//fim --> dnakamashi - bug #7379 - version 3.2.8 - 6/Jun/2011			
			
			updateSipSessionLogLastUse(ssl);
			ssl.getExpires();
			createNewSipsession = false;
		}

		if(createNewSipsession)				
			createSipSessionLog(username, domain, contact, originalContact, userAgent, expires, userKey);
	}
	/**
	 * Remove sessao atual do usuario pelo username e domain, e cria uma nossa sessao baseada no contact passado.
	 * @param username
	 * @param domain
	 * @param contact
	 * @param originalContact
	 * @param userAgent
	 * @param expires
	 * @param userKey
	 * @throws VoiceException
	 * @throws DAOException
	 * @throws ValidateObjectException
	 */
	public void registerSipUserAndRemoveCurrentSession(String username, String domain, String contact, String originalContact, String userAgent, int expires) throws VoiceException, DAOException, ValidateObjectException
	{
		User user = userDAO.getUserByUsernameAndDomain(username, domain);
		Sipsessionlog ssl  = sslDAO.getSipsessionlogByUserKeyAndContact(user.getKey(),contact);
	 	
		boolean createNewSipsession = true;	
		if(ssl != null)
		{
			updateSipSessionLogLastUse(ssl);
			ssl.getExpires();
			createNewSipsession = false;
		}
						
		if(createNewSipsession)
		{
			removeSipSessionByUsernameAndDomain(username, domain); // remocao de possivel session criada com outro contact
			createSipSessionLog(username, domain, contact, originalContact, userAgent, expires, user.getKey());
		}
		
	}
	
	private boolean verifyNumberOfSipSessions(String username, String domain, int currentActiveCount) throws DAOException
	{
		User user = userDAO.getUserByUsernameAndDomain(username, domain);
		
		boolean isTerminal = user.getAgentUser().equals(User.TYPE_TERMINAL);
		Pbxuser pbxUser = null;
		int activeSipSessionsCount = 0;
		activeSipSessionsCount = currentActiveCount;
		
		if(isTerminal)
		{
			Terminal terminal = tDAO.getTerminalByAddressAndDomain(username, domain);		
			if(terminal != null)
			{
				activeSipSessionsCount = sslDAO.getHowManyActiveSipSessionByPbxuser(terminal.getPbxuserKey());
				if(activeSipSessionsCount >= Terminal.MAX_SIPSESSION)
					return true;
			}
			
			pbxUser = puDAO.getPbxuserByTerminal(username, domain);	
			if(pbxUser != null)					
				activeSipSessionsCount = sslDAO.getHowManyActiveSipSessionByPbxuser(pbxUser.getKey());				
		}
		else
		{
			pbxUser = puDAO.getPbxuserByUsernameAndDomain(username, domain);
			
			if(pbxUser != null)					
				activeSipSessionsCount = sslDAO.getHowManyActiveSipSessionByPbxuser(pbxUser.getKey());
		}
		
		Pbxpreference pbxPref = pbxPrefDAO.getByDomain(domain);
		
		return pbxPref != null && activeSipSessionsCount >= pbxPref.getMaxSipsessions().intValue();
	}

	private void updateSipSessionLogLastUse(Sipsessionlog sipsessionlog) throws DAOException, ValidateObjectException 
	{
		sipsessionlog.getSessionlog().setSessionLastUse(Calendar.getInstance());
		slDAO.save(sipsessionlog.getSessionlog());
	}

	public User getUserByUsernameAndDomain(String username, String domain) throws DAOException
	{
		return userDAO.getUserByUsernameAndDomain(username, domain);
	}
	
	private void createSipSessionLog(String username, String domain, String contact, String originalContact, String userAgent, int expires, Long userKey) throws DAOException, ValidateObjectException
	{
		
		//se encontrou o usuário cria uma sessão do tipo sip e a sipsession propriamente dita
		Sessionlog sessionlog = new Sessionlog();
		Calendar now = Calendar.getInstance();
		sessionlog.setSessionLastUse(now);
		sessionlog.setSessionStart(now);
		sessionlog.setUserKey(userKey);
		sessionlog.setType(Sessionlog.TYPE_SIP_SESSION);
		
		slDAO.save(sessionlog);
		
		Sipsessionlog sipsessionlog = new Sipsessionlog();
		sipsessionlog.setContact(contact);
		sipsessionlog.setExpires(expires);
		sipsessionlog.setUserAgent(userAgent);
		sipsessionlog.setSessionlogKey(sessionlog.getKey());
		sipsessionlog.setOriginalContact(originalContact);
		sipsessionlog.setSessionLastHit(now);
		
		sslDAO.save(sipsessionlog);
	}
	
	/* Método possui lógica de verificar número de sip session e corrige problema para não limitar sessão de agent de media*/
	public UserRegisterExpiresInfo verifyRegisterExpire(String username, String domain, String contact,  int expires)throws VoiceException, DAOException 
	{
		User user = userDAO.getUserByAddressAndDomain(username, domain);
		if (user == null)
			user = userDAO.getUserByUsernameAndDomain(username, domain);
				
		//se não econtra o user notifica gera o erro e retorna
		if (user == null) 
			throw new SessionManagerException("Cannot register, user not found:" + username + "@" + domain);
		
		List<Sipsessionlog> activeSipSessions  = sslDAO.getActiveSipsessionByUsernameAndDomain(username, domain);
		
		boolean isNewSession = true;
		for(Sipsessionlog ssl: activeSipSessions)
		{
			//se já existe uma sessão para um UAC e este agent é um pbxdeve ser gerado um erro, esta regra é usada no esquema de fail-over do serviço PBXServer
			if(ssl.getSessionlog().getUser().getAgentUser().intValue() == User.TYPE_PBX && !ssl.getContact().equals(contact))
				throw new PbxAlreadyRegistered("Cannot register, this is a pbx and is already registered :"	+ username + "@" + domain);
			else if(ssl.getContact().equals(contact))
			{
				updateSipSessionLogLastUse(ssl);
				ssl.getExpires();
				isNewSession = false;		
				break;
			}
		}		
		
		if(isNewSession && (user.getAgentUser()==User.TYPE_PBXUSER || user.getAgentUser()==User.TYPE_SIPTRUNK || user.getAgentUser()==User.TYPE_TERMINAL) && this.verifyNumberOfSipSessions(username, domain, activeSipSessions.size()))
			throw new SipsessionRegisterDenied(new StringBuilder("Coudn't register contact: ").append(contact).append(", user ").append(username).append(" already has ").append(activeSipSessions.size()).
																		append(" sip sessions, please unregister another one!").toString());
		
		if(user.getAgentUser() != User.TYPE_GATEWAY)
		{
			if(user.getAgentUser() == User.TYPE_PBXUSER || user.getAgentUser() == User.TYPE_TERMINAL || user.getAgentUser() == User.TYPE_SIPTRUNK)
				expires = Integer.parseInt(IPXProperties.getProperty(IPXPropertiesType.SIP_SESSION_USER_AGENT_EXPIRES));
			else 			
				expires =  Integer.parseInt(IPXProperties.getProperty(IPXPropertiesType.SIP_SESSION_SYSTEM_AGENT_EXPIRES));
		}
				
		expires = user.getAgentUser() == User.TYPE_GATEWAY ? Sipsessionlog.GATEWAY_EXPIRES_DEFAULT : expires;
		
		return new UserRegisterExpiresInfo(user,expires);
	}
	
	private boolean isGatewayOrMedia(int agentType)
	{
		return agentType == User.TYPE_GATEWAY || 
			   agentType == User.TYPE_MUSIC_SERVER || 
			   agentType == User.TYPE_PARK_SERVER || 
			   agentType == User.TYPE_VOICEMAIL ||
			   agentType == User.TYPE_IVR;
	}
	

	public void unregisterSipUser(String contact) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		if(logger.isDebugEnabled())
			logger.debug(new StringBuilder("Unregistering contact ").append(contact));
		Sipsessionlog sipsessionlog = sslDAO.getSipsessionlogByContact(contact);
		if(sipsessionlog != null)
			removeSipsessionlog(sipsessionlog);
		else
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Could not unregister contact ").append(contact).append(" sipsession not found."));
			
	}
	
	public void unregisterSipUser(String contact, String domain) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		if(logger.isDebugEnabled())
			logger.debug(new StringBuilder("Unregistering contact ").append(contact).append(" on domain ").append(domain));
		
		Sipsessionlog sipsessionlog = sslDAO.getSipsessionlogByContactAndDomain(contact, domain);
		
		if(sipsessionlog != null)
			removeSipsessionlog(sipsessionlog);
		else
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Could not unregister contact ").append(contact).append(" on domain ").append(domain).append(" sipsession not found."));
			
	}
	
	public void removeSipSessionByUsernameAndDomain(String username, String domain) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		if(logger.isDebugEnabled())
			logger.debug(new StringBuilder("Removing sipsessionlog by username: ").append(username).append(" and domain: ").append(domain));

		Sipsessionlog sipsessionlog = sslDAO.getSipsessionlogByUserAndDomain(username, domain);

		if(sipsessionlog != null)
			removeSipsessionlog(sipsessionlog);
		else
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Could not remove sipsessionlog by username: ").
								append(username).append(" and domain: ").append(domain).append(", sipsession not found."));
	}
	
	public void removeAllSessionByFarmIP(String farmIP) throws DAOException
	{
		String queryClose = sslDAO.getAllSipsessionsByFarmIPQuery(farmIP);
		slDAO.updateMediaSessionlog(queryClose);
		sslDAO.removeMediaSipSessionLog(queryClose);
	}
		
	public ReportResult findSipsessionlogs(Report<SipsessionlogInfo> report)  throws DAOException
	{
		ReportDAO<Sipsessionlog, SipsessionlogInfo> sslogReport = dao.getReportDAO(SipsessionlogDAO.class);
		Long size = sslogReport.getReportCount(report);
		List<Sipsessionlog> sipsessionlogList = sslogReport.getReportList(report);
		List<SipsessionlogInfo> sipsessionlogInfoList = new ArrayList<SipsessionlogInfo>();
		Long domainKey = report.getInfo().getDomainKey();
		Domain domain = domainKey != null ? domainDAO.getByKey(domainKey) : domainDAO.getRootDomain();
		List<Duo<Long, String>> domainList = domainDAO.getDomainsByRootDomain(domain.getRootKey() != null ? domain.getRootKey() : domain.getKey());
		
		for(Sipsessionlog sslog : sipsessionlogList)
		{
			SipsessionlogInfo info;
			if(sslog.getSessionlog().getUser().getAgentUser() != User.TYPE_PBX)
			{
				List<String> extensionList = new ArrayList<String>();
				
				if(sslog.getSessionlog().getUser().getAgentUser() == User.TYPE_TERMINAL)
				{
					User u = tDAO.getAssociatedPbxuserByTerminalUserKey(sslog.getSessionlog().getUserKey());
					if(u != null)
						extensionList.add(u.getUsername());
					
				}else				
					extensionList = addressDAO.getExtensionListByUser(sslog.getSessionlog().getUserKey());
				
				info = new SipsessionlogInfo(sslog, sslog.getSessionlog().getUser().getUsername(), domainList, sslog.getSessionlog().getUser().getDomain().getDomain(), extensionList);
			}else
			{
				info = new SipsessionlogInfo(sslog, sslog.getSessionlog().getUser().getUsername(), domainList, sslog.getSessionlog().getUser().getDomain().getDomain());
			}
			sipsessionlogInfoList.add(info);
		}
		/*jluchetta - Início - Correcao do problema de combo de dominio vazia do wba quando nao ha registro
    	*/
		if(sipsessionlogInfoList.size() == 0)
		{
			SipsessionlogInfo info = new SipsessionlogInfo(domainList);
			sipsessionlogInfoList.add(info);
		}
		return new ReportResult<SipsessionlogInfo>(sipsessionlogInfoList, size);
	}
	
	public void deleteSipsessionlogs(List<Long> sipsessionlogKeyList, boolean isFromWeb) throws DAOException, ValidateObjectException, DeleteDependenceException 
	{
		for(Long key : sipsessionlogKeyList) 
			this.removeSipsessionlog(key, isFromWeb);
	}

	private void removeSipsessionlog(Long key, boolean isFromWeb) throws DAOException, ValidateObjectException, DeleteDependenceException
	{		
		Sipsessionlog sslog = sslDAO.getSipsessionlogAndSessionlog(key);
		
		if(isFromWeb && sslog.getSessionlog().getUser() != null)
			if(isSystemAgent(sslog.getSessionlog().getUser()))
					throw new DeleteDependenceException("It's not possible to delete a System Agent");
					
		sslog.setSessionlog(sslog.getSessionlog());
		removeSipsessionlog(sslog);
	}
			
	private boolean isSystemAgent(User user)
	{
		if(user.getAgentUser() == User.TYPE_PBXUSER || user.getAgentUser() == User.TYPE_TERMINAL || user.getAgentUser() == User.TYPE_SIPTRUNK || user.getAgentUser() == User.TYPE_GATEWAY)
			return false;
		return true;
	}
	
	public SipsessionlogInfo getSipsessionlogInfoByKey(Long sipKey) throws DAOException, ValidationException
	{
		return this.getSipsessionlogInfo(sipKey, false);
	}
	
	private SipsessionlogInfo getSipsessionlogInfo(Long sipkey, boolean onlyView) throws DAOException, ValidationException
	{
		Sipsessionlog sip = sslDAO.getSipsessionlogAndSessionlog(sipkey);
		if(!onlyView)
			if(sip.getContact().equals(Sessionlog.TYPE_SIP_SESSION) || sip.getContact().equals(Sessionlog.TYPE_USER_SESSION) || sip.getContact().equals(Sessionlog.DEFINE_DELETED))
				throw new ValidationException("Cannot delete this SipSession because its a reserved sip on system!!!", WebServiceConstantValues.RESULT_CODE_ROLE_IS_RESERVED_ON_SYSTEM);
		SipsessionlogInfo sipsessionInfo = new SipsessionlogInfo();
		sipsessionInfo.setContact(sip.getContact());
		sipsessionInfo.setDomainKey(sip.getSessionlog().getUser().getDomainKey());
		sipsessionInfo.setIpPhone(sip.getUserAgent());
		sipsessionInfo.setStart(sip.getSessionlog().getSessionStart());
		sipsessionInfo.setLastUse(sip.getSessionlog().getSessionLastUse());
		sipsessionInfo.setExpires(sip.getExpires());
		return sipsessionInfo;
		
	}
	private void removeSipsessionlog(Sipsessionlog sslog) throws DAOException, ValidateObjectException, DeleteDependenceException 
	{
		Sessionlog slog = sslog.getSessionlog();
		
		List<Duo<Activecall, Address>> activeCallList = activeCallDAO.getActivecallListBySipSessionLog(sslog.getKey());
		
		if(activeCallList != null && activeCallList.size() > 0) 
		{
		    StringBuilder sb = new StringBuilder("Cannot delete SIP session log ");
		    sb.append(activeCallList.get(0).getSecond().getAddress());
		    sb.append(". There is(are) ").append(activeCallList.size());
		   sb.append(" active call(s) for this session, Active Call IDs: ");
		    for(Duo<Activecall, Address> acDuo : activeCallList)
		    	sb.append(acDuo.getFirst().getCallID() +" ");
			
		    throw new DeleteDependenceException(sb.toString(), Activecall.class, 
		            activeCallList.size(), activeCallList.get(0).getSecond());
		}

		List<Eventsubscription> eventSubscriptionList = evtSubscriptionDAO.getBySipsessionlog(sslog.getKey());
		for(Eventsubscription evtSub: eventSubscriptionList)
			evtSubscriptionDAO.remove(evtSub);
		
		if(logger.isDebugEnabled())
			logger.debug(new StringBuilder("Closing Session, sessionlogkey ").append(slog.getKey()).append(" contact: ").append(sslog.getContact()));
		
		slog.setSessionEnd(Calendar.getInstance()); 
		slDAO.save(slog);
				
		sslDAO.remove(sslog);
		
		//Valida número de sessões sip online
		if (slog.getUser().getPbxuser() != null && // Remocao de sessao sip de PBX
				!(sslDAO.getHowManySipSessionByPbxuser(slog.getUser().getPbxuser().getKey()) > 0) && slog.getUser().getAgentUser() == User.TYPE_PBXUSER)
		//dsakuma/rribeiro - Notificação JMS para AcdCallCenter acerca de sipsession deletada - início
		JMSNotificationTools.getInstance().sendRemoveSipSessionJMSMessage(slog.getUser().getPbxuser().getKey(), slog.getUser().getDomainKey());
		//dsakuma/rribeiro - Notificação JMS para AcdCallCenter acerca de sipsession deletada - fim
		
		if(logger.isDebugEnabled())
			logger.debug(new StringBuilder("Session closed, sessionlogkey ").append(slog.getKey()).append(" contact: ").append(sslog.getContact()));
	}

	public void closeUserSession(Long sessionlogKey) throws DAOException, ValidateObjectException
	{
		Sessionlog slog = slDAO.getByKey(sessionlogKey);
		slog.setSessionEnd(Calendar.getInstance());
		slDAO.save(slog);
		if(slog.getType() == Sessionlog.TYPE_SIP_SESSION)
		{
			Sipsessionlog sslog = sslDAO.getSipsessionlogBySessionlog(sessionlogKey);
			sslDAO.remove(sslog);
		}
	}

	public UserSessionInfo createUserSession(LoginInfo loginInfo) throws DAOException, ValidateObjectException, InvalidLoginException
	{
		String username = loginInfo.getUsername();
		String password = loginInfo.getPassword();
		String domainName = loginInfo.getDomain();
		
		Pbxuser pbxuser = puDAO.getPbxuserByUsernameAndDomain(username, domainName);
		if(pbxuser == null)
		{	
			User user = userDAO.getUserCentrexAdmin(username, domainName);
			if(user != null)
			{
				if(user.getPassword().equals(password))
				{
					//TODO retirar essa duplicidade de c�digo depois que a implementa��o estiver est�vel
					Calendar now = Calendar.getInstance();
					Sessionlog sessionlog = new Sessionlog();
					sessionlog.setSessionStart(now);
					sessionlog.setSessionLastUse(now);
					sessionlog.setUserKey(user.getKey());
					sessionlog.setType(Sessionlog.TYPE_USER_SESSION);
					slDAO.save(sessionlog);
					
					//user.setRoleList(roleDAO.getRoleListByPbxuser(pbxuser.getKey()));
					user.setPreference(preferenceDAO.getPreferenceByUser(user.getKey()));
					return new UserSessionInfo(sessionlog, user);
				} else
					throw new InvalidLoginException("Wrong password for user " + username + " on domain " + domainName, InvalidLoginException.Type.PASSWORD);
			} else
				throw new InvalidLoginException("User " + username + " not found on domain " + domainName, InvalidLoginException.Type.USER);
			
		}	
		if(pbxuser != null)
		{
			User user = pbxuser.getUser();
			if(user.getPassword().equals(password))
			{
				Calendar now = Calendar.getInstance();
				Sessionlog sessionlog = new Sessionlog();
				sessionlog.setSessionStart(now);
				sessionlog.setSessionLastUse(now);
				sessionlog.setUserKey(user.getKey());
				sessionlog.setType(Sessionlog.TYPE_USER_SESSION);
				slDAO.save(sessionlog);
				
				user.setRoleList(roleDAO.getRoleListByPbxuser(pbxuser.getKey()));
				user.setPreference(preferenceDAO.getPreferenceByUser(user.getKey()));
				Domain domain = user.getDomain();
				Pbx pbx = pbxDAO.getPbxByDomain(domain.getKey());
				String loginTime = getLoginTime(loginInfo.getTimezone(), user.getPreference().getLocale());
				return new UserSessionInfo(sessionlog, user, pbxuser, pbx, domain, loginTime);
			} else
				throw new InvalidLoginException("Wrong password for user " + username + " on domain " + domainName, InvalidLoginException.Type.PASSWORD);
		} else
			throw new InvalidLoginException("User " + username + " not found on domain " + domainName, InvalidLoginException.Type.USER);
	}

	private String getLoginTime(String GMTTimezone, String localeString)
	{
		TimeZone timeZone = TimeZone.getTimeZone(GMTTimezone);
		Locale locale = new Locale(localeString.split("_")[0]);
		Calendar calendar = Calendar.getInstance(timeZone, locale);
		
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
		dateFormat.setTimeZone(timeZone);
		String date = dateFormat.format(calendar.getTime());
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		String time = (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second); 

		return date + " " + time;
	}

	public List<Duo<Long, String>> getDomainsByRootDomain(Long domainRootKey) throws DAOException
	{
		List<Duo<Long, String>> domainList = domainDAO.getDomainsByRootDomain(domainRootKey);
		if(domainList != null && domainList.size() > 0)
			return domainList;
		else
			return null;
	}
	
	public boolean validateSession(Long sessionlogKey) throws DAOException, ValidateObjectException
	{
		Sessionlog sessionlog = slDAO.getByKey(sessionlogKey);
		if(sessionlog == null || sessionlog.getSessionEnd() != null)
			return false;
		sessionlog.setSessionLastUse(Calendar.getInstance());
		return true;
	}
	
	public boolean addSubscription(String contact, String event) throws DAOException, ValidateObjectException, UserWithoutSipSessionlogException
	{
		return addSubscription(contact, event, null);
	}
	
	public boolean addSubscription(String contact, String event, String parameters) throws DAOException, ValidateObjectException, UserWithoutSipSessionlogException
	{
		Sipsessionlog ssl =  sslDAO.getSipsessionlogByContact(contact);
		boolean isNewSubscription = false;
		if (ssl != null)
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Sipsessionlog found, subscribing contact: ").append(contact));
			
			Eventsubscription evtSubscription = evtSubscriptionDAO.getBySipsessionlogAndEvent(ssl.getKey(), event);
			
			if (evtSubscription == null)
			{
				evtSubscription = new Eventsubscription();
				evtSubscription.setEvent(event);
				evtSubscription.setSipsessionlogKey(ssl.getKey());
				evtSubscription.setSequence(0);
				isNewSubscription = true;
			}
			
			evtSubscription.setParameters(parameters);
			evtSubscriptionDAO.save(evtSubscription);
		} else
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Sipsessionlog not found when subscribing contact: ").append(contact));
			throw new UserWithoutSipSessionlogException(contact, 0);
		}
		
		return isNewSubscription;
	}
	
	public long getNumberOfSusbcription() throws DAOException
	{
		return evtSubscriptionDAO.getNumberOfSusbcription();
	}
	
	public void saveSubscrition(Eventsubscription subscription) throws DAOException, ValidateObjectException
	{
		evtSubscriptionDAO.save(subscription);
	}
	
	public boolean removeSubscription(String contact, String event) throws DAOException, ValidateObjectException
	{
		boolean removed = false;
		Sipsessionlog ssl =  sslDAO.getSipsessionlogByContact(contact);
		if (ssl != null)
		{
			Eventsubscription evtSubscription = evtSubscriptionDAO.getBySipsessionlogAndEvent(
					ssl.getKey(), event);
			
			if (evtSubscription != null)
			{
				evtSubscriptionDAO.remove(evtSubscription);
				removed = true;
			}
		}
		return removed;
	}
	
	public List<Eventsubscription> getEventSubscriptionByDomain(String eventType, String domain) throws DAOException 
	{
		return evtSubscriptionDAO.getEventSubscriptionByDomain(eventType, domain);
	}
	
	public List<MWIEventInfo> getMWIEventList() throws Exception
	{
		List<Sipsessionlog> sslList = sslDAO.getAllSubscriptions(Eventsubscription.EVENT_MWI);
		List<MWIEventInfo> eventList = new ArrayList<MWIEventInfo>();
		
		for(Sipsessionlog ssl : sslList)
		{
			User u = ssl.getSessionlog().getUser();
			String to = new StringBuilder(u.getUsername()).append("@").append(u.getDomain().getDomain()).toString();
			if(u.getAgentUser().intValue() == User.TYPE_TERMINAL)
				u = tDAO.getAssociatedUserAndDomainByTerminalUserKey(u.getKey());
			int readMessages = ufDAO.countOldMessagesByUser(u.getKey(), false).intValue();
			int unreadMessages = ufDAO.countNewMessagesByUser(u.getKey(), false).intValue();
			if(ssl.getEventsubscriptions() != null && ssl.getEventsubscriptions().size() > 0)
			{
				Eventsubscription event = ssl.getEventsubscriptions().get(0);
				updateEventSubscriptionSequence(event);
				//String[] params = {"", "", ""};//event.getParameters().split("\\s");
				// jfarah 3.0.6 bug 6374 
				eventList.add(new MWIEventInfo(to, ssl.getContact(), readMessages, unreadMessages, event.getParameters(), event.getSequence(), u.getDomain().getDomain()));
			}
		}
		
		return eventList;
	}
	public List<Sessionlog> getOpenSessionLogByUsernameAndDomain(String username, String domain) throws DAOException
	{
		Pbxuser pbxuser = puDAO.getPbxuserByUsernameAndDomain(username, domain);
		List<Sessionlog> slList = new ArrayList<Sessionlog>();
		if(pbxuser != null)
		{
			slList = slDAO.getSessionlogListByPbxuser(pbxuser.getKey(), true, true);
		}
		
		return slList;
	}
	
	public Long	getNumberOfRegisteredUsers() throws DAOException
	{
		Long openedSipSession = sslDAO.getNumberOfOpenedSipsessionlog();
		return openedSipSession;
	}
	
	public Long	getNumberOfClosedSessions(int type) throws DAOException
	{
		Long openedSession = slDAO.getNumberOfClosedSessionlog(type);
		return openedSession;
	}
	
	private void updateEventSubscriptionSequence(Eventsubscription event) throws Exception
	{
		event.setSequence(getSequence(event));
		evtSubscriptionDAO.save(event);
	}
	
	private int getSequence(Eventsubscription eventsubscription)
	{
		int sequence = eventsubscription.getSequence() == null ? MIN_SEQUENCE_VALUE : eventsubscription.getSequence()+1;
		if(sequence > MAX_SEQUENCE_VALUE)
			sequence = MIN_SEQUENCE_VALUE;
		return sequence;
	}
	
	public List<Sipsessionlog> getOpenSipSessionLogByPbxUser(Long pbxUserKey) throws DAOException
	{
		List<Sipsessionlog> sslList = new ArrayList<Sipsessionlog>();
		if(pbxUserKey != null)
		{
			sslList = sslDAO.getActiveSipsessionlogListByPbxuser(pbxUserKey);
		}
		
		return sslList;
	}
	
}