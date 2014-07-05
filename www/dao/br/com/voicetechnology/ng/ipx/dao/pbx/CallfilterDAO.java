package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Callfilter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Filter;

public interface CallfilterDAO extends DAO<Callfilter>
{      
	public Callfilter getCallFilterByPbxuser(Long pbxuserKey) throws DAOException;
	
	public List<Filter> getFilterByCallFilter(Long callFilter) throws DAOException;
}
