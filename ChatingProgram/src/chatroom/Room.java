package chatroom;

import java.sql.Date;

import lombok.Data;
import member.Member;

@Data
public class Room {
	
	private String room_id;
	private Date createdate;
	private String room_name;
	private String exist;
	
	
	public static class ExistRoom extends Exception {
		public ExistRoom(String reason) {
			super(reason);
		}
	}
	
	public static class NotExistRoom extends Exception {
		public NotExistRoom(String reason) {
			super(reason);
		}
	}
	
	
}
