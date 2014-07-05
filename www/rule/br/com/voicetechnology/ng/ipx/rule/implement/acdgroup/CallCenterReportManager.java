package br.com.voicetechnology.ng.ipx.rule.implement.acdgroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.UserWithoutSipSessionlogException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallCenterGroupReportInDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallCenterLoginReportDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallCenterPaReportDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterGroupReportIn;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterLoginReport;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterLoginReportList;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterPaReport;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterPaReportList;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.db.prompt.PromptFile;
import br.com.voicetechnology.ng.ipx.rule.implement.GroupManager;
import br.com.voicetechnology.ng.ipx.rule.implement.prompt.PromptsFile;
import br.com.voicetechnology.ng.ipx.rule.implement.voicemail.DynaPrompts;
import br.com.voicetechnology.ng.ipx.rule.implement.voicemail.DynaPrompts.Variable;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class CallCenterReportManager extends Manager
{
	private GroupManager groupManager;
	private CallCenterGroupReportInDAO callCenterGroupReportInDAO;
	private CallCenterPaReportDAO callCenterPaReportDAO;
	private CallCenterLoginReportDAO callCenterLoginReportDAO;
	private PbxpreferenceDAO pbxpreferenceDAO;
	private PbxDAO pbxDAO;
	private GroupDAO groupDAO;

	public CallCenterReportManager(Logger logger) throws DAOException 
	{
		super(logger);
		groupManager = new GroupManager(logger.getName());
		callCenterGroupReportInDAO = dao.getDAO(CallCenterGroupReportInDAO.class);
		pbxpreferenceDAO = dao.getDAO(PbxpreferenceDAO.class);
		callCenterPaReportDAO = dao.getDAO(CallCenterPaReportDAO.class);
		callCenterLoginReportDAO = dao.getDAO(CallCenterLoginReportDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		groupDAO = dao.getDAO(GroupDAO.class);
	}

	public void saveGroupReportIn(CallCenterGroupReportIn report) throws DAOException, ValidateObjectException
	{
		callCenterGroupReportInDAO.save(report);
		for(CallCenterPaReport pareport : report.getPaReports().values())
		{
			pareport.setGroupReportKey(report.getKey());
			pareport.setType(report.getType());
			callCenterPaReportDAO.save(pareport);
		}
	}
	
	public void saveLoginReport(CallCenterLoginReport report) throws DAOException, ValidateObjectException
	{
		callCenterLoginReportDAO.save(report);
	}

	public CallCenterGroupReportIn getCurrentByGroupAndType(Long groupKey, int type) throws DAOException{
		Calendar startDate = getStartDate(type);
		Calendar endDate = getEndDate(type, startDate);	
		return getByGroupAndType(startDate, endDate, groupKey, type);
	}
	
//	public CallCenterGroupReportIn getYesterdayReportByGroupAndType(Long groupKey, int type) throws DAOException
//	{
//		Calendar startDate = getYesterdayDate(type);		
//		Calendar endDate = getEndDate(type, startDate);
//		return getByGroupAndType(startDate, endDate, groupKey, type);		
//	}
	
	public CallCenterGroupReportIn getByGroupAndType(Calendar startDate, Calendar endDate, Long groupKey, int type) throws DAOException
	{
		List<CallCenterGroupReportIn> reports = callCenterGroupReportInDAO.getReportsByDate(groupKey, type, startDate, endDate);
		CallCenterGroupReportIn callCenterGroupReportIn =  reports.size() > 0 ? reports.get(0) : null;
		
		if(callCenterGroupReportIn != null)
		{
			List<CallCenterPaReport> pareports = callCenterPaReportDAO.getByGroupReport(callCenterGroupReportIn.getKey());
			callCenterGroupReportIn.putInMap(pareports);		
		}		
		return callCenterGroupReportIn;
	}
	
	public List<CallCenterGroupReportIn> getLastHourReports(Long groupKey) throws DAOException
	{
		Calendar startDate = getCurrentDate(CallCenterGroupReportIn.HOUR);
		startDate.add(Calendar.HOUR, -1);
		Calendar endDate = getEndDate(CallCenterGroupReportIn.HOUR, startDate);
		
		List<CallCenterGroupReportIn> reports = callCenterGroupReportInDAO.getUnprocessedReportsByDate(groupKey, CallCenterGroupReportIn.TEN_MINUTES, startDate, endDate);
		for(CallCenterGroupReportIn report : reports)
		{
			List<CallCenterPaReport> pareports = callCenterPaReportDAO.getByGroupReport(report.getKey());
			report.putInMap(pareports);			
		}
		return reports;
	}
	
//	public List<CallCenterLoginReport> getLastHourLoginReports(Long groupKey) throws DAOException
//	{
//		Calendar startDate = getTodayDate(CallCenterLoginReport.HOUR);
//		startDate.add(Calendar.HOUR, -1);
//		Calendar endDate = getEndDate(CallCenterLoginReport.HOUR, startDate);
//		
//		List<CallCenterLoginReport> loginReports = callCenterLoginReportDAO.getUnprocessedLoginReportsByDate(groupKey, CallCenterGroupReportIn.TEN_MINUTES, startDate, endDate);
//
//		return loginReports;
//	}
	
	public List<Long> getCallCenterGroups(String pbxFarmIP) throws DAOException
	{
		return callCenterGroupReportInDAO.getCallCenterGroupsKey(pbxFarmIP);
	}
	
	public void unloggedAllPAs(String pbxFarmIP) throws DAOException, UserWithoutSipSessionlogException, ValidateObjectException
	{
		List<Pbx> pbxs = pbxDAO.getPbxListByFarmIP(pbxFarmIP);
		
		for(Pbx pbx : pbxs)
		{
			List<Long> pbxusers = groupDAO.getPAsByPbxkey(pbx.getKey());
			groupManager.changeACDLogin(pbxusers, false);
		}
	}
	
//	private Calendar getYesterdayDate(int type){
//		Calendar yesterday = Calendar.getInstance();
//		yesterday.add(Calendar.DAY_OF_MONTH, -1);
//		return getFormattedDate(yesterday, type);
//	}
	
	private Calendar getStartDate(int type)
	{
	    //dsakuma: O summarizeReport é executado com base na ultima hora. Entao, para resgatar o report do dia, mes e ano corretos, utilizamos a variavel lasthour
		Calendar lastHour = Calendar.getInstance();
		lastHour.add(Calendar.HOUR_OF_DAY, -1);
		
		return getFormattedDate(lastHour, type);
	}
	
	private Calendar getCurrentDate(int type)
	{
		Calendar currentDate = Calendar.getInstance();
		return getFormattedDate(currentDate, type);
	}
	
	
	private Calendar getFormattedDate(Calendar date, int type)
	{
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		switch (type) {
		case CallCenterGroupReportIn.TEN_MINUTES:
			int start_minute = calcMinute(date);
			date.set(Calendar.MINUTE, start_minute);
			break;
		case CallCenterGroupReportIn.HOUR:
			date.set(Calendar.MINUTE, 0);
			break;
		case CallCenterGroupReportIn.DAY:
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.HOUR_OF_DAY, 0);
			break;
		case CallCenterGroupReportIn.MONTH:
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.HOUR_OF_DAY, 0);
			date.set(Calendar.DAY_OF_MONTH, 1);
			break;
		case CallCenterGroupReportIn.YEAR:
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.HOUR_OF_DAY, 0);
			date.set(Calendar.DAY_OF_MONTH, 1);
			date.set(Calendar.MONTH, 0);
			break;			
		}		
		return date;
	}
	
	private Calendar getEndDate(int type, Calendar startDate)
	{
		Calendar endDate = (Calendar) startDate.clone();
		endDate.set(Calendar.SECOND, 59);
		endDate.set(Calendar.MILLISECOND, 999);
		switch (type) {
		case CallCenterGroupReportIn.TEN_MINUTES:
			endDate.add(Calendar.MINUTE, 9);
			break;
		case CallCenterGroupReportIn.HOUR:
			endDate.set(Calendar.MINUTE, 59);
			break;
		case CallCenterGroupReportIn.DAY:
			endDate.set(Calendar.HOUR_OF_DAY, 23);
			endDate.set(Calendar.MINUTE, 59);
			break;
		case CallCenterGroupReportIn.MONTH:
			endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH)); //configura com o ultimo dia do mes
			endDate.set(Calendar.HOUR_OF_DAY, 23);
			endDate.set(Calendar.MINUTE, 59);
			break;
		case CallCenterGroupReportIn.YEAR:
			endDate.set(Calendar.MONTH, 11); //11 corresponde a dezembro
			endDate.set(Calendar.DAY_OF_MONTH, 31);
			endDate.set(Calendar.HOUR_OF_DAY, 23);
			endDate.set(Calendar.MINUTE, 59);			
			break;		
		}		
		return endDate;
	}
	
	private int calcMinute(Calendar cal)
	{
		int current_minute = cal.get(Calendar.MINUTE);
		if(current_minute >= 50)
			return 50;
		else if(current_minute >= 40)
			return 40;
		else if(current_minute >= 30)
			return 30;
		else if(current_minute >= 20)
			return 20;
		else if(current_minute >= 10)
			return 10;
		else
			return 0;
	}
	
	public List<CallCenterGroupReportIn> getReportsByTypeAndGroup(Integer type, Long groupKey, Calendar start, Calendar end) throws DAOException
	{			
		return callCenterGroupReportInDAO.getReportsByDate(groupKey, type, start, end);
	}
	
	public String[] getCallCenterQueuePositionPrompt(int position, Long domainKey) throws DAOException
	{
		Pbxpreference preference = pbxpreferenceDAO.getByDomainKey(domainKey);
		DynaPrompts dynamic = new DynaPrompts(Variable.MSG_NUM, String.valueOf(position), DynaPrompts.Type.NUMBER);
		PromptsFile prompts = new PromptsFile();
		return prompts.getPromptPath(preference.getLocale(),  PromptFile.CALLCENTER_QUEUE_POSITION, dynamic);
	}
	
	public List<CallCenterPaReport> getPaReportByGroupReport(Long groupReportKey) throws DAOException{
		return callCenterPaReportDAO.getByGroupReport(groupReportKey);
	}	
	
	public List<CallCenterPaReportList> getPaReportsByTypeAndPbxusers(Integer type, Long groupKey, Calendar start, Calendar end, Long ... pbxuserKeys) throws DAOException
	{
		List<CallCenterPaReportList> list = new ArrayList<CallCenterPaReportList>();
		for(Long pbxuserKey : pbxuserKeys)
		{
			List<CallCenterPaReport> reports = getPaReportsByTypeAndPbxuser(type, pbxuserKey, groupKey, start, end);
			list.add(new CallCenterPaReportList(pbxuserKey, reports));
		}
		return list;
	}

	public List<CallCenterPaReport> getPaReportsByTypeAndPbxuser(Integer type, Long pbxuserKey, Long groupKey, Calendar start, Calendar end) throws DAOException
	{
		return callCenterPaReportDAO.getReportsByDate(pbxuserKey, groupKey, type, start, end);
	}

	public CallCenterLoginReport getLastCallCenterLoginReport(Long groupKey, Long pbxUserKey) throws DAOException {
		return callCenterLoginReportDAO.getLastCallCenterLoginReport(groupKey, pbxUserKey);
	}
	
	public List<CallCenterLoginReport> getLoginReportsByTypeAndPbxUser(Integer type, Long pbxUserKey, Long groupKey, Calendar start, Calendar end) throws DAOException
	{
		return callCenterLoginReportDAO.getReportsByDate(type, pbxUserKey, groupKey, start, end);
	}
	
	public List<CallCenterLoginReportList> getLoginReportsByTypeAndPbxUsers(Integer type, Long groupKey, Calendar start, Calendar end, Long ... pbxUserKeys) throws DAOException
	{
		List<CallCenterLoginReportList> list = new ArrayList<CallCenterLoginReportList>();
		for(Long pbxuserKey : pbxUserKeys)
		{
			List<CallCenterLoginReport> reports = getLoginReportsByTypeAndPbxUser(type, pbxuserKey, groupKey, start, end);
			list.add(new CallCenterLoginReportList(pbxuserKey, reports));
		}
		return list;
	}
}