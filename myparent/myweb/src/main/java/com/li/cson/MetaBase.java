package com.li.cson;

import java.io.IOException;

public abstract class MetaBase {
	private byte _metaCode;
    private boolean _isFixed;
    private KEYS _csonkeys;
    private STRUCTS _csonStructs;
    protected Object _data;
    
    protected KEYS getCsonKeys()
    {
        return _csonkeys;
    }
    protected STRUCTS getCsonStructs()
    {
       return _csonStructs;
        
    }
    public byte getMetaCode()
    {
       return _metaCode;
        
    }
    public boolean isFixed()
    {
        return _isFixed;
    }
    public void setData(Object data)
    {
        this._data = data;
    }
    public Object getData()
    {
        return this._data;
    }
    public Object getData(Class<?> t)
    {
    	return Utils.ConvertType(this._data, t);
       /* if (this._data == null)
            return Utils.DefaultValue(t);
        return t.cast(this._data);*/
    }
    public void init1(byte meta,boolean isFixed)
    {
        this._metaCode = meta;
        this._isFixed = isFixed;
    }
    public void init2(KEYS keys, STRUCTS structs)
    {
        this._csonkeys = keys;
        this._csonStructs = structs;
    }
    public abstract int read(byte[] carrier, int offset);
    public abstract void write(java.io.OutputStream stream) throws IOException;
    public abstract void read(java.io.InputStream stream) throws IOException;
}
