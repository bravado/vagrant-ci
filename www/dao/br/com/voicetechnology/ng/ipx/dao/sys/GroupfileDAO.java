package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Groupfile;

public interface GroupfileDAO extends DAO<Groupfile>
{
	/**
	 * Gets only Groupfile.
	 */
	public List<Groupfile> getGroupFileListByFile(Long key) throws DAOException;

	/**
	 * Loads Groupfile and Fileinfo.
	 */
	public Groupfile getSalutationByGroupKey(Long key, Integer useType) throws DAOException;

	/**
	 * Gets Groupfile by Group key and Fileinfo key.
	 */
	public Groupfile getGroupfileByGroupKeyAndFileinfoKey(Long key, Long fileinfoKey) throws DAOException;
	
	/**
	 * Gets Groupfile by CallogKey
	 */
	public Groupfile getGroupfileByCalllog(Long calllogKey) throws DAOException;
	/**
	 * Gets Groupfile by File Key
	 */
	public Groupfile getGroupfileByFileinfoKey(Long fileinfoKey) throws DAOException;
	/**
	 * Loads Groupfile and associated Fileinfo.
	 */
	public List<Groupfile> getNewMessages(Long gKey) throws DAOException;
	
	/**
	 * Loads Groupfile and associated Fileinfo.
	 */
	public List<Groupfile> getOldMessages(Long gKey) throws DAOException;

	public Long howMuchGroupfileByFileKey(Long fileinfoKey) throws DAOException;

	public List<Groupfile> getEmailNotificationList(boolean attachFile) throws DAOException;

	public Long countNewMessagesByGroup(Long gKey, boolean onlyNotYetNotified) throws DAOException;

	public Long countOldMessagesByGroup(Long gKey, boolean onlyNotYetNotified) throws DAOException;
	
	public Long countNewMessagesByGroupAdmin(Long pbxuserKey, boolean onlyNotYetNotified) throws DAOException;

	public Long countOldMessagesByGroupAdmin(Long  pbxuserKey, boolean onlyNotYetNotified) throws DAOException;
	
}