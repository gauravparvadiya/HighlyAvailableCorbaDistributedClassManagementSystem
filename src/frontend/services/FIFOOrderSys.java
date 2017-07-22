package frontend.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

import frontend.entities.Request;

public class FIFOOrderSys {
	private LinkedList<Request> queue = new LinkedList<Request>();

	public boolean addRequest(Request newRequest) {
		queue.add(newRequest);
		return true;
	}

	public String sendFirstRequest(String leadServerHostname, int leadServerPort) {
		if (!queue.isEmpty()) {
			try {
				DatagramSocket socket = new DatagramSocket();
				byte[] message = queue.getFirst().toString().getBytes();
				InetAddress host = InetAddress.getByName(leadServerHostname);
				DatagramPacket request = new DatagramPacket(message, message.length, host, leadServerPort);
				socket.send(request);

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return "Request sent";
		} else
			return "Error sending request";
	}

	public void removeFirstRequest() {
		queue.removeFirst();
	}
}
