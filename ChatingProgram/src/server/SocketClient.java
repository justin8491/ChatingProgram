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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import chatroom.ChatRoomRepositoryDB;
import lombok.Builder;
import lombok.Data;
import member.Member;
import member.MemberRepositoryDB;
import server1.NotExistChatRootException;

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
	public SocketClient(ChatServer chatServer, Socket socket) throws NotExistChatRootException {
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
	public void receive() throws NotExistChatRootException {
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
                        this.uid = jsonObject.getString("uid");
                        this.roomName = jsonObject.getString("roomName");
                        try {
                            chatServer.sendToAll(this, "들어오셨습니다.");
                            chatServer.addSocketClient(this);
                        } catch (NotExistChatRootException e) {
                            e.printStackTrace();
                        }
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
				chatServer.sendToAll(this, "나가셨습니다.");
				chatServer.removeSocketClient(this);
			}
		});
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
			    if (member.getLoginDateTime() == null) {
    			    member.setLoginDateTime(LocalDateTime.now());
    				jsonResult.put("statusCode", "0");
                    jsonResult.put("member", member.getJsonObject());
                    jsonResult.put("message", "로그인 성공");
			    } else {
                    jsonResult.put("statusCode", "0");
                    jsonResult.put("member", member.getJsonObject());
                    jsonResult.put("message", "이미 로그인 된 계정입니다");
			    }
			}
		} catch (Member.NotExistUidPwd e) {
			e.printStackTrace();
		}

		send(jsonResult.toString());
		
		close();
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

	/**
	 * Chat Room 1. 생성 2. 목록 3. 입장
	 * 
	 * @param jsonObject
	 */
	private void createChatRoom(JSONObject jsonObject) {
		JSONObject jsonResult = new JSONObject();
		try {
			System.out.println("this 진입 전");
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

		jsonResult.put("statusCode", "0");
		jsonResult.put("message", "채팅방 목록 조회");
		jsonResult.put("chatRooms", chatRoomList);

		send(jsonResult.toString());

	}

	private void chatRoomListRequest(JSONObject jsonObject) {
		List<String> chatRoomList = chatServer.getChatRoomList();

		JSONObject jsonResult = new JSONObject();

		jsonResult.put("statusCode", "0");
		jsonResult.put("message", "채팅방 목록 조회");
		jsonResult.put("chatRooms", chatRoomList);
		System.out.println("채팅방 리스트 조회 성공");
		send(jsonResult.toString());

		close();
	}

	// 메소드: JSON 보내기
	public void send(String json) {
		try {
			dos.writeUTF(json);
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