package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterPaReport;

public interface CallCenterPaReportDAO  extends DAO<CallCenterPaReport>{ 
	public List<CallCenterPaReport> getByGroupReport(Long groupReportKey) throws DAOException;
	public List<CallCenterPaReport> getReportsByDate(Long pbxuserKey, Long groupKey, int type, Calendar startDate, Calendar endDate) throws DAOException;
}
