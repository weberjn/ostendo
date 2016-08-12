package de.jwi.ostendo.test;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.jacorb.idl.Spec;
import org.omg.CORBA.ORB;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.jwi.ostendo.CDRParser;
import de.jwi.ostendo.Ostendo;
import de.jwi.ostendo.Output;
import de.jwi.ostendo.XMLOutput;
import de.jwi.ostendo.jacorbidl.ParserCaller;

/**
 * @author Juergen Weber 
 * Created on 16.08.2006
 */

public class OstendoTest extends TestCase
{
	Spec theParsedSpec;

	File messageDir;

	String repositoryID;

	org.jacorb.orb.ORB orb;
	
	String idlName = "src/main/idl/server.idl";

	protected void setUp() throws Exception
	{
		super.setUp();

		messageDir = new File("messagelog");

		theParsedSpec = ParserCaller.getInstance().loadIDL(idlName);

		repositoryID = ostendo.test.DataServerHelper.id();

		Properties p = new Properties();
		p.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
		p.setProperty("org.omg.CORBA.ORBSingletonClass",
				"org.jacorb.orb.ORBSingleton");

		orb = (org.jacorb.orb.ORB)ORB.init((String[]) null, p);
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	private byte[] executeTest(String requestName, String replyName)
			throws Exception
	{
		File request = new File(messageDir,
				requestName);
		File reply = new File(messageDir, replyName);
		
		byte[] requestMsg = Ostendo.readMessage(request);
		byte[] replyMsg = Ostendo.readMessage(reply);

		CDRParser c = new CDRParser(orb, theParsedSpec, repositoryID,
				requestMsg, replyMsg);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);

		Output out = new XMLOutput(new PrintWriter(bos), "UTF-8");

		c.parseMessage(out, idlName, request.toString(), reply.toString());

		return bos.toByteArray();
	}

	private void printResult(byte[] b) throws IOException
	{
		String l;

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(b)));
		while ((l = br.readLine()) != null)
		{
			System.out.println(l);
		}
		br.close();
	}

	private Document parseDocument(byte[] b)
			throws ParserConfigurationException, SAXException, IOException
	{
		InputStream is = new ByteArrayInputStream(b);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);

		return doc;
	}

	public void testGetData() throws Exception
	{
		String requestName = "00-getData-Request.bin";
		String replyName = "00-getData-Reply.bin";

		byte[] b = executeTest(requestName, replyName);
		printResult(b);

		InputSource inputSource = new InputSource(new ByteArrayInputStream(b));

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String s = xpath.evaluate(
				"/messages/message[1]/operation/parameter[@name='magic']",
				inputSource);

		assertEquals("WJ", s);

		inputSource = new InputSource(new ByteArrayInputStream(b));
		s = xpath
				.evaluate(
						"/messages/message[2]/operation/result/struct/member[@name='magic']",
						inputSource);

		assertEquals("WJ", s);

	}

	public void testGetDataException() throws Exception
	{
		String requestName = "02-getData-Request.bin";
		String replyName = "02-getData-Reply.bin";

		byte[] b = null;

		b = executeTest(requestName, replyName);
		printResult(b);

		InputSource inputSource = new InputSource(new ByteArrayInputStream(b));

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String s = xpath
				.evaluate(
						"/messages/message[2]/operation/UserException/struct/member[@name='why']",
						inputSource);

		assertEquals("Exception requested", s);


	}

	public void testHandleNested() throws Exception
	{
		String requestName = "04-handleNested-Request.bin";
		String replyName = "04-handleNested-Reply.bin";

		byte[] b = null;

		b = executeTest(requestName, replyName);
		printResult(b);

		InputSource inputSource = new InputSource(new ByteArrayInputStream(b));

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String s = xpath.evaluate(
				"/messages/message[1]/operation/parameter[@name='magic']",
				inputSource);

		assertEquals("WJ", s);

		inputSource = new InputSource(new ByteArrayInputStream(b));
		s = xpath
				.evaluate(
						"/messages/message[2]/operation/result/struct/member[@name='magic']",
						inputSource);

		assertEquals("WJ", s);
	}


	public void testSendArrays() throws Exception
	{
		String requestName = "06-sendArrays-Request.bin";
		String replyName = "06-sendArrays-Reply.bin";

		byte[] b = null;

		b = executeTest(requestName, replyName);
		printResult(b);

		InputSource inputSource = new InputSource(new ByteArrayInputStream(b));

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String s = xpath.evaluate(
				"/messages/message[1]/operation/parameter[1]/array/dimension/dimension[2]/element[3]",
				inputSource);

		assertEquals("2.3", s);

		inputSource = new InputSource(new ByteArrayInputStream(b));
		s = xpath.evaluate(
				"/messages/message[1]/operation/parameter[@name='magic']",
				inputSource);

		assertEquals("WJ", s);
		
		inputSource = new InputSource(new ByteArrayInputStream(b));
		s = xpath
				.evaluate(
						"/messages/message[2]/operation/result/struct/void",
						inputSource);

		assertEquals("", s);

	}


	public void testSendSequences() throws Exception
	{
		String requestName = "08-sendSequences-Request.bin";
		String replyName = "08-sendSequences-Reply.bin";

		byte[] b = null;

		b = executeTest(requestName, replyName);
		printResult(b);
		
		
		InputSource inputSource = new InputSource(new ByteArrayInputStream(b));

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String s = xpath.evaluate(
				"/messages/message[1]/operation/parameter[@name='magic']",
				inputSource);

		assertEquals("WJ", s);

	}


	public void testWriteUnions() throws Exception
	{
		String requestName = "10-writeUnions-Request.bin";
		String replyName = "10-writeUnions-Reply.bin";

		byte[] b = null;

		b = executeTest(requestName, replyName);
		printResult(b);
		
		InputSource inputSource = new InputSource(new ByteArrayInputStream(b));

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String s = xpath.evaluate(
				"/messages/message[1]/operation/parameter[@name='magic']",
				inputSource);

		assertEquals("WJ", s);
	}

	public void testGetObjectreference() throws Exception
	{
		String requestName = "14-getObjectreference-Request.bin";
		String replyName = "14-getObjectreference-Reply.bin";

		byte[] b = null;

		b = executeTest(requestName, replyName);
		printResult(b);
		
		InputSource inputSource = new InputSource(new ByteArrayInputStream(b));

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String s = xpath.evaluate(
				"/messages/message[2]/operation/result/struct/member[@name='magic']",
				inputSource);

		assertEquals("WJ", s);
	}
	
	public void testAddFixed() throws Exception
	{
		String requestName = "18-addFixed-Request.bin";
		String replyName = "18-addFixed-Reply.bin";

		byte[] b = null;

		b = executeTest(requestName, replyName);
		printResult(b);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		InputSource inputSource = new InputSource(new ByteArrayInputStream(b));
		String s = xpath.evaluate(
				"/messages/message[2]/operation/result/parameter[@name='res']/fixed",
				inputSource);
		
		assertEquals("7.90", s);

		inputSource = new InputSource(new ByteArrayInputStream(b));
		s = xpath.evaluate(
				"/messages/message[2]/operation/result/fixed",
				inputSource);
		
		assertEquals("248.070", s);
		
		
		
	}

	
	public static void main(String[] args) throws Exception
	{
		OstendoTest test = new OstendoTest();
		test.setUp();
		test.testAddFixed();
	}
	
}
