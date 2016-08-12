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

package de.jwi.ostendo.jacorbidl;

import org.jacorb.idl.Spec;


/**
 * @author Juergen Weber 
 * Created on 01.08.2006
 */
public class ParserCaller
{
	static ParserCaller instance = new ParserCaller();
	
	private ParserCaller() {}
	
	public static ParserCaller getInstance()
	{
		return instance;
	}
	
	private Spec theParsedSpec;
	
	public void setParsedSpec(Spec spec)
	{
		theParsedSpec = spec;
	}
	
	
	public Spec loadIDL(String idlname)
	{
		String[] args = {"-backend", IDLTreeVisitorImpl.class.getName(),
				idlname};

		boolean rc = org.jacorb.idl.parser.compileAndHandle(args);
		
		if (!rc)
		{
			return null;
		}
		

		return theParsedSpec;
	}
}
