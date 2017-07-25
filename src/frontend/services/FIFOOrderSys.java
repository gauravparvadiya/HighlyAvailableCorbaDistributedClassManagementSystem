package frontend.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;

import frontend.entities.Request;

/**
 * FIFO ordering defining methods to send and remove requests.
 * @author Hirangi Naik
 */
public class FIFOOrderSys {
	private LinkedList<Request> queue = new LinkedList<Request>();

	public boolean addRequest(Request newRequest) {
		queue.add(newRequest);
		return true;
	}

	public String sendFirstRequest(String leadServerHostname, int leadServerPort) {
		if (!queue.isEmpty()) {
			String msg;
			try {
				DatagramSocket socket = new DatagramSocket();
				byte[] message = queue.getFirst().toString().getBytes();
				InetAddress host = InetAddress.getByName(leadServerHostname);
				DatagramPacket request = new DatagramPacket(message, message.length, host, leadServerPort);
				socket.send(request);
				socket.close();
			} 
			catch(SocketException se)
			{
				msg="Error sending request!";
				System.out.println("Exception occurred during server interaction: " + se.getMessage());
				return msg;
			}
			catch (IOException ioe)
			{
				msg="Error sending request!";
				System.out.println("Exception occurred during server interaction: " + ioe.getMessage());
				return msg;
			}
			return "Request sent";
		} else
			return "Error sending request";
	}

	public void removeFirstRequest() {
		queue.removeFirst();
	}
}
