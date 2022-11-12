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
	
	public String getRoom_id() {
		return room_id;
	}

	public void setRoom_id(String room_id) {
		this.room_id = room_id;
	}

	public Date getCreatedate() {
		return createdate;
	}

	public void setCreatedate(Date createdate) {
		this.createdate = createdate;
	}

	public String getRoom_name() {
		return room_name;
	}

	public void setRoom_name(String room_name) {
		this.room_name = room_name;
	}

	public String getExist() {
		return exist;
	}

	public void setExist(String exist) {
		this.exist = exist;
	}

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
