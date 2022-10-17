package member;

import java.io.Serializable;
import java.util.Objects;

import org.json.JSONObject;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class Member implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7931541991854735188L;
	
	private String uid;
	private String pwd;
	private String name;
	private String phone;
//	private String email;
	private String sex;
	private String address;
  	// 각각의 아이디 페스워드는 외부에서 접근할 수 없도록
  	// 접근제어자 private 을 주어 해당 클레스에서만 접근할 수 있도록 했습니다.
	
	public Member(String uid, String pwd, String name, String phone, String sex, String address) 
	{
		this.uid = uid;
		this.pwd = pwd;
		this.name = name;
//		this.email = email;
		this.phone = phone;
		this.sex = sex;
		this.address = address;
	}
	// Member 생성자를 this를 사용하여 초기화 해주었습니다.

	public Member(JSONObject jsonObject) {
		uid = jsonObject.getString("uid");
		pwd = jsonObject.getString("pwd");
		name = jsonObject.getString("name");
		sex = jsonObject.getString("sex");
//		email = jsonObject.getString("email");
		phone = jsonObject.getString("phone");
		address = jsonObject.getString("address");
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Member other = (Member) obj;
		return Objects.equals(uid, other.uid);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(uid);
	}
	
	
	public static class ExistMember extends Exception {
		public ExistMember(String reason) {
			super(reason);
		}
	}
	
	public static class NotExistMember extends Exception {
		public NotExistMember(String reason) {
			super(reason);
		}
	}
	
	public static class NotExistUidPwd extends Exception {
		public NotExistUidPwd(String reason) {
			super(reason);
		}
	}



	
	
	
	
	
}