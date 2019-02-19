package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.li.cson.CSON2;
import com.li.cson.CSONException;
import com.li.cson.CsonRegister;
import com.li.cson.KEYS.KEYITEM;
import com.li.cson.MetaBase;
import com.li.cson.MetaFactory;
import com.li.cson.MetaInfo;
import com.li.cson.STRUCTS.STRUCTITEM;
import com.li.cson.Utils;

@SuppressWarnings("rawtypes")
@CsonRegister(isFixed = { false }, metaCodes = { 15 }, supports = {})
public class ComplexData extends MetaBase {
	public class PropertyNameException extends CSONException
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public PropertyNameException(String n1,String n2){
			super(String.format("CSON不支持忽略大小写的情况下存在同名字段或属性：%s与%s", n1,n2));
		}
	}
	static{
		_regex_promethod=Pattern.compile("^(get|set)([A-Za-z]\\w*|_\\w+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
		_cached_pros=new ConcurrentHashMap<String,Map>();
		HashSet<String> hs=new HashSet<String>();
		Method[] ms=Object.class.getMethods();
		for(Method m:ms){
			if(isPropertyMethod(m))
				hs.add(m.getName());
		}
		_promethod_except=hs;
	}
	
	static final Pattern _regex_promethod;
	static final HashSet<String> _promethod_except;

	static final ConcurrentHashMap<String,Map> _cached_pros;
	private int structId;
    private ArrayList<MetaBase> metaDatas;
    private ArrayList<Integer> keyindexs;
    private static boolean isPropertyMethod(Method m){
		String mname=m.getName();
		if(mname.length()>3
				&&_regex_promethod.matcher(mname).find()){
			if(mname.startsWith("s")){
				if(m.getParameterTypes().length==1
							&&m.getReturnType()==void.class)
					return true;
			}
			else{
				if(m.getParameterTypes().length==0
						&&m.getReturnType()!=void.class)
					return true;
			}
		}
		return false;
	}
    @SuppressWarnings("unchecked")
	private Map<String,Property> getProperty(Class<?> cls)
	{
    	Map<String,Property> hm;
    	String id=cls.getName();
    	hm=_cached_pros.get(id);
    	if(hm!=null)
    		return hm;
    	hm=new HashMap<String,Property>();
    	//扫描字段
    	for(Field f:cls.getFields()){
    		Property pro=new Property(f.getName(),f);
    		String key=f.getName().toLowerCase();
    		Property pro_o=hm.get(key);
    		if(pro_o!=null)
    		{
    			throw new PropertyNameException(f.getName(),pro_o.getName());
    		}
    		hm.put(key, pro);
    	}
    	//扫描属性方法
    	Map<String,Object[]> thm=new HashMap<String,Object[]>();
    	for(Method m:cls.getMethods()){
			if(_promethod_except.contains(m.getName())||!isPropertyMethod(m))
				continue;
			String name=m.getName().substring(3);
			String key=name.toLowerCase();
			Property pro_o1=hm.get(key);
			if(pro_o1!=null){
				throw new PropertyNameException(name+":"+m.getName(),pro_o1.getName());
			}
			int k=1;
			if(m.getName().startsWith("s"))
				k=2;
			Object[] ms=thm.get(key);
			if(ms==null){
				ms=new Object[3];
				ms[0]=name;
				ms[k]=m;
			}else{
				if(ms[k]!=null)
					throw new PropertyNameException(name+":"+m.getName(),k==1?"get":"set"+(String)ms[0]);
				else
					ms[k]=m;
			}
			thm.put(key, ms);
		}
    	//归并
    	for(Entry<String,Object[]> entry:thm.entrySet()){
    		String proname=((Object[])entry.getValue())[0].toString();
    		hm.put(entry.getKey(), new Property(proname,entry.getValue()));
    	}
    	_cached_pros.putIfAbsent(id, hm);
    	return hm;
	}
   /* @SuppressWarnings("unchecked")
	private static Map<String,Property> getProperty(Class<?> cls,boolean set){
    	Map<String,Property> hm;
    	String id=cls.getName()+"#"+(set?"set":"get");
    	hm=_cached_pros.get(id);
    	if(hm!=null)
    		return hm;
    	Map<String,List<Property>> thm=new HashMap<String,List<Property>>();
    	for(Field f:cls.getFields()){
    		Property pro=new Property(f.getName(),f);
    		String key=f.getName().toLowerCase();
    		List<Property> plist=thm.get(key);
    		if(plist==null){
    			plist=new ArrayList<Property>();
    			thm.put(key, plist);
    		}
    		plist.add(pro);
    		//hm.put(f.getName(), new Property(f.getName(),f));
		}
		for(Method m:cls.getMethods()){
			if(_promethod_except.contains(m.getName())||!isPropertyMethod(m))
				continue;
			if(set&&!m.getName().startsWith("s"))
				continue;
			if(!set&&m.getName().startsWith("s"))
				continue;
			String name=m.getName().substring(3);
			String key=name.toLowerCase();
			List<Property> plist=thm.get(key);
			if(plist==null){
    			plist=new ArrayList<Property>();
    			thm.put(key, plist);
    			plist.add(new Property(name,m));
    		}
			else{
				boolean sn=false;
				for(Property p:plist){
					//如果有存在同名属性，则不添加
					if(p.getName().equals(name)){
						sn=true;
						break;
					}
				}
				if(!sn)
					plist.add(new Property(name,m));
			}
			
			if(!hm.containsKey(name)){
				hm.put(name, new Property(name,m));
			}
		}
		hm=new HashMap<String,Property>();
		for(Entry<String,List<Property>> entry:thm.entrySet()){
			String key=entry.getKey();
			List<Property> plist=entry.getValue();
			//如果plist长度为1，直接忽略大小写
			if(plist.size()==1)
				hm.put(key.toLowerCase(), plist.get(0));
			else{
				for(Property itm:plist){
					hm.put(itm.getName(), itm);
				}
			}
		}
		_cached_pros.putIfAbsent(id, hm);
		return hm;
    }*/
    private static Object newInstance(Class<?> t) 
    throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
    	Object inst=null;
    	if(t.isMemberClass()){
    		
			Constructor constructor=t.getConstructor(t.getDeclaringClass());
    		inst=constructor.newInstance(t.getDeclaringClass().newInstance());
    	}
    	else{
    		inst=t.newInstance();
    	}
    	return inst;
    }
    private Object getInstance(Class<?> cls) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
    	Map<String,Property> pros=getProperty(cls);
    	Object inst=newInstance(cls);
    	for (int i = 0; i < this.metaDatas.size(); i++)
        {
            String proname = this.getCsonKeys().getKey(this.keyindexs.get(i)).name;
            String key=proname.toLowerCase();
            Property pro=pros.get(key);
            /*if(pro==null)
            	pro=pros.get(proname);*/
            if(pro==null)
            	continue;
            Object v = this.metaDatas.get(i).getData(pro.getPropertyType());
            pro.set(inst, v);
        }
    	return inst;
    }
    @Override
    public Object getData()
    {
    	STRUCTITEM struct=this.getCsonStructs().getStruct(this.structId);
    	if(struct.structName!=null){
    		//检测是否有映射名
    		Class<?> cls=CSON2.getMapped(struct.structName);
    		try {
    			if(cls==null){
    			   ClassLoader loader=Thread.currentThread().getContextClassLoader();
				   cls=Class.forName(struct.structName,true,loader);
				}
    			if(cls!=null)
    				return this.getInstance(cls);
			} 
    		catch (ClassNotFoundException e) {
			}
			catch(Exception ex){
	    		throw new RuntimeException(ex);
	    	}
    	}
        HashMap<String,Object> ht = new HashMap<String,Object>();
        for (int i = 0; i < this.metaDatas.size(); i++)
        {
            Object v = this.metaDatas.get(i).getData();
            if (v == null)
                continue;
            KEYITEM key = this.getCsonKeys().getKey(this.keyindexs.get(i));
            ht.put(key.name, v);
        }
        return ht;
    }
    @Override
    public Object getData(Class<?> t)
    {
    	try
    	{
    		return this.getInstance(t);
    	}
    	catch(Exception ex){
    		throw new RuntimeException(ex);
    	}
    }
	@Override
    public void setData(Object data){
		Class<?> type=data.getClass();
		//Field[] fields=type.getFields();
		Map<String,Property> hm=getProperty(type);
		keyindexs=new ArrayList<Integer>(hm.size());
		metaDatas=new ArrayList<MetaBase>(hm.size());
		ArrayList<Integer> allkeyindexs=new ArrayList<Integer>(keyindexs.size());
		try
		{
			for(Property f:hm.values()){
				String name=f.getName();
				Class<?> cls=f.getPropertyType();
				MetaBase metaData = MetaFactory.getMetaData(cls);
	            byte meta = metaData.getMetaCode();
	            //添加到KEY域
	            int keyIndex = this.getCsonKeys().addKey(meta, name);
	            allkeyindexs.add(keyIndex);
	            //获取值
	            Object value=f.get(data);
	            if(value!=null){
	            	 metaData.init2(this.getCsonKeys(), this.getCsonStructs());
	                 metaData.setData(value);
	                 this.metaDatas.add(metaData);
	                 this.keyindexs.add(keyIndex);
	            }

			}
			/*for(Field f:fields){
				String name=f.getName();
				Class<?> cls=f.getType();
				MetaBase metaData = MetaFactory.getMetaData(cls);
	            byte meta = metaData.getMetaCode();
	            //添加到KEY域
	            int keyIndex = this.getCsonKeys().addKey(meta, name);
	            allkeyindexs.add(keyIndex);
	            //获取值
	            Object value=f.get(data);
	            if(value!=null){
	            	 metaData.init2(this.getCsonKeys(), this.getCsonStructs());
	                 metaData.setData(value);
	                 this.metaDatas.add(metaData);
	                 this.keyindexs.add(keyIndex);
	            }
			}*/
		}
		catch(Exception ex){
			throw new RuntimeException(ex);
		}
		structId = this.getCsonStructs().addStruct(type.getName(),allkeyindexs);
	}
    
    @Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
    	int len = 0;
        //读取structId
        this.structId = carrier[offset];
        len++;
        if(this.structId==0)
        	return len;
        //读取有值的属性个数
        int count = carrier[offset+len];
        len++;
        this.metaDatas = new ArrayList<MetaBase>(count);
        this.keyindexs = new ArrayList<Integer>(count);
        //循环读取值
        for (int i = 0; i < count; i++)
        {
            int keyId;
            //读取keyid
            if (this.getCsonKeys().getKeyBit() == 1)
            {
                keyId = carrier[offset + len];
                len += 1;
            }
            else
            {
                keyId = Utils.toInt16(carrier, offset + len);
                len += 2;
            }
            //读取值
            byte metaCode = this.getCsonKeys().getKey(keyId).meta;
            MetaBase vmeta = MetaFactory.getMetaData(metaCode);
            vmeta.init2(this.getCsonKeys(), this.getCsonStructs());
            len += vmeta.read(carrier, offset + len);
            this.metaDatas.add(vmeta);
            this.keyindexs.add(keyId);
        }
        return len;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		//写入structId
        stream.write((byte)this.structId);
        if(this.structId==0)
        	return;
        //写入有值的属性个数
        stream.write((byte)this.metaDatas.size());
        //循环写入值
        for (int i = 0; i < this.metaDatas.size(); i++)
        {
            //写入keyId
            if (this.getCsonKeys().getKeyBit() == 1)
                stream.write(this.keyindexs.get(i).byteValue());
            else
            {
            	Utils.writeBinary(stream, this.keyindexs.get(i).shortValue());
            }
            //写入值
            this.metaDatas.get(i).write(stream);
        }
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		 //读取structId
        this.structId = stream.read();
        if(this.structId==0)
        	return;
        //读取有值的属性个数
        int count = stream.read();
        this.metaDatas = new ArrayList<MetaBase>(count);
        this.keyindexs = new ArrayList<Integer>(count);
        //循环读取值
        for (int i = 0; i < count; i++)
        {
            int keyId;
            //读取keyid
            if (this.getCsonKeys().getKeyBit() == 1)
            {
                keyId =stream.read();
            }
            else
            {
                keyId = Utils.toInt16(stream);
            }
            //读取值
            byte metaCode = this.getCsonKeys().getKey(keyId).meta;
            MetaBase vmeta = MetaFactory.getMetaData(metaCode);
            vmeta.init2(this.getCsonKeys(), this.getCsonStructs());
            vmeta.read(stream);
            this.metaDatas.add(vmeta);
            this.keyindexs.add(keyId);
        }
	}
	
	public static MetaInfo supported(Class<?> t){
		if(t.isAnnotation()|t.isInterface()|t.isPrimitive()|t.isArray()|t.isEnum()|List.class.isAssignableFrom(t))
			return null;
		 MetaInfo m= new MetaInfo();
		 m.Code = 15;
		 m.IsFixed = false;
		 m.MetaType=ComplexData.class;
		 return m;
		
	}

}
