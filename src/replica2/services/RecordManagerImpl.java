package replica2.services;

import replica2.entities.Record;
import replica2.entities.StudentRecord;
import replica2.entities.TeacherRecord;
import replica2.utilities.CenterServerUtil;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Center server implementation defining methods for student-teacher record management.
 * @author Jyotsana Gupta
 */
public class RecordManagerImpl
{	
	/**
	 * Counter for unique ID of student-teacher records for a server.
	 */
	private int recordIDCounter;
	
	/**
	 * Collection of student and teacher records for a server.
	 */
	private Map<Character, List<Record>> centerRecords;
	
	/**
	 * Object used as a lock for record ID counter manipulation
	 */
	private final Object recordIDCtrLock = new Object();
	
	private static final String LOG_FILE_PATH = "D:\\Concordia\\Courses - Summer 2017\\COMP 6231 - DSD\\Assignment\\Assignment 2\\Logs\\CenterServer\\";
	private String fileName;
	private String centerID;
		
	/**
	 * Parameterized constructor.
	 * @param	centerID	Acronym for identifying the server which creates this remote object
	 */
	public RecordManagerImpl(String centerID)
	{
		recordIDCounter = 10000;
		centerRecords = new HashMap<Character, List<Record>>();
		this.centerID = centerID;
		fileName = LOG_FILE_PATH + centerID + ".txt";
		
		//Populating the records hashmap with some initial hard-coded records
		createInitialRecords(centerID);
	}
	
	/**
	 * Creates a new teacher record with the values provided as parameters, and adds it to the server's record hashmap.
	 * @param	mgrID			Unique ID of the center manager who performs this operation
	 * @param	firstName		First name of the teacher
	 * @param 	lastName		Last name of the teacher
	 * @param 	address			Address of the teacher
	 * @param 	phone			Phone number of the teacher
	 * @param 	specialization	Subject that the teacher specializes in (e.g. French, Math, etc.)
	 * @param 	location		Location of the teacher (e.g. MTL, LVL, etc.)
	 * @return	Success or failure status message of the operation
	 */
	public String createTRecord(String mgrID, String firstName, String lastName, String address, String phone, 
								String specialization, String location)
	{
		String recordID = null;
		String opStatus = validateTValues(firstName, lastName, address, phone, specialization, location);
		
		if (opStatus == null)
		{
			synchronized(recordIDCtrLock)
			{
				recordID = "TR" + (recordIDCounter++);
			}
			TeacherRecord tRecord = new TeacherRecord(recordID, firstName, lastName, address, phone, specialization, location);
			boolean recAdded = addRecord(tRecord);
			if (!recAdded)
				opStatus = "Failed to create teacher record";
			else
				opStatus = "Teacher record created successfully";
		}
		
		//Recording the time of operation
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
				
		//Logging the operation
		String logText = "Create T Rec (ID: " + recordID
										+ " | FN: " + firstName 
										+ " | LN: " + lastName 
										+ " | ADR: " + address 
										+ " | PH: " + phone 
										+ " | SPL: " + specialization 
										+ " | LOC: " + location 
										+ ") @" + opTime + " by " + mgrID + " - " + opStatus;
		CenterServerUtil.writeToFile(fileName, logText);
		
		return opStatus;
	}

	/**
	 * Creates a new student record with the values provided as parameters, and adds it to the server's record hashmap.
	 * @param	mgrID				Unique ID of the center manager who performs this operation
	 * @param 	firstName			First name of the student
	 * @param 	lastName			Last name of the student
	 * @param 	coursesRegistered	List of courses that the student has registered for (e.g. French, Math, etc.)
	 * @param 	status				Status of the student (active/inactive)
	 * @param 	statusDate			Date of last status update of the student
	 * @return	Success or failure status message of the operation
	 */
	public String createSRecord(String mgrID, String firstName, String lastName, String coursesRegistered, 
								String status, String statusDate)
	{
		String recordID = null;
		
		//Creating list of courses registered from the input String
		List<String> courseList = Arrays.asList(coursesRegistered.split(","));
		
		String opStatus = validateSValues(firstName, lastName, courseList, status, statusDate);
		
		if (opStatus == null)
		{
			//Converting status date String into Date type
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Date statusDt = null;
			try
			{
				statusDt = dateFormat.parse(statusDate);
			}
			catch(ParseException pe)
			{
				//No action to be taken; status date has already been validated at this point
			}			
			
			//Creating student record
			synchronized(recordIDCtrLock)
			{
				recordID = "SR" + (recordIDCounter++);
			}
			StudentRecord sRecord = new StudentRecord(recordID, firstName, lastName, courseList, status, statusDt);
			boolean recAdded = addRecord(sRecord);
			if (!recAdded)
				opStatus = "Failed to create student record";
			else
				opStatus = "Student record created successfully";
		}
		
		//Recording the time of operation
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
		
		//Logging the operation
		String logText = "Create S Rec (ID: " + recordID
										+ " | FN: " + firstName 
										+ " | LN: " + lastName 
										+ " | CR: " + courseList 
										+ " | ST: " + status 
										+ " | SDT: " + statusDate
										+ ") @" + opTime + " by " + mgrID + " - " + opStatus;
		CenterServerUtil.writeToFile(fileName, logText);
		
		return opStatus;
	}
	
