package br.com.voicetechnology.ng.ipx.ejb.facade;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.ejb.PBXServerException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Target;
import br.com.voicetechnology.ng.ipx.pojo.db.callcenter.CallCenterCallEvent;
import br.com.voicetechnology.ng.ipx.pojo.db.callcenter.CallCenterCallLog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterGroupReportIn;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterLoginReport;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterPaReport;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.GroupPause;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UsergroupInfo;
import br.com.voicetechnology.ng.ipx.rule.implement.GroupManager;
import br.com.voicetechnology.ng.ipx.rule.implement.PauseManager;
import br.com.voicetechnology.ng.ipx.rule.implement.SessionManager;
import br.com.voicetechnology.ng.ipx.rule.implement.acdgroup.AcdGroupManager;
import br.com.voicetechnology.ng.ipx.rule.implement.acdgroup.CallCenterReportManager;

/**
 * 
 * <!-- begin-user-doc --> A generated session bean <!-- end-user-doc --> * <!-- begin-xdoclet-definition -->
 * 
 * @ejb.bean name="AcdGroupServerFacade" description="A session bean named AcdGroupServerFacade" display-name="AcdGroupServerFacade" jndi-name="br/com/voicetechnology/ng/ejb/facade/AcdGroupServerFacade" type="Stateless" transaction-type="Container"
 * 
 * <!-- end-xdoclet-definition -->
 * @generated
 */
public abstract class AcdGroupServerFacadeBean implements javax.ejb.SessionBean
{
	private Logger logger;
	private AcdGroupManager acdGroupManager;
	private SessionManager sessionManager;
	private CallCenterReportManager callCenterReportManager;
	private GroupManager groupManager;
	private PauseManager pauseManager;
	
