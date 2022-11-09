package chatroom;

import java.util.Scanner;

import org.json.JSONObject;

public interface ChatRoomRepositoryforDB {
	
	void createChatRoom(Scanner scanner); //생성
	void chatRoomListRequest(); // 조회
	void enterRoomRequest(Scanner scanner); // 입장
	
}
