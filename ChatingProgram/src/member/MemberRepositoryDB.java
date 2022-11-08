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
import member.Env;

public class MemberRepositoryDB implements MemberRepositoryForDB{
//	List<Member> memberList = null;
//	Map<String, Member> memberMap = null;
	Connection conn = null;
	PreparedStatement pstmt = null;

	public void open() {
		try {
			Class.forName(Env.getProperty("driverClass"));
			System.out.println("JDBC 드라이버 로딩 성공");
			
			conn = DriverManager.getConnection(Env.getProperty("dbServerConn")
					, Env.getProperty("dbUser")
					, Env.getProperty("dbPasswd"));
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
	
	public synchronized void insertMember(Member member) throws Member.ExistMember {
		
		try {
			open();
			
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
	
	public Member findByUid(String uid) throws Member.NotExistUidPwd {
		try {
			open();

			pstmt = conn.prepareStatement(Env.getProperty("findByUidMember"));

			//멤버 존재여부 확인
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

			pstmt = conn.prepareStatement(Env.getProperty("UPDATE_MEMBER"));

			//멤버 정보 설정 
			pstmt.setString(1, member.getPwd());
			pstmt.setString(2, member.getName());
			pstmt.setString(3, member.getSex());
			pstmt.setString(4, member.getAddress());
			pstmt.setString(5, member.getPhone());
			pstmt.setString(6, member.getUid());
			if (-1 == pstmt.executeUpdate()) {
				throw new Member.NotExistUidPwd("[" + member.getUid()+ "] 아이디가 존재하지 않습니다");			
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}		
	}
//
	public void insertTest() {
		String uid;
		String pwd;
		String name;
		String sex;
		String address;
		String phone;
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		Member member = new Member();
		Scanner sc=new Scanner(System.in);
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
	
	private void findByUidforAdmin() {
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		System.out.print("회원 아이디를 입력하세요 :");
		Scanner sc=new Scanner(System.in);
		String id=sc.nextLine();
		try {
			Member member = memberRepository.findByUid(id);
			if (member != null) {
				System.out.println("아이디:"+member.getUid());
				System.out.println("패스워드:"+member.getPwd());
				System.out.println("이름:"+member.getName());
				System.out.println("전화번호:"+member.getPhone());
				System.out.println("성별:"+member.getSex());
				System.out.println("주소:"+member.getAddress());
				String register=member.getExist();
				if (register.equals("1")){
					System.out.println("탈퇴여부 : X");}
				else {
					System.out.println("탈퇴여부 : O");
				}
				
			}
		} catch (NotExistUidPwd e) {
			System.out.println("아이디가 존재하지 않습니다");
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
			
			pstmt = conn.prepareStatement(Env.getProperty("EXIST_MEMBER"));

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
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	public void findPwd() {
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		
		Scanner sc=new Scanner(System.in);
		System.out.print("아이디 : ");
		String uid = sc.nextLine();
		System.out.print("이름 : ");
		String name = sc.nextLine();
	
		try {
			Member member = memberRepository.findByUid(uid);
			if(name.equals(member.getName())){
				String pwd=member.getPwd();
			System.out.println("비밀번호:"+pwd);
			}
			else {
				System.out.println("입력한 정보가 등록된 정보와 다릅니다");
			}
		}
		catch (NotExistUidPwd e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}	
	public void adminLogin() {
		Scanner sc=new Scanner(System.in);
		System.out.print("아이디 : ");
		String id = sc.nextLine();
		System.out.print("비밀번호 : ");
		String pwd = sc.nextLine();
		if (id.equals(Env.getProperty("ADMIN_ID")) && pwd.equals(Env.getProperty("ADMIN_PWD"))) {
			adminPage();
		}
		else {
			System.out.print("아이디 혹은 비밀번호가 다릅니다");
		}
		
		
		
	}
	public void adminPage() {
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
			boolean stop1 = false;
			try {
				while (false == stop1) {
					System.out.println("--------------------------------------------------");
					System.out.println(	" 관리자페이지 입니다.");
					System.out.println("--------------------------------------------------");
					System.out.println();
					System.out.println("1. 회원목록 조회");
					System.out.println("2. 회원 검색");
					System.out.println("3. 관리자페이지 종료");
					System.out.print("메뉴 선택 => ");
					Scanner scanner = new Scanner(System.in);
					String menu = scanner.nextLine();
					switch (menu) {
					case "1":
						memberRepository.memberList();
						break;
					case "2":
						memberRepository.findByUidforAdmin();
						break;
					case "3":
						stop1 = true;
						System.out.println("초기화면으로 돌아갑니다");
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	
	public void memberList() {
		try {
			
			open();
			pstmt = conn.prepareStatement(Env.getProperty("MEMBER_LIST"));
			//멤버 존재여부 확인
			ResultSet rs = pstmt.executeQuery();
			int cnt=0;
			 while(rs.next()){
			cnt+=1;
			System.out.println("-----------------------------------------------------------------------------");
			System.out.println("# "+cnt);
			System.out.print("아이디: "+rs.getString(1)+" ");
			System.out.print("비밀번호: "+rs.getString(2)+" ");
			System.out.print("이름: "+rs.getString(3)+" ");
			System.out.print("전화번호: "+rs.getString(4)+" ");
			System.out.print("성별: "+rs.getString(5)+" ");
			System.out.print("주소: "+rs.getString(6)+" ");
			System.out.println();
			 }
			 
		}
			catch (Exception e) {
				e.printStackTrace();
			} finally {
				close();
			}
		
	}
		
	public static void main(String []args) {
		
	}

}
