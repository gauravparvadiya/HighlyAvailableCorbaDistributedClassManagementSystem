package replica1.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import replica1.entities.Record;

/**
 * Client thread class for transferring a record from one Center Server to another through UDP/IP communication.
 * @author Jyotsana Gupta
 */
public class RecTransferClientThread extends Thread
{
	private Record targetRec;
	private String hostname;
	private int port;
	private String transferStatus;
	
	/**
	 * Constructor with all the attribute values provided as parameters.
	 * @param 	targetRec		The record to be transferred
	 * @param 	hostname		Hostname of the server contacted by this thread
	 * @param 	port			Port number of the server contacted by this thread
	 */
	public RecTransferClientThread(Record targetRec, String hostname, int port)
	{
		this.targetRec = targetRec;
		this.hostname = hostname;
		this.port = port;
	}
	
	/**
	 * Fetches the transfer status of this thread.
	 * @return	Transfer status of this thread
	 */
	public String getTransferStatus()
	{
		return transferStatus;
	}
	
	/**
	 * Sends the target record to the remote Center Server and receives the transfer status message in return 
	 * using UDP/IP socket communication.
	 */
	public void run()
	{
		DatagramSocket clientSocket = null;
		
		try
		{
			clientSocket = new DatagramSocket();
			InetAddress serverAddr = InetAddress.getByName(hostname);
			int serverPort = port;
			
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objOutput = new ObjectOutputStream(byteOutput);
			objOutput.writeObject(targetRec);
			byte[] objReqMsg = byteOutput.toByteArray();
			objOutput.close();
			byteOutput.close();
			
			DatagramPacket requestPacket = new DatagramPacket(objReqMsg, objReqMsg.length, serverAddr, serverPort);
			clientSocket.send(requestPacket);
			
			byte[] replyMsg = new byte[1000];
			DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length);	
			clientSocket.receive(replyPacket);
			
			transferStatus = new String(replyMsg).trim();			
		}
		catch(SocketException se)
		{
			System.out.println("Exception occurred while sending record from UDP/IP client: " + se.getMessage());
			transferStatus = "Failed to transfer record";
		}
		catch(UnknownHostException uhe)
		{
			System.out.println("Exception occurred while sending record from UDP/IP client: " + uhe.getMessage());
			transferStatus = "Failed to transfer record";
		}
		catch(IOException ioe)
		{
			System.out.println("Exception occurred while sending record from UDP/IP client: " + ioe.getMessage());
			transferStatus = "Failed to transfer record";
		}
		finally
		{
			if (clientSocket != null)
				clientSocket.close();
		}
	}
}