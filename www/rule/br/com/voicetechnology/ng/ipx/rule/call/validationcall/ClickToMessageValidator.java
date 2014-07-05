package br.com.voicetechnology.ng.ipx.rule.call.validationcall;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.DestinationCallBlockedException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.PermissionDeniedException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Leg;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.Permissions;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.RouteType;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;

public class ClickToMessageValidator extends ValidatorBase
{

	public ClickToMessageValidator() throws DAOException
	{
		super();
	}

	public void validate(Pbxuser from, Pbxuser to) throws Exception
	{
		String domain = from.getUser().getDomain().getDomain();
		SipAddressParser sipTo = new SipAddressParser(to.getUser().getUsername(), domain);
		Leg toLeg = new Leg(to, sipTo, sipTo, RouteType.STATION, false, false);
		
		validate(from, toLeg);
	}

	public void validate(Pbxuser from, Group group) throws Exception
	{
		String domain = from.getUser().getDomain().getDomain();
		SipAddressParser sipTo = new SipAddressParser(group.getName(), domain);
		Leg toLeg = new Leg(group, sipTo, sipTo, RouteType.STATION, false);
		
		validate(from, toLeg);
	}
	
	private void validate(Pbxuser from, Leg toLeg) throws Exception
	{
		String domain = from.getUser().getDomain().getDomain();
		SipAddressParser sipFrom = new SipAddressParser(from.getUser().getUsername(), domain);
		Leg fromLeg = new Leg(from, sipFrom, sipFrom, RouteType.STATION, true, false);
		
		validate(fromLeg, toLeg);
	}
	
	public void validate(Leg fromLeg, Leg toLeg) throws Exception
	{
		loadReferences(fromLeg);
		loadReferences(toLeg);

		Pbxuser caller = fromLeg.getPbxuser();

		if(fromLeg.isPbxuser())
			if(checkBlock(caller.getServiceclass().getConfig(), toLeg, Block.TYPE_OUTGOING, fromLeg))
				throw new DestinationCallBlockedException("Destination address blocked to origination call", CallStateEvent.CALL_BLOCKED);

		if(toLeg.isPbxuser())
		{
			if(!checkPermission(toLeg.getPbxuser(), Permissions.RECEIVE_CALL_PERMISSION))
				throw new PermissionDeniedException("Permission denied to receive call", false, CallStateEvent.PERMISSION_DENIED);
			checkBlock(fromLeg, toLeg);
		}
	}
}