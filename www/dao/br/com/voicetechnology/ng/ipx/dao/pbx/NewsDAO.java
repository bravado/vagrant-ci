package br.com.voicetechnology.ng.ipx.dao.pbx;

import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.News;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.NewsInfo;

public interface NewsDAO extends DAO<News>, ReportDAO<News, NewsInfo>
{

}
