package replica2.servers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import failuredetectionsys.RMFailObjectServer;
import frontend.entities.Request;
import replica2.services.ReplicaMgrUDPClient;
import replica2.servers.RMFailDetectUDPThread;
import replica2.services.ReplicaMgrService;

/**
 * Replica Manager's UDP server class for receiving and forwarding requests for processing
 * and communicating its results to clients.
 * @author Jyotsana Gupta
 */
public class ReplicaMgrUDPServer
{
	private static final int SERVER_PORT = 6794;
	private static ReplicaMgrService rmService = new ReplicaMgrService();
	private static DatagramSocket serverSocket = null;
	
	public static void main(String[] args) throws SocketException
	{		
		System.out.println("Manager UDP Server for Replica2 has been started.");
		
		//Launching a parallel thread for communicating with failure detection system
		RMFailDetectUDPThread rmFDUDPThread;
		try 
		{
			rmFDUDPThread = new RMFailDetectUDPThread(rmService.getFifoBroadcastSys());
			rmFDUDPThread.start();
			
			//Sending the failure detection thread to the failure detection system
//			ReplicaMgrUDPClient rmUDPClient = new ReplicaMgrUDPClient(rmFDUDPThread, "localhost", 6501);
//			rmUDPClient.sendFailDetectThread();
			
			//RMFailObjectServer server = new RMFailObjectServer();
			RMFailObjectServer.setRM2(rmFDUDPThread);
			
		} 
		catch (SocketException se) 
		{
			se.printStackTrace();
		} 
		
		//Performing major request processing tasks
		try
		{
			serverSocket = new DatagramSocket(SERVER_PORT);
			
			while (true)
			{
				//Receiving request from UDP client
				byte[] requestMsg = new byte[1000];
				DatagramPacket requestPacket = new DatagramPacket(requestMsg, requestMsg.length);
				serverSocket.receive(requestPacket);
				
				//Processing request based on its type
				String requestStr = new String(requestMsg);
				if (requestStr.trim().indexOf("leaderstatus") == 0)
					ReplicaMgrService.setIsLeader(requestStr);
				else
					forwardToService(requestPacket);
			}
		}
		catch(SocketException se)
		{
			System.out.println("Exception occurred during RM UDP server interaction: " + se.getMessage());
		}
		catch (IOException ioe)
		{
			System.out.println("Exception occurred during RM UDP server interaction: " + ioe.getMessage());
		}
		finally
		{
			if (serverSocket != null)
				serverSocket.close();
		}
	}
	
	/**
	 * Translates received byte stream into Request object and forwards it to service module for processing. 
	 * @param	requestMsg		Byte array containing the request message i.e. the Request to be processed
	 * @return	Success or failure status message of the processing
	 */
	private static void forwardToService(DatagramPacket requestPacket)
	{
		String processStatus = null;
		
		//Translating byte stream into Request object
		ByteArrayInputStream byteInput = new ByteArrayInputStream(requestPacket.getData());
		try
		{
			ObjectInputStream objInput = new ObjectInputStream(byteInput);
			Request newRequest = null;
			try
			{
				newRequest = (Request) objInput.readObject();
				objInput.close();
				byteInput.close();
				
				//Forwarding the request to service module for processing
				String methodName = newRequest.getMethodName();
				if (methodName.equalsIgnoreCase("crashCenterServer"))
					processStatus = rmService.crashCenterServer(newRequest);
				else
					processStatus = rmService.processRequest(newRequest);
			}
			catch(ClassNotFoundException cnfe)
			{
				processStatus = "Request class not found";
			}
			catch(ClassCastException cce)
			{
				processStatus = "Invalid request type";
			}
		}
		catch(StreamCorruptedException sce)
		{
			processStatus = "Invalid process method";
		}
		catch(IOException ioe)
		{
			processStatus = "I/O exception occurred";
		}
		
		//If processing fails
		if (processStatus == null)
			processStatus = "Failed to process request";
		
		//Sending request processing status as reply to UDP client
		byte[] replyMsg = processStatus.getBytes();
		DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length, 
														requestPacket.getAddress(), requestPacket.getPort());
		try
		{
			serverSocket.send(replyPacket);
		}
		catch (IOException ioe)
		{
			System.out.println("Exception occurred during RM UDP server interaction: " + ioe.getMessage());
		}
	}
}