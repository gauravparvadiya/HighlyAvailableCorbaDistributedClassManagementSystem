package replica1.services.impl;

import replica1.entities.Request;
import replica1.servers.CenterServerUDPThread;

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
	
	/**
	 * Montreal Center Server instance.
	 */
	private RecordManagerImpl mtlRecMgr;
	
	/**
	 * Laval Center Server instance.
	 */
	private RecordManagerImpl lvlRecMgr;
	
	/**
	 * DDO Center Server instance.
	 */
	private RecordManagerImpl ddoRecMgr;
	
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
		
		//TODO changes
		
		//Launching UDP/IP communication threads for all Center Servers
		CenterServerUDPThread mtlUdpServThread = new CenterServerUDPThread(6791, mtlRecMgr);
		CenterServerUDPThread lvlUdpServThread = new CenterServerUDPThread(6792, lvlRecMgr);
		CenterServerUDPThread ddoUdpServThread = new CenterServerUDPThread(6793, ddoRecMgr);
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
		
		//TODO changes
		
		//Finding the Center Server to process the request
		String mgrID = newRequest.getMethodArgs().get(0);
		String centerID = mgrID.substring(0, 3);		
		if (centerID.equalsIgnoreCase("MTL"))
			remServPort = 6791;
		else if (centerID.equalsIgnoreCase("LVL"))
			remServPort = 6792;
		else if (centerID.equalsIgnoreCase("DDO"))
			remServPort = 6793;
		
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
		
		//Checking the send operation status
		processStatus = recProcClientThread.getProcessStatus();
		
		//Additional processing for lead server
		if (getIsLeader())
		{
			//If the operation was successful on the leader, broadcast to other replicas
			if (processStatus.trim().toLowerCase().indexOf("success") >= 0)
			{
				fifoBroadcastSys.broadcastRequest(newRequest);
			}
		}
			
		return processStatus;
	}
	
	//TODO changes
}