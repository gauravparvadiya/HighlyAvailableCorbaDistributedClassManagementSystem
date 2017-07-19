package replica1.servers;

import java.net.DatagramSocket;

public class RMFailDetectUDPThread extends Thread
{
	private DatagramSocket serverSocket;
	private int serverPort;
	
	public RMFailDetectUDPThread()
	{
		serverSocket = null;
		serverPort = -1;
	}
	
	public void run()
	{
		//TODO code for communicating with failure detection system
	}
}