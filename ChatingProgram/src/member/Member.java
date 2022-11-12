package member;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import org.json.JSONObject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Member implements Serializable {

	private static final long serialVersionUID = -7931541991854735188L;
	private String uid;
	private String pwd;
	private String name;
	private String sex;
	private String address;
	private String phone;
	private LocalDateTime loginDateTime;
	private String exist;
	
	public Member(String uid, String pwd, String name, String sex, String address, String phone, LocalDateTime loginDateTime, String exist) {
		super();
		this.uid = uid;
		this.setPwd(pwd);
		this.setName(name);
		this.setPhone(phone);
		this.setSex(sex);
		this.setAddress(address);
		this.setExist(exist);
	}
	
	public Member(JSONObject jsonObject) {
		uid = jsonObject.getString("uid");
		pwd = jsonObject.getString("pwd");
		name = jsonObject.getString("name");
		sex = jsonObject.getString("sex");
		address = jsonObject.getString("address");
		phone = jsonObject.getString("phone");
		exist = jsonObject.getString("exist");
        loginDateTime = null;
		setPwd(jsonObject.getString("pwd"));
		setName(jsonObject.getString("name"));
		setSex(jsonObject.getString("sex"));
		setPhone(jsonObject.getString("phone"));
		setAddress(jsonObject.getString("address"));
	}
	
	public Member() {
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
    public JSONObject getJsonObject() {
        JSONObject jsonMember = new JSONObject();
        jsonMember.put("uid", uid);
        jsonMember.put("pwd", pwd);
        jsonMember.put("name", name);
        jsonMember.put("sex", sex);
        jsonMember.put("address", address);
        jsonMember.put("phone", phone);
        jsonMember.put("exist", exist);
        return jsonMember;
    }

	public String getUid() {
		// TODO Auto-generated method stub
		return uid;
	}
    public boolean login(String uid2, String pwd2) {
        return uid2.equals(uid) && pwd2.equals(pwd);
    }

	
	
	
	
	
}
