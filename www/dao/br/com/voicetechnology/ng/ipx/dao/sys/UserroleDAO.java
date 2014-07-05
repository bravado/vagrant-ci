package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userrole;

public interface UserroleDAO extends DAO<Userrole>
{
	List<Userrole> getUserroleListByUser(Long key) throws DAOException;
	
	List<Userrole> getUserroleListByRole(Long roleKey) throws DAOException;
}
