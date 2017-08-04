package replica3.entities;

public class StudentRecord extends Record{

	//private String fname;
	private String lname;
	/**
	 * @return the lname
	 */
	public String getLname() {
		return lname;
	}
	private String coursesRegistered;
	private String status;
	private String statusDueDate;
	//private String id;
	
	
	
	/**
	 * 
	 */
	public StudentRecord() {
		super();
		coursesRegistered = null;
		status = null;
		statusDueDate = null;
	}

	/**
	 * 
	 * @param fname
	 * @param lname
	 * @param coursesRegistered
	 * @param status
	 * @param statusDueDate
	 * @param id
	 */
	public StudentRecord(String fname, String lname, String coursesRegistered, String status,
			String statusDueDate, String id) {
		//this.fname = fname;
		//this.lname = lname;
		super(id,fname,lname);
		this.coursesRegistered = coursesRegistered;
		this.status = status;
		this.statusDueDate = statusDueDate;
		//this.id = id;
	}

	public String getCoursesRegistered() {
		return coursesRegistered;
	}
	public void setCoursesRegistered(String coursesRegistered) {
		this.coursesRegistered = coursesRegistered;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusDueDate() {
		return statusDueDate;
	}
	public void setStatusDueDate(String statusDueDate) {
		this.statusDueDate = statusDueDate;
	}
	
}
