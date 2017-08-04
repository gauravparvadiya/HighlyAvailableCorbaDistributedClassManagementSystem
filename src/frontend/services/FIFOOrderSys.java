package frontend.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import frontend.entities.Request;

/**
 * FIFO ordering, defining methods to send and remove requests.
 * @author Hirangi Naik
 * @author Jyotsana Gupta
 */
public class FIFOOrderSys 
{
	/**
	 * FIFO queue for holding incoming requests.
	 */
	private LinkedList<Request> reqQueue = new LinkedList<Request>();

	/**
	 * Adds an incoming request to the end of the request queue.
	 * @param 	newRequest	Request to be added
	 * @return	true, if request is successfully added
	 * 			false, otherwise
	 */
	public boolean addRequest(Request newRequest) 
	{
		boolean addStatus = true;
		try
		{
			reqQueue.add(newRequest);
		}
		catch(Exception e)
		{
			System.out.println("Exception occurred while adding request to queue: " + e.getMessage());
			addStatus = false;
		}
		
		return addStatus;
	}

	/**
	 * Sends the first request in the queue to the lead server for processing using UDP/IP communication.
	 * @param 	leadServerHostname	Host name of the lead server
	 * @param 	leadServerPort		Port number of the lead server
	 * @return	Success or failure message according to the processing status of the request.
	 */
	public String sendFirstRequest(String leadServerHostname, int leadServerPort) 
	{
		String processStatus = null;
		
		if (!reqQueue.isEmpty()) 
		{
			DatagramSocket fifoSocket = null;
			Request newRequest = reqQueue.getFirst();
			
			try 
			{
				fifoSocket = new DatagramSocket();
				fifoSocket.setSoTimeout(1000);
				InetAddress leadAddr = InetAddress.getByName(leadServerHostname);
				
				//Making multiple attempts to send request to leader in case it is down
				String sendStatus = null;
				for (int i=0; i<5; i++)
				{
					sendStatus = sendRequest(newRequest, fifoSocket, leadAddr, leadServerPort);
					if (!((sendStatus != null) && (sendStatus.equalsIgnoreCase("leader_down"))))
						break;					
				}
				
				if ((sendStatus != null) && (sendStatus.equalsIgnoreCase("leader_down")))
					processStatus = "Failed to send request to lead server as it is down and cannot recover at the moment";
				else if ((sendStatus != null) && (!sendStatus.equalsIgnoreCase("leader_down")))
					processStatus = "Failed to send request to lead server due to some technical fault";
				else
				{
					//Making multiple attempts to receive a response from leader in case it goes down
					String reply = null;		
					for (int i=0; i<5; i++)
					{
						if (sendStatus == null)
						{
							reply = receiveReply(fifoSocket);
							
							if (reply == null)
							{
								processStatus = "Failed to receive response from leader due to some technical fault";
								break;
							}
							else if (reply.toLowerCase().indexOf("timeout") >= 0)
							{
								processStatus = "Failed to process request as lead server is down and cannot recover at the moment";
								sendStatus = sendRequest(newRequest, fifoSocket, leadAddr, leadServerPort);
							}
							else
							{
								processStatus = reply;
								break;
							}
						}
						else							
							sendStatus = sendRequest(newRequest, fifoSocket, leadAddr, leadServerPort);
					}
				}				
			}
			catch(SocketException se)
			{
				System.out.println("Exception occurred while sending request to leader from FIFO system: " + se.getMessage());
				processStatus = "Failed to send request to lead server";
			}
			catch(UnknownHostException uhe)
			{
				System.out.println("Exception occurred while sending request to leader from FIFO system: " + uhe.getMessage());
				processStatus = "Failed to send request to lead server";
			}
			finally
			{
				if (fifoSocket != null)
					fifoSocket.close();
				
				if (processStatus == null)
					processStatus = "Failed to send/process request";
			}
		} 
		else
			processStatus = "No request found in queue for processing";
		
		return processStatus;
	}

	/**
	 * Removes the first request from the request queue.
	 */
	public void removeFirstRequest() 
	{
		reqQueue.removeFirst();
	}
	
	/**
	 * Sends request to lead server for processing.
	 * @param 	newRequest		Request to be sent
	 * @param 	fifoSocket		Datagram socket to be used for sending UDP request
	 * @param 	leadAddr		Inet address of the lead server
	 * @param 	leadServerPort	Port number of the lead server
	 * @return	Null, if the send operation is successful
	 * 			Error message, otherwise
	 */
	private String sendRequest(Request newRequest, DatagramSocket fifoSocket, InetAddress leadAddr, int leadServerPort)
	{
		String sendStatus = null;
		
		try
		{
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objOutput = new ObjectOutputStream(byteOutput);
			objOutput.writeObject(newRequest);
			byte[] objReqMsg = byteOutput.toByteArray();
			objOutput.close();
			byteOutput.close();
			
			DatagramPacket requestPacket = new DatagramPacket(objReqMsg, objReqMsg.length, leadAddr, leadServerPort);
			fifoSocket.send(requestPacket);
		}
		catch(NoRouteToHostException nrthe)
		{
			System.out.println("Exception occurred while sending request to leader from FIFO system: " + nrthe.getMessage());
			sendStatus = "leader_down";
		}
		catch(IOException ioe)
		{
			System.out.println("Exception occurred while sending request to leader from FIFO system: " + ioe.getMessage());
			sendStatus = "Failed to send request to lead server";
		}
		
		return sendStatus;
	}
	
	/**
	 * Receives a response from the lead server.
	 * @param 	fifoSocket	Datagram socket to be used for receiving UDP response
	 * @return	The received response, if successful
	 * 			null, otherwise
	 */
	private String receiveReply(DatagramSocket fifoSocket)
	{
		String reply = null;
		
		byte[] replyMsg = new byte[1000];
		DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length);	
		
		try
		{
			fifoSocket.receive(replyPacket);
			reply = new String(replyMsg).trim();
		}
		catch(SocketTimeoutException ste)
		{
			reply = "Processing timeout exceeded";
		}
		catch(IOException ioe)
		{
			System.out.println("Exception occurred in FIFO system while receiving response from lead server: " + ioe.getMessage());
		}
		
		return reply;
	}
}