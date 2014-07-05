package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userfile;

public interface UserfileDAO extends DAO<Userfile>
{
	public List<Userfile> getUserFileListByFile(Long fileKey) throws DAOException;

	/**
	 * Loads Userfile, File and Calllog.
	 */
	public List<Userfile> getNewMessagesByUser(Long userKey) throws DAOException;
	
	public Long countNewMessagesByUser(Long userKey, boolean onlyNotYetNotified) throws DAOException;
	
	/**
	 * Loads Userfile, File and Calllog.
	 */
	public List<Userfile> getOldMessagesByUser(Long uKey) throws DAOException;
	
	public Long countOldMessagesByUser(Long uKey, boolean onlyNotYetNotified) throws DAOException;
	
	public Userfile getUserfileByUserKeyAndFileinfoKey(Long uKey, Long fileinfoKey) throws DAOException;

	public Long howMuchUserfileByFileKey(Long fileinfoKey) throws DAOException;

	/**
	 * Loads Userfile and Fileinfo.
	 */
	public Userfile getSalutationByUserKey(Long key) throws DAOException;

	public List<Userfile> getUserFileListByUser(Long userKey) throws DAOException;
	
	public List<Fileinfo> getFileListByUserKey(Long uKey) throws DAOException;
	
	/**
	 *  Retorna a lista de usuários que devem receber email de notificação de mensagens de voice mail  
	 */
	public List<Userfile> getEmailNotificationList(boolean attachFile) throws DAOException;
	
	public Userfile getUserFileByCallLog(Long callLogKey) throws DAOException;
}