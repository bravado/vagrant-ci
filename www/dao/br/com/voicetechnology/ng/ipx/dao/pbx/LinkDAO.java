package br.com.voicetechnology.ng.ipx.dao.pbx;

import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Link;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.LinkInfo;

public interface LinkDAO extends DAO<Link>, ReportDAO<Link, LinkInfo>
{
	
}
