package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.pbx.DialPlanDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SpeeddialDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.UserspeeddialDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Speeddial;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Userspeeddial;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SpeeddialInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserSpeedDialInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class SpeeddialManager extends Manager
{
	private PbxDAO pbxDAO;
	private SpeeddialDAO speeddialDAO;
	private UserspeeddialDAO userSpeedDialDAO;
	private DialPlanDAO dialPlanDAO;
	private ReportDAO<Speeddial, SpeeddialInfo> reportSpeedDial;
	private ReportDAO<Userspeeddial, UserSpeedDialInfo> reportUserSpeedDial;
	
	
	public SpeeddialManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		pbxDAO = dao.getDAO(PbxDAO.class);
		speeddialDAO = dao.getDAO(SpeeddialDAO.class);
		userSpeedDialDAO = dao.getDAO(UserspeeddialDAO.class);
		reportSpeedDial = dao.getReportDAO(SpeeddialDAO.class);
		reportUserSpeedDial = dao.getReportDAO(UserspeeddialDAO.class);
		dialPlanDAO = dao.getDAO(DialPlanDAO.class);
	}

	public ReportResult<SpeeddialInfo> findSpeedDials(Report<SpeeddialInfo> info) throws DAOException
	{
		Long size = reportSpeedDial.getReportCount(info);
		List<Speeddial> speeddialList = reportSpeedDial.getReportList(info);
		List<SpeeddialInfo> speedInfoList = new ArrayList<SpeeddialInfo>(speeddialList.size());
		for (Speeddial speeddial : speeddialList)
			speedInfoList.add(new SpeeddialInfo(speeddial));
		return new ReportResult<SpeeddialInfo>(speedInfoList, size);
	}

	public ReportResult findUserSpeedDials(Report<UserSpeedDialInfo> info) throws DAOException 
	{
		Long size = reportUserSpeedDial.getReportCount(info);
		List<Userspeeddial> userSpeedDialList = reportUserSpeedDial.getReportList(info);
		List<UserSpeedDialInfo> userSpeedDialInfoList = new ArrayList<UserSpeedDialInfo>(userSpeedDialList.size());
		for(Userspeeddial userSpeedDial : userSpeedDialList)
			userSpeedDialInfoList.add(new UserSpeedDialInfo(userSpeedDial));
		return new ReportResult<UserSpeedDialInfo>(userSpeedDialInfoList, size);
	}
	
	public SpeeddialInfo getSpeeddialInfoByKey(Long speeddialKey) throws DAOException
	{
		Speeddial speeddial = speeddialDAO.getByKey(speeddialKey);
		SpeeddialInfo speeddialInfo = new SpeeddialInfo(speeddial);
		return speeddialInfo;
	}
	
	public void save(SpeeddialInfo speeddialInfo) throws DAOException, ValidateObjectException
	{
		Speeddial speeddial = speeddialInfo.getSpeeddial();
		validateSave(speeddial);
		speeddialDAO.save(speeddial);
	}

	public void deleteSpeeddials(List<Long> speeddialKeyList) throws DAOException
	{
		for (Long speeddialKey : speeddialKeyList)
			this.deleteSpeeddial(speeddialKey);
	}
	
	public void deleteSpeeddial(Long speeddialKey) throws DAOException
	{
		Speeddial speeddial = speeddialDAO.getByKey(speeddialKey);
		speeddialDAO.remove(speeddial);
	}
	
	protected void validateSave(Speeddial speeddial) throws DAOException, ValidateObjectException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(speeddial == null)
		{
			errorList.add(new ValidateError("Speeddial is null!", Speeddial.class, null, ValidateType.BLANK));
		} else
		{
			Long pbxKey = speeddial.getPbxKey();
			if(pbxKey == null)
				errorList.add(new ValidateError("Speeddial pbxKey is null!", Speeddial.Fields.PBX_KEY.toString(), Speeddial.class, speeddial, ValidateType.BLANK));
			
			String name = speeddial.getName();
			if(name == null)
				errorList.add(new ValidateError("Speeddial name is null!", Speeddial.Fields.NAME.toString(), Speeddial.class, speeddial, ValidateType.BLANK));
			else if(name.length() == 0)
				errorList.add(new ValidateError("Speeddial name is blank!", Speeddial.Fields.NAME.toString(), Speeddial.class, speeddial, ValidateType.BLANK));
			else if(!name.matches("\\d+"))
				errorList.add(new ValidateError("Speeddial name is not numeric!", Speeddial.Fields.NAME.toString(), Speeddial.class, speeddial, ValidateType.POSITIVE_INTEGER));
			
			
			String destination = speeddial.getDestination();
			if(destination == null)
				errorList.add(new ValidateError("Speeddial destination is null!", Speeddial.Fields.DESTINATION.toString(), Speeddial.class, speeddial, ValidateType.BLANK));
			else if(destination.length() == 0)
				errorList.add(new ValidateError("Speeddial destination is blank!", Speeddial.Fields.DESTINATION.toString(), Speeddial.class, speeddial, ValidateType.BLANK));
			else if(!destination.matches(getSipIDRegex()) && !destination.matches(getCommandRegex()) && !destination.matches(getPhoneNumberRegex()))
				errorList.add(new ValidateError("Speeddial destination is not sipid, command or phone number!", Speeddial.Fields.DESTINATION.toString(), Speeddial.class, speeddial, ValidateType.INVALID));
		}
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
		else
		{				
			DialPlan dialPlan = dialPlanDAO.getDialPlanByTypeAndPbx(DialPlan.TYPE_PUBLICSPEEDDIAL, speeddial.getPbxKey());				
			Integer integer = Integer.parseInt(speeddial.getName());
			if(integer < dialPlan.getStart() || integer > dialPlan.getEnd())
			{
				List<ValidateError> list = new ArrayList<ValidateError>();
				ValidateError error = new ValidateError("Already exisit a value out of the DialPlan Range", DialPlan.class, dialPlan, ValidateType.DIALPLAN_OUT_OF_RANGE);
				list.add(error);
				throw new ValidateObjectException("Already exisit a value out of the DialPlan Range", list) ;
			}
		}
	}
	
	private Duo<Integer, Integer> getCallParkRange(Long pbxKey) throws DAOException
	{
		Pbx pbx = pbxDAO.getByKey(pbxKey);
		Duo<Integer, Integer> result = null;
		if(pbx.getPbxPreferences().getParkBegin() != null && pbx.getPbxPreferences().getParkEnd() != null)
			result = new Duo<Integer, Integer>(pbx.getPbxPreferences().getParkBegin(), pbx.getPbxPreferences().getParkEnd());
		return result;
	}

	public void save(UserSpeedDialInfo userSpeedDialInfo) throws DAOException, ValidateObjectException 
	{
		Userspeeddial userSpeedDial = userSpeedDialInfo.getUserSpeedDial();
		validateSave(userSpeedDial);
		userSpeedDialDAO.save(userSpeedDial);
	}
	
	protected void validateSave(Userspeeddial userSpeedDial) throws DAOException, ValidateObjectException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(userSpeedDial == null)
		{
			errorList.add(new ValidateError("UserSpeeddial is null!", Userspeeddial.class, null, ValidateType.BLANK));
		} else
		{
			Long pbxuserKey = userSpeedDial.getPbxuserKey();
			if(pbxuserKey == null)
				errorList.add(new ValidateError("UserSpeeddial pbxuserKey is null!", Userspeeddial.Fields.PBXUSER_KEY.toString(), Userspeeddial.class, userSpeedDial, ValidateType.BLANK));
			
			String name = userSpeedDial.getName();
			if(name == null)
				errorList.add(new ValidateError("UserSpeeddial name is null!", Userspeeddial.Fields.NAME.toString(), Userspeeddial.class, userSpeedDial, ValidateType.BLANK));
			else if(name.length() == 0)
				errorList.add(new ValidateError("UserSpeeddial name is blank!", Userspeeddial.Fields.NAME.toString(), Userspeeddial.class, userSpeedDial, ValidateType.BLANK));
			else if(!name.matches("\\d+"))
				errorList.add(new ValidateError("UserSpeeddial name is not numeric!", Userspeeddial.Fields.NAME.toString(), Userspeeddial.class, userSpeedDial, ValidateType.POSITIVE_INTEGER));			
			
			String destination = userSpeedDial.getDestination();
			if(destination == null)
				errorList.add(new ValidateError("UserSpeeddial destination is null!", Userspeeddial.Fields.DESTINATION.toString(), Userspeeddial.class, userSpeedDial, ValidateType.BLANK));
			else if(destination.length() == 0)
				errorList.add(new ValidateError("UserSpeeddial destination is blank!", Userspeeddial.Fields.DESTINATION.toString(), Userspeeddial.class, userSpeedDial, ValidateType.BLANK));
			else if(!destination.matches(getSipIDRegex()) && !destination.matches(getPhoneNumberRegex()))
				errorList.add(new ValidateError("UserSpeeddial destination is not sipid or phone number!", Userspeeddial.Fields.DESTINATION.toString(), Userspeeddial.class, userSpeedDial, ValidateType.INVALID));
		}
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
		else
		{
			DialPlan dialPlan = dialPlanDAO.getDialPlanByTypeAndPbxuser(DialPlan.TYPE_USERSPEEDDIAL, userSpeedDial.getPbxuserKey());
			Integer integer = Integer.parseInt(userSpeedDial.getName());
			if(integer < dialPlan.getStart() || integer > dialPlan.getEnd())
			{
				List<ValidateError> dialPlanErrorList = new ArrayList<ValidateError>();
				ValidateError error = new ValidateError("Already exisit a value out of the DialPlan Range", DialPlan.class, dialPlan, ValidateType.DIALPLAN_OUT_OF_RANGE);
				dialPlanErrorList.add(error);
				throw new ValidateObjectException("Already exisit a value out of the DialPlan Range", dialPlanErrorList) ;
			}
		}
	}

	public UserSpeedDialInfo getUserSpeedDialInfoByKey(Long userSpeeddialKey) throws DAOException 
	{
		Userspeeddial userSpeedDial = userSpeedDialDAO.getByKey(userSpeeddialKey);
		return new UserSpeedDialInfo(userSpeedDial);
	}

	public void deleteUserSpeedDial(Long userSpeedDialKey) throws DAOException
	{
		Userspeeddial userSpeedDial = userSpeedDialDAO.getByKey(userSpeedDialKey);
		userSpeedDialDAO.remove(userSpeedDial);
	}
	
	public void deleteUserSpeedDials(List<Long> userSpeedDialsKeyList) throws DAOException
	{
		for(Long userSpeedDialKey : userSpeedDialsKeyList)
			this.deleteUserSpeedDial(userSpeedDialKey);
	}
}