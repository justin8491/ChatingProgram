package client;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.json.JSONObject;

import server.ChatServer;

public class ChatClient {
	// 필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String chatName;

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
	public void send(String json) throws IOException {
		dos.writeUTF(json);
		dos.flush();
	}

	// 메소드: 서버 연결 종료
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
			Socket sock = new Socket("localhost", 50001);
			System.out.println("파일주소를 입력하세요:");
			String fileName = scanner.next();
			File f = new File(fileName);
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
			dos.writeUTF(f.getName());
			dos.flush();
			FileInputStream fis = new FileInputStream(fileName);
			int n = 0;
			byte b[] = new byte[1024];
			while ((n = fis.read(b)) != -1) {
				dos.write(b, 0, n);
			}

			dos.close();
			fis.close();
			System.out.println("파일전송 완료");
			sock.close();
		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}
	}

	public void fileDownload(Scanner scanner) throws IOException {
		Socket sock = new Socket("localhost", 9999);
		ChatServer chatServer = new ChatServer();
		chatServer.files();
		DataInputStream dis = new DataInputStream(sock.getInputStream());
		System.out.println("파일명을 입력하세요:");
		String fileName = scanner.next();
		File f = new File(fileName);
		DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
		dos.writeUTF(f.getName());
		dos.flush();
		String path = "c:\\chatdown\\";
		File Folder = new File(path);
		if (!Folder.exists()) {
			try {
				Folder.mkdir();
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
		FileOutputStream fos = new FileOutputStream(path + fileName);
		byte[] b = new byte[1024];
		int n = 0;
		while ((n = dis.read(b)) != -1) {
			fos.write(b, 0, n);
		}
		fos.close();
		dis.close();
		sock.close();
		System.out.println("파일수신 완료");

	}

	private void createChatRoom() {
		// TODO Auto-generated method stub

	}

	private void readChatRoom() {
		// TODO Auto-generated method stub

	}

	// 메소드: 메인
	public static void main(String[] args) {
		try {
			ChatClient chatClient = new ChatClient();
			boolean stop = false;
			boolean logon = false;

			while (false == stop) {
				System.out.println();
				System.out.println("1. 로그인");
				System.out.println("2. 회원가입");
				System.out.println("3. 비밀번호검색");
				System.out.println("4. 파일업로드");
				System.out.println("5. 서버파일목록");
				System.out.println("q. 프로그램 종료");
				System.out.print("메뉴 선택 => ");
				Scanner scanner = new Scanner(System.in);
				String menuNum = scanner.nextLine();
				switch (menuNum) {
				case "1":
					chatClient.login(scanner);
					logon = true;
					break;
				case "2":
					chatClient.registerMember(scanner);
					break;
				case "3":
					chatClient.passwdSearch(scanner);
					break;
				case "4":
					chatClient.fileUpload(scanner);
					break;
				case "5":
					chatClient.fileDownload(scanner);
					break;
				case "Q", "q":
					scanner.close();
					stop = true;
					System.out.println("프로그램 종료됨");
					break;
				}
				if (logon == true) {
					System.out.println();
					System.out.println("1. 채팅방 생성");
					System.out.println("2. 채팅방 목록");
					System.out.println("3. 파일업로드");
					System.out.println("4. 서버파일목록");
					System.out.println("q. 프로그램 종료");
					System.out.print("메뉴 선택 => ");
					scanner = new Scanner(System.in);
					menuNum = scanner.nextLine();
					switch (menuNum) {
					case "1":
						chatClient.createChatRoom();
						break;
					case "2":
						chatClient.readChatRoom();
						break;
					case "3":
						chatClient.fileUpload(scanner);
						break;
					case "4":
						chatClient.fileDownload(scanner);
						break;
					case "Q", "q":
						scanner.close();
						stop = true;
						System.out.println("프로그램 종료됨");
						break;
					}
				}

			}

			while (true == logon && false == stop) {

			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[클라이언트] 서버 연결 안됨");
		}
	}

}