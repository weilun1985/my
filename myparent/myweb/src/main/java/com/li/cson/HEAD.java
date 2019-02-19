package com.li.cson;

import java.io.IOException;

class HEAD {
	private boolean _compressed;
    private byte _protoVersion;
    public boolean getCompressed(){
    	return this._compressed;
    }
    public void setCompressed(boolean value){
    	this._compressed=value;
    }
    public byte getProtoVersion(){
    	return this._protoVersion;
    }
    public void setProtoVersion(byte ver){
    	if(ver>127)
            throw new CSONException("协议版本号最大值不能超过127");
        this._protoVersion = ver;
    }
    public int read(byte[] carrier, int offset)
    {
        byte headByte = carrier[offset];
        //读取是否压缩
        this._compressed = headByte<0;
        //读取版本信息
        this._protoVersion = (byte)(headByte & 127);
        return 1;
    }
    public void write(java.io.OutputStream stream) throws IOException
    {
        byte headByte = 0;
        //写入是否压缩的值
        byte b_1 = (byte)(this._compressed ? 1 : 0);
        headByte |= (byte)(b_1 << 7);
        //写入版本信息
        headByte = (byte)(((headByte >> 7) << 7) | this._protoVersion);
        stream.write(headByte);
    }
    public void read(java.io.InputStream stream) throws IOException
    {
        byte headByte =(byte)stream.read();
        //读取是否压缩
        this._compressed = headByte< 0;
        //读取版本信息
        this._protoVersion = (byte)(headByte & 127);
    }
}
