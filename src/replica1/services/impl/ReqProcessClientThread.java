package replica1.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import replica1.entities.Request;

//TODO changes

/**
 * Client thread class for sending a Request for processing to a Center Server through UDP/IP communication.
 * @author Jyotsana Gupta
 */
public class ReqProcessClientThread extends Thread
{
	private Request newRequest;
	private String hostname;
	private int port;
	private String processStatus;
	
	/**
	 * Constructor with all the attribute values provided as parameters.
	 * @param 	newRequest	The request to be processed
	 * @param 	hostname	Hostname of the Center Server contacted by this thread
	 * @param 	port		Port number of the Center Server contacted by this thread
	 */
	public ReqProcessClientThread(Request newRequest, String hostname, int port)
	{
		this.newRequest = newRequest;
		this.hostname = hostname;
		this.port = port;
	}
	
	/**
	 * Fetches the request processing status of this thread.
	 * @return	Request processing status of this thread
	 */
	public String getProcessStatus()
	{
		return processStatus;
	}
	
	/**
	 * Sends the request to the remote Center Server and receives the processing status message in return 
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
			objOutput.writeObject(newRequest);
			byte[] objReqMsg = byteOutput.toByteArray();
			objOutput.close();
			byteOutput.close();
			
			DatagramPacket requestPacket = new DatagramPacket(objReqMsg, objReqMsg.length, serverAddr, serverPort);
			clientSocket.send(requestPacket);
			
			byte[] replyMsg = new byte[1000];
			DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length);	
			clientSocket.receive(replyPacket);
			
			processStatus = new String(replyMsg).trim();
		}
		catch(SocketException se)
		{
			System.out.println("Exception occurred while sending request from UDP/IP client: " + se.getMessage());
			processStatus = "Failed to process request";
		}
		catch(UnknownHostException uhe)
		{
			System.out.println("Exception occurred while sending request from UDP/IP client: " + uhe.getMessage());
			processStatus = "Failed to process request";
		}
		catch(IOException ioe)
		{
			System.out.println("Exception occurred while sending request from UDP/IP client: " + ioe.getMessage());
			processStatus = "Failed to process request";
		}
		finally
		{
			if (clientSocket != null)
				clientSocket.close();
		}
	}
}