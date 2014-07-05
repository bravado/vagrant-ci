package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.sys.PermissionDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Permission;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class PermissionManager extends Manager
{
	private PermissionDAO pDAO;
	
	public PermissionManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		initDAO();
	}

	public PermissionManager(Logger logger) throws DAOException
	{
		super(logger);
		initDAO();
	}
	
	private void initDAO() throws DAOException
	{
		pDAO = dao.getDAO(PermissionDAO.class);
	}
	
	public boolean checkPermission(Long userKey, String permission) throws DAOException
	{
	    List<Permission> pList = pDAO.getPermissionListByUser(userKey);
            boolean hasPermission = false;
            String[] permArray = permission.split("\\.");
            
            for(int i = 0; i < pList.size() && !hasPermission; i++)
            {
                Permission p = pList.get(i);
                
                if(p.getName().equals(permission))
                {
                	hasPermission = true;
                }
                else
                {
                    String[] tmp = p.getName().split("\\.");
                    int j;
                    
                    for(j = 0; j < tmp.length && j < permArray.length; j++)
                    {
                        if (!tmp[j].equals(permArray[j]))
                        {
                            break;
                        }
                    }
                    
                    if(tmp[j].equals("*"))
                    {
                    	hasPermission = true;
                    }
                }
            }
	
            return hasPermission;
	}
}
