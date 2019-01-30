package com.li.myweb;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.Cookie;

//import eagle.web.annotation.Param;

public final class HttpUtility {
	static class HtmlEntities{
		private static String[] _entitiesList;
		private static Dictionary<String, Character> _lookupTable;
		static{
			_entitiesList = new String[] { 
					"\"-quot", "&-amp", "'-apos", "<-lt", ">-gt", "\\x00a0-nbsp", "\\x00a1-iexcl", "\\x00a2-cent", "\\x00a3-pound", "\\x00a4-curren", "\\x00a5-yen", "\\x00a6-brvbar", "\\x00a7-sect", "\\x00a8-uml", "\\x00a9-copy", "\\x00aa-ordf", 
			        "\\x00ab-laquo", "\\x00ac-not", "\\x00ad-shy", "\\x00ae-reg", "\\x00af-macr", "\\x00b0-deg", "\\x00b1-plusmn", "\\x00b2-sup2", "\\x00b3-sup3", "\\x00b4-acute", "\\x00b5-micro", "\\x00b6-para", "\\x00b7-middot", "\\x00b8-cedil", "\\x00b9-sup1", "\\x00ba-ordm", 
			        "\\x00bb-raquo", "\\x00bc-frac14", "\\x00bd-frac12", "\\x00be-frac34", "\\x00bf-iquest", "\\x00c0-Agrave", "\\x00c1-Aacute", "\\x00c2-Acirc", "\\x00c3-Atilde", "\\x00c4-Auml", "\\x00c5-Aring", "\\x00c6-AElig", "\\x00c7-Ccedil", "\\x00c8-Egrave", "\\x00c9-Eacute", "\\x00ca-Ecirc", 
			        "\\x00cb-Euml", "\\x00cc-Igrave", "\\x00cd-Iacute", "\\x00ce-Icirc", "\\x00cf-Iuml", "\\x00d0-ETH", "\\x00d1-Ntilde", "\\x00d2-Ograve", "\\x00d3-Oacute", "\\x00d4-Ocirc", "\\x00d5-Otilde", "\\x00d6-Ouml", "\\x00d7-times", "\\x00d8-Oslash", "\\x00d9-Ugrave", "\\x00da-Uacute", 
			        "\\x00db-Ucirc", "\\x00dc-Uuml", "\\x00dd-Yacute", "\\x00de-THORN", "\\x00df-szlig", "\\x00e0-agrave", "\\x00e1-aacute", "\\x00e2-acirc", "\\x00e3-atilde", "\\x00e4-auml", "\\x00e5-aring", "\\x00e6-aelig", "\\x00e7-ccedil", "\\x00e8-egrave", "\\x00e9-eacute", "\\x00ea-ecirc", 
			        "\\x00eb-euml", "\\x00ec-igrave", "\\x00ed-iacute", "\\x00ee-icirc", "\\x00ef-iuml", "\\x00f0-eth", "\\x00f1-ntilde", "\\x00f2-ograve", "\\x00f3-oacute", "\\x00f4-ocirc", "\\x00f5-otilde", "\\x00f6-ouml", "\\x00f7-divide", "\\x00f8-oslash", "\\x00f9-ugrave", "\\x00fa-uacute", 
			        "\\x00fb-ucirc", "\\x00fc-uuml", "\\x00fd-yacute", "\\x00fe-thorn", "\\x00ff-yuml", "≈í-OElig", "≈ì-oelig", "≈†-Scaron", "≈°-scaron", "≈∏-Yuml", "∆í-fnof", "ÀÜ-circ", "Àú-tilde", "Œë-Alpha", "Œí-Beta", "Œì-Gamma", 
			        "Œî-Delta", "Œï-Epsilon", "Œñ-Zeta", "Œó-Eta", "Œò-Theta", "Œô-Iota", "Œö-Kappa", "Œõ-Lambda", "Œú-Mu", "Œù-Nu", "Œû-Xi", "Œü-Omicron", "Œ†-Pi", "Œ°-Rho", "Œ£-Sigma", "Œ§-Tau", 
			        "Œ•-Upsilon", "Œ¶-Phi", "Œß-Chi", "Œ®-Psi", "Œ©-Omega", "Œ±-alpha", "Œ≤-beta", "Œ≥-gamma", "Œ¥-delta", "Œµ-epsilon", "Œ∂-zeta", "Œ∑-eta", "Œ∏-theta", "Œπ-iota", "Œ∫-kappa", "Œª-lambda", 
			        "Œº-mu", "ŒΩ-nu", "Œæ-xi", "Œø-omicron", "œÄ-pi", "œÅ-rho", "œÇ-sigmaf", "œÉ-sigma", "œÑ-tau", "œÖ-upsilon", "œÜ-phi", "œá-chi", "œà-psi", "œâ-omega", "œë-thetasym", "œí-upsih", 
			        "œñ-piv", "‚Ä?ensp", "‚Ä?emsp", "‚Ä?thinsp", "‚Ä?zwnj", "‚Ä?zwj", "‚Ä?lrm", "‚Ä?rlm", "‚Ä?ndash", "‚Ä?mdash", "‚Ä?lsquo", "‚Ä?rsquo", "‚Ä?sbquo", "‚Ä?ldquo", "‚Ä?rdquo", "‚Ä?bdquo", 
			        "‚Ä?dagger", "‚Ä?Dagger", "‚Ä?bull", "‚Ä?hellip", "‚Ä?permil", "‚Ä?prime", "‚Ä?Prime", "‚Ä?lsaquo", "‚Ä?rsaquo", "‚Ä?oline", "‚Å?frasl", "‚Ç?euro", "‚Ñ?image", "‚Ñ?weierp", "‚Ñ?real", "‚Ñ?trade", 
			        "‚Ñ?alefsym", "‚Ü?larr", "‚Ü?uarr", "‚Ü?rarr", "‚Ü?darr", "‚Ü?harr", "‚Ü?crarr", "‚á?lArr", "‚á?uArr", "‚á?rArr", "‚á?dArr", "‚á?hArr", "‚à?forall", "‚à?part", "‚à?exist", "‚à?empty", 
			        "‚à?nabla", "‚à?isin", "‚à?notin", "‚à?ni", "‚à?prod", "‚à?sum", "‚à?minus", "‚à?lowast", "‚à?radic", "‚à?prop", "‚à?infin", "‚à?ang", "‚à?and", "‚à?or", "‚à?cap", "‚à?cup", 
			        "‚à?int", "‚à?there4", "‚à?sim", "‚â?cong", "‚â?asymp", "‚â?ne", "‚â?equiv", "‚â?le", "‚â?ge", "‚ä?sub", "‚ä?sup", "‚ä?nsub", "‚ä?sube", "‚ä?supe", "‚ä?oplus", "‚ä?otimes", 
			        "‚ä?perp", "‚ã?sdot", "‚å?lceil", "‚å?rceil", "‚å?lfloor", "‚å?rfloor", "‚å?lang", "‚å?rang", "‚ó?loz", "‚ô?spades", "‚ô?clubs", "‚ô?hearts", "‚ô?diams"
			     };
			_lookupTable = new Hashtable<String, Character>();
		    for (String str :_entitiesList)
		    {
		    	_lookupTable.put(str.substring(2), str.charAt(0));
		    }
		}
		public static char Lookup(String entity)
		{
		    char ch;
		    ch=_lookupTable.get(entity);
		    return ch;
		}
	}
	//private final static Pattern _regex_enc=Pattern.compile("^([\\x00-\\x7f]|[\\xfc-\\xff][\\x80-\\xbf]{5}|[\\xf8-\\xfb][\\x80-\\xbf]{4}|[\\xf0-\\xf7][\\x80-\\xbf]{3}|[\\xe0-\\xef][\\x80-\\xbf]{2}|[\\xc0-\\xdf][\\x80-\\xbf])+$");
	//public final static Pattern REGEX_URLENCODED=Pattern.compile("%[0-9a-zA-Z]+");
	//public final static Pattern REGEX_URLSAFE=Pattern.compile("^[0-9a-zA-Z\\-\\.~_]+$");
	
	
	 public static String escape(String src) {
		  int i;
		  char j;
		  StringBuffer tmp = new StringBuffer();
		  tmp.ensureCapacity(src.length() * 6);
		  for (i = 0; i < src.length(); i++) {
		   j = src.charAt(i);
		   if (Character.isDigit(j) || Character.isLowerCase(j)
		     || Character.isUpperCase(j))
		    tmp.append(j);
		   else if (j < 256) {
		    tmp.append("%");
		    if (j < 16)
		     tmp.append("0");
		    tmp.append(Integer.toString(j, 16));
		   } else {
		    tmp.append("%u");
		    tmp.append(Integer.toString(j, 16));
		   }
		  }
		  return tmp.toString();
		 }

