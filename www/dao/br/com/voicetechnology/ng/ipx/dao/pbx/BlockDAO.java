package br.com.voicetechnology.ng.ipx.dao.pbx;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;

public interface BlockDAO extends DAO<Block>
{
	public Block getBlockByConfig(Long configKey, int blockType) throws DAOException;

	public Block getBlockWithItens(Long configKey, int blockType) throws DAOException;

	public Block getBlockByPbxuser(Long pbxuserKey) throws DAOException;
}
