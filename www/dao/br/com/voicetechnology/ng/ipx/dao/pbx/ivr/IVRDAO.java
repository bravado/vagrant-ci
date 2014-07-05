package br.com.voicetechnology.ng.ipx.dao.pbx.ivr;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVR;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;

import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.IVRInfo;

public interface IVRDAO extends DAO<IVR>, ReportDAO<IVR, IVRInfo>
{
	/**
	 * Loads IVR, IVROptions, Pbxuser, User, Config, Preference and Presence. 
	 */
	public IVR getIVRFull(Long ivrKey) throws DAOException;
	
	public IVR getIVRWithOptions(Long ivrKey) throws DAOException;

	public Long countIVRs(Long domainKey) throws DAOException;

	public List<IVR> getIVRListByFarmIP(String farmIP) throws DAOException;

	public Long countIVRsUsingFile(Long domainKey, Long fileinfoKey) throws DAOException;

	public Long countForwardsToAddress(Long addressKey) throws DAOException;
	
	public Block getOutgoingBlockByIVRKey(Long ivrKey) throws DAOException;
	
	public List<IVR> getIVRListByForwards(Long addressKey) throws DAOException; //tveiga delete user forced from web services
		
	public List<IVR> getIVRListByDomain(String domain, String searchWord) throws DAOException;
	
	public List<IVR> getIVRListByDomain(String domain, Integer lastIndex, Integer maxResult) throws DAOException;
	
	public List<IVR> getIVRListByDomain(String domain) throws DAOException;
	
	public List<Duo<Long, String>> getIVRKeyAndNameList(Long domainKey) throws DAOException;
	
	public List<Duo<Long, String>> getIVRKeyAndNameList(Long domainKey,Integer lastIndex, Integer maxResult) throws DAOException;
	
	public List<IVR> getIVRsByAddress(Long addressKey) throws DAOException;
	
	public IVR getIVRByNameAndDomainName(String domainName, String ivrName) throws DAOException;
	
	public List<Address> getAddressByIVRNameAndDomainName(String domainName, String ivrName) throws DAOException;
	
		public List<IVR> getIVRsByTarget(Long addressKey) throws DAOException;
}