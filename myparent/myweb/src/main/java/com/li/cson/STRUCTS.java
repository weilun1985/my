package com.li.cson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class STRUCTS {
	public class STRUCTITEM
    {
        // 结构列表中的索引
        public int structIndex;
        
        // 结构中包含的键索引
        public List<Integer> keyIndexs;
        
        //结构体名称
        public String structName;
        public void write(java.io.OutputStream stream, int x) throws IOException
        {
        	int snlen=0;
        	byte[] snbyts=null; 
        	if(this.structName!=null&&this.structName.length()>0){
        		snbyts=this.structName.getBytes(CSON2.StringEncoding);
        		snlen=snbyts.length;
        		if(snlen>Byte.MAX_VALUE)
        			throw new CSONException(String.format("无法进行CSON序列化，结构体%s名称过长,结构体名称不允许超过%d个字符",this.structName,Byte.MAX_VALUE/2));
        	}
        	stream.write(snlen);
        	if(snlen>0){
        		stream.write(snbyts,0,snbyts.length);
        	}
        		
            for (int i = 0, j = 0; i < this.keyIndexs.size(); i++, j += x)
            {
                if (x == 1)
                    stream.write(this.keyIndexs.get(i));
                else
                {
                    byte[] keyibyts = Utils.getBinary((short)(int)this.keyIndexs.get(i));
                    stream.write(keyibyts, 0, keyibyts.length);
                }
            }
            //写入终结符
            stream.write(0);
            if (x == 2)
                stream.write(0);
        }
        public int read(byte[] carrier, int index, int x)
        {
            this.keyIndexs = new ArrayList<Integer>();
            int bytelen = 0;
            int snlen=carrier[index+bytelen++];
            if(snlen>0){
	            byte[] snbyts=new byte[snlen];
	            for(int i=0;i<snbyts.length;i++){
	            	snbyts[i]=carrier[index+bytelen++];
	            }
	            try {
					this.structName=new String(snbyts,CSON2.StringEncoding);
				} catch (UnsupportedEncodingException e) {
				}
            }
            for (int i = index+bytelen; ; i+=x)
            {
                int keyi;
                if (x == 1)
                    keyi = carrier[i];
                else
                    keyi = Utils.toInt16(carrier, i);
                bytelen += x;
                //读取到终结符，跳出
                if (keyi == 0)
                    break;
                this.keyIndexs.add(keyi);
            }
            return bytelen;
        }
        public void read(java.io.InputStream stream, int x) throws IOException
        {
            this.keyIndexs = new ArrayList<Integer>();
            int snlen=stream.read();
            if(snlen>0){
	            byte[] snbyts=new byte[snlen];
	            stream.read(snbyts);
	            try {
					this.structName=new String(snbyts,CSON2.StringEncoding);
				} catch (UnsupportedEncodingException e) {
				}
            }
            for (int i = 0; ; i += x)
            {
                int keyi;
                if (x == 1)
                    keyi = stream.read();
                else
                    keyi = Utils.toInt16(stream);
                //读取到终结符，跳出
                if (keyi == 0)
                    break;
                this.keyIndexs.add(keyi);
            }
        }
    }
	 private List<STRUCTITEM> _items;
     private Dictionary<String, STRUCTITEM> _itemsDict;
    /* private String getId(List<Integer> keyis)
     {
         StringBuilder idbl = new StringBuilder(keyis.size() * 2 - 1);
         for (int i = 0; i < keyis.size(); i++)
         {
             if (i > 0)
                 idbl.append("#");
             idbl.append(keyis.get(i));
         }
         String id = idbl.toString();
         return id;
     }*/
     public int count(){
    	 return this._items.size();
     }
     public int addStruct(String id,List<Integer> keyis)
     {
         if (keyis == null || keyis.size() == 0)
             return 0;
         if (_items == null)
         {
             _items = new ArrayList<STRUCTITEM>();
         }
         //如果结构体数量已经达到最大值，抛出异常
         if (_items.size() == Byte.MAX_VALUE)
             throw new CSONException(String.format("类型种类已达到CSON格式数据最多能支持的%d种类类型或者结构体！",Byte.MAX_VALUE));
         if (_itemsDict == null)
         {
             if (_items.size() > 0)
             {
                 _itemsDict = new Hashtable<String, STRUCTITEM>(_items.size());
                 for(STRUCTITEM itm:_items)
                 {
                     //_itemsDict.put(getId(itm.keyIndexs), itm);
                     _itemsDict.put(id, itm);
                 }
             }
             else
             {
                 _itemsDict = new Hashtable<String, STRUCTITEM>();
             }
         }
         //String id = this.getId(keyis);
         STRUCTITEM item=_itemsDict.get(id);
         if (item==null)
         {
             item = new STRUCTITEM();
             item.keyIndexs = keyis;
             item.structName=id;
             _items.add(item);
             item.structIndex = _items.size();
             _itemsDict.put(id, item);
         }
         return item.structIndex;
     }
    /* public List<Integer> getStructKeyIndexs(int structIndex)
     {
         return _items.get(structIndex - 1).keyIndexs;
     }*/
     public STRUCTITEM getStruct(int structIndex){
    	 return _items.get(structIndex-1);
     }
     public int read(byte[] carrier, int index,int x)
     {
         int bytelen=0;
         //读取结构体数量
         int count = carrier[index];
         bytelen+=1;
         //初始化容器
         this._items = new ArrayList<STRUCTITEM>(count);
         this._itemsDict = new Hashtable<String, STRUCTITEM>(count);
         //循环读取包含的结构体数据
         for (int i = 0; i < count; i++)
         {
        	 STRUCTITEM item = new STRUCTITEM();
             bytelen+=item.read(carrier, index + bytelen, x);
             this._items.add(item);
             item.structIndex = this._items.size();
         }
         return bytelen;
     }
     public void read(java.io.InputStream stream, int x) throws IOException
     {
         //读取结构体数量
         int count = stream.read();
         //初始化容器
         this._items = new ArrayList<STRUCTITEM>(count);
         this._itemsDict = new Hashtable<String, STRUCTITEM>(count);
         //循环读取包含的结构体数据
         for (int i = 0; i < count; i++)
         {
        	 STRUCTITEM item = new STRUCTITEM();
             item.read(stream, x);
             this._items.add(item);
             item.structIndex = this._items.size();
         }
     }
     public void write(java.io.OutputStream stream, int x) throws IOException
     {
         int count = this._items == null ? 0 : this._items.size();
         //写入结构体数量
         stream.write(count);
         if (count == 0)
             return;
         //循环写入结构体
         for (STRUCTITEM itm:this._items)
         {
             itm.write(stream, x);
         }
     }
}
