package br.com.voicetechnology.ng.ipx.dao.reporting;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.pojo.db.PersistentObject;
import br.com.voicetechnology.ng.ipx.pojo.info.Info;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;

public interface ReportDAO<T extends PersistentObject, E extends Info>
{
	
	public List<T> getReportList(Report<E> report) throws DAOException;
	
	public Long getReportCount(Report<E> report) throws DAOException;

}