	/**
	 * Fetches the record counts of all center servers.
	 * @param	mgrID	Unique ID of the center manager who performs this operation
	 * @return	Record counts of all center servers
	 */
	public String getRecordCounts(String mgrID)
	{
		Map<String, List<String>> serverConnDetails = getAllServers();
		
		//Initializing server record count list
		Map<String, Integer> serverRecCounts = new HashMap<String, Integer>();
		for (Map.Entry<String, List<String>> entry : serverConnDetails.entrySet())
			serverRecCounts.put(entry.getKey(), -1);
		
		//Launching threads for fetching record counts from all other servers
		int i = 0;
		RecCtrClientThread[] recCtrThreads = new RecCtrClientThread[serverRecCounts.size()-1];
		for (Map.Entry<String, List<String>> entry : serverConnDetails.entrySet())
		{
			if (!(entry.getKey().equalsIgnoreCase(centerID)))
			{
				String hostname = entry.getValue().get(0);
				int port = Integer.parseInt(entry.getValue().get(1));
				
				recCtrThreads[i] = new RecCtrClientThread(serverRecCounts, entry.getKey(), hostname, port);
				recCtrThreads[i].start();
				i++;
			}				
		}
		
		//Calculating record count of this server
		int ownRecCount = getOwnRecordCount();
		serverRecCounts.put(centerID.toUpperCase(), ownRecCount);
		
		//Waiting for all record counter threads to finish execution
		try
		{
			for (int j=0; j<recCtrThreads.length; j++)
				recCtrThreads[j].join();
		}
		catch(InterruptedException ie)
		{
			System.out.println("Exception occurred while counting records: " + ie.getMessage());
		}
		
		//Preparing final record count message to return
		String allServerRecCount = "Successful - ";
		for (Map.Entry<String, Integer> entry : serverRecCounts.entrySet())
			allServerRecCount = allServerRecCount + entry.getKey() + " " + entry.getValue() + ", ";
		allServerRecCount = allServerRecCount.trim().substring(0, allServerRecCount.lastIndexOf(","));
		
		//Recording the time of operation
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
				
		//Logging the operation
		String logText = "Get Rec Count @" + opTime + " by " + mgrID + " - " + allServerRecCount;
		CenterServerUtil.writeToFile(fileName, logText);
		
		return allServerRecCount;
	}
	
	/**
	 * Updates a particular field of an existing teacher or student record with a new value.
	 * @param	mgrID		Unique ID of the center manager who performs this operation
	 * @param 	recordID	Unique ID for identifying the record to be updated
	 * @param 	fieldName	Name of the field in the record to be updated
	 * @param 	newValue	New value to be assigned to the field
	 * @return	Success or failure message according to the execution status of the edit operation
	 */
	public String editRecord(String mgrID, String recordID, String fieldName, String newValue)
	{
		String editStatus = "Edit successful";
		
		if ((recordID == null) || (recordID.trim().length() == 0))
			editStatus = "Record ID cannot be null";
		else if ((fieldName == null) || (fieldName.trim().length() == 0))
			editStatus = "Field name cannot be null";
		else if (newValue == null)
			editStatus = "New value cannot be null";
		else
		{
			List<Record> targetList = locateRecList(recordID);
			if (targetList == null)
				editStatus = "No matching record found";
			else
			{
				synchronized(targetList)
				{
					Record targetRec = locateRecord(recordID);
					
					if (targetRec == null)
						editStatus = "No matching record found";
					else
					{
						if (recordID.substring(0, 1).equalsIgnoreCase("T"))
						{
							if (newValue.trim().length() == 0)
								editStatus = "New value cannot be null";
							else
							{
								TeacherRecord teacherRec = (TeacherRecord)targetRec;
								editStatus = updateTRecord(fieldName, newValue, teacherRec);
							}
						}
						else if (recordID.substring(0, 1).equalsIgnoreCase("S"))
						{
							StudentRecord studentRec = (StudentRecord)targetRec;
							editStatus = updateSRecord(fieldName, newValue, studentRec);
						}
						else
							editStatus = "Invalid record ID";
					}
				}
			}
		}
		
		//Recording the time of operation
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
		
		//Logging the operation
		String logText = "Edit Rec (ID: " + recordID 
									+ " | FLD: " + fieldName 
									+ " | VAL: " + newValue
									+ ") @" + opTime + " by " + mgrID + " - " + editStatus;
		CenterServerUtil.writeToFile(fileName, logText);

		return editStatus;
	}
	
