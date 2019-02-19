package demo.model;

import java.util.Date;

public class UserInfo {
	private long Id;
	private Date Borthday;
	private String Name;
	public void setId(long id) {
		Id = id;
	}
	public long getId() {
		return Id;
	}
	public void setBorthday(Date borthday) {
		Borthday = borthday;
	}
	public Date getBorthday() {
		return Borthday;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getName() {
		return Name;
	}
	public CartInfo Cart;
}
