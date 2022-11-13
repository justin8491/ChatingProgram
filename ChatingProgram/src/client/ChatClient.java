package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONObject;

import chatroom.ChatRoomRepositoryDB;
import member.Member;
import member.MemberRepositoryDB;

public class ChatClient {
	// 필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	public static String chatName;
	String roomName;

	// 객체 필드
	static ChatClient chatClient = new ChatClient();
	//static Scanner scanner;
	static Scanner scanner = new Scanner(System.in);
	Member member = new Member();

//	public ChatClient(){
//		scanner = new Scanner(System.in);
//	}
	// 로그인 멤버 객체 값
	public static Member logon = null;

	// 채팅방 정보 JSON 구조 ({no : 채팅방번호, roomName : "채팅방이름"})
	static List<String> chatRooms = new ArrayList<String>();

	// UI 반복문 여부
	static boolean stop = false;

	// 메소드: 서버 연결
	public void connect() throws IOException {
		socket = new Socket("localhost", 50001);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		System.out.println("[클라이언트] 서버에 연결됨");
	}

	// 메소드: JSON 받기
	public void receive() {
		Thread thread = new Thread(() -> {
			try {
				while (true) {
					String json = serverDataRead();
					JSONObject root = new JSONObject(json);
					String clientIp = root.getString("clientIp");
					String chatName = root.getString("chatName");
					String message = root.getString("message");
					System.out.println("<" + chatName + "> " + message);
				}
			} catch (Exception e1) {
				System.out.println("[클라이언트] 서버 연결 끊김");
				System.exit(0);
			}
		});
		thread.start();
	}

	// 메소드: JSON 보내기
	public void send(String json) throws IOException {
		byte[] data = json.getBytes("UTF8");

		dos.writeInt(data.length);// 문자열의 길이(4byte)
		dos.write(data);// 내용
		dos.flush();
	}

	// 메소드: 서버 연결 종료
	public void disconnect() throws IOException {
		System.out.println("서버 연결 종료");
		socket.close();
	}

