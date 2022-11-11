package chatroom;

import java.util.Scanner;

import org.json.JSONObject;

public interface ChatRoomRepositoryforDB {
	
	void createChatRoom(String roomName); //생성
	void chatRoomListRequest(); // 조회
	void enterRoomRequest(Scanner scanner); // 입장
	
}
