package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.MetaFactory;
import com.li.cson.Utils;

@CsonRegister(isFixed = { false }, metaCodes = { 24 }, supports = {"java.net.InetSocketAddress"})
public class IPEndPointData extends MetaBase {

	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		int len = 0;
        MetaBase ipmd = MetaFactory.getMetaData(InetAddress.class);
        len += ipmd.read(carrier, offset);
        InetAddress ipaddress = (InetAddress)ipmd.getData();
        int port = Utils.toInt32(carrier,offset+len);
        len += 4;
        this._data = new InetSocketAddress(ipaddress, port);
        return len;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		InetSocketAddress ipep=(InetSocketAddress)this._data;
        MetaBase ipmd = MetaFactory.getMetaData(InetAddress.class);
        ipmd.setData(ipep.getAddress());
        ipmd.write(stream);
        Utils.writeBinary(stream, ipep.getPort());
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		MetaBase ipmd = MetaFactory.getMetaData(InetAddress.class);
        ipmd.read(stream);
        InetAddress ipaddress= (InetAddress)ipmd.getData();
        int port= Utils.toInt32(stream);
        this._data = new InetSocketAddress(ipaddress, port);
	}

}
