package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.TerminalInfo;

public interface TerminalDAO extends DAO<Terminal>, ReportDAO<Terminal, TerminalInfo>
{

	public Terminal getTerminalFull(Long terminalKey) throws DAOException;

	/**
	 * Loads Terminal, Pbxuser and User.
	 */
	public Terminal getTerminalByAddress(String address, Long domainKey) throws DAOException;

	/**
	 * Loads Terminal, Pbxuser and User.
	 */
	public Terminal getTerminalByAddressAndDomain(String address, String domain) throws DAOException;
	
	public Terminal getTerminalByAddressAndDomain(String address, String domain, Integer status) throws DAOException;

	/**
	 * Loads a Pbxuser and User based on the terminal pbxuser key
	 */
	public Pbxuser getAssociatedPbxuserByTerminalPbxuserKey(Long terminalPbxuserkey) throws DAOException;
	
	public List<Terminal> getTerminalListByDomain(Long domainKey) throws DAOException;
	
	public List<Terminal> getTerminalListAssociatedWithPbxuser(Long pbxuserKey) throws DAOException;
	
	public List<Terminal> getTerminalListAssociatedWithPbxuser(Long pbxuserKey, Integer status) throws DAOException;

	/**
	 * Loads only User. 
	 * @throws DAOException
	 */
	public User getAssociatedPbxuserByTerminalUserKey(Long terminalUserKey) throws DAOException;
	
	public Terminal getTerminalByMacAddressAndDomain(String macAddress, String domainName) throws DAOException;
	
	public Terminal getTerminalByMacAddressAndDomain(String macAddress, String domainName, Integer status) throws DAOException;
	
	
	/**
	 * Loads User and Domain. 
	 * @throws DAOException
	 */
	public User getAssociatedUserAndDomainByTerminalUserKey(Long terminalUserKey) throws DAOException;		
}
