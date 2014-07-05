package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Mediaapplicationfile;

public interface MediaapplicationfileDAO extends DAO<Mediaapplicationfile>
{

	public List<Mediaapplicationfile> getMediaapplicationFileListByFile(Long fileKey) throws DAOException;

}