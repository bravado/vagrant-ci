package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;

public interface DomainDAO extends DAO<Domain>
{

	public Domain getDomainByPbx(Long pbxKey) throws DAOException;
	
	public List<Duo<Long, String>> getDomainsByRootDomain(Long domainRootKey) throws DAOException;

	public Domain getRootDomain() throws DAOException;
	
	public Domain getDomain(String domain) throws DAOException;	
	
	public List<Duo<Long, String>> getActiveDomainList() throws DAOException;		
	
	public Domain getDomainByPbxUserKey(Long puserKey) throws DAOException;
	
	public List<Domain> getDomainsWithCallCenterGroups() throws DAOException;
}