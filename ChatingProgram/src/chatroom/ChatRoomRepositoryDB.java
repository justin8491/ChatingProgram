package chatroom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import org.json.JSONObject;

import chatroom.Room.NotExistRoom;
import client.ChatClient;
import client.Env;
import member.Member;

public class ChatRoomRepositoryDB implements ChatRoomRepositoryforDB {

	// 커넥트 멤버변수 초기화
	Connection conn = null;
	PreparedStatement pstmt = null;
	ChatClient chatClient = new ChatClient();
	Room room = new Room();

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
	public synchronized void createChatRoom(Scanner scanner) {
		try {
			open();

			pstmt = conn.prepareStatement(Env.getProperty("INSERT_ROOM"));

			System.out.println("\n1. 채팅방 생성");
			System.out.print("채팅방 : ");
			String roomName = scanner.nextLine();
			pstmt.setString(1, roomName);
			pstmt.executeUpdate();

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

			// throw new Room.NotExistRoom("[" + room_name + "] 의 정보가 존재하지 않습니다");

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

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
