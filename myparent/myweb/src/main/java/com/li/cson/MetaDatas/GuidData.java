package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.Utils;

@CsonRegister(isFixed = { true }, metaCodes = { 16 }, supports = { "java.util.UUID" })
public class GuidData extends MetaBase {

	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		long least=Utils.toInt64(carrier,offset);
		long most=Utils.toInt64(carrier,offset+8);
		UUID uuid=new UUID(least,most);
		this._data=uuid;
		return 16;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		UUID uuid=((UUID)this._data);
		Utils.writeBinary(stream, uuid.getLeastSignificantBits());
        Utils.writeBinary(stream, uuid.getMostSignificantBits());
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		long least=Utils.toInt64(stream);
		long most=Utils.toInt64(stream);
		UUID uuid=new UUID(least,most);
		this._data=uuid;
	}

}
