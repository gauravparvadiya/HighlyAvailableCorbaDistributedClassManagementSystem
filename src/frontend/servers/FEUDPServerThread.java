package frontend.servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import frontend.services.RecordManagerFEImpl;

public class FEUDPServerThread extends Thread 
{
	private DatagramSocket serverSocket;
	private int serverPort;
	private RecordManagerFEImpl recMgrFE;
	
	/**
	 * Constructor with the attribute values provided as parameters.
	 * @param 	serverPort	Port number of this server 
	 * @param 	recMgr		Object of the remote interface implementation
	 */
	public FEUDPServerThread(RecordManagerFEImpl recMgrFE)
	{
		serverSocket = null;
		this.serverPort = 6789;
		this.recMgrFE = recMgrFE;
	}
	
	/**
	 * Receives and responds to requests from other system components using UDP/IP socket communication.
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
				if (requestStr.trim().indexOf("leader") == 0)
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