package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Userspeeddial;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.UserSpeedDialInfo;

public interface UserspeeddialDAO extends DAO<Userspeeddial>, ReportDAO<Userspeeddial, UserSpeedDialInfo>
{

	public Userspeeddial getUserspeeddial(String user, String position, String domain) throws DAOException;
	

	
}
