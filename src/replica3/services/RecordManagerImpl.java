package replica3.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jdk.nashorn.internal.parser.JSONParser;
import replica3.services.RecCtrClientThread;
import replica3.services.RecTransferClientThread;
import replica3.utilities.CenterServerUtil;
import replica3.entities.Record;
import replica3.entities.StudentRecord;
import replica3.entities.TeacherRecord;

public class RecordManagerImpl
{	
	/**
	 * Counter for unique ID of student-teacher records for a server.
	 */
	private int lastrecordIDCounter;
	
	/**
	 * Collection of student and teacher records for a server.
	 */
	private Map<Character, List<Record>> centerRecords;
	
	/**
	 * Object used as a lock for record ID counter manipulation
	 */
	private final Object recordIDCtrLock = new Object();
	
	private static final String LOG_FILE_PATH = "D:\\";
	private String fileName;
	
	private String centerID;
	
	
	/**
	 * Parameterized constructor.
	 * @param	centerID	Acronym for identifying the server which creates this remote object
	 */
	public RecordManagerImpl(String centerID)
	{
		lastrecordIDCounter = 10000;
		centerRecords = new HashMap<Character, List<Record>>();
		this.centerID = centerID;
		fileName = LOG_FILE_PATH + centerID + ".txt";
		
		//Populating the records hashmap with some initial hard-coded records
		createInitialRecords(centerID);
	}
	
	public String createTRecord(String mgrID, String firstName, String lastName, String address, String phone, 
								String specialization, String location)
	{		
		String lastTRecordId = "TR" + "" + ++lastrecordIDCounter;
		TeacherRecord t = new TeacherRecord(lastTRecordId,firstName, lastName, address, phone, specialization, location);
		//Logging the operation
		String logText = "Create T Rec (ID: " + lastTRecordId
												+ " | FN: " + firstName 
												+ " | LN: " + lastName 
												+ " | ADR: " + address 
												+ " | PH: " + phone 
												+ " | SPL: " + specialization 
												+ " | LOC: " + location 
												+ ")" + mgrID ;
		CenterServerUtil.writeToFile(fileName, logText);
		if(addToMap(t))
			return "success";
		else
			return "fail";
	}

	public String createSRecord(String mgrID, String firstName, String lastName, String coursesRegistered, 
								String status, String statusDate)
	{
		String lastSRecordId = "TR" + "" + ++lastrecordIDCounter;
		StudentRecord sRecord = new StudentRecord(lastSRecordId, firstName, lastName, coursesRegistered, status, statusDate);
		//Logging the operation
				String logText = "Create T Rec (ID: " + lastSRecordId
						+ " | FN: " + firstName 
						+ " | LN: " + lastName 
						+ " | CR: " + coursesRegistered 
						+ " | ST: " + status 
						+ " | SDT: " + statusDate
														+ ")" + mgrID ;
				CenterServerUtil.writeToFile(fileName, logText);
		if(addToMap(sRecord))
			return "success";
		else
			return "fail";
	}
	
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
		int ownRecCount = getCount();
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
	
	public String transferRecord (String mgrID, String recordID, String remServerName)
	{
					List<Record> targetList = locateRecList(recordID);
					String transferStatus=null;
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
			port = "6799";
		else if (centerID.equalsIgnoreCase("LVL"))
			port = "6800";
		else if (centerID.equalsIgnoreCase("DDO"))
			port = "6801";
		
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
	 * Validates the new value to be assigned and sets the intended student record field to it.
	 * @param 	fieldName	Name of the field in the record to be updated
	 * @param 	newValue	New value to be assigned to the field
	 * @param 	studentRec	Reference to the record to be updated
	 * @return	Success or failure message according to the execution status of the update operation
	 */
	private String updateSRecord(String fieldName, String newValue, StudentRecord studentRec)
	{
		String editStatus = null;
		
		
		if (fieldName.equalsIgnoreCase("status"))
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
					Date d=new Date();
					String statusDueDate=d.toString();
					studentRec.setStatusDueDate(statusDueDate);
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
					studentRec.setStatusDueDate(newDate.toString());
					editStatus = "Edit successful";
				}
			}
		}
		else
			editStatus = "Field " + fieldName + " is not editable for student";		
		
		return editStatus;
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
	 * Fetches the connection details of all center servers (required for UDP/IP communication).
	 * @return	Server connection details (center ID, hostname and port number)
	 */
	private Map<String, List<String>> getAllServers()
	{
		Map<String, List<String>> serverConnDetails = new HashMap<String, List<String>>();
		
		//Hard-coded connection details, for now; later, could be changed to file read
		serverConnDetails.put("MTL", new ArrayList<String>(Arrays.asList("localhost", "6799")));
		serverConnDetails.put("LVL", new ArrayList<String>(Arrays.asList("localhost", "6800")));
		serverConnDetails.put("DDO", new ArrayList<String>(Arrays.asList("localhost", "6801")));
		
		return serverConnDetails;
	}
	
	/**
	 * Adds a new teacher or student record to the server's record hashmap.
	 * @param 	record	Teacher or student record to be added
	 * @return	true, if the record is successfully added
	 */
	public boolean addToMap(Record record)
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
	 * Calculates the teacher-student record count of this server.
	 * @return	Record count of this server
	 */
	public int getCount()
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
		bRec = new StudentRecord("SR00003", "Frank", "Buffay","Math,French", 
									"active", "2016/08/28");
		
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
		
			jRec = new StudentRecord("SR00006", "Tag", "Jones", "Fashion Design,Spanish", 
										"active", "2015/05/02");
			mRec = new StudentRecord("SR00007", "Gavin", "Mitchell", "Computer Science", 
										"inactive","2017/04/12");
			yRec = new StudentRecord("SR00008", "Ethan", "Young", "Geology, Zoology", 
										"active", "2016/12/02");
			sRec = new StudentRecord("SR00009", "Mona", "Simmers", "Arts, Botany", 
										"inactive", "2015/05/02");
		
		
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
}