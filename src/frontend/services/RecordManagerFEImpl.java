package frontend.services;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import failuredetectionsys.RMFailObjectServer;
import frontend.corbasupport.RecordManagerApp.RecordManagerPOA;
import frontend.entities.Request;
import frontend.servers.FEUDPServerThread;
import replica1.servers.RMFailDetectUDPThread;

/**
 * Front end remote interface implementation defining methods for initiating student-teacher record management.
 * @author Jyotsana Gupta
 * @author Gauravkumar Parvadiya
 */
public class RecordManagerFEImpl extends RecordManagerPOA implements Serializable
{
	/**
	 * Instance of FIFOOrderSys for invoking FIFO total request ordering methods.
	 */
	private FIFOOrderSys fifoOrdSys;

	/**
	 * Lead server host name.
	 */
	private String leadServerHostname = null;

	/**
	 * Lead server port number.
	 */
	private int leadServerPort = -1;

	/**
	 * Object used as a lock for lead server UDP connection details update and access.
	 */
	private final Object leadUDPDtlsLock = new Object();

	/**
	 * Default (unparameterized) constructor.
	 */
	
	RMFailDetectUDPThread rm1;
	replica2.servers.RMFailDetectUDPThread rm2;
	replica3.servers.RMFailDetectUDPThread rm3;
	
	public RecordManagerFEImpl() 
	{		
		//Launching FIFO ordering system
		fifoOrdSys = new FIFOOrderSys();

		//Launching thread for front end UDP/IP communication
		FEUDPServerThread feUDPServThread = new FEUDPServerThread(this);
		feUDPServThread.start();
	}

	/**
	 * Updates the latest hostname and port number of the current leader server.
	 * @param	leadServerDetails	String containing the leader hostname and port number
	 */
	public void setLeadServerDetails(String leadServerDetails)
	{
		String[] leadDetails = leadServerDetails.trim().split("_");

		synchronized (leadUDPDtlsLock) 
		{
			leadServerHostname = leadDetails[1].trim();
			leadServerPort = Integer.parseInt(leadDetails[2].trim());
		}
	}

	/**
	 * Communicates a client request to create a new teacher record to the lead server via the FIFO System.
	 * @param	mgrID			Unique ID of the center manager who performs this operation
	 * @param 	firstName		First name of the teacher
	 * @param 	lastName		Last name of the teacher
	 * @param 	address			Address of the teacher
	 * @param 	phone			Phone number of the teacher
	 * @param 	specialization	Subject that the teacher specializes in (e.g. French, Math, etc.)
	 * @param 	location		Location of the teacher (e.g. MTL, LVL, etc.)
	 * @return 	Success or failure status message of the operation
	 */
	public String createTRecord(String mgrID, String firstName, String lastName, String address, String phone,
								String specialization, String location) 
	{
		String methodName = "createTRecord";

		List<String> methodArgs = new ArrayList<String>();
		methodArgs.add(mgrID);
		methodArgs.add(firstName);
		methodArgs.add(lastName);
		methodArgs.add(address);
		methodArgs.add(phone);
		methodArgs.add(specialization);
		methodArgs.add(location);
		
		String opStatus = processRequest(methodName, methodArgs);		
		return opStatus;
	}

	/**
	 * Communicates a client request to create a new student record to the lead server via the FIFO System.
	 * @param 	mgrID				Unique ID of the center manager who performs this operation
	 * @param 	firstName			First name of the student
	 * @param 	lastName			Last name of the student
	 * @param 	coursesRegistered	List of courses that the student has registered for (e.g. French, Math, etc.)
	 * @param 	status				Status of the student (active/inactive)
	 * @param 	statusDate			Date of last status update of the student
	 * @return 	Success or failure status message of the operation
	 */
	public String createSRecord(String mgrID, String firstName, String lastName, String coursesRegistered,
								String status, String statusDate) 
	{
		String methodName = "createSRecord";

		List<String> methodArgs = new ArrayList<String>();
		methodArgs.add(mgrID);
		methodArgs.add(firstName);
		methodArgs.add(lastName);
		methodArgs.add(coursesRegistered);
		methodArgs.add(status);
		methodArgs.add(statusDate);

		String opStatus = processRequest(methodName, methodArgs);
		return opStatus;
	}

