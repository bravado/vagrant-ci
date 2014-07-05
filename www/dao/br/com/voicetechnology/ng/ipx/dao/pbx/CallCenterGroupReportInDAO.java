package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterGroupReportIn;

public interface CallCenterGroupReportInDAO extends DAO<CallCenterGroupReportIn>{
	public List<Long> getCallCenterGroupsKey(String pbxFarmIP) throws DAOException;
	public List<CallCenterGroupReportIn> getUnprocessedReportsByDate(Long groupKey, int type, Calendar startDate, Calendar endDate) throws DAOException;
	public List<CallCenterGroupReportIn> getReportsByDate(Long groupKey, int type, Calendar startDate, Calendar endDate) throws DAOException;
}
