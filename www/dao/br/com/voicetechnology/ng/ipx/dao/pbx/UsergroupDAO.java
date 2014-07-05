package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserInACDGroupsInfo;

public interface UsergroupDAO extends DAO<Usergroup>, ReportDAO<Usergroup, UserInACDGroupsInfo>
{
	List<Usergroup> getUsergroupListByPbxuser(Long pbxuserKey) throws DAOException;

	List<Usergroup> getUsergroupListByGroup(Long groupKey) throws DAOException;
	
	List<Usergroup> getUsergroupListWithUserByGroup(Long groupKey) throws DAOException;

	/**
	 * Loads usegroup and group
	 */
	
	List<Usergroup> getUsergroupListByPbxuserWithGroup(Long pbxuserKey) throws DAOException;
	
	List<Usergroup> getUsergroupInACDByPbxuser(Long puKey) throws DAOException;

	Usergroup getUsergroupByPbxuserAndGroup(Long puKey, Long gKey) throws DAOException;

	List<Usergroup> getUsergroupListByPbxuserBroadCastMode(Long puKey) throws DAOException;

	Long getCountAdminGroupByPbxuser(Long puKey) throws DAOException;
	
	Long getCountAdminGroupWithVoicemailByPbxuser(Long puKey) throws DAOException;
	
	boolean verifyIsGroupAdmin(String pbxuserFrom, String groupName) throws DAOException;
	
	List<Usergroup> getUsergroupListByUsernameAndDomain(String username, String domain, boolean groupAdmin) throws DAOException;
	
	List<String> getCallCenterAdminGroupnameListByDomain(String username, String domain, boolean groupAdmin) throws DAOException;
	
	List<Usergroup> getUsergroupInACDByPbxuserAndGroupAddress(Long puKey, String groupAddress) throws DAOException;
	
	List<Usergroup> getUsergroupListByGroupKeyAndDomainKeyAndPbxuserKey(Long domainKey, Long groupKey, Long pbxuserKey) throws DAOException;
	List<Usergroup> getUsergroupListByGroupKeyAndDomainAndPbxuserKey(String domain, Long groupKey, Long pbxuserKey) throws DAOException;
	
	List<Pbxuser> getPbxuserListByGroupKeyAndDomain(Long groupKey, Long domainKey, String domain) throws DAOException;
	
	Usergroup getUsergroupFullByUsergroupKey(Long usergroupKey) throws DAOException; 
}
