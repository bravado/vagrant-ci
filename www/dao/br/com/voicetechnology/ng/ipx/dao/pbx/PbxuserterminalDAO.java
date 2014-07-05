package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuserterminal;

public interface PbxuserterminalDAO extends DAO<Pbxuserterminal>
{
	public List<Pbxuserterminal> getPbxuserterminalByTerminal(Long terminalKey) throws DAOException;
	
	public List<Pbxuserterminal> getPbxuserterminalByPbxuser(Long pbxuserKey) throws DAOException;

	public Pbxuserterminal getPbxuserterminalByPbxuserKeyAndTerminalPbxuserKey(Long pbxuserKey, Long terminalPbxuserKey) throws DAOException;
}
