package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PBXInfo;

public interface PbxDAO extends DAO<Pbx>, ReportDAO<Pbx, PBXInfo>
{    
	/**
	 * Load only PBX. 
	 */
	public Pbx getPbxByDomain(Long domainKey) throws DAOException;
	
	/**
	 * Load only PBX. 
	 */
	public Pbx getPbxByDomain(String domain) throws DAOException;

	/**
	 *	Loads PBX, User, Default Address, Nightmode Address and Domain.
	 */
	public Pbx getPbxFullByDomain(String domain) throws DAOException;

	/**
	 * Loads PBX and User.
	 */
	public Pbx getPbxFull(Long pbxKey) throws DAOException;

	/**
	 * Loads PBX with User and Domain.
	 */
	public List<Pbx> getPbxListByFarmIP(String farmIP) throws DAOException;

	/**
	 * Loads all PBX with User and Domain.
	 */
	public List<Pbx> getAllPbxList() throws DAOException;
	
	/**
	 *	Gets PBX Locale by Domain. 
	 */
	public String getLocaleByDomain(String domain) throws DAOException;
	
	public Long validateAccoundID(Integer accountID, Long pbxKey) throws DAOException;
	
	public List<Pbx> getAllPbxWithPbxPreference() throws DAOException;
	
	public Integer getMaxPAsOnlineNumber(Long pbxKey) throws DAOException;
}
