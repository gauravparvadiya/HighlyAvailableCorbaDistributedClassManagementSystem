package client.clients;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import client.utilities.CenterManagerUtil;
import client.corbasupport.RecordManagerApp.RecordManager;
import client.corbasupport.RecordManagerApp.RecordManagerHelper;

/**
 * Manager Client class for remote management of teacher-student records of a center.
 * @author Jyotsana Gupta
 */
class ManagerClient
{
	private static final String LOG_FILE_PATH = "D:\\Concordia\\Courses - Summer 2017\\COMP 6231 - DSD\\Project\\Logs\\CenterManager\\";
	private String fileName = null;
	
	public static void main(String[] args) throws Exception
	{
		ManagerClient mgrClient = new ManagerClient();
		Scanner input = new Scanner(System.in);
		String mgrID = null;
		boolean validMgr = true;
		
		//Authenticating center manager before allowing any operation
		do
		{
			System.out.println("Please enter a valid center manager ID for authentication. Enter 'X' to exit. ");
			mgrID = input.nextLine();
			
			if (mgrID.equalsIgnoreCase("X"))
				System.exit(0);
			
			validMgr = mgrClient.validateMgr(mgrID);
			if (!validMgr)
				System.out.println("Invalid manager ID. Authentication failed.");
		} while (!validMgr);
		
		//Generating manager log file name
		mgrClient.fileName = LOG_FILE_PATH + mgrID.toLowerCase() + ".txt";
		
		//Obtaining a handle on front end object
		RecordManager recMgr = mgrClient.getFEHandle();
		if (recMgr == null)
			System.exit(0);
		
		//Performing the required remote operations on the server
		int operationChoice = 0;
		String contProcessing = "Y";
		do
		{
			System.out.println("Please select one of the following operations to be performed:");
			System.out.println("1. Create Teacher Record");
			System.out.println("2. Create Student Record");
			System.out.println("3. Get Record Counts of All Servers");
			System.out.println("4. Edit Record");
			System.out.println("5. Transfer Record");
			System.out.println("6. Crash Leader Server");
			System.out.println("7. Crash a Secondary Server");
			System.out.println("8. Exit");
			try
			{
				operationChoice = Integer.parseInt(input.nextLine());
			}
			catch(NumberFormatException nfe)
			{
				operationChoice = 0;
			}
			
			switch (operationChoice)
			{
				case 1:	mgrClient.createTRecord(mgrID, recMgr, input);
						break;
				case 2:	mgrClient.createSRecord(mgrID, recMgr, input);
						break;
				case 3:	mgrClient.getRecordCounts(mgrID, recMgr);
						break;
				case 4:	mgrClient.editRecord(mgrID, recMgr, input);
						break;
				case 5:	mgrClient.transferRecord(mgrID, recMgr, input);
						break;
				case 6:	mgrClient.crashLeadServer(recMgr);
						break;
				case 7:	mgrClient.crashSecondaryServer(recMgr);
						break;
				case 8:	input.close();
						System.exit(0);
				default: System.out.println("Invalid choice.");
			}		
			
			System.out.println("Do you wish to continue (Y/N)?");
			contProcessing = input.nextLine();
			if ((contProcessing == null) || (contProcessing.trim().isEmpty()))
				contProcessing = "Y";
			contProcessing = contProcessing.trim().toUpperCase().substring(0, 1);
		} while (!(contProcessing.equalsIgnoreCase("N")));
		
		input.close();
	}
	
	/**
	 * Validates manager ID for correct format.
	 * @param 	mgrID	Manager ID to be validated
	 * @return	true, if manager ID has correct format
	 * 			false, otherwise
	 */
	private boolean validateMgr(String mgrID)
	{
		boolean validMgr = true;
		
		if (mgrID.trim().length() != 7)
			validMgr = false;
		else
		{
			String prefix = mgrID.substring(0, 3);
			String suffix = mgrID.substring(3);
			
			if (!((prefix.equalsIgnoreCase("MTL"))
					|| (prefix.equalsIgnoreCase("LVL"))
					|| (prefix.equalsIgnoreCase("DDO"))))
					validMgr = false;
			else
			{
				try
				{
					Integer.parseInt(suffix);
				}
				catch(NumberFormatException nfe)
				{
					validMgr = false;
				}
			}
		}

		return validMgr;
	}
	
