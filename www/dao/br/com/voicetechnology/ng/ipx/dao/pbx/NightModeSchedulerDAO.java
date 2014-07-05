package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.NightModeScheduler;

public interface NightModeSchedulerDAO extends DAO<NightModeScheduler>{

	public List<NightModeScheduler> getNightModeSchedulerByPbx(Long pbxKey, boolean isHoliday) throws DAOException;
	
	public List<NightModeScheduler> getNightModeSchedulerByPbx(Long pbxKey) throws DAOException;
	
	public List<NightModeScheduler> getNightModeSchedulerByGroup(Long groupKey) throws DAOException;	
}
