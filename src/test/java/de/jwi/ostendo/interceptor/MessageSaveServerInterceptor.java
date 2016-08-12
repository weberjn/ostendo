
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
import java.text.NumberFormat;
import java.util.HashMap;

import org.jacorb.orb.giop.Messages;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.omg.GIOP.MsgType_1_1;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * @author Juergen Weber 
 * Created on 04.12.2006
 */
public class MessageSaveServerInterceptor extends org.omg.CORBA.LocalObject
		implements
		ServerRequestInterceptor
{
	private HashMap mapRequestIDToOperation = new HashMap();

	private final int MSG_HEADER_SIZE = 12;
	
	File outputdir;
	
	NumberFormat nf;
	
	
	public MessageSaveServerInterceptor(String outdir)
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
		return "MessageSaveServerInterceptor";
	}

	public void destroy()
	{

	}




	public void receive_request(ServerRequestInfo ri) throws ForwardRequest
	{
		ServerRequestInfoImpl serverRequestInfoImpl = (ServerRequestInfoImpl) ri;

		String operation = serverRequestInfoImpl.operation();
/*
		byte[] buffer = serverRequestInfoImpl.r  request_os.getBufferCopy();
		

		// unfortunately JacORB hasn't inserted the message size yet.
		insertMsgSize(serverRequestInfoImpl.request_os, buffer);

		String magic = new String(buffer, 0, 4);

		int requestId = Messages.getRequestId(buffer);

		mapRequestIDToOperation.put(new Integer(requestId), operation);

		int msgSize = Messages.getMsgSize(buffer) + MSG_HEADER_SIZE;

		int msgType = Messages.getMsgType(buffer);
		String msgTypeS = MsgType_1_1.from_int(msgType).toString();

		String fileName = nf.format(requestId) +  "-" + operation + "-" 
				+ msgTypeS + ".bin";

		writeBuffer(fileName, buffer, msgSize);
*/
		int x = 5;
		int y=6;
	}


	public void receive_request_service_contexts(ServerRequestInfo ri)
			throws ForwardRequest
	{
		// TODO Auto-generated method stub
		
	}


	public void send_exception(ServerRequestInfo ri) throws ForwardRequest
	{
		// TODO Auto-generated method stub
		
	}


	public void send_other(ServerRequestInfo ri) throws ForwardRequest
	{
		// TODO Auto-generated method stub
		
	}


	public void send_reply(ServerRequestInfo ri)
	{
		// TODO Auto-generated method stub
		
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
