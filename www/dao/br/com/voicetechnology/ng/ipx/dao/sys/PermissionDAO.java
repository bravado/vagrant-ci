package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Permission;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;

public interface PermissionDAO extends DAO<Permission>
{

	public List<Permission> getPermissionListByUser(Long userKey) throws DAOException;

	public List<Duo<Long, String>> getPermissionKeyAndLabelList() throws DAOException;
	
	public List<Permission> getPermissionListByRole(Long roleKey) throws DAOException;
}
