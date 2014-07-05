package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Routerule;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.RouteRuleInfo;

public interface RouteruleDAO extends DAO<Routerule>, ReportDAO<Routerule, RouteRuleInfo>
{

	public List<Routerule> getRouteListByPriority() throws DAOException;
	
	public List<Routerule> getRouteListByGateway(Long gatewayKey) throws DAOException;
	
	public Routerule getRouteRuleByPriority(Integer priority) throws DAOException;
	
	public Routerule getRouteRuleByPattern(String pattern) throws DAOException;
}
