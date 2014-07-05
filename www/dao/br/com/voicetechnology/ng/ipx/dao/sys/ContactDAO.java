package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contact;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ContactsInfo;

public interface ContactDAO extends DAO<Contact>, ReportDAO<Contact, ContactsInfo>
{
	Contact getContactByUserAndAddress(Long userKey, String address) throws DAOException;

	Contact getPublicContact(Long domainKey, String address) throws DAOException;
	
	Contact getPublicContactByPbxKey(Long pbxKey, String address) throws DAOException;
	
	void validateUniquePrivateContact(Contact contact, Long userKey) throws DAOException, ValidateObjectException;

	List<Contact> getAllContacts(Long userKey, Long domainKey, Integer lastIndex, Integer maxResult) throws DAOException;

	List<Contact> getAllContacts(Long userKey, Long domainKey, String searchWord) throws DAOException;

	Contact getContactByUserAndPhoneAndDomain(String username, String phone, String domain) throws DAOException;

	Contact getPublicContactByPhoneAndDomain(String phone, String domain) throws DAOException;
}