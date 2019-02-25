/**
 * 
 */
package com.li.cson;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.li.cson.MetaDatas.BasicData;

/**
 * @author LCZ
 *
 */
public class MetaFactory {
	static {
		/*String location=MetaFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			jar=URLDecoder.decode(location,System.getProperty("file.encoding"));
		} catch (UnsupportedEncodingException e1) {
			
		}*/
		ArrayList<Class<?>> types=allMetaDataClass();
		try {
			scanMetaDatas(types);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private static final String METHORDNAME_SUPPORT = "supported";
	//private static String jar;
	private static ArrayList<Method> checksupports;
	private static ConcurrentHashMap<Byte,MetaInfo> meta_minfo;
	private static ConcurrentHashMap<Integer,MetaInfo> type_minfo;
	private static Hashtable<Byte, Class<?>> mt;
	private static HashSet<Byte> fixedmetas;
	private static HashSet<Integer> fixedTypes;
	
	private static ArrayList<Class<?>> allMetaDataClass(){
		String location=MetaFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String path=null;
		ArrayList<Class<?>> list=null;
		try {
			path=URLDecoder.decode(location,System.getProperty("file.encoding"));
		} catch (UnsupportedEncodingException e1) {
		}
		if(path.endsWith(".jar")){
			list = getFromJAR(path);
		}
		else{
			list=getFromBin(path);
		}
		return list;
	}
	private static ArrayList<Class<?>> getFromJAR(String jar) {
		JarInputStream jarStream=null;
		FileInputStream fstream=null;
		ArrayList<Class<?>> list=new ArrayList<Class<?>>();
		ClassLoader loader=Thread.currentThread().getContextClassLoader();
		try{
			fstream=new FileInputStream(jar);
		    jarStream=new JarInputStream(fstream);
			JarEntry entry;  
	        while ((entry = jarStream.getNextJarEntry()) != null) {  
	        	String entryName=entry.getName();
	        	if(!entryName.endsWith(".class")||!entryName.startsWith("com/li/cson/MetaDatas"))
	        		continue;
	        	String name=entryName.substring(0,entryName.length()-6).replace('/', '.');
	        	Class<?> cls=Class.forName(name,true,loader);
	        	if(!MetaBase.class.isAssignableFrom(cls))
	        		continue;
	        	list.add(cls);
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
	private static ArrayList<Class<?>> getFromBin(String dir) {
		ArrayList<Class<?>> list=new ArrayList<Class<?>>();
		ClassLoader loader=Thread.currentThread().getContextClassLoader();
		File file=new File(dir+"com/li/cson/MetaDatas");
		File[] childs = file.listFiles();
		if(childs==null)
			return null;
		try{
			for(File f:childs){
				if(f.isHidden()||f.isDirectory()||!f.getName().endsWith(".class"))
					continue;
				String name=f.getName();
				name=name.substring(0,name.length()-6);
				name="eagle.cson.MetaDatas."+name;
				Class<?> cls=Class.forName(name,true,loader);
	        	if(!MetaBase.class.isAssignableFrom(cls))
	        		continue;
	        	list.add(cls);
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		return list;
		
	}
	private static Class<?> getClassFromString(String name) throws ClassNotFoundException{
		Class<?> cls;
		if(name.endsWith("[]")){
			Class<?> icls=Class.forName(name.substring(0, name.length()-2));
			cls=Array.newInstance(icls, 0).getClass();
		}
		else{
			cls=Class.forName(name);
		}
		return cls;
	}
	private static void setBasicMD(){
		MetaInfo m=BasicData.M_basic(int.class);
		meta_minfo.put(m.Code, m);
		type_minfo.put(int.class.hashCode(), m);
		fixedTypes.add(int.class.hashCode());
		
		m=BasicData.M_basic(short.class);
		meta_minfo.put(m.Code, m);
		type_minfo.put(short.class.hashCode(), m);
		fixedTypes.add(short.class.hashCode());
		
		m=BasicData.M_basic(long.class);
		meta_minfo.put(m.Code, m);
		type_minfo.put(long.class.hashCode(), m);
		fixedTypes.add(long.class.hashCode());
		
		m=BasicData.M_basic(byte.class);
		meta_minfo.put(m.Code, m);
		type_minfo.put(byte.class.hashCode(), m);
		fixedTypes.add(byte.class.hashCode());
		
		m=BasicData.M_basic(float.class);
		meta_minfo.put(m.Code, m);
		type_minfo.put(float.class.hashCode(), m);
		fixedTypes.add(float.class.hashCode());
		
		m=BasicData.M_basic(double.class);
		meta_minfo.put(m.Code, m);
		type_minfo.put(double.class.hashCode(), m);
		fixedTypes.add(double.class.hashCode());
		
		m=BasicData.M_basic(char.class);
		meta_minfo.put(m.Code, m);
		type_minfo.put(char.class.hashCode(), m);
		fixedTypes.add(char.class.hashCode());
		
		m=BasicData.M_basic(boolean.class);
		meta_minfo.put(m.Code, m);
		type_minfo.put(boolean.class.hashCode(), m);
		fixedTypes.add(boolean.class.hashCode());
	}
	private static void scanMetaDatas(ArrayList<Class<?>> types) throws ClassNotFoundException{
		checksupports=new ArrayList<Method>();
		meta_minfo=new ConcurrentHashMap<Byte,MetaInfo>();
		type_minfo=new ConcurrentHashMap<Integer,MetaInfo>();
		fixedmetas=new HashSet<Byte>();
		fixedTypes=new HashSet<Integer>();
		mt=new Hashtable<Byte,Class<?>>();
		setBasicMD();
		for(Class<?> type:types){
			Method isSupportedMethod=null;
			try {
				isSupportedMethod = type.getMethod(METHORDNAME_SUPPORT,Class.class);
				checksupports.add(isSupportedMethod);
			} catch (Exception e) {}
			CsonRegister reg=type.getAnnotation(CsonRegister.class);
            if (reg == null || reg.metaCodes().length == 0)
                continue;
            /*if(reg.metaCodes().length!=reg.isFixed().length||reg.metaCodes().length!=reg.supports().length)
            	System.out.print("LENGTH NOT EQUAL!");*/
            for(int i=0;i<reg.metaCodes().length;i++){
            	
            	MetaInfo mi1=new MetaInfo(); 
            	mi1.Code = reg.metaCodes()[i];
                mi1.IsFixed =reg.isFixed()[i];
                mi1.MetaType = type;
                meta_minfo.putIfAbsent(mi1.Code, mi1);
                if(mi1.IsFixed){
                	fixedmetas.add(mi1.Code);
                }
                if (reg.supports().length>i&&reg.supports()[i] != null&&reg.supports()[i].length()>0){
                	String[] snlist=reg.supports()[i].split(",");
                	Class<?> fcls=null;
                	for(String sn:snlist){
                		Class<?> cls=getClassFromString(sn);
                		type_minfo.putIfAbsent(cls.hashCode(), mi1);
                		if(mi1.IsFixed){
                    		fixedTypes.add(cls.hashCode());
                    	}
                		if(fcls==null)
                			fcls=cls;
                	}
                	if (snlist.length == 1){
                        mt.put(mi1.Code, fcls);
                    }
                }
            }
            
		}
	}
	private static MetaInfo findMetaInfo(Class<?> type){
        int id = type.hashCode();
        MetaInfo mi=type_minfo.get(id);;
        if (mi==null)
        {
            if (checksupports.size() > 0)
            {
                for(Method fun:checksupports)
                {
                	Object obj;
                	try
                	{
                		obj=fun.invoke(null, type);
                	}
                	catch(Exception ex){
                		throw new RuntimeException(ex);
                	}
                    if (obj != null)
                    {
                    	mi=(MetaInfo)obj;
                        break;
                    }
                }
                if (mi != null)
                {
                    type_minfo.putIfAbsent(id, mi);
                    meta_minfo.putIfAbsent(mi.Code, mi);
                }
            }
        }
        return mi;
    }
	public static MetaBase getMetaData(byte meta)
    {
        MetaInfo mi=meta_minfo.get(meta);
        if (mi==null)
        {
            throw new CSONException(String.format("不支持meta值为%d的CSON数据类型！", meta));
        }
        try
        {
        	MetaBase metaInst = (MetaBase)mi.MetaType.newInstance();
	        metaInst.init1(mi.Code, mi.IsFixed);
	        return metaInst;
        }
        catch(Exception ex){
        	throw new RuntimeException(ex);
        }
    }
	public static MetaBase getMetaData(Class<?> type)
    {
        if (type == null)
        {
            return getMetaData((byte)0);
        }
        try
        {
	        MetaInfo mi = findMetaInfo(type);
	        MetaBase metaInst = (MetaBase)mi.MetaType.newInstance();
	        metaInst.init1(mi.Code, mi.IsFixed);
	        return metaInst;
        }
        catch(Exception ex){
        	throw new RuntimeException(ex);
        }
    }
	public static byte getMetaCode(Class<?> type)
    {
        MetaInfo mi = findMetaInfo(type);
        return mi.Code;
    }
	public static boolean isFixed(byte meta)
    {
        return fixedmetas.contains(meta);
    }
    public static boolean isFixed(Class<?> type)
    {
        if (type == null)
            return true;
        return fixedTypes.contains(type.hashCode());
    }
    public static boolean isSupport(Class<?> type)
    {
        try{
            return getMetaCode(type) > 0;
        }
        catch (CSONNotSupportedException ex)
        {
            return false;
        }
    }
    public static Class<?> tryFindType(byte meta)
    {
    	Class<?> t=mt.get(meta);
    	return t;
    }
}
