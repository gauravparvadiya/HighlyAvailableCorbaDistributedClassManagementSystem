package replica2.services;

import java.util.ArrayList;
import java.util.List;
import replica2.servers.CenterServerUDPThread;
import replica2.services.RecordManagerImpl;
import replica2.services.ReqProcessClientThread;
import replica2.entities.Request;

/**
 * Replica manager service class for parsing, processing and initiating broadcasting of requests.
 * @author Jyotsana Gupta
 */
public class ReplicaMgrService
{
	/**
	 * Indicates whether this is a leader or a secondary replica manager.
	 */
	private boolean isLeader;
	
	/**
	 * Object used as a lock for lead status access and update.
	 */
	private final Object leadStatLock = new Object();
	
	//Center Server instances
	private RecordManagerImpl mtlRecMgr;
	private RecordManagerImpl lvlRecMgr;
	private RecordManagerImpl ddoRecMgr;
	
	//UDP Center Server threads 
	private CenterServerUDPThread mtlUdpServThread;
	private CenterServerUDPThread lvlUdpServThread;
	private CenterServerUDPThread ddoUdpServThread;
	
	/**
	 * Instance of FIFOBroadcastSys for invoking FIFO request broadcasting methods. 
	 */
	private FIFOBroadcastSys fifoBroadcastSys;
	
	/**
	 * Default (unparameterized) constructor.
	 */
	public ReplicaMgrService()
	{
		isLeader = false;
		
		//Creating instances of all Center Servers
		mtlRecMgr = new RecordManagerImpl("mtl");
		lvlRecMgr = new RecordManagerImpl("lvl");
		ddoRecMgr = new RecordManagerImpl("ddo");
		
		//Launching UDP/IP communication threads for all Center Servers
		mtlUdpServThread = new CenterServerUDPThread(6795, mtlRecMgr);
		lvlUdpServThread = new CenterServerUDPThread(6796, lvlRecMgr);
		ddoUdpServThread = new CenterServerUDPThread(6797, ddoRecMgr);
		mtlUdpServThread.start();
		lvlUdpServThread.start();
		ddoUdpServThread.start();
		
		//Launching FIFO broadcast system
		fifoBroadcastSys = new FIFOBroadcastSys();
	}
	
	/**
	 * Sets the value of lead/secondary status for this replica.
	 * @param	rmStatusStr	Lead/secondary status
	 */
	public void setIsLeader(String rmStatusStr)
	{
		String[] rmStatus = rmStatusStr.trim().split("_");
		
		synchronized (leadStatLock) 
		{
			if (rmStatus[1].trim().equalsIgnoreCase("true"))
				this.isLeader = true;
			else
				this.isLeader = false;
		}
	}
	
	/**
	 * Fetches the value of lead/secondary status of this replica.
	 * @return	Current status of this replica
	 */
	private boolean getIsLeader()
	{
		synchronized (leadStatLock)
		{
			return isLeader;
		}
	}
	
	/**
	 * Parses, processes and broadcasts (if this is the leader) requests.
	 * @param 	newRequest	Request received for processing
	 * @return	Success or failure message based on the processing status of the request
	 */
	public String processRequest(Request newRequest)
	{
		String processStatus = null;
		String remServHostname = "localhost";
		int remServPort = -1;
		
		//Finding the Center Server to process the request
		String mgrID = newRequest.getMethodArgs().get(0);
		String centerID = mgrID.substring(0, 3);		
		if (centerID.equalsIgnoreCase("MTL"))
			remServPort = 6795;
		else if (centerID.equalsIgnoreCase("LVL"))
			remServPort = 6796;
		else if (centerID.equalsIgnoreCase("DDO"))
			remServPort = 6797;
		
		//Sending request to Center Server for executing the required operation
		processStatus = executeOperation(newRequest, remServHostname, remServPort);
		
		//Handle Center Server crash and re-process request
		if (processStatus.trim().toLowerCase().indexOf("timeout") >= 0)
		{
			System.out.println("Center Server processing timeout exceeded, indicating a crash.");
			processStatus = handleServerCrash(centerID, remServHostname, remServPort, newRequest);
		}
		
		//Additional processing for lead server
		if (getIsLeader())
		{
			//If the operation was successful on the leader, broadcast to other replicas
			if (processStatus.trim().toLowerCase().indexOf("success") >= 0)
				fifoBroadcastSys.broadcastRequest(newRequest);
		}
			
		return processStatus;
	}
	
