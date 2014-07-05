package br.com.voicetechnology.ng.ipx.dao.pbx;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;

public interface PbxpreferenceDAO extends DAO<Pbxpreference>
{

	Pbxpreference getByDomain(String domain) throws DAOException;
	
	public Pbxpreference getByDomainKey(Long domainKey) throws DAOException;
	
	public Pbxpreference getByPbxKey(Long pbxKey) throws DAOException;
	
}