	/**
	 * Transfers a record from one Center Server database to another.
	 * @param	mgrID			Unique ID of the center manager who performs this operation
	 * @param	recordID		Unique ID for identifying the record to be transferred
	 * @param	remServerName	Acronym for identifying the destination server
	 * @return	Success or failure message according to the execution status of the transfer operation
	 */
	public String transferRecord (String mgrID, String recordID, String remServerName)
	{
		//Validating record transfer details
		String transferStatus = validateRecTransfer(recordID, remServerName);
		
		//If validation is successful
		if (transferStatus == null)
		{
			List<Record> targetList = locateRecList(recordID);
			if (targetList == null)
				transferStatus = "No matching record found";
			else
			{
				synchronized(targetList)
				{
					//Locating the record to be transferred on the source server
					Record targetRec = locateRecord(recordID);
					
					if (targetRec == null)
						transferStatus = "No matching record found";
					else
					{
						//Fetching remote server connection details
						String[] remServDetails = getServerDetails(remServerName);
						String remServHostname = remServDetails[0];
						int remServPort = Integer.parseInt(remServDetails[1]);
						
						//Launching a thread for sending the record to the remote destination server
						RecTransferClientThread recTransClientThread = new RecTransferClientThread(targetRec, remServHostname, remServPort);
						recTransClientThread.start();
						
						//Waiting for the record transfer thread to finish its processing
						try
						{
							recTransClientThread.join();
						}
						catch(InterruptedException ie)
						{
							System.out.println("Exception occurred while transferring record: " + ie.getMessage());
						}
						
						//Checking the send operation status
						transferStatus = recTransClientThread.getTransferStatus();
						
						if (transferStatus.toLowerCase().indexOf("success") >= 0)
						{
							//Deleting the record from the source server
							deleteRecord(targetRec);
							transferStatus = "Transfer successful";
						}
					}
				}
			}
		}
		
		//Recording the time of operation
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String opTime = dateFormat.format(new Date());
		
		//Logging the operation
		String logText = "Transfer Rec (ID: " + recordID 
										+ " | SRV: " + remServerName
										+ ") @" + opTime + " by " + mgrID + " - " + transferStatus;
		CenterServerUtil.writeToFile(fileName, logText);

		return transferStatus;
	}
	
	/**
	 * Creates some initial hard-coded records for a Center Server.
	 * @param 	centerID	Acronym for identifying the server in which records are to be created
	 */
	private void createInitialRecords(String centerID)
	{
		if (centerID.equalsIgnoreCase("MTL"))
			createInitialMtlRecords();
		else if (centerID.equalsIgnoreCase("LVL"))
			createInitialLvlRecords();
		else if (centerID.equalsIgnoreCase("DDO"))
			createInitialDdoRecords();
	}
	
