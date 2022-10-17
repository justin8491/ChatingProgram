package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import org.json.JSONObject;

public class ChatClient {
	//필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String chatName;
	
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
			uid = scanner.nextLine();
			System.out.print("비밀번호 : ");
			pwd = scanner.nextLine();
			
			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "login");
			jsonObject.put("uid", uid);
			jsonObject.put("pwd", pwd);
			
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
	
	public void registerMember(Scanner scanner) {
		
		String uid;
		String pwd;
		String name;
		String sex;
		String address;
		String phone;
		//String email;
		
		try {
			System.out.println("registerMember 성공");
			System.out.print("아이디 : ");
			uid = scanner.nextLine();
			System.out.print("비번 : ");
			pwd = scanner.nextLine();
			System.out.print("이름 : ");
			name = scanner.nextLine();
			System.out.print("성별[남자(M)/여자(F)] : ");
			sex = scanner.nextLine();		
			System.out.print("주소 : ");
			address = scanner.nextLine();
			System.out.print("전화번호 : ");
			phone = scanner.nextLine();
//			System.out.print("이메일 : ");
//			email = scanner.nextLine();

			connect();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "registerMember");
			jsonObject.put("uid", uid);
			jsonObject.put("pwd", pwd);
			jsonObject.put("name", name);
			jsonObject.put("sex", sex);
			jsonObject.put("address", address);
			jsonObject.put("phone", phone);
//			jsonObject.put("email", email);
			String json = jsonObject.toString();
			send(json);
			System.out.println(json);
			registerMemberResponse();
			
			disconnect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
		
		
    public void registerMemberResponse() throws Exception, NoClassDefFoundError, IOException {
        String json = dis.readUTF();
        JSONObject root = new JSONObject(json);
        String statusCode = root.getString("statusCode");
        String message = root.getString("message");
        if (statusCode.equals("0")) {
            System.out.println("회원가입 성공");
        } else {
            System.out.println(message);
            System.out.println("회원가입 실패");
        }
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
	
	//메소드: 메인
	public static void main(String[] args) {		
		try {			
			ChatClient chatClient = new ChatClient();
			boolean stop = false;
			
			while(false == stop) {
				System.out.println();
				System.out.println("1. 로그인");
				System.out.println("2. 회원가입");
				System.out.println("3. 비밀번호검색");
				System.out.println("4. 회원정보수정");
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
				case "4":
					chatClient.updateMember(scanner);
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

	private void updateMember(Scanner scanner) {
		String uid;
		String pwd;
		String name;
		String sex;
		String address;
		String phone;
		
		try {
			System.out.println("\n4. 회원정보수정");
			System.out.print("아이디 : ");
			uid = scanner.nextLine();
			System.out.print("비번 : ");
			pwd = scanner.nextLine();
			System.out.print("이름 : ");
			name = scanner.nextLine();
			System.out.print("성별[남자(M)/여자(F)] : ");
			sex = scanner.nextLine();
			System.out.print("주소 : ");
			address = scanner.nextLine();
			System.out.print("전화번호 : ");
			phone = scanner.nextLine();

			connect();
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "updateMember");
			jsonObject.put("uid", uid);
			jsonObject.put("pwd", pwd);
			jsonObject.put("name", name);
			jsonObject.put("sex", sex);
			jsonObject.put("address", address);
			jsonObject.put("phone", phone);
			String json = jsonObject.toString();
			send(json);
			
			updateMemberResponse();
			
			disconnect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void updateMemberResponse() throws Exception {
		String json = dis.readUTF();
		JSONObject root = new JSONObject(json);
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");
		
		if (statusCode.equals("0")) {
			System.out.println("정상적으로 수정되었습니다");
		} else {
			System.out.println(message);
		}
	}
	
}