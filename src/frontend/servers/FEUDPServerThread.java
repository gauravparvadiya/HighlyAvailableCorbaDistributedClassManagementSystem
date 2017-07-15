package frontend.servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import frontend.services.RecordManagerFEImpl;

/**
 * Front End's UDP server class for communicating with Failure Detection System.
 * @author Jyotsana Gupta
 */
public class FEUDPServerThread extends Thread 
{
	private final int SERVER_PORT = 6789;
	private RecordManagerFEImpl recMgrFE;
	
	/**
	 * Constructor with the attribute values provided as parameters.
	 * @param 	serverPort	Port number of this server 
	 * @param 	recMgr		Object of the remote interface implementation
	 */
	public FEUDPServerThread(RecordManagerFEImpl recMgrFE)
	{
		this.recMgrFE = recMgrFE;
	}
	
	/**
	 * Receives and responds to requests from other system components using UDP/IP socket communication.
	 */
	public void run()
	{
		DatagramSocket serverSocket = null;
		try
		{
			serverSocket = new DatagramSocket(SERVER_PORT);
			
			while (true)
			{
				byte[] requestMsg = new byte[1000];
				DatagramPacket requestPacket = new DatagramPacket(requestMsg, requestMsg.length);
				serverSocket.receive(requestPacket);
				
				String requestStr = new String(requestMsg);
				if (requestStr.trim().indexOf("leaderdetails") == 0)
					recMgrFE.setLeadServerDetails(requestStr);
			}
		}
		catch(SocketException se)
		{
			System.out.println("Exception occurred during FE UDP server interaction: " + se.getMessage());
		}
		catch (IOException ioe)
		{
			System.out.println("Exception occurred during FE UDP server interaction: " + ioe.getMessage());
		}
		finally
		{
			if (serverSocket != null)
				serverSocket.close();
		}
	}
}