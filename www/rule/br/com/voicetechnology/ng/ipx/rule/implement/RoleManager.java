package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.PermissionDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RoleDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RolepermissionDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserroleDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Permission;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Role;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Rolepermission;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userrole;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.RoleInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.pojo.ws.WebServiceConstantValues;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;
 
public class RoleManager extends Manager 
{
	private ReportDAO<Role, RoleInfo> reportRole;
	private RoleDAO roleDAO;
	private UserroleDAO userRoleDAO;
	private PermissionDAO permissionDAO;
	private RolepermissionDAO rolePermissionDAO;
	
	public RoleManager(String loggerPath) throws DAOException 
	{
		super(loggerPath);
		reportRole = dao.getDAO(RoleDAO.class);
		roleDAO = dao.getDAO(RoleDAO.class);
		userRoleDAO = dao.getDAO(UserroleDAO.class);
		permissionDAO = dao.getDAO(PermissionDAO.class);
		rolePermissionDAO = dao.getDAO(RolepermissionDAO.class);
	}

	public ReportResult findRole(Report<RoleInfo> info) throws DAOException
	{
		Long size = reportRole.getReportCount(info);
		List<Role> roleList = reportRole.getReportList(info);
		List<RoleInfo> roleInfoList = new ArrayList<RoleInfo>(roleList.size());
		for(Role role : roleList)
			roleInfoList.add(new RoleInfo(role));
		return new ReportResult<RoleInfo>(roleInfoList, size);
	}
	
	public void deleteRole(List<Long> roleKeys) throws DAOException, DeleteDependenceException, ValidateObjectException, ValidationException
	{
		for(Long key : roleKeys)
			this.deleteRole(key);
	}
	
	private void deleteRole(Long roleKey) throws DAOException, DeleteDependenceException, ValidateObjectException, ValidationException
	{
		Role role = roleDAO.getByKey(roleKey);
		List<Userrole> userRoleList = userRoleDAO.getUserroleListByRole(roleKey);
		if(userRoleList != null && userRoleList.size() > 0)
			throw new DeleteDependenceException("Cannot delete role " + role.getName() + " because it's in use by " + userRoleList.size() + " users!!!", User.class, userRoleList.size(), role);

		if(role.getName().equals(Role.ROLE_NAME_ADMINISTRATOR) || role.getName().equals(Role.ROLE_NAME_USER) 
				|| role.getName().equals(Role.ROLE_NAME_CENTREXADMIN) || role.getName().equals(Role.ROLE_NAME_DEFAULT))
			throw new ValidationException("Cannot delete this Role because its a reserved role on system!!!", WebServiceConstantValues.RESULT_CODE_ROLE_IS_RESERVED_ON_SYSTEM);

		role.setActive(Role.DEFINE_DELETED);
		roleDAO.save(role);
	}

	public RoleInfo getRoleInfoByKey(Long roleKey) throws DAOException, ValidationException
	{
		return this.getRoleInfo(roleKey, false);
	}
	
	public RoleInfo getRoleInfoByKey(Long roleKey, boolean onlyView) throws DAOException, ValidationException
	{
		return this.getRoleInfo(roleKey, onlyView);
	}
	
	private RoleInfo getRoleInfo(Long rolekey, boolean onlyView) throws DAOException, ValidationException
	{
		Role role = roleDAO.getByKey(rolekey);
		if(!onlyView)
			if(role.getName().equals(Role.ROLE_NAME_ADMINISTRATOR) || role.getName().equals(Role.ROLE_NAME_USER) || role.getName().equals(Role.ROLE_NAME_CENTREXADMIN))
				throw new ValidationException("Cannot delete this Role because its a reserved role on system!!!", WebServiceConstantValues.RESULT_CODE_ROLE_IS_RESERVED_ON_SYSTEM);
		RoleInfo roleInfo = new RoleInfo(role);
		getRoleInfoContext(roleInfo);

		List<Permission> permissionInList = permissionDAO.getPermissionListByRole(role.getKey());
		for(Permission perm : permissionInList)
			roleInfo.addPermissionKey(perm.getKey());
		return roleInfo;
		
	}

	public void save(RoleInfo roleInfo) throws DAOException, ValidateObjectException
	{
		boolean edit = roleInfo.getKey() != null;
		Role role = roleInfo.getRole();
		role.setActive(Role.DEFINE_ACTIVE);
		validateSave(role);
		roleDAO.save(role);
		this.manageRolePermission(roleInfo.getPermissionKeyList(), role.getKey(), edit);
	}

	private void manageRolePermission(List<Long> permissionInList, Long roleKey, boolean edit) throws DAOException, ValidateObjectException
	{
		if(edit)
		{
			List<Rolepermission> rolePermissionList = rolePermissionDAO.getListByRole(roleKey);
			for(Rolepermission rolePermission : rolePermissionList)
				rolePermissionDAO.remove(rolePermission);
		}
		for(Long perm : permissionInList)
		{
			if(perm != null)
			{
				Rolepermission rolePermission = new Rolepermission();
				rolePermission.setPermissionKey(perm);
				rolePermission.setRoleKey(roleKey);
				rolePermissionDAO.save(rolePermission);
			}
		}
	}

	public void getRoleInfoContext(RoleInfo roleInfo) throws DAOException
	{
		roleInfo.addPermissionList(permissionDAO.getPermissionKeyAndLabelList());
	}
	
	private void validateSave(Role role) throws ValidateObjectException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(role == null)
		{
			errorList.add(new ValidateError("Role is null!", Role.class, null, ValidateType.BLANK));
		} else
		{
			String name = role.getName();
			if(name == null || name.length() < 1)
				errorList.add(new ValidateError("Role name is null!", Role.Fields.NAME.toString(), Role.class, role, ValidateType.BLANK));
		}
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
	}
}