package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;

@CsonRegister( metaCodes = { 17 }, isFixed = { false },supports = { "java.lang.Object" })
public class ObjectData extends MetaBase {

	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		this._data=new Object();
		return 0;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		this._data=new Object();
	}

}
