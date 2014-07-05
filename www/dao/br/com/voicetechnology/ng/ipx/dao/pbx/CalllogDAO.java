/*
 * Created on 25/04/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CalllogInfo;

public interface CalllogDAO extends DAO<Calllog>, ReportDAO<Calllog, CalllogInfo>
{
	public Calllog getCallLogByCallID(String callID, int type) throws DAOException;

	public List<String> getMostDialedCalls(Long pbxuserKey, boolean lookCommands, List<Address> addressList, Integer limit) throws DAOException;
	
	public List<Calllog> getCalllogListByPbxKey(Long pbxKey) throws DAOException;

	public List<String> getCallLogNumbersExcludingUsersAndContacts(Long pbxuserKey, Long userKey, Long domainKey, Integer lastIndex, Integer maxResult) throws DAOException;

	public List<Calllog> getCallLogListByContact(Long contactKey) throws DAOException;

	public List<Calllog> getCallLogListByAnotherPbxuser(Long anotherPbxuserKey) throws DAOException;
	
	public Calllog getCallLogByCallIDAndPbxuser(String callID, Long pbxuserKey, int type) throws DAOException;
	
	public Calllog getCallLogByCallIDAndUser(String callID, Long userKey, int type) throws DAOException;
	
	public Calllog getCallLogByCallIDAndVoicemail(String callID, Long domainKey) throws DAOException;
	
	public String getListByCalledNumber(Long pbxuserKey, String number) throws DAOException;
	
	public List<Calllog> getActiveMissedCallsByPbxuser(Long pbxuserKey, int[] statusList) throws DAOException;
	
	public void updateCallLogsCostCenter(Long oldKey, Long newKey) throws DAOException;
}