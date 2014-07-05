package br.com.voicetechnology.ng.ipx.dao.pbx;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterConfig;

public interface CallCenterConfigDAO extends DAO<CallCenterConfig>
{
	public CallCenterConfig getCallCenterConfigByGroupkey(Long groupKey) throws DAOException;
}
