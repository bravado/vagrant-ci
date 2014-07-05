package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.SipTrunkRouterule;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SipTrunkRouteRuleInfo;

public interface SipTrunkRouteruleDAO extends DAO<SipTrunkRouterule>, ReportDAO<SipTrunkRouterule, SipTrunkRouteRuleInfo> 
{
	public List<SipTrunkRouterule> getSipTrunkRouteruleOrderByPriority(Long domainKey) throws DAOException;
}
