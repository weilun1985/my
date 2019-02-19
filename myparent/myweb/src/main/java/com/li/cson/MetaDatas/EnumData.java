package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.MetaFactory;
import com.li.cson.MetaInfo;

@CsonRegister(isFixed = { false }, metaCodes = { 18 }, supports = {})
public class EnumData extends MetaBase {
	
	@Override
	public Object getData(Class<?> t)
    {
		try
		{
	        if (t.isEnum())
	        {
	        	int o=(Integer)this._data;
	        	Method getvalues=t.getDeclaredMethod("values", new Class<?>[]{});
	        	getvalues.setAccessible(true);
	            Object values=getvalues.invoke(null, new Object[]{});
	            return Array.get(values, o);
	        }
	        else
	        {
	            return super.getData(t);
	        }
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
    }
	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		int len = 0;
        //读取基础类型
        byte under_meta = carrier[offset];
        len++;
        //读取基础类型值
        MetaBase metaData = MetaFactory.getMetaData(under_meta);
        len+=metaData.read(carrier, offset + len);
        this._data = metaData.getData();
        return len;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		@SuppressWarnings("rawtypes")
		Enum em=(Enum)this._data;
		int o=em.ordinal();
		MetaBase metaData = MetaFactory.getMetaData(Integer.class);
        //写入基础类型metacode
        stream.write(metaData.getMetaCode());
        //写入基础类型值
        metaData.setData(o);
        metaData.write(stream);
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		//读取基础类型
        byte under_meta = (byte)stream.read();
        //读取基础类型值
        MetaBase metaData = MetaFactory.getMetaData(under_meta);
        metaData.read(stream);
        this._data = metaData.getData();
	}
	public static MetaInfo supported(Class<?> t){
		if(!t.isEnum())
			return null;
		MetaInfo minfo= new MetaInfo();
		minfo.Code=18;
		minfo.IsFixed=false;
		return minfo;
	}
}
