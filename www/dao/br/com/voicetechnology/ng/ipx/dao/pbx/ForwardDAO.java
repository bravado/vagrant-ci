package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contact;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ContactsInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ForwardInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;

public interface ForwardDAO extends DAO<Forward>, ReportDAO<Forward, ForwardInfo>
{

	public Forward getForwardByConfig(Long configKey, int mode) throws DAOException;
	
	public List<Forward> getForwardListByConfig(Long configKey) throws DAOException;

	public Long countForwardsToAddress(Long addressKey) throws DAOException;
	
	public List<Forward> getForwardListByAddress(Long addressKey) throws DAOException;
	
	public Long getForwardInfoReportCountByUser(Report<ForwardInfo> report) throws DAOException;
	
	public List<ForwardInfo> getForwardInfoReportByUser(Report<ForwardInfo> report) throws DAOException;
	
	public Long getForwardInfoReportCountByGroup(Report<ForwardInfo> report) throws DAOException;
	
	public List<ForwardInfo> getForwardInfoReportByGroup(Report<ForwardInfo> report) throws DAOException;

}
