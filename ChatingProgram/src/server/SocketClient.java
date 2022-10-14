package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONObject;

public class SocketClient {

	StartServer startServer;
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String clientIp;
	String chatName;

	public SocketClient(StartServer startServer, Socket socket) {

		try {
			this.startServer = startServer;
			this.socket = socket;
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
			
			//InetSocketAddress 로 실행한 클라이언트 주소 받기
			InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
			this.clientIp = isa.getHostName();
			
			receive();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void receive() {
		// TODO Auto-generated method stub
		startServer.threadPool.execute(()->{
			try {
				while (true) {
					String receiveJson = dis.readUTF();
					
					JSONObject jsonObject = new JSONObject(receiveJson);
					String command = jsonObject.getString("command");
					
					switch (command) {
					case "incoming":
						this.chatName = jsonObject.getString("data");
						startServer.sendToAll(this, "님이 들어오셨습니다.");
						startServer.addSocketClient(this);
						
						break;

					case "message":
						String message = jsonObject.getString("data");
						startServer.sendToAll(this, message);
						
						break;
					}
					
				}
			} catch (IOException e) {
				// TODO: handle exception
				startServer.sendToAll(this, "님이 나가셨습니다.");
				//startServer
			}
		});
	}

	// JSON 보내기
	
	public void send(String json) {
		try {
			dos.writeUTF(json);
			dos.flush();
		} catch (IOException e) {}
	}
	
	public void close() {
		try {
			socket.close();
		} catch (Exception e) {}
	}
	
	
	

}