	/**
	 * Sends the new request to the intended Center Server for processing and fetches its status.
	 * @param 	newRequest		Request received for processing
	 * @param 	remServHostname	Hostname of the Center Server to be contacted	
	 * @param 	remServPort		Port number of the Center Server to be contacted
	 * @return	Success or failure message based on the processing status of the request
	 */
	private String executeOperation(Request newRequest, String remServHostname, int remServPort)
	{
		//Launching a thread for sending the request to the intended Center Server for processing
		ReqProcessClientThread recProcClientThread = new ReqProcessClientThread(newRequest, remServHostname, remServPort);
		recProcClientThread.start();
		
		//Waiting for the request processing thread to finish its execution
		try
		{
			recProcClientThread.join();
		}
		catch(InterruptedException ie)
		{
			System.out.println("Exception occurred while processing request: " + ie.getMessage());
		}
		
		//Fetching the request execution status
		return recProcClientThread.getProcessStatus();
	}
	
	/**
	 * Restarts the crashed Center Server and gets the request executed on it.
	 * @param 	centerID		Unique ID of the Center Server to be contacted
	 * @param 	remServHostname	Hostname of the Center Server to be contacted
	 * @param 	remServPort		Port number of the Center Server to be contacted
	 * @param 	newRequest		Request received for processing
	 * @return	Success or failure message based on the processing status of the request
	 */
	private String handleServerCrash(String centerID, String remServHostname, int remServPort, Request newRequest)
	{
		RecordManagerImpl recMgr = null;
		CenterServerUDPThread udpServThread = null;
			
		if (centerID.equalsIgnoreCase("MTL"))
		{
			recMgr = mtlRecMgr;
			udpServThread = mtlUdpServThread;
		}
		else if (centerID.equalsIgnoreCase("LVL"))
		{
			recMgr = lvlRecMgr;
			udpServThread = lvlUdpServThread;
		}
		else if (centerID.equalsIgnoreCase("DDO"))
		{
			recMgr = ddoRecMgr;
			udpServThread = ddoUdpServThread;
		}
						
		System.out.println("Stopping the crashed Center Server if it is still running...");
		try
		{
			if (udpServThread.isAlive())
				udpServThread.stop();
		}
		catch(ThreadDeath td)
		{
			System.out.println(centerID + " Center Server has been stopped.");
		}
		
		System.out.println("Restarting the crashed Center Server");
		CenterServerUDPThread newUdpServThread = new CenterServerUDPThread(remServPort, recMgr);
		newUdpServThread.start();
			
		System.out.println("Resending the request to the Center Server for processing");
		return executeOperation(newRequest, remServHostname, remServPort);
	}
	
	/**
	 * Stops the Center Server identified using Manger ID, sends a dummy request for processing in crash scenario.
	 * @param 	crashRequest	Request received for crashing Center Server
	 * @return	Success or failure message based on the processing status of the dummy request
	 */
	public String crashCenterServer(Request crashRequest)
	{
		String mgrID = crashRequest.getMethodArgs().get(0);
		String centerID = mgrID.substring(0, 3);
		String processStatus = null;
		
		System.out.println("Crashing " + centerID + " Center Server...");		
		if (centerID.equalsIgnoreCase("MTL"))
		{
			try
			{
				if (mtlUdpServThread.isAlive())
					mtlUdpServThread.stop();
			}
			catch(ThreadDeath td)
			{
				System.out.println(centerID + " Center Server has crashed!");
			}
		}
		else if (centerID.equalsIgnoreCase("LVL"))
		{
			try
			{
				if (lvlUdpServThread.isAlive())
					lvlUdpServThread.stop();
			}
			catch(ThreadDeath td)
			{
				System.out.println(centerID + " Center Server has crashed!");
			}
		}
		else if (centerID.equalsIgnoreCase("DDO"))
		{
			try
			{
				if (ddoUdpServThread.isAlive())
					ddoUdpServThread.stop();
			}
			catch(ThreadDeath td)
			{
				System.out.println(centerID + " Center Server has crashed!");
			}
		}
		
		//Creating a sample request for processing in crash scenario
		Request newRequest = createDummyRequest(mgrID);
		
		System.out.println("Sending request to Center Server after crash...");
		processStatus = processRequest(newRequest);
		return processStatus;
	}
	
	/**
	 * Creates a dummy request to be used in Center Server crash simulation.
	 * @param 	mgrID	Unique ID of the manager who performs this operation
	 * @return	Dummy request created
	 */
	private Request createDummyRequest(String mgrID)
	{
		String methodName = "createTRecord";

		List<String> methodArgs = new ArrayList<String>();
		methodArgs.add(mgrID);
		methodArgs.add("Daniel");
		methodArgs.add("Rogers");
		methodArgs.add("989 Mason Avenue");
		methodArgs.add("1234567890");
		methodArgs.add("Arts");
		methodArgs.add("MTL");
		
		return new Request(methodName, methodArgs);
	}
}