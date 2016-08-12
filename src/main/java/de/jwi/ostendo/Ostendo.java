/*
 * Ostendo - CORBA IIOP Message Analyzer Copyright (C) 2006 Juergen Weber This file is part of Ostendo.
 * Ostendo is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version. Ostendo is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General Public
 * License along with Ostendo; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite
 * 330, Boston
 */

package de.jwi.ostendo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.jacorb.idl.Spec;

import de.jwi.ostendo.jacorbidl.ParserCaller;

/**
 * @author Juergen Weber 
 * Created on 28.07.2006
 */
public class Ostendo
{
	public static final String ENCODING = "UTF-8";

	public static byte[] readMessage(String n) throws IOException
	{
		File f = new File(n);
		return readMessage(f);
	}


	public static byte[] readMessage(File f) throws IOException
	{
		FileInputStream fin = new FileInputStream(f);
		long l = fin.available();

		byte[] b = new byte[(int) l];

		fin.read(b);

		fin.close();

		return b;
	}


	public static String readIOR(String n) throws IOException
	{
		String s = null;

		FileInputStream fin = null;
		try
		{
			fin = new FileInputStream(n);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));

			s = br.readLine();
		}
		finally
		{
			if (fin != null)
				fin.close();
		}

		return s;
	}


	public static void usage()
	{
		System.err.println("usage:");
		System.err.println(Ostendo.class.getName()
			+ "[-o outfile] <typeId> <IDL> <requestmessage> [<replymessage>]");
	}


	public static void main(String[] args) throws Exception
	{
		if (args.length < 3)
		{
			usage();
			System.exit(1);
		}

		Spec theParsedSpec = null;
		
		int n = 0;
		
		String outfile = null;
		
		if ("-o".equals(args[n]))
		{
			outfile = args[++n];
			n++;
		}
		
		String typeId = args[n];
		String idlName = args[n+1];
		String requestmessage = args[n+2];
		String replymessage = null;

		if ("-unknown".equals(args[n]))
		{
			requestmessage = args[n+1];
			idlName = null;
			typeId = null;
		}
		else
		if (args.length > n+3)
		{
			replymessage = args[n+3];
		}
		
		if (idlName != null)
		{
			theParsedSpec = ParserCaller.getInstance().loadIDL(idlName);

			if (theParsedSpec == null)
			{
				throw new RuntimeException("could not parse IDL " + idlName);
			}
		}

		String iorOrInterface = null;
		
		PrintStream ps = System.out; 
		
		if (outfile != null)
		{
			ps = new PrintStream(new FileOutputStream(outfile));
		}
		
		Output out = new XMLOutput(new PrintWriter(new OutputStreamWriter(ps,ENCODING)), ENCODING);

		byte[] messageReq = readMessage(requestmessage);

		byte[] messageRepl = null;

		if (replymessage != null)
		{
			messageRepl = readMessage(replymessage);
		}

		CDRParser c = new CDRParser(theParsedSpec, typeId, messageReq, messageRepl);

		c.parseMessage(out, idlName, requestmessage, replymessage);

		if (outfile != null)
		{
			ps.close();
		}
	}

}
