package com.li.cson;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

import com.li.core.StringParser;

public class Utils {
	public static byte[] getBinary(char a){
		byte[] binary=new byte[2];
		binary[0]=(byte)(a&0xff);
		binary[1]=(byte)((a>>8)&0xff);
		return binary;
	}
	public static byte[] getBinary(int a){
		byte[] binary=new byte[4];
		binary[0]=(byte)(a&0xff);
		binary[1]=(byte)((a&0xff00)>>8);
		binary[2]=(byte)((a&0xff0000)>>16);
		binary[3]=(byte)((a&0xff000000)>>24);
		return binary;
	}
	public static byte[] getBinary(short a){
		byte[] binary=new byte[2];
		binary[0]=(byte)(a&0xff);
		binary[1]=(byte)((a&0xff00)>>8);
		return binary;
	}
	public static byte[] getBinary(long a){
		byte[] binary=new byte[8];
		binary[0]=(byte)(a&0xffL);
		binary[1]=(byte)((a&(0xffL<<8))>>>8);
		binary[2]=(byte)((a&(0xffL<<16))>>>16);
		binary[3]=(byte)((a&(0xffL<<24))>>>24);
		binary[4]=(byte)((a&(0xffL<<32))>>>32);
		binary[5]=(byte)((a&(0xffL<<40))>>>40);
		binary[6]=(byte)((a&(0xffL<<48))>>>48);
		binary[7]=(byte)((a&(0xffL<<56))>>>56);
		return binary;
	}
	public static void writeBinary(OutputStream stream,int a) throws IOException{
		stream.write(getBinary(a));
	}
	public static void writeBinary(OutputStream stream,long a) throws IOException{
		stream.write(getBinary(a));
	}
	public static void writeBinary(OutputStream stream,short a) throws IOException{
		stream.write(getBinary(a));
	}
	public static char toChar(byte[] binary,int offset){
		return (char)((char)(binary[offset]&0xff)
				|((char)(binary[offset++]&0xff)<<8));
	}
	public static char toChar(java.io.InputStream stream) throws IOException{
		return (char)((char)(stream.read()&0xff)
				|((char)(stream.read()&0xff)<<8));
	}
	public static int toInt32(byte[] binary,int offset){
		int a=(int)(binary[offset+0]&0xff)
		                  |((int)(binary[offset+1]&0xff))<<8
		                  |((int)(binary[offset+2]&0xff))<<16
		                  |((int)(binary[offset+3]&0xff))<<24;
		 return a;
	}
	public static int toInt32(java.io.InputStream stream) throws IOException{
		int a=stream.read()
		|((stream.read()&0xff)<<8)
		|((stream.read()&0xff)<<16)
		|((stream.read()&0xff)<<24);
		 return a;
	}
	public static short toInt16(byte[] binary,int offset){
		short a=(short)((binary[offset+0]&0xff)
              |(binary[offset+1]&0xff)<<8);
		 return a;
	}
	public static short toInt16(java.io.InputStream stream) throws IOException{
		short a=(short)(((short)stream.read()&0xff)
				|((short)stream.read()&0xff)<<8);
		 return a;
	}
	public static long toInt64(byte[] binary,int offset){
		long a=((long)binary[offset+0]&0xff)
        |((long)binary[offset+1]&0xff)<<8
        |((long)binary[offset+2]&0xff)<<16
        |((long)binary[offset+3]&0xff)<<24
		 |((long)binary[offset+4]&0xff)<<32
		 |((long)binary[offset+5]&0xff)<<40
		 |((long)binary[offset+6]&0xff)<<48
		 |((long)binary[offset+7]&0xff)<<56;
		 return a;
	}
	public static long toInt64(java.io.InputStream stream) throws IOException{
		long a=((long)stream.read()&0xff)
		|((long)stream.read()&0xff)<<8
		|((long)stream.read()&0xff)<<16
		|((long)stream.read()&0xff)<<24
		 |((long)stream.read()&0xff)<<32
		 |((long)stream.read()&0xff)<<40
		 |((long)stream.read()&0xff)<<48
		 |((long)stream.read()&0xff)<<56;
		 return a;
	}
	public static Object defaultValue(Class<?> c){
		if(c==int.class||c==Integer.class
				||c==short.class||c==Short.class
				||c==long.class||c==Long.class
				||c==float.class||c==Float.class
				||c==double.class||c==Double.class
				||c==byte.class||c==Byte.class){
			return 0;
		}
		if(c==boolean.class||c==Boolean.class){
			return false;
		}
		if(c==char.class||c==Character.class){
			return (char)0;
		}
		return null;
	}
	private static Class<?> getArrayElementClassFromName(String name){
		char ch1=name.charAt(1);
		if(ch1=='B'){
			return byte.class;
			//return Byte.class;
		}
		else if(ch1=='S'){
			return short.class;
			//return Short.class;
		}
		else if(ch1=='I'){
			return int.class;
			//return Integer.class;
		}
		else if(ch1=='J'){
			return long.class;
			//return Long.class;
		}
		else if(ch1=='Z'){
			return boolean.class;
			//return Boolean.class;
		}
		else if(ch1=='C'){
			return char.class;
			//return Character.class;
		}
		else if(ch1=='L'){
			String className=name.substring(2,name.length()-1);
			ClassLoader loader=Thread.currentThread().getContextClassLoader();
			try {
				return Class.forName(className,true,loader);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		else if(ch1=='['){
			ClassLoader loader=Thread.currentThread().getContextClassLoader();
			try{
				return Class.forName(name.substring(1),true,loader);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	public static Class<?> getArrayElementClass(Class<?> arr){
		String name=arr.getName();
		return getArrayElementClassFromName(name);
	}
	/*public static Class<?> getArrayElementClass(Class<?> arr){
		try
		{
			String name=arr.getName();
			if(name.startsWith("[L")){
				String className=name.substring(2,name.length()-1);
				ClassLoader loader=Thread.currentThread().getContextClassLoader();
				return Class.forName(className,true,loader);
			}
			else if(name.startsWith("[")){
				String tag=name.substring(1);
				if(tag.equals("B")){
					return Byte.class;
				}
				else if(tag.equals("S")){
					return Short.class;
				}
				else if(tag.equals("I")){
					return Integer.class;
				}
				else if(tag.equals("J")){
					return Long.class;
				}
				else if(tag.equals("Z")){
					return Boolean.class;
				}
				else if(tag.equals("C")){
					return Character.class;
				}
			}
			return null;
		}
		catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}*/
	private static boolean IsWrapper(Class<?> a,Class<?> b){
		if(a==int.class&&b==Integer.class)
			return true;
		if(a==long.class&&b==Long.class)
			return true;
		if(a==float.class&&b==Float.class)
			return true;
		if(a==double.class&&b==Double.class)
			return true;
		if(a==short.class&&b==Short.class)
			return true;
		if(a==boolean.class&&b==Boolean.class)
			return true;
		if(a==char.class&&b==Character.class)
			return true;
		if(a==byte.class&&b==Byte.class)
			return true;
		return false;
	}
	/*@SuppressWarnings("deprecation")
	public static Object Parse(String s,Class<?> c){
		if(c==String.class){
			return s;
		}
		if(c==int.class||c==Integer.class){
			return Integer.parseInt(s);
		}
		if(c==short.class||c==Short.class){
			return Short.parseShort(s);
		}
		if(c==long.class||c==Long.class){
			return Long.parseLong(s);
		}
		if(c==float.class||c==Float.class){
			return Float.parseFloat(s);
		}
		if(c==double.class||c==Double.class){
			return Double.parseDouble(s);
		}
		if(c==boolean.class||c==Boolean.class){
			return Boolean.parseBoolean(s);
		}
		if(c==byte.class||c==Byte.class){
			return Byte.parseByte(s);
		}
		if(c==char.class||c==Character.class){
			return s.charAt(0);
		}
		if(c==UUID.class){
			return UUID.fromString(s);
		}
		if(c==Date.class){
			return Date.parse(s);
		}
		try {
			Method m=c.getMethod("parse", String.class);
			return m.invoke(null, new Object[]{s});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}*/
	public static Object ConvertType(Object o,Class<?> c){
		if(o==null)
			return defaultValue(c);
		Class<?> oc=o.getClass();
		//类型一致，直接返回
		if(c.equals(oc))
			return o;
		//若二者都是系统类型，直接返回
		if(oc.isPrimitive()&&c.isPrimitive()){
			return o;
		}
		if(IsWrapper(oc,c)||IsWrapper(c,oc)){
			return o;
		}
		if(c==String.class)
			return o.toString();
		if(oc==String.class){
			//return Parse((String)o,c);
			try {
				StringParser.parse(c, (String)o);
			} catch (Exception e) {
			}
		}
		try
		{
			return c.cast(o); 
		}catch(Exception ex){
			throw new ClassCastException(String.format("类型转换错误，无法将%s类型转换为%s类型！", o.getClass().getName(),c.getName()));
		}
	}
}
