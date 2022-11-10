package member;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import client.ChatClient;
import member.Member;
import member.Member.ExistMember;
import member.Member.NotExistUidPwd;
import member.MemberRepositoryDB;

public class MemberRepositoryDB implements MemberRepositoryForDB {
	Connection conn = null;
	PreparedStatement pstmt = null;
	ChatClient chatClient = new ChatClient();
	Scanner scanner = new Scanner(System.in);
	static Member loginMember = null;
	

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

	/**
	 * 로그인(login) MemberRepositoryDB 객체로 id 값을 member에 대입 후 해당 id 값이 입력한 값과 맞는지 확인 후
	 * 성공 or 실패 회원탈퇴 계정은 로그인 되지 않는다.
	 * 
	 * @param scanner String exist = member.getExist(); if(exist.equals("0")){...}
	 *                // 회원 = 1, 회원탈퇴 = 0
	 */
	public synchronized void login(Scanner scanner) {
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
			loginMember=member;
			String exist = member.getExist();
			System.out.println();
			if (!pwd.equals(member.getPwd()) || exist.equals("0")) {
				System.out.println("로그인 실패");
			} else {
				System.out.println("로그인 성공");
				ChatClient.chatName = member.getName();
				chatClient.connect();
				chatClient.logon = true;
			}

		} catch (Exception e) {
			System.out.println("------------- 실패 사유 : " + e.getMessage());
		} finally {
			close();
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
			System.out.println("------------- 실패 사유 : " + e.getMessage());
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
			System.out.println("------------- 실패 사유 : " + e.getMessage());
		} finally {
			close();
		}
		return null;
	}

	public Member findByName(String name) throws Member.NotExistUidPwd {
		try {
			open();

			pstmt = conn.prepareStatement(Env.getProperty("findByName_Member"));

			// 멤버 존재여부 확인
			pstmt.setString(1, name);
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
				throw new Member.NotExistUidPwd("[" + name + "] 님의 정보가 존재하지 않습니다");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("------------- 실패 사유 : " + e.getMessage());
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
			
			pstmt = conn.prepareStatement(Env.getProperty("UPDATE_MEMBER"));

			// 멤버 정보 설정

			pstmt.setString(1, member.getPwd());
			pstmt.setString(2, member.getName());
			pstmt.setString(3, member.getSex());
			pstmt.setString(4, member.getAddress());
			pstmt.setString(5, member.getPhone());
			pstmt.setString(6, member.getUid());

			pstmt.executeUpdate();
			loginMember=member;
			ChatClient.chatName = member.getName();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}

	}

	public void insertTest(Scanner scanner) {
		String uid;
		String pwd;
		String name;
		String sex;
		String address;
		String phone;
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		Member member = new Member();
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

		try {
			member.setUid(uid);
			member.setPwd(pwd);
			member.setName(name);
			member.setSex(sex);
			member.setAddress(address);
			member.setPhone(phone);
			memberRepository.insertMember(member);
		} catch (ExistMember e) {
			e.printStackTrace();

		}

	}

	private void findByUidforAdmin() {
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		System.out.print("회원 아이디를 입력하세요 :");
		Scanner sc = new Scanner(System.in);
		String id = sc.nextLine();
		try {
			Member member = memberRepository.findByUid(id);
			if (member != null) {
				System.out.println("아이디:" + member.getUid());
				System.out.println("패스워드:" + member.getPwd());
				System.out.println("이름:" + member.getName());
				System.out.println("전화번호:" + member.getPhone());
				System.out.println("성별:" + member.getSex());
				System.out.println("주소:" + member.getAddress());
				String register = member.getExist();
				if (register.equals("1")) {
					System.out.println("탈퇴여부 : X");
				} else {
					System.out.println("탈퇴여부 : O");
				}

			}
		} catch (NotExistUidPwd e) {
			System.out.println("아이디가 존재하지 않습니다");
		}
	}

	public void updateTest() {
		String uid;
		String pwd;
		String name;
		String sex;
		String address;
		String phone;
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		Member member=loginMember;
		Scanner sc=new Scanner(System.in);
		
		uid = member.getUid();
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
			member = memberRepository.findByUid(uid);
			member.setPwd(pwd);
			member.setName(name);
			member.setSex(sex);
			member.setAddress(address);
			member.setPhone(phone);
			memberRepository.updateMember(member);
		} catch (NotExistUidPwd e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	
		
	}

	public void deleteTest(Scanner scanner) {
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();

		Scanner sc = new Scanner(System.in);
		System.out.print("아이디 : ");
		String uid = sc.nextLine();
		System.out.print("비번 : ");
		String pwd = sc.nextLine();

		try {
			Member member = memberRepository.findByUid(uid);
			if (pwd.equals(member.getPwd())) {
				memberRepository.deleteMember(member);
				System.out.println("탈퇴 완료");
			} else {
				System.out.println("아이디나 비밀번호가 존재하지 않습니다");
			}
		} catch (NotExistUidPwd e) {
			e.printStackTrace();
		} finally {
			close();
		}

	}

	public synchronized void deleteMember(Member member) throws Member.NotExistUidPwd {

		try {
			open();

			pstmt = conn.prepareStatement(Env.getProperty("EXIST_MEMBER"));

			// 멤버 존재여부 확인
			pstmt.setString(1, member.getUid());
			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			if (count == 0) {
				throw new Member.ExistMember("[" + member.getUid() + "] 아이디가 없습니다");
			}
			pstmt.close();
			pstmt = conn.prepareStatement(Env.getProperty("DELETE_MEMBER"));

			// 멤버 정보 설정
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
		Scanner sc = new Scanner(System.in);
		System.out.print("아이디 : ");
		String uid = sc.nextLine();
		System.out.print("이름 : ");
		String name = sc.nextLine();
		try {
			Member member = memberRepository.findByUid(uid);
			if (name.equals(member.getName())) {
				String pwd = member.getPwd();
				System.out.println("비밀번호:" + pwd);
			} else {
				System.out.println("입력한 정보가 등록된 정보와 다릅니다");
			}
		} catch (NotExistUidPwd e) {
			e.printStackTrace();
		}
	}

	public void adminLogin() {
		Scanner sc = new Scanner(System.in);
		System.out.print("아이디 : ");
		String id = sc.nextLine();
		System.out.print("비밀번호 : ");
		String pwd = sc.nextLine();
		if (id.equals(Env.getProperty("ADMIN_ID")) && pwd.equals(Env.getProperty("ADMIN_PWD"))) {
			adminPage();
		} else {
			System.out.print("아이디 혹은 비밀번호가 다릅니다");
		}
	}

	public void adminPage() {
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		boolean stop1 = false;
		try {
			while (false == stop1) {
				System.out.println("--------------------------------------------------");
				System.out.println(" 관리자페이지 입니다.");
				System.out.println("--------------------------------------------------");
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
			// 멤버 존재여부 확인
			ResultSet rs = pstmt.executeQuery();
			int cnt = 0;
			while (rs.next()) {
				cnt += 1;
				// System.out.println("-----------------------------------------------------------------------------");
				System.out.print("# " + cnt + "\t");
				System.out.print("아이디: " + rs.getString(1) + " ");
				System.out.print("비밀번호: " + rs.getString(2) + " ");
				System.out.print("이름: " + rs.getString(3) + " ");
				System.out.print("전화번호: " + rs.getString(4) + " ");
				System.out.print("성별: " + rs.getString(5) + " ");
				System.out.print("주소: " + rs.getString(6) + " ");
				String Exist=rs.getString(8);
				if(Exist.equals("0")) {
					System.out.print("탈퇴회원");
				}
				else {
					System.out.print("가입회원");
				}
				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	@Override
	public void detail(Scanner scanner, ChatClient chatClient) throws NotExistUidPwd {
		try {
			open();
			MemberRepositoryDB memberRepository = new MemberRepositoryDB();
			Member member = loginMember;
			pstmt = conn.prepareStatement(Env.getProperty("DETAIL_MEMBER"));
			System.out.println("-------------------------------------");
			System.out.println("	" + member.getName() + " 님의 회원정보");
			System.out.println("-------------------------------------");
			System.out.println("1. 아이디 : " + member.getUid());
			System.out.println("2. 비밀번호 : " + Security(member, member.getPwd().length()));
			System.out.println("3. 이름 : " + member.getName());
			System.out.println("4. 성별 : " + member.getSex());
			System.out.println("5. 전화번호 : " + member.getPhone());
			System.out.println("6. 주소 : " + member.getAddress());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("------------- 실패 사유 : " + e.getMessage());
		} finally {
			close();
		}

	}

	public String Security(Member member, int pwd) {
		String pwdLenth = "";
		pwd = member.getPwd().length();

		for (int i = 0; i < pwd; i++) {
			pwdLenth += "*";
		}
		return pwdLenth;
	}

	public static void main(String[] args) {

		// updateTest();
		// findByUidTest();
	}

	@Override
	public void insertTest(Scanner scanner, Member member) throws ExistMember {

	}

}
