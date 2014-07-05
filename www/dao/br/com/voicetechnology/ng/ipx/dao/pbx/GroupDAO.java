package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.LinkedList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.GroupInfo;

public interface GroupDAO extends DAO<Group>, ReportDAO<Group, GroupInfo>
{

	public List<Group> getGroupListByPbxuser(Long pbxuserKey) throws DAOException;

	/**
	 * Loads Group and Config.
	 */
	public Group getGroupFull(Long groupKey) throws DAOException;
	
	/**
	 * Loads Group, Config and Domain.
	 */
	public Group getGroupFullWithDomain(Long groupKey) throws DAOException;

	/**
	 * Loads Usergroup, Pbxuser and User.
	 */
	public List<Usergroup> getUsersInGroup(Long groupKey) throws DAOException;
	
	/**
	 * Load only User.
	 */
	public List<User> getAdminUsersInGroup(Long groupKey) throws DAOException;

	public List<Duo<Long,String>> getUsersOutGroup(Long groupKey, Long domainKey, int type) throws DAOException;

	public List<Duo<Long, String>> getGroupKeyAndNameByDomain(Long domainKey) throws DAOException;

	public List<String> getGroupRingTargets(String to, String domain) throws DAOException;

	public List<String> getGroupHuntTargets(String to, String domain, int algorithmType) throws DAOException;
	
	public List<String> getGroupACDTargets(String to, String domain, int algorithmType) throws DAOException;
	
	public Long countForwardsToAddress(Long addressKey) throws DAOException;
	
	public List<Group> getGroupListByNightmodeAddress(Long addressKey) throws DAOException;
	
	public Group getGroupByAddressAndDomain(String name, String domain) throws DAOException;
	
	public List<Group> getGroupsInPBX(Long pbxKey, String searchWord) throws DAOException;
	
	public List<Group> getGroupsInPBXByGroupType(Long pbxKey, int type) throws DAOException;
	
	public List<Group> getGroupsInPBX(Long pbxKey, Integer lastIndex, Integer maxResult) throws DAOException;

	public List<Group> getGroupsInPBX(Long pbxKey) throws DAOException;
	
	public Group getGroupByNameAndAssociatedUser(String groupName, String username, String domainName) throws DAOException;
	
	public Long countGroupsUsingFile(Long domainKey, Long fileKey) throws DAOException;

	public Group getGroupByGroupNameAndDomain(String groupName,	String domainName)throws DAOException;
	
	public Group getGroupByGroupKey(Long groupKey)throws DAOException;
	
	public Group getGroupByAddressKey(Long addressKey) throws DAOException;
	
	public Group getGroupAndServiceClassByDID(String did) throws DAOException;
	
	public List<Long> getPAsAmmountByPbxkey(Long pbxKey) throws DAOException;
	
	public List<Long> getPAsByPbxkey(Long pbxKey) throws DAOException;

	public List<String> getGroupSipTrunkTargets(String to, String domain, Integer algorithmType) throws DAOException;
	
	public Integer getAlgorithmGroupTypeByUsernameAndDomain(String username, String domain) throws DAOException;
	
	public LinkedList<Usergroup> getGroupACDCallCenterTargets(String to, String domain, int algorithmType) throws DAOException;
	
	public List<Usergroup> getUsergroupListByPbxuserKey (Long pbxUserKey, Long groupKey) throws DAOException;
	
	public List<Usergroup> getUsergroupListByGroupKey (Long groupKey) throws DAOException;
	
	public List<Usergroup> getLoggedAndActiveUsergroupListByGroupKey (Long groupKey) throws DAOException;

	public Integer getGroupTypeByKey(Long groupKey) throws DAOException;
	
	public List<String> getCallCenterWCAGroupnameListByDomain(String domain) throws DAOException;
	
	public List<Group> getCallCenterGroupsByDomain(Long domainKey) throws DAOException;

	public List<Long> getGroupkeyListByAddressAndDomainKey(String address, Long  domainKey) throws DAOException;
	
	public List<Group> getCallCenterGroupsByDomain(String domain) throws DAOException;
}