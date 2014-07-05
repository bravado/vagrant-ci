package br.com.voicetechnology.ng.ipx.dao.sys;

import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.ActivityLog;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ActivityLogInfo;

public interface ActivityLogDAO extends DAO<ActivityLog>, ReportDAO<ActivityLog, ActivityLogInfo>
{

}
