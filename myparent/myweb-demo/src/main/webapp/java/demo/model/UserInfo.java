package demo.model;

import java.util.Date;

public class UserInfo {
	private long Id;
	private Date Birthday;
	private String Name;
	public void setId(long id) {
		Id = id;
	}
	public long getId() {
		return Id;
	}
	public void setBirthday(Date birthday) {
		Birthday = birthday;
	}
	public Date getBirthday() {
		return Birthday;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getName() {
		return Name;
	}
	public CartInfo Cart;
}
