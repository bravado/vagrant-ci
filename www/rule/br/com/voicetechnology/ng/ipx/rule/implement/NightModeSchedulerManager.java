package br.com.voicetechnology.ng.ipx.rule.implement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAONotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallCenterConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.NightModeSchedulerDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterConfig;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.NightModeScheduler;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.GroupInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PbxPreferenceInfo;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class NightModeSchedulerManager extends Manager 
{
	private NightModeSchedulerDAO nModeSchedulerDAO;
	private PbxpreferenceDAO pbxPreferenceDAO;
	private GroupDAO groupDAO;
	private PbxManager pbxManager;	
	private CallCenterConfigDAO ccConfigDAO;
	private SimpleDateFormat dateFormater = new SimpleDateFormat("kk:mm");
	
	public NightModeSchedulerManager(Logger logger) throws DAOException 
	{
		super(logger);		
		nModeSchedulerDAO = dao.getDAO(NightModeSchedulerDAO.class);
		pbxPreferenceDAO = dao.getDAO(PbxpreferenceDAO.class);
		groupDAO = dao.getDAO(GroupDAO.class);
		ccConfigDAO = dao.getDAO(CallCenterConfigDAO.class);
		pbxManager = new PbxManager(PbxManager.class.getName());
	}
	
	public void saveNigthModeScheduler(PbxPreferenceInfo info) throws DAOException, ValidateObjectException, ParseException
	{		
		Pbxpreference preference = pbxPreferenceDAO.getByPbxKey(info.getPbxkey());
		List<NightModeScheduler> nightModeSchedulerListOld = nModeSchedulerDAO.getNightModeSchedulerByPbx(info.getPbxkey(),false);
		nightModeSchedulerListOld.addAll(nModeSchedulerDAO.getNightModeSchedulerByPbx(info.getPbxkey(), true));		
		
		List<NightModeScheduler> nightModeSchedulerList = info.getNightModeSchedulerList();
		
		validateWeekDaysHours(nightModeSchedulerList);
		
		for(NightModeScheduler nightModeScheduler : nightModeSchedulerListOld)
			nModeSchedulerDAO.remove(nightModeScheduler);
		
		for(NightModeScheduler nightModeScheduler : nightModeSchedulerList)
		{
			nightModeScheduler.setKey(null);
			nightModeScheduler.setPbxPreferenceKey(preference.getKey());
			
			nModeSchedulerDAO.save(nightModeScheduler);
		}
		
		if(nightModeSchedulerList.size() > 0)
			preference.setNightModeScheduler(info.getNightmodeSchedulerStatus());
		else
		{
			preference.setNightModeScheduler(Pbxpreference.NIGHTMODE_SCHEDULER_OFF);
			pbxManager.changeNightModeStatus(info.getPbxkey(), Pbx.NIGHTMODE_OFF);
		}
		pbxPreferenceDAO.save(preference);
	}	
	
	public void saveNigthModeScheduler(GroupInfo info) throws DAOException, ValidateObjectException, ParseException
	{	
		List<NightModeScheduler> nightModeSchedulerListOld = nModeSchedulerDAO.getNightModeSchedulerByGroup(info.getKey());	
		
		List<NightModeScheduler> nightModeSchedulerList = info.getAllNightModeSchedulers();
		
		validateWeekDaysHours(nightModeSchedulerList);
		
		for(NightModeScheduler nightModeScheduler : nightModeSchedulerListOld)
			nModeSchedulerDAO.remove(nightModeScheduler);
		
		for(NightModeScheduler nightModeScheduler : nightModeSchedulerList)
		{
			nightModeScheduler.setKey(null);
			nightModeScheduler.setConfigKey(info.getConfigKey());			
			nModeSchedulerDAO.save(nightModeScheduler);
		}		
	}	
	
	public PbxPreferenceInfo getNightModeSchedulerByPbx(Long pbxKey) throws DAOException
	{
		PbxPreferenceInfo preferenceInfo = new PbxPreferenceInfo();
		List<List<NightModeScheduler>> nightModeSchedulerList = new ArrayList<List<NightModeScheduler>>();
		
		List<NightModeScheduler> nightModeSchedulerWeekDaysList = new ArrayList<NightModeScheduler>();		
		for(NightModeScheduler scheduler : nModeSchedulerDAO.getNightModeSchedulerByPbx(pbxKey,false))
			nightModeSchedulerWeekDaysList.add(scheduler);
		
		List<NightModeScheduler> nightModeSchedulerHolidayList = new ArrayList<NightModeScheduler>();
		for(NightModeScheduler scheduler : nModeSchedulerDAO.getNightModeSchedulerByPbx(pbxKey,true))
			nightModeSchedulerHolidayList.add(scheduler);
		
		nightModeSchedulerList.add(nightModeSchedulerWeekDaysList);
		nightModeSchedulerList.add(nightModeSchedulerHolidayList);
		
		preferenceInfo.setNightModeSchedulerList(nightModeSchedulerList);		 
		return preferenceInfo;
	}
	
	public List<NightModeScheduler> getNightModeSchedulerListByPbxKey(Long pbxKey) throws DAOException
	{
		return nModeSchedulerDAO.getNightModeSchedulerByPbx(pbxKey);
	}

	private void validateWeekDaysHours(List<NightModeScheduler> nightModeSchedulerList) throws ParseException, ValidateObjectException
	{
		List<NightModeScheduler> validateList = new ArrayList<NightModeScheduler>();
		
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		ValidateError error = new ValidateError("There are hours that are wrong", NightModeScheduler.class, nightModeSchedulerList, ValidateType.LENGTH);
		errorList.add(error);
		
		for(int i = 1; i < 8; i ++)
		{	
			validateList = getValidateList(nightModeSchedulerList, i);
			for(int j = 0; j < validateList.size(); j++)
			{
				NightModeScheduler nigthModeScheduler = validateList.get(j);
				
				for(int k = j + 1; k < validateList.size(); k++)
				{
					NightModeScheduler actualNigthModeScheduler = validateList.get(k);
					
					Date startTime = dateFormater.parse(nigthModeScheduler.getStartTime());
					Date endTime = dateFormater.parse(nigthModeScheduler.getEndTime());
					
					Date actualStartTime = dateFormater.parse(actualNigthModeScheduler.getStartTime());
					Date actualEndTime = dateFormater.parse(actualNigthModeScheduler.getEndTime());
					
					if(startTime.getTime() >= actualStartTime.getTime() && startTime.getTime() <= actualEndTime.getTime())
						throw new ValidateObjectException("Problem with Hour Ranges", errorList) ;
					
					if(actualStartTime.getTime() >= startTime.getTime() && actualStartTime.getTime() <= endTime.getTime())
						throw new ValidateObjectException("Problem with Hour Ranges", errorList) ;
				}
			}
			validateList.clear();
		}
	}	
	
	private List<NightModeScheduler> getValidateList(List<NightModeScheduler> nightModeSchedulerList, int weekDay) throws ParseException
	{
		int lastDay = 1;
		List<NightModeScheduler> sameDayList = new ArrayList<NightModeScheduler>();
		List<NightModeScheduler> lastDayList = new ArrayList<NightModeScheduler>();
		List<NightModeScheduler> validateList = new ArrayList<NightModeScheduler>();
		
		for(NightModeScheduler nightModeScheduler : nightModeSchedulerList)
		{
			if(weekDay == 1)
				lastDay = 7;
			else
				lastDay = weekDay - 1;
			
			if(nightModeScheduler.getWeekDays() != null && nightModeScheduler.getWeekDays().contains(Integer.toString(weekDay)))
				sameDayList.add(nightModeScheduler);
			else if(nightModeScheduler.getWeekDays() != null && nightModeScheduler.getWeekDays().contains(Integer.toString(lastDay)))
				lastDayList.add(nightModeScheduler);				
		}
		
		for(NightModeScheduler nigthModeScheduler : sameDayList)
		{
			NightModeScheduler validateNigthModeScheduler;
			
			Date startTime = dateFormater.parse(nigthModeScheduler.getStartTime());
			Date endTime = dateFormater.parse(nigthModeScheduler.getEndTime());
			if(startTime.getTime() > endTime.getTime())
			{
				validateNigthModeScheduler = new NightModeScheduler();
				validateNigthModeScheduler.setStartTime(nigthModeScheduler.getStartTime());
				validateNigthModeScheduler.setEndTime("23:59");				
			}			
			else
			{
				validateNigthModeScheduler = new NightModeScheduler();
				validateNigthModeScheduler.setStartTime(nigthModeScheduler.getStartTime());
				validateNigthModeScheduler.setEndTime(nigthModeScheduler.getEndTime());			
			}
			validateList.add(nigthModeScheduler);
		}
		
		for(NightModeScheduler nigthModeScheduler : lastDayList)
		{
			Date startTime = dateFormater.parse(nigthModeScheduler.getStartTime());
			Date endTime = dateFormater.parse(nigthModeScheduler.getEndTime());
			NightModeScheduler validateNigthModeScheduler;
			
			if(startTime.getTime() > endTime.getTime())
			{
				validateNigthModeScheduler = new NightModeScheduler();
				validateNigthModeScheduler.setStartTime("00:00");
				validateNigthModeScheduler.setEndTime(nigthModeScheduler.getEndTime());
				validateList.add(validateNigthModeScheduler);
			}			
		}
		
		return validateList;
	}

	public List<NightModeScheduler> getNightModeSchedulerListByGroupKey(Long groupKey) throws DAOException {
		
		return nModeSchedulerDAO.getNightModeSchedulerByGroup(groupKey);
	}

	public GroupInfo getGroupWithNightmodeSchedulers(Long groupKey) throws DAOException {
		
		Group group = groupDAO.getByKey(groupKey);
		
		List<NightModeScheduler> nightModeSchedulers = getNightModeSchedulerListByGroupKey(groupKey);
		List<NightModeScheduler> holidaySchedulers = new ArrayList<NightModeScheduler>();
		List<NightModeScheduler> workDaysSchedulers = new ArrayList<NightModeScheduler>();		
		for(NightModeScheduler nightModeScheduler : nightModeSchedulers)
		{
			if(nightModeScheduler.isHoliday())
				holidaySchedulers.add(nightModeScheduler);
			else
				workDaysSchedulers.add(nightModeScheduler);
		}
		CallCenterConfig ccConfig = ccConfigDAO.getCallCenterConfigByGroupkey(group.getKey());
		GroupInfo info = new GroupInfo(group, workDaysSchedulers, holidaySchedulers);
		group.setCallCenterConfig(ccConfig);
		
		return info;
	}		
}
