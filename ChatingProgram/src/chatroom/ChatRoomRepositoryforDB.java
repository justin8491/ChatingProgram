package chatroom;

import java.util.Scanner;

import org.json.JSONObject;

import member.Member;
import server.SocketClient;

public interface ChatRoomRepositoryforDB {
	
	void createChatRoom(Scanner scanner); //생성
	void createChatRoomResponse();
	void chatRoomListRequest(); // 조회
	void enterRoomRequest(String roomName, SocketClient socketClient); // 입장
	Room findByName(String name);
	
}
