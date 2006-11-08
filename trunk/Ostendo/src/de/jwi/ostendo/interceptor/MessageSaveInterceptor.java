
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


import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.HashMap;

import org.jacorb.orb.giop.Messages;
import org.jacorb.orb.giop.RequestOutputStream;
import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.omg.GIOP.MsgType_1_1;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

/**
 * @author Juergen Weber 
 * Created on 14.08.2006
 */
public class MessageSaveInterceptor extends org.omg.CORBA.LocalObject
		implements
			ClientRequestInterceptor
{
	private HashMap mapRequestIDToOperation = new HashMap();

	private final int MSG_HEADER_SIZE = 12;
	
	File outputdir;
	
	NumberFormat nf;
	
	
	public MessageSaveInterceptor(String outdir)
	{
		outputdir = new File(outdir);
		if (!outputdir.exists())
		{
			outputdir.mkdir();
		}
		
		nf = NumberFormat.getIntegerInstance();
		nf.setMinimumIntegerDigits(2);
	}
	

	public String name()
	{
		return "ClientForwardInterceptor";
	}

	public void destroy()
	{

	}

	public void send_request(ClientRequestInfo ri) throws ForwardRequest
	{
		ClientRequestInfoImpl clientRequestInfoImpl = (ClientRequestInfoImpl) ri;

		String operation = clientRequestInfoImpl.operation();

		byte[] buffer = getBufferCopy(clientRequestInfoImpl.request_os);

		// unfortunately JacORB hasn't inserted the message size yet.
		insertMsgSize(clientRequestInfoImpl.request_os, buffer);

		String magic = new String(buffer, 0, 4);

		int requestId = Messages.getRequestId(buffer);

		mapRequestIDToOperation.put(new Integer(requestId), operation);

		int msgSize = Messages.getMsgSize(buffer) + MSG_HEADER_SIZE;

		int msgType = Messages.getMsgType(buffer);
		String msgTypeS = MsgType_1_1.from_int(msgType).toString();

		String fileName = nf.format(requestId) +  "-" + operation + "-" 
				+ msgTypeS + ".bin";

		writeBuffer(fileName, buffer, msgSize);
	}

	private void insertMsgSize(RequestOutputStream request_os, byte[] buffer)
	{
		// this code is copyied from org.jacorb.orb.giop.MessageOutputStream.insertMsgSize()

		int allsize = request_os.size();

		int size = allsize - MSG_HEADER_SIZE;

		//using big endian byte ordering
		buffer[8] = (byte) ((size >> 24) & 0xFF);
		buffer[9] = (byte) ((size >> 16) & 0xFF);
		buffer[10] = (byte) ((size >> 8) & 0xFF);
		buffer[11] = (byte) (size & 0xFF);

	}

	public void send_poll(ClientRequestInfo ri)
	{
	}

	public void receive_reply(ClientRequestInfo ri)
	{
		ClientRequestInfoImpl clientRequestInfoImpl = (ClientRequestInfoImpl) ri;

		byte[] buffer = getBufferCopy(clientRequestInfoImpl.reply_is);

		int requestId = Messages.getRequestId(buffer);
		String operation = (String)mapRequestIDToOperation.get(new Integer(requestId));

		int msgSize = Messages.getMsgSize(buffer) + MSG_HEADER_SIZE;
		
		int msgType = Messages.getMsgType(buffer);
		String msgTypeS = MsgType_1_1.from_int(msgType).toString();

		String fileName = nf.format(requestId) +  "-" + operation + "-" 
		+ msgTypeS + ".bin";

		writeBuffer(fileName, buffer, msgSize);
	}

	public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
	{
		receive_reply(ri);
	}

	public void receive_other(ClientRequestInfo ri) throws ForwardRequest
	{
		int x = 5;
	}

	/*
	 * Field buffer of ReplyInputStream is not public, so get it via reflection
	 */
	private byte[] getBufferCopy(Object theStream)
	{
		Object o;
		Field field = null;

		try
		{
			Class c = theStream.getClass();
			do
			{
				try
				{
					field = c.getDeclaredField("buffer");
					break;
				}
				catch (NoSuchFieldException e)
				{
				}

				c = c.getSuperclass();
			}
			while (c != null);

			field.setAccessible(true);
			o = field.get(theStream);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		byte[] buffer = (byte[]) o;

		byte[] b = new byte[buffer.length];
		System.arraycopy(buffer, 0, b, 0, buffer.length);
		return b;

	}

	private void writeBuffer(String n, byte[] b, int len)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(outputdir,n));
			fos.write(b, 0, len);
			fos.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