		 /*
		  * ∂‘”¶javascriptµƒunescape()∫Ø ˝, ø…∂‘javascriptµƒescape()Ω¯––Ω‚¬Î
		  */
		 public static String unescape(String src) {
		  StringBuffer tmp = new StringBuffer();
		  tmp.ensureCapacity(src.length());
		  int lastPos = 0, pos = 0;
		  char ch;
		  while (lastPos < src.length()) {
		   pos = src.indexOf("%", lastPos);
		   if (pos == lastPos) {
		    if (src.charAt(pos + 1) == 'u') {
		     ch = (char) Integer.parseInt(src
		       .substring(pos + 2, pos + 6), 16);
		     tmp.append(ch);
		     lastPos = pos + 6;
		    } else {
		     ch = (char) Integer.parseInt(src
		       .substring(pos + 1, pos + 3), 16);
		     tmp.append(ch);
		     lastPos = pos + 3;
		    }
		   } else {
		    if (pos == -1) {
		     tmp.append(src.substring(lastPos));
		     lastPos = src.length();
		    } else {
		     tmp.append(src.substring(lastPos, pos));
		     lastPos = pos;
		    }
		   }
		  }
		  return tmp.toString();
		 }
	/*public static String matchURLEncoding(String input){
		try{
			input=URLDecoder.decode(input,"iso-8859-1");
		}
		catch(Exception e){
			return null;
		}
		Matcher matcher=_regex_enc.matcher(input);
		if(matcher.find()){
			return "UTF-8";
		}
		else{
			return "GB2312";
		}
	}*/
	
