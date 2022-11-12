package chatroom;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import chatroom.Room.NotExistRoom;
import client.ChatClient;
import client.Env;
import member.Member;
import server.ChatServer;
import server.SocketClient;

public class ChatRoomRepositoryDB implements ChatRoomRepositoryforDB {

	// 커넥트 멤버변수 초기화
	Connection conn = null;
	PreparedStatement pstmt = null;
	ChatClient chatClient = new ChatClient();
	Room room = new Room();

	Map<String, Map<String, SocketClient>> chatRooms;

	// DB Connect
	public void open() {
		try {
			Class.forName(Env.getProperty("driverClass"));
			System.out.println("JDBC 드라이버 로딩 성공");

			conn = DriverManager.getConnection(Env.getProperty("dbServerConn"), Env.getProperty("dbUser"),
					Env.getProperty("dbPasswd"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// DB close
	private void close() {
		try {
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	

	// 생성
	@Override
	public synchronized void createChatRoom(String roomName) {
		try {
			open();
			System.out.println(roomName);
			pstmt = conn.prepareStatement(Env.getProperty("INSERT_ROOM"));

			pstmt.setString(1, roomName);
			pstmt.executeUpdate();

			if (roomName != null)
				System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("------------- 실패 사유 : " + e.getMessage());
		} finally {
			close();
		}

	}

	// 목록
	@Override
	public void chatRoomListRequest() {
		try {
			open();

			pstmt = conn.prepareStatement(Env.getProperty("ROOM_LIST"));

			// 멤버 존재여부 확인
			ResultSet rs = pstmt.executeQuery();
			int cnt = 0;
			String room_name = null;
			while (rs.next()) {
				cnt += 1;

				System.out.print("# " + cnt + "\t");
				System.out.print("채팅방 번호 : " + rs.getString(1) + " ");
				System.out.print("만든 시간 : " + rs.getDate(2) + " ");
				System.out.print("채팅방 이름: " + rs.getString(3) + " ");
				if (rs.getString(4) != "1") {
					System.out.print("존재 여부: O ");
				} else {
					System.out.print("존재 여부: X ");
				}

				System.out.println();

			}

			System.out.println();
			// throw new Room.NotExistRoom("채팅방이 존재하지 않습니다. 채팅방을 생성해주세요.");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("------------- 실패 사유 : " + e.getMessage());
		} finally {
			close();
		}

	}

	// 입장
	@Override
	public void enterRoomRequest(Scanner scanner) {
		try {
			open();
		} catch (Exception e) {

		} finally {
			close();
		}
	}

	public Room findByUid(String room_id) throws Member.NotExistUidPwd {
		try {

			open();

			pstmt = conn.prepareStatement(Env.getProperty("findByRoom_ID"));

			// 멤버 존재여부 확인
			pstmt.setString(1, room_id);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {

				room.setRoom_id(rs.getString("ROOM_ID"));
				room.setCreatedate(rs.getDate("CREATEDATE"));
				room.setRoom_name(rs.getString("ROOM_NAME"));
				room.setExist(rs.getString("EXIST"));

				rs.close();
				return room;
			} else {
				throw new Member.NotExistUidPwd("[채팅방] DB 값 없음"/* "[" + room_id + "] 아이디가 존재하지 않습니다" */);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("------------- 실패 사유 : " + e.getMessage());
		} finally {
			close();
		}
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
