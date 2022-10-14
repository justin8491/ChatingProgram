package member;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MemberRepository {
	List<Member> memberList = null;
	Map<String, Member> memberMap = null;
	private static final String MEMBER_FILE_NAME = "c:\\temp\\member.db";

	public void loadMember() {
		try {
			File file = new File(MEMBER_FILE_NAME);
			if (file.exists() && file.length() != 0) {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
				memberList = (List<Member>) in.readObject();
				memberMap = memberList.stream()
						.collect(Collectors.toMap(
						m -> m.uId(),
						m -> m));
				in.close();
			} else {
				memberList = new ArrayList<>();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveMember() {
		try {
			File file = new File(MEMBER_FILE_NAME);
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			out.writeObject(memberList);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void insertMember(Member member) throws Member.ExistMember {
        if (true == memberList.stream().anyMatch(m -> member.uId().equals(m.uId()))) {
            throw new Member.ExistMember("[" + member.uId() + "] 아이디가 존재합니다" );
        }
        memberList.add(member);
        memberMap.put(member.uId(), member);
        saveMember();
	}
	
	public synchronized Member findByUid(String uid) throws Member.NotExistUidPwd {
//		  배열을 이용한 전체 비교(DB 용어 : full scan) 		
//		  Member findMember = Member.builder().uid(uid).build();
//        int index = memberList.indexOf(findMember);
//        if (-1 == index) {
//        	throw new Member.NotExistUidPwd("[" + uid + "] 아이디가 존재하지 않습니다"); 
//        }
//		키를 이용한 검색 (색인 검색)		
		Member findMember = memberMap.get(uid);
		if (null == findMember) {
			throw new Member.NotExistUidPwd("[" + uid + "] 아이디가 존재하지 않습니다");			
		}
		return findMember;
	}

	public synchronized void updateMember(Member member) throws Member.NotExistUidPwd {
		//배열에서 위치를 찾는다
		int index = memberList.indexOf(member);
		if (-1 == index) {
			throw new Member.NotExistUidPwd("[" + member.uId()+ "] 아이디가 존재하지 않습니다");			
		}
		//배열의 내용을 수정 한다
		memberList.set(index, member);
		memberMap.put(member.uId(), member);
		
        saveMember();
	}
	

}


