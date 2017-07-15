package replica3.services;

/**
 * Center server interface containing methods for student-teacher record management.
 * @author Jyotsana Gupta
 */
public interface RecordManager
{
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
	String createTRecord (String mgrID, String firstName, String lastName, String address, String phone, String specialization, String location);
  
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
	String createSRecord (String mgrID, String firstName, String lastName, String coursesRegistered, String status, String statusDate);
  
	/**
	 * Fetches the record counts of all center servers.
	 * @param	mgrID	Unique ID of the center manager who performs this operation
	 * @return	Record counts of all center servers
	 */
	String getRecordCounts (String mgrID);
  
	/**
	 * Updates a particular field of an existing teacher or student record with a new value.
	 * @param	mgrID		Unique ID of the center manager who performs this operation
	 * @param 	recordID	Unique ID for identifying the record to be updated
	 * @param 	fieldName	Name of the field in the record to be updated
	 * @param 	newValue	New value to be assigned to the field
	 * @return	Success or failure message according to the execution status of the edit operation
	 */
	String editRecord (String mgrID, String recordID, String fieldName, String newValue);
  
	/**
	 * Transfers a record from one Center Server database to another.
	 * @param	mgrID			Unique ID of the center manager who performs this operation
	 * @param	recordID		Unique ID for identifying the record to be transferred
	 * @param	remServerName	Acronym for identifying the destination server
	 * @return	Success or failure message according to the execution status of the transfer operation
	 */
	String transferRecord (String mgrID, String recordID, String remServerName);
}