package br.com.voicetechnology.ng.ipx.rule.call.validationcall;

import static br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes.CALLBACK_MODE;
import static br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes.CONSULTATIVE_TRANSFER_MODE;
import static br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes.FORWARD_MODE;
import static br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes.FULL_MODE;
import static br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes.NO_VALIDATION;
import static br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes.PREPROCESSIVR_TRANSFER_MODE;
import static br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes.RETURN_CALL_MODE;
import static br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes.SPY_MODE;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.UserWithoutSipSessionlogException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.DestinationCallBlockedException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.InvalidDestinationUser;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.MaxConcurrentCallsException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.OriginationCallBlockedException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.PermissionDeniedException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Leg;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.Permissions;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.RouteType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;

public class FromLegValidator extends ValidatorBase
{

	public FromLegValidator() throws DAOException
	{
		super();
	}

	public void validate(RouteInfo routeInfo, Leg fromLeg, Leg toLeg, ValidationModes mode) throws ValidationException, DAOException
	{
		if((isTerminalWithoutPbxuser(fromLeg) && !toLeg.getRouteType().equals(RouteType.COMMAND_CONFIG)) || isMediaLeg(fromLeg))
			throw new PermissionDeniedException("Permission denied to create call, Terminal without User", true, CallStateEvent.PERMISSION_DENIED);		
		
		if(fromLeg.hasServiceclass())
		{
			loadReferences(fromLeg);			
			Serviceclass fromLegServiceClass = null;
						
			if(fromLeg.isGroup())
				fromLegServiceClass = fromLeg.getGroup().getServiceclass();			
			else if(fromLeg.isPbxuser())
				fromLegServiceClass = getPbxuserServiceClass(fromLeg);//Se o Pbxuser usar como DID de saída o DID  do grupo, a ServiceClass do grupo é retornada.
			
			if(!mode.equals(CALLBACK_MODE) && !mode.equals(RETURN_CALL_MODE) && fromLegServiceClass != null)
			{			
				//início --> dnakamashi - bug #6386 - version 3.0.6
		    	if(toLeg.hasServiceclass() && isCallBlocked(toLeg, fromLegServiceClass.getConfig(), Block.TYPE_OUTGOING))
		    	{		    		
		    		boolean isCausedByServiceClass = checkBlockCauseByServiceClass(fromLegServiceClass.getConfig(), Block.TYPE_OUTGOING, fromLegServiceClass.getKey());
					throw new OriginationCallBlockedException("Origination address is blocked to this destination.", CallStateEvent.CALL_BLOCKED, isCausedByServiceClass);    			
		    	}//fim --> dnakamashi - bug #6386 - version 3.0.6				
				else if(checkBlock(fromLegServiceClass.getConfig(), toLeg, Block.TYPE_OUTGOING, fromLeg))
					throw new DestinationCallBlockedException("Destination address blocked to origination call", CallStateEvent.CALL_BLOCKED);
			}			
		}

		if(fromLeg.isPbxuser())
		{
			if(mode.equals(NO_VALIDATION))
				return;

			Pbxuser caller = fromLeg.getPbxuser();

			if(toLeg.isCommandConfig())
			{
				if(!checkPermission(caller, Permissions.CREATE_CALL_PERMISSION))
					throw new PermissionDeniedException("Permission denied to create call", true, CallStateEvent.PERMISSION_DENIED);
				//if(!checkPermission(caller, Permissions.RECEIVE_CALL_PERMISSION))
				//	throw new PermissionDeniedException("Permission denied to receive call", true, CallStateEvent.PERMISSION_DENIED);
				return ;
			}
			
			if(!mode.equals(CONSULTATIVE_TRANSFER_MODE))
			{	
				if(!mode.equals(CALLBACK_MODE) && !mode.equals(FORWARD_MODE) && !mode.equals(PREPROCESSIVR_TRANSFER_MODE) && needsSipSessionValidation(fromLeg) &&!checkSipSessionlog(caller.getKey()))
					throw new UserWithoutSipSessionlogException(fromLeg.getSipAddress().getAddress(), CallStateEvent.UNAVALIABLE);

				if (!checkPermission(caller, Permissions.CREATE_CALL_PERMISSION))
					throw new PermissionDeniedException("Permission denied to create call", true, CallStateEvent.PERMISSION_DENIED);

				if(toLeg.isPbxuser() && caller.getUserKey().intValue() == toLeg.getPbxuser().getUserKey().intValue())
					throw new InvalidDestinationUser("Origination and destination users are the same.", CallStateEvent.PERMISSION_DENIED);
			}		
			//INÍCIO -  vmartinez e jfarah - Correção da Issue #6677 - versão 3.0.5-RC 6.6
			if(mode.equals(FULL_MODE) && needToCheckFromMaxConcurrentCalls(toLeg) && !checkMaxConcurrentCalls(caller))
				throw new MaxConcurrentCallsException("FROM Exceded number of concurrent calls", CallStateEvent.MAX_CONCURRENT_CALLS_EXCEEDED, true);
			//FIM -  vmartinez e jfarah - Correção da Issue #6677 - versão 3.0.5-RC 6.6
			
			if(fromLeg.isSipTrunk())
				checkSipTrunkCalls(fromLeg.getSiptrunk(), true);
			
			if(mode.equals(SPY_MODE))
				checkSpyCall(caller, toLeg.getSipAddress().toString(), toLeg.getUser().getDomainKey());
			
			if(!isValidOwnerIp(routeInfo.getCallInfo().getOwnerIp(), fromLeg))
				throw new PermissionDeniedException("Permission denied to create call", true, CallStateEvent.PERMISSION_DENIED);			
		}
		
		String did = fromLeg.getDisplay();
		
		if(toLeg.isPSTN() && !canDIDMake(did))
			throw new PermissionDeniedException("Permission denied to create call", true, CallStateEvent.PERMISSION_DENIED);

		// Bloqueando chamadas cuja origem e destino são PSTN e a chamada não é um desvio nem transferencia
		if(routeInfo.getPreviousRouteInfo() == null && 
				fromLeg.getRouteType().equals(RouteType.PSTN) && 
				toLeg.getRouteType().equals(RouteType.PSTN) && 
				mode.equals(ValidationModes.FULL_MODE))
			throw new PermissionDeniedException("Permission denied to create call", true, CallStateEvent.PERMISSION_DENIED);
	}
	
	private boolean needToCheckFromMaxConcurrentCalls(Leg toLeg)
	{
		boolean check_max_calls_property = Boolean.parseBoolean(IPXProperties.getProperty(IPXPropertiesType.CHECK_FROM_MAXCONCURRENTCALLS)); 
		return((check_max_calls_property) && (toLeg.getRouteType() == RouteType.ON_NET || toLeg.getRouteType() == RouteType.PSTN)); 
	}
	
	// Agentes de media receptivos nao podem realizar chamadas
	private boolean isMediaLeg(Leg fromLeg)
	{
		return fromLeg.isParkAgent() || fromLeg.isMusicServer() || fromLeg.isVoicemail();
	}
}
