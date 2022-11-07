package server1;


import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import common.Member;

public class ChatServer {
	//필드
	ServerSocket serverSocket;
	ExecutorService threadPool;
	Map<String, Map<String, SocketClient>> chatRooms;
	MemberRepositoryDB memberRepository;
	Logger logger;

	public ChatServer() {
	    threadPool = Executors.newFixedThreadPool(Env.getThreadPoolSize());
	    chatRooms = Collections.synchronizedMap(new HashMap<>());
	    memberRepository = new MemberRepositoryDB();
	    logger = new Logger();
        try {
            serverSocket = new ServerSocket(50001);
        } catch (IOException e) {
            e.printStackTrace();
        } 
	}
	
	//메소드: 서버 시작
	public void start() throws IOException {
		
		memberRepository.loadMember();
		
		System.out.println( "[서버] 시작됨");
		
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
	
	public void addChatRoom(SocketClient socketClient) throws ExistChatRootException {
        if (chatRooms.containsKey(socketClient.getRoomName())) {
            throw new ExistChatRootException();
        }
        chatRooms.put(socketClient.getRoomName(), new HashMap<>());
        //폴더 생성 
        String path = Env.getWorkPath() + File.separatorChar + String.valueOf(socketClient.getRoomName().hashCode());
        File chatRoomFileFolder = new File(path);
        System.out.println(chatRoomFileFolder.getAbsolutePath());
        if (!chatRoomFileFolder.exists()) {
            chatRoomFileFolder.mkdirs();
        }
        
    }
	
//	//메소드: 클라이언트 연결시 SocketClient 생성 및 추가
    public void addSocketClient(SocketClient socketClient) throws NotExistChatRootException  {
        if (!chatRooms.containsKey(socketClient.getRoomName())) {
            throw new NotExistChatRootException();
        }
        Map<String, SocketClient> chatRoom = chatRooms.get(socketClient.getRoomName());
        
        String key = socketClient.getKey();
        chatRoom.put(key, socketClient);
        System.out.println("입장: " + key);
        System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
    }

    //메소드: 클라이언트 연결 종료시 SocketClient 제거
    public void removeSocketClient(SocketClient socketClient) throws NotExistChatRootException {
        if (!chatRooms.containsKey(socketClient.getRoomName())) {
            throw new NotExistChatRootException();
        }
        Map<String, SocketClient> chatRoom = chatRooms.get(socketClient.getRoomName());
        
        String key = socketClient.uid + "@" + socketClient.clientIp;
        chatRoom.remove(key);
        System.out.println("나감: " + key);
        System.out.println("현재 채팅자 수: " + chatRoom.size() + "\n");
        
        if (chatRoom.size() == 0) {
            System.out.println("[" + socketClient.getRoomName() + "] 채팅방 삭제");
            chatRooms.remove(socketClient.getRoomName());
            
            //폴더 생성 
            String path = Env.getWorkPath() + File.separatorChar + String.valueOf(socketClient.getRoomName().hashCode());
            File chatRoomFileFolder = new File(path);
            if (chatRoomFileFolder.exists()) {
                Stream.of(chatRoomFileFolder.listFiles()).forEach(file -> file.delete());
                chatRoomFileFolder.delete();
            }
            
        }
        
    }       
    //메소드: 모든 클라이언트에게 메시지 보냄
    public void sendToAll(SocketClient sender, String message) throws NotExistChatRootException  {
        if (!chatRooms.containsKey(sender.getRoomName())) {
            throw new NotExistChatRootException();
        }
        Map<String, SocketClient> chatRoom = chatRooms.get(sender.getRoomName());

        JSONObject root = new JSONObject();
        root.put("clientIp", sender.clientIp);
        root.put("chatName", sender.uid);
        root.put("message", message);
        
        //로그 기록 
        logger.write(root.toString());
        
        //채팅방에 참여자 목록 요청 
        if (message.startsWith("@userlist")) {
            JSONArray userList = new JSONArray();
            chatRoom.keySet().forEach(userid -> userList.put(userid));
            root.put("userList", userList);
            
            sender.send(root.toString());    

            //첨부파일 업로드(@up:파일명) 
        } else if (message.startsWith("@up")) {
        
            //첨부파일 업로드(@filelist:파일명) 
        } else if (message.startsWith("@filelist")) {
            String path = Env.getWorkPath() + File.separatorChar + String.valueOf(sender.getRoomName().hashCode());
            File chatRoomFileFolder = new File(path);
            if (chatRoomFileFolder.exists()) {
                JSONArray fileList = new JSONArray();
                Stream.of(chatRoomFileFolder.listFiles()).forEach(f -> {
                    fileList.put(new Object[] {f.getName(), f.length()});
                });
                root.put("fileList", fileList);
            }
            sender.send(root.toString());    

            //첨부파일 업로드(@download:파일명) 
        } else if (message.startsWith("@download")) {
            
        } else if (message.indexOf("@") == 0) { //귀속말 존재 여부 확인 
            int pos = message.indexOf(" ");
            String key = message.substring(1, pos);
            message = "(귀속말)" + message.substring(pos+1);
            
            SocketClient targetClient = chatRoom.get(key);
            if (null != targetClient) {
                targetClient.send(root.toString());    
            }
            
        } else {
            //체팅방에 있은 모든 사람(본인제외) 
            chatRoom.values().stream()
            .filter(socketClient -> socketClient != sender)
            .forEach(socketClient -> socketClient.send(root.toString()));
        }
    }   	
	
    public List<String> getChatRoomList() {
        return chatRooms.keySet().stream().toList();
    }
    
	//메소드: 서버 종료
	public void stop() {
		try {
			logger.endLogger();
			serverSocket.close();
			threadPool.shutdownNow();
			chatRooms.values().stream().forEach(charRoom -> charRoom.values().stream().forEach(sc -> sc.close()));
			System.out.println( "[서버] 종료됨 ");
		} catch (IOException e1) {}
	}
	
	public synchronized void registerMember(Member member) throws Member.ExistMember {
		memberRepository.insertMember(member);
	}
	
	public synchronized Member findByUid(String uid) throws Member.NotExistUidPwd {
		return memberRepository.findByUid(uid);
	}
	
	//메소드: 메인
	public static void main(String[] args) {	
		try {
			ChatServer chatServer = new ChatServer();
			chatServer.start();
			
			String path = "C:\\Users";
			System.out.println(path);
			
			//서버의 클래스 폴더 모니터링  
			Thread watchThread = new Thread(new ChatServerWatchService(path));
			watchThread.setDaemon(true);
			watchThread.start();

			System.out.println("----------------------------------------------------");
			System.out.println("서버를 종료하려면 q를 입력하고 Enter.");
			System.out.println("----------------------------------------------------");
			
			Scanner scanner = new Scanner(System.in);
			while(true) {
				String key = scanner.nextLine();
				if(key.equals("q")) {
					break;
				}
			}
			scanner.close();
			chatServer.stop();
		} catch(IOException e) {
			System.out.println("[서버] " + e.getMessage());
		}
	}

    
}