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

import member.Member.ExistMember;
import member.Member.NotExistUidPwd;

public class MemberRepository implements MemberRepositoryForDB{
	List<Member> memberList = null;
	Map<String, Member> memberMap = null;
	//private static final String MEMBER_FILE_NAME = "c:\\temp\\member.db";
	MemberRepositoryDB memberRepository = new MemberRepositoryDB();
	

	@SuppressWarnings("unchecked")
	public void loadMember() {
		
		memberRepository.open();
		
//		try {
//			File file = new File(MEMBER_FILE_NAME);
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
//			File file = new File(MEMBER_FILE_NAME);
//			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
//			out.writeObject(memberList);
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public synchronized void registerMember(Member member) throws ExistMember {
		if (true == memberList.stream().anyMatch(m -> member.getUid().equals(m.getUid()))) {
            throw new Member.ExistMember("[" + member.getUid() + "] 아이디가 존재합니다" );
        }
        memberList.add(member);
        memberMap.put(member.getUid(), member);
        //saveMember();
	}

	@Override
	public Member findByUid(String uid) throws NotExistUidPwd {
		Member findMember = memberMap.get(uid);
		if (null == findMember) {
			throw new Member.NotExistUidPwd("[" + uid + "] 아이디가 존재하지 않습니다");			
		}
		return findMember;
	}

	@Override
	public void updateMember(Member member) throws NotExistUidPwd {
		//배열에서 위치를 찾는다
				int index = memberList.indexOf(member);
				if (-1 == index) {
					throw new Member.NotExistUidPwd("[" + member.getUid()+ "] 아이디가 존재하지 않습니다");			
				}
				//배열의 내용을 수정 한다
				memberList.set(index, member);
				memberMap.put(member.getUid(), member);
				
		        saveMember();
		
	}
	
	/*
	public synchronized void insertMember(Member member) throws Member.ExistMember {
        if (true == memberList.stream().anyMatch(m -> member.getUid().equals(m.getUid()))) {
            throw new Member.ExistMember("[" + member.getUid() + "] 아이디가 존재합니다" );
        }
        memberList.add(member);
        memberMap.put(member.getUid(), member);
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
			throw new Member.NotExistUidPwd("[" + member.getUid()+ "] 아이디가 존재하지 않습니다");			
		}
		//배열의 내용을 수정 한다
		memberList.set(index, member);
		memberMap.put(member.getUid(), member);
		
        saveMember();
	}
	*/

}


