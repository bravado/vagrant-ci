package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.VoiceException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.SessionManagerException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.GatewayDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.RouteruleDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipsessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.SessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Target;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Gateway;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Routerule;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.GatewayInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class GatewayManager extends Manager
{
	private GatewayDAO gatewayDAO;
	private ReportDAO<Gateway, GatewayInfo> reportGateway;
	private UserDAO userDAO;
	private SessionManager sessionManager;
	private DomainDAO domainDAO;
	private RouteruleDAO routeRuleDAO;
	private SessionlogDAO sessionlogDAO;
	private SipsessionlogDAO sipSessionlogDAO;
	
	public GatewayManager(String loggerPath) throws DAOException 
	{
		super(loggerPath);
		gatewayDAO = dao.getDAO(GatewayDAO.class);
		reportGateway = dao.getDAO(GatewayDAO.class);
		userDAO = dao.getDAO(UserDAO.class);
		sessionManager = new SessionManager(loggerPath);
		domainDAO = dao.getDAO(DomainDAO.class);
		routeRuleDAO = dao.getDAO(RouteruleDAO.class);
		sessionlogDAO = dao.getDAO(SessionlogDAO.class);
		sipSessionlogDAO = dao.getDAO(SipsessionlogDAO.class);
	}
	
	public List<Gateway> getRegisterGateways() throws DAOException
	{
		return gatewayDAO.getRegisterGateways();
	}
		
	public ReportResult findGateway(Report<GatewayInfo> info) throws DAOException
	{
		Long size = reportGateway.getReportCount(info);
		List<Gateway> gatewayList = reportGateway.getReportList(info);
		List<GatewayInfo> gatewayInfoList = new ArrayList<GatewayInfo>(gatewayList.size());
		for(Gateway gateway : gatewayList)
			gatewayInfoList.add(new GatewayInfo(gateway));
		return new ReportResult<GatewayInfo>(gatewayInfoList, size);
	}
	
	public void deleteGateways(List<Long> gatewaysKeys) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		for(Long gatewaysKey : gatewaysKeys)
			this.deleteGateway(gatewaysKey);
	}
	
	private void deleteGateway(Long gatewayKey) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		Gateway gateway = gatewayDAO.getGatewayFull(gatewayKey);
		List<Routerule> routeRuleList = routeRuleDAO.getRouteListByGateway(gateway.getKey());
		if(routeRuleList != null && routeRuleList.size() > 0)
			throw new DeleteDependenceException("Cannot delete " + gateway.getUser().getUsername() + " because there is route rules associated!!!", Routerule.class, routeRuleList.size(), gateway);

		User user = gateway.getUser();
		user.setActive(User.DEFINE_DELETED);
		userDAO.save(user);

		List<String> sipContactList = makeSipContactList(gateway.getSipContact());
		for(String sipContact : sipContactList)
		{
			sessionManager.unregisterSipUser(sipContact);
		}
		removeSipSessions(user.getKey());
		gateway.setActive(Gateway.DEFINE_DELETED);
		gatewayDAO.save(gateway);
	}

	public GatewayInfo getGatewayInfo(Long gatewayKey) throws DAOException
	{
		Gateway gateway = gatewayDAO.getGatewayFull(gatewayKey);
		List<String> sipContactList = null;
		if(gateway.getSipContact() != null)
		{
			String[] sipContacts = gateway.getSipContact().split(",");
			sipContactList = new ArrayList<String>();
			for(int i = 0; i < sipContacts.length; i++)
				sipContactList.add(sipContacts[i]);
		}
		GatewayInfo gatewayInfo = new GatewayInfo(gateway, sipContactList);
		return gatewayInfo;
	}

	public void saveGateway(GatewayInfo gatewayInfo) throws DAOException, ValidateObjectException, DeleteDependenceException, SessionManagerException, VoiceException
	{
		boolean edit = gatewayInfo.getKey() != null;
		User user = gatewayInfo.getUser();
		user.setAgentUser(User.TYPE_GATEWAY);
		user.setActive(User.DEFINE_ACTIVE);
		userDAO.save(user);

		List<String> sipContactListOld = new ArrayList<String>();
		List<String> sipContactListNew = gatewayInfo.getSipContactList() != null && gatewayInfo.getSipContactList().size() > 0 ? gatewayInfo.getSipContactList() : new ArrayList<String>();
		if(edit)
		{
			Gateway gatewayOld = gatewayDAO.getByKey(gatewayInfo.getKey());
			if(gatewayOld.getSipContact() != null && gatewayOld.getSipContact().length() > 0)
				sipContactListOld = makeSipContactList(gatewayOld.getSipContact());
			gatewayOld.setActive(Gateway.DEFINE_ACTIVE);
			gatewayOld.setUserKey(user.getKey());
			gatewayOld.setSipContact(makeSipContact(sipContactListNew));
			gatewayOld.setPrefix(gatewayInfo.getPrefix());
			gatewayOld.setRegister(gatewayInfo.getRegister());
			gatewayOld.setRegisterAddress(gatewayInfo.getRegisterAddress());
			validateSave(gatewayOld);
			gatewayDAO.save(gatewayOld);
		} else
		{
			Gateway gateway = gatewayInfo.getGateway();
			gateway.setActive(Gateway.DEFINE_ACTIVE);
			gateway.setUserKey(user.getKey());
			gateway.setSipContact(makeSipContact(sipContactListNew));
			validateSave(gateway);
			gatewayDAO.save(gateway);
		}
		manageSipContacList(user.getUsername(), domainDAO.getByKey(user.getDomainKey()).getDomain(), user.getKey(), sipContactListOld, sipContactListNew);		
	}
	
	private void manageSipContacList(String username, String domainName, Long userKey, List<String> sipContactListOld, List<String> sipContactListNew) throws DAOException, ValidateObjectException, DeleteDependenceException, SessionManagerException, VoiceException
	{
		Collections.sort(sipContactListNew);
		Collections.sort(sipContactListOld);
		int i = 0, j = 0;
		while(i < sipContactListNew.size() && j < sipContactListOld.size())
		{
			String newSipContact = sipContactListNew.get(i);
			String oldSipContact = sipContactListOld.get(j);
			int compare = newSipContact.compareTo(oldSipContact);
			if(compare == 0)
			{
				sessionManager.registerSipUser(username, domainName, oldSipContact, oldSipContact, Gateway.GATEWAY_USERAGENT, Gateway.GATEWAY_EXPIRES,userKey);
				i++;	j++;
			} else if(compare < 0)
			{
				sessionManager.registerSipUser(username, domainName, newSipContact, newSipContact, Gateway.GATEWAY_USERAGENT, Gateway.GATEWAY_EXPIRES,userKey);
				i++;
			} else
			{
				sessionManager.unregisterSipUser(oldSipContact);
				j++;
			}
		}
		if(i < sipContactListNew.size())
			while(i < sipContactListNew.size())
			{
				sessionManager.registerSipUser(username, domainName, sipContactListNew.get(i), sipContactListNew.get(i), Gateway.GATEWAY_USERAGENT, Gateway.GATEWAY_EXPIRES,userKey);
				i++;
			}	
		else if (j < sipContactListOld.size())
			while(j < sipContactListOld.size())
			{
				sessionManager.unregisterSipUser(sipContactListOld.get(j));
				j++;
			}
	}
	
	private void removeSipSessions(Long gatewayUserKey) throws DAOException, ValidateObjectException
	{
		List<Sessionlog> sessionlogList = sessionlogDAO.getSessionlogListByGatewayUser(gatewayUserKey);
		for(Sessionlog sessionlog : sessionlogList)
		{
			Sipsessionlog sipsessionlog = sipSessionlogDAO.getSipsessionlogBySessionlog(sessionlog.getKey());
			if(sipsessionlog != null)
				sipSessionlogDAO.remove(sipsessionlog);

			sessionlog.setSessionEnd(Calendar.getInstance());
			sessionlogDAO.save(sessionlog);
		}
	}

	private String makeSipContact(List<String> sipContactList)
	{
		String sipContactFull = ""; 
		if(sipContactList != null && sipContactList.size() > 0)
			for(int i = 0; i < sipContactList.size() ; i++)
			{
				if ((sipContactList.size() > 0) && (i < sipContactList.size() -1))
					sipContactFull += sipContactList.get(i) + ",";
				else
					sipContactFull += sipContactList.get(i);
			}
		return sipContactFull;
	}
	
	private List<String> makeSipContactList(String sipContact)
	{
		List<String> sipContactList = new ArrayList<String>();
		if(sipContact != null && sipContact.length() > 0)
		{
			String[] sipContacts = sipContact.split(",");
			for(int i = 0; i < sipContacts.length; i++)
				sipContactList.add(sipContacts[i]);
		}
		return sipContactList;
	}
	
	protected void validateSave(Gateway gateway) throws DAOException, ValidateObjectException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(gateway == null)
		{
			errorList.add(new ValidateError("Gateway is null!", Gateway.class, null, ValidateType.BLANK));
		} else
		{
			Long userKey = gateway.getUserKey();
			if(userKey == null)
				errorList.add(new ValidateError("Gateway Userkey is null!", Gateway.Fields.USER_KEY.toString(), Gateway.class, gateway, ValidateType.BLANK));
		}
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
	}
	
	public void updateGatewayLastHit(Target completedCall, List<Target> gatewaysTried, boolean isFailed) throws DAOException, ValidateObjectException
	{
		User u = getUser(completedCall, gatewaysTried);
		if(completedCall != null)
			updateGatewayLastHit(u, completedCall, isFailed);
		else
			for(Target target : gatewaysTried)
				updateGatewayLastHit(u, target, isFailed);
				
	}
	
	private User getUser(Target completedCall, List<Target> tList) throws DAOException
	{
		User u = null;
		if(completedCall != null)
			u = userDAO.getUserByUsernameAndDomain(completedCall.getUsername(), completedCall.getDomain());
		else if(tList.size() > 0)
		{
			Target tmp = tList.get(0);
			u = userDAO.getUserByUsernameAndDomain(tmp.getUsername(), tmp.getDomain());
		}
		return u;
	}
	
	private void updateGatewayLastHit(User u, Target t, boolean isFailed) throws DAOException, ValidateObjectException
	{
		if(t == null && u == null)
			return ;
		boolean isGateway = u.getAgentUser().intValue() == User.TYPE_GATEWAY;
		Sipsessionlog sslGateway = sipSessionlogDAO.getSipSessionByGatewayIP(u.getKey(), isGateway ? getGatewayIP(t) : getMediaContact(t));
		if(sslGateway == null)
			return ;
		
		Calendar now = Calendar.getInstance();
		if(isFailed)
		{
			int extraDelayTime = Integer.parseInt(IPXProperties.getProperty(IPXPropertiesType.GATEWAY_LOAD_BALANCE_DELAY));
			now.add(Calendar.MINUTE, extraDelayTime);
		}
			
		sslGateway.setSessionLastHit(now);
		sipSessionlogDAO.save(sslGateway);
	}
	
	private String getGatewayIP(Target t)
	{
		if(t.getSipsessionlogList().size() == 0)
			return null;
		String contact = t.getSipsessionlogList().get(0).getContact();
		return contact.substring(contact.indexOf("@") + 1);
	}
	
	private String getMediaContact(Target t)
	{
		if(t.getSipsessionlogList().size() == 0)
			return null;
		String contact = t.getSipsessionlogList().get(0).getContact();
		if(contact.startsWith("sip:"))
			contact = contact.substring(3);
		return contact;
	}
	
	public Gateway getGatewayFull(Long gatewayKey) throws DAOException
	{
		return gatewayDAO.getGatewayFull(gatewayKey);
	}
}
