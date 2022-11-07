package server1;

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

import common.Member;

public interface MemberRepository {
	void insertMember(Member member) throws Member.ExistMember;
	Member findByUid(String uid) throws Member.NotExistUidPwd;
	void updateMember(Member member) throws Member.NotExistUidPwd;
}
