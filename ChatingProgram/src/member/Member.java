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
	private static final long serialVersionUID = 1449132512754742285L;
	private String uid;
	private String pwd;
	private String name;
	private String sex;
	private String address;
	private String phone;
	private LocalDateTime loginDateTime;
	
	public Member(String uid, String pwd, String name, String sex, String address, String phone, LocalDateTime loginDateTime) {
		super();
		this.uid = uid;
		this.pwd = pwd;
		this.name = name;
		this.sex = sex;
		this.address = address;
		this.phone = phone;
		this.loginDateTime = loginDateTime;
	}
	
	public Member(JSONObject jsonObject) {
		uid = jsonObject.getString("uid");
		pwd = jsonObject.getString("pwd");
		name = jsonObject.getString("name");
		sex = jsonObject.getString("sex");
		address = jsonObject.getString("address");
		phone = jsonObject.getString("phone");
        loginDateTime = null;
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

    public JSONObject getJsonObject() {
        JSONObject jsonMember = new JSONObject();
        jsonMember.put("uid", uid);
        jsonMember.put("pwd", pwd);
        jsonMember.put("name", name);
        jsonMember.put("sex", sex);
        jsonMember.put("address", address);
        jsonMember.put("phone", phone);
        return jsonMember;
    }

    public boolean login(String uid2, String pwd2) {
        return uid2.equals(uid) && pwd2.equals(pwd);
    }

}
