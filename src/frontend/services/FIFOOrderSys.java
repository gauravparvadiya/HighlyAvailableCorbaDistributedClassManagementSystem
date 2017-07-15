package frontend.services;

import frontend.entities.Request;

public class FIFOOrderSys 
{
	public boolean addRequest(Request newRequest)
	{
		return true;
	}
	
	public String sendFirstRequest(String leadServerHostname, int leadServerPort)
	{
		return null;
	}
	
	public void removeFirstRequest()
	{
		
	}
}
