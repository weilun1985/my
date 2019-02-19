package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.Utils;

@CsonRegister(isFixed = { true }, metaCodes = { 19 }, supports = { "java.util.Date,java.sql.Date,java.sql.Timestamp,java.util.Calendar" })
public class DateTimeData extends MetaBase {
	@Override
	public void setData(Object data)
    {
		if(data instanceof java.util.Date)
		{
			java.util.Date t = (java.util.Date)data;
			this._data =t.getTime();
		}
		else if(data instanceof java.sql.Date)
		{
			java.sql.Date t=(java.sql.Date)data;
			this._data =t.getTime();
		}
		else if(data instanceof java.sql.Timestamp){
			java.sql.Timestamp t=(java.sql.Timestamp)data;
			this._data=t.getTime();
		}
		else if(data instanceof java.sql.Time){
			java.sql.Time t=(java.sql.Time)data;
			this._data=t.getTime();
		}
		else if(data instanceof java.util.Calendar){
			java.util.Calendar t=(java.util.Calendar)data;
			this._data=t.getTimeInMillis();
		}
    }
	@Override
    public Object getData()
    {
    	return new java.util.Date((Long)this._data);
        //return this._data;
    }
	@Override
	public Object getData(Class<?> t)
    {
		if(t==java.util.Date.class)
			return new java.util.Date((Long)this._data);
		else if(t== java.sql.Date.class)
			return new java.sql.Date((Long)this._data);
		else if(t==java.sql.Timestamp.class)
			return new java.sql.Timestamp((Long)this._data);
		else if(t==java.sql.Time.class)
			return new java.sql.Time((Long)this._data);
		else if(t==java.util.Calendar.class){
			java.util.Calendar c=java.util.Calendar.getInstance();
			c.setTimeInMillis((Long)this._data);
			return c;
		}
    	return super.getData(t);
    }
	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		long tm = Utils.toInt64(carrier, offset);
        /*Date dt = new Date();
        dt.setTime(tm);
        this._data = dt;*/
		this._data=tm;
        return 8;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		/*Date t = (Date)this._data;
        long tm =t.getTime();*/
		long tm=(Long)this._data;
        Utils.writeBinary(stream, tm);
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		long tm = Utils.toInt64(stream);
        /*Date dt = new Date();
        dt.setTime(tm);
        this._data = dt;*/
		this._data=tm;
	}

}
