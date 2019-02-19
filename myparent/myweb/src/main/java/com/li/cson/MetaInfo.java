package com.li.cson;

public class MetaInfo {
	public byte Code;
    public Class<?> MetaType;
    public boolean IsFixed;
    @Override
    public String toString(){
    	return String.format("%d %s", this.Code,this.MetaType);
    }
}
