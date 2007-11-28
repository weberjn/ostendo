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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Properties;

import org.jacorb.idl.AliasTypeSpec;
import org.jacorb.idl.ArrayTypeSpec;
import org.jacorb.idl.BaseType;
import org.jacorb.idl.BooleanType;
import org.jacorb.idl.Case;
import org.jacorb.idl.CharType;
import org.jacorb.idl.ConstrTypeSpec;
import org.jacorb.idl.Declaration;
import org.jacorb.idl.Declarator;
import org.jacorb.idl.Definition;
import org.jacorb.idl.DoubleType;
import org.jacorb.idl.EnumType;
import org.jacorb.idl.FixedPointType;
import org.jacorb.idl.FloatType;
import org.jacorb.idl.IdlSymbol;
import org.jacorb.idl.Interface;
import org.jacorb.idl.InterfaceBody;
import org.jacorb.idl.LongLongType;
import org.jacorb.idl.LongType;
import org.jacorb.idl.Member;
import org.jacorb.idl.Module;
import org.jacorb.idl.OctetType;
import org.jacorb.idl.OpDecl;
import org.jacorb.idl.Operation;
import org.jacorb.idl.ParamDecl;
import org.jacorb.idl.RaisesExpr;
import org.jacorb.idl.ScopedName;
import org.jacorb.idl.SequenceType;
import org.jacorb.idl.ShortType;
import org.jacorb.idl.Spec;
import org.jacorb.idl.StringType;
import org.jacorb.idl.StructType;
import org.jacorb.idl.SwitchBody;
import org.jacorb.idl.TypeSpec;
import org.jacorb.idl.UnionType;
import org.jacorb.idl.VoidTypeSpec;
import org.jacorb.orb.ORB;
import org.jacorb.orb.Reference;
import org.jacorb.orb.giop.MessageInputStream;
import org.jacorb.orb.giop.Messages;
import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.orb.giop.RequestInputStream;
import org.omg.GIOP.MsgType_1_1;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.IOP.ServiceContext;

/**
 * @author Juergen Weber 
 * Created on 28.07.2006
 */
public class CDRParser
{

	private RequestInputStream misRequest;

	private ReplyInputStream misReply;

	private MessageInputStream currentMessageInputStream;

	private Output out;

	private Spec theParsedSpec;

	private String typeId;

	private byte[] requestMessage, replyMessage;

	private Object switch_type_encountered;

	private ORB orb;

	private static String version = "";