	/**
	 * Creates some initial hard-coded records for Montreal center server.
	 */
	private void createInitialMtlRecords()
	{
		TeacherRecord tRec = new TeacherRecord("TR00001", "Joey", "Tribbiani", "34 Grove Street", "3548576788", 
												"Performing Arts", "MTL");
		TeacherRecord gRec = new TeacherRecord("TR00002", "Monica", "Geller", "110 Main Street", "3548342315", 
												"French", "DDO");
		StudentRecord bRec = null;
		try
		{
			bRec = new StudentRecord("SR00003", "Frank", "Buffay", new ArrayList<String>(Arrays.asList("Math", "French")), 
										"active", new SimpleDateFormat("yyyy/MM/dd").parse("2016/08/28"));
		}
		catch(ParseException pe)
		{
			//No action to be taken; hard-coded date
		}
		
		List<Record> tRecList = new ArrayList<Record>();
		tRecList.add(tRec);
		centerRecords.put('T', tRecList);
		
		List<Record> gRecList = new ArrayList<Record>();
		gRecList.add(gRec);
		centerRecords.put('G', gRecList);
		
		List<Record> bRecList = new ArrayList<Record>();
		bRecList.add(bRec);
		centerRecords.put('B', bRecList);
	}
	
	/**
	 * Creates some initial hard-coded records for Laval center server.
	 */
	private void createInitialLvlRecords()
	{
		TeacherRecord gRec1 = new TeacherRecord("TR00004", "Ross", "Geller", "234 Woodwork Avenue", "8657456788", 
												"Paleontology", "LVL");
		TeacherRecord gRec2 = new TeacherRecord("TR00005", "Rachel", "Green", "110 Main Street", "3548342879", 
												"Fashion Design", "DDO");
		
		List<Record> gRecList = new ArrayList<Record>();
		gRecList.add(gRec1);
		gRecList.add(gRec2);
		centerRecords.put('G', gRecList);
	}
	
	/**
	 * Creates some initial hard-coded records for Dollard-des-Ormeaux center server.
	 */
	private void createInitialDdoRecords()
	{
		StudentRecord jRec = null;
		StudentRecord mRec = null;
		StudentRecord yRec = null;
		StudentRecord sRec = null;
		try
		{
			jRec = new StudentRecord("SR00006", "Tag", "Jones", new ArrayList<String>(Arrays.asList("Fashion Design", "Spanish")), 
										"active", new SimpleDateFormat("yyyy/MM/dd").parse("2015/05/02"));
			mRec = new StudentRecord("SR00007", "Gavin", "Mitchell", new ArrayList<String>(Arrays.asList("Computer Science")), 
										"inactive", new SimpleDateFormat("yyyy/MM/dd").parse("2017/04/12"));
			yRec = new StudentRecord("SR00008", "Ethan", "Young", new ArrayList<String>(Arrays.asList("Geology", "Zoology")), 
										"active", new SimpleDateFormat("yyyy/MM/dd").parse("2016/12/02"));
			sRec = new StudentRecord("SR00009", "Mona", "Simmers", new ArrayList<String>(Arrays.asList("Arts", "Botany")), 
										"inactive", new SimpleDateFormat("yyyy/MM/dd").parse("2015/05/02"));
		}
		catch(ParseException pe)
		{
			//No action to be taken; hard-coded dates
		}
		
		List<Record> jRecList = new ArrayList<Record>();
		jRecList.add(jRec);
		centerRecords.put('J', jRecList);
		
		List<Record> mRecList = new ArrayList<Record>();
		mRecList.add(mRec);
		centerRecords.put('M', mRecList);
		
		List<Record> yRecList = new ArrayList<Record>();
		yRecList.add(yRec);
		centerRecords.put('Y', yRecList);
		
		List<Record> sRecList = new ArrayList<Record>();
		sRecList.add(sRec);
		centerRecords.put('S', sRecList);
	}
	
	/**
	 * Validates input field values for a teacher record.
	 * @param 	firstName		First name of the teacher
	 * @param 	lastName		Last name of the teacher
	 * @param 	address			Address of the teacher
	 * @param 	phone			Phone number of the teacher
	 * @param 	specialization	Subject that the teacher specializes in (e.g. French, Math, etc.)
	 * @param 	location		Location of the teacher (e.g. MTL, LVL, etc.)
	 * @return	Success or failure status message of the validation
	 */
	private String validateTValues(String firstName, String lastName, String address, String phone, 
									String specialization, String location)
	{
		String validationFailure = null;
		
		if ((firstName == null) || (firstName.trim().length() == 0))
			validationFailure = "First name cannot be null";
		else if ((lastName == null) || (lastName.trim().length() == 0))
			validationFailure = "Last name cannot be null";
		else if ((address == null) || (address.trim().length() == 0))
			validationFailure = "Address cannot be null";
		else if ((phone == null) || (phone.trim().length() == 0))
			validationFailure = "Phone number cannot be null";
		else if ((specialization == null) || (specialization.trim().length() == 0))
			validationFailure = "Specialization cannot be null";
		else if ((location == null) || (location.trim().length() == 0))
			validationFailure = "Location cannot be null";
		else if (!((location.equalsIgnoreCase("MTL"))
				|| (location.equalsIgnoreCase("LVL"))
				|| (location.equalsIgnoreCase("DDO"))))
			validationFailure = "Invalid location value for teacher";
		else
		{
			if (!isNumber(phone))
				validationFailure = "Invalid phone number value for teacher";
		}
		
		return validationFailure;
	}
	
