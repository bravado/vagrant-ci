package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAONotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.dao.pbx.PhoneDialPlanDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PhoneDialPlanGroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PhoneDialPlanGroupElemDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlanGroupElem;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.PhoneDialPlanResponse;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PhoneDialPlanInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class PhoneDialPlanManager extends Manager{

	private PhoneDialPlanDAO phoneDialPlanDAO;
	private PhoneDialPlanGroupDAO phoneDialPlanGroupDAO;
	private PhoneDialPlanGroupElemDAO elemDAO;
	private DomainDAO domainDAO;
	public PhoneDialPlanManager(Logger logger) throws DAONotFoundException {
		super(logger);
		phoneDialPlanDAO = dao.getDAO(PhoneDialPlanDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
		elemDAO = dao.getDAO(PhoneDialPlanGroupElemDAO.class);
		phoneDialPlanGroupDAO = dao.getDAO(PhoneDialPlanGroupDAO.class);
	}

	public ReportResult find(Report<PhoneDialPlanInfo> report) throws DAOException
	{	
		Long size = phoneDialPlanDAO.getReportCount(report);
		List<PhoneDialPlan> phoneDialPlans= phoneDialPlanDAO.getReportList(report);
					
		List<PhoneDialPlanInfo> infos = new ArrayList<PhoneDialPlanInfo>();
		for(PhoneDialPlan phoneDialPlan : phoneDialPlans){
			infos.add(new PhoneDialPlanInfo(phoneDialPlan));
		}
		
		return new ReportResult<PhoneDialPlanInfo>(infos, size);
	}
	
	public void save(PhoneDialPlanInfo info) throws DAOException, ValidateObjectException{
		phoneDialPlanDAO.save(info.getPhoneDialPlan());
	}

	public void deletePhoneDialPlans(List<Long> keysList) throws DAOException, ValidateObjectException, DeleteDependenceException 
	{
		for(Long key : keysList)
			deletePhoneDialPlan(key);
	}
	
	public void deletePhoneDialPlan(Long key) throws DAOException, ValidateObjectException, DeleteDependenceException{
		PhoneDialPlan plan = phoneDialPlanDAO.getByKey(key);
		if(plan.getType() == PhoneDialPlan.TYPE_GROUP)
		{
			List<PhoneDialPlanGroupElem> elems = elemDAO.getByPhoneDialPlanKey(plan.getKey());
			if(elems.size() > 0){				
				throw new DeleteDependenceException("The PhoneDialPlan is a member of one or more groups!", PhoneDialPlan.class, (long) elems.size(), plan);
			}
		}
		
		phoneDialPlanDAO.remove(plan);
	}

	public PhoneDialPlanResponse executePhoneDialPlan(String address, Long domainKey) throws DAOException 
	{
		Domain domain = domainDAO.getByKey(domainKey);
		return executePhoneDialPlan(address, domain.getDomain());
	}
	
	public List<PhoneDialPlan> getPhoneDialPlansByPbxuser(Long pbxuserKey) throws DAOException{
		return phoneDialPlanDAO.getPhoneDialPlansByPbxuser(pbxuserKey);
	}
	
	/**
	 * Executa o PhoneDialPlan e retorna o resultado do primeiro Dial Plan em que ele se 
	 * encaixar (primeiro tenta os dial plans locais do dominio e depois os do rootDomain)
	 * 
	 * Caso nenhum dial plan seja executado, é retornado o mesmo address passado como parâmetro
	 */
	public PhoneDialPlanResponse executePhoneDialPlan(String address, String domain) throws DAOException 
	{
		Domain root = domainDAO.getRootDomain();
		List<PhoneDialPlan> phoneDialPlans = phoneDialPlanDAO.getPhoneDialPlansByDomain(domain, PhoneDialPlan.TYPE_NORMAL);
		List<PhoneDialPlan> rootPhoneDialPlans = phoneDialPlanDAO.getPhoneDialPlansByDomain(root.getKey(), PhoneDialPlan.TYPE_NORMAL);		
				
		for(PhoneDialPlan phoneDialPlan: phoneDialPlans)
		{
			PhoneDialPlanResponse resp = phoneDialPlan.exec(address);
			if(resp.isReady())
				return new PhoneDialPlanResponse(phoneDialPlan, resp.getResult(), false);
		}

		for(PhoneDialPlan phoneDialPlan: rootPhoneDialPlans)
		{
			PhoneDialPlanResponse resp = phoneDialPlan.exec(address);
			if(resp.isReady())
				return new PhoneDialPlanResponse(phoneDialPlan, resp.getResult(), true);
		}

		return new PhoneDialPlanResponse(address);
	}

}
