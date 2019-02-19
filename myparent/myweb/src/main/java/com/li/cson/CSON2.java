package com.li.cson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ws.commons.util.*;


//import eagle.core.Base64;

//import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
//import com.sun.org.apache.xml.internal.security.utils.Base64;

@SuppressWarnings("restriction")
public class CSON2 {
	public CSON2(){
        _header = new HEAD();
        _keys = new KEYS();
        _structs = new STRUCTS();
        metadatas = new ArrayList<MetaBase>();
        _header.setProtoVersion(PROTOVERSION);
    }
	@SuppressWarnings("rawtypes")
	private static ConcurrentHashMap<String, Class> _structsMap
									=new ConcurrentHashMap<String, Class>();
	public static final String StringEncoding="utf-8";
	public static final byte PROTOVERSION=3;
    private HEAD _header;
    private KEYS _keys;
    private STRUCTS _structs;
    private ArrayList<MetaBase> metadatas;
    private boolean _compressed;
    private static final int _COMPRESSLEVEL = 3;
    
    public boolean getCompressed(){
        return this._compressed;
    }
    public void setCompressed(boolean compress){
        this._compressed=compress;
    }
    public int counts()
    {
        if (this.metadatas == null)
            return 0;
        return metadatas.size();
    }
    public Object getData(int i)
    {
        return metadatas.get(i).getData();
    }
    public Object getData(int i, Class<?> t)
    {
        return metadatas.get(i).getData(t);
    }
    public void append(Object obj)
    {
        //获取类型
        Class<?> type=null;
        if(obj!=null)
             type = obj.getClass();
        //获取MetaData
        MetaBase metadata=MetaFactory.getMetaData(type);
        metadata.init2(this._keys, this._structs);
        metadata.setData(obj);
        metadatas.add(metadata);
    }
    public void write(OutputStream stream) throws IOException
    {
        _header.setCompressed(this._compressed);
        
        OutputStream cstream;
        if (_header.getCompressed())
        {
            cstream = new ByteArrayOutputStream();
        }
        else
        {
            cstream = stream;
          //写入头信息
            _header.write(stream);
          
        }
        //写入键信息
        _keys.write(cstream);
        //写入结构信息
        int x = _keys.getKeyBit();
        _structs.write(cstream, x);
        //写入内容数量
        Utils.writeBinary(cstream, this.metadatas.size());
        //写入内容信息
        for(MetaBase metadata:this.metadatas)
        {
            //写入内容类型
            cstream.write(metadata.getMetaCode());
            //写入内容值
            metadata.write(cstream);
        }
        if (_header.getCompressed())
        {
        	ByteArrayOutputStream cstream2=(ByteArrayOutputStream)cstream;
        	if (cstream2.size() < 1024)
            {
                this._compressed = false;
                _header.setCompressed(false);
                _header.write(stream);
                cstream2.writeTo(stream);
            }
        	else
        	{
	        	byte[] binary0=cstream2.toByteArray();
	            byte[] binary=QuickLZ.compress(binary0, _COMPRESSLEVEL);
	            _header.write(stream);
	            stream.write(binary, 0, binary.length);
        	}
        }
    }
    private int readContent(int coffset, byte[] ccarrier)
    {
        int cbytelen = 0;
        //读取键信息
        cbytelen += _keys.read(ccarrier, coffset);
        //读取结构信息
        int x = _keys.getKeyBit();
        cbytelen += _structs.read(ccarrier, coffset + cbytelen, x);
        //读取内容类型数量
        int ccount = Utils.toInt32(ccarrier, coffset + cbytelen);
        cbytelen += 4;
        //读入内容
        for (int i = 0; i < ccount; i++)
        {
            //读取内容meta类型
            byte metaCode = ccarrier[coffset + cbytelen];
            cbytelen++;
            //获取MetaData
            MetaBase metadata = MetaFactory.getMetaData(metaCode);
            metadata.init2(this._keys, this._structs);
            //读取内容值
            cbytelen += metadata.read(ccarrier, coffset + cbytelen);
            this.metadatas.add(metadata);
        }
        return cbytelen;
    }
    private void readContent(java.io.InputStream stream) throws IOException
    {
        //读取键信息
        _keys.read(stream);
        //读取结构信息
        int x = _keys.getKeyBit();
        _structs.read(stream, x);
        //读取内容类型数量
        int ccount = Utils.toInt32(stream);
        //读入内容
        for (int i = 0; i < ccount; i++)
        {
            //读取内容meta类型
            byte metaCode = (byte)stream.read();
            //获取MetaData
            MetaBase metadata = MetaFactory.getMetaData(metaCode);
            metadata.init2(this._keys, this._structs);
            //读取内容值
            metadata.read(stream);
            this.metadatas.add(metadata);
        }
    }
    public int read(byte[] carrier, int offset)
    {
        int bytelen = 0;
        int coffset;
        byte[] ccarrier;
        //读取头信息
        bytelen += _header.read(carrier, offset);
        if (_header.getProtoVersion()!=PROTOVERSION)
        {
            throw new CSONVersionNotMatchException(_header.getProtoVersion());
        }
        if (this._compressed = _header.getCompressed())
        {
            coffset = 0;
            byte[] buffer=new byte[carrier.length-bytelen];
            
            System.arraycopy(carrier, bytelen, buffer, 0, buffer.length);
            ccarrier = QuickLZ.decompress(buffer);
        }
        else
        {
            coffset = offset + bytelen;
            ccarrier = carrier;
        }
        int cbytelen = readContent(coffset, ccarrier);
        bytelen += cbytelen;
        return bytelen;
    }
    public void read(java.io.InputStream stream) throws IOException
    {
        //读取头信息
        _header.read(stream);
        if (_header.getProtoVersion()!=PROTOVERSION)
        {
            throw new CSONVersionNotMatchException(_header.getProtoVersion());
        }
        if (this._compressed = _header.getCompressed())
        {
            ArrayList<Byte> list=new ArrayList<Byte>();
            int tmp;
            while((tmp=stream.read())!=-1){
            	list.add((byte)tmp);
            }
            byte[] buffer=new byte[list.size()];
            for(int i=0;i<buffer.length;i++){
            	buffer[i]=list.get(i);
            }
            byte[] ccarrier = QuickLZ.decompress(buffer);
            readContent(0, ccarrier);
        }
        else
        {
            readContent(stream);
        }
        
    }
    public void read(String base64){
    	byte[] buffer;
		//try {
    	try{
    		buffer=Base64.decode(base64);
			//buffer = Base64.decode(base64.toCharArray());
			this.read(buffer,0);
    	}catch(Exception e){
    		throw new RuntimeException(e);
    	}
		//} catch (Base64DecodingException e) {
		//	throw new RuntimeException(e);
		//}
        
    }
    public static CSON2 read0(java.io.InputStream stream) throws IOException{
    	 CSON2 cson = new CSON2();
         cson.read(stream);
         return cson;
    }
    public static CSON2 read0(String base64){
    	CSON2 cson = new CSON2();
        cson.read(base64);
        return cson;
    }
    public static void serialize(Object obj, OutputStream stream,boolean compress) throws IOException
    {
        CSON2 cson = new CSON2();
        cson.setCompressed(compress);
        cson.append(obj);
        cson.write(stream);
    }
    public static void serializeMu(Object[] objs, OutputStream stream,boolean compress) throws IOException
    {
        CSON2 cson = new CSON2();
        cson.setCompressed(compress);
        for(Object obj:objs){
        	cson.append(obj);
        }
        cson.write(stream);
    }
    public static byte[] serialize(Object obj, boolean compress)
    {
    	try
    	{
	        ByteArrayOutputStream ms = new ByteArrayOutputStream();
	        serialize(obj,ms,compress);
	        byte[] byts=ms.toByteArray();
	        ms.close();
	        return byts;
    	}
    	catch(Exception ex){
    		throw new RuntimeException(ex);
    	}
    }
    public static byte[] serializeMu(Object[] objs, boolean compress)
    {
    	try
    	{
	        ByteArrayOutputStream ms = new ByteArrayOutputStream();
	        serializeMu(objs,ms,compress);
	        byte[] byts=ms.toByteArray();
	        ms.close();
	        return byts;
    	}
    	catch(Exception ex){
    		throw new RuntimeException(ex);
    	}
    }
    public static String serializeBase64(Object obj, boolean compress)
    {
        return new String(Base64.encode(serialize(obj,compress)));
    }
    public static String serializeBase64Mu(Object[] obj, boolean compress)
    {
        return new String(Base64.encode(serializeMu(obj,compress)));
    }
    public static Object deserialize(java.io.InputStream stream,Class<?> type) throws IOException
    {
        CSON2 cson = new CSON2();
        cson.read(stream);
        return cson.getData(0, type);
    }
    public static Object deserialize(java.io.InputStream stream) throws IOException
    {
        CSON2 cson = new CSON2();
        cson.read(stream);
        if (cson.counts() == 1)
            return cson.getData(0);
        else
        {
            Object[] rs = new Object[cson.counts()];
            for (int i = 0; i < rs.length; i++)
            {
                rs[i] = cson.getData(i);
            }
            return rs;
        }
    }
    public static Object[] deserialize(java.io.InputStream stream,Class<?>[] types) throws IOException
    {
        CSON2 cson = new CSON2();
        cson.read(stream);
        /*if (cson.counts() == 1)
            return cson.getData(0,types[0]);
        else
        {*/
            Object[] rs = new Object[cson.counts()];
            for (int i = 0; i < rs.length; i++)
            {
                rs[i] = cson.getData(i,types[i]);
            }
            return rs;
        /*}*/
    }
    public static Object deserialize(byte[] binary,int offset, Class<?> type)
    {
        CSON2 cson = new CSON2();
        cson.read(binary,offset);
        return cson.getData(0, type);
    }
    public static Object deserialize(byte[] binary, int offset)
    {
        CSON2 cson = new CSON2();
        cson.read(binary, offset);
        if (cson.counts() == 1)
            return cson.getData(0);
        else
        {
            Object[] rs = new Object[cson.counts()];
            for (int i = 0; i < rs.length; i++)
            {
                rs[i] = cson.getData(i);
            }
            return rs;
        }
    }
    public static Object[] deserialize(byte[] binary,int offset, Class<?>[] types)
    {
        CSON2 cson = new CSON2();
        cson.read(binary,offset);
        /*if (cson.counts() == 1)
            return cson.getData(0, types[0]);
        else
        {*/
            Object[] rs = new Object[cson.counts()];
            for (int i = 0; i < rs.length; i++)
            {
                rs[i] = cson.getData(i, types[i]);
            }
            return rs;
        /*}*/
    }
    