	static
	{
		InputStream is = CDRParser.class
				.getResourceAsStream("/version.properties");
		if (is != null)
		{
			Properties p = new Properties();
			try
			{
				p.load(is);
				version = p.getProperty("ostendo.version");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}
	}

	public CDRParser(ORB orb, Spec theParsedSpec, String typeId,
			byte[] requestMessage, byte[] replyMessage)
	{
		this.orb = orb;
		this.theParsedSpec = theParsedSpec;
		this.typeId = typeId;
		this.requestMessage = requestMessage;
		this.replyMessage = replyMessage;
	}

	public CDRParser(Spec theParsedSpec, String ior, byte[] requestMessage,
			byte[] replyMessage)
	{
		this(null, theParsedSpec, ior, requestMessage, replyMessage);
	}

	public void parseMessage(Output out) throws OutputException
	{
		Element element = null;

		int _GIOPMajor;
		int _GIOPMinor;
		int requestId;
		int msgSize;
		int msgType;
		String msgTypeS;


		this.out = out;

		if (orb == null)
		{
			Properties p = new Properties();
			p.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
			p.setProperty("org.omg.CORBA.ORBSingletonClass",
					"org.jacorb.orb.ORBSingleton");

			orb = (org.jacorb.orb.ORB) ORB.init((String[]) null, p);
		}

		Interface theInterface = findInterfaceByTypeID(theParsedSpec, typeId);
		if (theInterface == null)
		{
			throw new RuntimeException("No interface found for " + typeId
					+ " in parsed IDL.");
		}

		_GIOPMajor = Messages.getGIOPMajor(requestMessage);
		_GIOPMinor = Messages.getGIOPMinor(requestMessage);
		requestId = Messages.getRequestId(requestMessage);
		msgSize = Messages.getMsgSize(requestMessage);
		msgType = Messages.getMsgType(requestMessage);

		msgTypeS = MsgType_1_1.from_int(msgType).toString();

		/*
		 * see org.jacorb.orb.giop.ServerRequestListener.requestReceived()
		 */


		misRequest = new RequestInputStream(orb, requestMessage);

		currentMessageInputStream = misRequest;

		int messageLen = misRequest.msg_size;

		String operationName = misRequest.req_hdr.operation;

		Operation theOperation = findOperation(theInterface, operationName);
		out.start();
		Element messages = new Element("messages").att("Ostendo", version);
		out.startElement(messages);

		int pos = currentMessageInputStream.get_pos();
		
		element = new Element("message").att("position", Integer.toHexString(pos)).att("interface", typeId);
		element.att("msgType", msgTypeS).att("msgSize",
				Integer.toString(msgSize));
		element.att("GIOP", Integer.toString(_GIOPMajor) + "."
				+ Integer.toString(_GIOPMinor));


		out.startElement(element);

		listServiceContexts(misRequest.req_hdr.service_context);

		listOperationRequest(theOperation);

		out.endElement(element);


		if (replyMessage != null)
		{
			// TODO checks for reqID and Object equality

			misReply = new ReplyInputStream(orb, replyMessage);
			
			currentMessageInputStream = misReply;
			
			_GIOPMajor = Messages.getGIOPMajor(replyMessage);
			_GIOPMinor = Messages.getGIOPMinor(replyMessage);
			requestId = Messages.getRequestId(replyMessage);
			msgSize = Messages.getMsgSize(replyMessage);
			msgType = Messages.getMsgType(replyMessage);

			msgTypeS = MsgType_1_1.from_int(msgType).toString();

			ReplyStatusType_1_2 replyStatus = misReply.rep_hdr.reply_status;
			String status = replyStatus.toString();

			element = new Element("message");
			element.att("GIOPMajor", Integer.toString(_GIOPMajor)).att(
					"GIOPMinor", Integer.toString(_GIOPMinor));
			element.att("msgType", msgTypeS).att("msgSize",
					Integer.toString(msgSize)).att("status", status);

			element.att("interface", typeId);
			out.startElement(element);

			listServiceContexts(misReply.rep_hdr.service_context);

			listOperationReply(theOperation, replyStatus);

			out.endElement(element);


		}
		out.endElement(messages);
		out.end();
	}

	private void listServiceContexts(
			org.omg.IOP.ServiceContext[] service_context)
			throws OutputException
	{
		int pos = currentMessageInputStream.get_pos();
		
		Element serviceContexts = new Element("servicecontexts").att("position", Integer.toHexString(pos));
		Element context;

		out.startElement(serviceContexts);

		for (int i = 0; i < service_context.length; i++)
		{
			ServiceContext sc = service_context[i];


			context = new Element("context");
			context.att("id", Integer.toString(sc.context_id));
			out.startElement(context);

			String s = toString(sc.context_data);
			out.data(s);

			out.endElement(context);
		}

		out.endElement(serviceContexts);
	}

	private void listOperationRequest(Operation operation)
			throws OutputException
	{
		OpDecl opDecl = (OpDecl) operation;
		String name = opDecl.name();

		int pos = currentMessageInputStream.get_pos();
		
		Element element = new Element("operation").att("position", Integer.toHexString(pos));
		element.att("name", name);
		out.startElement(element);

		try
		{
			for (Enumeration e = opDecl.paramDecls.elements(); e
					.hasMoreElements();)
			{
				ParamDecl p = ((ParamDecl) e.nextElement());

				if (p.paramAttribute != ParamDecl.MODE_OUT)
				{
					listParameter(p);
				}
			}
		}
		catch (RuntimeException e)
		{
			outputParseException(e);
			out.popElements(element);
		}

		out.endElement(element);
	}

	private void outputParseException(RuntimeException e) throws OutputException
	{
		Element broken = new Element("BrokenMessage");
		out.startElement(broken);

		StringWriter sw = new StringWriter();

		e.printStackTrace(new PrintWriter(sw));
		String s = sw.toString();

		out.data(s);
		
		out.endElement(broken);
	}
	
	private void listOperationReply(Operation operation,
			ReplyStatusType_1_2 replyStatus) throws OutputException
	{
		/*
		 "If the reply_status value is NO_EXCEPTION, the body is encoded as if it were
		 a structure holding first any operation return value, then any inout and out
		 parameters in the order in which they appear in the operation�s OMG IDL
		 definition, from left to right." (15.4.3)
		 */

		OpDecl opDecl = (OpDecl) operation;
		String name = opDecl.name();

		int pos = currentMessageInputStream.get_pos();
		
		Element element = new Element("operation").att("position", Integer.toHexString(pos));
		element.att("name", name);
		out.startElement(element);


		try
		{
			if (replyStatus == ReplyStatusType_1_2.NO_EXCEPTION)
			{
				String type = opDecl.opTypeSpec.getIDLTypeName();

				pos = currentMessageInputStream.get_pos();
				
				Element result = new Element("result").att("position", Integer.toHexString(pos)).att("type", type);
				out.startElement(result);

				listType(opDecl.opTypeSpec);

				for (Enumeration e = opDecl.paramDecls.elements(); e
						.hasMoreElements();)
				{
					ParamDecl p = ((ParamDecl) e.nextElement());

					if (p.paramAttribute != ParamDecl.MODE_IN)
					{
						listParameter(p);
					}
				}

				out.endElement(result);
			}
			else
			{
				if (replyStatus == ReplyStatusType_1_2.SYSTEM_EXCEPTION)
				{
					listSystemException();
				}
				else if (replyStatus == ReplyStatusType_1_2.USER_EXCEPTION)
				{
					listUserException(opDecl.raisesExpr);
				}

			}
		}
		catch (RuntimeException e)
		{
			outputParseException(e);
			out.popElements(element);
		}
		out.endElement(element);
	}

	String[] completionStatusStrings = {"COMPLETED_YES", "COMPLETED_NO",
			"COMPLETED_MAYBE"};

	private void listSystemException() throws OutputException
	{
		org.omg.CORBA.SystemException systemException = org.jacorb.orb.SystemExceptionHelper
				.read(currentMessageInputStream);

		int pos = currentMessageInputStream.get_pos();
		
		Element element = new Element("SystemException").att("position", Integer.toHexString(pos));
		out.startElement(element);

		Element content;

		content = new Element("class");
		out.startElement(content);
		out.data(systemException.getClass().getName());
		out.endElement(content);

		content = new Element("minor");
		out.startElement(content);
		out.data(systemException.minor);
		out.endElement(content);

		content = new Element("completionstatus");
		out.startElement(content);
		out.data(completionStatusStrings[systemException.completed.value()]);
		out.endElement(content);

		out.data(systemException.getMessage());

		out.endElement(element);
	}

	private void listUserException(RaisesExpr raisesExpr)
			throws OutputException
	{
		int i;

		String[] exIDs = raisesExpr.getExceptionIds();

		String repositoryId = null;
		
		int pos = currentMessageInputStream.get_pos();		

		repositoryId = currentMessageInputStream.read_string();

		for (i = 0; i < exIDs.length; i++)
		{
			if (repositoryId.equals(exIDs[i]))
			{
				break;
			}
		}

		ScopedName sn = (ScopedName) raisesExpr.nameList.elementAt(i);

		TypeSpec t = sn.resolvedTypeSpec();

		Element element = new Element("UserException").att("position", Integer.toHexString(pos));
		element.att("repositoryId", repositoryId);
		out.startElement(element);

		listType(t);

		out.endElement(element);
	}

	private void listParameter(ParamDecl p) throws OutputException
	{
		String paramAttr = null;

		switch (p.paramAttribute)
		{
			case ParamDecl.MODE_OUT:
				paramAttr = "out";
				break;
			case ParamDecl.MODE_IN:
				paramAttr = "in";
				break;
			case ParamDecl.MODE_INOUT:
				paramAttr = "inout";
				break;
		}


		String pname = p.simple_declarator.name();
		String ptype = p.paramTypeSpec.getIDLTypeName();
		
		int pos = currentMessageInputStream.get_pos();

		Element element = new Element("parameter").att("position", Integer.toHexString(pos)).att("name", pname).att(
				"attribute", paramAttr).att("type", ptype);
		out.startElement(element);

		listType(p.paramTypeSpec);

		out.endElement(element);

	}


	private void listType(ConstrTypeSpec constrTypeSpec) throws OutputException
	{
		if (constrTypeSpec.c_type_spec instanceof StructType)
		{
			listType((StructType) constrTypeSpec.c_type_spec);
		}
		else if (constrTypeSpec.c_type_spec instanceof UnionType)
		{
			listType((UnionType) constrTypeSpec.c_type_spec);
		}
		else if (constrTypeSpec.c_type_spec instanceof EnumType)
		{
			listType((EnumType) constrTypeSpec.c_type_spec);
		}
		else if (constrTypeSpec.c_type_spec instanceof Interface)
		{
			listType((Interface) constrTypeSpec.c_type_spec);
		}
		else
		{
			throw new RuntimeException("unknown constrTypeSpec");
		}
	}


	private void listType(FixedPointType fixedPointType) throws OutputException
	{
		int scale = fixedPointType.scale;
		int digits = fixedPointType.digits;
		BigDecimal fixed = currentMessageInputStream.read_fixed();
		fixed = fixed.movePointLeft(2);
		Element element = new Element("fixed").att("digits",
				Integer.toString(digits)).att("scale", Integer.toString(scale));
		out.startElement(element);
		out.data(fixed);
		out.endElement(element);
	}

	private void listType(SequenceType sequenceType) throws OutputException
	{
		int i;

		int sequenceLength = currentMessageInputStream.read_ulong();

		String elementType = sequenceType.elementTypeSpec().getIDLTypeName();

		int pos = currentMessageInputStream.get_pos();
		
		Element sequence = new Element("sequence").att("position", Integer.toHexString(pos)).att("length",
				Integer.toString(sequenceLength)).att("elementtype",
				elementType);
		out.startElement(sequence);

		for (i = 0; i < sequenceLength; i++)
		{
			Element element = new Element("element");
			out.startElement(element);

			listType(sequenceType.elementTypeSpec());

			out.endElement(element);
		}

		out.endElement(sequence);
	}

	private void listType(ArrayTypeSpec arrayTypeSpec) throws OutputException
	{
		int i;

		int[] dims = arrayTypeSpec.dims;
		
		int pos = currentMessageInputStream.get_pos();	

		Element array = new Element("array").att("position", Integer.toHexString(pos));
		out.startElement(array);
		
		ArrayTypeSpec t = arrayTypeSpec;
		for (i = 0; i < dims.length - 1; i++)
		{
			t = (ArrayTypeSpec) t.elementTypeSpec();
		}
		TypeSpec elementTypeSpec = t.elementTypeSpec();

		walkArray(dims, 0, elementTypeSpec);
		
		out.endElement(array);
	}

	private void walkArray(int[] dims, int k, TypeSpec t)
			throws OutputException
	{
		int i;

		Element dimension = new Element("dimension");
		out.startElement(dimension);


		for (i = 0; i < dims[k]; i++)
		{
			if (k < dims.length - 1)
			{
				walkArray(dims, k + 1, t);
			}
			else
			{
				Element element = new Element("element");
				out.startElement(element);
				listType(t);
				out.endElement(element);
			}
		}

		out.endElement(dimension);
	}


	private void listType(StructType structType) throws OutputException
	{
		// @see org.jacorb.idl.StructType.printHelperClass()

		int pos = currentMessageInputStream.get_pos();
		
		Element element = new Element("struct").att("position", Integer.toHexString(pos));
		out.startElement(element);


		for (Enumeration e = structType.memberlist.elements(); e
				.hasMoreElements();)
		{
			Member m = (Member) e.nextElement();
			Declarator dm = m.declarator;

			String name = dm.name();
			String type = m.type_spec.getIDLTypeName(); 

			pos = currentMessageInputStream.get_pos();

			Element memberelement = new Element("member").att("position", Integer.toHexString(pos)).att("name", name)
					.att("type", type);
			out.startElement(memberelement);

			listType(m.type_spec);

			out.endElement(memberelement);
		}

		out.endElement(element);

	}

	private void listType(UnionType unionType) throws OutputException
	{
		//		 @see org.jacorb.idl.UnionType.printHelperClass()

		/*
		15.3.2.3 Union
		Unions are encoded as the discriminant tag of the type specified in the union
		declaration, followed by the representation of any selected member, encoded as its type
		indicates.
		 */

		Enumeration en;

		TypeSpec switch_type_spec = unionType.switch_type_spec.type_spec;

		TypeSpec switch_ts_resolved = switch_type_spec;

		if (switch_type_spec.type_spec instanceof ScopedName)
		{
			switch_ts_resolved = ((ScopedName) switch_type_spec.type_spec)
					.resolvedTypeSpec();
		}

		String unionTypeName = unionType.name();

		String switchTypeName = switch_ts_resolved.toString();

		int pos = currentMessageInputStream.get_pos();
		
		Element unionElement = new Element("union").att("position", Integer.toHexString(pos)).att("type", unionTypeName);
		out.startElement(unionElement);

		Element switchElement = new Element("switch").att("type",
				switchTypeName);
		out.startElement(switchElement);

		listType(switch_type_spec);

		out.endElement(switchElement);

		String disc = switch_type_encountered.toString();


		//.att(				"value", disc);

		String discType = switch_type_spec.getIDLTypeName();


		SwitchBody switchBody = unionType.switch_body;

		Case c = null, actualCase = null, defaultCase = null;
		for (Enumeration e = switchBody.caseListVector.elements(); e
				.hasMoreElements();)
		{
			c = (Case) e.nextElement();

			for (en = c.case_label_list.elements(); en.hasMoreElements();)
			{
				Object o = en.nextElement();
				if (o == null)
				{
					defaultCase = c;
					break;
				}
				else
				{
					String s = o.toString();

					if ((s.length() > 1) && s.startsWith("'")
							&& s.endsWith("'"))
					{
						// JacORB IDL returns char discriminats as 'X' -> X
						s = s.substring(1, s.length() - 1);
					}

					if (disc.equals(s))
					{
						actualCase = c;
						break;
					}

				}
			}
		}

		if (actualCase == null)
		{
			if (defaultCase != null)
			{
				actualCase = defaultCase;
			}
			else
			{
				// boolean false
				actualCase = null;
			}
		}

		if (actualCase != null)
		{
			// I can't believe this
			TypeSpec t = actualCase.element_spec.typeSpec;

			//	lastInteger

			String bodyType = t.getIDLTypeName();

			//			unionElement.att("bodyType", bodyType);

			Element bodyElement = new Element("body").att("type", bodyType);
			out.startElement(bodyElement);


			listType(t);

			out.endElement(bodyElement);

		}


		out.endElement(unionElement);
	}

	private void listType(EnumType enumType) throws OutputException
	{
		int value = currentMessageInputStream.read_long();

		//		String idlType = enumType.getIDLTypeName();
		String idlType = enumType.typeName();
		String label = null;
		int n = 0;

		for (Enumeration e = enumType.enumlist.elements(); e.hasMoreElements();)
		{
			String l = (String) e.nextElement();
			if (n == value)
			{
				label = l;
				break;
			}
			n++;
		}

		Element element = new Element("enum").att("type", idlType);
		out.startElement(element);
		out.data(label);
		out.endElement(element);

	}

	private void listType(Interface theInterface) throws OutputException
	{
		Element id;
		Element element = new Element("object");
		out.startElement(element);
		org.omg.CORBA.Object o = currentMessageInputStream.read_Object();

		Reference reference = (Reference) o;

		String[] ids = reference._ids();

		for (int i = 0; i < ids.length; i++)
		{
			id = new Element("id");
			out.startElement(id);
			out.data(ids[i]);
			out.endElement(id);
		}

		out.endElement(element);
	}


	private void listType(VoidTypeSpec voidTypeSpec) throws OutputException
	{
		Element element = new Element("void");
		out.startElement(element);
		out.endElement(element);
	}


	private void listType(TypeSpec typeSpec) throws OutputException
	{
		if (typeSpec instanceof StringType)
		{
			listType((StringType) typeSpec);
		}
		else if (typeSpec instanceof ShortType)
		{
			listType((ShortType) typeSpec);
		}
		else if (typeSpec instanceof LongType)
		{
			listType((LongType) typeSpec);
		}
		else if (typeSpec instanceof LongLongType)
		{
			listType((LongLongType) typeSpec);
		}
		else if (typeSpec instanceof BooleanType)
		{
			listType((BooleanType) typeSpec);
		}
		else if (typeSpec instanceof CharType)
		{
			listType((CharType) typeSpec);
		}
		else if (typeSpec instanceof OctetType)
		{
			listType((OctetType) typeSpec);
		}
		else if (typeSpec instanceof FloatType)
		{
			listType((FloatType) typeSpec);
		}
		else if (typeSpec instanceof DoubleType)
		{
			listType((DoubleType) typeSpec);
		}
		else if (typeSpec instanceof AliasTypeSpec)
		{
			TypeSpec t = ((AliasTypeSpec) typeSpec).originalType;
			listType(t);
		}
		else if (typeSpec instanceof ConstrTypeSpec)
		{
			listType((ConstrTypeSpec) typeSpec);
		}
		else if (typeSpec instanceof BaseType)
		{
			listType((BaseType) typeSpec);
		}
		else if (typeSpec instanceof ArrayTypeSpec)
		{
			listType((ArrayTypeSpec) typeSpec);
		}
		else if (typeSpec instanceof SequenceType)
		{
			listType((SequenceType) typeSpec);
		}
		else if (typeSpec instanceof FixedPointType)
		{
			listType((FixedPointType) typeSpec);
		}
		else if (typeSpec instanceof VoidTypeSpec)
		{
			listType((VoidTypeSpec) typeSpec);
		}
		else
		{
			listType(typeSpec.type_spec);
		}

	}


	private void listType(StringType type) throws OutputException
	{
		String value = currentMessageInputStream.read_string();
		out.data(value);
	}

	private void listType(ShortType type) throws OutputException
	{
		short value = currentMessageInputStream.read_short();
		switch_type_encountered = new Short(value);
		out.data(value);
	}

	private void listType(LongType type) throws OutputException
	{
		int value = currentMessageInputStream.read_long();
		switch_type_encountered = new Integer(value);
		out.data(value);
	}

	private void listType(LongLongType type) throws OutputException
	{
		long value = currentMessageInputStream.read_longlong();
		out.data(value);
	}


	private void listType(BooleanType type) throws OutputException
	{
		boolean value = currentMessageInputStream.read_boolean();
		switch_type_encountered = new Boolean(value);
		out.data(value);
	}

	private void listType(OctetType type) throws OutputException
	{
		byte value = currentMessageInputStream.read_octet();
		out.data(value);
	}


	private void listType(CharType type) throws OutputException
	{
		char value = currentMessageInputStream.read_char();
		switch_type_encountered = new Character(value);
		out.data(value);
	}


	private void listType(FloatType type) throws OutputException
	{
		float value = currentMessageInputStream.read_float();
		out.data(value);
	}


	private void listType(DoubleType type) throws OutputException
	{
		double value = currentMessageInputStream.read_double();
		out.data(value);
	}


	private void listType(BaseType baseType) throws OutputException
	{
		int kind = baseType.getTCKind();

		switch (kind)
		{
			case org.omg.CORBA.TCKind._tk_boolean:
				boolean bvalue = currentMessageInputStream.read_boolean();
				out.data(bvalue);
				break;

			case org.omg.CORBA.TCKind._tk_char:
				char cvalue = currentMessageInputStream.read_char();
				out.data(cvalue);
				break;

			case org.omg.CORBA.TCKind._tk_short:
				short svalue = currentMessageInputStream.read_short();
				switch_type_encountered = new Short(svalue);
				out.data(svalue);
				break;

			case org.omg.CORBA.TCKind._tk_long:
				int lvalue = currentMessageInputStream.read_long();
				out.data(lvalue);
				break;

			case org.omg.CORBA.TCKind._tk_longlong:
				long llvalue = currentMessageInputStream.read_longlong();
				out.data(llvalue);
				break;

			case org.omg.CORBA.TCKind._tk_octet:
				byte ovalue = currentMessageInputStream.read_octet();
				out.data(ovalue);
				break;

			case org.omg.CORBA.TCKind._tk_float:
				float fvalue = currentMessageInputStream.read_float();
				out.data(fvalue);
				break;

			case org.omg.CORBA.TCKind._tk_double:
				double cdouble = currentMessageInputStream.read_double();
				out.data(cdouble);
				break;

			default:
				break;
		}
	}


	private Interface findInterfaceByTypeID(Spec theParsedSpec, String typeId)
	{
		// typeId:
		// IDL:demo/getdata/DataServer:1.0

		String[] s0 = typeId.split(":");
		String[] mods = s0[1].split("/");

		Interface result = null;

		if (mods.length < 2)
		{
			result = findInterface(theParsedSpec, mods[0]);
			return result;
		}

		Enumeration e = theParsedSpec.definitions.elements();
		while (e.hasMoreElements())
		{
			IdlSymbol s = ((IdlSymbol) e.nextElement());

			if (s instanceof Definition)
			{
				Definition d = (Definition) s;
				Declaration dc = d.get_declaration();

				if (dc instanceof Module)
				{
					String n = ((Module) dc).name();
					//System.out.println("Module: "+n);
					if (mods[0].equals(n))
					{
						result = findModuleInterface((Module) dc, mods, 1);
						if (result != null)
						{
							return result;
						}
					}
				}
			}
		}

		return null;

	}


	private Interface findModuleInterface(Module module, String[] mods, int pos)
	{
		Interface ifc = null;

		Enumeration e = module.getDefinitions().elements();
		while (e.hasMoreElements())
		{
			IdlSymbol s = ((IdlSymbol) e.nextElement());

			if (s instanceof Definition)
			{
				Definition d = (Definition) s;
				Declaration dc = d.get_declaration();

				if (dc instanceof Interface)
				{
					String n = ((Interface) dc).name();
					if (mods[pos].equals(n))
					{
						return (Interface) dc;
					}
				}
				if (dc instanceof Module)
				{
					Module m = (Module) dc;
					String n = m.name();
					if (mods[pos].equals(n))
					{
						if (pos == mods.length)
						{
							ifc = findInterface(m, mods[mods.length - 1]);
						}
						else
						{
							ifc = findModuleInterface(m, mods, pos + 1);
						}
						return ifc;
					}
				}
			}
		}

		return null;
	}

	private Interface findInterface(Module module, String name)
	{
		Enumeration e = module.getDefinitions().elements();
		while (e.hasMoreElements())
		{
			IdlSymbol s = ((IdlSymbol) e.nextElement());

			if (s instanceof Definition)
			{
				Definition d = (Definition) s;
				Declaration dc = d.get_declaration();

				if (dc instanceof Interface)
				{
					String n = ((Interface) dc).name();
					if (name.equals(n))
					{
						return (Interface) dc;
					}
				}
			}
		}

		return null;
	}

	private Interface findInterface(Spec spec, String name)
	{
		Enumeration e = spec.definitions.elements();
		while (e.hasMoreElements())
		{
			IdlSymbol s = ((IdlSymbol) e.nextElement());

			if (s instanceof Definition)
			{
				Definition d = (Definition) s;
				Declaration dc = d.get_declaration();

				if (dc instanceof Interface)
				{
					String n = ((Interface) dc).name();
					if (name.equals(n))
					{
						return (Interface) dc;
					}
				}
			}
		}

		return null;
	}

	private Operation findOperation(Interface iface, String name)
	{
		InterfaceBody interfaceBody = iface.body;

		for (Enumeration e = interfaceBody.v.elements(); e.hasMoreElements();)
		{
			Definition d = (Definition) e.nextElement();
			if (d.get_declaration() instanceof OpDecl)
			{
				OpDecl o = (OpDecl) d.get_declaration();

				String n = o.opName();
				if (name.equals(n))
				{
					return o;
				}
			}
		}

		return null;

	}

	private String toHex(byte b)
	{
		char[] c = new char[2];

		c[0] = Character.forDigit((b & 0xf0) >> 4, 16);
		c[1] = Character.forDigit((b & 0xf), 16);

		return new String(c);
	}

	private String toString(byte[] message)
	{
		StringBuffer sb = new StringBuffer();
		int i, p = 0, n = message.length;


		int s;

		byte[] chunk;

		for (i = 0; i < n; i++)
		{
			sb.append(toHex(message[i]));
			sb.append(' ');
		}
		sb.append(' ');

		for (i = 0; i < n; i++)
		{
			char c = (char) message[i];
			if (!(Character.isLetterOrDigit(c)))
			{
				c = '.';
			}
			sb.append(c);

		}

		return sb.toString();
	}

}