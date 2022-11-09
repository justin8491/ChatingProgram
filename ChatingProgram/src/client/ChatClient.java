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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

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
	String roomName;
	
	//객체 필드
	static ChatClient chatClient = new ChatClient();
	Scanner scanner = new Scanner(System.in);
	Member member = new Member();
	
	// 로그인 여부
	public static Member logon = null;
	
	//채팅방 정보 JSON 구조 ({no : 채팅방번호, roomName : "채팅방이름"})
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
					String json = dis.readUTF();
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
		byte [] data = json.getBytes("UTF8");
        
        dos.writeInt(data.length);//문자열의 길이(4byte)
        dos.write(data);//내용 
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
			
			send(jsonObject.toString());

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
			MemberRepositoryDB memberRepository = new MemberRepositoryDB();
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
				System.out.println("6. 파일 업로드");
				System.out.println("7. 파일 목록 조회");
				System.out.println("8. 파일 다운로드");
				System.out.println("q. 프로그램 종료");
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
					enterRoomResponse();
					break;
				case "4":
					memberRepository.detail(scanner, chatClient);
					break;
				case "5":
					chatClient.updateMember(scanner);
					break;
				case "6":
					chatClient.fileUpload(scanner);
					break;
				case "7":
					chatClient.fileListRequest(scanner);
					break;
				case "8":
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

			chatClient.send(jsonObject.toString());
			
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
				chatClient.send(jsonObject.toString());
			}
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
			send(jsonObject.toString());

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
			send(jsonObject.toString());

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
			send(jsonObject.toString());

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
			send(jsonObject.toString());

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
	
	/**
	 * ✔Room_Management
	 * 1. 생성
	 * 2. 목록
	 * 3. 입장
	 * @param createChatRoom
	 */
	private void createChatRoom(Scanner scanner) {
        try {
            
            System.out.println("\n1. 채팅방 생성");
            System.out.print("채팅방 : ");
            roomName = scanner.nextLine();

            connect();
            
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "createChatRoom");
            jsonObject.put("uid", logon.getUid());
            jsonObject.put("roomName", roomName);

            send(jsonObject.toString());

            createChatRoomResponse();
            
            disconnect();
            
            enterRoom(scanner);
            
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
	
	//챗 룸 리스트 요청
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
    
    private void chatRoomListResponse() throws IOException  {
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
    }
	
	//입장 할 채팅방 요청
    private void enterRoomRequest(Scanner scanner) {
        try {
        	
            System.out.print("입장할 채팅방 번호 : ");
            int roomNum = Util.parseInt(scanner.nextLine(), 0);
            
            if (isChekeRoomNum(roomNum)) {
                System.out.print("채팅방 번호를 잘못 입력하셨습니다");
                
                return ;
            }

            roomName = getRoomName(roomNum);
            //roomName = chatRooms.get(roomNum-1);
            
            enterRoom(scanner);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
	
	private void enterRoom(Scanner scanner) {
        try {
            
            connect();
            
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "incoming");
            jsonObject.put("uid", logon.getUid());
            jsonObject.put("roomName", roomName);

            send(jsonObject.toString());
            
            enterRoomResponse();
            
            chatJoin();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
	
	private void enterRoomResponse() throws IOException {
        //메시지 수신을 위한 스레드 구동 
        receive();
            
    }
	
	private boolean isChekeRoomNum(int roomNum) {
    	return (0 >= roomNum || roomNum > chatRooms.size());    	
    }
    
    private String getRoomName(int roomNum) {
    	return chatRooms.get(roomNum-1);
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
				System.out.println("5. 관리자페이지");
				System.out.println("q. 프로그램 종료");
				System.out.print("메뉴 선택 => ");

				// 스캐너
				Scanner scanner = new Scanner(System.in);
				String menuNum = scanner.nextLine();
				
				switch (menuNum) {
				case "1":
					memberRepository.login(scanner);
					if (logon != null) {
						stop = true;
					}
					break;
				case "2":
					memberRepository.insertTest(scanner);
					break;
				case "3":
					chatClient.passwdSearch(scanner);
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


}