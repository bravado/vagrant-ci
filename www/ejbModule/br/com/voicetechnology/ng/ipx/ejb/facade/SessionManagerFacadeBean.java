/**
 * 
 */
package br.com.voicetechnology.ng.ipx.ejb.facade;

import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.VoiceException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.UserWithoutSipSessionlogException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.PBXServerException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.PbxAlreadyRegistered;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.SessionManagerException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.SipsessionRegisterDenied;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.mwi.MWIEventInfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Eventsubscription;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.registrar.UserRegisterExpiresInfo;
import br.com.voicetechnology.ng.ipx.rule.implement.SessionManager;


/**
 *
 * <!-- begin-user-doc -->
 * A generated session bean
 * <!-- end-user-doc -->
 * *
 * <!-- begin-xdoclet-definition --> 
 * @ejb.bean name="SessionManagerFacade"	
 *           description="An EJB named SessionManagerFacade"
 *           display-name="SessionManageFacader"
 *           jndi-name="br/com/voicetechnology/ng/ejb/facade/SessionManagerFacade"
 *           type="Stateless" 
 *           transaction-type="Container"
 * 
 * <!-- end-xdoclet-definition --> 
 * @generated
 */

public abstract class SessionManagerFacadeBean implements javax.ejb.SessionBean 
{

	private Logger logger;
	private SessionManager sessionManager;
	