	public static Object deserialize(String base64, Class<?> type)
    {
        CSON2 cson = new CSON2();
        cson.read(base64);
        return cson.getData(0,type);
    }
    
	public static Object deserialize(String base64)
    {
        CSON2 cson = new CSON2();
        cson.read(base64);
        if (cson.counts() == 1)
            return cson.getData(0);
        else
        {
            Object[] rs = new Object[cson.counts()];
            for (int i = 0; i < rs.length; i++)
            {
                rs[i] = cson.getData(i);
            }
            return rs;
        }
    }
 
	public static Object[] deserialize(String base64,Class<?>[] types)
    {
        CSON2 cson = new CSON2();
        cson.read(base64);
        /*if (cson.counts() == 1)
            return cson.getData(0,types[0]);
        else
        {*/
            Object[] rs = new Object[cson.counts()];
            for (int i = 0; i < rs.length; i++)
            {
                rs[i] = cson.getData(i,types[i]);
            }
            return rs;
        /*}*/
    }
    
    @SuppressWarnings("rawtypes")
	public static void mapped(String structName1, Class localType)
    {
        Class tmp;
        tmp=_structsMap.putIfAbsent(structName1, localType);
        if(tmp!=null){
        	if(tmp!=localType)
        		throw new CSONMappedException(structName1, localType.getName());
        }
        /*tmp=_structsMap.putIfAbsent(structName2, structName1);
        if(tmp!=null){
        	if(!structName1.equals(tmp))
        		throw new CSONMappedException(structName2, structName1);
        }*/
    }
    @SuppressWarnings("rawtypes")
	public static Class getMapped(String name)
    {
        return _structsMap.get(name);
    }
}
