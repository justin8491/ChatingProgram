package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONObject;

import lombok.Builder;
import lombok.Data;
import member.Member;


public class SocketClient {
	//필드
	ChatServer chatServer;
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String clientIp;	
	String chatName;
	
	//생성자
	public SocketClient(ChatServer chatServer, Socket socket) {
		try {
			this.chatServer = chatServer;
			this.socket = socket;
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
			InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
			this.clientIp = isa.getHostName();			
			receive();
		} catch(IOException e) {
		}
	}	
	//메소드: JSON 받기
	public void receive() {
		chatServer.threadPool.execute(() -> {
			try {
				boolean stop = false;
				while(true != stop) {
					String receiveJson = dis.readUTF();		
					System.out.println(receiveJson);
					JSONObject jsonObject = new JSONObject(receiveJson);
					String command = jsonObject.getString("command");
					
					/*
					입장 : incoming,이름
					{command:incoming, data:'홍길동'}
					
					 채팅 : message,내용
					{command:message, data:'안녕'}
					
					*/
					switch(command) {
						case "login":
							login(jsonObject);
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
						
						case "registerMember":
						    registerMember(jsonObject);
						    stop = true;
						    break;
						case "updateMember":
							updateMember(jsonObject);
						    stop = true;
						    break;
						    
						case "incoming":
							this.chatName = jsonObject.getString("data");
							chatServer.sendToAll(this, "들어오셨습니다.");
							chatServer.addSocketClient(this);
							break;
						case "message":
							String message = jsonObject.getString("data");
							chatServer.sendToAll(this, message);
							break;
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
				chatServer.sendToAll(this, "나가셨습니다.");
				chatServer.removeSocketClient(this);
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
    
    
	//메소드: JSON 보내기
	public void send(String json) {
		try {
			dos.writeUTF(json);
			dos.flush();
		} catch(IOException e) {
		}
	}	
	//메소드: 연결 종료
	public void close() {
		try { 
			socket.close();
		} catch(Exception e) {}
	}
}