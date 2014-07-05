package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.CategoryDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.CategorycontactDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.ContactDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.ContactphonesDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Category;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Categorycontact;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contact;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contactphones;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ContactsInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class ContactManager extends Manager
{
	private ContactDAO contactDAO;
	private CategorycontactDAO categoryContactDAO;
	private CategoryDAO categoryDAO;
	private ReportDAO<Contact, ContactsInfo> reportContact;
	private ContactphonesDAO contactPhonesDAO;
	private CalllogDAO clDAO;
	
	public ContactManager(String loggerPath) throws DAOException 
	{
		super(loggerPath);
		contactDAO = dao.getDAO(ContactDAO.class);
		categoryContactDAO = dao.getDAO(CategorycontactDAO.class);
		categoryDAO = dao.getDAO(CategoryDAO.class);
		reportContact = dao.getDAO(ContactDAO.class);
		contactPhonesDAO = dao.getDAO(ContactphonesDAO.class);
		clDAO = dao.getDAO(CalllogDAO.class);
	}

	public void deleteContacts(List<Long> contactsKeys) throws DAOException, ValidateObjectException
	{
		for(Long contactKey : contactsKeys)
			this.deleteContact(contactKey);
	}

	private void deleteContact(Long contactKey) throws DAOException, ValidateObjectException
	{
		Contact contact = contactDAO.getByKey(contactKey);
		//remove as associacoes com categorias do contato que esta sendo removido caso este seja privado
		if(contact.getType() == Contact.TYPE_PRIVATE)
		{
			List<Categorycontact> categoryContactList = categoryContactDAO.getByContactKey(contactKey);
			for(Categorycontact categoryContact : categoryContactList)
				categoryContactDAO.remove(categoryContact);
		}
		removeContactPhones(contactKey);
		removeCallLogsRelationships(contactKey);

		contactDAO.remove(contact);
	}

	private void removeContactPhones(Long contactKey) throws DAOException
	{
		List<Contactphones> contactPhonesList = contactPhonesDAO.getContactphonesListByContact(contactKey);
		for(Contactphones contactPhones : contactPhonesList)
			contactPhonesDAO.remove(contactPhones);
	}
	
	private void removeCallLogsRelationships(Long contactKey) throws DAOException, ValidateObjectException
	{
		List<Calllog> callLogList = clDAO.getCallLogListByContact(contactKey);
		for(Calllog cl : callLogList)
		{
			cl.setContactKey(null);
			clDAO.save(cl);
		}
	}
	
	public ReportResult findContacts(Report<ContactsInfo> info) throws DAOException
	{
		Long size = reportContact.getReportCount(info);
		List<Contact> contactsList = reportContact.getReportList(info);
		List<ContactsInfo> contactsInfoList = new ArrayList<ContactsInfo>(contactsList.size());
		for(Contact contact : contactsList)
		{
			ContactsInfo contactInfo = new ContactsInfo(contact);
			if(info.getInfo().getType() == Contact.TYPE_PRIVATE)
			{
				contactInfo.addCategoryInList(categoryDAO.getCategoryKeyAndNameByContactAndUser(contact.getKey(), info.getInfo().getUserKey()));
				contactInfo.addCategoryOutList(categoryDAO.getCategoryKeyAndNameByUser(info.getInfo().getUserKey()));
			}
			contactInfo.addContactphoneList(contactPhonesDAO.getContactphonesListByContact(contact.getKey()));
			contactsInfoList.add(contactInfo);
		}
		return new ReportResult<ContactsInfo>(contactsInfoList, size);
	}

	public ContactsInfo getContactInfo(Long contactKey) throws DAOException
	{
		if(contactKey == null)
			return null;
		Contact contact = contactDAO.getByKey(contactKey);
		if(contact == null)
			return null;
		ContactsInfo contactInfo = new ContactsInfo(contact);
		//contactInfo.addCategoryInList(categoryDAO.getCategoryKeyAndNameByContact(contact.getKey()));
		contactInfo.addContactphoneList(contactPhonesDAO.getContactphonesListByContact(contact.getKey()));
		return contactInfo;
	}
	
	public void saveContact(ContactsInfo contactInfo) throws DAOException, ValidateObjectException
	{
		Contact contact = contactInfo.getContact();
		boolean edit = contactInfo.getKey() != null;
		
		//caso o contato seja privado, valida se ja nao existe este contato para o usuario da operacao
		if(contact.getType() == Contact.TYPE_PRIVATE)
			contactDAO.validateUniquePrivateContact(contact, contactInfo.getUserKey());

		validateSave(contact, contactInfo.getContactphonesList());
		contactDAO.save(contact);
		
		//caso seja novo contato, cria relacionamento com a categoria default do usuario
		if(!edit && contact.getType() == Contact.TYPE_PRIVATE)
			saveContactToDefaultCategory(contact.getKey(), contactInfo.getUserKey());

		saveContactphonesList(contactInfo.getContactphonesList(), contact.getKey(), edit);
	}
	
	private void saveContactToDefaultCategory(Long contactKey, Long contactUserKey) throws DAOException, ValidateObjectException
	{
		Category category = categoryDAO.getDefaultCategoryByUser(contactUserKey);
		if(category != null)
		{
			Categorycontact categoryContact = new Categorycontact();
			categoryContact.setCategoryKey(category.getKey());
			categoryContact.setContactKey(contactKey);
			categoryContactDAO.save(categoryContact);
		}
	}

	private void saveContactphonesList(List<Contactphones> contactPhoneNewList, Long contactKey, boolean edit) throws DAOException, ValidateObjectException
	{
		if(edit)
		{
			List<Contactphones> contactPhoneOldList = contactPhonesDAO.getContactphonesListByContact(contactKey);
			for(Contactphones contactPhone : contactPhoneOldList)
				contactPhonesDAO.remove(contactPhone);
		}
		
		for(Contactphones contactPhone : contactPhoneNewList)
		{
			contactPhone.setKey(null);
			contactPhone.setContactKey(contactKey);
			String number = contactPhone.getPrefix() != null ? contactPhone.getPrefix()+contactPhone.getPhone() : contactPhone.getPhone();
			contactPhone.setPhone(getPSTNNumber(contactPhone.getPhone(), false));			
			contactPhone.setPrefix(getPSTNNumber(number, true));
			contactPhonesDAO.save(contactPhone);
		}
	}
	
	protected void validateSave(Contact contact, List<Contactphones> contactPhoneList) throws DAOException, ValidateObjectException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(contact == null)
			errorList.add(new ValidateError("Contact is null!", Contact.class, null, ValidateType.BLANK));
		else if(contactPhoneList == null || contactPhoneList.size() == 0)
			errorList.add(new ValidateError("Contactphones List is null!", Contactphones.class, null, ValidateType.BLANK));
		else
		{
			Long domainKey = contact.getDomainKey();
			if(domainKey == null)
				errorList.add(new ValidateError("Contact domainKey is null!", Contact.Fields.DOMAIN_KEY.toString(), Contact.class, contact, ValidateType.BLANK));

			if(contact.getName() == null && contact.getKanjiName() == null)
				errorList.add(new ValidateError("Contact names are null!", Contact.Fields.NAME.toString(), Contact.class, contact, ValidateType.BLANK));
			else if(contact.getName().length() == 0 && contact.getKanjiName().length() == 0)
				errorList.add(new ValidateError("Contact names are null!", Contact.Fields.NAME.toString(), Contact.class, contact, ValidateType.BLANK));
		}
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
	}
	
	public void applyCategory(Long userKey, List<Long> contactKeys, Long categoryKey) throws DAOException, ValidateObjectException
	{
		Category category = categoryDAO.getByUserAndKey(userKey, categoryKey);
		if(category != null)
			for(Long contactKey : contactKeys)
			{
				Categorycontact categoryContact = new Categorycontact();
				categoryContact.setCategoryKey(categoryKey);
				categoryContact.setContactKey(contactKey);
				categoryContactDAO.save(categoryContact);
			}
	}
	
	public void unapplyCategories(Long userKey, List<Long> contactKeys) throws DAOException
	{
		List<Categorycontact> categoryContactList = categoryContactDAO.getListWithoutDefaultByContact(userKey, contactKeys);
		for(Categorycontact categoryContact : categoryContactList)
			categoryContactDAO.remove(categoryContact);
	}
}