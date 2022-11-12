package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import chatroom.ChatRoomRepositoryDB;
import client.Env;
import member.Member;
import member.Member.ExistMember;
import member.Member.NotExistUidPwd;
import server.Logger;
import member.MemberRepositoryDB;
import member.MemberRepositoryForDB;


public class ChatServer{
	static //필드
	ServerSocket serverSocket;
	ExecutorService threadPool = Executors.newFixedThreadPool(100);
	Map<String, Map<String, SocketClient>> chatRooms = new HashMap<>();
	MemberRepositoryDB memberRepository = new MemberRepositoryDB();
	Logger logger;
	ChatRoomRepositoryDB chatRoomRepository = new ChatRoomRepositoryDB();
	

	
	//채팅방 리스트
	public List<String> getChatRoomList() {
		return chatRooms.keySet().stream().toList();
	}
	
	
	
	//메소드: 서버 시작
	public void start() throws IOException {
		serverSocket = new ServerSocket(50001);	
		System.out.println( "[서버] 시작됨test");
		logger = new Logger();
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					Socket socket = serverSocket.accept();
					SocketClient sc = new SocketClient(this, socket);
				}
			} catch(IOException e) {
				
			}
		});
		thread.start();
	}
	//메소드: 클라이언트 연결시 SocketClient 생성 및 추가
	public void addSocketClient(SocketClient socketClient) throws NotExistChatRootException {
		Map<String, SocketClient> chatRoom = chatRooms.get(socketClient.getRoomName());
		
		String key = socketClient.getKey();
		chatRoom.put(key, socketClient);
		

		System.out.println("입장: " + key);
		System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
	}

	//메소드: 클라이언트 연결 종료시 SocketClient 제거
	public void removeSocketClient(SocketClient socketClient) throws NotExistChatRootException  {
		Map<String, SocketClient> chatRoom = chatRooms.get(socketClient.getRoomName());
		
		String key = socketClient.getKey();
		chatRoom.remove(key);
		System.out.println("나감: " + key);
		System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
	}		
	//메소드: 모든 클라이언트에게 메시지 보냄 + 귓속말
	public void sendToAll(SocketClient sender, String message) throws NotExistChatRootException  {
		JSONObject root = new JSONObject();
		Map<String, SocketClient> chatRoom = chatRooms.get(sender.getRoomName());
		
		
		// 귓속말 조건
		if(message.indexOf("/") == 0) {

			int pos = message.indexOf(" ");
			String key = message.substring(1, pos); // chatName 타겟 키
			message = "(귓)" + message.substring(pos+1);
			root.put("clientIp", sender.clientIp);
			root.put("chatName", sender.chatName);
			root.put("message", message);
			String json = root.toString();
			
			//타겟 닉네임
			//소켓 클라이언트 변수에 채팅방 values를 대입
			for(SocketClient socketClient : chatRoom.values()) {
				//소켓 클라이언트에 멤버변수인 chatName에 key값(해당이름)이 있을 경우
				if(socketClient.chatName.equals(key)) {
					socketClient.send(json);
				}
			}
		}else {
			root.put("clientIp", sender.clientIp);
			root.put("chatName", sender.chatName);
			root.put("message", message);
			String json = root.toString();
			logger.write(root.toString(), sender.getRoomName());
			chatRoom.values().stream()
			.filter(socketClient -> socketClient != sender)
			.forEach(socketClient -> socketClient.send(json));
		}
		
		
		
		
		
	}	
	//메소드: 서버 종료
	public void stop() {
		try {
			logger.endLogger();
			serverSocket.close();
			threadPool.shutdownNow();
			chatRooms.values().stream().forEach(
				root -> root.values().stream().forEach(sc -> sc.close())
			);
			System.out.println( "[서버] 종료됨 ");
		} catch (IOException e1) {}
	}
	
	

	
	//메소드 : INSERT
	public void insertTest(Member member) throws ExistMember {
		memberRepository.insertMember(member);
		
	}
	
	//메소드 : SELECT
	public Member findByUid(String uid) throws NotExistUidPwd {
		return memberRepository.findByUid(uid);
	}
	
	//메소드 : UPDATE
	public void updateMember(Member member) throws NotExistUidPwd {
		memberRepository.updateMember(member);
		
	}
	
	public void addChatRoom(SocketClient socketClient) throws ExistChatRootException {
		
		if (chatRooms.containsKey(socketClient.getRoomName())) {
			throw new ExistChatRootException();
		}
		chatRooms.put(socketClient.getRoomName(), new HashMap<>());
		
		// 폴더 생성
		String path = Env.getWorkPath() + File.separatorChar + String.valueOf(socketClient.getRoomName().hashCode());
		File chatRoomFileFolder = new File(path);
		System.out.println(chatRoomFileFolder.getAbsolutePath());
		if (!chatRoomFileFolder.exists()) {
			chatRoomFileFolder.mkdirs();
		}

	}

	public static void main(String[] args) {	
		try {
			ChatServer chatServer = new ChatServer();
			//chatServer.oracleDBConnect();
			chatServer.start();
			
			System.out.println("----------------------------------------------------");
			System.out.println("서버를 종료하려면 q를 입력하고 Enter.");
			System.out.println("----------------------------------------------------");
			
			Scanner scanner = new Scanner(System.in);
			
			while(true) {
				String key = scanner.nextLine();
				if(key.equals("q")) 	break;
			}
			scanner.close();
			chatServer.stop();
		} catch(IOException e) {
			System.out.println("[서버] " + e.getMessage());
		}
	}
	

	
	
	
}