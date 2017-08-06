package replica2.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import frontend.entities.Request;

/**
 * Class for broadcasting requests from lead server to secondary servers.
 * @author Hirangi Naik
 * @author Jyotsana Gupta
 */
public class FIFOBroadcastSys 
{
	/**
	 * Stores hostnames and port numbers of secondary servers.
	 */
	private List<String[]> secServerDetails = new ArrayList<String[]>();
	
	/**
	 * Object used as a lock for secondary server details access and update.
	 */
	private final Object secServDtlsLock = new Object();
	
	/**
	 * Sets the hostnames and port numbers for the secondary servers.
	 * @param	secServerDetails	Details of secondary servers
	 */
	public void setSecServerDetails(List<String[]> secServerDetails)
	{		
		synchronized (secServDtlsLock) 
		{
			this.secServerDetails = secServerDetails;
		}
	}
	
	/**
	 * Fetches the hostnames and port numbers of the secondary servers.
	 * @return	Details of secondary servers
	 */
	public List<String[]> getSecServerDetails()
	{
		synchronized (secServDtlsLock) 
		{			
			//TODO uncomment for testing with Replica 2 as leader
			/*
			List<String[]> tempDtls = new ArrayList<String[]>();
			String[] sec1 = {"localhost", "6790"};
			String[] sec2 = {"localhost", "6798"};
			tempDtls.add(sec1);
			tempDtls.add(sec2);
			secServerDetails = tempDtls;
			*/
			
			return secServerDetails;
		}
	}

	/**
	 * To broadcast the request to secondary servers
	 * @param newRequest
	 */
	public void broadcastRequest(Request newRequest) 
	{
		//Sending request to secondary server 1 and receiving its response
		String[] secServer1 = getSecServerDetails().get(0);
		String secServHost1 = secServer1[0];
		int secServPort1 = Integer.parseInt(secServer1[1]);		
		RequestCommunicator secServCommrThread1 = new RequestCommunicator(newRequest, secServHost1, secServPort1);
		secServCommrThread1.start();
		
		//Sending request to secondary server 2 and receiving its response
		String[] secServer2 = getSecServerDetails().get(1);
		String secServHost2 = secServer2[0];
		int secServPort2 = Integer.parseInt(secServer2[1]);	
		RequestCommunicator secServCommrThread2 = new RequestCommunicator(newRequest, secServHost2, secServPort2);
		secServCommrThread2.start();
		
		//Waiting for the request broadcasting threads to finish their execution
		try
		{
			secServCommrThread1.join();
			secServCommrThread2.join();
		}
		catch(InterruptedException ie)
		{
			System.out.println("Exception occurred during UDP interaction between FIFO broadcast system and secondary server: " + ie.getMessage());
		}
	}
}

/**
 * Thread class for sending request to and receiving response from secondary servers.
 * @author Jyotsana Gupta
 */
class RequestCommunicator extends Thread
{
	private Request newRequest;
	private String secServHostname;
	private int secServPort;
	
	/**
	 * Parameterized constructor with all attributes provided as arguments.
	 * @param 	newRequest			Request to be communicated
	 * @param 	secServHostname		Hostname of the secondary server to be contacted
	 * @param 	secServPort			Port number of the secondary server to be contacted
	 */
	public RequestCommunicator(Request newRequest, String secServHostname, int secServPort)
	{
		this.newRequest = newRequest;
		this.secServHostname = secServHostname;
		this.secServPort = secServPort;
	}
	
	/**
	 * Sends request to and receives response from the secondary server.
	 */
	public void run()
	{
		DatagramSocket broadcastSocket = null;
		try
		{
			broadcastSocket = new DatagramSocket();
			broadcastSocket.setSoTimeout(150);
			InetAddress secServAddr = InetAddress.getByName(secServHostname);
			
			//Sending request to secondary server for processing
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objOutput = new ObjectOutputStream(byteOutput);
			objOutput.writeObject(newRequest);
			byte[] objReqMsg = byteOutput.toByteArray();
			objOutput.close();
			byteOutput.close();
			
			DatagramPacket requestPacket = new DatagramPacket(objReqMsg, objReqMsg.length, secServAddr, secServPort);
			broadcastSocket.send(requestPacket);
			
			//Waiting to receive response from secondary server
			byte[] replyMsg = new byte[1000];
			DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length);
			broadcastSocket.receive(replyPacket);
		}
		catch(SocketException se)
		{
			System.out.println("Exception occurred during UDP interaction between FIFO broadcast system and secondary server: " + se.getMessage());
		}
		catch(UnknownHostException uhe)
		{
			System.out.println("Exception occurred during UDP interaction between FIFO broadcast system and secondary server: " + uhe.getMessage());
		}
		catch(SocketTimeoutException ste)
		{
			System.out.println("Processing timeout exceeded for secondary server.");
		}
		catch(IOException ioe)
		{
			System.out.println("Exception occurred during UDP interaction between FIFO broadcast system and secondary server: " + ioe.getMessage());
		}
		finally
		{
			if (broadcastSocket != null)
				broadcastSocket.close();
		}
	}
}