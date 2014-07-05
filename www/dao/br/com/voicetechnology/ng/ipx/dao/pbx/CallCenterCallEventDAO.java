package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.callcenter.CallCenterCallEvent;

public interface CallCenterCallEventDAO extends DAO<CallCenterCallEvent>
{
	public CallCenterCallEvent getCallCenterCallEventBykey(Long key) throws DAOException;
	
	public LinkedList<CallCenterCallEvent> getCallEventByCallLogKey(Long callCenterCallLogKey, Long domainKey) throws DAOException;
	
	public List<CallCenterCallEvent> getCallEventByEventType(Integer eventType) throws DAOException;
	
	public List<CallCenterCallEvent> getCallEventByEventTypeAndPbxuserKey(Integer eventType, Long pbxuserKey) throws DAOException;
	
	public LinkedList<CallCenterCallEvent> getCallEventAll(Long domainKey) throws DAOException;
	
	public Long countCallEvents(String domain) throws DAOException;
	
	public List<CallCenterCallEvent> findCallEventEntries(Long callLogKey, Integer eventType, String username, Calendar startCallDate, Calendar endCallDate, 
			Integer firstResult, Integer maxResults, Long domainKey, Long groupKey) throws DAOException;
	
	public LinkedList<CallCenterCallEvent> getCallEventList(Long callCenterCallLogKey, Integer eventType, String username, Calendar startCallDate, 
			Calendar endCallDate, Integer firstResult, Integer maxResults, Long domainKey, Long groupKey) throws DAOException;
	
	public Long getEspecifCallEventCount(Long callLogKey, Integer eventType, String username, 
			Calendar startCallDate, Calendar endCallDate, Long domainKey, Long groupKey) throws DAOException;
}
