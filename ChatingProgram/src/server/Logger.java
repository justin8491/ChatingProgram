package server;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;

import member.Env;
import member.Member;
import lombok.Data;

enum CommandType {
	NORMAL_CMD,
	EXIT_CMD
}

@Data
class Message {
	private CommandType commandType;
	private String message;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private Date createDate;
	
	private Message(CommandType commandType, String message) {
		this.commandType = commandType;
		this.message = message;
		this.createDate = new Date(Calendar.getInstance().getTime().getTime());
	}
	
	static Message of(String message) {
		return new Message(CommandType.NORMAL_CMD, message);
	}
	
	static Message exitMessage() {
		return new Message(CommandType.EXIT_CMD, "");
	}
	
	public String getMessageStr() {
		return "[" + sdf.format(createDate) + "] : " + message;
	}
	
	public Date getCreateDate() {
		return createDate;
	}
	
	public String getMessage() {
		return message;
	}
}

public class Logger implements Runnable {
  private LinkedBlockingQueue<Message> queue;
  private PrintStream out;
  private boolean dbWrite = false;
  private Connection conn = null;
  private PreparedStatement pstmt = null;
  
  public Logger() {
      try {
    	  this.queue = new LinkedBlockingQueue<Message>();
    	  this.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(Env.getLoggerFileName())));
          this.dbWrite = Boolean.parseBoolean(Env.getProperty("logger.DBWrite", "false"));
          
          open();
          pstmt = conn.prepareStatement(Env.getProperty("INSERT_LOG"));
      } catch (Exception e) {
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
        	  out.println(msg.getMessageStr());
        	  if (this.dbWrite) {
	        	  writeLogDB(msg);
        	  }
        	  try {
        		  Thread.sleep(10000);
        	  }catch (Exception ex) {
        		  
        	  }
        	  
          }
          
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
    	  out.close();
    	  close();
      }
  }
  
  public void write(String msg) {
	  queue.offer(Message.of(msg));
  }
  
  public void endLogger() {
	queue.offer(Message.exitMessage());
  }
  

	private void open() {
		try {
			Class.forName(Env.getProperty("driverClass"));
			System.out.println("JDBC 드라이버 로딩 성공");
			
			conn = DriverManager.getConnection(Env.getProperty("dbServerConn")
					, Env.getProperty("dbUser")
					, Env.getProperty("dbPasswd"));
			conn.setAutoCommit(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void close() {
		try {
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private void writeLogDB(Message message) {
		try {
			pstmt.setDate(1, message.getCreateDate());
			pstmt.setString(2, message.getMessage());
			pstmt.executeUpdate();
			conn.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		}	  
	}
}
