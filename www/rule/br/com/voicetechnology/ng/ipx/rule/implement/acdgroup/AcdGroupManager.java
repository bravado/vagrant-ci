package br.com.voicetechnology.ng.ipx.rule.implement.acdgroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.file.FileUtils;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.ActivecallDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallCenterCallEventDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallCenterCallLogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.NightModeSchedulerDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipsessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.UsergroupDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.FileinfoDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.callcenter.CallCenterCallEvent;
import br.com.voicetechnology.ng.ipx.pojo.db.callcenter.CallCenterCallLog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.NightModeScheduler;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class AcdGroupManager extends Manager
{
	private UserDAO uDAO;
	private FileinfoDAO fDAO;
	private GroupDAO gDAO;
	private SipsessionlogDAO sslDAO;
	private AddressDAO addDAO;
	private CallCenterCallEventDAO callEventDAO;
	private CallCenterCallLogDAO callLogDAO;
	private UsergroupDAO ugDAO;
	private DomainDAO dmDAO;
	private NightModeSchedulerDAO nightModeSchedulerDAO;
	private ActivecallDAO aDAO;
	private ForwardDAO fwDAO;
	
	public AcdGroupManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		uDAO = dao.getDAO(UserDAO.class);
		fDAO = dao.getDAO(FileinfoDAO.class);
		gDAO = dao.getDAO(GroupDAO.class);
		sslDAO = dao.getDAO(SipsessionlogDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		callEventDAO = dao.getDAO(CallCenterCallEventDAO.class);
		callLogDAO = dao.getDAO(CallCenterCallLogDAO.class);
		ugDAO = dao.getDAO(UsergroupDAO.class);
		dmDAO = dao.getDAO(DomainDAO.class);
		nightModeSchedulerDAO = dao.getDAO(NightModeSchedulerDAO.class);
		aDAO = dao.getDAO(ActivecallDAO.class);
		fwDAO = dao.getDAO(ForwardDAO.class);
	}
	
	public List<User> getAcdGroupServerListByFarmIP(String farmIP) throws DAOException
	{
		List<User> uList = uDAO.getAcdGroupServerListByFarmIP(farmIP);
		setDefaultFile(uList);
		return uList;
	}

	public List<User> getAcdGroupServerListByDomain(String domain) throws DAOException
	{
		List<User> uList = uDAO.getAcdGroupServerListByDomain(domain);
		setDefaultFile(uList);
		return uList;
	}
	
	private void setDefaultFile(List<User> uList) throws DAOException
	{
		for(User u : uList)
			u.setFileList(getAcdGroupFileList(u.getKey()));
	}
	public List<Fileinfo> getAcdGroupFileList(Long domainKey) throws DAOException
	{
		List<Fileinfo> fileList = fDAO.getSimpleFilesInDomain(domainKey);

		if (fileList == null)
			fileList = new ArrayList<Fileinfo>();
		
		//adição da música default para qualquer domínio, caso ainda não existam files adicionados para o mesmo.
		Fileinfo defaultMusic = new Fileinfo();
		defaultMusic.setAbsoluteName(FileUtils.getRelativeBasePath() + IPXProperties.getProperty(IPXPropertiesType.FILES_DEFAULT_MUSIC_ON_HOLD));
		fileList.add(defaultMusic);

		return fileList;
	}
	
	public LinkedList<Usergroup> getCallCenterTargets(String username, String domain, int algorithmType) throws DAOException
	{
		LinkedList<Usergroup> uList = gDAO.getGroupACDCallCenterTargets(username, domain, algorithmType);
		for(Usergroup ug : uList)
		{
			ug.getPbxuser().setAddressList(getExtensionListByPbxuser(ug.getPbxuserKey()));
		}
		return uList;
	}
	
	public List<Address> getExtensionListByPbxuser(Long pbxuserKey) throws DAOException
	{
		return addDAO.getExtensionListByPbxuser(pbxuserKey);
	}
	
	public List<Sipsessionlog> getActiveSipsessionByUsernameAndDomain(String username, String domain) throws DAOException
	{
		List<Sipsessionlog> sslList = sslDAO.getActiveSipsessionByUsernameAndDomain(username, domain);
		return sslList;
	}
	
	public Integer getAlgorithmGroupTypeByUsernameAndDomain(String username, String domain) throws DAOException
	{
		Integer algorithmGroupType = gDAO.getAlgorithmGroupTypeByUsernameAndDomain(username, domain);
		return algorithmGroupType;
	}
	
	public List<Usergroup> getUsergroupListByPbxuserKey (Long pbxUserKey, Long groupKey) throws DAOException
	{
		List<Usergroup> usergroup = gDAO.getUsergroupListByPbxuserKey(pbxUserKey, groupKey);
		for(Usergroup ug : usergroup)
		{
			ug.getPbxuser().setAddressList(addDAO.getExtensionListByPbxuser(ug.getPbxuserKey()));
		}
		return usergroup;
	}
	
	public List<Usergroup> getUsergroupListByGroupKey (Long groupKey) throws DAOException
	{
		List<Usergroup> usergroup = gDAO.getUsergroupListByGroupKey(groupKey);
		for(Usergroup ug : usergroup)
		{
			ug.getPbxuser().setAddressList(addDAO.getExtensionListByPbxuser(ug.getPbxuserKey()));
		}
		return usergroup;
	}
	
	public List<Usergroup> getLoggedAndActiveUsergroupListByGroupKey (Long groupKey) throws DAOException
	{
		List<Usergroup> usergroup = gDAO.getLoggedAndActiveUsergroupListByGroupKey(groupKey);
		for(Usergroup ug : usergroup)
		{
			ug.getPbxuser().setAddressList(addDAO.getExtensionListByPbxuser(ug.getPbxuserKey()));
		}
		return usergroup;
	}
	
	public Group getGroupByGroupNameAndDomain (String groupname, String domain) throws DAOException
	{
		Group group = gDAO.getGroupByGroupNameAndDomain(groupname, domain);
		return group;
	}
	
	public Group getGroupByGroupKey (Long groupKey) throws DAOException
	{
		Group group = gDAO.getGroupByGroupKey(groupKey);
		return group;
	}
	
	public Address getAddressByKey (Long addressKey) throws DAOException
	{
		Address address = addDAO.getByKey(addressKey);
		return address;
	}

	public void saveCallCenterCallLog(CallCenterCallLog callLog) throws DAOException, ValidateObjectException 
	{
		validateCallCenterCallLog(callLog);
		callLogDAO.save(callLog);
		
		for (CallCenterCallEvent ce : callLog.getCallEventList())
			ce.setCallLogKey(callLog.getKey());
		
		saveCallCenterCallEvents(callLog.getCallEventList());		
	}

	private void saveCallCenterCallEvents(List<CallCenterCallEvent> callEventList) throws DAOException, ValidateObjectException 
	{
		for(CallCenterCallEvent callEvent : callEventList)
			callEventDAO.save(callEvent);
	}
	
	private void validateCallCenterCallLog(CallCenterCallLog callLog) throws ValidateObjectException 
	{	
		if(callLog == null)
			throw new ValidateObjectException("CallCenterCallLog is null, doesn't have any info! Please check this call!", CallCenterCallLog.class, callLog, ValidateType.INVALID);
		if(callLog.getCallEventList() == null || callLog.getCallEventList().isEmpty())
			throw new ValidateObjectException("CallCenterCallLog incorrect, doesn't have any Call Events! Please check this call!", CallCenterCallLog.class, callLog, ValidateType.INVALID);
	}
	
	public List<CallCenterCallLog> getCallLogAll(String domain) throws DAOException
	{
		List<CallCenterCallLog> callLogList = callLogDAO.getCallLogAll(domain);
		return callLogList;
	}
	
	public LinkedList<CallCenterCallEvent> getCallEventAll(Long domainKey) throws DAOException
	{
		LinkedList<CallCenterCallEvent> callEventList = callEventDAO.getCallEventAll(domainKey);
		return callEventList;
	}
	
	public LinkedList<CallCenterCallEvent> getCallEventByCallLogKey(Long callCenterCallLogKey, Long domainKey) throws DAOException
	{
		LinkedList<CallCenterCallEvent> callEventList = callEventDAO.getCallEventByCallLogKey(callCenterCallLogKey, domainKey);
		return callEventList;
	}

	public Long getEspecifCallLogCount(String origination, String destination, String callsuccess, String did, String usernamePA, 
			Calendar startCallDate, Calendar endCallDate, Long domainKey, Long groupKey) throws DAOException
	{
		Long callLogList = callLogDAO.getEspecifCallLogCount(origination, destination, callsuccess, did, usernamePA, startCallDate, endCallDate, domainKey, groupKey);
		return callLogList;
	}
	
	public User getUserByUsergroupKey(Long usergroupKey) throws DAOException
	{
		User u = uDAO.getUserByUsergroupKey(usergroupKey);
		return u;
	}
	
	public CallCenterCallLog getCallLogByCallId(String callId) throws DAOException
	{
		return callLogDAO.getCallLogByCallId(callId);
	}
	
	public Long countCallLogs(String domain) throws DAOException
	{
		return callLogDAO.countCallLogs(domain);
	}
	
	public CallCenterCallLog getCallLogBykey(Long key) throws DAOException
	{
		return callLogDAO.getCallLogBykey(key);
	}
	
	public List<CallCenterCallLog> findCallLogEntries(String origination, String destination, String callsuccess, String did, String usernamePA, 
			Calendar startCallDate, Calendar endCallDate, Long domainKey, Long groupKey, Integer firstResult, Integer maxResults) throws DAOException
	{
		return callLogDAO.findCallLogEntries(origination, destination, callsuccess, did, usernamePA, startCallDate, endCallDate, domainKey, groupKey, firstResult, maxResults);
	}
	
	public Long countCallEvents(String domain) throws DAOException
	{
		return callEventDAO.countCallEvents(domain);
	}
	
	public CallCenterCallLog getCallLogByCallEventKey(Long callEventKey) throws DAOException
	{
		return callLogDAO.getCallLogByCallEventKey(callEventKey);
	}
	
	public List<CallCenterCallEvent> findCallEventEntries(Long callLogKey, Integer eventType, String username, Calendar startCallDate, Calendar endCallDate, 
			Integer firstResult, Integer maxResults, Long domainKey, Long groupKey) throws DAOException
	{
		return callEventDAO.findCallEventEntries(callLogKey, eventType, username, startCallDate, endCallDate, firstResult, maxResults, domainKey, groupKey);
	}
	
	public Long getEspecifCallEventCount(Long callLogKey, Integer eventType, String username, 
			Calendar startCallDate, Calendar endCallDate, Long domainKey, Long groupKey) throws DAOException
	{
		return callEventDAO.getEspecifCallEventCount(callLogKey, eventType, username, startCallDate, endCallDate, domainKey, groupKey);
	}
	
	public List<Usergroup> getUsergroupListByUsernameAndDomain(String username, String domain, boolean groupAdmin) throws DAOException
	{
		return ugDAO.getUsergroupListByUsernameAndDomain(username, domain, groupAdmin);
	}
	
	public User getUserWithRoleListByUsernameAndDomain(String username, String domain) throws DAOException
	{
		return uDAO.getUserWithRoleListByUsernameAndDomain(username, domain);
	}
	
	public List<String> getCallCenterWCAGroupnameListByDomain(String domain) throws DAOException
	{
		return gDAO.getCallCenterWCAGroupnameListByDomain(domain);
	}
	
	public List<String> getCallCenterAdminGroupnameListByDomain(String username, String domain, boolean groupAdmin) throws DAOException
	{
		return ugDAO.getCallCenterAdminGroupnameListByDomain(username, domain, groupAdmin);
	}
	
	public List<Domain> getDomainsWithCallCenterGroups() throws DAOException
	{
		return dmDAO.getDomainsWithCallCenterGroups();
	}
	
	public Domain getDomain(String domain) throws DAOException
	{
		return dmDAO.getDomain(domain);
	}

	public List<Group> getCallCenterGroupByDomain(Long domainKey) throws DAOException {
		return gDAO.getCallCenterGroupsByDomain(domainKey);
	}

	public List<Group> getCallCenterGroupByDomain(String domain) throws DAOException{
		return gDAO.getCallCenterGroupsByDomain(domain);
	}
	
	public boolean isInWorkHour(Long groupKey) throws DAOException
	{
		Calendar actualTime = Calendar.getInstance();
		List<NightModeScheduler> schedulers = nightModeSchedulerDAO.getNightModeSchedulerByGroup(groupKey);		
		List<NightModeScheduler> workDaysSchedulers = new ArrayList<NightModeScheduler>();
		List<NightModeScheduler> holidaySchedulers = new ArrayList<NightModeScheduler>();		
		
		for(NightModeScheduler scheduler : schedulers)
		{			
			if(scheduler.isHoliday())
				holidaySchedulers.add(scheduler);
			else
				workDaysSchedulers.add(scheduler);
		}
		
		for(NightModeScheduler scheduler : holidaySchedulers)
		{			
			SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
			String todayDate = format.format(actualTime.getTime());
			String holidayDate = format.format(scheduler.getHoliday().getTime());
			if(todayDate.equals(holidayDate))
				return false;
		}
		
		for(NightModeScheduler scheduler : workDaysSchedulers)
		{			
			if(scheduler.isWorkTime(actualTime))
				return true;
		}
		
		return false;
	}
	
	public boolean hasNoActiveCall(Long pbxuserKey) throws DAOException
	{
		Long count = aDAO.howManyActiveCallByPbxuser(pbxuserKey);
		return !(count >0);
	}
	
	public List<Forward> getForwardListByConfig(Long configKey) throws DAOException
	{
		return fwDAO.getForwardListByConfig(configKey);
	}
}