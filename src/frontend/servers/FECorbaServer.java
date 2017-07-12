package frontend.servers;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import frontend.corbasupport.RecordManagerApp.RecordManager;
import frontend.corbasupport.RecordManagerApp.RecordManagerHelper;
import frontend.services.RecordManagerFEImpl;

/**
 * Front End's CORBA server class for communicating with Manager Client and initiating request processing.
 * @author Jyotsana Gupta
 */
public class FECorbaServer 
{
	private static final String FE_HOST = "localhost";
	private static final int ORB_PORT = 1050;
	private static final String FE_BIND_NAME = "frontend";
	
	public static void main(String[] args) 
	{
		try
		{
			//Creating and initializing the ORB
			String orbInitStr = "-ORBInitialPort " + ORB_PORT + " -ORBInitialHost " + FE_HOST;
			String[] orbInitArr = orbInitStr.split(" ");
			ORB orb = ORB.init(orbInitArr, null);
			
			//Getting reference to Root POA and activating POA Manager
			POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPOA.the_POAManager().activate();
			
			//Creating servant instance and registering it with the ORB
			RecordManagerFEImpl recMgrFE = new RecordManagerFEImpl();
			
			//Making servant instance a CORBA object by registering it with POA
			org.omg.CORBA.Object feObjRef = rootPOA.servant_to_reference(recMgrFE);
			
			//Casting the CORBA reference to a Java reference
			RecordManager feIfaceObjRef = RecordManagerHelper.narrow(feObjRef);
			
			//Getting the root naming context; NameService invokes the transient name service
			org.omg.CORBA.Object rootNameContext = orb.resolve_initial_references("NameService");
		  
			//Casting the CORBA reference to a Java reference
			NamingContextExt nameContextRef = NamingContextExtHelper.narrow(rootNameContext);
		   
			//Binding the Object Reference in Naming
			NameComponent bindPath[] = nameContextRef.to_name(FE_BIND_NAME);
			nameContextRef.rebind(bindPath, feIfaceObjRef);
			
			System.out.println("Front end CORBA server has been started.");
			
			//Waiting for CORBA invocations from clients
			orb.run();
		} 
		catch (Exception e) 
		{
			System.out.println("Exception occurred during FE CORBA server interaction: " + e.getMessage());
		}
		
		System.out.println("Exiting front end CORBA server.");
	}
}