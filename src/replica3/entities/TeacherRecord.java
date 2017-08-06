package replica3.entities;

import replica3.entities.Record;

/**
 * Class for defining and managing the attributes of a teacher record.
 * @author Hirangi Naik
 */
public class TeacherRecord extends Record
{
	private static final long serialVersionUID = 1L;
	private String address;
	private String phone;
	private String specialization;
	private String location;
	
	/**
	 * Default (unparameterized) constructor.
	 */
	public TeacherRecord()
	{
		super();		
		address = null;
		phone = null;
		specialization = null;
		location = null;
	}
	
	/**
	 * Constructor with all the attribute values provided as parameters.
	 * @param	recordID		Unique ID to identify the teacher
	 * @param	firstName		First name of the teacher
	 * @param 	lastName		Last name of the teacher
	 * @param 	address			Address of the teacher
	 * @param 	phone			Phone number of the teacher
	 * @param 	specialization	Subject that the teacher specializes in (e.g. French, Math, etc.)
	 * @param 	location		Location of the teacher (e.g. MTL, LVL, etc.)
	 */
	public TeacherRecord(String recordID, String firstName, String lastName, String address, 
							String phone, String specialization, String location)
	{
		super(recordID, firstName, lastName);
		this.address = address;
		this.phone = phone;
		this.specialization = specialization;
		this.location = location;
	}

	/**
	 * Fetches the address of this teacher record.
	 * @return	address of this teacher
	 */
	public String getAddress() 
	{
		return address;
	}

	/**
	 * Fetches the phone number of this teacher record.
	 * @return	phone number of this teacher
	 */
	public String getPhone() 
	{
		return phone;
	}

	/**
	 * Fetches the specialization subject of this teacher record.
	 * @return	specialization subject of this teacher
	 */
	public String getSpecialization() 
	{
		return specialization;
	}

	/**
	 * Fetches the location of this teacher record.
	 * @return	location of this teacher
	 */
	public String getLocation() 
	{
		return location;
	}	
	
	/**
	 * Assigns a specific address to this teacher record.
	 * @param 	address	Address to be assigned
	 */
	public void setAddress(String address) 
	{
		this.address = address;
	}
	
	/**
	 * Assigns a specific phone number to this teacher record.
	 * @param 	phone	Phone number to be assigned
	 */
	public void setPhone(String phone)
	{
		this.phone = phone;
	}
	
	/**
	 * Assigns a specific location to this teacher record.
	 * @param 	location	Location to be assigned
	 */
	public void setLocation(String location) 
	{
		this.location = location;
	}
}