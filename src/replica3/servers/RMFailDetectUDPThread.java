package replica3.servers;

import java.io.IOException;
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
	private static Boolean statusOfRM1;
	private static String checkStatusOf;

	public RMFailDetectUDPThread() throws SocketException {
		serverSocket = null;
		serverPort = 6492;
		info = new ReplicaInfo(serverPort, "localhost", false);
		statusOfRM2 = true;
		statusOfRM1 = true;
		checkStatusOf = "";
	}

	public static void main(String[] args) throws SocketException {

		RMFailDetectUDPThread rm3 = new RMFailDetectUDPThread();

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
							DatagramPacket request = new DatagramPacket(message, message.length, host, 6499);
							socket.send(request);

							byte[] replyMessage = new byte[1000];
							DatagramPacket reply = new DatagramPacket(replyMessage, replyMessage.length);
							socket.receive(reply);

							if (new String(reply.getData()).trim().equalsIgnoreCase("RM2 is live")) {
								statusOfRM1 = true;
							} else {
								// Restart RM2
								System.out.println("Restart RM2");
							}
							socket.close();

						} else if (checkStatusOf.equals("RM1")) {
							// connect to RM2-T5

							DatagramSocket socket = new DatagramSocket();
							byte[] message = "RM1".getBytes();
							InetAddress host = InetAddress.getByName("localhost");
							DatagramPacket request = new DatagramPacket(message, message.length, host, 6497);
							socket.send(request);

							byte[] replyMessage = new byte[1000];
							DatagramPacket reply = new DatagramPacket(replyMessage, replyMessage.length);
							socket.receive(reply);
							System.out.println("here 1");
							if (new String(reply.getData()).trim().equalsIgnoreCase("RM1 is live")) {
								statusOfRM1 = true;
								System.out.println("here 2");
							} else {
								// Restart RM2
								System.out.println("here 3");
								System.out.println("Restart RM1");
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
				DatagramPacket request = null;
				String message = null;

				byte[] buffer = new byte[1000];

				while (true) {
					message = null;
					try {
						serverSocket = new DatagramSocket(6494);
						request = new DatagramPacket(buffer, buffer.length);
						serverSocket.setSoTimeout(10000);
						serverSocket.receive(request);
						System.out.println(new String(request.getData()));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					message = new String(request.getData());
					if (message.equals("RM2 is live")) {
						statusOfRM2 = true;
					} else {
						statusOfRM2 = false;
						if (info.getIsLeader()) {
							// start T4
							checkStatusOf = "RM2";
							t4.start();
						} else {

						}
					}
					serverSocket.close();
				}

			}
		}).start();

		// Thread to get status of RM1.
		new Thread(new Runnable() {
			@Override
			public void run() {

				DatagramSocket serverSocket = null;
				DatagramPacket request = null;
				String message = null;

				while (true) {
					message = null;
					try {
						serverSocket = new DatagramSocket(6492);
						byte[] buffer = new byte[1000];
						request = new DatagramPacket(buffer, buffer.length);
						serverSocket.setSoTimeout(10000);
						serverSocket.receive(request);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					System.out.println(new String(request.getData()));
					message = new String(request.getData());
					if (message.equals("RM1 is live")) {
						statusOfRM1 = true;
					} else {
						statusOfRM1 = false;
						if (info.getIsLeader()) {
							// start T4
							checkStatusOf = "RM1";
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
					byte[] buffer = new byte[1000];
					while (true) {
						String message = null;
						String replyMessage = null;
						DatagramPacket request = new DatagramPacket(buffer, buffer.length);
						try {
							message = null;
							serverSocket = new DatagramSocket(6498);
							serverSocket.setSoTimeout(10000);
							serverSocket.receive(request);
						} catch (Exception e) {
							e.printStackTrace();
						}

						System.out.println(new String(request.getData()));
						message = new String(request.getData());
						if (message.equals("RM2")) {
							if (statusOfRM2) {
								replyMessage = "RM2 is live";
							} else {
								replyMessage = "RM2 is failed";
							}
						} else {
							if (statusOfRM1) {
								replyMessage = "RM1 is live";
							} else {
								replyMessage = "RM1 is failed";
							}
						}
						buffer = replyMessage.getBytes();
						DatagramPacket reply = new DatagramPacket(buffer, buffer.length, request.getAddress(),
								request.getPort());
						try {
							serverSocket.send(reply);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						serverSocket.close();
					}
				}
			}).start();
		}
	}
}