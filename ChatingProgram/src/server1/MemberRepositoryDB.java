package server1;

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
import java.util.stream.Collectors;

import common.Member;
import common.Member.ExistMember;
import common.Member.NotExistUidPwd;

public class MemberRepositoryDB implements MemberRepository{
//	List<Member> memberList = null;
//	Map<String, Member> memberMap = null;
	Connection conn = null;
	PreparedStatement pstmt = null;

	public MemberRepositoryDB() {
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
	@SuppressWarnings("unchecked")
    public void loadMember() {
//		try {
//			File file = new File(Env.getMemberFileName());
//			if (file.exists() && file.length() != 0) {
//				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
//				memberList = (List<Member>) in.readObject();
//				memberMap = memberList.stream()
//						.collect(Collectors.toMap(
//						m -> m.getUid(),
//						m -> m));
//				in.close();
//			} else {
//				memberList = new ArrayList<>();
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	private void saveMember() {
//		try {
//			File file = new File(Env.getMemberFileName());
//			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
//			out.writeObject(memberList);
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	public synchronized void insertMember(Member member) throws Member.ExistMember {
		
		try {
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
	private static void insertTest() {
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		Member member = new Member();
		member.setUid("abcd123");
		member.setPwd("pwd");
		member.setName("이순");
		member.setSex("M");
		member.setAddress("혜화동");
		member.setPhone("010-1234-3333");
		try {
			memberRepository.insertMember(member);
		} catch (ExistMember e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void findByUidTest() {
		MemberRepositoryDB memberRepository = new MemberRepositoryDB();
		try {
			Member member = memberRepository.findByUid("abcd123");
			if (member != null) {
				System.out.println(member.toString());
			}
		} catch (NotExistUidPwd e) {
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
		member.setPhone("010-1111-4444");
		try {
			memberRepository.updateMember(member);
		} catch (NotExistUidPwd e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String []args) {
		insertTest();
		updateTest();
		findByUidTest();
	}
	

}
