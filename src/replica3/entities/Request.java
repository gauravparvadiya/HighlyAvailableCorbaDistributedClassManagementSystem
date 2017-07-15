package replica3.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Class for representing a client Request and managing its attributes.
 * @author Jyotsana Gupta
 */
public class Request implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String methodName;
	private List<String> methodArgs;
	
	/**
	 * Default (unparameterized) constructor.
	 */
	public Request()
	{
		methodName = null;
		methodArgs = null;
	}
	
	/**
	 * Constructor with all the attribute values provided as parameters.
	 * @param	methodName	Name of the method to be invoked on server
	 * @param	methodArgs	Arguments required as input for the method
	 */
	public Request(String methodName, List<String> methodArgs)
	{
		this.methodName = methodName;
		this.methodArgs = methodArgs;
	}
	
	/**
	 * Fetches the method name of this request.
	 * @return	Method name of this request
	 */
	public String getMethodName() 
	{
		return methodName;
	}

	/**
	 * Fetches the method arguments of this request.
	 * @return	Method arguments of this request
	 */
	public List<String> getMethodArgs() 
	{
		return methodArgs;
	}
}