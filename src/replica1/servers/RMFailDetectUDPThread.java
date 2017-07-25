package replica1.servers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import replica.info.ReplicaInfo;

public class RMFailDetectUDPThread extends Thread {
	private static DatagramSocket serverSocket;
	private static int serverPort;
	private static ReplicaInfo info;
	private static Boolean statusOfRM2;
	private static Boolean statusOfRM3;
	private static String checkStatusOf;

	public RMFailDetectUDPThread() throws SocketException {
		// serverSocket = null;
		serverPort = 6490;
		info = new ReplicaInfo(serverPort, "localhost", true);
		statusOfRM2 = true;
		statusOfRM3 = true;
		checkStatusOf = "";
	}

	// public void run() {
	//
	//
	// }

	public static void main(String[] args) throws SocketException {

		RMFailDetectUDPThread rm1 = new RMFailDetectUDPThread();

		Thread t4 = new Thread(new Runnable() {
			Boolean status = true;

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (status) {
					try {
						System.out.println(checkStatusOf);
						if (checkStatusOf.equals("RM2")) {
							DatagramSocket socket = new DatagramSocket();
							byte[] message = "RM2".getBytes();
							InetAddress host = InetAddress.getByName("localhost");
							DatagramPacket request = new DatagramPacket(message, message.length, host, 6498);
							socket.send(request);

							byte[] replyMessage = new byte[1000];
							DatagramPacket reply = new DatagramPacket(replyMessage, replyMessage.length);
							socket.receive(reply);

							if (new String(reply.getData()).trim().equalsIgnoreCase("RM2 is live")) {
								statusOfRM2 = true;
							} else {
								// Restart RM2
								System.out.println("Restart RM2");
							}
							socket.close();

						} else if (checkStatusOf.equals("RM3")) {
							// connect to RM2-T5

							DatagramSocket socket = new DatagramSocket();
							byte[] message = "RM3".getBytes();
							InetAddress host = InetAddress.getByName("localhost");
							DatagramPacket request = new DatagramPacket(message, message.length, host, 6497);
							socket.send(request);

							byte[] replyMessage = new byte[1000];
							DatagramPacket reply = new DatagramPacket(replyMessage, replyMessage.length);
							socket.receive(reply);

							if (new String(reply.getData()).trim().equalsIgnoreCase("RM3 is live")) {
								statusOfRM3 = true;
							} else {
								// Restart RM2
								System.out.println("Restart RM3");
							}
							socket.close();
						}
					} catch (Exception e) {
						// TODO: handle exception
					}

					status = false;
				}

			}
		});

		// Thread to send RM1 status to RM2 and RM3 at every 10ms.
		new Thread(new Runnable() {
			@Override
			public void run() {
				// DatagramSocket serverSocket = null;
				try {
					while (true) {
						sleep(5000);
						serverSocket = new DatagramSocket();
						byte[] message = "RM1 is live".getBytes();
						InetAddress host = InetAddress.getByName("localhost");

						DatagramPacket request = new DatagramPacket(message, message.length, host, 6491);
						serverSocket.send(request);
						serverSocket.close();

						serverSocket = new DatagramSocket();
						request = new DatagramPacket(message, message.length, host, 6492);
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
					System.out.println("RM1 - Check status of RM2");
					serverSocket = new DatagramSocket(6493);
					byte[] buffer = new byte[1000];

					while (true) {
						String message = null;
						DatagramPacket request = new DatagramPacket(buffer, buffer.length);
						serverSocket.setSoTimeout(10000);
						serverSocket.receive(request);
						System.out.println("RM1 - Check status of RM2 - Request Received");

						message = new String(request.getData());
						System.out.println(message);
						if (message.trim().equalsIgnoreCase("RM2 is live")) {
							statusOfRM2 = true;
							System.out.println("RM1 - Check status of RM2 - Live");
						} else {
							System.out.println("RM1 - Check status of RM2 - Failed");
							statusOfRM2 = false;
							if (info.getIsLeader()) {
								// start T4
								checkStatusOf = "RM2";
								t4.start();
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

		// Thread to get status of RM3.
		new Thread(new Runnable() {
			@Override
			public void run() {
				DatagramSocket serverSocket = null;
				DatagramPacket request = null;
				byte[] buffer = new byte[1000];

				while (true) {
					String message = null;
					try {
						serverSocket = new DatagramSocket(6495);
						request = new DatagramPacket(buffer, buffer.length);
						serverSocket.setSoTimeout(10000);
						serverSocket.receive(request);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						serverSocket.close();
					}
					
					System.out.println("RM1 - Check status of RM3 - Request received");
					System.out.println(new String(request.getData()));
					message = new String(request.getData());
					if (message.trim().equalsIgnoreCase("RM3 is live")) {
						statusOfRM3 = true;
						System.out.println("RM1 - Check status of RM3 - Live");
					} else {
						statusOfRM3 = false;
						System.out.println("RM1 - Check status of RM3 - Failed");
						if (info.getIsLeader()) {
							checkStatusOf = "RM3";
							System.out.println("RM1 - Check status of RM3 - start of Thread 4");
							t4.start();
						}
					}
				}
			}
		}).start();

	}
}