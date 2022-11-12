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
	private String sex;
	private String address;
	private String exist;
  	// 각각의 아이디 페스워드는 외부에서 접근할 수 없도록
  	// 접근제어자 private 을 주어 해당 클레스에서만 접근할 수 있도록 했습니다.
	
	public Member(String uid, String pwd, String name, String phone, String sex, String address,String exist) 
	{	
		super();
		this.uid = uid;
		this.setPwd(pwd);
		this.setName(name);
		this.setPhone(phone);
		this.setSex(sex);
		this.setAddress(address);
		this.setExist(exist);
	}
	// Member 생성자를 this를 사용하여 초기화 해주었습니다.

	public Member(JSONObject jsonObject) {
		uid = jsonObject.getString("uid");
		setPwd(jsonObject.getString("pwd"));
		setName(jsonObject.getString("name"));
		setSex(jsonObject.getString("sex"));
		setPhone(jsonObject.getString("phone"));
		setAddress(jsonObject.getString("address"));
	}
	
	public Member(){}
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
	
	
	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}


	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}


	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}


	public String getExist() {
		return exist;
	}

	public void setExist(String exist) {
		this.exist = exist;
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

	public void setUid( String uid) {
		this.uid = uid;
		
	}

	public String getUid() {
		// TODO Auto-generated method stub
		return uid;
	}



	
	
	
	
	
}