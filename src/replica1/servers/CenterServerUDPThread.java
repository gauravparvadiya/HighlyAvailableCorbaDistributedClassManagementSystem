package replica1.servers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import frontend.entities.Request;
import replica1.entities.Record;
import replica1.services.RecordManagerImpl;

/**
 * Center Server thread class for UDP/IP communication between Center Servers and with Replica Manager.
 * @author Jyotsana Gupta
 */
public class CenterServerUDPThread extends Thread
{
	private DatagramSocket serverSocket;
	private int serverPort;
	private RecordManagerImpl recMgr;
	
	/**
	 * Constructor with the attribute values provided as parameters.
	 * @param 	serverPort	Port number of this server 
	 * @param 	recMgr		Object of the remote interface implementation
	 */
	public CenterServerUDPThread(int serverPort, RecordManagerImpl recMgr)
	{
		serverSocket = null;
		this.serverPort = serverPort;
		this.recMgr = recMgr;
	}
	
	/**
	 * Receives and responds to requests from another center server or replica manager using UDP/IP socket communication.
	 */
	public void run()
	{
		try
		{
			serverSocket = new DatagramSocket(serverPort);
			
			while (true)
			{
				byte[] requestMsg = new byte[1000];
				DatagramPacket requestPacket = new DatagramPacket(requestMsg, requestMsg.length);
				serverSocket.receive(requestPacket);
				
				String requestStr = new String(requestMsg);
				if (requestStr.trim().equalsIgnoreCase("get_record_count"))
					sendRecordCount(requestPacket);
				else
					identifyObject(requestMsg, requestPacket);
			}
		}
		catch(SocketException se)
		{
			System.out.println("Exception occurred during UDP/IP server interaction: " + se.getMessage());
		}
		catch (IOException ioe)
		{
			System.out.println("Exception occurred during UDP/IP server interaction: " + ioe.getMessage());
		}
		finally
		{
			if (serverSocket != null)
				serverSocket.close();
		}
	}	
	
