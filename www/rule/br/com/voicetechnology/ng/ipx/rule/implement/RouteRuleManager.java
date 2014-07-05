package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.utils.regex.RegEx;
import br.com.voicetechnology.ng.ipx.dao.pbx.GatewayDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.RouteruleDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Routerule;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.RouteRuleInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class RouteRuleManager extends Manager 
{
	private RouteruleDAO routeRuleDAO;
	private ReportDAO<Routerule, RouteRuleInfo> reportRouteRule;
	private GatewayDAO gatewayDAO;

	public RouteRuleManager(String loggerPath) throws DAOException 
	{
		super(loggerPath);
		routeRuleDAO = dao.getDAO(RouteruleDAO.class);
		reportRouteRule = dao.getDAO(RouteruleDAO.class);
		gatewayDAO = dao.getReportDAO(GatewayDAO.class);
	}

	public ReportResult findRouteRule(Report<RouteRuleInfo> info) throws DAOException
	{
		Long size = reportRouteRule.getReportCount(info);
		List<Routerule> routeRuleList = reportRouteRule.getReportList(info);
		List<RouteRuleInfo> routeRuleInfoList = new ArrayList<RouteRuleInfo>(routeRuleList.size());
		List<Duo<Long, String>> gatewayList = gatewayDAO.getGatewayList();
		for(Routerule routeRule : routeRuleList)
			routeRuleInfoList.add(new RouteRuleInfo(routeRule, gatewayList));
		return new ReportResult<RouteRuleInfo>(routeRuleInfoList, size);
	}

	public void deleteRouteRules(List<Long> routeRulesKeys) throws DAOException
	{
		for(Long routeRuleKey : routeRulesKeys)
			this.deleteRouteRules(routeRuleKey);
	}

	private void deleteRouteRules(Long routeRuleKey) throws DAOException
	{
		Routerule routeRule = routeRuleDAO.getByKey(routeRuleKey);
		routeRuleDAO.remove(routeRule);
	}

	public RouteRuleInfo getRouteRuleInfo(Long routeRuleKey) throws DAOException
	{
		Routerule routeRule = routeRuleDAO.getByKey(routeRuleKey);
		List<Duo<Long, String>> gatewayList = gatewayDAO.getGatewayList();
		RouteRuleInfo routeRuleInfo = new RouteRuleInfo(routeRule, gatewayList);
		return routeRuleInfo;
	}

	public void getRouteRuleInfoContext(RouteRuleInfo routeRuleInfo) throws DAOException
	{
		routeRuleInfo.addGatewayList(gatewayDAO.getGatewayList());
	}

	public void saveRouteRule(RouteRuleInfo routeRuleInfo) throws DAOException, ValidateObjectException
	{
		Routerule routeRule = routeRuleInfo.getRouteRule();
		routeRule.setRegex(RegEx.getRegEx(routeRule.getPattern()));
		if(routeRule.getKey() == null)
			routeRule.setRouteruleDefault(Routerule.ROUTERULE_DEFAULT_OFF);
		routeRule.setType(Routerule.TYPE_ROUTERULE);
		validateSave(routeRule);
		routeRuleDAO.save(routeRule);
	}
	
	protected void validateSave(Routerule routeRule) throws DAOException, ValidateObjectException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(routeRule == null)
		{
			errorList.add(new ValidateError("RouteRule is null!", Routerule.class, null, ValidateType.BLANK));
		} else
		{
			Long gatewayKey = routeRule.getGatewayKey();
			if(gatewayKey == null)
				errorList.add(new ValidateError("RouteRule Gateway Key is null!", Routerule.Fields.GATEWAY_KEY.toString(), Routerule.class, routeRule, ValidateType.BLANK));
		}
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
	}
}