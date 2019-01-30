package com.li.core;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringParser {
	static final Pattern _regex_date=Pattern.compile(
			"^(\\d{2}|\\d{4})(\\-|/)(\\d{1,2})\\2(\\d{1,2})(\\s(\\d{1,2})\\:(\\d{1,2})(\\:(\\d{1,2})|)|)$"
			,Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean supported(Class c){
		if(c==int.class||c==Integer.class
				||c==short.class||c==Short.class
				||c==long.class||c==Long.class
				||c==float.class||c==Float.class
				||c==double.class||c==Double.class
				||c==byte.class||c==Byte.class
				||c==boolean.class||c==Boolean.class
				||c==char.class||c==Character.class
				||c==UUID.class
				||c==Date.class
				||c==InetAddress.class
				||c.isEnum()
				||c==String.class
		){
			return true;
		}
		try {
			c.getMethod("parse", String.class);
		} catch (NoSuchMethodException e) {
			return false;
		}
		return false;
	}
	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	public static Object parse(Class c,String s) throws StringParseUnSupportException{
		if(c==String.class){
			return s;
		}
		if(s!=null)
			s=s.trim();
		if(c==int.class||c==Integer.class){
			return (s==null||s.length()==0)?0:Integer.parseInt(s);
		}
		if(c==short.class||c==Short.class){
			return (s==null||s.length()==0)?0:Short.parseShort(s);
		}
		if(c==long.class||c==Long.class){
			return (s==null||s.length()==0)?0:Long.parseLong(s);
		}
		if(c==float.class||c==Float.class){
			return (s==null||s.length()==0)?0:Float.parseFloat(s);
		}
		if(c==double.class||c==Double.class){
			return (s==null||s.length()==0)?0:Double.parseDouble(s);
		}
		if(c==boolean.class||c==Boolean.class){
			return (s==null||s.length()==0)?false:Boolean.parseBoolean(s);
		}
		if(c==byte.class||c==Byte.class){
			return (s==null||s.length()==0)?0:Byte.parseByte(s);
		}
		if(c==char.class||c==Character.class){
			return (s==null)?0:s.charAt(0);
		}
		if(c==UUID.class){
			return (s==null||s.length()==0)?null:UUID.fromString(s);
		}
		if(c==Date.class){
			if(s==null||s.length()==0)
				return null;
			else{
				DateFormat format=getDateFormat(s);
				if(format!=null){
					try {
						return format.parse(s);
					} catch (ParseException e) {
						throw new StringParseUnSupportException(c,s,e);
					}
				}
				else
					return new Date(Date.parse(s));
			}
		}
		if(c==InetAddress.class){
			if(s==null||s.length()==0)
				return null;
			try {
				return InetAddress.getByName(s);
			} catch (UnknownHostException ex) {
				new RuntimeException(ex);
			}
		}
		if(c.isEnum()){
			if(s==null||s.length()==0)
				return null;
			else
				return java.lang.Enum.valueOf(c, s);
		}
		try{
			Method m=c.getMethod("parse", String.class);
			return m.invoke(null, new Object[]{s});
		}
		catch(NoSuchMethodException ex){
			throw new StringParseUnSupportException(c,s);
		}
		catch(Exception ex){
			throw new RuntimeException(ex);
		} 
		
	}
	//获取日期格式化形式
	public static DateFormat getDateFormat(String dateStr){
		DateFormat format=null;
		Matcher matcher=_regex_date.matcher(dateStr);
		if(matcher.find()){
			int n_y=matcher.group(1)!=null?matcher.group(1).length():0;    //y的长度
			int n_M=matcher.group(3)!=null?matcher.group(3).length():0;    //M的长度
			int n_d=matcher.group(4)!=null?matcher.group(4).length():0;    //d的长度
			int n_H=matcher.group(6)!=null?matcher.group(6).length():0;    //H的长度
			int n_m=matcher.group(7)!=null?matcher.group(7).length():0;    //m的长度
			int n_s=matcher.group(9)!=null?matcher.group(9).length():0;    //s的长度
			String sep=matcher.group(2);          //日期分隔符
			if(n_y>0){
				StringBuffer fbuffer=new StringBuffer();
				for(int i=0;i<n_y;i++){
					fbuffer.append("y");
				}
				if(n_M>0){
					fbuffer.append(sep);
					for(int i=0;i<n_M;i++){
						fbuffer.append("M");
					}
					if(n_d>0){
						fbuffer.append(sep);
						for(int i=0;i<n_d;i++){
							fbuffer.append("d");
						}
						//含有时间
						if(n_H>0){
							fbuffer.append(" ");
							for(int i=0;i<n_H;i++){
								fbuffer.append("H");
							}
							if(n_m>0){
								fbuffer.append(":");
								for(int i=0;i<n_m;i++){
									fbuffer.append("m");
								}
								if(n_s>0){
									fbuffer.append(":");
									for(int i=0;i<n_s;i++){
										fbuffer.append("s");
									}
								}
							}
						}
					}
				}
				format=new SimpleDateFormat(fbuffer.toString());
			}
		}
		return format;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getParseSrc(Class c,String varexp,String vexpress){
		if(c==String.class){
			return varexp+"="+vexpress+";";
		}
		if(c==int.class||c==Integer.class){
			return varexp+"=Integer.parseInt("+vexpress+");";
		}
		if(c==short.class||c==Short.class){
			return varexp+"=Short.parseShort("+vexpress+");";
		}
		if(c==long.class||c==Long.class){
			return varexp+"=Long.parseLong("+vexpress+");";
		}
		if(c==float.class||c==Float.class){
			return varexp+"=Float.parseFloat("+vexpress+");";
		}
		if(c==double.class||c==Double.class){
			return varexp+"=Double.parseDouble("+vexpress+");";
		}
		if(c==boolean.class||c==Boolean.class){
			return varexp+"=Boolean.parseBoolean("+vexpress+");";
		}
		if(c==byte.class||c==Byte.class){
			return varexp+"=Byte.parseByte("+vexpress+");";
		}
		if(c==char.class||c==Character.class){
			return varexp+"="+vexpress+".charAt(0);";
		}
		if(c==UUID.class){
			return varexp+"=UUID.fromString("+vexpress+");";
		}
		if(c==InetAddress.class){
			return varexp+"=InetAddress.getByName("+vexpress+");";
		}
		if(c.isEnum()){
			return varexp+"=java.lang.Enum.valueOf(c, "+vexpress+");";
		}
		if(c==Date.class){
			return varexp+"=StringParser.getDateFormat("+vexpress+").format("+"vexpress);";
		}
		try{
			c.getMethod("parse", String.class);
			return varexp+"="+c.getName()+".parse("+vexpress+");";
		}catch(Exception ex){
			return null;
		}
	}
}
