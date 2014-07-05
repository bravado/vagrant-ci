package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Categorycontact;

public interface CategorycontactDAO extends DAO<Categorycontact>
{
	public List<Categorycontact> getByCategoryKey(Long categoryKey) throws DAOException;
	
	public List<Categorycontact> getByContactKey(Long contactKey) throws DAOException;
	
	public List<Categorycontact> getListWithoutDefaultByContact(Long userKey, List<Long> contactKeys) throws DAOException;
}
