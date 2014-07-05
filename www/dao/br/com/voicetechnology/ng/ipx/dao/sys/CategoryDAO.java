package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Category;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CategoryInfo;

public interface CategoryDAO extends DAO<Category>, ReportDAO<Category, CategoryInfo>
{
	public Category getDefaultCategoryByUser(Long userKey) throws DAOException;
	
	public List<Duo<Long, String>> getCategoryKeyAndNameByContactAndUser(Long contactKey, Long userKey) throws DAOException;
	
	public List<Duo<Long, String>> getCategoryKeyAndNameByUser(Long userKey) throws DAOException;
	
	public Category getByUserAndKey(Long userKey, Long categoryKey) throws DAOException;
}
