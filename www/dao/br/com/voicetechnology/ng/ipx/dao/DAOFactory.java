package br.com.voicetechnology.ng.ipx.dao;

import java.lang.reflect.Constructor;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOFactoryNotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAONotFoundException;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;

/**
 * Classe abstrata <code>Factory</code> para <code>Data Access Object</code>'s.
 * Define quais sao os possiveis beans. Possui o metodo
 * <code>createFactory(int)</code> para criar uma <code>Factory</code> de
 * objetos e obriga a classe filha a implementar o metodo
 * <code>getDAO(int)</code>, que retornara um objeto bean desejado. 
 * @author Fernando Fontes
 * @version 1.0
 */
public abstract class DAOFactory
{
    public static DAOFactory createFactory(String className, String jndiPath) throws DAOFactoryNotFoundException
    {
    	try
    	{
    		Class sClass = Class.forName(className);
    		Constructor constructor = sClass.getConstructor(String.class);
    		return (DAOFactory) constructor.newInstance(jndiPath);
    	}catch(Exception e)
    	{
    		throw new DAOFactoryNotFoundException("DAOFactory not found!", className, jndiPath, e);
    	}
    }
	
    /**
	 * Getter para o <code>Data Access Object</code> definido pelo parametro
	 * <code>daoObject</code>.
	 * 
	 * @param daoObject
	 *            Define qual sera o <code>Data Access Object</code> a ser
	 *            retornado.
	 * @return <code>Data Access Object</code> definida por
	 *         <code>daoObject</code>.
	 * @throws ObjectNotFoundException
	 *             Caso nao seja encontrado um <code>Data Access Object</code>
	 *             associado a <code>daoObject</code>.
	 */
    public abstract <T extends DAO> T getDAO(Class<T> clazz) throws DAONotFoundException;
    
    public abstract <T extends ReportDAO> T getReportDAO(Class clazz) throws DAONotFoundException;
}