package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.NewsDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.News;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.Info;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.NewsInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class NewsManager extends Manager
{
	private NewsDAO newsDAO;
	private GroupDAO gDAO;
	private DomainDAO domainDAO;
	public NewsManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		newsDAO = dao.getDAO(NewsDAO.class);
		gDAO = dao.getDAO(GroupDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
	}
	
	public <T extends Info> ReportResult<T> find(Report<NewsInfo> report) throws DAOException
	{
		ReportDAO<News, NewsInfo> newsReport = dao.getReportDAO(NewsDAO.class);
		Long size = newsReport.getReportCount(report);
		List<News> newsList = newsReport.getReportList(report);
		List<NewsInfo> newsInfoList = new ArrayList<NewsInfo>();
		Long domainKey = report.getInfo().getDomainKey();
		Domain domain = domainKey != null ? domainDAO.getByKey(domainKey) : domainDAO.getRootDomain();
		for(News news : newsList)
		{
			domain = domainDAO.getByKey(news.getDomainKey());
			NewsInfo newsInfo = new NewsInfo(news, domain.getDomain(), null);
			if(!report.getInfo().isHomeTab())
				this.getNewsInfoContext(newsInfo, news.getDomainKey());
			newsInfoList.add(newsInfo);
		}
		return (ReportResult<T>) new ReportResult<NewsInfo>(newsInfoList, size);
	}
	
	public NewsInfo getNewsInfoByKey(Long newsKey) throws DAOException
	{
		News news = newsDAO.getByKey(newsKey);
		NewsInfo info = new NewsInfo(news, null, null);
		getNewsInfoContext(info, news.getDomainKey());
		return info;
	}

	public void getNewsInfoContext(NewsInfo info, Long domainKey) throws DAOException
	{
		Domain domain = domainDAO.getByKey(domainKey);
		info.setDomainList(domainDAO.getDomainsByRootDomain(domain.getRootKey() != null ? domain.getRootKey() : domain.getKey()));
		if(domainKey != null)
		{
			List<Duo<Long, String>> groupList = gDAO.getGroupKeyAndNameByDomain(domainKey);
			info.addGroupList(groupList);
		}
		if(info.getGroupKey() != null)
		{
			Group g = gDAO.getByKey(info.getGroupKey());
			info.setGroupName(g != null ? g.getName() : null);
		}
	}
	
	public void save(NewsInfo info) throws DAOException, ValidateObjectException
	{
		News news = info.getNews();
		validateNews(news);
		newsDAO.save(news);
	}
	
	private void validateNews(News news) throws ValidateObjectException
	{
		if(news.getStartDate() != null && news.getEndDate() != null)
			if(news.getStartDate().after(news.getEndDate()))
				throw new ValidateObjectException("End date must be after start date!", News.class, news, ValidateType.INVALID);
	}

	public void deleteNews(List<Long> newsKeyList) throws DAOException
	{
		for(Long key : newsKeyList)
			deleteNews(key);
	}

	public void deleteNews(Long newsKey) throws DAOException
	{
		News news = newsDAO.getByKey(newsKey);
		newsDAO.remove(news);
	}
}