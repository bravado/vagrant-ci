package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterLoginReport;

public interface CallCenterLoginReportDAO extends DAO<CallCenterLoginReport>{
	public List<CallCenterLoginReport> getUnprocessedLoginReportsByDate(Long groupKey, int type, Calendar startDate, Calendar endDate) throws DAOException;
	public CallCenterLoginReport getLastCallCenterLoginReport(Long groupKey, Long pbxUserKey) throws DAOException;
	public List<CallCenterLoginReport> getReportsByDate(int type, Long pbxuserKey, Long groupKey, Calendar startDate, Calendar endDate) throws DAOException;
}
