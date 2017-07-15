package replica3.services.impl;

import replica3.services.RecordManager;

public class RecordManagerImpl implements RecordManager
{	
	private String centerID;
		
	/**
	 * Parameterized constructor.
	 * @param	centerID	Acronym for identifying the server which creates this remote object
	 */
	public RecordManagerImpl(String centerID)
	{
		this.centerID = centerID;
	}
	
	public String createTRecord(String mgrID, String firstName, String lastName, String address, String phone, 
								String specialization, String location)
	{
		return null;
	}

	public String createSRecord(String mgrID, String firstName, String lastName, String coursesRegistered, 
								String status, String statusDate)
	{
		return null;
	}
	
	public String getRecordCounts(String mgrID)
	{
		return null;
	}
	
	public String editRecord(String mgrID, String recordID, String fieldName, String newValue)
	{
		return null;
	}
	
	public String transferRecord (String mgrID, String recordID, String remServerName)
	{
		return null;
	}
}