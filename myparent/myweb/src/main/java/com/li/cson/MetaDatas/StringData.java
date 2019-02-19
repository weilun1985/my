package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.li.cson.CSON2;
import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.Utils;
@CsonRegister( metaCodes = { 14 }, isFixed = { false },supports = { "java.lang.String" })
public class StringData extends MetaBase {

	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		int len = 0;
        int strlen = Utils.toInt32(carrier, offset);
        len += 4;
        byte[] buffer=new byte[strlen];
        for(int i=0;i<buffer.length;i++){
        	buffer[i]=carrier[offset+len++];
        }
        String str=null;
		try {
			str = new String(buffer,CSON2.StringEncoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
        this._data = str;
        return len;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		String str = (String)this._data;
        byte[] buffer = str.getBytes(CSON2.StringEncoding);
        //写入长度
        Utils.writeBinary(stream,buffer.length);
        //写入文本
        stream.write(buffer, 0, buffer.length);
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		 int strlen = Utils.toInt32(stream);
         byte[] buffer = new byte[strlen];
         stream.read(buffer, 0, buffer.length);
         String str = new String(buffer,CSON2.StringEncoding);
         this._data = str;
	}

}
