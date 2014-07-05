package br.com.voicetechnology.ng.ipx.rule.call.validationcall;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.DestinationInDNDException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.ForwardAlwaysException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.InvalidDestinationUser;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.MaxConcurrentCallsException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.PermissionDeniedException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Leg;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.Permissions;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;

public class ToLegValidator extends ValidatorBase
{

	public ToLegValidator() throws DAOException
	{
		super();
	}

	public void validate(boolean isFromCallCenter, Leg fromLeg, Leg toLeg, ValidationModes mode) throws ValidationException, DAOException
	{
		if(isTerminalWithoutPbxuser(toLeg))
			throw new PermissionDeniedException("Permission denied to create call, Terminal without User", true, CallStateEvent.PERMISSION_DENIED);		
		
		if(mode.equals(ValidationModes.NO_VALIDATION))
            return;
		
		if(toLeg.hasServiceclass())
		{
			loadReferences(toLeg);
			
			if(!mode.equals(ValidationModes.CALLBACK_MODE) && !mode.equals(ValidationModes.RETURN_CALL_MODE))
				checkBlock(fromLeg, toLeg);
		}
		
		if(toLeg.isPbxuser())
		{
			Pbxuser called = toLeg.getPbxuser();
			
			if(mode.equals(ValidationModes.CONSULTATIVE_TRANSFER_MODE))
				checkBlock(fromLeg, toLeg);
			else
			{
				validatePermission(called, mode);
				validateUser(fromLeg, called);
				validateDND(isFromCallCenter, called, mode);
				
				if(!mode.equals(ValidationModes.RETURN_CALL_MODE))
					checkFilters(fromLeg, toLeg, mode);
				
				validateMaxConcurrentCalls(called, mode);
				validateForwardAlways(isFromCallCenter, called, mode);
			}			
			
			if(toLeg.isSipTrunk())
				checkSipTrunkCalls(toLeg.getSiptrunk(), false);
			
			//inicio --> dnakamashi - bug #6109 - version 3.0.5
			
		}else if(toLeg.isGroup())
		{
			if(checkForward(toLeg.getGroup().getConfigKey(), Forward.ALWAYS_MODE))
				throw new ForwardAlwaysException("Destination in forward always.", CallStateEvent.CALL_FORWARDED);
			//fim --> dnakamashi - bug #6109 - version 3.0.5
		} else if(toLeg.isSipTrunk())
		{
			Pbxuser called = toLeg.getSiptrunk().getPbxuser();
			validatePermission(called, mode);
            validateUser(fromLeg, called);
            checkSipTrunkCalls(toLeg.getSiptrunk(), false);
            validateForwardAlways(isFromCallCenter, called, mode);            
		}

		String did = toLeg.getOriginalSipAddress() != null? toLeg.getOriginalSipAddress().getExtension() : null;
			
		if(fromLeg.isPSTN() && !canDIDReceive(did))
		{
			throw new PermissionDeniedException("Permission denied to receive call: DID is BLOCKED!", false, CallStateEvent.PERMISSION_DENIED);
		}	
	}
	
	private void validatePermission(Pbxuser called, ValidationModes mode) throws PermissionDeniedException, DAOException
	{
		if(!mode.equals(ValidationModes.CALLBACK_MODE) && !checkPermission(called, Permissions.RECEIVE_CALL_PERMISSION))
            throw new PermissionDeniedException("Permission denied to receive call", false, CallStateEvent.PERMISSION_DENIED);
	}
	
	private void validateUser(Leg fromLeg, Pbxuser called) throws InvalidDestinationUser
	{
        if(fromLeg.isPbxuser() && called.getUserKey().intValue() == fromLeg.getPbxuser().getUserKey().intValue())
            throw new InvalidDestinationUser("Origination and destination users are the same.", CallStateEvent.PERMISSION_DENIED);
	}
	
	private void validateDND(boolean isFromCallCenter, Pbxuser called, ValidationModes mode) throws DestinationInDNDException
	{
		if(!mode.equals(ValidationModes.RETURN_CALL_MODE) && called.getConfig().getDndStatus().intValue() == Config.DND_ON 
				&& !isFromCallCenter)
            throw new DestinationInDNDException("Destination in DND", CallStateEvent.DESTINATION_BUSY);
	}
	
	private void validateMaxConcurrentCalls(Pbxuser called, ValidationModes mode) throws MaxConcurrentCallsException, DAOException
	{
		if(!mode.equals(ValidationModes.CALLBACK_MODE) && !mode.equals(ValidationModes.RETURN_CALL_MODE) && !checkMaxConcurrentCalls(called))
            throw new MaxConcurrentCallsException("TO Exceded number of concurrent calls", CallStateEvent.DESTINATION_BUSY);
	}
	
	private void validateForwardAlways(boolean isFromCallCenter, Pbxuser called, ValidationModes mode) throws ForwardAlwaysException, DAOException
	{
		if(!mode.equals(ValidationModes.RETURN_CALL_MODE) && checkForward(called.getConfigKey(), Forward.ALWAYS_MODE)
				&& !isFromCallCenter)
            throw new ForwardAlwaysException("Destination in forward always.", CallStateEvent.CALL_FORWARDED);
	}
}