package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.GroupPause;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pause;

public interface GroupPauseDAO  extends DAO<GroupPause>
{
	public List<GroupPause> getGroupPauseListByGroupKey(Long groupKey) throws DAOException;
	
	public List<Pause> getPauseListActiveByGroupKey(Long groupKey) throws DAOException;
	
	public List<GroupPause> getGroupPauseListActiveByPbxuserkey(Long pbxuserKey) throws DAOException;
	
	public GroupPause getGroupPauseByGroupKeyAndPauseCode(Long groupKey, Integer pauseCode) throws DAOException;
	
	public GroupPause getGroupPauseByGroupKeyAndGroupPosition(Long groupKey, Integer groupPosition) throws DAOException;
	
	public List<GroupPause> getGroupPauseListActiveByGroupKey(Long groupKey) throws DAOException;
}
