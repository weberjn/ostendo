
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
import java.math.BigDecimal;

import org.omg.CORBA.BooleanHolder;
import org.omg.PortableServer.POA;

import ostendo.MAGICVALUE;
import ostendo.test.DataServer;
import ostendo.test.DataServerHelper;
import ostendo.test.DataServerPOA;
import ostendo.test.InData;
import ostendo.test.Nested;
import ostendo.test.OutData;
import ostendo.test.DataServerPackage.Currency;
import ostendo.test.DataServerPackage.DataException;
import ostendo.test.DataServerPackage.DataServerContainer;
import ostendo.test.DataServerPackage.Fixed52Holder;
import ostendo.test.DataServerPackage.Floats;
import ostendo.test.DataServerPackage.StringContainer;



/**
 * @author Juergen Weber 
 * Created on 28.07.2006
 */
public class DataServerImpl extends DataServerPOA
{
/*	public org.omg.CORBA.portable.OutputStream _invoke(String method,
			org.omg.CORBA.portable.InputStream _input,
			org.omg.CORBA.portable.ResponseHandler handler)
			throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.InputStream iswrapper = new InputStreamWrapper(
				_input, new CDRLogger());
		org.omg.CORBA.portable.ResponseHandler handlerwrapper = new ResponseHandlerWrapper(
				handler, new CDRLogger());
		return super._invoke(method, iswrapper, handlerwrapper);
	}
*/

	POA poa = null;
	
	public DataServerImpl(POA poa)
	{
		this.poa = poa;
	}
	

	
	public OutData getData(InData data, BooleanHolder doRaise, String magic) throws DataException
	{
		OutData d = new OutData("theid", 1967, "String_s", 3.1415f, MAGICVALUE.value);
		if (doRaise.value)
		{
			throw new DataException("Exception requested");
		}
		
		doRaise.value = false;
		
		return d;
	}

	public OutData handleNested(String astring, int along, Nested data, String magic)
	{
		OutData d = new OutData("theid", 1967, "String_s", 3.1415f, MAGICVALUE.value);

		return d;
	}

	public void sendArrays(float[][] theFloats23,
			StringContainer theStringContainer, String magic)
	{
	}

	public void sendSequences(int[] theLongSeq, int[][] theLongLongSeq, String magic)
	{

	}

	public void writeUnions(Floats theFloats,
			ostendo.test.DataServerPackage.Nums[] theNumsSeq, String magic)
	{

	}

	public double changeCurrency(Currency fromCurrency, Currency toCurrency, double amount, String magic)
	{
		System.out.println("from Currency: " + fromCurrency.value() + ":"
				+ fromCurrency.toString());
		
		System.out.println("to Currency: " + toCurrency.value() + ":"
				+ toCurrency.toString());
		
		return amount*2; 

	}
	
	public DataServerContainer getObjectreference()
	{
		DataServer server;
		try
		{
			server = DataServerHelper.narrow(poa.servant_to_reference(this));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage(),e);
		}
		return new DataServerContainer(server, MAGICVALUE.value);
	}
	
	public BigDecimal addFixed(BigDecimal a, BigDecimal b, Fixed52Holder res)
	{
		 BigDecimal c = a.add(b);
		 
		 BigDecimal d = new BigDecimal("248.070");
		 
		 res.value = c;
		 
		 return d;
	}
	
}
