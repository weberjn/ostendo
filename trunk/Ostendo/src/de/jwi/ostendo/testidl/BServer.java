
package de.jwi.ostendo.testidl;

/*
 * Ostendo - CORBA IIOP Message Analyzer
 * 
 * Copyright (C) 2006 Juergen Weber
 * 
 * This file is part of Ostendo.
 * 
 * Ostendo is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * Ostendo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with Ostendo; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston
 */


import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;


/**
 * @author Juergen Weber 
 * Created on 28.07.2006
 */
public class BServer
{
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: jaco demo.getdata.Server <ior_file>");
			System.exit(1);
		}

		try
		{
			Properties p = new Properties();
			
//	        p.put
//	            ("org.omg.PortableInterceptor.ORBInitializerClass.ForwardInit",
//	            		ServerInitializer.class.getName());
				
				
			p.setProperty("org.omg.CORBA.ORBClass","org.jacorb.orb.ORB");
			p.setProperty("org.omg.CORBA.ORBSingletonClass","org.jacorb.orb.ORBSingleton");
			p.setProperty("jacorb.verbosity","4");
			p.setProperty("jacorb.log.default.verbosity","4");
			
			
			ORB orb = ORB.init(args, p);

			
			POA poa = POAHelper.narrow(orb
					.resolve_initial_references("RootPOA"));

			poa.the_POAManager().activate();

//			DataServerImpl server = new DataServerImpl(poa);
			
			org.omg.CORBA.Object newo = poa.create_reference("IDL:ostendo/test/DataServer:1.0");

			Servant server = new DefaultPOA();
			
			if (true) {
			org.omg.CORBA.Object obj = poa.servant_to_reference(server);

			PrintWriter pw = new PrintWriter(new FileWriter(args[0]));

			pw.println(orb.object_to_string(obj));

			pw.flush();
			pw.close();
			}
			orb.run();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
