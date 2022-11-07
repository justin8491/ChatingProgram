package client1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import common.Member;
import common.Util;

public class ChatClient {
    
	//필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
    String roomName;
	
	//메뉴 관리 변수 
	MenuMode menuMode = MenuMode.NOT_LOGIN_MENU;
	
	//전역 변수 관리용 객체 
    //로그인 멤버 (key : loingMember, value : Member 객체)
	static Member loginMember = null;
    //채팅방 정보 JSON 구조 ({no : 채팅방번호, roomName : "채팅방이름"})
	static List<String> chatRooms = new ArrayList<String>();
	
	//메소드: 서버 연결
	public  void connect() throws IOException {
		socket = new Socket("localhost", Env.getPort());
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		System.out.println("[클라이언트] 서버에 연결됨");
	}
	
	//메소드: JSON 받기
	public void receive() {
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					String json = serverDataRead();
					JSONObject root = new JSONObject(json);
					if (root.has("userList")) {
					    JSONArray userList = root.getJSONArray("userList");
					    userList.forEach(item -> {
					        String userid = (String)item;
					        System.out.println(userid + (userid.contains(loginMember.getUid()) ? "(*)" : ""));
					    });
					} else if (root.has("fileList")) {
                        JSONArray fileList = root.getJSONArray("fileList");
                        fileList.forEach(item -> {
                            System.out.println(item);
                        });

					} else {
					    
					}
					String clientIp = root.getString("clientIp");
					String chatName = root.getString("chatName");
					String message = root.getString("message");
					System.out.println("<" + chatName + "@" + clientIp + "> " + message);
				}
			} catch(Exception e1) {
			    e1.printStackTrace();
				System.out.println("[클라이언트] 서버 연결 끊김");
			}
		});
		thread.start();
	}

	//메소드: JSON 보내기
	public void send(String json) throws IOException {
        byte [] data = json.getBytes("UTF8");
        
        dos.writeInt(data.length);//문자열의 길이(4byte)
        dos.write(data);//내용 
        dos.flush();
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
			
			send(jsonObject.toString());
			
			loginResponse();

			disconnect();
			
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
			//로그인 한 멤버 전역 객체에 저장 
			loginMember = new Member(root.getJSONObject("member"));
			System.out.println("loginMember = " + loginMember);
			
			//메뉴 모드를 설정한다
	        menuMode = MenuMode.LOGIN_MENU;

		} else {
			System.out.println(message);
            //멤버 전역 객체에 로그인 멤버 초기화  
            loginMember = null;
		}
	}
	
	public void registerMember(Scanner scanner) {
        try {
            String uid;
            String pwd;
            String name;
            String sex;
            String address;
            String phone;
            
            System.out.println("\n2. 회원가입");
            System.out.print("아이디 : ");
            uid = scanner.nextLine();
            System.out.print("비밀번호 : ");
            pwd = scanner.nextLine();
            System.out.print("이름 : ");
            name = scanner.nextLine();
            System.out.print("성별(M/F) : ");
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

            send(jsonObject.toString());
            
            registerMemberResponse();
            
            disconnect();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	
	public void registerMemberResponse() throws Exception {
        String json = serverDataRead();
        JSONObject root = new JSONObject(json);
        String statusCode = root.getString("statusCode");
        String message = root.getString("message");
        
        if (statusCode.equals("0")) {
            System.out.println("회원가입성공");
        } else {
            System.out.println(message);
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
	
	public void logout(Scanner scanner) {
        //로그인 멤버 전역 객체에 초기화  
        loginMember = null;
        
        //메뉴 모드를 설정한다
        menuMode = MenuMode.NOT_LOGIN_MENU;
	    
	}

	public void memberView(Scanner scanner) {
	    if (loginMember == null) return;
	    
        System.out.println("\n2. 회원상세정보");
        System.out.println("아이디 : " + loginMember.getUid());
        System.out.println("비밀번호 : " + loginMember.getPwd());
        System.out.println("이름 : "  + loginMember.getName());
        System.out.println("성별(M/F) : " + loginMember.getSex());
        System.out.println("주소 : " + loginMember.getAddress());
        System.out.println("전화번호 : " + loginMember.getPhone());
	    
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
    

	private void chatMenu(Scanner scanner) {
        
	    menuMode = MenuMode.CHAT_MANAGER_MENU;
    }

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

    private void createChatRoom(Scanner scanner) {
        try {
            
            System.out.println("\n1. 채팅방 생성");
            System.out.print("채팅방 : ");
            roomName = scanner.nextLine();

            connect();
            
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "createChatRoom");
            jsonObject.put("uid", loginMember.getUid());
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
    
    
    private void enterRoomRequest(Scanner scanner) {
        try {
            displayChattingRoomList();
            if (chatRooms.size() == 0) {
                return;
            }
            
            System.out.print("입장할 채팅방 번호 : ");
            int roomNum = Util.parseInt(scanner.nextLine(), 0);
            
            if (isChekeRoomNum(roomNum)) {
                System.out.print("채팅방 번호를 잘못 입력하셨습니다");
                menuMode = MenuMode.CHAT_MANAGER_MENU;
                return ;
            }

            roomName = getRoomName(roomNum);
            //roomName = chatRooms.get(roomNum-1);
            
            enterRoom(scanner);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private boolean isChekeRoomNum(int roomNum) {
    	return (0 >= roomNum || roomNum > chatRooms.size());    	
    }
    
    private String getRoomName(int roomNum) {
    	return chatRooms.get(roomNum-1);
    }
    
    private void enterRoom(Scanner scanner) {
        try {
            
            connect();
            
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "incoming");
            jsonObject.put("uid", loginMember.getUid());
            jsonObject.put("roomName", roomName);

            send(jsonObject.toString());
            
            enterRoomResponse();
            
            inputChatMessage(scanner);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private void enterRoomResponse() throws IOException {
        //메시지 수신을 위한 스레드 구동 
        receive();
            
    }

    private void inputChatMessage(Scanner scanner) throws IOException {
      System.out.println("--------------------------------------------------");
      System.out.println("보낼 메시지를 입력하고 Enter");
      System.out.println("채팅를 종료하려면 q를 입력하고 Enter");
      System.out.println("채팅방에 참여자 목록(@userlist), 귀속말(@아이디), 첨부파일 업로드(@up:파일명), 첨부파일목록 요청(@filelist), 첨부파일 다운로드(@download:파일명), 첨부파일 업로드 또는 다운로드중 취소 (@cancel)");
      System.out.println("--------------------------------------------------");
      JSONObject jsonObject = new JSONObject();
      while(true) {
          String message = scanner.nextLine();
          if(message.toLowerCase().equals("q")) {
              break;
          } else if(message.startsWith("@up:")) {
              String fileName = message.substring("@up:".length());
              File file = new File(fileName);
              if (!file.exists()) {
                  System.out.println("업로드할 파일이 존재하지 않습니다");
              } else {
                  
                  new Thread(()->{
                      try {
                          //전송할 파일의 내용을 읽는다 
                          BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                          byte [] data = new byte[(int)file.length()];
                          in.read(data);
                          in.close();

                          //전송할 메시지를 구성한다  
                          jsonObject.put("command", "fileUpload");
                          jsonObject.put("roomName", roomName);
                          jsonObject.put("fileName", file.getName());
                          jsonObject.put("content", new String(Base64.getEncoder().encode(data)));
                          
                          String json = jsonObject.toString();
                          //서버에 연결 
                          Socket socket = new Socket("localhost", Env.getPort());
                          DataInputStream dis = new DataInputStream(socket.getInputStream());
                          DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                          byte [] sendData = json.getBytes("UTF8");
                          //서버에 첨부파일 전송 
                          dos.writeInt(sendData.length);//문자열의 길이(4byte)
                          dos.write(sendData);//내용 
                          dos.flush();

                          //서버에서 결과 수신 
                          int length = dis.readInt();
                          int pos = 0; 
                          byte [] recvData = new byte[length];
                          do {
                              int len = dis.read(recvData, pos, length - pos);
                              pos += len;
                          } while(length != pos);
                          
                          String responseJson = new String(recvData, "UTF8");
                          JSONObject root = new JSONObject(responseJson);
                          String statusCode = root.getString("statusCode");
                          
                          //서버의 처리 결과를 출력한다 
                          System.out.println(root.getString("message"));
                          
                          //서버와 연결을 끊는다
                          socket.close();

                      } catch (Exception e) {
                          e.printStackTrace();
                      }
                  }).start();
              }
          } else if(message.startsWith("@download:")) {
              String fileName = message.substring("@download:".length());
                  
              new Thread(()->{
                  try {
                      //전송할 메시지를 구성한다  
                      jsonObject.put("command", "download");
                      jsonObject.put("roomName", roomName);
                      jsonObject.put("fileName", fileName);
                      
                      String json = jsonObject.toString();
                      //서버에 연결 
                      Socket socket = new Socket("localhost", Env.getPort());
                      DataInputStream dis = new DataInputStream(socket.getInputStream());
                      DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                      byte [] sendData = json.getBytes("UTF8");
                      //서버에 첨부파일 전송 
                      dos.writeInt(sendData.length);//문자열의 길이(4byte)
                      dos.write(sendData);//내용 
                      dos.flush();

                      //서버에서 결과 수신 
                      int length = dis.readInt();
                      int pos = 0; 
                      byte [] recvData = new byte[length];
                      do {
                          int len = dis.read(recvData, pos, length - pos);
                          pos += len;
                      } while(length != pos);
                      
                      String responseJson = new String(recvData, "UTF8");
                      JSONObject root = new JSONObject(responseJson);
                      String statusCode = root.getString("statusCode");
                      
                      //서버와 연결을 끊는다
                      socket.close();

                      if ("0".equals(statusCode)) {
                          //서버에 받은 정보를 기준으로 파일 저장을 한다 
                          byte [] data = Base64.getDecoder().decode(root.getString("content").getBytes());
    
                          System.out.println("WorkPath : " + Env.getWorkPath());
                          File workPath = new File(Env.getWorkPath());
                          if (!workPath.exists()) {
                              workPath.mkdirs();
                          }
                                  
                          File file = new File(workPath, fileName);
                          try {
                              System.out.println("저장위치 : " + file.getAbsolutePath());
                              BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
                              fos.write(data);
                              fos.close();
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                          System.out.println("파일 다운로드 완료");
                      } else {
                          System.out.println(root.getString("message"));
                      }
                      
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              }).start();
          } else {
              jsonObject.put("command", "message");
              jsonObject.put("roomName", roomName);
              jsonObject.put("data", message);
              send(jsonObject.toString());
          }
      }
      socket.close();
      
    }

    public void mainMenu() {
        
        boolean stop = false;
        
        while(false == stop) {
            menuDisplay();
            System.out.print("메뉴 선택 => ");
            Scanner scanner = new Scanner(System.in);
            String menuNum = scanner.nextLine();
            switch(menuMode) {
            case NOT_LOGIN_MENU:
                switch(menuNum) {
                case "1":
                    login(scanner);
                    break;
                case "2":
                    registerMember(scanner);
                    break;
                case "3":
                    passwdSearch(scanner);
                    break;
                case "Q", "q":
                    scanner.close();
                    stop = true;
                    System.out.println("프로그램 종료됨");
                    break;
                }
                break;
            case LOGIN_MENU:
                switch(menuNum) {
                case "1":
                    logout(scanner);
                    break;
                case "2":
                    memberView(scanner);
                    break;
                case "3":
                    updateMember(scanner);
                    break;
                case "4":
                    chatMenu(scanner);
                    break;
                case "Q", "q":
                    scanner.close();
                    stop = true;
                    System.out.println("프로그램 종료됨");
                    break;
                }
                break;
            case CHAT_MANAGER_MENU:
                switch(menuNum) {
                case "1":
                    createChatRoom(scanner);
                    break;
                case "2":
                    chatRoomListRequest(scanner);
                    break;
                case "Q", "q":
                    menuMode = MenuMode.LOGIN_MENU;
                    break;
                }
                break;
            default:
                break;
            }
        }
    }
    
    
    private void menuDisplay() {
	        
        System.out.println();
        switch(menuMode) {
        case NOT_LOGIN_MENU:
            System.out.println("1. 로그인");
            System.out.println("2. 회원가입");
            System.out.println("3. 비밀번호검색");
            System.out.println("q. 프로그램 종료");
            break;
        case LOGIN_MENU:
            System.out.println("1. 로그아웃");
            System.out.println("2. 회원상세정보");
            System.out.println("3. 회원정보수정");
            System.out.println("4. 채팅");
            System.out.println("q. 프로그램 종료");
            break;
        case CHAT_MANAGER_MENU:
            System.out.println("1. 채팅방 생성");
            System.out.println("2. 채팅방 목록");
            System.out.println("q. 이전 메뉴");
            break;
//        case CHAT_MENU:
//            displayChattingRoomList();
//            System.out.println("q. 이전 메뉴");
        default:
            break;
        }
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
        }        
    }

	//메소드: 메인
//	public static void main(String[] args) {		
//		try {			
//			new ChatClient().mainMenu();
//			
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
//					jsonObject = new JSONObject();
//					jsonObject.put("command", "message");
//					jsonObject.put("data", message);
//					chatClient.send(jsonObject.toString());
//				}
//			}
//			scanner.close();
//			chatClient.unconnect();
//		} catch(Exception e) {
//			e.printStackTrace();
//			System.out.println("[클라이언트] 서버 연결 안됨");
//		}
//	}	
}