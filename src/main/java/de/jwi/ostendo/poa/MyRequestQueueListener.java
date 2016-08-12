package de.jwi.ostendo.poa;

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


import java.io.FileOutputStream;
import java.io.IOException;

import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.giop.ReplyOutputStream;
import org.jacorb.orb.giop.RequestInputStream;
import org.omg.GIOP.RequestHeader_1_2;

/**
 * @author Juergen Weber 
 * Created on 28.07.2006
 */
public class MyRequestQueueListener implements org.jacorb.poa.RequestQueueListener

{

	public void requestAddedToQueue(ServerRequest request, int queue_size)
	{
		System.err.println("requestAddedToQueue");
		
		RequestInputStream in = request.get_in();
		
		 byte[] buf = in.getBufferCopy(); 
		 
		 RequestHeader_1_2 h = in.req_hdr;
		 String s = h.operation + "_" +  h.request_id+".bin";
		
		 int hdrsize = 12;
		 
		int n = in.msg_size;

		writeBuffer(s,buf,n + hdrsize);
		
		int x = 5;
		int y = 5;
		int z = x+y;
	}

	private void writeBuffer(String n, byte[] b, int len)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(n);
			fos.write(b, 0, len);
			fos.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void requestRemovedFromQueue(ServerRequest request, int queue_size)
	{
		System.err.println("requestRemovedFromQueue");
		
		ReplyOutputStream out = request.get_out();
		
		int x = 5;
		int y = 5;
		int z = x+y;
		
	}

}
