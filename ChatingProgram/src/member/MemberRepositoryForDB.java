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
import java.util.Scanner;
import java.util.stream.Collectors;

import client.ChatClient;
import member.Member;

public interface MemberRepositoryForDB {
	void login(Scanner scanner) throws Member.NotExistUidPwd;
	void insertTest(Scanner scanner, Member member) throws Member.ExistMember;
	Member findByUid(String uid) throws Member.NotExistUidPwd;
	void updateMember(Scanner scanner) throws Member.NotExistUidPwd;
	void detail(Scanner scanner, ChatClient chatClient) throws Member.NotExistUidPwd;
}
