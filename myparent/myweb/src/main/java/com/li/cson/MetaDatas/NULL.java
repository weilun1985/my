package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;

@CsonRegister(isFixed = { true }, metaCodes = { 0 }, supports = {})
public class NULL extends MetaBase {

	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub

	}

}
