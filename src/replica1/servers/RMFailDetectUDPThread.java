package replica1.servers;

import java.net.DatagramSocket;

public class RMFailDetectUDPThread extends Thread
{
	private DatagramSocket serverSocket;
	private int serverPort;
	
	public RMFailDetectUDPThread(int serverPort)
	{
		serverSocket = null;
		this.serverPort = serverPort;
	}
	
	public void run()
	{
		//TODO code for communicating with failure detection system
	}
}