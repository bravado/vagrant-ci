package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAONotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PhoneDialPlanDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PhoneDialPlanGroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PhoneDialPlanGroupElemDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlanGroup;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlanGroupElem;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PBXInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PhoneDialPlanGroupInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class PhoneDialPlanGroupManager extends Manager
{
	private PhoneDialPlanGroupDAO phoneDialPlanGroupDAO;
	private PhoneDialPlanGroupElemDAO phoneDialPlanGroupElemDAO;
	private PhoneDialPlanDAO phoneDialPlanDAO;
	private PbxuserDAO pbxuserDAO;
	private TerminalDAO terminalDAO;

	public PhoneDialPlanGroupManager(Logger logger) throws DAONotFoundException
	{
		super(logger);
		phoneDialPlanGroupDAO = dao.getDAO(PhoneDialPlanGroupDAO.class);
		pbxuserDAO = dao.getDAO(PbxuserDAO.class);
		phoneDialPlanDAO = dao.getDAO(PhoneDialPlanDAO.class);
		phoneDialPlanGroupElemDAO = dao.getDAO(PhoneDialPlanGroupElemDAO.class);
		terminalDAO = dao.getDAO(TerminalDAO.class);
	}
	
	public ReportResult find(Report<PhoneDialPlanGroupInfo> report) throws DAOException
	{	
		Long size = phoneDialPlanGroupDAO.getReportCount(report);
		List<PhoneDialPlanGroup> phoneDialPlanGroups= phoneDialPlanGroupDAO.getReportList(report);				
		
		List<PhoneDialPlanGroupInfo> infos = new ArrayList<PhoneDialPlanGroupInfo>();
		for(PhoneDialPlanGroup group : phoneDialPlanGroups){
			infos.add(new PhoneDialPlanGroupInfo(group));
		}
		return new ReportResult<PhoneDialPlanGroupInfo>(infos, size);
	}

	public PhoneDialPlanGroupInfo getPhoneDialPlanGroupContext(Long phoneDialPlanGroupKey, Long domainKey) throws DAOException
	{
		PhoneDialPlanGroupInfo info = new PhoneDialPlanGroupInfo();
		List<PhoneDialPlan> domainDialPlans = phoneDialPlanDAO.getPhoneDialPlansByDomain(domainKey, PhoneDialPlan.TYPE_GROUP);
		List<Pbxuser> pbxuserWithoutDialPlanGroup = pbxuserDAO.getPbxUserWithoutPhoneGroupDialPlan(domainKey, User.TYPE_PBXUSER);
		List<Pbxuser> terminalsWithoutDialPlanGroup = pbxuserDAO.getPbxUserWithoutPhoneGroupDialPlan(domainKey, User.TYPE_TERMINAL);
		
		if(phoneDialPlanGroupKey != null){
			PhoneDialPlanGroup phoneDialPlanGroup = phoneDialPlanGroupDAO.getByKey(phoneDialPlanGroupKey);
			info.setPhoneDialPlanGroup(phoneDialPlanGroup);
			info.setPbxusers(pbxuserDAO.getPbxUserByPhoneDialPlanGroup(phoneDialPlanGroupKey, User.TYPE_PBXUSER));
			info.setPhoneDialPlanList(phoneDialPlanDAO.getPhoneDialPlansByGroup(phoneDialPlanGroupKey));
			for(PhoneDialPlan dialPlan : info.getPhoneDialPlanList())
				domainDialPlans.remove(dialPlan);
		}		
		
		info.setPbxuserWithoutDialPlanGroup(pbxuserWithoutDialPlanGroup);		
		info.setDomainPhoneDialPlans(domainDialPlans);		
		
		return info;
	}

	public void save(PhoneDialPlanGroupInfo info) throws DAOException, ValidateObjectException
	{	
		PhoneDialPlanGroup group = info.getPhoneDialPlanGroup();
		phoneDialPlanGroupDAO.save(group);
		
		List<PhoneDialPlanGroupElem> oldElems = phoneDialPlanGroupElemDAO.getByGroupKey(group.getKey());
		
		for(PhoneDialPlanGroupElem elem : oldElems){
			phoneDialPlanGroupElemDAO.remove(elem);
		}
		
		int priority = 1;
		for(PhoneDialPlan dialPlan : info.getPhoneDialPlanList()){		
			PhoneDialPlanGroupElem elem = new PhoneDialPlanGroupElem();
			elem.setPriority(priority++);
			elem.setPhoneDialPlanGroupKey(group.getKey());
			elem.setPhoneDialPlanKey(dialPlan.getKey());
			phoneDialPlanGroupElemDAO.save(elem);
		}
		
		List<Pbxuser> oldUsers = pbxuserDAO.getPbxUserByPhoneDialPlanGroup(group.getKey(), null);
		
		for(Pbxuser user : oldUsers){
			user.setPhoneDialPlanGroupKey(null);
			pbxuserDAO.save(user);
		}		
		
		List<Pbxuser> pbxusers = info.getPbxusers();		
		
		for(Pbxuser user : pbxusers){
			Pbxuser newUser = pbxuserDAO.getByKey(user.getKey());
			newUser.setPhoneDialPlanGroupKey(group.getKey());
			pbxuserDAO.save(newUser);
		}		
	}

	public void deletePhoneDialPlanGroups(List<Long> keysList) throws DAOException, ValidateObjectException, DeleteDependenceException{
		for(Long key : keysList){
			deletePhoneDialPlanGroup(key);
		}
	}
	
	public void deletePhoneDialPlanGroup(Long key) throws DAOException, ValidateObjectException, DeleteDependenceException{
		PhoneDialPlanGroup group = phoneDialPlanGroupDAO.getByKey(key);		
		List<Pbxuser> pbxusers = pbxuserDAO.getPbxUserByPhoneDialPlanGroup(key, null);
		
		if(pbxusers.size() > 0)
			throw new DeleteDependenceException("There are users using this group!", PhoneDialPlanGroup.class, (long) pbxusers.size(), group);
		
		for(Pbxuser pbxuser : pbxusers){
			pbxuser.setPhoneDialPlanGroupKey(null);
			pbxuserDAO.save(pbxuser);
		}
		
		List<PhoneDialPlanGroupElem> items = new ArrayList<PhoneDialPlanGroupElem>();
		items = phoneDialPlanGroupElemDAO.getByGroupKey(key);
		
		for(PhoneDialPlanGroupElem item : items){
			phoneDialPlanGroupElemDAO.remove(item);
		}
		
		phoneDialPlanGroupDAO.remove(group);
	}

}