	public AcdGroupServerFacadeBean()
	{
		try
		{
			logger = Logger.getLogger(this.getClass()); 
			acdGroupManager = new AcdGroupManager(logger.getName());
			sessionManager = new SessionManager(logger.getName());
			callCenterReportManager = new CallCenterReportManager(logger);
			groupManager = new GroupManager(logger.getName());
			pauseManager = new PauseManager(logger.getName());
		}catch(Exception e)
		{
			PBXServerException ex = new PBXServerException("Error in AcdGroupServerFacade construtor!", e);
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
	public List<User> getAcdGroupServerListByFarmIP(String farmIP) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAcdGroupServerListByFarmIP, farmIP: ").append(farmIP));

			List<User> agentList = acdGroupManager.getAcdGroupServerListByFarmIP(farmIP);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAcdGroupServerListByFarmIP, farmIP: ").append(farmIP));
			return agentList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAcdGroupServerListByFarmIP was executed!", t);
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
	public List<User> getAcdGroupServerListByDomain(String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAcdGroupServerListByDomain, domain: ").append(domain));

			List<User> agentList = acdGroupManager.getAcdGroupServerListByDomain(domain);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAcdGroupServerListByDomain, domain: ").append(domain));
			return agentList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAcdGroupServerListByDomain was executed!", t);
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
	public List<Fileinfo> getAcdGroupFileList(Long domainKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAcdGroupFileList, domainKey: ").append(domainKey));

			List<Fileinfo> fileList = acdGroupManager.getAcdGroupFileList(domainKey);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAcdGroupFileList, domainKey: ").append(domainKey));
			return fileList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAcdGroupFileList was executed!", t);
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
	public LinkedList<Usergroup> getCallCenterTargets(String username, String domain, int algorithmType) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallCenterTargets, username: ").append(username).append(" domain: ").append(domain));

			LinkedList<Usergroup> uList = acdGroupManager.getCallCenterTargets(username, domain, algorithmType);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallCenterTargets, username: ").append(username).append(" domain: ").append(domain));
			return uList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallCenterTargets was executed!", t);
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
	public List<Sipsessionlog> getActiveSipsessionByUsernameAndDomain(String username, String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getActiveSipsessionByUsernameAndDomain, username: ").append(username).append(" domain: ").append(domain));

			List<Sipsessionlog> sslList = acdGroupManager.getActiveSipsessionByUsernameAndDomain(username, domain);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getActiveSipsessionByUsernameAndDomain, username: ").append(username).append(" domain: ").append(domain));
			return sslList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActiveSipsessionByUsernameAndDomain was executed!", t);
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
	public Integer getAlgorithmGroupTypeByUsernameAndDomain(String username, String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAlgorithmGroupTypeByUsernameAndDomain, username: ").append(username).append(" domain: ").append(domain));

			Integer algorithmGroupType = acdGroupManager.getAlgorithmGroupTypeByUsernameAndDomain(username, domain);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAlgorithmGroupTypeByUsernameAndDomain, username: ").append(username).append(" domain: ").append(domain));
			return algorithmGroupType;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAlgorithmGroupTypeByUsernameAndDomain was executed!", t);
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
	public List<Usergroup> getUsergroupListByPbxuserKey (Long pbxUserKey, Long groupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUsergroupListByPbxuserKey, pbxUserKey: ").append(pbxUserKey));

			List<Usergroup> usergroup = acdGroupManager.getUsergroupListByPbxuserKey(pbxUserKey, groupKey);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUsergroupListByPbxuserKey, pbxUserKey: ").append(pbxUserKey));
			return usergroup;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getUsergroupListByPbxuserKey was executed!", t);
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
	public List<Usergroup> getUsergroupListByGroupKey (Long groupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUsergroupListByGroupKey, groupKey: ").append(groupKey));

			List<Usergroup> usergroup = acdGroupManager.getUsergroupListByGroupKey(groupKey);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUsergroupListByGroupKey, pbxUserKey: ").append(groupKey));
			return usergroup;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getUsergroupListByGroup was executed!", t);
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
	public List<Usergroup> getLoggedAndActiveUsergroupListByGroupKey (Long groupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getLoggedAndActiveUsergroupListByGroupKey, groupKey: ").append(groupKey));

			List<Usergroup> usergroup = acdGroupManager.getLoggedAndActiveUsergroupListByGroupKey(groupKey);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getLoggedAndActiveUsergroupListByGroupKey, pbxUserKey: ").append(groupKey));
			return usergroup;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getLoggedAndActiveUsergroupListByGroupKey was executed!", t);
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
	public Usergroup getUsergroupFullByUsergroupKey (Long usergroupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUsergroupFullByUsergroupKey, usergroupKey: ").append(usergroupKey));

			Usergroup usergroup = groupManager.getUsergroupFullByUsergroupKey(usergroupKey);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUsergroupFullByUsergroupKey, usergroupKey: ").append(usergroupKey));
			return usergroup;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getUsergroupFullByUsergroupKey was executed!", t);
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
	public Group getGroupByGroupNameAndDomain (String groupname, String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getGroupByUsernameAndDomain, groupname: ").append(groupname).append(" domain: ").append(domain));
			
			Group group = acdGroupManager.getGroupByGroupNameAndDomain(groupname, domain);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getGroupByUsernameAndDomain, groupname: ").append(groupname).append(" domain: ").append(domain));
			return group;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getGroupByUsernameAndDomain was executed!", t);
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
    public Group getGroupByGroupKey (Long groupKey) throws PBXServerException
    {
        try
        {
            if(logger.isDebugEnabled())
                logger.debug(new StringBuilder("Start getGroupByGroupKey, groupKey: ").append(groupKey));
            
            Group group = acdGroupManager.getGroupByGroupKey(groupKey);
            
            if(logger.isDebugEnabled())
                logger.debug(new StringBuilder("End getGroupByGroupKey, groupKey: ").append(groupKey));
            return group;
        }catch(Throwable t)
        {
            PBXServerException ex = new PBXServerException("A error ocurrs when getGroupByGroupKey was executed!", t);
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
	public boolean isInWorkHour(Long groupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start isInWorkHour, groupname: ").append(groupKey));			
			return acdGroupManager.isInWorkHour(groupKey);			
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when isInWorkHour was executed!", t);
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
	public Address getAddressByKey (Long addressKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAddressByKey, addressKey: ").append(addressKey));
			
			Address address = acdGroupManager.getAddressByKey(addressKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getAddressByKey, addressKey: ").append(addressKey));
			return address;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAddressByKey was executed!", t);
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
	public Target lookupSipSession (String address, String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start lookupSipSession, address: ").append(address).append("@").append(domain));

			Target target = sessionManager.lookupSipSession(address, domain);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End lookupSipSession, address: ").append(address).append("@").append(domain));
			return target;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when lookupSipSession was executed!", t);
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
	public void saveCallCenterCallLog (CallCenterCallLog callLog) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start save CallCenterCallLog: ").append(callLog));
			
			acdGroupManager.saveCallCenterCallLog(callLog);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End save CallCenterCallLog: ").append(callLog));
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveCallCenterCallLog was executed!", t);
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
	public CallCenterLoginReport getLastCallCenterLoginReport (Long groupKey, Long pbxUserKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getLastCallCenterLoginReport: ").append(groupKey).append(",").append(pbxUserKey));
			
			CallCenterLoginReport loginReport = callCenterReportManager.getLastCallCenterLoginReport(groupKey, pbxUserKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getLastCallCenterLoginReport: ").append(groupKey).append(",").append(pbxUserKey));
			
			return loginReport;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getLastCallCenterLoginReport was executed!", t);
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
	public void saveCallCenterGroupReportIn (CallCenterGroupReportIn report) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start save saveCallCenterGroupReportIn: ").append(report));
			
			callCenterReportManager.saveGroupReportIn(report);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End save saveCallCenterGroupReportIn: ").append(report));
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveCallCenterGroupReportIn was executed!", t);
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
	public void saveCallCenterLoginReport (CallCenterLoginReport report) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start save saveCallCenterLoginReport: ").append(report));
			
			callCenterReportManager.saveLoginReport(report);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End save saveCallCenterLoginReport: ").append(report));
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveCallCenterLoginReport was executed!", t);
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
	public List<CallCenterCallLog> getCallLogAll(String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallLogAll: ").append(domain));
			
			List<CallCenterCallLog> callLogList = acdGroupManager.getCallLogAll(domain);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getCallLogAll: ").append(domain));
			return callLogList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallLogAll was executed!", t);
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
	public LinkedList<CallCenterCallEvent> getCallEventAll(Long domainKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallEventAll: ").append(domainKey));
			
			LinkedList<CallCenterCallEvent> callEventList = acdGroupManager.getCallEventAll(domainKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getCallEventAll: ").append(domainKey));
			return callEventList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallEventAll was executed!", t);
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
	public Long getEspecifCallLogCount(String origination, String destination, String callsuccess, String did, String usernamePA, 
			Calendar startCallDate, Calendar endCallDate, Long domainKey, Long groupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getEspecifCallLog: ").append(origination).append(", ").append(destination).append(", ").append(callsuccess).
						append(", ").append(did).append(", ").append(usernamePA).append(", ").append(startCallDate).append(", ").append(endCallDate).
						append(", ").append(domainKey).append(", ").append(groupKey));
			
			Long counter = acdGroupManager.getEspecifCallLogCount(origination, destination, callsuccess, did, usernamePA, startCallDate, endCallDate, domainKey, groupKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getEspecifCallLog").append(origination).append(", ").append(destination).append(", ").append(callsuccess).
						append(", ").append(did).append(", ").append(usernamePA).append(", ").append(startCallDate).append(", ").append(endCallDate).
						append(", ").append(domainKey).append(", ").append(groupKey));
			return counter;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getEspecifCallLog was executed!", t);
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
	public User getUserByUsergroupKey(Long usergroupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUserByUsergroupKey, usergroupKey: ").append(usergroupKey));

			User u = acdGroupManager.getUserByUsergroupKey(usergroupKey);

			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUserByUsergroupKey, usergroupKey: ").append(usergroupKey));
			return u;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getUserByUsergroupKey was executed!", t);
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
	public LinkedList<CallCenterCallEvent> getCallEventByCallLogKey(Long callCenterCallLogKey, Long domainKey) throws PBXServerException
	{
		try
		{
			LinkedList<CallCenterCallEvent> callEventList = acdGroupManager.getCallEventByCallLogKey(callCenterCallLogKey, domainKey);
			return callEventList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallEventByCallLogKey was executed!", t);
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
	public CallCenterCallLog getCallLogByCallId(String callId) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallLogByCallId: ").append(callId));
			
			CallCenterCallLog callCenterCallLog = acdGroupManager.getCallLogByCallId(callId);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getCallLogByCallId: ").append(callId));
			return callCenterCallLog;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallLogByCallId was executed!", t);
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
	public Long countCallLogs(String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start countCallLogs: ").append(domain));
			
			Long count = acdGroupManager.countCallLogs(domain);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End countCallLogs: ").append(domain));
			return count;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when countCallLogs was executed!", t);
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
	public CallCenterCallLog getCallLogBykey(Long key) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallLogBykey: ").append(key));
			
			CallCenterCallLog callCenterCallLog = acdGroupManager.getCallLogBykey(key);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getCallLogBykey: ").append(key));
			return callCenterCallLog;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallLogBykey was executed!", t);
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
	public List<CallCenterCallLog> findCallLogEntries(String origination, String destination, String callsuccess, String did, String usernamePA, 
			Calendar startCallDate, Calendar endCallDate, Long domainKey, Long groupKey, Integer firstResult, Integer maxResults) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start findCallLogEntries: ").append(firstResult).append(", ").append(maxResults).append(", ").append(domainKey));
			
			List<CallCenterCallLog> callLogList = acdGroupManager.findCallLogEntries(origination, destination, callsuccess, did, usernamePA, 
					startCallDate, endCallDate, domainKey, groupKey, firstResult, maxResults);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End findCallLogEntries: ").append(firstResult).append(", ").append(maxResults).append(", ").append(domainKey));
			return callLogList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when findCallLogEntries was executed!", t);
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
	public Long countCallEvents(String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start countCallEvents(): ").append(domain));
			
			Long count = acdGroupManager.countCallEvents(domain);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End countCallEvents(): ").append(domain));
			return count;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when countCallEvents() was executed!", t);
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
	public CallCenterCallLog getCallLogByCallEventKey(Long callEventKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallLogByCallEventKey: ").append(callEventKey));
			
			CallCenterCallLog callCenterCallLog = acdGroupManager.getCallLogByCallEventKey(callEventKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getCallLogByCallEventKey: ").append(callEventKey));
			return callCenterCallLog;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallLogByCallEventKey was executed!", t);
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
	public List<CallCenterCallEvent> findCallEventEntries(Long callLogKey, Integer eventType, String username, Calendar startCallDate, Calendar endCallDate, 
			Integer firstResult, Integer maxResults, Long domainKey, Long groupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start findCallEventEntries: ").append(firstResult).append(", ").append(maxResults).append(", ").append(domainKey));
			
			List<CallCenterCallEvent> callEventList = acdGroupManager.findCallEventEntries(callLogKey, eventType, username, startCallDate, endCallDate, firstResult, maxResults, domainKey, groupKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End findCallEventEntries: ").append(firstResult).append(", ").append(maxResults).append(", ").append(domainKey));
			return callEventList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when findCallEventEntries was executed!", t);
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
	public Long getEspecifCallEventCount(Long callLogKey, Integer eventType, String username, Calendar startCallDate, Calendar endCallDate, 
			Long domainKey, Long groupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getEspecifCallEvent: ").append(callLogKey).append(", ").append(eventType).append(", ").
						append(username).append(", ").append(startCallDate).append(", ").append(endCallDate).append(", ").append(domainKey).append(", ").append(groupKey));
			
			Long count = acdGroupManager.getEspecifCallEventCount(callLogKey, eventType, username, startCallDate, endCallDate, domainKey, groupKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getEspecifCallEvent: ").append(callLogKey).append(", ").append(eventType).append(", ").
						append(username).append(", ").append(startCallDate).append(", ").append(endCallDate).append(", ").append(domainKey).append(", ").append(groupKey));
			return count;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getEspecifCallEvent was executed!", t);
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
	public List<Usergroup> getUsergroupListByUsernameAndDomain(String username, String domain, boolean groupAdmin) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUsergroupListByUsernameAndDomain: ").append(username).append(", ").append(domain).append(", ").append(groupAdmin));
			
			List<Usergroup> usergroupList = acdGroupManager.getUsergroupListByUsernameAndDomain(username, domain, groupAdmin);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getUsergroupListByUsernameAndDomain: ").append(username).append(", ").append(domain).append(", ").append(groupAdmin));
			return usergroupList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getUsergroupListByUsernameAndDomain was executed!", t);
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
	public User getUserWithRoleListByUsernameAndDomain(String username, String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUserWithRoleListByUsernameAndDomain: ").append(username).append(", ").append(domain));
			
			User user = acdGroupManager.getUserWithRoleListByUsernameAndDomain(username, domain);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getUserWithRoleListByUsernameAndDomain: ").append(username).append(", ").append(domain));
			return user;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getUserWithRoleListByUsernameAndDomain was executed!", t);
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
	public List<String> getCallCenterWCAGroupnameListByDomain(String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallCenterWCAGroupnameListByDomain: ").append(domain));
			
			List<String> groups = acdGroupManager.getCallCenterWCAGroupnameListByDomain(domain);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getCallCenterWCAGroupnameListByDomain: ").append(domain));
			return groups;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallCenterWCAGroupnameListByDomain was executed!", t);
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
	public List<String> getCallCenterAdminGroupnameListByDomain(String username, String domain, boolean groupAdmin) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallCenterAdminGroupnameListByDomain: ").append(username).append(", ").append(domain).append(", ").append(groupAdmin));
			
			List<String> groups = acdGroupManager.getCallCenterAdminGroupnameListByDomain(username, domain, groupAdmin);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getCallCenterAdminGroupnameListByDomain: ").append(username).append(", ").append(domain).append(", ").append(groupAdmin));
			return groups;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallCenterAdminGroupnameListByDomain was executed!", t);
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
	public List<Domain> getDomainsWithCallCenterGroups() throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getDomainsWithCallCenterGroups"));
			
			List<Domain> domains = acdGroupManager.getDomainsWithCallCenterGroups();
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getDomainsWithCallCenterGroups"));
			return domains;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getDomainsWithCallCenterGroups was executed!", t);
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
	public Domain getDomain(String domain) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getDomain to CallCenter: ").append(domain));
			
			Domain domaintmp = acdGroupManager.getDomain(domain);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getDomain to CallCenter: ").append(domain));
			return domaintmp;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getDomain to CallCenter was executed!", t);
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
	public CallCenterGroupReportIn getCurrentGroupReportByGroupAndType(Long groupKey, int type) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getByGroupAndType: ").append(groupKey));
												
			CallCenterGroupReportIn report = callCenterReportManager.getCurrentByGroupAndType(groupKey, type);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getByGroupAndType: ").append(groupKey));
			return report;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getByGroupAndType was executed!", t);
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
	public List<CallCenterGroupReportIn> getLastHourReports(Long groupKey) throws PBXServerException{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getLastHourReports: ").append(groupKey));
												
			List<CallCenterGroupReportIn> reports = callCenterReportManager.getLastHourReports(groupKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getLastHourReports: ").append(groupKey));
			return reports;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getLastHourReports was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}	
	}
	
//	/**
// 	 * @ejb.interface-method view-type="remote"
//	 * @generated
//	 * @ejb.transaction type="Required"
//	 * @throws PBXServerException
//	 */
//	public List<CallCenterLoginReport> getLastHourLoginReports(Long groupKey) throws PBXServerException{
//		try
//		{
//			if(logger.isDebugEnabled())
//				logger.debug(new StringBuilder("Start getLastHourLoginReports: ").append(groupKey));
//												
//			List<CallCenterLoginReport> loginReports = callCenterReportManager.getLastHourLoginReports(groupKey);
//			
//			if(logger.isDebugEnabled())
//				logger.debug(new StringBuilder("End getLastHourLoginReports: ").append(groupKey));
//			return loginReports;
//		}catch(Throwable t)
//		{
//			PBXServerException ex = new PBXServerException("A error ocurrs when getLastHourLoginReports was executed!", t);
//			logger.error(ex.getLocalizedMessage());
//			throw ex;
//		}	
//	}

	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public List<Long> getCallCenterGroups(String pbxFarmIP) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallCenterGroups: ").append(pbxFarmIP));
			
			List<Long> groups = callCenterReportManager.getCallCenterGroups(pbxFarmIP);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getCallCenterGroups: ").append(pbxFarmIP));
			return groups;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallCenterGroups was executed!", t);
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
	public String[] getCallCenterQueuePosition(int position, Long domainKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getCallCenterQueuePosition: ").append(position));
			
			String[] prompt = callCenterReportManager.getCallCenterQueuePositionPrompt(position, domainKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getCallCenterQueuePosition: ").append(position));
			return prompt;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallCenterQueuePosition was executed!", t);
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
	public List<UsergroupInfo> getUsers(Long groupKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getUsers: ").append(groupKey));
			
			List<UsergroupInfo> users = groupManager.getUsers(groupKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getUsers: ").append(groupKey));
			return users;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getUsers was executed!", t);
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
	public List<CallCenterPaReport> getPaReportByGroupReport(Long groupReportKey) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getPaReportByGroupReport: ").append(groupReportKey));
			
			List<CallCenterPaReport> reports = callCenterReportManager.getPaReportByGroupReport(groupReportKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getPaReportByGroupReport: ").append(groupReportKey));
			return reports;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getPaReportByGroupReport was executed!", t);
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
	public void unLoggedAllPAs(String pbxFarmIP) throws PBXServerException
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start unLoggedAllPAs: ").append(pbxFarmIP));
			
			callCenterReportManager.unloggedAllPAs(pbxFarmIP);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End unLoggedAllPAs: ").append(pbxFarmIP));			
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when unLoggedAllPAs was executed!", t);
			logger.error(ex.getLocalizedMessage());
			//throw ex;
		}	
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public boolean hasNoActiveCall(Long pbxuserKey) throws PBXServerException
	{
		try
		{
			return acdGroupManager.hasNoActiveCall(pbxuserKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when hasNoActiveCall was executed!", t);
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
	public List<Forward> getForwardListByConfig(Long configKey) throws PBXServerException
	{
		try
		{
			return acdGroupManager.getForwardListByConfig(configKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getForwardListByConfig was executed!", t);
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
	public GroupPause getGroupPauseByGroupKeyAndPauseCode(Long groupKey, Integer pauseCode) throws PBXServerException
	{
		try
		{
			return pauseManager.getGroupPauseByGroupKeyAndPauseCode(groupKey, pauseCode);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getGroupPauseByGroupKeyAndPauseCode was executed!", t);
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
	public List<Address> getExtensionListByPbxuser(Long pbxuserKey) throws PBXServerException
	{
		try
		{
			return acdGroupManager.getExtensionListByPbxuser(pbxuserKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getExtensionListByPbxuser was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
}