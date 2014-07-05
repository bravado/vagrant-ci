package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.CategoryDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.CategorycontactDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Category;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Categorycontact;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CategoryInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class CategoryManager extends Manager
{
	private CategoryDAO categoryDAO;
	private CategorycontactDAO categoryContactDAO;
	private ReportDAO<Category, CategoryInfo> reportCategory;
	
	public CategoryManager(String loggerPath) throws DAOException 
	{
		super(loggerPath);
		this.initDAOs();
	}
	
	public CategoryManager(Logger logger) throws DAOException
	{
		super(logger);
		this.initDAOs();
	}
	
	private void initDAOs() throws DAOException
	{
		categoryDAO = dao.getDAO(CategoryDAO.class);
		categoryContactDAO = dao.getDAO(CategorycontactDAO.class);
		reportCategory = dao.getReportDAO(CategoryDAO.class);
	}
	
	public ReportResult findCategories(Report<CategoryInfo> info) throws DAOException
	{
		Long size = reportCategory.getReportCount(info);
		List<Category> categoryList = reportCategory.getReportList(info);
		List<CategoryInfo> categoryInfoList = new ArrayList<CategoryInfo>(categoryList.size());
		for(Category category : categoryList)
			categoryInfoList.add(new CategoryInfo(category));
		return new ReportResult<CategoryInfo>(categoryInfoList, size);
	}

	public void save(CategoryInfo categoryInfo) throws DAOException, ValidateObjectException 
	{
		Category category = categoryInfo.getCategory();
		if(category.getType() == null)
			category.setType(Category.TYPE_OTHERS);
		validateSave(category);
		categoryDAO.save(category);
	}

	protected void validateSave(Category category) throws DAOException, ValidateObjectException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(category == null)
		{
			errorList.add(new ValidateError("Category is null!", Category.class, null, ValidateType.BLANK));
		} else
		{
			Long userKey = category.getUserKey();
			if(userKey == null)
				errorList.add(new ValidateError("Category userKey is null!", Category.Fields.USER_KEY.toString(), Category.class, category, ValidateType.BLANK));
			
			String name = category.getName();
			if(name == null)
				errorList.add(new ValidateError("Category name is null!", Category.Fields.NAME.toString(), Category.class, category, ValidateType.BLANK));
			else if(name.length() == 0)
				errorList.add(new ValidateError("Category name is blank!", Category.Fields.NAME.toString(), Category.class, category, ValidateType.BLANK));
		}
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
	}

	public CategoryInfo getCategoryInfoByKey(Long categoryKey) throws DAOException
	{
		Category category = categoryDAO.getByKey(categoryKey);
		return new CategoryInfo(category);
	}

	public void deleteCategories(List<Long> categoriesKeyList) throws DAOException
	{
		for(Long categoryKey : categoriesKeyList)
			this.deleteCategory(categoryKey);
	}

	private void deleteCategory(Long categoryKey) throws DAOException
	{
		Category category = categoryDAO.getByKey(categoryKey);
		this.RemoveDepenciesWithContacts(categoryKey);
		categoryDAO.remove(category);
	}

	private void RemoveDepenciesWithContacts(Long categoryKey) throws DAOException
	{
		List<Categorycontact> categoryContactList = categoryContactDAO.getByCategoryKey(categoryKey);
		if(categoryContactList != null && categoryContactList.size() > 0)
			for(Categorycontact categoryContact : categoryContactList)
				categoryContactDAO.remove(categoryContact);
	}
}