	public SessionManagerFacadeBean()
	{
		try
		{
			logger = Logger.getLogger(SessionManagerFacadeBean.class);
			logger.info("Creating instance of " + this.getClass().getName());
			sessionManager = new SessionManager(logger.getName());
		}catch(Exception e)
		{
			PBXServerException ex = new PBXServerException("Error in SessionManagerFacade construtor!", e);
            logger.error(ex);
			throw ex;
		}
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.create-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void ejbCreate() 
	{
		//do nothing...
	}

	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public long closeExpiredSessions(int timeout) 
	{
		try
		{
			logger.debug("Start closeExpiredSessions, timeout: " + timeout);
			long closedSessions = sessionManager.closeExpiredSessions(timeout);
			logger.debug("End closeExpiredSessions, timeout: " + timeout);
			return closedSessions;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when closeExpiredSessions was executed!", t);
            logger.error(ex);
			throw ex;
		}
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public long closeExpiredSipsessions()
	{
		try
		{
			logger.debug("Start closeExpiredSipsessions");
			long closedSessions = sessionManager.closeExpiredSipSessions();
			logger.debug("End closeExpiredSipsessions");
			return closedSessions; 
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when closeExpiredSipsessions was executed!", t);
            logger.error(ex);
			throw ex;
		}
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void registerSipUser(String username, String domain, String contact, String originalContact, String userAgent, int expires, Long userKey) throws SessionManagerException, VoiceException
	{
		try
		{
			logger.debug("Start registerSipUser: " + contact);
			sessionManager.registerSipUser(username, domain, contact, originalContact, userAgent, expires, userKey);
			logger.debug("End registerSipUser: " + contact);
		} catch(PbxAlreadyRegistered e)
		{			
			throw e;
			
		} catch(Throwable t)
		{
			SessionManagerException ex = new SessionManagerException("A error ocurrs when registerSipUser was executed!", t);
            logger.error(ex);
			throw ex;
		}	
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void registerSipUserAndRemoveCurrentSession(String username, String domain, String contact, String originalContact, String userAgent, int expires) throws VoiceException, DAOException, ValidateObjectException
	{
		try
		{
			logger.debug("Start registerSipUserAndRemoveCurrentSession: " + contact);
			sessionManager.registerSipUserAndRemoveCurrentSession(username, domain, contact, originalContact, userAgent, expires);
			logger.debug("End registerSipUserAndRemoveCurrentSession: " + contact);
		} catch(SipsessionRegisterDenied e)
		{			
			throw e;
			
		} catch(Throwable t)
		{
			SessionManagerException ex = new SessionManagerException("A error ocurrs when registerSipUser was executed!", t);
            logger.error(ex);
			throw ex;
		}	
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void registerSipUser(String username, String domain, String contact, String originalContact, String userAgent, int expires) throws SessionManagerException, VoiceException
	{
		try
		{
			logger.debug("Start registerSipUser: " + contact);
			UserRegisterExpiresInfo registerExpires = sessionManager.verifyRegisterExpire(username, domain, contact,  expires);
			sessionManager.registerSipUser(username, domain, contact, originalContact, userAgent, registerExpires.getExpires(), registerExpires.getUser().getKey());
			logger.debug("End registerSipUser: " + contact);
			
		} catch(PbxAlreadyRegistered e)
		{			
			throw e;
			
		} catch(SipsessionRegisterDenied e)
		{			
			throw e;
			
		} catch(Throwable t)
		{
			SessionManagerException ex = new SessionManagerException("A error ocurrs when registerSipUser was executed!", t);
           logger.error(ex);
			throw ex;
		}	
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public UserRegisterExpiresInfo verifyRegisterExpires(String username, String domain, String contact, int expires)throws SessionManagerException
	{
		try
		{
			return sessionManager.verifyRegisterExpire(username, domain, contact, expires);
		} catch(SipsessionRegisterDenied ex)
		{
			PBXServerException pe = new PBXServerException(new StringBuilder("A error ocurrs when verifyRegisterExpires ").append(username).append("@").append(domain).append(" was executed!").toString(), ex);
            logger.error(new StringBuilder("Error while verifying register expires: ").append(ex.getMessage()));
			throw pe;
		} catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException(new StringBuilder("A error ocurrs when verifyRegisterExpires ").append(username).append("@").append(domain).append(" was executed!").toString(), t);
            logger.error(ex);
			throw ex;
		}
		
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void unregisterSipUser(String contact) throws SessionManagerException
	{
		try
		{
			logger.debug("Start unregisterSipUser: " + contact);
			sessionManager.unregisterSipUser(contact);
			logger.debug("End unregisterSipUser: " + contact);
		}catch(Throwable t)
		{
			SessionManagerException ex = new SessionManagerException("A error ocurrs when unregisterSipUser "+contact+" was executed!", t);
            logger.error(ex);
			throw ex;
		}
	
	}

	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void unregisterSipUser(String contact, String domain) throws SessionManagerException
	{
		try
		{
			logger.debug(new StringBuilder("Start unregisterSipUser: ").append(contact).append(" on domain: ").append(domain));
			sessionManager.unregisterSipUser(contact, domain);
			logger.debug(new StringBuilder("End unregisterSipUser: ").append(contact).append(" on domain: ").append(domain));
		}catch(Throwable t)
		{
			SessionManagerException ex = new SessionManagerException("A error ocurrs when unregisterSipUser "+contact+" was executed!", t);
            logger.error(ex);
			throw ex;
		}

	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void removeSipSessionByUsernameAndDomain(String username, String domain) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start removing sipsessionlog by username: ").append(username).append(" and domain: ").append(domain));
			sessionManager.removeSipSessionByUsernameAndDomain(username, domain);
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End removing sipsessionlog by username: ").append(username).append(" and domain: ").append(domain));
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException(new StringBuilder("A error ocurrs when removing sipsessionlog by username: ").
					append(username).append(" and domain: ").append(domain).toString(), t);
            logger.error(ex);
			throw ex;
		}
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void removeAllSessionByFarmIP(String farmIP) throws SessionManagerException
	{
		try
		{
			logger.debug("Start removeAllMediaSessionByFarmIP: " + farmIP);
			sessionManager.removeAllSessionByFarmIP(farmIP);
			logger.debug("End removeAllMediaSessionByFarmIP: " + farmIP);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when removeAllMediaSessionByFarmIP "+farmIP+" was executed!", t);
           logger.error(ex);
			throw ex;
		}
	
	}

	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public User getUserByUsernameAndDomain(String username, String domain)  
	{
		try
		{
			User u;
			logger.debug("Start getUserByUsernameAndDomain: " + username + "@" + domain);
			u = sessionManager.getUserByUsernameAndDomain(username, domain);
			logger.debug("End getUserByUsernameAndDomain: " + username + "@" + domain);
			return u;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getUserByUsernameAndDomain was executed!" + username + "@" + domain, t);
            logger.error(ex);
			throw ex;
		}
	
	}

	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @throws UserWithoutSipSessionlogException 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public boolean addSubscription(String contact, String event) throws SessionManagerException, UserWithoutSipSessionlogException
	{
		try
		{
			logger.debug("Start addSubscription contact: " + contact + " event: " + event) ;
			boolean isNewSusbscription = sessionManager.addSubscription(contact, event);
			logger.debug("End addSubscription contact: " + contact + " event: " + event);
			return isNewSusbscription;
			
		} catch (UserWithoutSipSessionlogException e) 
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Could not addSubscription User Without SipSessionlog ").append(contact));
			throw e;
		} catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when addSubscription was executed!", t);
            logger.error(ex);
			throw ex;
		}
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @throws UserWithoutSipSessionlogException 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public boolean addSubscription(String contact, String event, String parameters) throws SessionManagerException, UserWithoutSipSessionlogException
	{
		try
		{
			logger.debug("Start addSubscription contact: " + contact + " event: " + event+" parameters: "+parameters) ;
			boolean isNewSusbscription = sessionManager.addSubscription(contact, event, parameters);
			logger.debug("End addSubscription contact: " + contact + " event: " + event+" parameters: "+parameters);
			return isNewSusbscription;
		} catch (UserWithoutSipSessionlogException e) 
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Could not addSubscription User Without SipSessionlog ").append(contact));
			throw e;
		} catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when addSubscription was executed, " +
					"contact: " + contact + " event: " + event+" parameters: "+parameters, t);
            logger.error(ex);
			throw ex;
		}
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void saveSubscrition(Eventsubscription subscription) throws SessionManagerException
	{
		try
		{
			logger.debug("Start saveSubscrition subscription: "+subscription) ;
			sessionManager.saveSubscrition(subscription);
			logger.debug("End saveSubscrition subscription: "+subscription) ;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveSubscription.", t);
            logger.error(ex);
			throw ex;
		}
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public boolean removeSubscription(String contact, String event) throws SessionManagerException
	{
		try
		{
			logger.debug("Start removeSubscription contact: " + contact + " event: " + event) ;
			boolean removed = sessionManager.removeSubscription(contact, event);
			logger.debug("End removeSubscription contact: " + contact + " event: " + event);
			return removed;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when removeSubscription was executed!", t);
            logger.error(ex);
			throw ex;
		}
	}
	
	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public long getNumberOfSusbcription() throws SessionManagerException
	{
		try
		{
			logger.debug("Start getNumberOfSusbcription") ;
			long count = sessionManager.getNumberOfSusbcription();
			logger.debug("End getNumberOfSusbcription") ;
			return count;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getNumberOfSusbcription was executed!", t);
           logger.error(ex);
			throw ex;
		}
	}
	
	 /** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public List<MWIEventInfo> getMWIEventList()
	{
		try
		{
			logger.debug("Start getReadAndUnreadMessages");
			List<MWIEventInfo> eventList = sessionManager.getMWIEventList();
			logger.debug("End getReadAndUnreadMessages");
			return eventList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getReadAndUnreadMessages was executed!", t);
            logger.error(ex);
			throw ex;
		}
	}	 
	 /** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	 public List<Eventsubscription> getEventSubscriptionByDomain(String eventType, String domain) throws PBXServerException
	 {
		 try
		 {
			 logger.debug("Start getEventSubscriptionByDomain, domain: "+domain+" eventType: "+eventType);
			 return sessionManager.getEventSubscriptionByDomain(eventType, domain);

		 }catch(Throwable t)
		 {
			 PBXServerException ex = new PBXServerException("A error ocurrs when getEventSubscriptionByDomain, domain: "+domain+" eventType: "+eventType +" was executed!", t);
             logger.error(ex);
			 throw ex;
		 }
	 }
	 
	 	 
	 /** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	 public Long getNumberOfRegisteredUsers() throws PBXServerException
	 {
		 try
		 {
			 logger.debug("Start getNumberOfRegisteredUsers ");
			 return sessionManager.getNumberOfRegisteredUsers();

		 }catch(Throwable t)
		 {
			 PBXServerException ex = new PBXServerException("A error ocurrs when getNumberOfRegisteredUsers ", t);
             logger.error(ex);
			 throw ex;
		 }
	 }
	 
	 /** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	 public Long getNumberOfClosedSipSessions() throws PBXServerException
	 {
		 try
		 {
			 logger.debug("Start getNumberOfClosedSipSessions ");
			 return sessionManager.getNumberOfClosedSessions(Sessionlog.TYPE_SIP_SESSION);

		 }catch(Throwable t)
		 {
			 PBXServerException ex = new PBXServerException("A error ocurrs when getNumberOfClosedSipSessions ", t);
             logger.error(ex);
			 throw ex;
		 }
	 }
	 
	 /** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="remote"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	 public Long getNumberOfClosedWebSessions() throws PBXServerException
	 {
		 try
		 {
			 logger.debug("Start getNumberOfClosedWebSessions ");
			 return sessionManager.getNumberOfClosedSessions(Sessionlog.TYPE_USER_SESSION);

		 }catch(Throwable t)
		 {
			 PBXServerException ex = new PBXServerException("A error ocurrs when getNumberOfClosedWebSessions ", t);
             logger.error(ex);
			 throw ex;
		 }
	 }
}