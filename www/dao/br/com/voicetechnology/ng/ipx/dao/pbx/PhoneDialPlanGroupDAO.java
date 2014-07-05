package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlanGroup;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PhoneDialPlanGroupInfo;

public interface PhoneDialPlanGroupDAO extends DAO<PhoneDialPlanGroup>, ReportDAO<PhoneDialPlanGroup, PhoneDialPlanGroupInfo>{ 
	public List<PhoneDialPlanGroup> getByDomainKey(Long domainKey) throws DAOException;
	public List<PhoneDialPlanGroup> getByPhoneDialPlanKey(Long phoneDialPlanKey) throws DAOException;
}
