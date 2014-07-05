package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SipsessionlogInfo;

public interface SipsessionlogDAO extends DAO<Sipsessionlog>, ReportDAO<Sipsessionlog, SipsessionlogInfo>
{
	/**
	 * Gets all Sipsessionlog by Pbxuser. Each Sipsessionlog has sessionglog attribute loaded. 
	 */
	public List<Sipsessionlog> getSipsessionlogListByPbxuser(Long pbxuserKey) throws DAOException;
	
	/**
	 * Gets all Sipsessionlog that Sessionlog end is null. 
	 */
	public List<Sipsessionlog> getActiveSipsessionlogListByPbxuser(Long pbxuserKey) throws DAOException;
	
	/**
	 * Count how many active Sipsessionlog that Pbxuser has. 
	 */
	public Long getHowManySipSessionByPbxuser(Long pbxuserKey) throws DAOException;

	/**
	 * Count how many active Sipsessionlog that Pbxuser has, considering all terminals associated
	 */	
	public int getHowManyActiveSipSessionByPbxuser(Long pbxuserKey)throws DAOException;
	
	/**
	 * Gets only Sipsessionlog in return list. Active Sipsessionlog is that Sessionlog end is null.
	 */
	public List<Sipsessionlog> getActiveSipsessionByUsernameAndDomain(String username, String domain) throws DAOException;
	
	/**
	 * Gets only query to que sipsession of media in a farmip.
	 */
	public String getAllSipsessionsByFarmIPQuery(String farmIP) throws DAOException;
	
	/**
	 * Gets all opened Sipsessionlog (and related SessionLog). 
	 */
	public StringBuilder getOpenedSipsessionlogList(long timeout) throws DAOException;
	
	/**
	 * Remove all Sipsessionlog by keys. 
	 */
	public long removeSipSessionLog(String query, Long currentTime) throws DAOException;
	
	//jluchetta - BUG 6727 - Versão 3.0.5 RC6.6 - Alteracao para trabalhar com PBX somente em uma porta
	public long removeMediaSipSessionLog(String query) throws DAOException;
	
	/**
	 * Get number of all opened Sipsessionlog . 
	 */
	public Long getNumberOfOpenedSipsessionlog() throws DAOException;
	
	/**
	 * Gets a Sipsessionlog (and related SessionLog) based on contact field. 
	 */
	public Sipsessionlog getSipsessionlogByContact(String contact) throws DAOException;
	
	/**
	 * Gets a Sipsessionlog (and related SessionLog) based on contact field and domain name. 
	 */
	public Sipsessionlog getSipsessionlogByContactAndDomain(String contact, String domain) throws DAOException;
	
	/**
	 * Gets a Sipsessionlog (and related SessionLog) based on contact field. 
	 */
	public Sipsessionlog getSipsessionlogByUserKeyAndContact(Long userKey, String contact) throws DAOException;

	/**
	 * Gets an oppened Sipsessionlog (and related SessionLog) based on username and domain. 
	 */
	public Sipsessionlog getSipsessionlogByUserAndDomain(String username, String domain) throws DAOException;
	
	/**
	 * Gets a Sipsessionlog (and related SessionLog) based on sipsession key. 
	 */
	public Sipsessionlog getSipsessionlogAndSessionlog(Long sipsessionlogKey) throws DAOException;

	
	public Sipsessionlog getSipsessionlogBySessionlog(Long sessionlogKey) throws DAOException;
	
	/**
	 * Gets all Sipsessionlog (related SessionLog and subscriptions)  
	 */
	public List<Sipsessionlog> getAllSubscriptions(String event) throws DAOException;
	
	public List<Sipsessionlog> getActiveSipSessionLogListByTerminalOfPbxuser(Long pbxuserKey) throws DAOException;
	
	public Sipsessionlog getSipSessionByGatewayIP(Long userKey, String contact) throws DAOException;
	
	public List<String> getContactListUsingLoadBalance(String username, String domain) throws DAOException;
}
