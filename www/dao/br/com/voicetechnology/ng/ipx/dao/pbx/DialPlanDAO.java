package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Park;

public interface DialPlanDAO  extends DAO<DialPlan>
{
	public List<DialPlan> getPBXDialPlanByDomain(Long domainKey) throws DAOException;
	
	public List<DialPlan> getPBXDialPlanByDomain(String domain) throws DAOException;
	
	public List<DialPlan> getPBXDialPlanByPbxpreference(Long pbxPreferenceKey) throws DAOException;
	
	public DialPlan getDialPlanByTypeAndPbxPreference(int type, Long pbxpreferenceKey) throws DAOException;
	
	public DialPlan getDialPlanByTypeAndDomain(int type, Long domainKey) throws DAOException;
	
	public DialPlan getDialPlanByTypeAndDomain(int type, String domain) throws DAOException;
	
	public DialPlan getDialPlanByTypeAndPbx(int type, Long pbxKey) throws DAOException;
	
	public DialPlan getDialPlanByTypeAndPbxuser(int type, Long pbxuserKey) throws DAOException;
	
	public List<DialPlan> getPBXDialPlanByPbx(Long pbxKey) throws DAOException;
	
	public List<Park> getParkListByPbxKey(Long pbxKey, Integer startValue, Integer EndValue) throws DAOException;
	
	public List<Address> getExtensionByPbx(Long pbxKey) throws DAOException;
	
	public List getDefaultOperatorByPbx(Long pbxKey, Integer startValue, Integer endValue) throws DAOException;
	
	public List getSpeedDialByPbx(Long pbxKey, Integer startValue, Integer endValue)  throws DAOException;
	
	public List getUserspeeddial(Long domainKey, Integer startValue, Integer endValue) throws DAOException;
}
