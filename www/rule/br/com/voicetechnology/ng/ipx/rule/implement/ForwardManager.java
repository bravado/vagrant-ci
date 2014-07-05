package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAONotFoundException;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ConfigInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ForwardInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserForwardsInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class ForwardManager extends Manager{

	private ForwardDAO forwardDao;
	private ReportDAO<Forward, ForwardInfo> reportForward;
	
	public ForwardManager(Logger logger) throws DAONotFoundException {
		super(logger);
		forwardDao = dao.getDAO(ForwardDAO.class);
		reportForward = (ReportDAO<Forward, ForwardInfo>) dao.getDAO(ForwardDAO.class);
		// TODO Auto-generated constructor stub
	}
	
	public ReportResult findForwards(Report<ForwardInfo> report) throws DAOException 
	{
		
		List <ForwardInfo> reportList = null;
		Long size = null;
		
		if(report.getInfo().getListBy() == report.getInfo().LIST_BY_USERS){
			reportList = forwardDao.getForwardInfoReportByUser(report);
			size = forwardDao.getForwardInfoReportCountByUser(report);
		}
		else if(report.getInfo().getListBy() == report.getInfo().LIST_BY_GROUP){
			reportList = forwardDao.getForwardInfoReportByGroup(report);
			size = forwardDao.getForwardInfoReportCountByGroup(report);
		}
		
		return new ReportResult<ForwardInfo>(reportList,size);		
	}
	

}
