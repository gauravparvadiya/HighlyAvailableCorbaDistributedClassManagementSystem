package replica3.entities;

public class TeacherRecord extends Record{
	
	private static final long serialVersionUID = 1L;
	private String address;
	private String phone;
	private String specialization;
	private String location;
	private String lname;

	/**
	 * @return the lname
	 */
	public String getLname() {
		return lname;
	}

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
	 * @param fname
	 * @param lname
	 * @param address
	 * @param phone
	 * @param specialization
	 * @param location
	 * @param id
	 */
	public TeacherRecord(String id,String fname, String lname, String address, String phone, String specialization, String location) {
		super();
		this.address = address;
		this.phone = phone;
		this.specialization = specialization;
		this.location = location;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the specialization
	 */
	public String getSpecialization() {
		return specialization;
	}

	/**
	 * @param specialization the specialization to set
	 */
	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	
}
