package server1;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import common.Member;
import lombok.Data;

enum CommandType {
	NORMAL_CMD,
	EXIT_CMD
}

@Data
class Message {
	private CommandType commandType;
	private String message;
	private Date makeDate; 
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	
	
	private Message(CommandType commandType, String message) {
		this.commandType = commandType;
		this.message = message;
		this.makeDate = Calendar.getInstance().getTime();
	}
	
	static Message of(String message) {
		return new Message(CommandType.NORMAL_CMD, message);
	}
	
	static Message exitMessage() {
		return new Message(CommandType.EXIT_CMD, "");
	}
	
	public String getMessage() {
		return "[" + sdf + "] : " + message;
	}
}

//공급자 
class Producer implements Runnable {
  private LinkedBlockingQueue<Message> queue = null;
  
  public Producer(LinkedBlockingQueue<Message> queue) {
      this.queue = queue; 
  }

  @Override
  public void run() {
      System.out.println("[Start Producer...]");
      try {
          int i = 0;
          while (!Thread.currentThread().isInterrupted()) {
              System.out.println("Producer : " + i);
              queue.offer(Message.of(String.valueOf(i++)));
          }
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
        System.out.println("[End Producer...]");  
      }
  }
}

//Consumer ( 소비자 )
public class Logger implements Runnable {
  private LinkedBlockingQueue<Message> queue;
  PrintStream out;
  
  public Logger() {
      this.queue = new LinkedBlockingQueue<Message>();
      try {
    	  this.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(Env.getLoggerFileName())));
      } catch (FileNotFoundException e) {
    	  e.printStackTrace();
      }
      new Thread(this).start();
  }
  
  @Override
  public void run() {
      try {
          while(!Thread.currentThread().isInterrupted()) {
        	  Message msg = queue.take();
        	  if (msg.getCommandType() == CommandType.EXIT_CMD) {
        		  break;
        	  }
              //System.out.println(name + " : " + msg);
        	  out.println(msg.getMessage());
          }
          
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
    	  out.close();
      }
  }
  
  public void write(String msg) {
	queue.offer(Message.of(msg));
  }
  
  public void endLogger() {
	queue.offer(Message.exitMessage());
  }
  
  Connection conn = null;
  PreparedStatement pstmt = null;
  
  private void writeLogDB(Message message) {
	  try {
		  
		  Member member = new Member();
			pstmt = conn.prepareStatement(Env.getProperty("EXIST_MEMBER"));

			//멤버 존재여부 확인
			pstmt.setString(1, member.getUid());
			ResultSet rs = pstmt.executeQuery();
			int count = 0; 
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			if (count != 0) {
				throw new Member.ExistMember("[" + member.getUid() + "] 아이디가 존재합니다" );
			}
			pstmt.close();
			pstmt = conn.prepareStatement(Env.getProperty("INSERT_MEMBER"));

			//멤버 정보 설정 
			pstmt.setString(1, member.getUid());
			pstmt.setString(2, member.getPwd());
			pstmt.setString(3, member.getName());
			pstmt.setString(4, member.getSex());
			pstmt.setString(5, member.getAddress());
			pstmt.setString(6, member.getPhone());
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
  }
}
