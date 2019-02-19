package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.Utils;

@CsonRegister( metaCodes = { 1 }, isFixed = { false },supports = { "[B" })
public class BYTESData extends MetaBase {

	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		int len = 0;
        int alen = Utils.toInt32(carrier, offset);
        len += 4;
        byte[] byts = new byte[alen];
        if (alen > 0)
        {
            System.arraycopy(carrier, offset + len, byts, 0, alen);
            len += alen;
        }
        this._data = byts;
        return len;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		byte[] byts = (byte[])this._data;
        //写入长度
        Utils.writeBinary(stream, byts.length);
        //写入字节序列
        if (byts.length > 0)
        {
            stream.write(byts, 0, byts.length);
        }
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		int alen = Utils.toInt32(stream);
        byte[] byts = new byte[alen];
        if (alen > 0)
        {
            stream.read(byts, 0, alen);
        }
        this._data = byts;
	}

}
