package server1;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import common.Member;

public class MemberTestData {
	//메소드: 메인
	@SuppressWarnings("null")
	public static void main(String[] args) {	
		try {
			List<Member> memberList = new ArrayList<>();
			String MEMBER_FILE_NAME = "c:\\temp\\member.db";
			for(int i=0;i<10;i++) {
				memberList.add(Member.builder()
						.uid("userid" + i)
						.pwd("pwd" + i)
						.name("홍길동" + i)
						.sex(i % 2 == 0 ? "F" : "M")
						.phone("010-1234-123" + i)
						.address("혜화동" + i)
						.build());
			}
			File file = new File(MEMBER_FILE_NAME);
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			out.writeObject(memberList);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}