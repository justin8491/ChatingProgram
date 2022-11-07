package member;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import client.ChatClient;
import member.Member;
import member.Member.ExistMember;
import member.Member.NotExistUidPwd;

public class MemberRepositoryDB {
//	List<Member> memberList = null;
//	Map<String, Member> memberMap = null;
	Connection conn = null;
	PreparedStatement pstmt = null;
	ChatClient chatClient = new ChatClient();
	Scanner scanner = new Scanner(System.in);

	public void open() {
		try {
			Class.forName(Env.getProperty("driverClass"));
			System.out.println("JDBC 드라이버 로딩 성공");

			conn = DriverManager.getConnection(Env.getProperty("dbServerConn"), Env.getProperty("dbUser"),
					Env.getProperty("dbPasswd"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized void login() {
		try {
			MemberRepositoryDB memberrepository = new MemberRepositoryDB();
			Member member = new Member();
			open();
			System.out.println("\n1. 로그인 작업");
			System.out.print("아이디 : ");
			String uid = scanner.nextLine();
			System.out.print("비밀번호 : ");
			String pwd = scanner.next();
			member = memberrepository.findByUid(uid);
			if(!pwd.equals(member.getPwd())) {
				System.out.println("로그인 실패");
			}else {
				System.out.println("로그인 성공");
				chatClient.chatName = member.getName();
				chatClient.connect();
				chatClient.logon = true;
			}
			
			

		} catch (Exception e) {
			// TODO: handle exception
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

	public synchronized void insertMember(Member member) throws Member.ExistMember {

		try {
			open();

			pstmt = conn.prepareStatement(Env.getProperty("SELECT_MEMBER"));

			// 멤버 존재여부 확인
			pstmt.setString(1, member.getUid());
			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			if (count != 0) {
				throw new Member.ExistMember("[" + member.getUid() + "] 아이디가 존재합니다");
			}
			pstmt.close();
			pstmt = conn.prepareStatement(Env.getProperty("INSERT_MEMBER"));

			// 멤버 정보 설정
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

	public Member findByUid(String uid) throws Member.NotExistUidPwd {
		try {
			open();

			pstmt = conn.prepareStatement(Env.getProperty("findByUidMember"));

			// 멤버 존재여부 확인
			pstmt.setString(1, uid);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Member member = new Member();
				member.setUid(rs.getString("USERID"));
				member.setPwd(rs.getString("PWD"));
				member.setName(rs.getString("NAME"));
				member.setSex(rs.getString("SEX"));
				member.setAddress(rs.getString("ADDRESS"));
				member.setPhone(rs.getString("PHONE"));
				member.setExist(rs.getString("EXIST")); 
				rs.close();
				return member;
			} else {
				throw new Member.NotExistUidPwd("[" + uid + "] 아이디가 존재하지 않습니다");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return null;
	}

//
	public synchronized void updateMember(Member member) throws Member.NotExistUidPwd {
		try {
			open();

			pstmt = conn.prepareStatement(Env.getProperty("SELECT_MEMBER"));

			// 멤버 존재여부 확인
			pstmt.setString(1, member.getUid());
			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			if (count != 0) {
				throw new Member.ExistMember("[" + member.getUid() + "] 아이디가 존재합니다");
			}
			pstmt.close();
			pstmt = conn.prepareStatement(Env.getProperty("UPDATE_MEMBER"));

			// 멤버 정보 설정

			pstmt.setString(1, member.getPwd());
			pstmt.setString(2, member.getName());
			pstmt.setString(3, member.getSex());
			pstmt.setString(4, member.getAddress());
			pstmt.setString(5, member.getPhone());
			pstmt.setString(6, member.getUid());
			
			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}

	}


	public void insertTest() {
		String uid;
		String pwd;
		String name;
		String sex;
		String address;
		String phone;
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		Member member = new Member();
		Scanner sc = new Scanner(System.in);
		System.out.print("아이디 : ");
		uid = sc.nextLine();
		System.out.print("비번 : ");
		pwd = sc.nextLine();
		System.out.print("이름 : ");
		name = sc.nextLine();
		System.out.print("성별[남자(M)/여자(F)] : ");
		sex = sc.nextLine();
		System.out.print("주소 : ");
		address = sc.nextLine();
		System.out.print("전화번호 : ");
		phone = sc.nextLine();

		try {
			member.setUid(uid);
			member.setPwd(pwd);
			member.setName(name);
			member.setSex(sex);
			member.setAddress(address);
			member.setPhone(phone);
			memberRepository.insertMember(member);
		} catch (ExistMember e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	private static void updateTest() {
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		Member member = new Member();
		member.setUid("abcd123");
		member.setPwd("ppasswd");
		member.setName("이순신");
		member.setSex("F");
		member.setAddress("송파구");
		member.setPhone("010-1111-2222");
		try {
			memberRepository.updateMember(member);
		} catch (NotExistUidPwd e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteTest() { 
		MemberRepositoryDB memberRepository = new MemberRepositoryDB(); 
		 
		Scanner sc=new Scanner(System.in); 
		System.out.print("아이디 : "); 
		String uid = sc.nextLine(); 
		System.out.print("비번 : "); 
		String pwd = sc.nextLine(); 
	 
		try { 
			Member member = memberRepository.findByUid(uid); 
			if(pwd.equals(member.getPwd())){ 
			memberRepository.deleteMember(member); 
			System.out.println("탈퇴 완료"); 
			} 
			else { 
				System.out.println("아이디나 비밀번호가 존재하지 않습니다"); 
			} 
		} 
		catch (NotExistUidPwd e) { 
			// TODO Auto-generated catch block 
			e.printStackTrace(); 
		}			 
	} 
	public synchronized void deleteMember(Member member) throws Member.NotExistUidPwd{ 
		 
		try { 
			open(); 
			 
			pstmt = conn.prepareStatement(Env.getProperty("SELECT_MEMBER")); 
 
			//멤버 존재여부 확인 
			pstmt.setString(1, member.getUid()); 
			ResultSet rs = pstmt.executeQuery(); 
			int count = 0;  
			if (rs.next()) { 
				count = rs.getInt(1); 
			} 
			rs.close(); 
			if (count == 0) { 
				throw new Member.ExistMember("[" + member.getUid() + "] 아이디가 없습니다" ); 
			} 
			pstmt.close(); 
			pstmt = conn.prepareStatement(Env.getProperty("DELETE_MEMBER")); 
 
			//멤버 정보 설정  
			pstmt.setString(1, member.getUid()); 
			pstmt.setString(2, member.getPwd());
			System.out.println("-------------" + pstmt.toString());
			pstmt.executeUpdate(); 
			
			
		} catch (Exception e) { 
			e.printStackTrace();

		} finally { 
			close(); 
		} 
	} 
	
	public static void main(String[] args) {

		// updateTest();
		// findByUidTest();
	}

}
