package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.enums.FieldEnum;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PbxuserInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SimpleContactInfo;


public interface PbxuserDAO extends DAO<Pbxuser>, ReportDAO<Pbxuser, PbxuserInfo>
{
	/**
	 * Gets Pbxuser with User, Config, Preference, Presence and Domain.
	 * @throws DAOException
	 */
	public Pbxuser getPbxuserFull(Long pbxuserKey) throws DAOException;
	
	/**
	 * @param pbxuserKey
	 * @return Pbxuser with its user
	 */
	public Pbxuser getPbxuserAndUser(Long pbxuserKey) throws DAOException;
	
	public List<Duo<Long, String>> getPbxuserKeyAndUsernameList(Long domain, String... excludeUsers) throws DAOException;
	
	public List<Duo<Long, String>> getPbxuserKeyAndUsernameByTerminal(Long terminalKey, Long domainKey, boolean pbxuser) throws DAOException;
	
	public List<Duo<Long, String>> getPbxuserKeyAndUsernameByTerminal(Long terminalKey, Long domainKey, boolean pbxuser, Integer lastIndex, Integer maxResult) throws DAOException;
	
	public Pbxuser getPbxuserByTerminal(String terminalName, String domain) throws DAOException;

	public Long countUsers(Long domainKey) throws DAOException;

	/**
	 * Loads Pbxuser and User.
	 */
	public Pbxuser getPbxuserByAddressAndDomain(String address, String domain) throws DAOException;
	
	public Pbxuser getPbxuserByAddressAndDomainKey(String address, Long  domainKey) throws DAOException;

	public Pbxuser getPbxuserByAddressKey(Long address) throws DAOException;

	public List<Pbxuser> getVoicemailListByFarmIP(String farmIP) throws DAOException;	
	
	public List<Pbxuser> getVoicemailListByDomain(String Domain) throws DAOException;
	
	public List<Pbxuser> getPbxuserListByDefaultDID(Long didKey) throws DAOException;
	
	public List<Pbxuser> getPbxuserAndUsersInDomain(Long domainKey) throws DAOException;
	
	public List<Pbxuser> getMediaAgentsInDomain(Long domainKey) throws DAOException;
	
	public List<Pbxuser> getMediaAgentToConfiguration(Long domainKey, Integer type) throws DAOException;
	
	public List<Pbxuser> getCallCenterAgentToConfiguration(Long domainKey) throws DAOException;
	
	public List<Pbxuser> getSipTrunkUsers(Long domainKey) throws DAOException;
	/**
	 *	Loads Pbxuser, User and Domain. 
	 */
	public Pbxuser getPbxuserByEmail(String email) throws DAOException;

	/**
	 * Loads Pbxuser, User and Domain.
	 */
	public Pbxuser getPbxuserByUsernameAndDomain(String username, String domain) throws DAOException;
	
	public Pbxuser getActiveOrInactivePbxuserByUsernameAndDomain(String username, String domain) throws DAOException;

	/**
	 * Loads Pbxuser, User and Domain.
	 */
	public Pbxuser getPbxuserByUsernameAndDomain(String username, String domain, Integer status) throws DAOException;
	
	/**
	 * Loads Pbxuser, User and Domain. 
	 */
	public Pbxuser getKoushiKubunOwnerByNumber(String extension) throws DAOException;

	public List<SimpleContactInfo> getUsersAndPbxusersWithPresenceByDomain(Long domainKey, List<Integer> limits) throws DAOException;

	public List<SimpleContactInfo> getUsersAndPbxusersWithPresenceByDomain(Long domainKey, String searchWord, FieldEnum field, Integer parameterOrder) throws DAOException;

	public Pbxuser getPbxuserByConfig(Long configKey) throws DAOException;
	
	public List<Pbxuser> getPbxuserByPartOfUsernameOrName(Long domainKey, String searchWord, FieldEnum field) throws DAOException;
	
	public Pbxuser getPbxuserByPresence(Long presenceKey) throws DAOException;
	
	public Pbxuser getPbxuserWithConfigByKey(Long key) throws DAOException;
	
	public boolean isGroupAdministrator(Long pbxuserkey) throws DAOException; //tveiga issue 4329
	
	public List<Long> getGroupkeyListOfGroupAdministrator(Long pbxuserkey) throws DAOException; //tveiga issue 4329
	
	public List<Pbxuser> getPbxusersByAllowedRecordCall(Long pbxKey, Integer allowedRecordCall, Long pbxuserKey) throws DAOException;
	
	public List<Pbxuser> getPbxusersByAllowedVideoCall(Long pbxKey, Integer allowedRecordCall, Long pbxuserKey) throws DAOException;	

	public Long countPbxuserRecordCall(Long domainKey) throws DAOException;
	
	public Long countPbxuserVideoCall(Long domainKey) throws DAOException;

	public List<Pbxuser> getPbxUserWithoutPhoneGroupDialPlan(Long domainKey, Integer userAgent) throws DAOException;
	
	public List<Pbxuser> getPbxUserByPhoneDialPlanGroup(Long phoneDialPlanGroupKey, Integer userAgent) throws DAOException;
	
	public List<Pbxuser> getPbxUsers(Long ... keys) throws DAOException;

	public List<Pbxuser> getPbxusersWithConfigByDomain(Long domainKey, Integer ... agents) throws DAOException;
	
	public void updateCostCenters(Long[] checkeds, Long[] uncheckeds) throws DAOException;
	
	public Pbxuser getPbxuserWithConfigByAddressAndDomain(String address, String domain, Integer agentUser) throws DAOException;
	
	public Pbxuser getPbxuserWithConfigByTerminal(String terminalName, String domain) throws DAOException;
	
	public List<Duo<Long, String>> getSipTrunkKeyAndUsernameList(Long domainKey, String... excludeUsers) throws DAOException;
}

