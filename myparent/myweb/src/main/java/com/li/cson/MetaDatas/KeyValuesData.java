package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;


import com.li.cson.CSONNotSupportedException;
import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.MetaFactory;

@CsonRegister(isFixed = { false }, metaCodes = { 20 }, supports = {"java.util.Hashtable,java.util.HashMap"})
public class KeyValuesData extends MetaBase {
	private MetaBase metadk;
    private MetaBase metadv;
    @SuppressWarnings("rawtypes")
	@Override
    public void setData(Object data){
    	Class<?> t=data.getClass();
    	ArrayList<Object> list_keys=new ArrayList<Object>();
    	ArrayList<Object> list_values=new ArrayList<Object>();
    	Class<?> kc=null;
    	Class<?> vc=null;
    	if(t==Hashtable.class){
    		Hashtable dict=(Hashtable)data;
    		while(dict.keys().hasMoreElements()){
    			Object k=dict.keys().nextElement();
    			Object v=dict.get(k);
    			list_keys.add(k);
    			list_values.add(v);
    			if(kc==null){
    				kc=k.getClass();
    			}
    			else{
    				if(kc!=Object.class&&kc!=k.getClass())
    					kc=Object.class;
    			}
    			if(vc==null&&v!=null){
    				vc=v.getClass();
    			}
    			else{
    				if(vc!=Object.class&&vc!=v.getClass())
    					vc=Object.class;
    			}
    		}
    	}
    	else if(t==HashMap.class){
    		HashMap dict=(HashMap)data;
    		for(Object itm:dict.entrySet()){
    			Entry entry=(Entry)itm;
    			Object k=entry.getKey();
    			Object v=entry.getValue();
    			list_keys.add(k);
    			list_values.add(v);
    			if(kc==null){
    				kc=k.getClass();
    			}
    			else{
    				if(kc!=Object.class&&kc!=k.getClass())
    					kc=Object.class;
    			}
    			if(vc==null&&v!=null){
    				vc=v.getClass();
    			}
    			else{
    				if(vc!=Object.class&&vc!=v.getClass())
    					vc=Object.class;
    			}
    		}
    	}
    	Object keys=Array.newInstance(kc, list_keys.size());
    	Object values=Array.newInstance(vc, list_values.size());
    	for(int i=0;i<list_keys.size();i++){
    	     Array.set(keys, i, list_keys.get(i));
    	     Array.set(values,i,list_values.get(i));
    	}
    	this.metadk = MetaFactory.getMetaData(keys.getClass());
        metadk.init2(this.getCsonKeys(), this.getCsonStructs());
        metadk.setData(keys);
        this.metadv = MetaFactory.getMetaData(values.getClass());
        metadv.init2(this.getCsonKeys(), this.getCsonStructs());
        metadv.setData(values);
    }
    @Override
    public Object getData(){
    	return this.getData(HashMap.class);
    }
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public Object getData(Class<?> t){
    	Object keys;
    	Object values;
    	/*TypeVariable[] tv = t.getTypeParameters();
    	if(tv.length>0){
    		Class<?> kc=tv[0].getClass();
    		Class<?> vc=tv[1].getClass();
    		keys=this.metadk.getData(kc);
    		values=this.metadv.getData(vc);
    	}
    	else{*/
    	keys=this.metadk.getData();
    	values=this.metadv.getData();
    	//}
    	int size=Array.getLength(keys);
    	if(t==Hashtable.class){
    		Hashtable ht=null;
			try {
				ht = (Hashtable)t.newInstance();
				for(int i=0;i<size;i++){
	    			ht.put(Array.get(keys, i), Array.get(values, i));
	    		}
			} catch (Exception e) {
			}
    		return ht;
    	}
    	else if(t==HashMap.class){
    		HashMap ht=null;
			try {
				ht = (HashMap)t.newInstance();
				for(int i=0;i<size;i++){
	    			ht.put(Array.get(keys, i), Array.get(values, i));
	    		}
			} catch (Exception e) {
			}
			return ht;
    	}
    	else{
    		throw new CSONNotSupportedException(t);
    	}
    }
	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		int len = 0;
        this.metadk = MetaFactory.getMetaData(Object[].class);
        this.metadk.init2(this.getCsonKeys(), this.getCsonStructs());
        len+=this.metadk.read(carrier,offset+len);
        this.metadv = MetaFactory.getMetaData(Object[].class);
        this.metadv.init2(this.getCsonKeys(), this.getCsonStructs());
        len+=this.metadv.read(carrier, offset + len);
        return len;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		this.metadk.write(stream);
        this.metadv.write(stream);
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		this.metadk = MetaFactory.getMetaData(Object[].class);
        this.metadk.init2(this.getCsonKeys(), this.getCsonStructs());
        this.metadk.read(stream);
        this.metadv = MetaFactory.getMetaData(Object[].class);
        this.metadv.init2(this.getCsonKeys(), this.getCsonStructs());
        this.metadv.read(stream);
	}

}
