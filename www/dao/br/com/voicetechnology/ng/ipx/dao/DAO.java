package br.com.voicetechnology.ng.ipx.dao;


import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.pojo.db.PersistentObject;

/**
 * Interface pai. Define os metodos que deverao ser implementados em todos os
 * <b>Data Access Object</b>'s que implementarem esta interface.
 * @author Fernando Fontes
 * @version 1.0
 */
public interface DAO<T extends PersistentObject>
{
    /**
     * Define operacao de <code>save(PersistentObject)</code>.
     */
    int SAVE = 0;
    
    /**
     * Define operacao de <code>update(PersistentObject)</code>.
     */
    int UPDATE = 1;

    /**
     * Salva o <code>PersistentObject obj</code>.
     * @param obj <code>PersistentObject</code> a ser persistido.
     * @throws DAOException Caso nao possa salvar o objeto.
     */
	public void save(T obj) throws DAOException, ValidateObjectException;

    /**
     * Apaga o <code>PersistentObject obj</code> do local de persistencia.
     * @param obj <code>PersistentObject</code> a ser removido.
     * @throws DAOException Caso nao possa excluir o objeto.
     */
    public void remove(T obj) throws DAOException;

    /**
     * Verifica se o <code>PersistentObject</code> definido por <code>obj</code>
     * e um objeto valido para ser armazenado no local de persistencia.
     * @param obj <code>PersistentObject</code> a ser validado.
     * @param operation Pode ser <code>SAVE</code> ou <code>UPDATE</code>.
     * @param errors TODO
     * @throws DAOException Caso a validacao falhe por algum motivo.
     * @throws ValidateObjectException 
     */
    void validate(T obj, int operation, List<ValidateError> errors) throws DAOException, ValidateObjectException;

	/**
     * Retorna o objeto <code>PersistentObject</code> relacionado ao bean
     * associado cujo idendificador ou chave seja igual a <code>key</code>.
     * @param key Idendificador ou chave do <code>PersistentObject</code>
     * procurado.
     * @return Objeto <code>PersistentObject</code> cujo indentificador ou
     * chave e igual a <code>key</code>.
	 * @throws DAOException Joga uma excecao do tipo
     * <code>DBObjectNotFoundException</code> caso o objeto requisitado nao seja
     * encontrado, ou caso seja encontrado mais de um objeto com o mesmo
     * identificador ou chave <code>key</code>. Joga uma excecao do tipo
     * <code>CalleidoscopeDBException</code> caso a busca pelo objeto falhe por
     * algum motivo.
     */  
    public T getByKey(Long key) throws DAOException;

}