	/**
	 * Communicates a client request to fetch server record counts to the lead server via the FIFO System.
	 * @param 	mgrID	Unique ID of the center manager who performs this operation
	 * @return 	Record counts of all center servers, if successful 
	 * 			Failure status message, otherwise
	 */
	public String getRecordCounts(String mgrID) 
	{
		String methodName = "getRecordCounts";

		List<String> methodArgs = new ArrayList<String>();
		methodArgs.add(mgrID);

		String recCount = processRequest(methodName, methodArgs);
		return recCount;
	}

	/**
	 * Communicates a client request to edit an existing record to the lead server via the FIFO System.
	 * @param 	mgrID		Unique ID of the center manager who performs this operation
	 * @param 	recordID	Unique ID for identifying the record to be updated
	 * @param 	fieldName	Name of the field in the record to be updated
	 * @param 	newValue	New value to be assigned to the field
	 * @return 	Success or failure status message of the operation
	 */
	public String editRecord(String mgrID, String recordID, String fieldName, String newValue) 
	{
		String methodName = "editRecord";

		List<String> methodArgs = new ArrayList<String>();
		methodArgs.add(mgrID);
		methodArgs.add(recordID);
		methodArgs.add(fieldName);
		methodArgs.add(newValue);

		String opStatus = processRequest(methodName, methodArgs);
		return opStatus;
	}

	/**
	 * Communicates a client request to transfer a record to another Center Server to the lead server via the FIFO System.
	 * @param 	mgrID			Unique ID of the center manager who performs this operation
	 * @param 	recordID		Unique ID for identifying the record to be transferred
	 * @param 	remServerName	Acronym for identifying the destination server
	 * @return 	Success or failure status message of the operation
	 */
	public String transferRecord(String mgrID, String recordID, String remServerName) 
	{
		String methodName = "transferRecord";

		List<String> methodArgs = new ArrayList<String>();
		methodArgs.add(mgrID);
		methodArgs.add(recordID);
		methodArgs.add(remServerName);

		String opStatus = processRequest(methodName, methodArgs);
		return opStatus;
	}
	
	/**
	 * Simulates a Center Server crash scenario.
	 * @param 	mgrID	Center Manager ID to identify the Center Server to be crashed
	 * @return	Success or failure status message of the dummy request performed during the crash
	 */
	public String crashCenterServer(String mgrID)
	{
		String methodName = "crashCenterServer";
		List<String> methodArgs = new ArrayList<String>();
		methodArgs.add(mgrID);
		
		String opStatus = processRequest(methodName, methodArgs);
		return opStatus;
	}

	/**
	 * Shuts down the lead server process to simulate a crash.
	 */
	public String crashLeadServer() 
	{
		System.out.println("in Crash Lead");
		
		if (RMFailObjectServer.getRm1() != null) {
			System.out.println("Data available.");
		}
		
		rm1 = RMFailObjectServer.getRm1();
		if (rm1 != null) {
			System.out.println("object not null.");
		}
		System.out.println("rm1 null");
//		rm1.stopChildThread();
//		rm1.stop();
		return "Lead Crashed";
//		try {
//			//RMFailDetectUDPThread rmFail1 = null;
//			DatagramSocket socket1 = new DatagramSocket();
//			byte[] requestMessage = "RM1".getBytes();
//			InetAddress host = InetAddress.getByName("localhost");
//			DatagramPacket request1 = new DatagramPacket(requestMessage, requestMessage.length,
//					host, 6502);
//			socket1.send(request1);
//			System.out.println("request sent");
//			byte[] buffer = new byte[1000];
//			DatagramPacket receive = new DatagramPacket(buffer, buffer.length);
//			socket1.receive(receive);
//			System.out.println("Receive reply");
//			ByteArrayInputStream in = new ByteArrayInputStream(buffer);
//			System.out.println("1");
//			ObjectInputStream is = new ObjectInputStream(in);
//			System.out.println("1");
//			//Object o = is.readObject();
//			System.out.println("1");
//			rm1 = (replica1.servers.RMFailDetectUDPThread) is.readObject();
////			if (is.readObject() instanceof replica1.servers.RMFailDetectUDPThread) {
////				
////			}
//			if (rm1 != null) {
//				System.out.println("got object of Rm1");
//				rm1.stopChildThread();
//				try {
//					rm1.stop();
//				} catch (ThreadDeath e) {
//					System.out.println("RM1 stopped");
//				}
//			}
//			socket1.close();
//			return "Leader crashed";
//			//rmFail1.start();
//		} catch (Exception e) {
//			
//		}
//		return "Leader crashed";
	}

