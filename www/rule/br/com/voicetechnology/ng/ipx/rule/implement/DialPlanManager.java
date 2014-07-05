package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAONotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.pbx.DialPlanDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PBXDialPlanInfo;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class DialPlanManager extends Manager
{
	private DialPlanDAO dialPlanDAO;
	public DialPlanManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		dialPlanDAO = dao.getDAO(DialPlanDAO.class);
	}
	
	public boolean validateNumber(int dialPlanType, Long domainKey, int number) throws DAOException
	{
		DialPlan dialPlan = dialPlanDAO.getDialPlanByTypeAndDomain(dialPlanType, domainKey);
		if(dialPlan == null)
			return false;
		
		int start = dialPlan.getStart();
		int end = dialPlan.getEnd();
			
		if(number >= start && number <= end)
			return true;
		
		return false;
	}
	
	public DialPlan getDialPlanByTypeAnddomain(String domain, int type) throws DAOException
	{
		DialPlan dPlan = dialPlanDAO.getDialPlanByTypeAndDomain(type, domain);
		return dPlan;
	}
	
	public PBXDialPlanInfo getPBXDialPlan(Long pbxKey) throws DAOException
	{
		PBXDialPlanInfo pbxDialPlanInfo = new PBXDialPlanInfo();
		Pbx pbx = new Pbx();
		pbxDialPlanInfo.setPbx(pbx);
		
		List<DialPlan> list = dialPlanDAO.getPBXDialPlanByPbx(pbxKey);
				
		pbxDialPlanInfo.setKey(pbxKey);
		buildPBXDialPlanInfo(pbxDialPlanInfo, list);
		return pbxDialPlanInfo;				
	}
	
	public PBXDialPlanInfo getPBXDialPlanByPbxpreference(Long pbxpreferenceKey) throws DAOException
	{
		PBXDialPlanInfo pbxDialPlanInfo = new PBXDialPlanInfo();
		Pbx pbx = new Pbx();
		pbxDialPlanInfo.setPbx(pbx);
		
		List<DialPlan> list = dialPlanDAO.getPBXDialPlanByPbxpreference(pbxpreferenceKey);
				
		pbxDialPlanInfo.setKey(pbxpreferenceKey);
		buildPBXDialPlanInfo(pbxDialPlanInfo, list);
		
		return pbxDialPlanInfo;				
	}
	
	public PBXDialPlanInfo getPBXDialPlanByDomain(String domain) throws DAOException
	{
		PBXDialPlanInfo pbxDialPlanInfo = new PBXDialPlanInfo();
		Pbx pbx = new Pbx();
		pbxDialPlanInfo.setPbx(pbx);
		
		List<DialPlan> list = dialPlanDAO.getPBXDialPlanByDomain(domain);			
		buildPBXDialPlanInfo(pbxDialPlanInfo, list);
		
		return pbxDialPlanInfo;				
	}
	
	public PBXDialPlanInfo getPBXDialPlanByDomain(Long domainKey) throws DAOException
	{
		PBXDialPlanInfo pbxDialPlanInfo = new PBXDialPlanInfo();
		Pbx pbx = new Pbx();
		pbxDialPlanInfo.setPbx(pbx);
		
		List<DialPlan> list = dialPlanDAO.getPBXDialPlanByDomain(domainKey);			
		buildPBXDialPlanInfo(pbxDialPlanInfo, list);
		
		return pbxDialPlanInfo;				
	}
	
	public void updatePBXDialPlan(PBXDialPlanInfo info, Long pbxpreferenceKey) throws DAOException, ValidateObjectException
	{
		validatePBXDialPlan(info, info.getPbx().getKey());
		PBXDialPlanInfo infoTmp;
		if(info.getKey() != null)
			infoTmp = getPBXDialPlan(info.getKey());
		else
			infoTmp = getPBXDialPlanByPbxpreference(pbxpreferenceKey);		
		
		updateDialPlan(infoTmp.getExtensionDialPlan(), info.getExtensionDialPlan());
		updateDialPlan(infoTmp.getParkDialPlan(), info.getParkDialPlan());
		updateDialPlan(infoTmp.getSpeedDialDialPlan(), info.getSpeedDialDialPlan());
		updateDialPlan(infoTmp.getPublicSpeedDialDialPlan(), info.getPublicSpeedDialDialPlan());
		updateDialPlan(infoTmp.getDefaultOperatorDialPlan(), info.getDefaultOperatorDialPlan());				
	}
	
	public void savePBXDialPlan(PBXDialPlanInfo info, Long pbxKey, Long pbxpreferenceKey) throws DAOException, ValidateObjectException
	{		
		validatePBXDialPlan(info, pbxKey);
		
		info.getDefaultOperatorDialPlan().setPbxpreferenceKey(pbxpreferenceKey);
		info.getExtensionDialPlan().setPbxpreferenceKey(pbxpreferenceKey);
		info.getSpeedDialDialPlan().setPbxpreferenceKey(pbxpreferenceKey);
		info.getPublicSpeedDialDialPlan().setPbxpreferenceKey(pbxpreferenceKey);
		info.getParkDialPlan().setPbxpreferenceKey(pbxpreferenceKey);
		dialPlanDAO.save(info.getExtensionDialPlan());
		dialPlanDAO.save(info.getDefaultOperatorDialPlan());
		dialPlanDAO.save(info.getPublicSpeedDialDialPlan());
		dialPlanDAO.save(info.getSpeedDialDialPlan());
		dialPlanDAO.save(info.getParkDialPlan());
	}
	
	private DialPlan get(int type, List<DialPlan> list)
	{
		for(DialPlan dialPlan : list)
		{
			if(dialPlan.getType() == type)
				return dialPlan;
		}
		return null;
	}
	
	private void updateDialPlan(DialPlan dialPlanOld, DialPlan dialPlanNew) throws DAOException, ValidateObjectException
	{
		dialPlanOld.setStart(dialPlanNew.getStart());
		dialPlanOld.setEnd(dialPlanNew.getEnd());
		
		dialPlanDAO.save(dialPlanOld);		
	}
	
	public void validatePBXDialPlan(PBXDialPlanInfo info, Long pbxKey) throws ValidateObjectException, DAOException
	{		
		List<DialPlan> dialPlanList = new ArrayList<DialPlan>();
		dialPlanList.add(info.getExtensionDialPlan());
		dialPlanList.add(info.getDefaultOperatorDialPlan());
		dialPlanList.add(info.getSpeedDialDialPlan());
		dialPlanList.add(info.getPublicSpeedDialDialPlan());
		dialPlanList.add(info.getParkDialPlan());
			
		DialPlan dialPlan;
		for(int j = 0; j < dialPlanList.size(); j ++)
		{			
			dialPlan = dialPlanList.get(j);
			
			Integer start = dialPlan.getStart();
			Integer end = dialPlan.getEnd();
			
			if(start >= end)
			{
				List<ValidateError> errorList = new ArrayList<ValidateError>();
				ValidateError error = new ValidateError("The end value must be bigger than start value", DialPlan.class, dialPlan, ValidateType.DIALPLAN_START_VALUE);
				errorList.add(error);
				throw new ValidateObjectException("The end value must be bigger than start value", errorList) ;
			}
			
			for(int i = j + 1; i < dialPlanList.size(); i ++)
			{				
				Integer actualStart = dialPlanList.get(i).getStart();
				Integer actualEnd = dialPlanList.get(i).getEnd();
				
				if(start >= actualStart && start <= actualEnd)					
				{
					List<ValidateError> errorList = new ArrayList<ValidateError>();
					ValidateError error = new ValidateError("Problem with DialPlans Ranges", DialPlan.class, dialPlan, ValidateType.DIALPLAN_RANGE);
					errorList.add(error);
					 throw new ValidateObjectException("Problem with DialPlans Ranges", errorList) ;
				}
				  
				if(actualStart >= start && actualStart <= end)
				{
					List<ValidateError> errorList = new ArrayList<ValidateError>();
					ValidateError error = new ValidateError("Problem with DialPlans Ranges", DialPlan.class, dialPlan, ValidateType.DIALPLAN_RANGE);
					errorList.add(error);
					 throw new ValidateObjectException("Problem with DialPlans Ranges", errorList) ;					
				}
			}
		}		
		
		validateDialPlan(info.getExtensionDialPlan(), info.getParkDialPlan());
		verifyOldPBXDialPlan(dialPlanList, pbxKey);
	}
	
	private void verifyOldPBXDialPlan(List<DialPlan> dialPlanList, Long pbxKey) throws ValidateObjectException, DAOException
	{		
		for(DialPlan dialPlan : dialPlanList)
		{
			List list = getNumber(dialPlan, pbxKey);
			if(list != null && list.size() > 0)
			{
				List<ValidateError> errorList = new ArrayList<ValidateError>();
				ValidateError error = new ValidateError("Already exisit a value out of the DialPlan Range", DialPlan.class, dialPlan, ValidateType.DIALPLAN_OUT_OF_RANGE);
				errorList.add(error);
				throw new ValidateObjectException("Already exisit a value out of the DialPlan Range", errorList) ;
			}
		}
	}
	
	private void buildPBXDialPlanInfo(PBXDialPlanInfo pbxDialPlanInfo, List<DialPlan> list)
	{
		pbxDialPlanInfo.setExtensionDialPlan(get(DialPlan.TYPE_EXTENSION, list));
		pbxDialPlanInfo.setSpeedDialDialPlan(get(DialPlan.TYPE_USERSPEEDDIAL, list));
		pbxDialPlanInfo.setPublicSpeedDialDialPlan(get(DialPlan.TYPE_PUBLICSPEEDDIAL, list));
		pbxDialPlanInfo.setParkDialPlan(get(DialPlan.TYPE_PARK, list));
		pbxDialPlanInfo.setDefaultOperatorDialPlan(get(DialPlan.TYPE_DEFAULTOPERATOR, list));		
	}
	
	public void validateDialPlan(DialPlan ...dPlans) throws ValidateObjectException
	{
		for(DialPlan dPlan : dPlans)
		{
			switch (dPlan.getType()) 
			{
				case DialPlan.TYPE_EXTENSION:
					String start = String.valueOf(dPlan.getStart());
					if(start.length() < DialPlan.EXTENSION_MIN_LENGTH)
					{
						List<ValidateError> errorList = new ArrayList<ValidateError>();
						ValidateError error = new ValidateError("Extension minimun length : " + DialPlan.EXTENSION_MIN_LENGTH, DialPlan.class, dPlan, ValidateType.DIALPLAN_EXTENSION_LENGTH);
						errorList.add(error);
						throw new ValidateObjectException("Extension minimun length : " + DialPlan.EXTENSION_MIN_LENGTH, errorList) ;
					}
					
					String end = String.valueOf(dPlan.getEnd());
					
					if(start.length() != end.length()){						
						List<ValidateError> errorList = new ArrayList<ValidateError>();
						ValidateError error = new ValidateError("The start and end value length are different!", DialPlan.class, dPlan, ValidateType.DIALPLAN_DIFFERENT_LENGTH);
						errorList.add(error);
						throw new ValidateObjectException("The start and end value length are different!", errorList) ;
					}
					break;
		
				case DialPlan.TYPE_PARK:
					if(dPlan.getEnd() - dPlan.getStart() > 50)
					{
						List<ValidateError> errorList = new ArrayList<ValidateError>();
						ValidateError error = new ValidateError("Problem with DialPlans Ranges", DialPlan.class, dPlan, ValidateType.DIALPLAN_PARKRANGE);
						errorList.add(error);
						throw new ValidateObjectException("Problem with DialPlans Ranges", errorList) ;
					}
					break;
				default:
					break;
			}		
		}
	}	
	
	public void validateDialPlanNumber(DialPlan dialPlan, String address) throws ValidateObjectException, NumberFormatException, DAOException
	{
		boolean isValid = Integer.valueOf(address) >= dialPlan.getStart() && Integer.valueOf(address) <= dialPlan.getEnd();
		if(!isValid)
		{
			StringBuilder str = new StringBuilder("The ");
			str.append(getDialPlanName(dialPlan.getType()));
			str.append(" number is out of range");
			
			List<ValidateError> errorList = new ArrayList<ValidateError>();
			ValidateError error = new ValidateError(str.toString() , DialPlan.class, address, ValidateType.DIALPLAN_OUT_OF_RANGE);
			errorList.add(error);
			 throw new ValidateObjectException(str.toString(), errorList) ;		
		}
	}
	
	private String getDialPlanName(int type)
	{
		switch(type)
		{
			case DialPlan.TYPE_DEFAULTOPERATOR:
				return "Default Operator";
			case DialPlan.TYPE_EXTENSION:
				return "Extension";
			case DialPlan.TYPE_PARK:
				return "Park";
			case DialPlan.TYPE_PUBLICSPEEDDIAL:
				return "Public Speed Dial";
			case DialPlan.TYPE_USERSPEEDDIAL:
				return "User Speed Dial";
				default :
				return null;
		}
	}
	
	private List getNumber(DialPlan dialPlan, Long pbxKey) throws DAOException	
	{		
		List dialPlanList = null;
		
		switch (dialPlan.getType()) 
		{
			case DialPlan.TYPE_DEFAULTOPERATOR:
					dialPlanList = dialPlanDAO.getDefaultOperatorByPbx(pbxKey, dialPlan.getStart(), dialPlan.getEnd());
				break;
	
			case DialPlan.TYPE_EXTENSION:		
				List<Address> extensionList = dialPlanDAO.getExtensionByPbx(pbxKey);
				dialPlanList = new ArrayList();
				for(Address ad : extensionList)
				{
					if(isNumber(ad.getAddress()))
					{
						Integer extension = Integer.valueOf(ad.getAddress());
						if(extension < dialPlan.getStart() || extension > dialPlan.getEnd())
							dialPlanList.add(ad);
					}
				}				
				break;
	
			case DialPlan.TYPE_PARK:
				dialPlanList = dialPlanDAO.getParkListByPbxKey(pbxKey, dialPlan.getStart(), dialPlan.getEnd());
				break;
	
			case DialPlan.TYPE_PUBLICSPEEDDIAL:
				dialPlanList = dialPlanDAO.getSpeedDialByPbx(pbxKey, dialPlan.getStart(), dialPlan.getEnd());
				break;
	
			case DialPlan.TYPE_USERSPEEDDIAL:
				dialPlanList = dialPlanDAO.getUserspeeddial(pbxKey, dialPlan.getStart(), dialPlan.getEnd());				
				break;
	
			default:
				break;
		}	
		return dialPlanList;
	}
	
	private boolean isNumber(String string)
    {
    	return string.matches("(\\d[- \\.]?)+");
    }
}
