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

import java.io.Writer;
import java.math.BigDecimal;
import java.util.Stack;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;


/**
 * @author Juergen Weber 
 * Created on 07.08.2006
 */
public class XMLOutput implements Output
{
	TransformerHandler transformerHandler;
	Stack openElements;

	public XMLOutput(Writer out) throws OutputException
	{
		try
		{
			SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
					.newInstance();

			//		   SAX2.0 ContentHandler.
			transformerHandler = tf.newTransformerHandler();
			Transformer serializer = transformerHandler.getTransformer();

			serializer.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult streamResult = new StreamResult(out);

			transformerHandler.setResult(streamResult);
			
			openElements = new Stack();
		}
		catch (Exception e)
		{
			throw new OutputException(e);
		}

	}

	public void start() throws OutputException
	{
		try
		{
			transformerHandler.startDocument();
		}
		catch (Exception e)
		{
			throw new OutputException(e);
		}
	}

	public void end() throws OutputException
	{
		try
		{
			transformerHandler.endDocument();
		}
		catch (Exception e)
		{
			throw new OutputException(e);
		}
	}

	public void popElements(Element lastToPop) throws OutputException
	{
		while (  !openElements.empty())
		{
			if (openElements.peek() == lastToPop)
			{
				break;
			}
			Element e = (Element)openElements.pop();
			endElement(e, false);
		}
	}
	
	public void startElement(Element element) throws OutputException
	{

		try
		{
			transformerHandler.startElement("", "", element.getName(), element
					.getAttributes());
			
			openElements.push(element);
		}
		catch (Exception e)
		{
			throw new OutputException(e);
		}
	}

	public void endElement(Element element) throws OutputException
	{
		endElement(element, true);
	}
	
	private void endElement(Element element, boolean pop) throws OutputException
	{

		try
		{
			transformerHandler.endElement("", "", element.getName());
			
			if (pop)
			{
				openElements.pop();
			}
		}
		catch (Exception e)
		{
			throw new OutputException(e);
		}
	}
	
	
	public void data(String s) throws OutputException
	{
		char ch[] = s.toCharArray();
		
		try
		{
			transformerHandler.characters(ch, 0, ch.length);
		}
		catch (Exception e)
		{
			throw new OutputException(e);
		}
	}
	
	public void data(short s) throws OutputException
	{
		data(Short.toString(s));
	}
	
	public void data(int s) throws OutputException
	{
		data(Integer.toString(s));
	}
	
	public void data(boolean b) throws OutputException
	{
		data(Boolean.toString(b));
	}
	
	public void data(byte b) throws OutputException
	{
		data(Byte.toString(b));
	}
	
	public void data(char c) throws OutputException
	{
		data(Character.toString(c));
	}

	public void data(double d) throws OutputException
	{
		data(Double.toString(d));
	}

	public void data(float f) throws OutputException
	{
		data(Float.toString(f));
	}

	public void data(BigDecimal b) throws OutputException
	{
		data(b.toString());
	}

	
	

}
