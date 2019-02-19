package com.li.cson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class KEYS {
	public class KEYITEM
    {
        /// <summary>
        /// 键标
        /// </summary>
        public int index;
        /// <summary>
        /// 键类型
        /// </summary>
        public byte meta;
        /// <summary>
        /// 键名称
        /// </summary>
        public String name;
        public void write(java.io.OutputStream stream) throws IOException
        {
            //写入meta类型
            stream.write(this.meta);
            byte[] namebyts =this.name.getBytes(CSON2.StringEncoding);
            if (namebyts.length > 255)
                throw new CSONException(String.format("CSON不支持长度超过%d的键名%s", 255, this.name));
            //写入名称长度
            stream.write((byte)namebyts.length); 
            //写入名称
            stream.write(namebyts, 0, namebyts.length);
        }
        public void read(java.io.InputStream stream) throws IOException
        {
            //读取meta类型
            this.meta = (byte)stream.read();
            //读取名称长度
            int namebytl = stream.read();
            byte[] binary=new byte[namebytl];
            stream.read(binary, 0, namebytl);
            //读取名称
            this.name =new String(binary,CSON2.StringEncoding);
        }
        /// <summary>
        /// 从字节流中读取
        /// </summary>
        /// <param name="carrier"></param>
        /// <param name="offset"></param>
        /// <returns></returns>
        public int read(byte[] carrier, int offset)
        {
            int bytelen = 0;
            //读取meta类型
            this.meta = carrier[offset];
            bytelen++;
            //读取名称长度
            int namebytl = carrier[offset + bytelen];
            bytelen++;
            byte[] binary=new byte[namebytl];
            for(int i=0;i<binary.length;i++){
            	binary[i]=carrier[offset+bytelen++];
            }
            //读取名称
            try {
				this.name = new String(binary,CSON2.StringEncoding);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
            //bytelen += namebytl;
            return bytelen;
        }
        @Override
        public String toString()
        {
            return String.format("%d %s@%d", this.index, this.name, this.meta);
        }
    }
	private Hashtable<String, KEYITEM> _keyiDict;
	private List<KEYITEM> _keyiList;
	public int getCount(){
		if (this._keyiList == null)
            return 0;
        return _keyiList.size();
	}
	public int getKeyBit(){
		if (this._keyiList == null || _keyiList.size() < Byte.MAX_VALUE)
            return 1;
        return 2;
	}
	public int addKey(byte meta, String name)
    {
        if (this._keyiList == null)
        {
            this._keyiList = new ArrayList<KEYITEM>();
        }
        if (this._keyiList.size() > 65535)
            throw new CSONException("CSON最多只能支持总共65535个属性");
        if (this._keyiDict == null)
        {
            if (this._keyiList.size()> 0)
            {
                this._keyiDict = new Hashtable<String, KEYITEM>();
                for(int i=0;i<this._keyiList.size();i++){
                	KEYITEM itm =this._keyiList.get(i);
                	this._keyiDict.put(itm.name + "@" + meta, itm);
                }
            }
            else
            {
                this._keyiDict = new Hashtable<String, KEYITEM>();
            }
        }
        String id=name+"@"+meta;
        KEYITEM item=this._keyiDict.get(id);
        if (item==null)
        {
            item = new KEYITEM();
            item.meta = meta;
            item.name = name;
            _keyiDict.put(id, item);
            _keyiList.add(item);
            item.index = _keyiList.size();
        }
        return item.index;
    }
	public KEYITEM getKey(int keyIndex)
    {
        return _keyiList.get(keyIndex-1);
    }
	public KEYITEM getKey(byte meta, String name)
    {
        String id = name + "@" + meta;
        return this._keyiDict.get(id);
    }
	public void write(java.io.OutputStream stream) throws IOException
    {
        //写入键数量
        short c=(short)this.getCount();
        byte[] sbinary=Utils.getBinary(c);
        stream.write(sbinary);
        if (c == 0)
            return;
        //循环写入键
        for(int i=0;i<c;i++){
        	KEYITEM itm=this._keyiList.get(i);
        	itm.write(stream);
        }
    }
	 public int read(byte[] carrier, int offset)
     {
         int bytelen = 0;
         //读取键数量
         short count=Utils.toInt16(carrier, offset);
         this._keyiList = new ArrayList<KEYITEM>(count);
         bytelen += 2;
         //循环读取键信息
         for (int i = 0; i < count; i++)
         {
             KEYITEM itm = new KEYITEM();
             bytelen+=itm.read(carrier, offset + bytelen);
             this._keyiList.add(itm);
             itm.index = this._keyiList.size();
         }
         return bytelen;
     }
	 public void read(java.io.InputStream stream) throws IOException
     {
		 byte[] cb=new byte[2];
		 cb[0]=(byte)stream.read();
		 cb[1]=(byte)stream.read();
         short count =Utils.toInt16(cb,0);
         this._keyiList = new ArrayList<KEYITEM>(count);
         //循环读取键信息
         for (int i = 0; i < count; i++)
         {
             KEYITEM itm = new KEYITEM();
             itm.read(stream);
             this._keyiList.add(itm);
             itm.index = this._keyiList.size();
         }
     }
}
