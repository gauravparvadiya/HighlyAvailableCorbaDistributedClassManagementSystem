package client.corbasupport.RecordManagerApp;


/**
* RecordManagerApp/RecordManagerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from RecordManager.idl
* Sunday, July 9, 2017 10:07:55 PM EDT
*/

public interface RecordManagerOperations
{
	/**
	 * Communicates a client request to create a new teacher record to the lead server via the FIFO System.
	 * @param	mgrID			Unique ID of the center manager who performs this operation
	 * @param	firstName		First name of the teacher
	 * @param 	lastName		Last name of the teacher
	 * @param 	address			Address of the teacher
	 * @param 	phone			Phone number of the teacher
	 * @param 	specialization	Subject that the teacher specializes in (e.g. French, Math, etc.)
	 * @param 	location		Location of the teacher (e.g. MTL, LVL, etc.)
	 * @return	Success or failure status message of the operation
	 */
	String createTRecord (String mgrID, String firstName, String lastName, String address, String phone, String specialization, String location);
	
	/**
	 * Communicates a client request to create a new student record to the lead server via the FIFO System.
	 * @param	mgrID				Unique ID of the center manager who performs this operation
	 * @param 	firstName			First name of the student
	 * @param 	lastName			Last name of the student
	 * @param 	coursesRegistered	List of courses that the student has registered for (e.g. French, Math, etc.)
	 * @param 	status				Status of the student (active/inactive)
	 * @param 	statusDate			Date of last status update of the student
	 * @return	Success or failure status message of the operation
	 */
	String createSRecord (String mgrID, String firstName, String lastName, String coursesRegistered, String status, String statusDate);
	
	/**
	 * Communicates a client request to fetch server record counts to the lead server via the FIFO System.
	 * @param	mgrID	Unique ID of the center manager who performs this operation
	 * @return	Record counts of all center servers, if successful
	 * 			Failure status message, otherwise
	 */
	String getRecordCounts (String mgrID);
	
	/**
	 * Communicates a client request to edit an existing record to the lead server via the FIFO System.
	 * @param	mgrID		Unique ID of the center manager who performs this operation
	 * @param 	recordID	Unique ID for identifying the record to be updated
	 * @param 	fieldName	Name of the field in the record to be updated
	 * @param 	newValue	New value to be assigned to the field
	 * @return	Success or failure status message of the operation
	 */
	String editRecord (String mgrID, String recordID, String fieldName, String newValue);
	
	/**
	 * Communicates a client request to transfer a record to another Center Server to the lead server via the FIFO System.
	 * @param	mgrID			Unique ID of the center manager who performs this operation
	 * @param	recordID		Unique ID for identifying the record to be transferred
	 * @param	remServerName	Acronym for identifying the destination server
	 * @return	Success or failure status message of the operation
	 */
	String transferRecord (String mgrID, String recordID, String remServerName);
	
	/**
	 * Shuts down the lead server process to simulate a crash.
	 */
	String crashLeadServer ();
	
	/**
	 * Shuts down a secondary server process to simulate a crash.
	 */
	String crashSecondaryServer ();
} // interface RecordManagerOperations