package replica2.entities;

import java.io.Serializable;

/**
 * Parent class for different record types, such as TeacherRecord, StudentRecord, etc.
 * @author Jyotsana Gupta
 */
public class Record implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected String recordID;
	protected String firstName;
	protected String lastName;
	
	/**
	 * Default (unparameterized) constructor.
	 */
	public Record()
	{
		recordID = null;
		firstName = null;
		lastName = null;
	}
	
	/**
	 * Constructor with all the attribute values provided as parameters.
	 * @param	recordID		Unique ID to identify the record
	 * @param	firstName		First name of the person
	 * @param 	lastName		Last name of the person
	 */
	public Record(String recordID, String firstName, String lastName)
	{
		this.recordID = recordID;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	/**
	 * Fetches the record ID of this record.
	 * @return	Record ID of this record
	 */
	public String getRecordID() 
	{
		return recordID;
	}

	/**
	 * Fetches the first name of this record.
	 * @return	first name of this record
	 */
	public String getFirstName() 
	{
		return firstName;
	}

	/**
	 * Fetches the last name of this record.
	 * @return	last name of this record
	 */
	public String getLastName() 
	{
		return lastName;
	}
}