	/**
	 * Shuts down a secondary server process to simulate a crash.
	 */
	public String crashSecondaryServer() 
	{
//		replica3.servers.RMFailDetectUDPThread rmFail3 = null;
//		replica2.servers.RMFailDetectUDPThread rmFail2 = null;
		
		rm2 = RMFailObjectServer.getRm2();
		rm3 = RMFailObjectServer.getRm3();
		rm2.stopChildThread();
		rm3.stopChildThread();
		rm2.stop();
		rm3.stop();
		
//		try {
//			DatagramSocket socket1 = new DatagramSocket();
//			byte[] requestMessage = "RM3".getBytes();
//			InetAddress host = InetAddress.getByName("localhost");
//			DatagramPacket request1 = new DatagramPacket(requestMessage, requestMessage.length,
//					host, 6502);
//			socket1.send(request1);
//			byte[] buffer = new byte[100];
//			DatagramPacket receive = new DatagramPacket(buffer, buffer.length);
//			socket1.receive(receive);
//			ByteArrayInputStream in = new ByteArrayInputStream(buffer);
//			ObjectInputStream is = new ObjectInputStream(in);
//			//Object o = is.readObject();
//			rm3 = (replica3.servers.RMFailDetectUDPThread) is.readObject();
////			if (o instanceof replica3.servers.RMFailDetectUDPThread) {
////				
////			}
//			rm3.stopChildThread();
//			try {
//				rm3.stop();
//			} catch (ThreadDeath e) {
//				System.out.println("RM3 stopped");
//			}
//			
//			socket1 = new DatagramSocket();
//			requestMessage = "RM2".getBytes();
//			request1 = new DatagramPacket(requestMessage, requestMessage.length,
//					host, 6502);
//			socket1.send(request1);
//			
//			buffer = new byte[100];
//			receive = new DatagramPacket(buffer, buffer.length);
//			socket1.receive(receive);
//			in = new ByteArrayInputStream(buffer);
//			is = new ObjectInputStream(in);
//			rm2 = (replica2.servers.RMFailDetectUDPThread) is.readObject();
//			rm2.stopChildThread();
//			try {
//				rm2.stop();
//			} catch (ThreadDeath e) {
//				System.out.println("RM2 stopped");
//			}
//			rm2.start();
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
		
		return "Rm2 and Rm3 crashed.";
	}

	/**
	 * Invokes FIFO System for adding a new Request to the queue and getting it processed by the lead server.
	 * @param 	methodName	Name of the method to be invoked on the server
	 * @param 	methodArgs	Arguments required as input to the server method
	 * @return 	Success or failure status message of the processing
	 */
	private String processRequest(String methodName, List<String> methodArgs) 
	{
		String processStatus = null;

		//Creating a Request object with details of the client request to be processed
		Request newRequest = new Request(methodName, methodArgs);

		//Invoking FIFO System to add the new request to the request queue
		boolean reqAdded = fifoOrdSys.addRequest(newRequest);
		if (!reqAdded)
			processStatus = "Failed to add request to queue";
		else 
		{
			String[] leadServerDetails = getLeadServerDetails();
			String leadHostname = leadServerDetails[0];
			int leadPort = Integer.parseInt(leadServerDetails[1]);

			//Invoking FIFO System to send first request in the queue to the lead server for processing
			processStatus = fifoOrdSys.sendFirstRequest(leadHostname, leadPort);

			//Invoking FIFO System to remove the request just processed from the queue
			fifoOrdSys.removeFirstRequest();
		}

		return processStatus;
	}

	/**
	 * Fetches the hostname and port number of the leader server.
	 * @return	String containing the leader hostname and port number
	 */
	private String[] getLeadServerDetails() 
	{
		String[] leadDetails = new String[2];
		
		//TODO uncomment for testing with Replica 2 as leader
		//leadServerPort = 6794;

		synchronized (leadUDPDtlsLock) {
			leadDetails[0] = leadServerHostname;
			leadDetails[1] = leadServerPort + "";
		}
		
		return leadDetails;
	}
}