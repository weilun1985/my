package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;

@CsonRegister(isFixed = { true }, metaCodes = { 23 }, supports = {"java.net.InetAddress"})
public class IPAddressData extends MetaBase {

	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		int len = 0;
        int ver = carrier[offset + len++];
        byte[] binary = new byte[ver];
        for (int i = 0; i < binary.length; i++)
        {
            binary[i] = carrier[offset + len++];
        }
        InetAddress ip;
		try {
			ip = InetAddress.getByAddress(binary);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
        this._data = ip;
        return len;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		InetAddress ip = (InetAddress)this._data;
        byte[] binary=ip.getAddress();
        stream.write((byte)binary.length);
        stream.write(binary,0,binary.length);
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		int ver = stream.read();
        byte[] binary = new byte[ver];
        stream.read(binary, 0, binary.length);
        InetAddress ip = InetAddress.getByAddress(binary);
        this._data = ip;
	}

}
