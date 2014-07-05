package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CostCenter;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CostCenterInfo;

public interface CostCenterDAO  extends DAO<CostCenter>, ReportDAO<CostCenter, CostCenterInfo>
{
	public List<CostCenter> getByDomain(Long domainKey) throws DAOException;

	public CostCenter getByDomainAndCode(String domain, String code) throws DAOException;

	public CostCenter getByNameAndDomain(String name, String domain) throws DAOException;
	
	public List<CostCenter> getListByDomainKey(Long domainKey) throws DAOException;
}
