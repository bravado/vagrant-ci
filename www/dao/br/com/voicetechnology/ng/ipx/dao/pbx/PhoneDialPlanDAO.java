package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlan;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PhoneDialPlanInfo;

public interface PhoneDialPlanDAO  extends DAO<PhoneDialPlan>, ReportDAO<PhoneDialPlan, PhoneDialPlanInfo>{
	public List<PhoneDialPlan> getPhoneDialPlansByDomain(String domain, int type) throws DAOException;
	public List<PhoneDialPlan> getPhoneDialPlansByDomain(Long domainKey, int type) throws DAOException;	
	public List<PhoneDialPlan> getPhoneDialPlansByGroup(Long groupKey) throws DAOException;	
	public List<PhoneDialPlan> getPhoneDialPlansByPbxuser(Long pbxuserKey) throws DAOException;
}
