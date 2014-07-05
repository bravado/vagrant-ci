package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Koushikubun;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.KoushiKubunItemInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.webreport.wcc.settings.KoushiKubunWebReport;

public interface KoushikubunDAO extends DAO<Koushikubun>, ReportDAO<Koushikubun, KoushiKubunItemInfo>
{
	public List<Koushikubun> getKoushiKubunListByPbxuserKey(Long pbxuserKey) throws DAOException;
	
	public List<Koushikubun> getKoushiKubunListByPbxuserKey(Long pbxuserKey, Integer active) throws DAOException;
	
	public List<Koushikubun> getListByDomain(Long domainKey) throws DAOException;
	
}