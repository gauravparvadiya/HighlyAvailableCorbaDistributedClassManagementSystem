package replica2.servers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import failuredetectionsys.ReplicaInfo;
import frontend.services.ReplicaLeaderManager;
import replica1.services.FIFOBroadcastSys;
import replica1.services.ReplicaMgrService;

public class RMFailDetectUDPThread extends Thread implements Serializable {
	private  DatagramSocket serverSocket;
	private  int serverPort;
	private  ReplicaInfo info;
	private  Boolean statusOfRM1;
	private  Boolean statusOfRM3;
	private  String checkStatusOf;
	private  Thread t1;
	private  Thread t2;
	private  Thread t3;
	private  Thread t4;
	private  Thread t5;
	FIFOBroadcastSys sys = null;
	
	replica1.servers.RMFailDetectUDPThread rmFail1;
	replica3.servers.RMFailDetectUDPThread rmFail3;

	public RMFailDetectUDPThread(FIFOBroadcastSys sys) throws SocketException {
		this.sys = sys;
		serverSocket = null;
		serverPort = 6491;
		info = new ReplicaInfo(serverPort, "localhost", false);
		statusOfRM1 = true;
		statusOfRM3 = false;
		checkStatusOf = "";
	}
	
	@SuppressWarnings("deprecation")
	public void stopChildThread() {
		t1.stop();
		t2.stop();
		t3.stop();
		t4.stop();
		t5.stop();
	}
	 public void run() {
		 
		// RMFailDetectUDPThread rm2 = new RMFailDetectUDPThread();
			
			//ReplicaLeaderManager rl = new ReplicaLeaderManager();
			if (ReplicaLeaderManager.getWhoIsLeader().equals("RM2")) {
				info.setIsLeader(true);
			}
			
			List<String[]> secDetails = new ArrayList<String[]>();
			String[] svr1 = new String[2];
			svr1[0] = "localhost";
			svr1[1] = "6790";
			String[] svr2 = new String[2];
			svr2[0] = "localhost";
			svr2[1] = "6798";
			secDetails.add(svr1);
			secDetails.add(svr2);
			
			
			//FIFOBroadcastSys sys = new FIFOBroadcastSys();
			sys.setSecServerDetails(secDetails);
			
			t4 = new Thread(new Runnable() {
				Boolean status = true;

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (status) {
						try {
							System.out.println(checkStatusOf);
							if (checkStatusOf.equals("RM1")) {
								DatagramSocket socket = new DatagramSocket();
								byte[] message = "RM1".getBytes();
								InetAddress host = InetAddress.getByName("localhost");
								DatagramPacket request = new DatagramPacket(message, message.length, host, 6498);
								socket.send(request);

								byte[] replyMessage = new byte[1000];
								DatagramPacket reply = new DatagramPacket(replyMessage, replyMessage.length);
								socket.receive(reply);

								if (new String(reply.getData()).trim().equalsIgnoreCase("RM1 is live")) {
									statusOfRM1 = true;
								} else {
									// Restart RM2
									if (ReplicaLeaderManager.getWhoIsLeader().equals("RM1")) {
										info.setIsLeader(true);
										ReplicaLeaderManager.setWhoIsLeader("RM2");
										
										ReplicaMgrService.setIsLeader("leaderstatus_false");
										replica2.services.ReplicaMgrService.setIsLeader("leaderstatus_true");
										replica3.services.ReplicaMgrService.setIsLeader("leaderstatus_false");
										
										DatagramSocket socket1 = null;
										try {
											socket1 = new DatagramSocket();
											byte[] message1 = "leaderdetails_localhost_6794".getBytes();
											InetAddress host1 = InetAddress.getByName("localhost");
											DatagramPacket request1 = new DatagramPacket(message1, message1.length, host1, 6789);
											socket1.send(request1);
											socket1.close();
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
									}
									DatagramSocket socket1 = new DatagramSocket();
									byte[] requestMessage = "RM1".getBytes();
									DatagramPacket request1 = new DatagramPacket(requestMessage, requestMessage.length,
											host, 6502);
									socket1.send(request1);
									byte[] buffer = new byte[100];
									DatagramPacket receive = new DatagramPacket(buffer, buffer.length);
									socket1.receive(receive);
									ByteArrayInputStream in = new ByteArrayInputStream(buffer);
									ObjectInputStream is = new ObjectInputStream(in);
									rmFail1 = (replica1.servers.RMFailDetectUDPThread)is.readObject();
									
									rmFail1.stopChildThread();
									try {
										rmFail1.stop();
									} catch (ThreadDeath e) {
										System.out.println("RM1 stopped");
									}
									rmFail1.start();
								}
								socket.close();

							} else if (checkStatusOf.equals("RM3")) {
								// connect to RM2-T5

								DatagramSocket socket = new DatagramSocket();
								byte[] message = "RM3".getBytes();
								InetAddress host = InetAddress.getByName("localhost");
								DatagramPacket request = new DatagramPacket(message, message.length, host, 6499);
								socket.send(request);

								byte[] replyMessage = new byte[1000];
								DatagramPacket reply = new DatagramPacket(replyMessage, replyMessage.length);
								socket.receive(reply);
								System.out.println("here 1");
								if (new String(reply.getData()).trim().equalsIgnoreCase("RM3 is live")) {
									statusOfRM3 = true;
									System.out.println("here 2");
								} else {
									DatagramSocket socket1 = new DatagramSocket();
									byte[] requestMessage = "RM3".getBytes();
									DatagramPacket request1 = new DatagramPacket(requestMessage, requestMessage.length,
											host, 6502);
									socket1.send(request1);
									byte[] buffer = new byte[100];
									DatagramPacket receive = new DatagramPacket(buffer, buffer.length);
									socket1.receive(receive);
									ByteArrayInputStream in = new ByteArrayInputStream(buffer);
									ObjectInputStream is = new ObjectInputStream(in);
									rmFail3 = (replica3.servers.RMFailDetectUDPThread)is.readObject();
									rmFail3.stopChildThread();
									try {
										rmFail3.stop();
									} catch (ThreadDeath e) {
										System.out.println("RM3 stopped");
									}
									rmFail3.start();
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
			t1 = new Thread(new Runnable() {
				@Override
				public void run() {
					// DatagramSocket serverSocket = null;
					try {
						while (true) {
							sleep(5000);
							serverSocket = new DatagramSocket();
							byte[] message = "RM2 is live".getBytes();
							InetAddress host = InetAddress.getByName("localhost");

							DatagramPacket request = new DatagramPacket(message, message.length, host, 6493);
							serverSocket.send(request);
							serverSocket.close();

							serverSocket = new DatagramSocket();
							request = new DatagramPacket(message, message.length, host, 6494);
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
			});
			t1.start();

			// Thread to get status of RM1.
			t2 = new Thread(new Runnable() {
				@Override
				public void run() {

					DatagramSocket serverSocket = null;
					DatagramPacket request = null;
					String message = null;

					while (true) {
						message = null;

						try {
							serverSocket = new DatagramSocket(6491);
							byte[] buffer = new byte[1000];
							request = new DatagramPacket(buffer, buffer.length);
							serverSocket.setSoTimeout(10000);
							serverSocket.receive(request);
							System.out.println(new String(request.getData()));
							message = new String(request.getData());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

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
			});
			t2.start();

			// Thread to get status of RM3.
			t3 = new Thread(new Runnable() {
				@Override
				public void run() {
					DatagramSocket serverSocket = null;
					DatagramPacket request = null;
					String message = null;

					while (true) {
						try {
							serverSocket = new DatagramSocket(6496);
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
								t4.start();
							}
						}
						serverSocket.close();
					}
				}
			});
			t3.start();

			if (!info.getIsLeader()) {
				t5 = new Thread(new Runnable() {

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
								serverSocket = new DatagramSocket(6497);
								serverSocket.setSoTimeout(10000);
								serverSocket.receive(request);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

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
								if (statusOfRM1) {
									replyMessage = "RM1 is live - T5";
								} else {
									replyMessage = "RM1 is failed - T5";
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
				});
				t5.start();
			}
		 
	 }
}