	public void login(Scanner scanner) {
		try {
			scanner = new Scanner(System.in);
			MemberRepositoryDB memberRepository = new MemberRepositoryDB();
			
			String uid;
			String pwd;

			System.out.println("\n1. 로그인 작업");
			System.out.print("아이디 : ");
			uid = scanner.nextLine();
			System.out.print("비밀번호 : ");
			pwd = scanner.nextLine();
			member = memberRepository.findByUid(uid);
			String exist = member.getExist();
			System.out.println();
			if (!pwd.equals(member.getPwd()) || exist.equals("0")) {
				System.out.println("로그인 실패");
			} else {
			
			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "login");
			jsonObject.put("uid", uid);
			jsonObject.put("pwd", pwd);
			
			logon = member;
			chatName = member.getName();
			
			send(jsonObject.toString());
			
			loginResponse();
			
			disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loginResponse() throws Exception {
		String json = serverDataRead();
		
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
		
		try {
			System.out.print("아이디 : ");
			uid = scanner.nextLine();

			// 호출 닉네임 저장

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
			jsonObject.put("command", "registerMember");
			jsonObject.put("uid", uid);
			jsonObject.put("pwd", pwd);
			jsonObject.put("name", name);
			jsonObject.put("sex", sex);
			jsonObject.put("address", address);
			jsonObject.put("phone", phone);
			jsonObject.put("exist", "1");
			send(jsonObject.toString());
			
			registerMemberResponse();

			disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerMemberResponse() throws Exception, NoClassDefFoundError, IOException {
		String json = serverDataRead();
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

			send(jsonObject.toString());

			passwdSearchResponse();

			disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void passwdSearchResponse() throws Exception {
		String json = serverDataRead();
		JSONObject root = new JSONObject(json);
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");

		if (statusCode.equals("0")) {
			System.out.println("비밀번호 : " + root.getString("pwd"));
		} else {
			System.out.println(message);
		}
	}

	public void loginSucessMenu() throws EOFException {
		stop = false;
		try {
			MemberRepositoryDB memberRepository = new MemberRepositoryDB();
			
			//Member nameKey = memberRepository.findByName(chatName);
			while (false == stop && logon != null) {
				
				
				System.out.println("-------------------------------------");
				System.out.println("	" + chatName + " 님 환영합니다.");
				System.out.println("-------------------------------------");
				System.out.println();
				System.out.println("1. 채팅방 생성");
				System.out.println("2. 채팅방 목록");
				System.out.println("3. 채팅방 입장");
				System.out.println("4. 나의 회원 정보");
				System.out.println("5. 회원정보수정");
				System.out.println("6. 채팅로그 검색");
				System.out.println("q. 로그아웃");
				System.out.print("메뉴 선택 => ");
				Scanner scanner = new Scanner(System.in);
				String menuNum = scanner.nextLine();
				switch (menuNum) {
				case "1":
					createChatRoom(scanner);
					break;
				case "2":
					chatRoomListRequest(scanner);
					break;
				case "3":
					enterRoom(scanner);
					break;
				case "4":
					memberRepository.detail();
					break;
				case "5":
					memberRepository.updateTest();
					break;
				case "Q", "q":
					System.out.println(chatName + "님 로그아웃 하셨습니다.");
					logon = null;
					mainMenu();
					break;
				}
				
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}
	}

	private void chatJoin() {
		try {
			// 채팅 연결
			//this.serverDataRead();
			System.out.println("[" + roomName + "]" + " 채팅방 입장");
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("command", "incoming");
//			jsonObject.put("uid", member.getUid());
//			jsonObject.put("roomName", roomName);

//			send(jsonObject.toString());

			//receive(); 

			System.out.println("--------------------------------------------------");
			System.out.println("보낼 메시지를 입력하고 Enter");
			System.out.println("채팅를 종료하려면 q를 입력하고 Enter");
			System.out.println("--------------------------------------------------");
			runChat();
			
			
			scanner.close();
			disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}

	}

	private void runChat() throws IOException {
		JSONObject jsonObject = new JSONObject();

		while (true) {
			String message = scanner.nextLine();
			if (message.toLowerCase().equals("q")) {
				break;
			} else {
				jsonObject = new JSONObject();
				jsonObject.put("command", "message");
				jsonObject.put("data", message);
				send(jsonObject.toString());
			}
		}
		scanner.close();
		loginSucessMenu();
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
			send(jsonObject.toString());

			updateMemberResponse();

			disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateMemberResponse() throws Exception {
		String json = serverDataRead();
		JSONObject root = new JSONObject(json);
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");

		if (statusCode.equals("0")) {
			System.out.println("정상적으로 수정되었습니다");
		} else {
			System.out.println(message);
		}
	}

	/**
	 * ✔Room_Management 1. 생성 2. 목록 3. 입장
	 * 
	 * @param createChatRoom
	 * 
	 */
	// 1. 채팅방 생성
	private void createChatRoom(Scanner scanner) {
		try {
			
			System.out.println("\n1. 채팅방 생성");
			System.out.print("채팅방 : ");
			roomName = scanner.nextLine();

			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "createChatRoom");
			jsonObject.put("uid", member.getUid());
			jsonObject.put("roomName", roomName);
			send(jsonObject.toString());

			createChatRoomResponse();

			ChatRoomRepositoryDB chatRoomRepository = new ChatRoomRepositoryDB();
			chatRoomRepository.createChatRoom(roomName);
			
			
			disconnect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createChatRoomResponse() throws IOException {
		String json = serverDataRead();
		JSONObject root = new JSONObject(json);
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");
		
		
		if (statusCode.equals("0")) {
			System.out.println("정상적으로 채팅방이 생성 되었습니다");
		} else {
			System.out.println(message);
		}
	}
	
	
	//INT형 데이터 변환 후 UTF8 로 변환 => 받는 데이터를 압축 시키기 위함
	private String serverDataRead() throws IOException {
        int length = dis.readInt();
        int pos = 0; 
        byte [] data = new byte[length];
        do {
            int len = dis.read(data, pos, length - pos);
            pos += len;
        } while(length != pos);
        
        return new String(data, "UTF8");
	}

	// 2. 목록
	// 챗 룸 리스트 요청
	public void chatRoomListRequest(Scanner scanner) {
		try {

			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "chatRoomListRequest");

			send(jsonObject.toString());

			chatRoomListResponse();

			disconnect();

			enterRoomRequest(scanner);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void chatRoomListResponse() throws IOException {
		String json = serverDataRead();
		JSONObject root = new JSONObject(json);
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");

		if (statusCode.equals("0")) {
			chatRooms.clear();
			root.getJSONArray("chatRooms").forEach(s -> chatRooms.add((String) s));
		} else {
			System.out.println(message);
		}

		disconnect();

		System.out.println(chatRooms);
	}

	private void displayChattingRoomList() {
		int idx = 1;
		System.out.println("----------------");
		System.out.println("* 채팅방 목록 *");
		for (String chatRoom : chatRooms) {
			System.out.println(idx + ". " + chatRoom);
			idx++;
		}
		if (0 == chatRooms.size()) {
			System.out.println("* 입장 가능한 채팅방이 없습니다. 채팅방 생성을 먼저 생성하세요 *");
			
			System.out.println();
			
		}
	}

	// 3. 입장
	// 입장 할 채팅방 요청
	private void enterRoomRequest(Scanner scanner) {
		try {
			
			displayChattingRoomList();
			if (chatRooms.size() == 0) {
				System.out.println();
				enterRoom(scanner);
			} else {
				return;
			}

			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void enterRoom(Scanner scanner) {
		try {
			String roomName;
			System.out.println("입장을 원하는 채팅방 이름 : ");
			roomName = scanner.nextLine();
			
			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "incoming");
			jsonObject.put("uid", member.getUid());
			jsonObject.put("roomName", roomName);
			
			send(jsonObject.toString());

			enterRoomResponse();

			chatJoin();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void enterRoomResponse() throws IOException {
		// 메시지 수신을 위한 스레드 구동
		receive();

	}

	private boolean isChekeRoomNum(int roomNum) {
		return (0 >= roomNum || roomNum > chatRooms.size());
	}

	private String getRoomName(int roomNum) {
		return chatRooms.get(roomNum - 1);
	}

	public static void mainMenu() {
		try {
			ChatClient chatClient = new ChatClient();
			MemberRepositoryDB memberRepository = new MemberRepositoryDB();
			while (false == stop) {
				System.out.println();
				System.out.println("1. 로그인");
				System.out.println("2. 회원가입");
				System.out.println("3. 비밀번호검색");
				System.out.println("4. 회원탈퇴");
				System.out.println("5. 관리자페이지");
				System.out.println("q. 프로그램 종료");
				System.out.print("메뉴 선택 => ");

				// 스캐너

				String menuNum = scanner.nextLine();

				switch (menuNum) {
				case "1":
					chatClient.login(scanner);
					if (logon != null) {
						stop = true;
					}
					break;
				case "2":
					chatClient.registerMember(scanner);
					break;
				case "3":
					memberRepository.findPwd();
					break;
				case "4":
					memberRepository.deleteTest(scanner);
					break;
				case "5":
					memberRepository.adminLogin();
					break;
				case "Q", "q":
					scanner.close();
					stop = true;
					System.out.println("프로그램 종료됨");
					break;
				}
			}
			// 로그온이 true 일때 만
			if (logon != null)
				chatClient.loginSucessMenu();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}
	}

	// 메소드: 메인
	public static void main(String[] args) {
		mainMenu();
	}

}