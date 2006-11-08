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

package de.jwi.ostendo;

import java.math.BigDecimal;

/**
 * @author Juergen Weber 
 * Created on 08.08.2006
 */
public interface Output
{

	void start() throws OutputException;

	void end() throws OutputException;

	void startElement(Element element) throws OutputException;

	void endElement(Element element) throws OutputException;

	void data(String s) throws OutputException;

	void data(short s) throws OutputException;

	void data(int s) throws OutputException;

	void data(boolean b) throws OutputException;

	void data(byte b) throws OutputException;

	void data(char c) throws OutputException;

	void data(double d) throws OutputException;

	void data(float f) throws OutputException;
	
	void data(BigDecimal b) throws OutputException;
	

}