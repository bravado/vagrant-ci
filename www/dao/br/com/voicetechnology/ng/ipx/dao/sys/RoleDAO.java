package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Role;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.RoleInfo;

public interface RoleDAO extends DAO<Role>, ReportDAO<Role, RoleInfo>
{
	public List<Role> getRoleListByPbxuser(Long pbxuserKey) throws DAOException;

	public Role getDefaultRole() throws DAOException;
	
	public Role getAdminRole() throws DAOException;
	
	public List<Role> getRoleListByCentrexUser(Long userKey) throws DAOException;
	
	public List<Duo<Long, String>> getRoleKeyAndNameListMinusDefaultRole() throws DAOException;
	
	public Role getRoleByName(String roleName) throws DAOException;
	
	public Role getSimpleRole() throws DAOException;
	
	public Role getRoleCentrexAdmin() throws DAOException;
	
	public List<Role> getRoleListMinusDefaultAndRootRole() throws DAOException;
}