	/**
	 * Validates input field values for a student record.
	 * @param 	firstName			First name of the student
	 * @param 	lastName			Last name of the student
	 * @param 	coursesRegistered	List of courses that the student has registered for (e.g. French, Math, etc.)
	 * @param 	status				Status of the student (active/inactive)
	 * @param 	statusDate			Date of last status update of the student
	 * @return	Success or failure status message of the validation
	 */
	private String validateSValues(String firstName, String lastName, List<String> coursesRegistered, 
									String status, String statusDate)
	{
		String validationFailure = null;
		
		if ((firstName == null) || (firstName.trim().length() == 0))
			validationFailure = "First name cannot be null";
		else if ((lastName == null) || (lastName.trim().length() == 0))
			validationFailure = "Last name cannot be null";
		else if ((coursesRegistered == null) || (coursesRegistered.isEmpty()))
			validationFailure = "Course list cannot be null";
		else if ((status == null) || (status.trim().length() == 0))
			validationFailure = "Status cannot be null";
		else if ((statusDate == null) || (statusDate.trim().length() == 0))
			validationFailure = "Status date cannot be null";
		else if (!((status.equalsIgnoreCase("active")) || (status.equalsIgnoreCase("inactive"))))
			validationFailure = "Invalid status value for student";
		else
		{
			for (String course : coursesRegistered)
				if ((course == null) || (course.trim().length() == 0))
				{
					validationFailure = "Course name cannot be null";
					break;
				}
			
			if (validationFailure == null)
			{
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
				try
				{
					Date newDate = dateFormat.parse(statusDate);
					String newParsedDateStr = dateFormat.format(newDate);
					boolean dtFormatCorrect = statusDate.equalsIgnoreCase(newParsedDateStr);
					if (!dtFormatCorrect)
						validationFailure = "Invalid status date format";
				}
				catch(ParseException pe)
				{
					validationFailure = "Value for status date is not a date";
				}
			}
		}
		
		return validationFailure;
	}
	
	/**
	 * Adds a new teacher or student record to the server's record hashmap.
	 * @param 	record	Teacher or student record to be added
	 * @return	true, if the record is successfully added
	 */
	public boolean addRecord(Record record)
	{
		String lastName = record.getLastName();
		char keyChar = lastName.charAt(0);
		keyChar = Character.toUpperCase(keyChar);
		
		List<Record> targetList = null;
		synchronized(centerRecords)
		{
			targetList = centerRecords.get(keyChar);
			
			if (targetList == null)
			{
				List<Record> newRecordList = new ArrayList<Record>();
				newRecordList.add(record);
				centerRecords.put(keyChar, newRecordList);
			}
		}
		
		if (targetList != null)
		{
			synchronized(targetList)
			{
				if (!targetList.contains(record))
					targetList.add(record);
			}
		}
		
		return true;
	}
	
	/**
	 * Fetches the connection details of all center servers (required for UDP/IP communication).
	 * @return	Server connection details (center ID, hostname and port number)
	 */
	private Map<String, List<String>> getAllServers()
	{
		Map<String, List<String>> serverConnDetails = new HashMap<String, List<String>>();
		
		//Hard-coded connection details, for now; later, could be changed to file read
		serverConnDetails.put("MTL", new ArrayList<String>(Arrays.asList("localhost", "6795")));
		serverConnDetails.put("LVL", new ArrayList<String>(Arrays.asList("localhost", "6796")));
		serverConnDetails.put("DDO", new ArrayList<String>(Arrays.asList("localhost", "6797")));
		
		return serverConnDetails;
	}
	