	/**
	 * Obtains a handle on the remote front end object.
	 * @return	Front end object handle
	 */
	private RecordManager getFEHandle()
	{
		RecordManager recMgr = null;
		String feHost = "localhost";
		String feOrbPort = "1050";
		String feBindName = "frontend";
		
		try
		{
			//Creating and initializing the ORB
			String orbInitStr = "-ORBInitialPort " + feOrbPort + " -ORBInitialHost " + feHost;
			String[] orbInitArr = orbInitStr.split(" ");
			ORB orb = ORB.init(orbInitArr, null);
		     
		    //Getting the root naming context
		    org.omg.CORBA.Object nameSvcObjRef = orb.resolve_initial_references("NameService");
		    
		    //Casting naming context CORBA object to Java object  
		    NamingContextExt nameContextRef = NamingContextExtHelper.narrow(nameSvcObjRef);
		 
		    //Resolving the Object Reference in Naming
		    recMgr = RecordManagerHelper.narrow(nameContextRef.resolve_str(feBindName));
		}
		catch (Exception e) 
		{
			System.out.println("Exception occurred while fetching FE handle in CORBA client: " + e.getMessage());
		}
		
		return recMgr;
	}
	
	/**
	 * Collects details for teacher record creation and invokes remote front end method for the operation.
	 * @param	mgrID	Unique ID of the center manager who performs this operation
	 * @param 	recMgr	Front end handle for performing remote operations
	 * @param	input	Scanner object for accepting user input
	 */
	private void createTRecord(String mgrID, RecordManager recMgr, Scanner input)
	{
		System.out.println("Please enter the following details required for creating the teacher record:");
		System.out.print("First Name: ");
		String firstName = input.nextLine().trim();
		System.out.print("Last Name: ");
		String lastName = input.nextLine().trim();
		System.out.print("Address: ");
		String address = input.nextLine().trim();
		System.out.print("Phone Number (only digits): ");
		String phone = input.nextLine().trim();
		System.out.print("Specialization Subject (e.g. French, Math, etc.): ");
		String specialization = input.nextLine().trim();
		System.out.print("Location (MTL/LVL/DDO): ");
		String location = input.nextLine().toUpperCase().trim();
		
		//Performing the operation and recording the time at which it completes
		String opStatus = recMgr.createTRecord(mgrID, firstName, lastName, address, phone, specialization, location);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
		System.out.println("Teacher record creation status: " + opStatus);
		
		//Logging the operation
		String logText = "Create T Rec (FN: " + firstName 
										+ " | LN: " + lastName 
										+ " | ADR: " + address 
										+ " | PH: " + phone 
										+ " | SPL: " + specialization 
										+ " | LOC: " + location 
										+ ") @" + opTime + " - " + opStatus;
		CenterManagerUtil.writeToFile(fileName, logText);
	}
	
	/**
	 * Collects details for student record creation and invokes remote front end method for the operation.
	 * @param	mgrID	Unique ID of the center manager who performs this operation
	 * @param 	recMgr	Front end handle for performing remote operations
	 * @param	input	Scanner object for accepting user input
	 */
	private void createSRecord(String mgrID, RecordManager recMgr, Scanner input)
	{
		System.out.println("Please enter the following details required for creating the student record:");
		System.out.print("First Name: ");
		String firstName = input.nextLine().trim();
		System.out.print("Last Name: ");
		String lastName = input.nextLine().trim();
		System.out.print("Courses Registered (comma-separated list): ");
		String coursesRegistered = input.nextLine().trim();
		System.out.print("Status (active/inactive): ");
		String status = input.nextLine().toLowerCase().trim();
		System.out.print("Status Date (YYYY/MM/DD): ");
		String statusDate = input.nextLine().trim();
		
		//Performing the operation and recording the time at which it completes
		String opStatus = recMgr.createSRecord(mgrID, firstName, lastName, coursesRegistered, status, statusDate);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
		System.out.println("Student record creation status: " + opStatus);
		
		//Logging the operation
		String logText = "Create S Rec (FN: " + firstName 
										+ " | LN: " + lastName 
										+ " | CR: " + coursesRegistered 
										+ " | ST: " + status 
										+ " | SDT: " + statusDate 
										+ ") @" + opTime + " - " + opStatus;
		CenterManagerUtil.writeToFile(fileName, logText);
	}
	
