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

/**
 * Client thread class for fetching record counts from all other center servers through UDP/IP communication.
 * @author Jyotsana Gupta
 */
public class RecCtrClientThread extends Thread
{
	private Map<String, Integer> serverRecCounts;
	private String serverID;
	private String hostname;
	private int port;
	
	/**
	 * Constructor with all the attribute values provided as parameters.
	 * @param 	serverRecCounts	Hashmap for storing all server record counts
	 * @param 	serverID		Acronym for identifying the server contacted by this thread
	 * @param 	hostname		Hostname of the server contacted by this thread
	 * @param 	port			Port number of the server contacted by this thread
	 */
	public RecCtrClientThread(Map<String, Integer> serverRecCounts, String serverID, String hostname, int port)
	{
		this.serverRecCounts = serverRecCounts;
		this.serverID = serverID;
		this.hostname = hostname;
		this.port = port;
	}
	
	/**
	 * Requests and receives record count from another center server using UDP/IP socket communication.
	 * The fetched count is updated in the shared hashmap for storing all server record counts.
	 */
	public void run()
	{
		DatagramSocket clientSocket = null;
		String requestMsgStr = "get_record_count";
		
		try
		{
			clientSocket = new DatagramSocket();
			byte[] requestMsg = requestMsgStr.getBytes();
			InetAddress serverAddr = InetAddress.getByName(hostname);
			int serverPort = port;
			
			DatagramPacket requestPacket = new DatagramPacket(requestMsg, requestMsg.length, serverAddr, serverPort);
			clientSocket.send(requestPacket);
			
			byte[] replyMsg = new byte[1000];
			DatagramPacket replyPacket = new DatagramPacket(replyMsg, replyMsg.length);	
			clientSocket.receive(replyPacket);
			
			ByteArrayInputStream byteInput = new ByteArrayInputStream(replyPacket.getData());
			DataInputStream dataInput = new DataInputStream(byteInput);
			int recCount = dataInput.readInt();
			dataInput.close();
			byteInput.close();
			
			serverRecCounts.put(serverID, recCount);
		}
		catch(SocketException se)
		{
			System.out.println("Exception occurred during UDP/IP client interaction: " + se.getMessage());
		}
		catch(UnknownHostException uhe)
		{
			System.out.println("Exception occurred during UDP/IP client interaction: " + uhe.getMessage());
		}
		catch(IOException ioe)
		{
			System.out.println("Exception occurred during UDP/IP client interaction: " + ioe.getMessage());
		}
		finally
		{
			if (clientSocket != null)
				clientSocket.close();
		}
	}
}