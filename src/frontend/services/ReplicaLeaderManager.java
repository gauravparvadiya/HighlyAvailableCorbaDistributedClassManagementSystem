package frontend.services;

import java.io.Serializable;

public class ReplicaLeaderManager implements Serializable {

	private static String whoIsLeader = "RM1"; 

	public static String getWhoIsLeader() {
		return whoIsLeader;
	}

	public static void setWhoIsLeader(String whoIsLeader) {
		ReplicaLeaderManager.whoIsLeader = whoIsLeader;
	}
	
	
}