	/**
	 * Invokes remote front end method for fetching the record count from all center servers.
	 * @param	mgrID	Unique ID of the center manager who performs this operation
	 * @param 	recMgr	Front end handle for performing remote operations
	 */
	private void getRecordCounts(String mgrID, RecordManager recMgr)
	{
		//Performing the operation and recording the time at which it completes
		String recCount = recMgr.getRecordCounts(mgrID);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
		System.out.println("Record count: " + recCount);
		
		//Logging the operation
		String logText = "Get Rec Count @" + opTime + " - " + recCount;
		CenterManagerUtil.writeToFile(fileName, logText);
	}
	
	/**
	 * Collects details for editing a record and invokes remote front end method for the operation.
	 * @param	mgrID	Unique ID of the center manager who performs this operation
	 * @param 	recMgr	Front end handle for performing remote operations
	 * @param	input	Scanner object for accepting user input
	 */
	private void editRecord(String mgrID, RecordManager recMgr, Scanner input)
	{
		String opStatus = null;
		
		System.out.println("Please enter the following details required for editing a record:");
		System.out.print("Record ID: ");
		String recordID = input.nextLine().trim();
		System.out.println("Field Name: ");
		System.out.println("Teacher fields: address (A) / phone (P) / location (L)");
		System.out.println("Student fields: courses_registered (C) / status (S) / status_date (D)");
		String fieldName = input.nextLine().toUpperCase().trim();
		switch(fieldName)
		{
			case "A":	fieldName = "address";
						break;
			case "P":	fieldName = "phone";
						break;
			case "L":	fieldName = "location";
						break;
			case "C":	fieldName = "courses_registered";
						break;
			case "S":	fieldName = "status";
						break;
			case "D":	fieldName = "status_date";
						break;
		}
		System.out.print("New Value: ");
		String newValue = input.nextLine().trim();
				
		//Performing the operation and recording the time at which it completes
		opStatus = recMgr.editRecord(mgrID, recordID, fieldName, newValue);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
		System.out.println("Record edit status: " + opStatus);
		
		//Logging the operation
		String logText = "Edit Rec (ID: " + recordID
									+ " | FLD: " + fieldName 
									+ " | VAL: " + newValue
									+ ") @" + opTime + " - " + opStatus;
		CenterManagerUtil.writeToFile(fileName, logText);
	}
	
	/**
	 * Collects details for transferring a record and invokes remote front end method for the operation.
	 * @param 	mgrID	Unique ID of the center manager who performs this operation
	 * @param 	recMgr	Front end handle for performing remote operations
	 * @param 	input	Scanner object for accepting user input
	 */
	private void transferRecord(String mgrID, RecordManager recMgr, Scanner input)
	{
		String opStatus = null;
		
		System.out.println("Please enter the following details required for transferring a record:");
		System.out.print("Record ID: ");
		String recordID = input.nextLine().trim();
		System.out.print("Destination Server Name (MTL/LVL/DDO): ");
		String remServerName = input.nextLine().toUpperCase().trim();
				
		//Performing the operation and recording the time at which it completes
		opStatus = recMgr.transferRecord(mgrID, recordID, remServerName);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
		System.out.println("Record transfer status: " + opStatus);
		
		//Logging the operation
		String logText = "Transfer Rec (ID: " + recordID 
										+ " | SRV: " + remServerName 
										+ ") @" + opTime + " - " + opStatus;
		CenterManagerUtil.writeToFile(fileName, logText);
	}
	
	private void crashLeadServer(RecordManager recMgr)
	{
		String opStatus = recMgr.crashLeadServer();
		System.out.println("Lead server crash status: " + opStatus);
	}
	
	private void crashSecondaryServer(RecordManager recMgr)
	{
		String opStatus = recMgr.crashSecondaryServer();
		System.out.println("Secondary server crash status: " + opStatus);
	}
}