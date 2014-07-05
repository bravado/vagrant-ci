package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ConfigInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;

public interface ConfigDAO extends DAO<Config>
{

	public Config getConfigServiceclassByPbxuser(Long puKey) throws DAOException;

	public Config getConfigByPbxuser(Long pbxuserKey) throws DAOException;

	public Config getConfigByBlock(Long blockKey) throws DAOException;
	
	public List<Config> getConfigListByUsersInDomain(Long domainKey) throws DAOException;

	public Config getConfigWithForwardListByConfigkey(Long configKey)throws DAOException; // tveiga basix store

	public Long getReportCount(Report<ConfigInfo> report) throws DAOException; // tveiga basix store

	public List<Config> getReportList(Report<ConfigInfo> report)throws DAOException; // tveiga basix store
}
