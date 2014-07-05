package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Role;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserCentrexInfo;

public interface UserDAO extends DAO<User>, ReportDAO<User, UserCentrexInfo>
{
	/**
	 * Gets a user based on username and domain fiedls 
	 */
	public User getUserByUsernameAndDomain(String username, String domain) throws DAOException;
	
	public User getUserByGatewayKey(Long gatewayKey) throws DAOException;

	public List<User> getUsersInPBXWithPresence(Long domainKey) throws DAOException; 

	public List<User> getUsersInDomain(Long domainKey, Integer userType) throws DAOException;
	
	public User getUserByAddressAndDomain(String address, String domain) throws DAOException;
	
	/**
	 * Gets user type MusicServer and loads user and domain.
	 */
	public List<User> getMusicServerListByFarmIP(String farmIP) throws DAOException;

	/**
	 * Gets user type MusicServer and loads user and domain.
	 */
	public List<User> getMusicServerListByDomain(String domain) throws DAOException;
	
	public List<User> getParkServerListByDomain(String domain) throws DAOException;
	public User getParkServerByDomain(String domain) throws DAOException;
	
	public User getUserCentrexAdmin(String username, String domainName) throws DAOException;
	
	public User getUserWithPreference(Long userKey) throws DAOException;
	
	public List<User> getMediaAgentsList(Long domainKey) throws DAOException;

	/**
	 * Load User and Domain.
	 */
	public User getUserByEmail(String string) throws DAOException;
	
	public List<User> getUserCentrexList() throws DAOException;
	
	/**
	 * Gets user type AcdGroupServer and loads user and domain.
	 */
	public List<User> getAcdGroupServerListByFarmIP(String farmIP) throws DAOException;
	
	public List<User> getAcdGroupServerListByDomain(String domain) throws DAOException;
	
	/**
	 * Load User
	 */
	public User getUserByUsergroupKey(Long usergroupKey) throws DAOException;
	
	public User getUserWithRoleListByUsernameAndDomain(String username, String domain) throws DAOException;
}
