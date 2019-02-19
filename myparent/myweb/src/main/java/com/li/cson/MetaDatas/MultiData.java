package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.li.cson.CSONNotSupportedException;
import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.MetaFactory;
import com.li.cson.MetaInfo;
import com.li.cson.Utils;

@CsonRegister(isFixed = { false }, metaCodes = { 13 }, supports = {})
public class MultiData extends MetaBase {
	static{
		MetaInfo mi=new MetaInfo();
		mi.Code=13;
		mi.IsFixed=false;
		mi.MetaType=MultiData.class;
		_mi=mi;
	}
	private static MetaInfo _mi;
	private boolean itemfixed;          //是否是定长
    private ArrayList<MetaBase> metas;
    @Override
    public Object getData()
    {
        if (this.itemfixed)
        {
            if(this.metas.size()==0)
                return new Object[0];
            Class<?> eleType=MetaFactory.tryFindType(this.metas.get(0).getMetaCode());
            if (eleType!=null)
            {
                Object arry =Array.newInstance(eleType, this.metas.size());
                for (int i = 0; i < this.metas.size(); i++)
                {
                    Object obj=this.metas.get(i).getData();
                    Array.set(arry, i, obj);
                }
                return arry;
            }
        }
        Object[] list = new Object[this.metas.size()];
        for (int i = 0; i < this.metas.size(); i++)
        {
            list[i] = this.metas.get(i).getData();
        }
        return list;
        
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public Object getData(Class<?> t)
    {
        if (t.isArray()||List.class.isAssignableFrom(t))
        {
            //List list;
            Class<?> eleType=null;
            if (t.isArray())
            {
                eleType = Utils.getArrayElementClass(t);
                Object arry =Array.newInstance(eleType, this.metas.size());
                for (int i = 0; i < this.metas.size(); i++)
                {
                	Object obj=(eleType == null||eleType==Object.class)?this.metas.get(i).getData():this.metas.get(i).getData(eleType);
                    Array.set(arry, i, obj);
                }
                return arry;
            }
            else
            {
            	if(t== List.class)
            		t= ArrayList.class;
            	List list;
				try {
					list = (List)t.newInstance();
					Method method = t.getMethod("get",new Class<?>[]{int.class});
	            	eleType= method.getReturnType(); 
	                for (int i = 0; i < this.metas.size(); i++)
	                {
	                	Object obj=(eleType == null||eleType==Object.class) ?this.metas.get(i).getData():this.metas.get(i).getData(eleType);
	                    list.add(obj);
	                }
	                return list;
				}catch(Exception ex){
					if(ex instanceof RuntimeException)
						throw (RuntimeException)ex;
					else
						throw new RuntimeException(ex);
				}
            }
        }
        throw new CSONNotSupportedException(t);
    }
    @SuppressWarnings("rawtypes")
	@Override
    public void setData(Object data)
    {
        metas = new ArrayList<MetaBase>();
        Class<?> arrytp = data.getClass();
        Class<?> eleType = null;
        int length;
        if(data instanceof List){
        	length=((List)data).size();
        	eleType=Object.class;
        }else if(arrytp.isArray()){
        	length=Array.getLength(data);
        	eleType = Utils.getArrayElementClass(arrytp);
        }else{
        	throw new RuntimeException("unsupport class as multi:"+arrytp);
        }
        if (length== 0)
            return;
        if (eleType != null && eleType != Object.class&&(itemfixed=MetaFactory.isFixed(eleType)))
        {
            super.setData(data);
            MetaBase metaData = MetaFactory.getMetaData(eleType);
            metaData.init2(this.getCsonKeys(), this.getCsonStructs());
            metas.add(metaData);
        }
        else
        {
            for(int i=0;i<length;i++)
            {
            	Object obj=arrytp.isArray()?Array.get(data, i):((List)data).get(i);
                Class<?> eletype = null;
                if (obj != null)
                    eletype = obj.getClass();
                MetaBase metaData = MetaFactory.getMetaData(eletype);
                metaData.init2(this.getCsonKeys(), this.getCsonStructs());
                metaData.setData(obj);
                metas.add(metaData);
            }
        }
    }
	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		int len = 0;
        //读取总长度
        int count = Utils.toInt32(carrier, offset);
        this.metas = new ArrayList<MetaBase>(count);
        len += 4;
        itemfixed = carrier[offset + len] == 1;
        len++;
        if (itemfixed)
        {
            //读取元素类型
            byte metaCode = carrier[offset + len];
            len++;
            //读取数据
            for (int i = 0; i < count; i++)
            {
                MetaBase metaData = MetaFactory.getMetaData(metaCode);
                metaData.init2(this.getCsonKeys(), this.getCsonStructs());
                len += metaData.read(carrier, offset + len);
                this.metas.add(metaData);
            }
        }
        else
        {
            for (int i = 0; i < count; i++)
            {
                //读取meta类型
                byte metacode = carrier[offset + len++];
                MetaBase metaData = MetaFactory.getMetaData(metacode);
                metaData.init2(this.getCsonKeys(), this.getCsonStructs());
                len += metaData.read(carrier, offset + len);
                this.metas.add(metaData);
            }
        }
        return len;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		//如果元素是定长类型的
        if (itemfixed)
        {
            int length=Array.getLength(this._data);
            //写入总长度
            Utils.writeBinary(stream, length);
            if (length == 0)
                return;
            //写入定长类型
            stream.write(1);
            //写入元素类型
            stream.write(this.metas.get(0).getMetaCode());
            //写入数据
            for(int i=0;i<length;i++)
            {
                this.metas.get(0).setData(Array.get(this._data, i));
                this.metas.get(0).write(stream);
            }
        }
        else
        {
            //写入总长度
            Utils.writeBinary(stream, metas.size());
            //写入同构信息
            stream.write(0);
            //写入数据
            for(MetaBase itm:metas)
            {
                stream.write(itm.getMetaCode());
                itm.write(stream);
            }
        }
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		//读取总长度
        int count = Utils.toInt32(stream);
        this.metas = new ArrayList<MetaBase>(count);
        itemfixed = stream.read() == 1;
        if (itemfixed)
        {
            //读取元素类型
            byte metaCode = (byte)stream.read();
            for (int i = 0; i < count; i++)
            {
                //读取数据
                MetaBase metaData = MetaFactory.getMetaData(metaCode);
                metaData.init2(this.getCsonKeys(), this.getCsonStructs());
                metaData.read(stream);
                this.metas.add(metaData);
            }
        }
        else
        {
            for (int i = 0; i < count; i++)
            {
                //读取meta类型
                byte metacode = (byte)stream.read();
                MetaBase metaData = MetaFactory.getMetaData(metacode);
                metaData.init2(this.getCsonKeys(), this.getCsonStructs());
                metaData.read(stream);
                this.metas.add(metaData);
            }
        }
	}
	public static MetaInfo supported(Class<?> t)
    {
		if(t.isArray()||List.class.isAssignableFrom(t)){
			return _mi;
		}
		/*for(Class<?> inte:t.getInterfaces()){
			if(inte.equals(List.class)){
				return _mi;
			}
		}*/
        return null;
    }
}
