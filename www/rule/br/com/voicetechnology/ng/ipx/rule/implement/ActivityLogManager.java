package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.ActivityLogDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.ActivityLog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ActivityLogInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class ActivityLogManager extends Manager 
{
	private ActivityLogDAO activityLogDAO;
	private DomainDAO domainDAO;
	private ReportDAO<ActivityLog, ActivityLogInfo> reportActivityLog;
	
	public ActivityLogManager(String loggerPath) throws DAOException 
	{
		super(loggerPath);
		activityLogDAO = dao.getDAO(ActivityLogDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
		reportActivityLog = dao.getDAO(ActivityLogDAO.class);
	}
	
	public void saveActivityLog(ActivityLog log) throws DAOException, ValidateObjectException
	{
		activityLogDAO.save(log);
	}
	
	protected void validateSave(ActivityLog log) throws DAOException, ValidateObjectException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(log == null)
		{
			errorList.add(new ValidateError("ActivityLog is null!", ActivityLog.class, null, ValidateType.BLANK));
		} else
		{
			//TODO colocar validacao nos campos que nao podem ser nulos
		}
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
	}
	
	public ReportResult findActivityLog(Report<ActivityLogInfo> info) throws DAOException
	{
		Long size = reportActivityLog.getReportCount(info);
		List<ActivityLog> activityLogList = reportActivityLog.getReportList(info);	
		Domain domain = domainDAO.getRootDomain();
		List<Duo<Long, String>> domainList = domainDAO.getDomainsByRootDomain(domain.getKey());
		
		List<ActivityLogInfo> activityLogInfoList = new ArrayList<ActivityLogInfo>(activityLogList.size());
		for(ActivityLog activityLog : activityLogList)
			activityLogInfoList.add(new ActivityLogInfo(activityLog,  domainList));
		return new ReportResult<ActivityLogInfo>(activityLogInfoList, size);
	}
}