/*
 * Created on 11.12.2006
 *
 */
package de.jwi.ostendo.testidl;

import org.omg.CORBA.ARG_IN;
import org.omg.CORBA.Any;
import org.omg.CORBA.NVList;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ServerRequest;
import org.omg.PortableServer.POA;

public class DSIImpl extends org.omg.PortableServer.DynamicImplementation
{

	ORB orb;
	
	
	public DSIImpl(ORB orb)
	{
		this.orb = orb;
	}

	public void invoke(ServerRequest request)
	{
		String s = request.operation();
		
		NVList args = orb.create_list(1);
		Any arg = orb.create_any();
		
		org.omg.CORBA.TypeCode tcArray = orb.create_array_tc(
                5, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_octet));
		
		arg.type(tcArray);
		
		args.add_value("", arg, ARG_IN.value);
		
		request.arguments(args);
		
		byte[] b = org.omg.CORBA.OctetSeqHelper.extract(arg);
		
		int x = 7;
	}

	public String[] _all_interfaces(POA poa, byte[] objectId)
	{
		String[] ids = {"IDL:ostendo/test/DataServer:1.0"};
		
		return ids;
	} 

}
