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
import replica1.entities.Record;
import replica1.services.impl.RecordManagerImpl;

/**
 * Center Server thread class for UDP/IP communication between Center Servers.
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
	 * Receives and responds to requests from another center server using UDP/IP socket communication.
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
					addReceivedRecord(requestMsg, requestPacket);
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
	 * Adds the received Record to the database of this Center Server and returns the addition status to the client. 
	 * @param	requestMsg		Byte array containing the request message i.e. the target Record
	 * @param 	requestPacket	Datagram packet of client request
	 */
	private void addReceivedRecord(byte[] requestMsg, DatagramPacket requestPacket)
	{
		String addStatus = null;
		
		ByteArrayInputStream byteInput = new ByteArrayInputStream(requestMsg);
		try
		{
			ObjectInputStream objInput = new ObjectInputStream(byteInput);
			Record targetRec = null;
			try
			{
				targetRec = (Record) objInput.readObject();
				objInput.close();
				byteInput.close();
				
				//Adding the received record to this center server's database
				boolean recAdded = recMgr.addRecord(targetRec);
				if (recAdded)
					addStatus = "Record addition successful";
				else
					addStatus = "Failed to add record";
			}
			catch(ClassNotFoundException cnfe)
			{
				addStatus = "Record class not found";
			}
			catch(ClassCastException cce)
			{
				addStatus = "Invalid record type";
			}
		}
		catch(StreamCorruptedException sce)
		{
			addStatus = "Invalid transfer method";
		}
		catch(IOException ioe)
		{
			addStatus = "I/O exception occurred";
		}
		
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
}