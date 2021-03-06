package replica1.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import replica1.servers.RMFailDetectUDPThread;

/**
 * UDP client class for Replica Manager.
 * @author Jyotsana Gupta
 */
public class ReplicaMgrUDPClient implements Serializable
{
	private RMFailDetectUDPThread rmFailDetectThread;
	private String hostname;
	private int port;
	
	/**
	 * Constructor with all the attribute values provided as parameters.
	 * @param 	rmFailDetectThread	The failure detection thread object to be transferred
	 * @param 	hostname			Hostname of the server contacted by this client
	 * @param 	port				Port number of the server contacted by this client
	 */
	public ReplicaMgrUDPClient(RMFailDetectUDPThread rmFailDetectThread, String hostname, int port)
	{
		this.rmFailDetectThread = rmFailDetectThread;
		this.hostname = hostname;
		this.port = port;
	}
	
	/**
	 * Sends the failure detection thread object to the failure detection system over UDP.
	 */
	public void sendFailDetectThread()
	{
		DatagramSocket clientSocket = null;
		
		try
		{
			clientSocket = new DatagramSocket();
			InetAddress serverAddr = InetAddress.getByName(hostname);
			int serverPort = port;
			System.out.println("Here1");
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			ObjectOutputStream objOutput = new ObjectOutputStream(byteOutput);
			System.out.println("Here2");
			objOutput.writeObject(rmFailDetectThread);
			byte[] objReqMsg = byteOutput.toByteArray();
			System.out.println("Here3");
			objOutput.close();
			byteOutput.close();
			
			DatagramPacket requestPacket = new DatagramPacket(objReqMsg, objReqMsg.length, serverAddr, serverPort);
			clientSocket.send(requestPacket);			
		}
		catch(SocketException se)
		{
			se.printStackTrace();
			System.out.println("Exception occurred while sending failure detection thread from RM UDP/IP client: " + se.getMessage());
		}
		catch(UnknownHostException uhe)
		{
			uhe.printStackTrace();
			System.out.println("Exception occurred while sending failure detection thread from RM UDP/IP client: " + uhe.getMessage());
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			System.out.println("Exception occurred while sending failure detection thread from RM UDP/IP client: " + ioe.getMessage());
		}
		finally
		{
			if (clientSocket != null)
				clientSocket.close();
		}
	}
}