	/**
	 * Fetches record count of this Center Server and sends it back to the client.
	 * @param	requestPacket	Datagram packet of client request
	 */
	private void sendRecordCount(DatagramPacket requestPacket)
	{
		int recCount = -1;
		recCount = recMgr.getOwnRecordCount();
		
		try
		{
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			DataOutputStream dataOutput = new DataOutputStream(byteOutput);
			dataOutput.writeInt(recCount);
			byte[] replyMsg = byteOutput.toByteArray();
			dataOutput.close();
			byteOutput.close();
			
			DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length, requestPacket.getAddress(), 
															requestPacket.getPort());
			serverSocket.send(replyPacket);
		}
		catch (IOException ioe)
		{
			System.out.println("Exception occurred while sending record count from UDP/IP server: " + ioe.getMessage());
		}
	}
	
	/**
	 * Identifies the type of object received from client.
	 * @param 	requestMsg		Byte array containing the request message
	 * @param 	requestPacket	Datagram packet of client request
	 */
	private void identifyObject(byte[] requestMsg, DatagramPacket requestPacket)
	{
		Object inObject = null;
		Record targetRec = null;
		Request newRequest = null;
		String failStatus = null;
		
		//Converting the received request into Record or Request object
		ByteArrayInputStream byteInput = new ByteArrayInputStream(requestMsg);
		try
		{
			ObjectInputStream objInput = new ObjectInputStream(byteInput);
			try
			{
				inObject = objInput.readObject();
				objInput.close();
				byteInput.close();
				
				targetRec = (Record) inObject;
			}
			catch(ClassNotFoundException reccnfe)
			{
				try
				{
					newRequest = (Request) inObject;
				}
				catch(ClassCastException reqcce)
				{
					failStatus = "Record class not found or invalid request type";
				}
			}
			catch(ClassCastException reccce)
			{
				try
				{
					newRequest = (Request) inObject;
				}
				catch(ClassCastException reqcce)
				{
					failStatus = "Invalid object type";
				}
			}
		}
		catch(StreamCorruptedException sce)
		{
			failStatus = "Invalid method call";
		}
		catch(IOException ioe)
		{
			failStatus = "I/O exception occurred";
		}
		
		//Sending operation status as reply to client in case of failure
		if (failStatus != null)
		{
			byte[] replyMsg = failStatus.getBytes();
			DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length, 
															requestPacket.getAddress(), requestPacket.getPort());
			try
			{
				serverSocket.send(replyPacket);
			}
			catch(IOException ioe)
			{
				System.out.println("Exception occurred while receiving record or request at UDP/IP server: " + ioe.getMessage());
			}
		}
		else
		{
			//Adding target Record to destination server database if the object received is of Record type
			if (targetRec != null)
				addReceivedRecord(targetRec, requestPacket);
			//Invoking Center Server implementation for request processing if the object received is of Request type
			else if (newRequest != null)
				invokeCenterServer(newRequest, requestPacket);
		}
	}
	
	/**
	 * Adds the received Record to the database of this Center Server and returns the addition status to the client. 
	 * @param	targetRec		Target Record to be added
	 * @param 	requestPacket	Datagram packet of client request
	 */
	private void addReceivedRecord(Record targetRec, DatagramPacket requestPacket)
	{
		String addStatus = null;
		
		//Adding the received record to this center server's database
		boolean recAdded = recMgr.addRecord(targetRec);
		if (recAdded)
			addStatus = "Record addition successful";
		else
			addStatus = "Failed to add record";
		
		//Sending addition operation status as reply to client
		byte[] replyMsg = addStatus.getBytes();
		DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length, 
														requestPacket.getAddress(), requestPacket.getPort());
		try
		{
			serverSocket.send(replyPacket);
		}
		catch(IOException ioe)
		{
			System.out.println("Exception occurred while receiving record at UDP/IP server: " + ioe.getMessage());
		}
	}
	
	/**
	 * Invokes the intended operation on the intended Center Server in this replica.
	 * @param 	newRequest		Request received for processing
	 * @param 	requestPacket	Datagram packet of client request
	 */
	private void invokeCenterServer(Request newRequest, DatagramPacket requestPacket)
	{
		String opStatus = null;
		String methodName = newRequest.getMethodName();
		
		//Calling the required method on Center Server
		if (methodName.equalsIgnoreCase("createTRecord"))
		{
			opStatus = recMgr.createTRecord(newRequest.getMethodArgs().get(0)
											,newRequest.getMethodArgs().get(1)
											,newRequest.getMethodArgs().get(2)
											,newRequest.getMethodArgs().get(3)
											,newRequest.getMethodArgs().get(4)
											,newRequest.getMethodArgs().get(5)
											,newRequest.getMethodArgs().get(6));
		}
		else if (methodName.equalsIgnoreCase("createSRecord"))
		{
			opStatus = recMgr.createSRecord(newRequest.getMethodArgs().get(0)
											,newRequest.getMethodArgs().get(1)
											,newRequest.getMethodArgs().get(2)
											,newRequest.getMethodArgs().get(3)
											,newRequest.getMethodArgs().get(4)
											,newRequest.getMethodArgs().get(5));
		}
		else if (methodName.equalsIgnoreCase("getRecordCounts"))
		{
			opStatus = recMgr.getRecordCounts(newRequest.getMethodArgs().get(0));
		}
		else if (methodName.equalsIgnoreCase("editRecord"))
		{
			opStatus = recMgr.editRecord(newRequest.getMethodArgs().get(0)
										,newRequest.getMethodArgs().get(1)
										,newRequest.getMethodArgs().get(2)
										,newRequest.getMethodArgs().get(3));
		}
		else if (methodName.equalsIgnoreCase("transferRecord"))
		{
			opStatus = recMgr.transferRecord(newRequest.getMethodArgs().get(0)
											,newRequest.getMethodArgs().get(1)
											,newRequest.getMethodArgs().get(2));
		}
		
		//Sending operation status as reply to client
		byte[] replyMsg = opStatus.getBytes();
		DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length, 
														requestPacket.getAddress(), requestPacket.getPort());
		try
		{
			serverSocket.send(replyPacket);
		}
		catch(IOException ioe)
		{
			System.out.println("Exception occurred while invoking Center Server at UDP/IP server: " + ioe.getMessage());
		}
	}
}