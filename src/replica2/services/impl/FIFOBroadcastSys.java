package replica2.services.impl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import replica2.entities.Request;

/**
 * FIFO broadcast defining methods to broadcast request, get and set secondary server details.
 * @author Hirangi Naik
 */

public class FIFOBroadcastSys {

	private String[] hostname;

	private int[] portno;

	/**
	 * To broadcast the request to secondary servers
	 * @param newRequest
	 */
	public void broadcastRequest(Request newRequest) {
		String[] secServerDetail=getSecServerDetails();
		try {
			DatagramSocket socket = new DatagramSocket();
			byte[] message = newRequest.toString().getBytes();
			InetAddress host = InetAddress.getByName(secServerDetail[0]);
			DatagramPacket request = new DatagramPacket(message, message.length, host, Integer.parseInt(secServerDetail[1]));
			socket.send(request);
			socket.close();
			
			DatagramSocket socket2 = new DatagramSocket();
			byte[] message2 = newRequest.toString().getBytes();
			InetAddress host2 = InetAddress.getByName(secServerDetail[2]);
			DatagramPacket request2 = new DatagramPacket(message2, message2.length, host2, Integer.parseInt(secServerDetail[3]));
			socket2.send(request2);
			socket2.close();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Set secondary sever details
	 * 
	 * @param secServerDetails
	 *            secondary server details
	 */
	public void setSecServerDetails(List<String[]> secServerDetails) {
		String[] secDetails1 = secServerDetails.get(0).toString().trim().split("_");
		String[] secDetails2 = secServerDetails.get(1).toString().trim().split("_");
		synchronized (this) {
			this.hostname[0] = secDetails1[1].trim();
			this.portno[0] = Integer.parseInt(secDetails1[2].trim());
			this.hostname[1] = secDetails2[1].trim();
			this.portno[1] = Integer.parseInt(secDetails2[2].trim());
		}
	}

	/**
	 * Fetches the value of secondary server details
	 * 
	 * @return secodary servers detail
	 */
	public String[] getSecServerDetails() {
		String[] secDetails = new String[4];
		synchronized (this) {
			secDetails[0] = hostname[0];
			secDetails[1] = portno[0] + "";
			secDetails[2] = hostname[1];
			secDetails[3] = portno[1] + "";
		}
		return secDetails;
	}
}