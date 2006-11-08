
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.Properties;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;

import ostendo.MAGICVALUE;
import ostendo.test.BasicTypes;
import ostendo.test.DataServer;
import ostendo.test.DataServerHelper;
import ostendo.test.InData;
import ostendo.test.Nested;
import ostendo.test.OutData;
import ostendo.test.DataServerPackage.Currency;
import ostendo.test.DataServerPackage.DataException;
import ostendo.test.DataServerPackage.DataServerContainer;
import ostendo.test.DataServerPackage.FixedTHolder;
import ostendo.test.DataServerPackage.Floats;
import ostendo.test.DataServerPackage.Nums;
import ostendo.test.DataServerPackage.StringContainer;
import de.jwi.ostendo.interceptor.ClientInitializer;

/**
 * @author Juergen Weber 
 * Created on 28.07.2006
 */
public class Client
{
	public static final String OUTPUTDIRPREFIX="outputdir";
	
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: jaco demo.getdata.Client <ior_file>");
			System.exit(1);
		}

		try
		{
			File f = new File(args[0]);
			String logFolder = System.getProperty("logfolder", "messagelog");

			Properties props = new Properties();
            props.put
                ("org.omg.PortableInterceptor.ORBInitializerClass.ForwardInit",
                		ClientInitializer.class.getName());
            
            String[] orbargs = new String[args.length+1];
            System.arraycopy(args, 0, orbargs, 0, args.length);
            orbargs[orbargs.length-1]=OUTPUTDIRPREFIX+"=" + logFolder;
			
			ORB orb = ORB.init(orbargs, props);

			BufferedReader br = new BufferedReader(new FileReader(f));

			org.omg.CORBA.Object obj = orb.string_to_object(br.readLine());

			br.close();

			DataServer server = DataServerHelper.narrow(obj);

			getData(server);
			
			handleNested(server);
			
			sendArrays(server);

			sendSequences(server);
			
			writeUnions(server);

			sendEnum(server);

			getObjectreference(server);
			
			addFixed(server);
			
			System.out.println("done");

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private static void getData(DataServer server)
	{
		InData data = new InData("my_id", "my_String s");
		try
		{
			OutData outData = server.getData(data,new BooleanHolder(false), MAGICVALUE.value);
			
			server.getData(data,new BooleanHolder(true), MAGICVALUE.value);
		}
		catch (DataException e)
		{
			System.out.println(e.getClass().getName()+":"+e.why);
		}
	}
	
	private static void handleNested(DataServer server)
	{
	BasicTypes b = new BasicTypes(3.14f, 6.28, (short) 1967, 19671967,
			'X', true, (byte) 0x67);
	
	Nested nested = new Nested((short) 5, b);

	OutData outdata = server.handleNested("hello", (short) 29, nested, MAGICVALUE.value);

	}
	
	private static void sendArrays(DataServer server)
	{
		String[] theStrings = {"hello", "world"};

		float[][] floats = {{1.1f, 1.2f, 1.3f}, {2.1f, 2.2f, 2.3f}};

		StringContainer sc = new StringContainer(theStrings);

		server.sendArrays(floats, sc, MAGICVALUE.value);
	}

	private static void sendSequences(DataServer server)
	{
		int[] theLongSeq = {-1, -2, -3};

		int[][] theLongLongSeq = {{11, 12, 13}, {21, 22, 23}, {31, 32, 33}};

		server.sendSequences(theLongSeq, theLongLongSeq, MAGICVALUE.value);
	}
	
	private static void writeUnions(DataServer server)
	{
		Floats floats = new Floats();
		floats.id(new InData("eins","zwei"));
		
		
		Nums[] nums = new Nums[2];
		
		nums[0] = new Nums();
		nums[1] = new Nums();
		
		nums[0].f(3.14f);
		nums[1].l(2006);
		
		server.writeUnions(floats, nums, MAGICVALUE.value);
	}
	
	private static void sendEnum(DataServer server)
	{
		Currency fromCurrency = Currency.from_int(Currency._pound);
		Currency toCurrency = Currency.from_int(Currency._mark);
		
		double result = 
			server.changeCurrency(fromCurrency, toCurrency, 30000.0, MAGICVALUE.value);
	}
	
	private static void getObjectreference(DataServer server)
	{
		DataServerContainer container = server.getObjectreference();
		sendEnum(container.theDataServer);
	}
	
	private static void addFixed(DataServer server)
	{
		BigDecimal a = new BigDecimal(3.5).setScale(2);
		BigDecimal b = new BigDecimal("4.4").setScale(2);
		FixedTHolder res = new FixedTHolder();
		
		server.addFixed(a, b, res);
		
		BigDecimal c = res.value;
		
	}
	
}