	/**
	 * Calculates the teacher-student record count of this server.
	 * @return	Record count of this server
	 */
	public int getOwnRecordCount()
	{
		int recCount = 0;
		
		for (Map.Entry<Character, List<Record>> entry : centerRecords.entrySet())
		{
			List<Record> recList = entry.getValue();
			int listRecCount = recList.size();
			recCount += listRecCount;
		}
				
		return recCount;
	}
	
	/**
	 * Locates the list containing a student or teacher record in the server record hashmap using a record ID.
	 * @param 	targetRecordID	Unique ID of the record to be found
	 * @return	A reference to the found record list
	 * 			null, if no matching record is found
	 */
	private List<Record> locateRecList(String targetRecordID)
	{
		List<Record> foundRecList = null;
		
		for (Map.Entry<Character, List<Record>> entry : centerRecords.entrySet())
		{
			List<Record> recList = entry.getValue();
			for (Record rec : recList)
			{
				if (rec.getRecordID().equalsIgnoreCase(targetRecordID))
				{
					foundRecList = recList;
					break;
				}
			}
			
			if (foundRecList != null)
				break;
		}		
		
		return foundRecList;
	}
	
	/**
	 * Locates a student or teacher record in the server record hashmap using a record ID.
	 * @param 	targetRecordID	Unique ID of the record to be found
	 * @return	A reference to the found record
	 * 			null, if no matching record is found
	 */
	private Record locateRecord(String targetRecordID)
	{
		Record foundRec = null;
		
		for (Map.Entry<Character, List<Record>> entry : centerRecords.entrySet())
		{
			List<Record> recList = entry.getValue();
			for (Record rec : recList)
			{
				if (rec.getRecordID().equalsIgnoreCase(targetRecordID))
				{
					foundRec = rec;
					break;
				}
			}
			
			if (foundRec != null)
				break;
		}		
		
		return foundRec;
	}
	
	/**
	 * Validates the new value to be assigned and sets the intended teacher record field to it. 
	 * @param 	fieldName		Name of the field in the record to be updated 
	 * @param 	updatedValue	New value to be assigned to the field
	 * @param 	teacherRec		Reference to the record to be updated
	 * @return	Success or failure message according to the execution status of the update operation
	 */
	private String updateTRecord(String fieldName, String updatedValue, TeacherRecord teacherRec)
	{
		String editStatus = null;
		
		if (fieldName.equalsIgnoreCase("location"))
		{
			if (!((updatedValue.equalsIgnoreCase("MTL"))
				|| (updatedValue.equalsIgnoreCase("LVL"))
				|| (updatedValue.equalsIgnoreCase("DDO"))))
				editStatus = "Invalid location value for teacher";
			else
			{
				teacherRec.setLocation(updatedValue.toUpperCase());
					editStatus = "Edit successful";
			}
		}
		else if (fieldName.equalsIgnoreCase("address"))
		{
			teacherRec.setAddress(updatedValue);
				editStatus = "Edit successful";
		}
		else if (fieldName.equalsIgnoreCase("phone"))
		{
			if (isNumber(updatedValue))
			{
				teacherRec.setPhone(updatedValue);
				editStatus = "Edit successful";
			}
			else
				editStatus = "Invalid phone number value for teacher";
		}
		else
			editStatus = "Field " + fieldName + " is not editable for teacher";
		
		return editStatus;
	}
	
