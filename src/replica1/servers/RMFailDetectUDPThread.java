package replica1.servers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import failuredetectionsys.ReplicaInfo;
import frontend.services.ReplicaLeaderManager;

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

		ReplicaLeaderManager rl = new ReplicaLeaderManager();
		if (rl.getWhoIsLeader().equals("RM1")) {
			info.setIsLeader(true);
		}
		
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
								replica2.servers.RMFailDetectUDPThread.main(null);
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
							System.out.println("here 1");
							if (new String(reply.getData()).trim().equalsIgnoreCase("RM3 is live")) {
								statusOfRM3 = true;
								System.out.println("here 2");
							} else {
								// Restart RM2
								replica3.servers.RMFailDetectUDPThread.main(null);
								System.out.println("here 3");
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

				while (true) {

					try {
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
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					serverSocket.close();
				}
			}
		}).start();

		// Thread to get status of RM2.
		new Thread(new Runnable() {
			@Override
			public void run() {
				DatagramSocket serverSocket = null;
				byte[] buffer = new byte[1000];

				while (true) {
					String message = null;
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					try {
						message = null;
						serverSocket = new DatagramSocket(6493);
						serverSocket.setSoTimeout(10000);
						serverSocket.receive(request);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					message = new String(request.getData());
					System.out.println(message);
					if (message.trim().equalsIgnoreCase("RM2 is live")) {
						statusOfRM2 = true;
					} else {
						statusOfRM2 = false;
						if (info.getIsLeader()) {
							// start T4
							checkStatusOf = "RM2";
							t4.start();
						}
					}
					serverSocket.close();
				}

			}
		}).start();

		// Thread to get status of RM3.
		new Thread(new Runnable() {
			@Override
			public void run() {
				DatagramSocket serverSocket = null;
				DatagramPacket request = null;
				String message = null;

				while (true) {
					try {
						serverSocket = new DatagramSocket(6495);
						message = null;
						byte[] buffer = new byte[1000];
						request = new DatagramPacket(buffer, buffer.length);
						serverSocket.setSoTimeout(10000);
						serverSocket.receive(request);
					} catch (Exception e) {
						e.printStackTrace();
					}

					System.out.println(new String(request.getData()));
					message = new String(request.getData());
					if (message.trim().equalsIgnoreCase("RM3 is live")) {
						statusOfRM3 = true;
					} else {
						statusOfRM3 = false;
						if (info.getIsLeader()) {
							// start T4
							checkStatusOf = "RM3";
							t4.start();
						}
					}
					serverSocket.close();
				}
			}
		}).start();

		if (!info.getIsLeader()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					DatagramSocket serverSocket = null;
					try {
						serverSocket = new DatagramSocket(6499);
						byte[] buffer = new byte[1000];
						while (true) {
							String message = null;
							String replyMessage = null;
							DatagramPacket request = new DatagramPacket(buffer, buffer.length);
							serverSocket.setSoTimeout(10000);
							serverSocket.receive(request);
							System.out.println(new String(request.getData()));
							message = new String(request.getData());
							if (message.trim().equalsIgnoreCase("RM3")) {
								if (statusOfRM3) {
									System.out.println("in thread 5");
									replyMessage = "RM3 is live";
								} else {
									System.out.println("in thread 5");
									replyMessage = "RM3 is failed";
								}
							} else {
								if (statusOfRM2) {
									replyMessage = "RM2 is live";
								} else {
									replyMessage = "RM2 is failed";
								}
							}
							buffer = replyMessage.getBytes();
							DatagramPacket reply = new DatagramPacket(buffer, buffer.length, request.getAddress(),
									request.getPort());
							serverSocket.send(reply);
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
}