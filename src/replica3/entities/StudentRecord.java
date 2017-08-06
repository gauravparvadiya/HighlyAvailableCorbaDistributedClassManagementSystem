package replica3.entities;

import java.util.Date;
import java.util.List;

/**
 * Class for defining and managing the attributes of a student record.
 * @author Jyotsana Gupta
 */
public class StudentRecord extends Record
{
	private static final long serialVersionUID = 1L;
	private List<String> coursesRegistered;
	private String status;
	private Date statusDate;
	
	/**
	 * Default (unparameterized) constructor.
	 */
	public StudentRecord()
	{
		super();
		coursesRegistered = null;
		status = null;
		statusDate = null;
	}
	
	/**
	 * Constructor with all the attribute values provided as parameters.
	 * @param	recordID			Unique ID to identify the student
	 * @param 	firstName			First name of the student
	 * @param 	lastName			Last name of the student
	 * @param 	coursesRegistered	List of courses that the student has registered for (e.g. French, Math, etc.)
	 * @param 	status				Status of the student (active/inactive)
	 * @param 	statusDate			Date of last status update of the student
	 */
	public StudentRecord(String recordID, String firstName, String lastName, 
							List<String> coursesRegistered, String status, Date statusDate)
	{
		super(recordID, firstName, lastName);
		this.coursesRegistered = coursesRegistered;
		this.status = status;
		this.statusDate = statusDate;
	}

	/**
	 * Fetches the list of registered courses of this student record.
	 * @return	course list of this student
	 */
	public List<String> getCoursesRegistered() 
	{
		return coursesRegistered;
	}

	/**
	 * Fetches the status of this student record.
	 * @return	status of this student
	 */
	public String getStatus() 
	{
		return status;
	}
	
	/**
	 * Fetches the last status update date of this student record.
	 * @return	Status update date of this student
	 */
	public Date getStatusDate()
	{
		return statusDate;
	}
	
	/**
	 * Assigns a specific list of registered courses to this student record.
	 * @param 	coursesRegistered	List of registered courses to be assigned
	 */
	public void setCoursesRegistered(List<String> coursesRegistered) 
	{
		this.coursesRegistered = coursesRegistered;
	}

	/**
	 * Assigns a specific status to this student record.
	 * @param 	status	Status to be assigned
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * Assigns a specific status update date to this student record.
	 * @param 	statusDate	Status update date to be assigned
	 */
	public void setStatusDate(Date statusDate)
	{
		this.statusDate = statusDate;
	}
}