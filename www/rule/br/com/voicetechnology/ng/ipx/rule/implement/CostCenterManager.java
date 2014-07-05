package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAONotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CostCenterDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CostCenter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CostCenterInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class CostCenterManager extends Manager
{
	private CostCenterDAO costCenterDAO;
	private PbxpreferenceDAO pbxpreferenceDAO;
	private CalllogDAO calllogDAO;

	public CostCenterManager(Logger logger) throws DAONotFoundException
	{
		super(logger);
		costCenterDAO = dao.getDAO(CostCenterDAO.class);
		pbxpreferenceDAO = dao.getDAO(PbxpreferenceDAO.class);
		calllogDAO = dao.getDAO(CalllogDAO.class);
	}
	
	public ReportResult find(Report<CostCenterInfo> report) throws DAOException
	{			
		Long size = costCenterDAO.getReportCount(report);
		List<CostCenter> costCenters= costCenterDAO.getReportList(report);				
		
		List<CostCenterInfo> infos = new ArrayList<CostCenterInfo>();
		for(CostCenter costCenter : costCenters){
			infos.add(new CostCenterInfo(costCenter));
		}
		return new ReportResult<CostCenterInfo>(infos, size);
	}

	public CostCenter getByDomainAndCode(String domain, String code) throws DAOException{
		return costCenterDAO.getByDomainAndCode(domain, code);
	}	
	
	public void save(CostCenterInfo info) throws DAOException, ValidateObjectException
	{
		validate(info);
		CostCenter costCenter = info.getCostCenter();		
		costCenterDAO.save(costCenter);
	}
	
	public void validate(CostCenterInfo info) throws DAOException, ValidateObjectException
	{
		Long domainKey = info.getDomainKey();
		Pbxpreference pbxpreference = pbxpreferenceDAO.getByDomainKey(domainKey);
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		int costCenterDigits = pbxpreference.getCostCenterCodeDigits();		
		
		if(info.getCode().length() != costCenterDigits)
			errorList.add(new ValidateError("Cost Center Code Length Invalid!", CostCenter.class, costCenterDigits, ValidateType.LENGTH));
		
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
	}
	
	public void delete(Long key) throws DAOException, ValidateObjectException
	{
		CostCenter costCenter =  costCenterDAO.getByKey(key);
		calllogDAO.updateCallLogsCostCenter(costCenter.getKey(), null);		
		costCenterDAO.remove(costCenter);
	}

	public void delete(List<Long> keysList) throws DAOException, ValidateObjectException
	{
		for(Long key : keysList)
			delete(key);
	}	

	public CostCenter getCostCenter(Long key) throws DAOException
	{
		return costCenterDAO.getByKey(key);
	}

	public List<CostCenter> getCostCenterList(Long domainKey) throws DAOException
	{
		return costCenterDAO.getListByDomainKey(domainKey);		
	}
	
}
