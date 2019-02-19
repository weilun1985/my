package com.li.myweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.codehaus.jackson.map.ObjectMapper;

import com.li.core.StringParseUnSupportException;
import com.li.core.StringParser;
import com.li.cson.CSON2;
import com.alibaba.fastjson.JSON;

public class Utils {
	public static class ParamParser{
		/*static final Pattern _regex_date=Pattern.compile(
				"^(\\d{2}|\\d{4})(\\-|/)(\\d{1,2})\\2(\\d{1,2})(\\s(\\d{1,2})\\:(\\d{1,2})(\\:(\\d{1,2})|)|)$"
				,Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);*/
		static final Pattern _regex_set=Pattern.compile("^set([A-Za-z]\\w*|_\\w+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
		@SuppressWarnings("rawtypes")
		private static ConcurrentHashMap<Long, HashMap> _cache_prosets=new ConcurrentHashMap<Long, HashMap>();
		public static Object[] parse(String[] paramNames,Class<?>[] pmTypes,Object implInst,Map<String,String[]> map) 
		throws Exception
		{
			if(pmTypes==null||pmTypes.length==0)
				return new Object[0];
			Object[] pms=new Object[pmTypes.length];
			
			//循环设置参数
			for(int i=0;i<pms.length;i++){
				Object obj=null;
				String name=paramNames[i].toLowerCase();    //参数名称
				Class<?> type=pmTypes[i];            //参数类型
				
				if(map==null){
					pms[i]=defaultValue(type);
					continue;
				}
				
				//如果参数是简单类型
				if(StringParser.supported(type)){
					String[] strVArr=map.get(name);
					//如果没有值，赋给默认值；否则进行类型转换
					if(strVArr==null||strVArr.length==0){
						obj=StringParser.parse(type,null);
					}
					else{
						obj=StringParser.parse(type,strVArr[0]);
					}
				}
				//数组类型
				else if(type.isArray()){
					//获取元素类型
					Class<?> ctype=type.getComponentType();
					if(StringParser.supported(ctype)){
						//简单类型的数组类型
						obj=parseSimpleTypeArray(ctype,map.get(name));
					}
					else{
						//实体类型的数组类型
						obj=parseModelArray(name,ctype,implInst,map);
					}
				}
				//实体类型
				else{
					obj=parseModel(name,type,implInst,map);
				}
				pms[i]=obj;
			}
			return pms;
		}
		/*
		//基本类型转换
		private static Object parsePrimitive(Class<?> cls,String str){
			Object obj=null;
			if(cls==int.class){
				if(str==null||str.length()==0)
					obj=0;
				else
					obj= Integer.parseInt(str);
			}
			else if(cls==double.class){
				if(str==null||str.length()==0)
					obj=0D;
				else
					obj=Double.parseDouble(str);
			}
			else if(cls==float.class){
				if(str==null||str.length()==0)
					obj=0F;
				else
					obj=Float.parseFloat(str);
			}
			else if(cls==long.class){
				if(str==null||str.length()==0)
					obj=0L;
				else
					obj=Long.parseLong(str);
			}
			else if(cls==short.class){
				if(str==null||str.length()==0)
					obj=0;
				else
					obj=Short.parseShort(str);
			}
			else if(cls==boolean.class){
				if(str==null||str.length()==0)
					obj=false;
				else
					obj=Boolean.parseBoolean(str);
			}
			else if(cls==byte.class){
				if(str==null||str.length()==0)
					obj=0;
				else
					obj=Byte.parseByte(str);
			}
			else if(cls==char.class){
				if(str==null||str.length()==0)
					obj=(char)0;
				else
					obj=str.charAt(0);
			}
			return obj;
		}
		//简单类型转换
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static Object parseSimpleType(Class cls,String str) 
		throws ParseException{
			if(cls==String.class)
				return str;
			if(cls.isPrimitive())
				return parsePrimitive(cls,str=str.trim());
			if(cls.isEnum()){
				if(str==null||(str=str.trim()).length()==0)
					return null;
				else
					return java.lang.Enum.valueOf(cls, str);
			}
			if(cls==UUID.class){
				if(str==null||(str=str.trim()).length()==0)
					return null;
				else
					return UUID.fromString(str);
			}
			if(cls==Date.class){
				if(str==null||(str=str.trim()).length()==0)
					return null;
				else{
					DateFormat format=getDateFormat(str);
					return format.parse(str);
				}
			}
			if(cls==InetAddress.class){
				try {
					return InetAddress.getByName(str);
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				}
			}
			try {
				Method m=cls.getMethod("parse", String.class);
				if(m!=null)
					return m.invoke(null, new Object[]{str});
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
			return null;
		}*/
		/*//判定是否是简单类型
		public static boolean isSimpleType(Class<?> cls){
			return cls==String.class
			||cls.isPrimitive()
			||cls.isEnum()
			||cls==UUID.class
			||cls==Date.class;
		}*/
		@SuppressWarnings("unchecked")
		private static HashMap<String,Object> getModelSetProperties(Class<?> cls){
			HashMap<String,Object> hm;
			long hs=cls.hashCode();
			hm=_cache_prosets.get(hs);
			if(hm==null){
				hm=new HashMap<String,Object>();
				for(Field f:cls.getFields()){
					hm.put(f.getName().toLowerCase(), f);
				}
				for(Method m:cls.getMethods()){
					String mname=m.getName();
					if(mname.length()>3
							&&_regex_set.matcher(mname).find()
							&&m.getParameterTypes().length==1
							&&m.getReturnType()==void.class){
						String name=mname.substring(3).toLowerCase();
						if(!hm.containsKey(name)){
							hm.put(name, m);
						}
					}
				}
				_cache_prosets.put(hs, hm);
			}
			return hm;
		}
		//实体类型转换
		@SuppressWarnings("rawtypes")
		public static Object parseModel(String pmName,Class cls,Object declInstn,Map<String,String[]> map) 
		throws Exception 
		{
			//实例
			Object inst=null;
			try
			{
				//CSON
				if(cls==CSON2.class){
					String[] values=map.get(pmName);
					if(values!=null&&values.length>0&&values[0].length()>0){
						String cb64=values[0];
						CSON2 cson = new CSON2();
				        cson.read(cb64);
				        inst=cson;
					}
				}
				else{
					Map hm=getModelSetProperties(cls);
					for(Object itm:hm.entrySet()){
						Entry entry=(Entry)itm;
						String pname=(String)entry.getKey();
						String itemName=pmName+"."+pname;
						String[] values=map.get(itemName);
						if(values==null||values.length==0){
							continue;
						}
						if(inst==null){
							if(cls.isMemberClass()){
								@SuppressWarnings("unchecked")
								Constructor constructor=cls.getConstructor(cls.getDeclaringClass());
								if(constructor!=null){
									if(declInstn==null){
										declInstn=cls.getDeclaringClass().newInstance();
									}
									inst=constructor.newInstance(declInstn);
								}
								else{
									return null;
								}
							}
							else{
								inst=cls.newInstance();
							}
						}
						Object set=entry.getValue();
						if(set instanceof Field){
							Field field=(Field)set;
							Class f_cls= field.getType();                        //属性的类型
							//Object f_obj=parseSimpleType(f_cls,values[0]);       //属性的值
							Object f_obj=StringParser.parse(f_cls,values[0]);
							//设置属性值
							field.set(inst, f_obj);
						}
						else{
							Method m=(Method)set;
							Class f_cls=m.getParameterTypes()[0];
							//Object f_obj=parseSimpleType(f_cls,values[0]);
							Object f_obj=StringParser.parse(f_cls,values[0]);
							//设置属性值
							m.invoke(inst, f_obj);
						}
					}
				}
				/*
				//获取类型的所有属性
				Field[] fields=cls.getFields();
				//属性的参数名格式：参数名.属性名
				for(int i=0;i<fields.length;i++){
					Field field=fields[i];
					String fieldName=field.getName().toLowerCase();
					String itemName=pmName+"."+fieldName;
					String[] values=map.get(itemName);
					if(values==null||values.length==0){
						itemName=pmName+":"+fieldName;
						values=map.get(itemName);
						if(values==null||values.length==0){
							continue;
						}
					}
					if(inst==null){
						if(cls.isMemberClass()){
							@SuppressWarnings("unchecked")
							Constructor constructor=cls.getConstructor(cls.getDeclaringClass());
							if(constructor!=null){
								if(declInstn==null){
									declInstn=cls.getDeclaringClass().newInstance();
								}
								inst=constructor.newInstance(declInstn);
							}
							else{
								return null;
							}
						}
						else{
							inst=cls.newInstance();
						}
					}
					Class f_cls= field.getType();      //属性的类型
					Object f_obj=parseSimpleType(f_cls,values[0]);       //属性的值
					//设置属性值
					field.set(inst, f_obj);
					
				}*/
			}
			catch(Exception ex){
				throw ex;
			}
			return inst;
		}
		//简单类型数组转换
		@SuppressWarnings("rawtypes")
		public static Object parseSimpleTypeArray(Class cls,String[] strarr) 
		throws ArrayIndexOutOfBoundsException, IllegalArgumentException
		, ParseException, StringParseUnSupportException{
			if(strarr==null)
				return null;
			//如果值数组的长度为0,直接返回长度为0的数组
			if(strarr.length==0)
				return Array.newInstance(cls, 0);
			//如果不是String类型，且strarr维度为1，且值中包含有“,”,按“,”分隔组合成一个String[]
			if(cls!=String.class&&strarr.length==1&&strarr[0].indexOf(",")!=-1){
				strarr=strarr[0].split(",");
			}
			Object arr=Array.newInstance(cls, strarr.length);
			
			for(int i=0;i<strarr.length;i++){
				//Array.set(arr, i, parseSimpleType(cls,strarr[i]));
				Array.set(arr, i, StringParser.parse(cls,strarr[i]));
			}
			return arr;
		}
		//实体型数组转换
		@SuppressWarnings("rawtypes")
		public static Object parseModelArray(String pmName,Class cls,Object declInstn,Map<String,String[]> map) 
		throws Exception{
			//Field[] fields=cls.getFields();                     //类型的所有属性
			//Class[] field_clss=new Class[fields.length];        //所有属性的类型
			//String[][] pmValues=new String[fields.length][];    //各属性字段对应的值序列
			Map hm=getModelSetProperties(cls);
			if(hm.size()==0)
				return null;
			Class[] field_clss=new Class[hm.size()];
			String[][] pmValues=new String[hm.size()][];
			Object[] fields=new Object[hm.size()];
			int arrLen=-1;										//实体数组的长度
			/*//获取值序列
			for(int i=0;i<fields.length;i++){
				Field field=fields[i];
				field_clss[i]=field.getType();
				String itemName=pmName+"."+field.getName().toLowerCase();
				pmValues[i]=map.get(itemName);
				if(pmValues[i]!=null&&pmValues[i].length>arrLen)
					arrLen=pmValues[i].length;
			}*/
			int index=0;
			for(Object itm:hm.entrySet()){
				Entry entry=(Entry)itm;
				Object set=entry.getValue();
				fields[index]=set;
				if(set instanceof Field){
					Field field=((Field)set);
					field_clss[index]=field.getType();
				}
				else{
					Method m=((Method)set);
					field_clss[index]=m.getParameterTypes()[0];
				}
				String itemName=pmName+"."+entry.getKey();
				pmValues[index]=map.get(itemName);
				if(pmValues[index]!=null&&pmValues[index].length>arrLen)
					arrLen=pmValues[index].length;
				index++;
			}
			if(arrLen==-1)
				return null;
			try
			{
				Object arr=Array.newInstance(cls, arrLen);
				for(int i=0;i<arrLen;i++){
					Object inst;
					if(cls.isMemberClass()){
						@SuppressWarnings("unchecked")
						Constructor constructor=cls.getConstructor(cls.getDeclaringClass());
						if(constructor!=null){
							if(declInstn==null){
								declInstn=cls.getDeclaringClass().newInstance();
							}
							inst=constructor.newInstance(declInstn);
						}
						else{
							inst=null;
						}
					}
					else{
						inst=cls.newInstance();
					}
					if(inst!=null){
						for(int j=0;j<fields.length;j++){
							if(pmValues[j]==null||pmValues[j].length<=i)
								continue;
							if(fields[j] instanceof Field){
								Field field=(Field)fields[j];
								//field.set(inst, parseSimpleType(field_clss[j],pmValues[j][i]));
								field.set(inst, StringParser.parse(field_clss[j],pmValues[j][i]));
							}
							else{
								Method field=(Method)fields[j];
								//field.invoke(inst, parseSimpleType(field_clss[j],pmValues[j][i]));
								field.invoke(inst, StringParser.parse(field_clss[j],pmValues[j][i]));
							}
						}
					}
					Array.set(arr, i, inst);
				}
				return arr;
			}
			catch(Exception ex){
				throw ex;
			}
		}
		
		/*//获取日期格式化形式
		private static DateFormat getDateFormat(String dateStr){
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
		}*/
	}
	private final static Pattern REX_W=Pattern.compile("[A-Za-z]+");
	//组合数组，形成一串String
	public static String join(AbstractCollection<?> collection,String sep){
		if(collection==null)
			return null;
		if(collection.isEmpty())
			return "";
		Iterator<?> enumer=collection.iterator();
		StringBuffer buffer=new StringBuffer();
		buffer.append(enumer.next());
		while(enumer.hasNext()){
			buffer.append(sep);
			buffer.append(enumer.next());
		}
		return buffer.toString();
	}
	public static String join(Enumeration<?> enumer,String sep){
		if(enumer==null)
			return null;
		if(!enumer.hasMoreElements())
			return "";
		StringBuffer buffer=new StringBuffer();
		buffer.append(enumer.nextElement());
		while(enumer.hasMoreElements()){
			buffer.append(sep);
			buffer.append(enumer.nextElement());
		}
		return buffer.toString();
	}
	public static String join(String[] arry,String sep){
		if(arry==null)
			return null;
		if(arry.length==0)
			return "";
		StringBuffer buffer=new StringBuffer();
		for(String s:arry){
			if(buffer.length()>0)
				buffer.append(sep);
			buffer.append(s);
		}
		return buffer.toString();
	}
	public static boolean[] isImplements(Class<?> cls,Class<?>[] interfs){
		boolean[] rs=new boolean[interfs.length];
		while(cls!=null&&cls!=Object.class){
			 Class<?>[] face = cls.getInterfaces();
			 if(face==null||face.length==0)
				 break;
			 for(int i=0;i<interfs.length;i++){
				 if(rs[i])
					 continue;
				 for(int j=0;j<face.length;j++){
					 if(face[j]==interfs[i]){
						 rs[i]=true;
					 }
				 }
			 }
			 boolean found=true;
			 for(boolean b:rs){
				 found&=b;
			 }
			 //如果接口已经全部找到，跳出循环
			 if(found)
				 break;
			 //如果当前类的父类为null或Object，跳出循环
			 cls=cls.getSuperclass();
		}
		return rs;
	}
	public static Properties convertProperties(String cfgStr){
		if(cfgStr==null)
			return null;
		Properties props=new Properties();
		if(cfgStr!=null&&cfgStr.length()>0){
			for(String s:cfgStr.split(";")){
				String[] itms=s.split("=",2);
				if(itms.length<2||itms[0].length()==0||itms[1].length()==0)
					continue;
				props.put(itms[0], itms[1]);
			}
		}
		return props;
	}
	public static String md5String(String input,int bit) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("MD5"); 
		byte[] b=md.digest(input.getBytes());
		int i;
		StringBuffer buf = new StringBuffer(""); 
		for (int offset = 0; offset < b.length; offset++) { 
			i = b[offset]; 
			if(i<0) i+= 256; 
			if(i<16) 
			buf.append("0"); 
			buf.append(Integer.toHexString(i)); 
		}
		if(bit==32)
			return buf.toString();//32位
		else if(bit==16)
			return buf.substring(8,24);//16位
		return null;
	}
	public static String md5String(String input) throws NoSuchAlgorithmException{
		return md5String(input,16);
	}
	public static Object[] paramParse(String[] paramNames,Class<?>[] pmTypes,Object declarInstns,Map<String,String[]> map) 
	throws Exception{
		return ParamParser.parse(paramNames, pmTypes,declarInstns, map);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String,List<String>> ignoreKeyUL(Map<String,List<String>> ht){
		Map<String,List<String>> ht2=new HashMap<String,List<String>>();
		for(Object eo:ht.entrySet()){
			Entry entry=(Entry)eo;
			String key=(String)entry.getKey();
			String keyLower=key.toLowerCase();
			List<String> ht2_v=ht2.get(keyLower);
			List<String> v=(List<String>)entry.getValue();
			if(ht2_v==null){
				ht2.put(keyLower, v);
			}
			else{
				ht2_v.addAll(v);
			}
		}
		/*Enumeration<String> kEnumers= ht.keys();
		while(kEnumers.hasMoreElements()){
			String key=kEnumers.nextElement();
			String keyLower=key.toLowerCase();
			List<String> ht2_v=ht2.get(keyLower);
			List<String> v=ht.get(key);
			if(ht2_v==null){
				ht2.put(keyLower, v);
			}
			else{
				ht2_v.addAll(v);
			}
		}*/
		return ht2;
	}
	//忽略方法名大小写获取公共方法
	public static Method lookupMethodIg(Class<?> cls,Class<?> stopCls ,String methodname){
		Method[] methods=cls.getMethods();
		for(Method m:methods){
			if(m.getName().equalsIgnoreCase(methodname))
			{
				return m;
			}
		}
		//没有找到，且当前类的父类不是Controller,则寻找当前类的父类方法
		Class<?> parent=cls.getSuperclass();
		if(parent!=null&&parent!=stopCls&&parent!=Object.class)
			return lookupMethodIg(parent,stopCls,methodname);
		else
			return null;
	}
	public static List<String> listJarClasses(File jar){
		List<String> list=new ArrayList<String>();
		JarInputStream jarStream=null;
		FileInputStream fstream=null;
		try{
			fstream=new FileInputStream(jar);
		    jarStream=new JarInputStream(fstream);
			JarEntry entry;  
	        while ((entry = jarStream.getNextJarEntry()) != null) {  
	        	String entryName=entry.getName();
	        	if(!entryName.endsWith(".class"))
	        		continue;
	        	String clsName=entryName.substring(0,entryName.length()-6).replace('/', '.');
	        	list.add(clsName);
	        }  
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		finally{
			try
			{
				if(jarStream!=null){
					jarStream.closeEntry();
					jarStream.close();
				}
				if(fstream!=null){
					fstream.close();
					fstream=null;
				}
			}
			catch(Exception e){
			}
		}
		return list;
	}
	public static List<String> listDirClasses(File dir){
		List<String> list=new ArrayList<String>();
		File[] files=dir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File f) {
				if(f.isFile()&&f.getName().toLowerCase().endsWith(".class")||f.isDirectory())
					return true;
				return false;
			}});
		for(File f:files){
			if(f.isDirectory()){
				list.addAll(listDirClasses(f));
			}else{
				String fpath=f.getPath();
				int a=fpath.lastIndexOf("classes")+8;
				int b=fpath.lastIndexOf(".");
				String name=f.getPath().substring(a, b).replace(File.separator, ".");
				list.add(name);
			}
		}
		return list;
	}
	public static String matchNameFromDirectory(String nameIg,String dir){
		String realName=null;
		//对类名按“.”进行切分
		String[] sp=Utils.split(nameIg, ".");
		String lfname=sp[sp.length-1]+".class";
		String[] rsp=new String[sp.length];
		int deep=0;
		File file=new File(dir);
		while(file!=null&&deep<sp.length){
			File[] childs = file.listFiles();
			if(childs==null)
				return null;
			boolean matched=false;
			for(File f:childs){
				if(f.isHidden())
					continue;
				String name=f.getName();
				//如果deep值是数组长度-1，则表示此时开始比较文件名
				if(deep==sp.length-1&&f.isFile()&&name.equalsIgnoreCase(lfname)){
					rsp[deep++]=name.substring(0,name.lastIndexOf('.'));
					matched=true;
					break;
				}
				if(f.isFile())
					continue;
				//比较目录名,如果目录相同则跳出，进入下一级比对
				if(name.equalsIgnoreCase(sp[deep])){
					rsp[deep++]=name;
					file=f;
					matched=true;
					break;
				}
			}
			//本级未匹配到,可以认为无法匹配，跳出
			if(!matched){
				return null;
			}
			
		}
		//匹配到结果
		if(deep==sp.length){
			realName=join(rsp, ".");
		}
		return realName;
	}
	
	public static String matchNameFromJAR(String nameIg,String jarpath){
		String realName=null;
		JarInputStream jarStream=null;
		FileInputStream fstream=null;
		try{
			fstream=new FileInputStream(jarpath);
		    jarStream=new JarInputStream(fstream);
			JarEntry entry;  
	        while ((entry = jarStream.getNextJarEntry()) != null) {  
	        	String entryName=entry.getName();
	        	if(!entryName.endsWith(".class"))
	        		continue;
	            String name2=entryName.substring(0,entryName.length()-6).replace('/', '.'); 
	            if (name2.equalsIgnoreCase(nameIg)) {  
	                realName=name2; 
	                break;
	            }  
	        }  
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		finally{
			try
			{
				if(jarStream!=null){
					jarStream.closeEntry();
					jarStream.close();
				}
				if(fstream!=null){
					fstream.close();
					fstream=null;
				}
			}
			catch(Exception e){
			}
		}
		return realName;
	}
	public static String[] split(String input,String sep){
		int a=0;
		int b=input.indexOf(sep);
		if(b==-1)
			return new String[]{input};
		ArrayList<String> list=new ArrayList<String>();
		while(b>0){
			list.add(input.substring(a,b));
			a=b+1;
			b=input.indexOf(sep, a);
		}
		if(a<input.length())
			list.add(input.substring(a));
		String[] arr=new String[list.size()];
		return list.toArray(arr);
	}
	public static String[] allULCompounding(String input){
		Matcher matcher=REX_W.matcher(input);
		if(!matcher.find()){
			return new String[]{input};
		}
		int s=0;
		for(int i=0;i<input.length();i++){
			s=(s<<1)|1;
		}
		HashSet<String> set=new HashSet<String>();
		input=input.toLowerCase();
		set.add(input);
		for(int i=0;i<=s;i++){
			char[] chars=input.toCharArray();
			boolean changed=false;
			for(int j=0;j<input.length();j++){
				if(chars[j]<'A'||(chars[j]>'Z'&&chars[j]<'a')||chars[j]>'z')
					continue;
				if((i&(1<<j))!=0){
					chars[j]=(char)((int)chars[j]-32);
					changed|=true;
				}
			}
			if(changed)
				set.add(new String(chars));
		}
		String[] arr=new String[set.size()];
		set.toArray(arr);
		return arr;
	}
	public static long readLong(byte[] byts){
		long a=0;
		for(int i=0;i<byts.length;i++){
			long b=byts[i];
			a=a|b<<(i*8);
		}
		return a;
	}
	public static List<String> readTextFileAllLine(File file) throws IOException{
		ArrayList<String> lines=new ArrayList<String>();
		FileInputStream inputStream=null;
		InputStreamReader inputReader=null;
		BufferedReader reader=null;
		try
		{
			inputStream=new FileInputStream(file);
			inputReader=new InputStreamReader(inputStream,"utf-8");
			reader=new BufferedReader(inputReader);
			String line;
			while((line=reader.readLine())!=null){
				if(line.charAt(0)==65279)
					line=line.substring(1);
				lines.add(line);
			}
		}
		finally{
			if(inputStream!=null)
				inputStream.close();
			if(inputReader!=null)
				inputReader.close();
			if(reader!=null)
				reader.close();
		}
		return lines;
	}
	public static List<String> readRemoteFileAllLine(String url) throws IOException{
		ArrayList<String> lines=new ArrayList<String>();
		InputStreamReader inputReader=null;
		BufferedReader reader=null;
		InputStream inputStream=null;
		try
		{
			HttpClient httpclient =new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response=httpclient.execute(httpget);
			int code=response.getStatusLine().getStatusCode();
			if(code==200){
				inputStream=response.getEntity().getContent();
				inputReader=new InputStreamReader(inputStream,"utf-8");
				reader=new BufferedReader(inputReader);
				String line;
				while((line=reader.readLine())!=null){
					if(line.charAt(0)==65279)
						line=line.substring(1);
					lines.add(line);
				}
			}
			else{
				return null;
			}
		}
		finally{
			if(inputStream!=null)
				inputStream.close();
			if(inputReader!=null)
				inputReader.close();
			if(reader!=null)
				reader.close();
		}
		return lines;
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
	//private static ObjectMapper jsonOM = new ObjectMapper();
	public static String writeJson(Object obj) {
		if(obj == null){
			return null;
		}
		try {
			//return jsonOM.writeValueAsString(obj);
			return JSON.toJSONString(obj);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object readJson(String json,Class cls){
		return JSON.parseObject(json, cls);
	}
	@SuppressWarnings("rawtypes")
	public static Object[] readJson(String json, Class[] cls){
		return JSON.parseArray(json, cls).toArray();
	}
	@SuppressWarnings("rawtypes")
	public static Object readCson(String cson,Class cls){
		return CSON2.deserialize(cson, cls);
	}
	@SuppressWarnings("rawtypes")
	public static Object[] readCson(String cson,Class[] cls){
		return (Object[])CSON2.deserialize(cson, cls);
	}
	
}
