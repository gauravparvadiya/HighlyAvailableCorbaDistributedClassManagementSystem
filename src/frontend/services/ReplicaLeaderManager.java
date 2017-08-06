package frontend.services;

public class ReplicaLeaderManager {

	private static String whoIsLeader = "RM1"; 

	public static String getWhoIsLeader() {
		return whoIsLeader;
	}

	public static void setWhoIsLeader(String whoIsLeader) {
		ReplicaLeaderManager.whoIsLeader = whoIsLeader;
	}
	
	
}
