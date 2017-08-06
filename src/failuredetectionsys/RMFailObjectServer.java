package failuredetectionsys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import replica1.servers.RMFailDetectUDPThread;

public class RMFailObjectServer {

	static RMFailDetectUDPThread rm1;
	static replica2.servers.RMFailDetectUDPThread rm2;
	static replica3.servers.RMFailDetectUDPThread rm3;

	public static void main(String[] args) {
		
		System.out.println("RMFailObject Started");

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					DatagramSocket serverSocket = null;
					byte[] buffer = new byte[1000];
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					try {
						// message = null;
						System.out.println("Here");
						serverSocket = new DatagramSocket(6501);
						serverSocket.receive(request);
						System.out.println("Here");
						buffer = request.getData();
						ByteArrayInputStream byteInput = new ByteArrayInputStream(buffer);
						ObjectInputStream obj = new ObjectInputStream(byteInput);
						Object obj1 = obj.readObject();
						obj.close();
						byteInput.close();
						System.out.println("Here");
						if (obj1 instanceof RMFailDetectUDPThread) {
							System.out.println("RM1 initialized");
							rm1 = (RMFailDetectUDPThread) obj1;
						} else if (obj1 instanceof replica2.servers.RMFailDetectUDPThread) {
							System.out.println("RM2 initialized");
							rm2 = (replica2.servers.RMFailDetectUDPThread) obj1;
						} else if (obj1 instanceof replica3.servers.RMFailDetectUDPThread) {
							System.out.println("RM3 initialized");
							rm3 = (replica3.servers.RMFailDetectUDPThread) obj1;
						}
						serverSocket.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					}
					serverSocket.close();
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					DatagramSocket serverSocket = null;
					byte[] buffer = new byte[1000];
					String message = null;
					byte[] replyMessage = null;
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					ObjectOutputStream os = null;
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					try {
						
						message = null;
						serverSocket = new DatagramSocket(6502);
						serverSocket.receive(request);
						System.out.println("request received in RMFailObject");
						message = new String(request.getData());
						os = new ObjectOutputStream(out);
						if (message.trim().equalsIgnoreCase("RM1")) {
							System.out.println("Request received for RM1");
							os.writeObject((Object)rm1);
							replyMessage = out.toByteArray();
						} else if (message.trim().equalsIgnoreCase("RM2")) {
							System.out.println("Request received for RM2");
							os.writeObject((Object)rm2);
							replyMessage = out.toByteArray();
						} else if (message.trim().equalsIgnoreCase("RM3")) {
							System.out.println("Request received for RM3");
							os.writeObject((Object)rm3);
							replyMessage = out.toByteArray();
						}
						DatagramPacket reply = new DatagramPacket(buffer, buffer.length, request.getAddress(),
								request.getPort());
						serverSocket.send(reply);
						os.flush();
						System.out.println("reply sent to RMFailObject");
						serverSocket.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

}
