package frontend.services;

public class ReplicaLeaderManager {

	private static String whoIsLeader = "RM1"; 

	public String getWhoIsLeader() {
		return whoIsLeader;
	}

	public void setWhoIsLeader(String whoIsLeader) {
		ReplicaLeaderManager.whoIsLeader = whoIsLeader;
	}
	
}
