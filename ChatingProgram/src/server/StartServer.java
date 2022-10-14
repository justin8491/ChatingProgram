package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;



public class StartServer {

	ServerSocket serverSocket;
	
	//스레드 MAX 설정
	ExecutorService threadPool = Executors.newFixedThreadPool(100);
	
	Map<String, SocketClient> chatRoom = Collections.synchronizedMap(new HashMap<>());
	
	
	
	

	// 서버 시작 후 소켓 클라이언트 연결대기 상태
	public void start() throws IOException {
		serverSocket = new ServerSocket(50001);
		System.out.println("[서버] 시작됨");

		Thread thread = new Thread(() -> {
			try {
				while (true) {
					Socket socket = serverSocket.accept();

					// 클래스 만들고 변경 필요
					SocketClient sc = new SocketClient(this, socket);
				}
			} catch (IOException e) {
			}
		});
		thread.start();
	}

	// 클라이언트 연결시 소켓클라이언트 생성 및 추가
	public void addSocketClient(SocketClient socketClient) {
		String key = socketClient.chatName + "@" + socketClient.clientIp;
		chatRoom.put(key, socketClient);
		System.out.println("입장: " + key);
		System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
	}

	public void sendToAll(SocketClient sender, String message) {
		JSONObject root = new JSONObject();
		root.put("clientIp", sender.clientIp);
		root.put("chatName", sender.chatName);
		root.put("message", message);
		String json = root.toString();

		if(message.indexOf("/") == 0) {
			int pos = message.indexOf(" ");
			String key = message.substring(1, pos);
			message = "(귓)" + message.substring(pos+1);
			
			//닉네임 Target
			for(SocketClient socketClient : chatRoom.values()) {
				if(socketClient.chatName.equals(key)) {
					socketClient.send(json);
				}
			}
			
			//닉네임+아이피 Target
//			SocketClient targetClient = chatRoom.get(key);
//			
//			if(null != targetClient) {
//				targetClient.send(json);
//			}

		} else {
			chatRoom.values().stream()
			.filter(SocketClient -> SocketClient != sender)
			.forEach(SocketClient -> SocketClient.send(json));
		}
		
		
//		Collection<SocketClient> socketClients = chatRoom.values();
//		for(SocketClient sc : socketClients) {
//			if(sc == sender) continue;
//			sc.send(json);
//		}
	}
	/*
	// 귓속말 멘션
	public void whisper(SocketClient from, SocketClient to, String message) {
		JSONObject root = new JSONObject();
		//귓속말
		
		//방식  
		//닉네임 채팅
		
		// 소켓에 /닉네임 이 적 힐 경우
		// 해당 닉네임 키값 구하기
		
		//대화명에 해당하는 Socket의 OutputStream 객체 구하기
		root.put("clientIp", from.clientIp);
		root.put("chatName", from.chatName);
		root.put("message", message);
//		StartServer startServer = new StartServer();
//		startServer.stop();
		String json = root.toString();
		
		
		
		
		
	}
	*/

	public void stop() {
		try {
			serverSocket.close();
			// threadPool.shutdownNow();
			chatRoom.values().stream().forEach(sc -> sc.close());
			System.out.println("[서버] 종료");
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) {
		
		StartServer startServer = new StartServer(); //서버 객체 선언
		try {
			
			startServer.start();
			
			System.out.println("----------------------------------------------------");
			System.out.println("서버를 종료하려면 q를 입력하고 Enterasdf");
			System.out.println("----------------------------------------------------");
			
			Scanner scanner = new Scanner(System.in);
			
			while (true) {
				String key = scanner.nextLine();
				if(key.equals("q")) break;
			}
			
			scanner.close();
			startServer.stop();
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("[서버] " + e.getMessage());
		}
		
		
		
	}

}
