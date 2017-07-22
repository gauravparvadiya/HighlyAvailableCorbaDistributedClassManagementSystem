package replica3.servers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import replica.info.ReplicaInfo;

public class RMFailDetectUDPThread extends Thread
{
	private static DatagramSocket serverSocket;
	private static int serverPort;
	private static ReplicaInfo info;
	private static Boolean statusOfRM2;
	private static Boolean statusOfRM1;

	public RMFailDetectUDPThread() throws SocketException {
		serverSocket = null;
		serverPort = 6492;
		info = new ReplicaInfo(serverPort, "localhost", false);
		statusOfRM2 = true;
		statusOfRM1 = true;
	}

	// public void run() {
	//
	//
	// }

	public static void main(String[] args) throws SocketException {

		RMFailDetectUDPThread rm3 = new RMFailDetectUDPThread();
		
		// Thread to send RM1 status to RM2 and RM3 at every 10ms.
		new Thread(new Runnable() {
			@Override
			public void run() {
				//DatagramSocket serverSocket = null;
				try {
					while (true) {
						sleep(10);
						serverSocket = new DatagramSocket();
						byte[] message = "RM3 is live".getBytes();
						InetAddress host = InetAddress.getByName("localhost");

						DatagramPacket request = new DatagramPacket(message, message.length, host, 6495);
						serverSocket.send(request);
						serverSocket.close();

						serverSocket = new DatagramSocket();
						request = new DatagramPacket(message, message.length, host, 6496);
						serverSocket.send(request);
						serverSocket.close();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (serverSocket != null) {
						serverSocket.close();
					}
				}
			}
		}).start();

		// Thread to get status of RM2.
		new Thread(new Runnable() {
			@Override
			public void run() {
				DatagramSocket serverSocket = null;
				try {
					serverSocket = new DatagramSocket(6494);
					byte[] buffer = new byte[1000];

					while (true) {
						String message = null;
						DatagramPacket request = new DatagramPacket(buffer, buffer.length);
						serverSocket.setSoTimeout(10000);
						serverSocket.receive(request);
						System.out.println("Thread 2");
						System.out.println(new String(request.getData()));
						message = new String(request.getData());
						if (message.equals("RM2 is live")) {
							statusOfRM2 = true;
						} else {
							statusOfRM2 = false;
							if (info.getIsLeader()) {
								// start T4
							} else {
								
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (serverSocket != null) {
						serverSocket.close();
					}
				}
			}
		}).start();

		// Thread to get status of RM1.
		new Thread(new Runnable() {
			@Override
			public void run() {
				DatagramSocket serverSocket = null;
				try {
					serverSocket = new DatagramSocket();
					byte[] buffer = new byte[1000];

					while (true) {
						String message = null;
						DatagramPacket request = new DatagramPacket(buffer, buffer.length);
						serverSocket.setSoTimeout(10000);
						serverSocket.connect(InetAddress.getByName("localhost"), 6492);
						serverSocket.receive(request);
						System.out.println("Thread 3");
						System.out.println(new String(request.getData()));
						message = new String(request.getData());
						if (message.equals("RM1 is live")) {
							statusOfRM1 = true;
						} else {
							statusOfRM1 = false;
							if (info.getIsLeader()) {
								// start T4
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (serverSocket != null) {
						serverSocket.close();
					}
				}
			}
		}).start();
	}
}