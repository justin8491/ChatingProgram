package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import member.Member;
import member.Member.ExistMember;
import member.Member.NotExistUidPwd;
import member.MemberRepositoryDB;
import member.MemberRepositoryForDB;

public class ChatClient {
	// 필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	public static String chatName;
	
	//객체 필드
	static ChatClient chatClient = new ChatClient();
	Scanner scanner = new Scanner(System.in);

	// 로그인 여부
	public static boolean logon = false;

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
					String json = dis.readUTF();
					JSONObject root = new JSONObject(json);
					String clientIp = root.getString("clientIp");
					String chatName = root.getString("chatName");
					String message = root.getString("message");
					System.out.println("<" + chatName + "@" + clientIp + "> " + message);
				}
			} catch (Exception e1) {
				System.out.println("[클라이언트] 서버 연결 끊김");
				System.exit(0);
			}
		});
		thread.start();
	}

	// 메소드: JSON 보내기
	public void send(JSONObject json) throws IOException {
		dos.writeUTF(json.toString());
		dos.flush();
	}

	// 메소드: 서버 연결 종료
	public void disconnect() throws IOException {
		socket.close();
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
			send(jsonObject);

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
	
	
	public void loginSucessMenu() {
		stop = false;
		try {

			while (false == stop && logon == true) {
				System.out.println("--------------------------------------------------");
				System.out.println("	" + chatName + " 님 환영합니다.");
				System.out.println("--------------------------------------------------");
				System.out.println();
				System.out.println("1. 채팅방 입장");
				System.out.println("2. 나의 회원 정보");
				System.out.println("3. 회원정보수정");
				System.out.println("4. 파일 업로드");
				System.out.println("5. 파일 목록 조회");
				System.out.println("6. 파일 다운로드");
				System.out.println("q. 프로그램 종료");
				System.out.print("메뉴 선택 => ");
				Scanner scanner = new Scanner(System.in);
				String menuNum = scanner.nextLine();
				switch (menuNum) {
				case "1":
					chatClient.chatJoin();
					break;
				case "2":
					
					break;
				case "3":
					chatClient.updateMember(scanner);
					break;
				case "4":
					chatClient.fileUpload(scanner);
					break;
				case "5":
					chatClient.fileListRequest(scanner);
					break;
				case "6":
					chatClient.fileDownload(scanner);
					break;
				case "Q", "q":
					scanner.close();
					stop = true;
					System.out.println("프로그램 종료됨");
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
			chatClient.connect();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "incoming");
			jsonObject.put("data", chatName);

			chatClient.send(jsonObject);
			
			chatClient.receive();
			
			System.out.println("--------------------------------------------------");
			System.out.println("보낼 메시지를 입력하고 Enter");
			System.out.println("채팅를 종료하려면 q를 입력하고 Enter");
			System.out.println("--------------------------------------------------");
			runChat();
			
			
			scanner.close();
			 chatClient.disconnect();
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
				chatClient.send(jsonObject);
			}
		}
	}
	
	private void updateMember(Scanner scanner) {
		String uid;
		String pwd;
		String address;
		String phone;
		String sex;
		String name;

		try {
			System.out.println("\n4. 회원정보수정");
			System.out.print("아이디 : ");
			uid = scanner.nextLine();
			System.out.print("비번 : ");
			pwd = scanner.nextLine();
			System.out.print("주소 : ");
			address = scanner.nextLine();
			System.out.print("성별 : ");
			sex = scanner.nextLine();
			System.out.print("이름 : ");
			name = scanner.nextLine();
			System.out.print("전화번호 : ");
			phone = scanner.nextLine();

			connect();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "updateMember");
			jsonObject.put("uid", uid);
			jsonObject.put("pwd", pwd);
			jsonObject.put("address", address);
			jsonObject.put("phone", phone);
			send(jsonObject);

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

	private void fileListRequest(Scanner scanner) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "fileListRequest");

			// 사전에 서버에서 파일 목록을 받을 참조 변수를 null로 초기화 한다
			fileList = null;

			connect();
			send(jsonObject);

			System.out.println("서버에 파일 목록 요청");

			fileListResponse();

		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}

	}

	JSONArray fileList = null;

	private void fileListResponse() throws IOException {
		String json = dis.readUTF();
		JSONObject root = new JSONObject(json);
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");
		fileList = root.getJSONArray("fileList");

		if (statusCode.equals("0")) {
			// 파일 목록 출력
			fileList.forEach(item -> {
				JSONObject jsonItem = (JSONObject) item;
				System.out.println(jsonItem.getString("fileName") + " : " + jsonItem.getLong("size"));
			});
		} else {
			System.out.println(message);
		}

	}

	public void fileUpload(Scanner scanner) {
		try {
			System.out.println("파일주소를 입력하세요:");
			String fileName = scanner.next();
			JSONObject jsonObject = new JSONObject();
			File file = new File(fileName);
			if (!file.exists()) {
				System.out.println("파일이 존재 하지 않습니다");
				return;
			}
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] data = new byte[(int) file.length()];
			in.read(data);
			in.close();
			jsonObject.put("command", "fileUpload");
			jsonObject.put("fileName", file.getName());
			jsonObject.put("content", new String(Base64.getEncoder().encode(data)));

			connect();
			send(jsonObject);

			System.out.println("파일전송 완료");
		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}
	}

	public void fileDownload(Scanner scanner) throws IOException {
		try {
			// 서버에 있는 파일목록 존재 여부 확인
			if (fileList == null) {
				System.out.println("서버에 파일목록을 요청하세요");
				return;
			}
			System.out.print("다운로드할 파일명을 입력하세요: ");
			String fileName = scanner.next();

			// 서버에 있는 파일명 존재 여부 확인
			boolean isExist = fileList.toList().stream()
					.anyMatch(item -> ((HashMap<?, ?>) item).get("fileName").equals(fileName));
			if (false == isExist) {
				System.out.println("서버에 파일이 존재하지 않습니다");
				return;
			}

			JSONObject jsonObject = new JSONObject();

			jsonObject.put("command", "fileDownload");
			jsonObject.put("fileName", fileName);

			connect();
			send(jsonObject);

			fileDownloadResponse(fileName);

		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
		} catch (Exception ie) {
			System.out.println(ie.getMessage());
		}

	}

	private void fileDownloadResponse(String fileName) throws Exception {
		String json = dis.readUTF();
		JSONObject root = new JSONObject(json);
		String statusCode = root.getString("statusCode");
		String message = root.getString("message");

		if (statusCode.equals("0")) {
			byte[] data = Base64.getDecoder().decode(root.getString("content").getBytes());

			File downloadPath = new File("c:\\down\\client");
			if (!downloadPath.exists()) {
				try {
					downloadPath.mkdir();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
			try {
				BufferedOutputStream fos = new BufferedOutputStream(
						new FileOutputStream(new File(downloadPath, fileName)));
				fos.write(data);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println(message);
		}
	}

	// 메소드: 메인
	public static void main(String[] args) {
		try {
			ChatClient chatClient = new ChatClient();
			MemberRepositoryDB memberRepository=new MemberRepositoryDB();
			while (false == stop) {
				System.out.println();
				System.out.println("1. 로그인");
				System.out.println("2. 회원가입");
				System.out.println("3. 비밀번호검색");
				System.out.println("4. 회원탈퇴");
				System.out.println("q. 프로그램 종료");
				System.out.print("메뉴 선택 => ");

				// 스캐너
				Scanner scanner = new Scanner(System.in);
				String menuNum = scanner.nextLine();
				
				switch (menuNum) {
				case "1":
					memberRepository.login(scanner);
					if (logon == true) {
						stop = true;
					}
					break;
				case "2":
					memberRepository.insertTest(scanner);
					break;
				case "3":
					chatClient.passwdSearch(scanner);
					break;
				case "4":
					memberRepository.deleteTest(scanner);
					break;
				case "Q", "q":
					scanner.close();
					stop = true;
					System.out.println("프로그램 종료됨");
					break;
				}
			}
			// 로그온이 true 일때 만
			if (logon)
				chatClient.loginSucessMenu();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}
	}


}