package replica3.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import replica3.entities.Record;

public class RecTransferClientThread extends Thread {
	private String status;
	private String host;
	private int port;
	private Record rec;

	public RecTransferClientThread(Record rec, String host, int port) {
		this.rec = rec;
		this.host = host;
		this.port = port;
	}

	public String getStatus() {
		return status;
	}

	public void run() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			InetAddress addr = InetAddress.getByName(host);
			int portNum = port;
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(rec);
			byte[] reqMsg = bo.toByteArray();
			oo.close();
			bo.close();
			DatagramPacket outPack = new DatagramPacket(reqMsg, reqMsg.length, addr, portNum);
			socket.send(outPack);

			byte[] repMsg = new byte[1000];
			DatagramPacket inPack = new DatagramPacket(repMsg, repMsg.length);
			socket.receive(inPack);
			status = new String(repMsg).trim();
		} catch (SocketException se) {
			System.out.println("Exception in RecTransferClientThread: " + se.getMessage());
			status = "Transfer record failed";
		} catch (UnknownHostException uhe) {
			System.out.println("Exception in RecTransferClientThread: " + uhe.getMessage());
			status = "Transfer record failed";
		} catch (IOException ioe) {
			System.out.println("Exception in RecTransferClientThread: " + ioe.getMessage());
			status = "Transfer record failed";
		} finally {
			if (socket != null)
				socket.close();
		}
	}
}