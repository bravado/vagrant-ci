package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.dao.pbx.PreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RoleDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.SessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserroleDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Preference;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Role;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userrole;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserCentrexInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class UserCentrexManager extends Manager 
{
	private UserDAO userDAO;
	private RoleDAO roleDAO;
	private PreferenceDAO preferenceDAO;
	private UserroleDAO userroleDAO;
	private SessionlogDAO slogDAO;
	private DomainDAO domainDAO;
	private ReportDAO<User, UserCentrexInfo> reportUserCentrex;
	
	public UserCentrexManager(String loggerPath) throws DAOException 
	{
		super(loggerPath);
		userDAO = dao.getDAO(UserDAO.class);
		roleDAO = dao.getDAO(RoleDAO.class);
		preferenceDAO = dao.getDAO(PreferenceDAO.class);
		userroleDAO = dao.getDAO(UserroleDAO.class);
		slogDAO = dao.getDAO(SessionlogDAO.class);
		reportUserCentrex = dao.getDAO(UserDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
	}

	public ReportResult findUserCentrex(Report<UserCentrexInfo> info) throws DAOException
	{
		Long size = reportUserCentrex.getReportCount(info);
		List<User> userCentrexList = reportUserCentrex.getReportList(info);
		List<UserCentrexInfo> userCentrexInfoList = new ArrayList<UserCentrexInfo>(userCentrexList.size());
		//List<Duo<Long, String>> roleFullList = roleDAO.getRoleKeyAndNameListMinusDefaultRole();
		for(User user : userCentrexList)
		{
			UserCentrexInfo userCentrexInfo = new UserCentrexInfo(user);
			//List<Role> roleList = roleDAO.getRoleListByCentrexUser(user.getKey());
			//for(Role role : roleList)
			//	userCentrexInfo.addRoleKey(role.getKey());
			userCentrexInfoList.add(userCentrexInfo);
		}
		return new ReportResult<UserCentrexInfo>(userCentrexInfoList, size);
	}
	
	public void deleteUserCentrex(List<Long> userCentrexKeys) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		for(Long userCentrexKey : userCentrexKeys)
			this.deleteUserCentrex(userCentrexKey);
	}
	
	private void deleteUserCentrex(Long userCentrexKey) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		User user = userDAO.getUserWithPreference(userCentrexKey);
		
		this.deleteUserCentrexDependences(user);
		
		user.setActive(User.DEFINE_DELETED);
		userDAO.save(user);
	}
	
	private void deleteUserCentrexDependences(User user) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		List<Sessionlog> sessionlogList = slogDAO.getSessionlogListByUserCentrex(user.getKey());
		if(sessionlogList != null && sessionlogList.size() > 0) 
			throw new DeleteDependenceException("Cannot delete " + user.getUsername() + ": " + sessionlogList.size() + " active web session(s) points to this user", Sessionlog.class, sessionlogList.size(), user);

		preferenceDAO.remove(user.getPreference());

		List<Userrole> urList = userroleDAO.getUserroleListByUser(user.getKey());
		for(Userrole ur : urList)
			userroleDAO.remove(ur);
	}
	
	public UserCentrexInfo getUserCentrexInfo(Long userCentrexKey) throws DAOException
	{
		User user = userDAO.getUserWithPreference(userCentrexKey);
		//List<Duo<Long, String>> roleFullList = roleDAO.getRoleKeyAndNameListMinusDefaultRole();
		UserCentrexInfo userCentrexInfo = new UserCentrexInfo(user);

		//List<Role> roleList = roleDAO.getRoleListByCentrexUser(userCentrexKey);
		//for(Role role : roleList)
		//	userCentrexInfo.addRoleKey(role.getKey());
		return userCentrexInfo;
	}
	
	public void saveUserCentrex(UserCentrexInfo userCentrexInfo) throws DAOException, ValidateObjectException
	{
		boolean edit = userCentrexInfo.getKey() != null;
		User user = userCentrexInfo.getUser();
		user.setAgentUser(User.TYPE_CENTREX);
		user.setActive(User.DEFINE_ACTIVE);
		validateSave(user);
		userDAO.save(user);

		this.createPreference(userCentrexInfo, user.getKey());

		if(!edit)
			this.createUserRoleCentrex(user.getKey());
	}
	
	private void createUserRoleCentrex(Long userKey) throws DAOException, ValidateObjectException
	{
		Role roleCentrexAdmin = roleDAO.getRoleCentrexAdmin();
		if(roleCentrexAdmin != null)
		{
			Userrole userRole = new Userrole();
			userRole.setRoleKey(roleCentrexAdmin.getKey());
			userRole.setUserKey(userKey);
			userroleDAO.save(userRole);
		}
	}
	
//	private void removeUserrole(Long userKey) throws DAOException
//	{
//		List<Userrole> userRoleList = userroleDAO.getUserroleListByUser(userKey);
//		for(Userrole userRole : userRoleList)
//			userroleDAO.remove(userRole);
//	}
	
	private void createPreference(UserCentrexInfo userCentrexInfo, Long userKey) throws DAOException, ValidateObjectException
	{
		Preference preference = userCentrexInfo.getPreference();
		preference.setClickToCallConfirmation(Preference.CLICKTOCALLCONFIRMATION_DISABLED);
		preference.setUserKey(userKey);
		preference.setLastCalllogView(Calendar.getInstance());
		preferenceDAO.save(preference);
	}

	protected void validateSave(User user) throws DAOException, ValidateObjectException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(user == null)
		{
			errorList.add(new ValidateError("Centrex User is null!", User.class, null, ValidateType.BLANK));
		} else
		{
			Long domainKey = user.getDomainKey();
			if(domainKey == null)
				errorList.add(new ValidateError("Centrex User domainKey is null!", User.Fields.DOMAIN_KEY.toString(), User.class, user, ValidateType.BLANK));

			String username = user.getUsername();
			if(username == null || username.length() < 1)
				errorList.add(new ValidateError("Centrex User username is null!", User.Fields.USERNAME.toString(), User.class, user, ValidateType.BLANK));

			String password = user.getPassword();
			if(password == null || password.length() < 1)
				errorList.add(new ValidateError("Centrex User password is null!", User.Fields.PASSWORD.toString(), User.class, user, ValidateType.BLANK));
		
			//tveiga issue 5912 - 3.0.6 - inicio 
			User u =  userDAO.getUserByEmail(user.getEmail());
			if (u != null)
			   errorList.add(new ValidateError("E-mail already exists!", User.Fields.EMAIL.toString(), User.class, user, ValidateType.DUPLICATED)); 
			//tveiga issue 5912 - 3.0.6 - fim  
		}
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
	}
}