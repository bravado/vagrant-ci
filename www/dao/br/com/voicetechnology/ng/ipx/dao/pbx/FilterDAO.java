package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Filter;

public interface FilterDAO extends DAO<Filter>
{

	public List<Filter> getActiveFilterListByConfig(Long configKey) throws DAOException;
	
	public List<Filter> getFilterListByConfig(Long configKey) throws DAOException;

	public List<String> getTerminalByUser(String string, Long domainKey)throws DAOException;
}
