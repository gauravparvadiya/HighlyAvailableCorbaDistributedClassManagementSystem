package frontend.services;

import java.util.ArrayList;
import java.util.List;
import frontend.corbasupport.RecordManagerApp.RecordManagerPOA;
import frontend.entities.Request;
import frontend.servers.FEUDPServerThread;

/**
 * Front end remote interface implementation defining methods for initiating student-teacher record management.
 * @author Jyotsana Gupta
 * @author Gauravkumar Parvadiya
 */
public class RecordManagerFEImpl extends RecordManagerPOA 
{
	/**
	 * Instance of FIFOOrderSys for invoking FIFO total request ordering methods.
	 */
	private FIFOOrderSys fifoOrdSys;

	/**
	 * Lead server host name.
	 */
	private String leadServerHostname;

	/**
	 * Lead server port number.
	 */
	private int leadServerPort;

	/**
	 * Object used as a lock for lead server UDP connection details update and access.
	 */
	private final Object leadUDPDtlsLock = new Object();

	/**
	 * Default (unparameterized) constructor.
	 */
	public RecordManagerFEImpl() 
	{
		leadServerHostname = null;
		leadServerPort = -1;

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
			this.leadServerHostname = leadDetails[1].trim();
			this.leadServerPort = Integer.parseInt(leadDetails[2].trim());
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
		//TODO leader process crash simulation to be implemented here
		//If UDP server is required, the same host and port used by FIFO should be used

		return null;
	}

	/**
	 * Shuts down a secondary server process to simulate a crash.
	 */
	public String crashSecondaryServer() 
	{
		//TODO secondary process crash simulation to be implemented here
		//If UDP server is required, the same host and port used by FIFO should be used

		return null;
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

		synchronized (leadUDPDtlsLock) {
			leadDetails[0] = leadServerHostname;
			leadDetails[1] = leadServerPort + "";
		}

		return leadDetails;
	}
}