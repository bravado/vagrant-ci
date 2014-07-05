package br.com.voicetechnology.ng.ipx.rule.implement;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.LinkDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Link;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.Info;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.LinkInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class LinkManager extends Manager
{
	private LinkDAO linkDAO;
	private GroupDAO gDAO;
	
	public LinkManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		linkDAO = dao.getDAO(LinkDAO.class);
		gDAO = dao.getDAO(GroupDAO.class);
	}
	
	public <T extends Info> ReportResult<T> find(Report<LinkInfo> report) throws DAOException
	{
		ReportDAO<Link, LinkInfo> linkReport = dao.getReportDAO(LinkDAO.class);
		Long size = linkReport.getReportCount(report);
		List<Link> linkList = linkReport.getReportList(report);
		List<LinkInfo> linkInfoList = new ArrayList<LinkInfo>();
		for(Link link : linkList)
		{
			LinkInfo linkinfo = new LinkInfo(link, new ArrayList<Duo<Long,String>>()); 
			if(!report.getInfo().isHomeTab())
				getLinkInfoContext(linkinfo, linkinfo.getDomainKey());
			linkInfoList.add(linkinfo);
		}
		return (ReportResult<T>) new ReportResult<LinkInfo>(linkInfoList, size);
	}
	
	public LinkInfo getLinkInfoByKey(Long linkKey) throws DAOException
	{
		Link link = linkDAO.getByKey(linkKey);
		List<Duo<Long, String>> gList = gDAO.getGroupKeyAndNameByDomain(link.getDomainKey());
		return new LinkInfo(link, gList);
	}

	public void getLinkInfoContext(LinkInfo info, Long domainKey) throws DAOException
	{
		List<Duo<Long, String>> groupList = gDAO.getGroupKeyAndNameByDomain(domainKey);
		info.addGroupList(groupList);
		if(info.getGroupKey() != null)
		{
			Group g = gDAO.getByKey(info.getGroupKey());
			if(g != null)
				info.setGroupName(g.getName());
		}
	}
	
	public void save(LinkInfo info) throws DAOException, ValidateObjectException, MalformedURLException
	{
		Link link = info.getLink();
		linkDAO.save(link);
	}

	private void validateLink(Link link) throws MalformedURLException
	{
		String url = link.getUrl();
		if(url.matches("www[\\d]?\\..+"))
			link.setUrl("http://" + url);
		else if(!url.matches("http://.+") && !url.matches("https://.+") && 
				!url.matches("ftp://.+") && !url.matches("file:///[/?.+]"))
			throw new MalformedURLException("Invalid protocol, try to use http, https, ftp or file!!!");
	}

	public void deleteLinks(List<Long> linkKeyList) throws DAOException
	{
		for(Long key : linkKeyList)
			deleteLink(key);
	}
	
	public void deleteLink(Long linkKey) throws DAOException
	{
		Link link = linkDAO.getByKey(linkKey);
		linkDAO.remove(link);
	}
}
