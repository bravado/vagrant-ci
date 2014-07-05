package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlanGroupElem;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PhoneDialPlanGroupElemInfo;

public interface PhoneDialPlanGroupElemDAO extends DAO<PhoneDialPlanGroupElem>, ReportDAO<PhoneDialPlanGroupElem, PhoneDialPlanGroupElemInfo>
{
	public List<PhoneDialPlanGroupElem> getByGroupKey(Long phoneDialPlanGroupKey) throws DAOException;
	public List<PhoneDialPlanGroupElem> getByPhoneDialPlanKey(Long phoneDialPlanKey) throws DAOException;

}
