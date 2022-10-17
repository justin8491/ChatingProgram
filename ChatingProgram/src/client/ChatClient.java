package client;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Member;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.json.JSONObject;

import server.ChatServer;
public class ChatClient {
	private static final String String = null;
	//필드
	
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String chatName;
	Scanner sc = new Scanner(System.in);
	
	
	
	//메소드: 서버 연결
	public  void connect() throws IOException {
		socket = new Socket("localhost", 50001);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		System.out.println("[클라이언트] 서버에 연결됨");		
	}
	
	//메소드: JSON 받기
	public void receive() {
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					String json = dis.readUTF();
					JSONObject root = new JSONObject(json);
					String clientIp = root.getString("clientIp");
					String chatName = root.getString("chatName");
					String message = root.getString("message");
					System.out.println("<" + chatName + "@" + clientIp + "> " + message);
				}
			} catch(Exception e1) {
				System.out.println("[클라이언트] 서버 연결 끊김");
				System.exit(0);
			}
		});
		thread.start();
	}

	//메소드: JSON 보내기
	public void send(String json) throws IOException {
		dos.writeUTF(json);
		dos.flush();
	}
	
	//메소드: 서버 연결 종료
	public void disconnect() throws IOException {
		socket.close();
	}	
	
	public void login(Scanner scanner) {
		try {
			String uid;
			String pwd;
			
			System.out.println("\n1. 로그인 작업");
			System.out.print("아이디 : ");
			String uid1 = sc.nextLine();
			System.out.print("비밀번호 : ");
			String pwd1 = sc.nextLine();
			
			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "login");
			jsonObject.put("uid", uid1);
			jsonObject.put("pwd", pwd1);
			
			System.out.println("jsonObject = " + jsonObject.toString());
			send(jsonObject.toString());
			
			loginResponse();

			disconnect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loginResponse() throws Exception {
		String json = dis.readUTF();
		JSONObject root = new JSONObject(json);
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");
		
		if (statusCode.equals("0")) {
			System.out.println("로그인 성공");
		} else {
			System.out.println(message);
		}
	}
	
	void registerMember(Scanner scanner) {
		System.out.println("회원가입 입니다.\n");
		System.out.println("아이디를 입력하세요");
		String uid = sc.nextLine();
		System.out.println("비밀번호를 입력하세요");
		String pwd = sc.nextLine();
		System.out.println("휴대폰번호를 입력하세요");
		String number = sc.nextLine();
		System.out.println("성병을 입력하세요");
		String sex = sc.nextLine();
		System.out.println("이름을 입력하세요");
		String name = sc.nextLine();
		System.out.println("이메일을 입력하세요");
		String email = sc.nextLine();
		
		
	}
	


	public void memberInformation(Scanner scanner) {
		
		
		
		
		
		
		
	}
	
	
	
	
	public void passwdSearch(Scanner scanner) {
		try {
			String uid;
			
			System.out.println("\n3. 비밀번호 찾기");
			System.out.print("아이디 : ");
			uid = scanner.nextLine();

			connect();
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "passwdSearch");
			jsonObject.put("uid", uid);
			String json = jsonObject.toString();
			send(json);
			
			passwdSearchResponse();
			
			disconnect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void passwdSearchResponse() throws Exception {
		String json = dis.readUTF();
		JSONObject root = new JSONObject(json);
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");
		
		if (statusCode.equals("0")) {
			System.out.println("비밀번호 : " + root.getString("pwd"));
		} else {
			System.out.println(message);
		}
	}
	
	
	public void fileUpload(Scanner scanner) {
		
		try { 
			Socket sock=new Socket("localhost",50001);
			System.out.println("파일주소를 입력하세요:");
			String fileName=scanner.next();
			File f=new File(fileName);
			DataOutputStream dos=new DataOutputStream(sock.getOutputStream());
			dos.writeUTF(f.getName());
			dos.flush();
			FileInputStream fis=new FileInputStream(fileName);
		
		dos.close();
		fis.close();
		System.out.println("파일전송 완료");
		sock.close();}
		catch(UnknownHostException ue) {
			System.out.println(ue.getMessage());
		}
		catch(IOException ie) {
			System.out.println(ie.getMessage());
		}}
		
	public void downloadName(Scanner scanner) {
		System.out.println("파일명을 입력하세요:");
		String fileName=scanner.next();
		String path="c:\\down\\";
		String file=path+fileName;
		System.out.println(file+"을 다운로드 합니다");
	}
	
	//메소드: 메인
	public static void main(String[] args) {		
		try {			
			ChatClient chatClient = new ChatClient();
			boolean stop = false;
			ChatServer chatServer =new ChatServer();
			while(false == stop) {
				System.out.println();
				System.out.println("1. 로그인");
				System.out.println("2. 회원가입");
				System.out.println("3. 비밀번호검색");
				System.out.println("4. 회원정보수정");
				System.out.println("5. 파일업로드");
				System.out.println("6. 서버파일목록");
				System.out.println("q. 프로그램 종료");
				System.out.print("메뉴 선택 => ");
				Scanner scanner = new Scanner(System.in);
				String menuNum = scanner.nextLine();
				switch(menuNum) {
				case "1":
					chatClient.login(scanner);
					break;
				case "2":
					chatClient.registerMember(scanner);
					break;
				case "3":
					chatClient.passwdSearch(scanner);
					break;
				case"4":
					chatClient.memberInformation(scanner);
				case "5":
					chatClient.fileUpload(scanner);
					break;
				case "6":
					chatServer.files();
					chatClient.downloadName(scanner);
					break;
				case "Q", "q":
					scanner.close();
					stop = true;
					System.out.println("프로그램 종료됨");
					break;
				}
			}
			
//			ChatClient chatClient = new ChatClient();
//			chatClient.connect();
//			System.out.println("대화명 입력: ");
//			chatClient.chatName = scanner.nextLine();
//			
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("command", "incoming");
//			jsonObject.put("data", chatClient.chatName);
//			String json = jsonObject.toString();
//			chatClient.send(json);
//			
//			chatClient.receive();			
//			
//			System.out.println("--------------------------------------------------");
//			System.out.println("보낼 메시지를 입력하고 Enter");
//			System.out.println("채팅를 종료하려면 q를 입력하고 Enter");
//			System.out.println("--------------------------------------------------");
//			while(true) {
//				String message = scanner.nextLine();
//				if(message.toLowerCase().equals("q")) {
//					break;
//				} else {
////					jsonObject = new JSONObject();
//					jsonObject.put("command", "message");
//					jsonObject.put("data", message);
//					chatClient.send(jsonObject.toString());
//				}
//			}
//			scanner.close();
//			chatClient.unconnect();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}
	}
}