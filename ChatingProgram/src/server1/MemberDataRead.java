package server1;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import common.Member;

public class MemberDataRead {
	//메소드: 메인
	@SuppressWarnings("null")
	public static void main(String[] args) {	
		try {
			String MEMBER_FILE_NAME = "c:\\temp\\member.db";
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(MEMBER_FILE_NAME)));
			List<Member> memberList = (List<Member>) in.readObject();
			memberList.stream().forEach(m -> System.out.println(m));
			
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}