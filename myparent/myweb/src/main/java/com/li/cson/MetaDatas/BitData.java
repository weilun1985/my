package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.Utils;

@CsonRegister(metaCodes = { 21 }, isFixed = { false }, supports = { "[Z" })
public class BitData extends MetaBase {

	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		int len = 0;
        boolean[] bools;
        //读取数组长度
        int count = Utils.toInt32(carrier,offset);
        len += 4;
        //计算需要的字节数量
        int bitcout = count / 8;
        int res = count % 8;
        if (res > 0)
            bitcout += 1;
        if (count == 0)
            bools = new boolean[0];
        else
            bools = new boolean[count];
        for (int i = 0; i < bitcout; i++)
        {
            byte byt = carrier[offset+len++];
            if (byt == 0)
                continue;
            int a = i << 3;
            int b = res == 0 ? 8 : ((i == bitcout - 1) ? res : 8);
            for (int j = 0; j < b; j++)
            {
                bools[a + j] = (byt >> j & 1) == 1;
            }
        }
        this._data = bools;
        return len;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		boolean[] bools = (boolean[])this._data;
        //计算需要的字节数量
        int bitcout = bools.length / 8;
        int res=bools.length % 8;
        if (res > 0)
            bitcout += 1;
        //写入数组长度
        Utils.writeBinary(stream, bools.length);
        if (bitcout == 0)
            return;
        for (int i = 0; i < bitcout; i++)
        {
            int a=i<<3;
            int b = res == 0 ? 8 : ((i == bitcout - 1) ? res : 8);
            byte byt = 0;
            for(int j=0;j<b;j++)
            {
                if (bools[a + j])
                {
                    byt |= (byte)(1 << j);
                }
            }
            stream.write(byt);
        }
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		boolean[] bools;
        //读取数组长度
        int count = Utils.toInt32(stream);
        //计算需要的字节数量
        int bitcout = count / 8;
        int res = count % 8;
        if (res > 0)
            bitcout += 1;
        if (count == 0)
            bools = new boolean[0];
        else
            bools = new boolean[count];
        for (int i = 0; i < bitcout; i++)
        {
            byte byt = (byte)stream.read();
            if (byt == 0)
                continue;
            int a = i << 3;
            int b = res == 0 ? 8 : ((i == bitcout - 1) ? res : 8);
            for (int j = 0; j < b; j++)
            {
                bools[a + j] = (byt >> j & 1) == 1;
            }
        }
        this._data = bools;
	}

}