	/*public static boolean isURLEncoded(String param){
		Matcher m1=REGEX_URLENCODED.matcher(param);
		return m1.find();
	}*/
	
	/*public static boolean isSafeURLString(String str){
		Matcher matcher=REGEX_URLSAFE.matcher(str);
		if(matcher.find())
			return true;
		return false;
	}*/
	public static String urlEncode(String input,String encoding){
		try
		{
			return URLEncoder.encode(input, encoding);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	public static String urlDecode(String input,String encoding){
		try
		{
			return URLDecoder.decode(input, encoding);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	public static String htmlEncode(String input){
		if(input==null||input.isEmpty())
			return input;
		StringBuffer stringbuffer = new StringBuffer();
        int j = input.length();
        for(int i = 0; i < j; i++)
        {
            char c = input.charAt(i);
            switch(c){
	            case '&':
	            	stringbuffer.append("&amp;");
	            case '\'':
	            	stringbuffer.append("&#39;");
	                continue;
	            case '"':
	            	stringbuffer.append("&quot;");
	                continue;
	            case '<':
	            	stringbuffer.append("&lt;");
	                continue;
	            case '>':
	            	stringbuffer.append("&gt;");
	                continue;
            }
            if ((c >=0x00a0) && (c < 'ƒÄ'))
            {
            	stringbuffer.append("&#"+(int)c+";");
            }
            else
            {
            	stringbuffer.append(c);
            }
    	}
        return stringbuffer.toString();
	}
	public static String htmlDecode(String input)
	{
		if(input==null||input.isEmpty())
			return input;
		if(input.indexOf('&')==-1)
			return input;
		StringBuffer buffer=new StringBuffer();
		 int length = input.length();
         for (int i = 0; i < length; i++)
         {
             char ch = input.charAt(i);
             if (ch == '&')
             {
                 int num3 =-1;
                 for(int j=i+1;j<length;j++){
                	 if(input.charAt(j)==';'||input.charAt(j)=='&'){
                		 num3=j;
                		 break;
                	 }
                }
                if ((num3 > 0) && (input.charAt(num3) == ';'))
                 {
                     String entity = input.substring(i + 1, num3);
                     if ((entity.length() > 1) && (entity.charAt(0) == '#'))
                     {
                         int num4=0;
                    	 if ((entity.charAt(1) == 'x') || (entity.charAt(1) == 'X'))
                         {
                             num4=Integer.parseInt(entity.substring(2));
                    	 }
                         else
                         {
                        	 num4=Integer.parseInt(entity.substring(1));
                         }
                         if (num4 != 0)
                         {
                             ch = (char) num4;
                             i = num3;
                         }
                     }
                     else
                     {
                         i = num3;
                         char ch2 = HtmlEntities.Lookup(entity);
                         if (ch2 != '\0')
                         {
                             ch = ch2;
                         }
                         else
                         {
                        	 buffer.append('&');
                        	 buffer.append(entity);
                        	 buffer.append(';');
                         }
                     }
                 }
             }
             buffer.append(ch);
         }
         return buffer.toString();
	}
	public static Map<String,String[]> getCookieItems(Cookie cook,String encoding){
		String valueStr=cook.getValue();
		String[] groups=Utils.split(valueStr,"&");
		Hashtable<String,ArrayList<String>> table=new Hashtable<String,ArrayList<String>>();
		for(String s:groups){
			String[] kv=s.split("=",2);
			if(kv.length<2)
				continue;
			ArrayList<String> list=table.get(kv[0]);
			if(list==null){
				list=new ArrayList<String>();
				table.put(kv[0], list);
			}
			try {
				kv[1]=URLDecoder.decode(kv[1], encoding);
			} catch (UnsupportedEncodingException e) {
			}
			list.add(kv[1]);
		}
		Hashtable<String,String[]> result=new Hashtable<String,String[]>();
		for(Entry<String,ArrayList<String>> entry:table.entrySet()){
			String key=entry.getKey();
			ArrayList<String> value=entry.getValue();
			String[] arr=new String[value.size()];
			value.toArray(arr);
			result.put(key, arr);
		}
		return result;
	}
}
