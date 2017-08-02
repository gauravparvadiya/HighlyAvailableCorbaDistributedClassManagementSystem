/**
 * 
 */
package failuredetectionsys;

/**
 * @author gauravparvadiya
 *
 */
public class ReplicaInfo {

	private int portNo;
	private String hostName;
	private Boolean isLeader;
	
	
	
	/**
	 * @param portNo
	 * @param hostName
	 * @param isLeader
	 */
	public ReplicaInfo(int portNo, String hostName, Boolean isLeader) {
		super();
		this.portNo = portNo;
		this.hostName = hostName;
		this.isLeader = isLeader;
	}

	/**
	 * @return the portNo
	 */
	public int getPortNo() {
		return portNo;
	}

	/**
	 * @param portNo the portNo to set
	 */
	public void setPortNo(int portNo) {
		this.portNo = portNo;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return the isLeader
	 */
	public Boolean getIsLeader() {
		return isLeader;
	}

	/**
	 * @param isLeader the isLeader to set
	 */
	public void setIsLeader(Boolean isLeader) {
		this.isLeader = isLeader;
	}

}
