package replica3.services;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

public class RecCtrClientThread extends Thread
{
	private String servID;
	private String host;
	private int port;
	private Map<String, Integer> servRecCount;
	
	public RecCtrClientThread(Map<String, Integer> servRecCount, String servID, String host, int port)
	{
		this.servRecCount = servRecCount;
		this.servID = servID;
		this.host = host;
		this.port = port;
	}
	
	public void run()
	{
		DatagramSocket socket = null;
		String reqStr = "get_record_count";
		
		try
		{
			socket = new DatagramSocket();
			byte[] reqMsg = reqStr.getBytes();
			InetAddress addr = InetAddress.getByName(host);
			int portNum = port;
			DatagramPacket outPack = new DatagramPacket(reqMsg, reqMsg.length, addr, portNum);
			socket.send(outPack);
			
			byte[] repMsg = new byte[1000];
			DatagramPacket inPack = new DatagramPacket(repMsg, repMsg.length);	
			socket.receive(inPack);
			ByteArrayInputStream bi = new ByteArrayInputStream(inPack.getData());
			DataInputStream di = new DataInputStream(bi);
			int recCt = di.readInt();
			di.close();
			bi.close();
			
			servRecCount.put(servID, recCt);
		}
		catch(SocketException se)
		{
			System.out.println("Exception in RecCtrClientThread: " + se.getMessage());
		}
		catch(UnknownHostException uhe)
		{
			System.out.println("Exception in RecCtrClientThread: " + uhe.getMessage());
		}
		catch(IOException ioe)
		{
			System.out.println("Exception in RecCtrClientThread: " + ioe.getMessage());
		}
		finally
		{
			if (socket != null)
				socket.close();
		}
	}
}