	/**
	 * Validates the new value to be assigned and sets the intended student record field to it.
	 * @param 	fieldName	Name of the field in the record to be updated
	 * @param 	newValue	New value to be assigned to the field
	 * @param 	studentRec	Reference to the record to be updated
	 * @return	Success or failure message according to the execution status of the update operation
	 */
	private String updateSRecord(String fieldName, String newValue, StudentRecord studentRec)
	{
		String editStatus = null;
		
		if (fieldName.equalsIgnoreCase("courses_registered"))
		{
			//Creating list of courses registered from the input String
			List<String> updCourseList = Arrays.asList(newValue.split(","));
			
			if (updCourseList.isEmpty())
				editStatus = "New course list cannot be empty";
			else
			{
				for (String course : updCourseList)
					if ((course == null) || (course.trim().length() == 0))
					{
						editStatus = "Course name cannot be null";
						break;
					}
				
				if (editStatus == null)
				{
					studentRec.setCoursesRegistered(updCourseList);
						editStatus = "Edit successful";
				}
			}
		}
		else if (fieldName.equalsIgnoreCase("status"))
		{
			if (newValue.trim().length() == 0)
				editStatus = "New value cannot be null";
			else if (!((newValue.equalsIgnoreCase("active")) || (newValue.equalsIgnoreCase("inactive"))))
				editStatus = "Invalid status value for student";
			else
			{
				String currStatus = studentRec.getStatus();
				String newStatus = newValue.toLowerCase();
				if (!(currStatus.equalsIgnoreCase(newStatus)))
				{
					studentRec.setStatus(newStatus);
					studentRec.setStatusDate(new Date());
					editStatus = "Edit successful";
				}
				else
					editStatus = "Student is already " + newStatus;
			}
		}
		else if (fieldName.equalsIgnoreCase("status_date"))
		{
			if (newValue.trim().length() == 0)
				editStatus = "New value cannot be null";
			else
			{
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
				Date newDate = null;
				try
				{
					newDate = dateFormat.parse(newValue);
					String newParsedDateStr = dateFormat.format(newDate);
					boolean dtFormatCorrect = newValue.equalsIgnoreCase(newParsedDateStr);
					if (!dtFormatCorrect)
						editStatus = "Invalid status date format";
				}
				catch(ParseException pe)
				{
					editStatus = "New value for status date is not a date";
				}
				
				if (editStatus == null)
				{
					studentRec.setStatusDate(newDate);
					editStatus = "Edit successful";
				}
			}
		}
		else
			editStatus = "Field " + fieldName + " is not editable for student";		
		
		return editStatus;
	}
	
	/**
	 * Validates the record transfer details before allowing the transfer.
	 * @param 	recordID		Unique ID of the record to be transferred
	 * @param 	remServerName	Acronym for identifying the remote server
	 * @return	Success or failure status message of the validation process
	 */
	private String validateRecTransfer(String recordID, String remServerName)
	{
		String transferStatus = null;
		
		if ((recordID == null) || (recordID.trim().length() == 0))
			transferStatus = "Record ID cannot be null";
		else if ((remServerName == null) || (remServerName.trim().length() == 0))
			transferStatus = "Remote server name cannot be null";
		else if ((!((remServerName.equalsIgnoreCase("MTL"))
				|| (remServerName.equalsIgnoreCase("LVL"))
				|| (remServerName.equalsIgnoreCase("DDO")))))
			transferStatus = "Invalid remote server name";
		else if (remServerName.equalsIgnoreCase(centerID))
			transferStatus = "Source and destination servers cannot be the same";
		
		return transferStatus;
	}
	
	/**
	 * Fetches remote Center Server hostname and port number based on the Center ID.
	 * @param	centerID	Acronym for identifying the remote server
	 * @return	Center Server hostname and port number
	 */
	private String[] getServerDetails(String centerID)
	{
		String[] serverDetails = new String[2];
		String hostname = "localhost";
		String port = null;
		
		if (centerID.equalsIgnoreCase("MTL"))
			port = "6795";
		else if (centerID.equalsIgnoreCase("LVL"))
			port = "6796";
		else if (centerID.equalsIgnoreCase("DDO"))
			port = "6797";
		
		serverDetails[0] = hostname;
		serverDetails[1] = port;
		
		return serverDetails;
	}
	
	/**
	 * Deletes a record from this center server's database.
	 * @param 	targetRec	Record to be deleted
	 */
	private void deleteRecord(Record targetRec)
	{
		List<Record> targetList = null;
		String targetRecordID = targetRec.getRecordID();
		
		//Locating the list containing the target record
		for (Map.Entry<Character, List<Record>> entry : centerRecords.entrySet())
		{
			List<Record> recList = entry.getValue();
			for (Record rec : recList)
			{
				if (rec.getRecordID().equalsIgnoreCase(targetRecordID))
				{
					targetList = recList;
					break;
				}
			}
			
			if (targetList != null)
				break;
		}		
		
		//Deleting the target record from its list
		if (targetList != null)
		{
			int targetIndex = targetList.indexOf(targetRec);
			targetList.remove(targetIndex);
		}
	}
	
	/**
	 * Checks if the input String contains only digits.
	 * @param 	numberString	String to be evaluated
	 * @return	true, if the String contains only digits
	 * 			false, otherwise
	 */
	private boolean isNumber(String numberString)
	{
		for (int i=0; i<numberString.length(); i++)
		{
			char currChar = numberString.charAt(i);
			if (!((currChar >= '0') && (currChar <= '9')))
				return false;
		}
		
		return true;
	}
}