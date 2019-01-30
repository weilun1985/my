package com.li.myweb;

public enum BrowserCacheControl {
	PUBLIC("public"){
    	public String toString(){
    		return "public";
    	}
    },
	PRIVATE("private"),
	NOCACHE("no-cache"),
	NOSTORE("no-store"),
	MUSTREVALIDATION("must-revalidation"),
	PROXYREVALIDATION("proxy-revalidation"),
	MAXAGE("max-age");
	private String _express;
	private Object _value;
	BrowserCacheControl(String s){
		_express=s;
	}
	@Override
	public String toString(){
		if(this._express=="max-age"&&this._value==null){
			this._value=0;
		}
		return this._express+(this._value==null?"":"="+this._value);
	}
	public void setValue(Object state){
		this._value=state;
	}
}
