package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import chatroom.ChatRoomRepositoryDB;
import lombok.Builder;
import lombok.Data;
import member.Member;
import member.MemberRepositoryDB;

@Data
public class SocketClient {
	// 필드
	ChatServer chatServer;
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String clientIp;
	String chatName;
	String uid;
	private String roomName;
	

	// 생성자
	public SocketClient(ChatServer chatServer, Socket socket) {
		try {
			this.chatServer = chatServer;
			this.socket = socket;
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
			InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
			this.clientIp = isa.getHostName();
			receive();
		} catch (IOException e) {
		}
	}

	// 클라이언트 키값
	public String getKey() {
		return uid + "@" + clientIp;
	}

	// 클라이언트 데이터 읽기
	private String clientDataRead() throws IOException {
		int length = dis.readInt();
		int pos = 0;
		byte[] data = new byte[length];
		do {
			int len = dis.read(data, pos, length - pos);
			pos += len;
		} while (length != pos);

		return new String(data, "UTF8");
	}

	// 메소드: JSON 받기
	public void receive() {
		chatServer.threadPool.execute(() -> {
			try {
				boolean stop = false;
				while (true != stop) {
					String receiveJson = clientDataRead();

					JSONObject jsonObject = new JSONObject(receiveJson);
					String command = jsonObject.getString("command");

					/*
					 * 입장 : incoming,이름 {command:incoming, data:'홍길동'}
					 * 
					 * 채팅 : message,내용 {command:message, data:'안녕'}
					 * 
					 */
					switch (command) {
					case "login":
						login(jsonObject);
						stop = true;
						break;

					case "registerMember":
						registerMember(jsonObject);
						stop = true;
						break;

					case "passwdSearch":
						passwdSearch(jsonObject);
						stop = true;
						break;

					case "updateMember":
						updateMember(jsonObject);
						stop = true;
						break;

					case "createChatRoom":
						
						this.roomName = jsonObject.getString("roomName");
						createChatRoom(jsonObject);
						stop = true;
						break;

					case "chatRoomListRequest":
						chatRoomListRequest(jsonObject);
						stop = true;
						break;

					case "incoming":
						System.out.println("receiveJson : " + receiveJson);
						this.uid = jsonObject.getString("uid");
						this.roomName = jsonObject.getString("roomName");
						try {
							chatServer.sendToAll(this, "들어오셨습니다.");
							chatServer.addSocketClient(this);
						} catch (NotExistChatRootException e) {
							e.printStackTrace();
							
						}
						break;

					case "fileUpload":
						this.roomName = jsonObject.getString("roomName");
						fileUpload(jsonObject);
						stop = true;
						break;

					case "message":
						try {
							chatServer.sendToAll(this, jsonObject.getString("data"));
						} catch (NotExistChatRootException e) {
							e.printStackTrace();
						}
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					chatServer.sendToAll(this, "나가셨습니다.");
					chatServer.removeSocketClient(this);
					close();
				} catch (NotExistChatRootException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	private void registerMember(JSONObject jsonObject) {
		Member member = new Member(jsonObject);
		JSONObject jsonResult = new JSONObject();

		jsonResult.put("statusCode", "-1");
		jsonResult.put("message", "[소켓]회원가입 아이디가 존재 합니다.");

		try {
			chatServer.memberRepository.insertMember(member);
			jsonResult.put("statusCode", "0");
			jsonResult.put("message", "[소켓]회원가입 성공");
		} catch (Member.ExistMember e) {
			e.printStackTrace();
		}

		send(jsonResult.toString());

		close();
	}

	private void login(JSONObject jsonObject) {
		String uid = jsonObject.getString("uid");
		String pwd = jsonObject.getString("pwd");
		JSONObject jsonResult = new JSONObject();

		jsonResult.put("statusCode", "-1");
		jsonResult.put("message", "로그인 아이디가 존재하지 않습니다");

		try {
			Member member = chatServer.findByUid(uid);
			
			if (null != member && pwd.equals(member.getPwd())) {
				jsonResult.put("statusCode", "0");
				jsonResult.put("message", "로그인 성공");
				
			}
			
		} catch (Member.NotExistUidPwd e) {
			e.printStackTrace();
		}
		
		send(jsonResult.toString());

		close();
	}

	private void passwdSearch(JSONObject jsonObject) {
		String uid = jsonObject.getString("uid");
		JSONObject jsonResult = new JSONObject();

		jsonResult.put("statusCode", "-1");
		jsonResult.put("message", "로그인 아이디가 존재하지 않습니다");

		try {
			Member member = chatServer.findByUid(uid);
			if (null != member) {
				jsonResult.put("statusCode", "0");
				jsonResult.put("message", "비밀번호 찾기 성공");
				jsonResult.put("pwd", member.getPwd());
			}
		} catch (Member.NotExistUidPwd e) {
			e.printStackTrace();
		}

		send(jsonResult.toString());

		close();
	}

	private void updateMember(JSONObject jsonObject) {
		Member member = new Member(jsonObject);

		JSONObject jsonResult = new JSONObject();

		jsonResult.put("statusCode", "-1");
		jsonResult.put("message", "로그인 아이디가 존재하지 않습니다");

		try {
			chatServer.memberRepository.updateMember(member);
			jsonResult.put("statusCode", "0");
			jsonResult.put("message", "회원정보수정이 정상으로 처리되었습니다");
		} catch (Member.NotExistUidPwd e) {
			e.printStackTrace();
		}

		send(jsonResult.toString());

		close();

	}

	private void fileDownload(JSONObject jsonObject) {
		try {
			String fileName = jsonObject.getString("fileName");
			JSONObject jsonResult = new JSONObject();

			File file = new File("C:\\down\\" + fileName);
			if (!file.exists()) {
				System.out.println("파일이 존재 하지 않습니다");
				jsonResult.put("statusCode", "-1");
				jsonResult.put("message", "알수 없는 오류 ");
			} else {
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
				byte[] data = new byte[(int) file.length()];
				in.read(data);
				in.close();

				jsonResult.put("statusCode", "0");
				jsonResult.put("fileName", file.getName());
				jsonResult.put("message", "성공");
				if (fileName.contains("jpg") || fileName.contains("png") || fileName.contains("jpeg")) {
					Runtime.getRuntime().exec("mspaint c:\\down\\" + fileName);
				} else if (fileName.contains("txt")) {
					Runtime.getRuntime().exec("notepad c:\\down\\" + fileName);
				}

				jsonResult.put("content", new String(Base64.getEncoder().encode(data)));

				send(jsonResult.toString());

				close();
			}
		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
		} catch (IOException ie) {
			System.out.println(ie.getMessage());
		}
	}

	private void fileListRequest(JSONObject jsonObject) {
		JSONObject jsonResult = new JSONObject();

		JSONArray jsonArray = new JSONArray();

		File downPath = new File("c:\\down");
		for (File file : downPath.listFiles()) {
			if (!file.isFile())
				continue;
			JSONObject jsonItem = new JSONObject();
			jsonItem.put("fileName", file.getName());
			jsonItem.put("size", file.length());
			jsonArray.put(jsonItem);
		}

		jsonResult.put("statusCode", "0");
		jsonResult.put("fileList", jsonArray);
		jsonResult.put("message", "성공");

		send(jsonResult.toString());

		close();
	}

	private void fileUpload(JSONObject jsonObject) {
		String fileName = jsonObject.getString("fileName");
		byte[] data = Base64.getDecoder().decode(jsonObject.getString("content").getBytes());
		JSONObject jsonResult = new JSONObject();

		String path = "c:\\down";
		File Folder = new File(path);

		if (!Folder.exists()) {
			Folder.mkdir();
		}

		try {

			BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream("c:\\down\\" + fileName));
			fos.write(data);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("파일수신 완료");
		}
		jsonResult.put("message", "파일수신 완료");
		jsonResult.put("statusCode", "0");
		send(jsonResult.toString());
		close();

	}

	/**
	 * Chat Room 1. 생성 2. 목록 3. 입장
	 * 
	 * @param jsonObject
	 */
	private void createChatRoom(JSONObject jsonObject) {
		JSONObject jsonResult = new JSONObject();
		try {

			chatServer.addChatRoom(this);
			jsonResult.put("statusCode", "0");
			jsonResult.put("message", "[" + roomName + "] 채팅방이 생성되었습니다");
		} catch (ExistChatRootException e) {
			e.printStackTrace();
			jsonResult.put("statusCode", "-1");
			jsonResult.put("message", "[" + roomName + "] 채팅방은 이미 존재합니다");
		}

		send(jsonResult.toString());

		System.out.println(jsonResult);
	}

	private void enterRoomRequest(JSONObject jsonObject) {
		List<String> chatRoomList = chatServer.getChatRoomList();

		JSONObject jsonResult = new JSONObject();

		jsonResult.put("chatRooms", chatRoomList);
		if(chatRoomList != null && chatRoomList.size() != 0) {
			send(jsonResult.toString());
		} else {
			System.out.println("채팅방이 존재 하지 않습니다.");
		}
		

	}

	private void chatRoomListRequest(JSONObject jsonObject) {
		List<String> chatRoomList = chatServer.getChatRoomList();

		JSONObject jsonResult = new JSONObject();

		jsonResult.put("statusCode", "0");
		jsonResult.put("message", "채팅방 목록 조회");
		jsonResult.put("chatRooms", chatRoomList);
		System.out.println(jsonResult);
		
		
		send(jsonResult.toString());

		close();
	}

	// 메소드: JSON 보내기
	public void send(String json) {
		try {
			
			byte [] data = json.getBytes("UTF8"); 
			dos.writeInt(data.length);
			dos.write(data);
			dos.flush();
		} catch (IOException e) {
		}
	}

	// 메소드: 연결 종료
	public void close() {
		try {
			socket.close();
		} catch (Exception e) {
		}
	}
}