package de.jwi.ostendo.testidl;



public class DefaultPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler
{
	private String[] ids = {"IDL:omg.org/CORBA/Object:1.0"};
	public ostendo.test.DataServer _this()
	{
		return ostendo.test.DataServerHelper.narrow(_this_object());
	}
	public ostendo.test.DataServer _this(org.omg.CORBA.ORB orb)
	{
		return ostendo.test.DataServerHelper.narrow(_this_object(orb));
	}
	public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.OutputStream _out = null;
		return _out;
	}
	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}
