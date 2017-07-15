package replica3.services.impl;

import replica3.entities.Request;
import replica3.services.RecordManager;

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
		mtlRecMgr = new RecordManagerImpl("mtl");
		lvlRecMgr = new RecordManagerImpl("lvl");
		ddoRecMgr = new RecordManagerImpl("ddo");
		
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
		RecordManager recMgr = null;
		
		//Finding the Center Server to process the request
		String mgrID = newRequest.getMethodArgs().get(0);
		String centerID = mgrID.substring(0, 3);		
		if (centerID.equalsIgnoreCase("MTL"))
			recMgr = mtlRecMgr;
		else if (centerID.equalsIgnoreCase("LVL"))
			recMgr = lvlRecMgr;
		else if (centerID.equalsIgnoreCase("DDO"))
			recMgr = ddoRecMgr;
			
		//Invoking the intender Center Server for processing
		processStatus = invokeCenterServer(newRequest, recMgr);
		
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
	
	/**
	 * Invokes the intended operation on the intended Center Server in this replica.
	 * @param 	newRequest	Request received for processing
	 * @param 	recMgr		Instance of the intended Center Server
	 * @return	Success or failure message based on the processing status of the request
	 */
	private String invokeCenterServer(Request newRequest, RecordManager recMgr)
	{
		String opStatus = null;
		String methodName = newRequest.getMethodName();
		
		if (methodName.equalsIgnoreCase("createTRecord"))
		{
			opStatus = recMgr.createTRecord(newRequest.getMethodArgs().get(0)
											,newRequest.getMethodArgs().get(1)
											,newRequest.getMethodArgs().get(2)
											,newRequest.getMethodArgs().get(3)
											,newRequest.getMethodArgs().get(4)
											,newRequest.getMethodArgs().get(5)
											,newRequest.getMethodArgs().get(6));
		}
		else if (methodName.equalsIgnoreCase("createSRecord"))
		{
			opStatus = recMgr.createSRecord(newRequest.getMethodArgs().get(0)
											,newRequest.getMethodArgs().get(1)
											,newRequest.getMethodArgs().get(2)
											,newRequest.getMethodArgs().get(3)
											,newRequest.getMethodArgs().get(4)
											,newRequest.getMethodArgs().get(5));
		}
		else if (methodName.equalsIgnoreCase("getRecordCounts"))
		{
			opStatus = recMgr.getRecordCounts(newRequest.getMethodArgs().get(0));
		}
		else if (methodName.equalsIgnoreCase("editRecord"))
		{
			opStatus = recMgr.editRecord(newRequest.getMethodArgs().get(0)
										,newRequest.getMethodArgs().get(1)
										,newRequest.getMethodArgs().get(2)
										,newRequest.getMethodArgs().get(3));
		}
		else if (methodName.equalsIgnoreCase("transferRecord"))
		{
			opStatus = recMgr.transferRecord(newRequest.getMethodArgs().get(0)
											,newRequest.getMethodArgs().get(1)
											,newRequest.getMethodArgs().get(2));
		}
		
		return opStatus;
	}
}