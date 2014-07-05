package br.com.voicetechnology.ng.ipx.ejb.facade;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.ejb.PBXServerException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.ParkPositionBusy;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Target;
import br.com.voicetechnology.ng.ipx.pojo.db.park.ParkInfo;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Park;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.rule.implement.CallManager;
import br.com.voicetechnology.ng.ipx.rule.implement.SessionManager;
import br.com.voicetechnology.ng.ipx.rule.implement.park.ParkManager;


/**
 * 
 * <!-- begin-user-doc --> A generated session bean <!-- end-user-doc --> * <!-- begin-xdoclet-definition -->
 * 
 * @ejb.bean name="ParkServerFacade" description="A session bean named ParkServerFacade" display-name="parkServerFacade" jndi-name="br/com/voicetechnology/ng/ejb/facade/ParkServerFacade" type="Stateless" transaction-type="Container"
 * 
 * <!-- end-xdoclet-definition -->
 * @generated
 */

public abstract class ParkServerFacadeBean implements javax.ejb.SessionBean
{
	private Logger logger;
	private ParkManager parkManager;
	private CallManager callManager;
	private SessionManager sessionManager;
	
	public ParkServerFacadeBean()
	{
		try
		{
			logger = Logger.getLogger(this.getClass()); 
			parkManager = new ParkManager(logger.getName());
			callManager = new CallManager(logger.getName());
			sessionManager = new SessionManager(logger.getName());
		} catch(Exception e)
		{
			PBXServerException ex = new PBXServerException("Error in ParkServerFacade construtor!", e);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * 
	 * <!-- begin-xdoclet-definition -->
	 * 
	 * @ejb.create-method view-type="remote" <!-- end-xdoclet-definition -->
	 * @generated
	 * 
	 * //TODO: Must provide implementation for bean create stub
	 */
	public void ejbCreate()
	{
		//do nothing
	}
	
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public Park getByPositionAndDomainKey(String position, Long domainKey) throws PBXServerException
	{
		try
		{
			logger.debug("Start getByPositionAndDomainKey, position: "+position+" domainKey: " + domainKey);
			Park park = parkManager.getByPositionAndDomainKey(position, domainKey);
			logger.debug("End getparkServerListByDomain, position: "+position+" domainKey: " + domainKey);
			return park;
			
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getByPositionAndDomainKey was executed, position: "+position+" domainKey: " + domainKey, t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public List<User> getParkServerListByDomain(String domain) throws PBXServerException
	{
		try
		{
			logger.debug("Start getParkServerListByDomain, domain: " + domain);
			List<User> agentList = parkManager.getParkServerListByDomain(domain);
			logger.debug("End getparkServerListByDomain, domain: " + domain);
			return agentList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getParkServerListByDomain was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public User getParkServerByDomain(String domain) throws PBXServerException
	{
		try
		{
			logger.debug("Start getParkServerByDomain, domain: " + domain);
			User agent = parkManager.getParkServerByDomain(domain);
			logger.debug("End getparkServerByDomain, domain: " + domain);
			return agent;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getParkServerByDomain was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActiveCallByAddress(String address, String domain, String callId)
	{
			try
		{
			logger.debug("Start getActivecall, from: " + address + "@" + domain);
			Activecall ac = callManager.getActivecall(address, domain, callId);
			logger.debug("End getActivecall, from: " + address + "@" + domain);
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActivecall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public boolean isPositionBusy(String position, Long domainKey) throws PBXServerException
	{
		try
		{
			logger.debug("Start isPositionBusy, position: " + position + " domainKey: "+domainKey);
			boolean isBusy = parkManager.isPositionBusy(position, domainKey);
			logger.debug("End isPositionBusy, park: " + position + " domain: "+domainKey);
			return isBusy;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when isPositionBusy was executed, park: " + position + " domain: "+domainKey, t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 * @throws ParkPositionBusy 
	 */
	@SuppressWarnings("unchecked")
	public void parkPosition(Park park) throws PBXServerException, ParkPositionBusy
	{
		try
		{
			logger.debug("Start parkPosition, park: " + park);
			parkManager.parkPosition(park);
			logger.debug("End parkPosition, park: " + park);			
		}
		catch(Throwable t)
		{
			/*****************************************************************/
			/**
		     * jluchetta - Bug 6000 - Versao 3.0.5
		     * Problem to put two user in the same park position
		     */
			if(t instanceof ValidateObjectException)
			{
				ValidateObjectException validateEx = (ValidateObjectException)t;
				List<ValidateError> errorList =validateEx.getErrorsList();
				for(ValidateError error : errorList)
					if(error.getType().equals(ValidateType.DUPLICATED))
						throw new ParkPositionBusy("The park position is already in use in this domain, park"+park, t);
			}
			/*********************************************************************/
			PBXServerException ex = new PBXServerException("A error ocurrs when parkPosition was executed, park: " + park, t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public void pickUpPosition(String position, Long domainKey) throws PBXServerException
	{
		try
		{
			logger.debug("Start pickUpPosition, position: " + position+" domainKey: "+domainKey);
			parkManager.pickUpPosition(position, domainKey);
			logger.debug("End pickUpPosition, position: " + position+" domainKey: "+domainKey);			
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when pickUpPosition was executed, position: " + position+" domainKey: "+domainKey, t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public void removeAllParkedConnections(Long domainKey) throws RemoteException
	{
		try
		{
			logger.debug("Start removeAllParkedConnections, domainKey: "+domainKey);
			parkManager.removeAllParkedConnections(domainKey);
			logger.debug("End pickUpPosition, removeAllParkedConnections, domainKey: "+domainKey);			
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when removeAllParkedConnections, domainKey: "+domainKey, t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public ParkInfo getParkInfo(String from, String to) throws PBXServerException
	{
		try
		{
			logger.debug("Start getParkInfo, from: " + from +" to: "+to);
			ParkInfo parkInfo = parkManager.getParkInfo(from, to);
			logger.debug("End getParkInfo, from: " + from +" to: "+to);
			return parkInfo;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getParkInfo was executed, from: " + from +" to: "+to, t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public Target lookupSipSession(String address, String domain)
	{
		try
		{
			logger.debug("Start lookupSipSession, address: " + address + "@" + domain);
			Target target = sessionManager.lookupSipSession(address, domain);
			logger.debug("End lookupSipSession, farmIP: " + address + "@" + domain);
			return target;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when lookupSipSession was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}	
}
