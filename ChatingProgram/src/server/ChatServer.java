package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import member.Member;
import member.MemberRepository;


public class ChatServer {
	static //필드
	ServerSocket serverSocket;
	ExecutorService threadPool = Executors.newFixedThreadPool(100);
	Map<String, SocketClient> chatRoom = Collections.synchronizedMap(new HashMap<>());
	MemberRepository memberRepository = new MemberRepository();
	
	//DB 연결
	public void oracleDB_Connect() throws IOException, SQLException, ClassNotFoundException {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("db.properties"));
			
			Class.forName(prop.getProperty("driverClass"));
			System.out.println("JDBC 드라이버 로딩 성공");
			
			Connection conn1 = DriverManager.getConnection(prop.getProperty("dbServerConn")
					, prop.getProperty("dbUser")
					, prop.getProperty("dbPasswd"));
			
			conn1.setAutoCommit(false);
			System.out.println("DB 서버에 연결됨");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	//메소드: 서버 시작
	public void start() throws IOException {
		
		memberRepository.loadMember();
		
		serverSocket = new ServerSocket(50001);	
		System.out.println( "[서버] 시작됨test");
		
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
	public void addSocketClient(SocketClient socketClient) {
		String key = socketClient.chatName + "@" + socketClient.clientIp;
		chatRoom.put(key, socketClient);
		System.out.println("입장: " + key);
		System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
	}

	//메소드: 클라이언트 연결 종료시 SocketClient 제거
	public void removeSocketClient(SocketClient socketClient) {
		String key = socketClient.chatName + "@" + socketClient.clientIp;
		chatRoom.remove(key);
		System.out.println("나감: " + key);
		System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
	}		
	//메소드: 모든 클라이언트에게 메시지 보냄 + 귓속말
	public void sendToAll(SocketClient sender, String message) {
		JSONObject root = new JSONObject();
		
		
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
			
			chatRoom.values().stream()
			.filter(socketClient -> socketClient != sender)
			.forEach(socketClient -> socketClient.send(json));
		}
		
		
		
		
		
	}	
	//메소드: 서버 종료
	public void stop() {
		try {
			serverSocket.close();
			threadPool.shutdownNow();
			chatRoom.values().stream().forEach(sc -> sc.close());
			System.out.println( "[서버] 종료됨 ");
		} catch (IOException e1) {}
	}
	
	public synchronized void registerMember(Member member) throws Member.ExistMember {
		memberRepository.insertMember(member);
	}
	
	public synchronized Member findByUid(String uid) throws Member.NotExistUidPwd {
		return memberRepository.findByUid(uid);
	}
	

	
	
	
	public void files() {
		try {
	String DATA_DIRECTORY = "c:/down/";
	
	File dir = new File(DATA_DIRECTORY);

	String[] filenames = dir.list();
	for (String filename : filenames) {
	System.out.println("filename : " + filename);
	}}
		catch(Exception e) {
			System.out.println("파일이 없습니다");
		}
	}
	
	
	
	
	public static void main(String[] args) {	
		try {
			ChatServer chatServer = new ChatServer();
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