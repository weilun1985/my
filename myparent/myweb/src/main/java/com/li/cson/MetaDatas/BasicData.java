package com.li.cson.MetaDatas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.li.cson.CsonRegister;
import com.li.cson.MetaBase;
import com.li.cson.MetaInfo;
import com.li.cson.Utils;

@CsonRegister(
	metaCodes = { 2,3,4,5,6,7,8,9,10,11,12,22,25 },
	isFixed={true,true,true,true,true,true,true,true,true,true,true,true,true},
	supports={"java.lang.Short"
			,"java.lang.Integer"
			,"java.lang.Long"
			,""
			,""
			,""
			,"java.lang.Byte"
			,""
			,"java.lang.Float"
			,"java.lang.Double"
			,"java.lang.Character"
			,"java.lang.Boolean"
			,""
			}
)
public class BasicData extends MetaBase {

	@Override
	public int read(byte[] carrier, int offset) {
		// TODO Auto-generated method stub
		int rlen = 0;
        Object obj = null;
        //byte[] buffer;
        switch (this.getMetaCode())
        {
            case 2:
            case 5:
                obj = Utils.toInt16(carrier, offset);
                rlen += 2;
                break;
            case 3:
            case 6:
                obj = Utils.toInt32(carrier, offset);
                rlen +=4;
                break;
            case 4:
            case 7:
                obj = Utils.toInt64(carrier, offset);
                rlen += 8;
                break;
            /*case 5:
            	buffer=new byte[4];
            	System.arraycopy(carrier, offset, buffer, 0, 2);
            	obj =Utils.toInt32(buffer,0);
                rlen += 2;
                break;*/
            /*case 6:
            	buffer=new byte[8];
            	System.arraycopy(carrier, offset, buffer, 0, 4);
            	obj =Utils.toInt64(buffer,0);
                rlen += 4;
                break;*/
           /* case 7:
            	buffer = new byte[8];
            	System.arraycopy(carrier, offset, buffer, 0, 8);
                obj = new BigInteger(buffer);
                rlen+=8;
                break;*/
            case 8:
            case 9:
                obj = carrier[offset];
                rlen++;
                break;
           /* case 9:
            	byte tmp = carrier[offset];
                if(((tmp>>>7)&1)==1){
                	obj=~(tmp-1);
                }
                else{
                	obj=(int)tmp;
                }
                rlen++;
                break;*/
            case 10:
            	int ibits=Utils.toInt32(carrier,offset);
                obj = Float.intBitsToFloat(ibits);
                rlen += 4;
                break;
            case 11:
            case 25:
            	long lbits=Utils.toInt64(carrier,offset);
                obj = Double.longBitsToDouble(lbits);
                rlen += 8;
                break;
            case 12:
            	obj =Utils.toChar(carrier,offset);
                rlen += 2;
                break;
            case 22:
                obj =carrier[offset]>0;
                rlen++;
                break;
        }
        this._data = obj;
        return rlen;
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		byte[] binary=null;
		switch(this.getMetaCode()){
			case 2:
				binary=Utils.getBinary((Short)this._data);
				break;
			case 3:
				binary=Utils.getBinary((Integer)this._data);
				break;
			case 4:
				binary=Utils.getBinary((Long)this._data);
				break;
			case 8:
				stream.write((Byte)this._data);
				break;
			case 10:
				binary=Utils.getBinary(Float.floatToIntBits((Float)this._data));
				break;
			case 11:
				binary=Utils.getBinary(Double.doubleToLongBits((Double)this._data));
				break;
			case 12:
				char chr=(Character)this._data;
				binary=Utils.getBinary(chr);
				break;
			case 22:
				byte b=(byte)((Boolean)this._data?1:0);
				stream.write(b);
				break;
		}
		if(binary!=null){
			stream.write(binary,0,binary.length);
		}
	}

	@Override
	public void read(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		Object obj = null;
        //byte[] buffer;
        switch (this.getMetaCode())
        {
            case 2:
            case 5:
                obj = Utils.toInt16(stream);
                break;
            case 3:
            case 6:
                obj =Utils.toInt32(stream);
                break;
            case 4:
            case 7:
                obj =Utils.toInt64(stream);
                break;
           /* case 5:
            	buffer=new byte[4];
            	stream.read(buffer,0,2);
            	obj =Utils.toInt32(buffer,0);
                break;
            case 6:
            	buffer=new byte[8];
            	stream.read(buffer,0,4);
            	obj =Utils.toInt64(buffer,0);
                break;
            case 7:
                buffer = new byte[8];
                stream.read(buffer, 0, buffer.length);
                obj = new BigInteger(buffer);
                break;*/
            case 8:
            case 9:
                obj = (byte)stream.read();
                break;
           /* case 9:
                byte tmp = (byte)stream.read();
                if(((tmp>>>7)&1)==1){
                	obj=~(tmp-1);
                }
                else{
                	obj=(int)tmp;
                }
                break;*/
            case 10:
                int ibits=Utils.toInt32(stream);
                obj = Float.intBitsToFloat(ibits);
                break;
            case 11:
            case 25:
                long lbits=Utils.toInt64(stream);
                obj = Double.longBitsToDouble(lbits);
                break;
            case 12:
                obj =Utils.toChar(stream);
                break;
            case 22:
                obj =stream.read()>0;
                break;
        }
        this._data = obj;
	}
	
	public static MetaInfo M_basic(Class<?> b){
		MetaInfo m=new MetaInfo();
		m.IsFixed=true;
		m.MetaType=BasicData.class;
		if(b==int.class){
			m.Code=3;
		}
		else if(b==short.class){
			m.Code=2;
		}
		else if(b==long.class){
			m.Code=4;
		}
		else if(b==byte.class){
			m.Code=8;
		}
		else if(b==float.class){
			m.Code=10;
		}
		else if(b==double.class){
			m.Code=11;
		}
		else if(b==char.class){
			m.Code=12;
		}
		else if(b==boolean.class){
			m.Code=22;
		}
		return m;
	}
	
}
