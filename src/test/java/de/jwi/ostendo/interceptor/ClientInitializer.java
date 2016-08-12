
package de.jwi.ostendo.interceptor;


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


import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

import de.jwi.ostendo.testidl.Client;

/**
 * @author Juergen Weber 
 * Created on 14.08.2006
 */
public class ClientInitializer extends org.omg.CORBA.LocalObject
		implements
			ORBInitializer
{

	public void post_init(ORBInitInfo info)
	{
		String[] arguments = info.arguments();
		
		String outputdir=".";
		
		for (int i=0;i<arguments.length;i++)
		{
			if (arguments[i].startsWith(Client.OUTPUTDIRPREFIX))
			{
				String[] s = arguments[i].split("=");
				outputdir = s[1];
			}
		}
		
		try
		{
			info.add_client_request_interceptor(new MessageSaveInterceptor(outputdir));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void pre_init(ORBInitInfo info)
	{
	}
} 
