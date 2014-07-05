package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.callcenter.CallCenterCallLog;

public interface CallCenterCallLogDAO extends DAO<CallCenterCallLog>
{
	public CallCenterCallLog getCallLogBykey(Long key) throws DAOException;
	
	public List<CallCenterCallLog> getCallLogByDomainkey(Long domainKey) throws DAOException;
	
	public List<CallCenterCallLog> getCallLogAll(String domain) throws DAOException;
	
	/**
	 * @param key
	 *  @return Retorna o CallCenterCallLog com a lista de CallCenterCallEvents
	 */

	public Long getEspecifCallLogCount(String origination, String destination, String callsuccess, String did, 
			String usernamePA, Calendar startCallDate, Calendar endCallDate, Long domainKey, Long groupKey) throws DAOException;
	
	public CallCenterCallLog getCallLogByCallId(String callId) throws DAOException;
	
	public Long countCallLogs(String domain) throws DAOException;
	
	public List<CallCenterCallLog> findCallLogEntries(String origination, String destination, String callsuccess, String did, String usernamePA, 
			Calendar startCallDate, Calendar endCallDate, Long domainKey, Long groupKey, Integer firstResult, Integer maxResults) throws DAOException;
	
	public CallCenterCallLog getCallLogByCallEventKey(Long callEventKey) throws DAOException;
}
