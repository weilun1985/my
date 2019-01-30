package com.li.myweb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class HttpRequest extends HttpServletRequestWrapper {

	public HttpRequest(HttpServletRequest request,String enc) throws UnsupportedEncodingException {
		super(request);
		// TODO Auto-generated constructor stub
		//设置请求编码
		if(enc!=null)
			this.setCharacterEncoding(enc);
		this.method=request.getMethod();
		this.methodCode=HttpMethod.getCode(this.method);
		this.cookieItems=new HashMap<String,Map<String,String[]>>();
		this.fileSizeMax=((HttpServerUtility)this.getServletContext().getAttribute(HttpRuntime.APP_ATTR_SERVER)).getConfigure().getUploadSet().getFileSizeMax();
		this.m1();
	}
	public final static String ACTIONPARAME_NAME="_action";
	//private final static Pattern _regex_urlparam=Pattern.compile("(^|&)([^&=]+)=([^&]*)");
		//Pattern.compile("([^&=:/]+)=([^&=:/]*)");
	public final String method;
	public final int methodCode;
	private Map<String,String[]> forms;
	private Map<String,String[]> queryStrings;
	private Map<String,String[]> paramters;
	private Map<String,Cookie> cookies;
	private Map<String,Map<String,String[]>> cookieItems;
	
	private boolean cookiesgot;
	private long fileSizeMax;
	//private boolean pmSplited=false;
	private String qstrEncoding;
	//private boolean urlEncoded=false;
	private String actionName;
	private boolean isDefaultActionName;
	private HttpUploadedFileCollection fileCll=new HttpUploadedFileCollection();
	
	public String getQueryStringEncdoing(){
		if(qstrEncoding!=null)
			return qstrEncoding;
		return this.getCharacterEncoding();
	}
	public HttpUploadedFile getFile(String name){
		return this.fileCll.get(name);
	}
	public HttpUploadedFile getFile(int index){
		return this.fileCll.get(index);
	}
	public int getFileCount(){
		return this.fileCll.count();
	}
	private class queryItem{
		public queryItem(){
			values=new ArrayList<String>();
		}
		public String key0;
		public List<String> values;
	}
	private void m1(){
		Map<String,queryItem> queryAnchors=new LinkedHashMap<String,queryItem>();
		String pmstr=super.getQueryString();
		if(pmstr!=null&&pmstr.length()>0){
			int len=pmstr.length();
			int e=0;
			while(e<len){
				int p=pmstr.indexOf('=',e);
				if(p<1)
					break;
				String key0=null;
				int ka=p;
				while(ka>e){
					char c=pmstr.charAt(--ka);
					if(c=='&'||c=='='){
						ka++;
						break;
					}
				}
				if(ka==p){
					e=p+1;
					continue;
				}
				key0=pmstr.substring(ka, p);
				
				int vb=p+1;
				while(vb<len){
					char c=pmstr.charAt(vb);
					if(c=='&'){
						break;
					}
					vb++;
				}
				String value0="";
				if(vb!=p+1){
					value0=pmstr.substring(p+1,vb);
				}
				e=vb++;
				String key=key0;
				try{
					key=URLDecoder.decode(key0, this.getCharacterEncoding());
				}catch(Exception ex){
				}
				queryItem qitm=queryAnchors.get(key);
				if(qitm==null){
					qitm=new queryItem();
					qitm.key0=key0;
				}
				value0=this.decode(value0);
				qitm.values.add(value0);
				queryAnchors.put(key, qitm);
			}
			
		}
		boolean post=this.getMethod().equals(HttpMethod.POST);
		String contentType=this.getContentType();
		//Map<String,String[]> paramsMap=super.getParameterMap();
		//Map<String,String[]> map_q1=new HashMap<String,String[]>();//GET
		Map<String,List<String>> map_f1=new LinkedHashMap<String,List<String>>();//POST
		if(post){
			if(contentType!=null&&(contentType=contentType.toLowerCase()).startsWith("multipart/form-data;")){
				this.getFromMultipart(map_f1);
			}
			else{
				//split get and post
				Map<String,String[]> paramsMap=super.getParameterMap();
				for(Entry<String,String[]> entry:paramsMap.entrySet()){
					String[] values=entry.getValue();
					if(queryAnchors.size()>0){
						queryItem qitm=queryAnchors.get(entry.getKey());
						int anchor=0;
						if(qitm!=null)
							anchor=qitm.values.size();
						if(anchor>0){
							/*String[] values_q=new String[anchor];
							for(int i=0;i<anchor;i++){
								values_q[i]=values[i];
							}
							map_q1.put(entry.getKey(), values_q);*/
							List<String> values_f=new ArrayList<String>(values.length-anchor);
							for(int i=anchor;i<values.length;i++){
								values_f.add(values[i]);
							}
							map_f1.put(entry.getKey(), values_f);
						}else{
							List<String> values_f=new ArrayList<String>(values.length);
							this.addAllToList(values, values_f);
							map_f1.put(entry.getKey(), values_f);
						}
					}
					else{
						List<String> values_f=new ArrayList<String>(values.length);
						this.addAllToList(values, values_f);
						map_f1.put(entry.getKey(), values_f);
					}
				}
			}
		}
		
		
		Map<String,List<String>> map_q2=new LinkedHashMap<String,List<String>>();//GET
		Map<String,List<String>> map_f2=new LinkedHashMap<String,List<String>>();//POST
		Map<String,List<String>> map_all=new HashMap<String,List<String>>();
		//GET merger
		for(Entry<String,queryItem> entry:queryAnchors.entrySet()){
			String key=this.decode(entry.getKey()).toLowerCase();
			List<String> vlist=map_q2.get(key);
			if(vlist==null){
				vlist=new ArrayList<String>();
				map_q2.put(key, vlist);
			}
			queryItem qitm=entry.getValue();
			vlist.addAll(qitm.values);
			
			List<String> vlist2=map_all.get(key);
			if(vlist2==null){
				vlist2=new ArrayList<String>();
				map_all.put(key, vlist2);
			}
			vlist2.addAll(qitm.values);
		}
		//POST merger
		for(Entry<String,List<String>> entry:map_f1.entrySet()){
			String key=entry.getKey().toLowerCase();
			List<String> vlist=map_f2.get(key);
			if(vlist==null){
				vlist=new ArrayList<String>();
				map_f2.put(key, vlist);
			}
			vlist.addAll(entry.getValue());
			
			List<String> vlist2=map_all.get(key);
			if(vlist2==null){
				vlist2=new ArrayList<String>();
				map_all.put(key, vlist2);
			}
			vlist2.addAll(entry.getValue());
		}
		//queryString
		this.queryStrings=new NameValueCollection();
		for(Entry<String,List<String>> entry:map_q2.entrySet()){
			String[] arry=new String[entry.getValue().size()];
			this.queryStrings.put(entry.getKey(),entry.getValue().toArray(arry));
		}
		//forms
		this.forms=new NameValueCollection();
		for(Entry<String,List<String>> entry:map_f2.entrySet()){
			String[] arry=new String[entry.getValue().size()];
			this.forms.put(entry.getKey(),entry.getValue().toArray(arry));
		}
		//params
		this.paramters=new NameValueCollection();
		for(Entry<String,List<String>> entry:map_all.entrySet()){
			String[] arry=new String[entry.getValue().size()];
			this.paramters.put(entry.getKey(),entry.getValue().toArray(arry));
		}
		
	}
	private void getFromMultipart(Map<String,List<String>> hm){
		DiskFileItemFactory factory = new DiskFileItemFactory(); 
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setFileSizeMax(this.fileSizeMax);
		try
		{
			List <FileItem> items = upload.parseRequest(this);
			for(FileItem itm:items){
				if(itm.isFormField()){
					String name=itm.getFieldName().toLowerCase();
					String value=itm.getString(this.getCharacterEncoding());
					List<String> list;
					if((list=hm.get(name))==null){
						list=new ArrayList<String>();
						hm.put(name, list);
					}
					list.add(value);
				}
				else{
					HttpUploadedFile file=new HttpUploadedFile(itm);
					this.fileCll.add(file);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private void addAllToList(String[] array,List<String> list){
		for(String str:array){
			list.add(str);
		}
	}
	private String decode(String org){
		boolean needcheckenc=false;
		for(int i=0;i<org.length();i++){
			char c=org.charAt(i);
			if(c=='%'){
				needcheckenc=true;
				break;
			}
		}
		if(needcheckenc&&org.length()>3){
			if(this.qstrEncoding==null){
				this.checkEnc(org);
			}
			if(this.qstrEncoding!=null&&this.qstrEncoding.equals("JSESCA")){
				org=HttpUtility.unescape(org);
			}else{
				try{
					org=URLDecoder.decode(org, qstrEncoding==null?this.getCharacterEncoding():qstrEncoding);
				}catch(Exception ex){
					System.out.println(ex);
				}
			}
		}
		return org;
	}
	/*private Map<String,List<String>> getparams2(String pmstr){
		Map<String,List<String>> hm=new HashMap<String,List<String>>();
		Matcher matcher=_regex_urlparam.matcher(pmstr);
		while(matcher.find()){
			String name=matcher.group(2).toLowerCase();
			String value=matcher.group(3);
			List<String> list;
			if((list=hm.get(name))==null){
				list=new ArrayList<String>();
				hm.put(name, list);
			}
			list.add(value);
		}
		return hm;
	}*/
	private boolean getEncFromBuffer(int[] buffer,int pos){
		//满3字节，判定是否是UTF-8编码,如果不是则判断是否是GB2312
		//满2字节，进行GB2312判断
		if(pos==3)
		{
			boolean isUtf8=buffer[0]>=0xE0&&buffer[0]<=0xEF
				&&buffer[1]>=0x80&&buffer[1]<=0xBF
				&&buffer[2]>=0x80&&buffer[2]<=0xBF;
			if(isUtf8){
				this.qstrEncoding="utf-8";
				return true;
			}
			else
				return getEncFromBuffer(buffer,2);
		}
		else if(pos==2){
			boolean isgb2312=buffer[0]>=0xA1&&buffer[0]<=0xF7
			&&buffer[1]>=0xA1&&buffer[1]<=0xFE;
			if(isgb2312){
				this.qstrEncoding="gb2312";
				return true;
			}
		}
		return false;
	}
	private void checkEnc(String input){
		int len=input.length();
		if(len<6)
			return;
		int[] buffer=new int[3];
		int pos=0;
		for(int i=0;i<len;i++){
			if(input.charAt(i)=='%'&&i<len-2){
				char nxc=input.charAt(i+1);
				char nxc2=input.charAt(i+2);
				//判断是否是javascript escape
				if((nxc=='u'||nxc=='U')&&i<len-5){
					char nxc3=input.charAt(i+3);
					char nxc4=input.charAt(i+4);
					char nxc5=input.charAt(i+5);
					if(((nxc2>47&&nxc2<58)||(nxc2>96&&nxc2<103)||(nxc2>64&&nxc2<71))
							&&((nxc3>47&&nxc3<58)||(nxc3>96&&nxc3<103)||(nxc3>64&&nxc3<71))
							&&((nxc4>47&&nxc4<58)||(nxc4>96&&nxc4<103)||(nxc4>64&&nxc4<71))
							&&((nxc5>47&&nxc5<58)||(nxc5>96&&nxc5<103)||(nxc5>64&&nxc5<71)))
						this.qstrEncoding="JSESCA";
					return;
				}
				else{
					if(!(((nxc>47&&nxc<58)||(nxc>96&&nxc<103)||(nxc>64&&nxc<71))
							&&((nxc2>47&&nxc2<58)||(nxc2>96&&nxc2<103)||(nxc2>64&&nxc2<71))))
					{
						//非16进制串,清除缓存且继续
						buffer[0]=buffer[1]=buffer[2]=0;
						pos=0;
						continue;
					}
					int b=Integer.decode("0x"+nxc+nxc2);
					if(pos==0&&b<0x80)
						continue;
					buffer[pos++]=b;
					//若满3字节，判定编码
					if(pos==3)
					{
						//如果已判定编码，返回
						//否则清除缓存且继续
						if(getEncFromBuffer(buffer,3)){
							return;
						}
						else{
							buffer[0]=buffer[1]=buffer[2]=0;
							pos=0;
						}
					}
				}
			}
		}
		if(pos>1){
			getEncFromBuffer(buffer,pos);
		}
		
	}
	/*private Map<String,List<String>> getparams(String pmstr,boolean autocheckenc){
		Map<String,List<String>> hm=new LinkedHashMap<String,List<String>>();
		
		int len=pmstr.length();
		int e=0;
		
		while(e<len){
			int p=pmstr.indexOf('=',e);
			if(p<1)
				return hm;
			//获取键
			String key=null;
			String value="";
			boolean needcheckenc=false;
			boolean errdecode=false;
			int ka=p;
			while(ka>e){
				char c=pmstr.charAt(--ka);
				if(c=='&'||c=='='){
					ka++;
					break;
				}
				else if(c=='%'){
					needcheckenc=true;
				}
				else if(c>0x7f){
					errdecode=true;
				}
			}
			if(ka==p){
				e=p+1;
				continue;
			}
			key=pmstr.substring(ka, p);
			//检测编码
			if(autocheckenc){
				//如果有错码
				if(errdecode){
					try{
						key=new String(key.getBytes("iso-8859-1"),this.getCharacterEncoding().equals("utf-8")?"gb2312":"utf-8");
					}catch(Exception ex){
						System.out.println(ex);
					}
				}
				else if(needcheckenc&&key.length()>3){
					if(this.qstrEncoding==null){
						this.checkEnc(key);
					}
					if(this.qstrEncoding!=null&&this.qstrEncoding.equals("JSESCA")){
						key=HttpUtility.unescape(key);
					}else{
						try{
							key=URLDecoder.decode(key, qstrEncoding==null?this.getCharacterEncoding():qstrEncoding);
						}catch(Exception ex){
							System.out.println(ex);
						}
					}
				}
			}
			needcheckenc=false;
			errdecode=false;
			//获取值
			int vb=p+1;
			while(vb<len){
				char c=pmstr.charAt(vb);
				if(c=='&'){
					break;
				}
				else if(c=='%'){
					needcheckenc=true;
				}
				else if(c>0x7f){
					errdecode=true;
				}
				vb++;
			}
			if(vb!=p+1){
				value=pmstr.substring(p+1,vb);
				//检测编码
				if(autocheckenc){
					if(errdecode){
						try{
							value=new String(value.getBytes("iso-8859-1"),this.getCharacterEncoding().equals("utf-8")?"gb2312":"utf-8");
						}catch(Exception ex){}
					}
					else if(needcheckenc&&value.length()>3){
						if(this.qstrEncoding==null){
							this.checkEnc(value);
						}
						if(this.qstrEncoding!=null&&this.qstrEncoding.equals("JSESCA")){
							value=HttpUtility.unescape(value);
						}else{
							try{
								value=URLDecoder.decode(value, qstrEncoding==null?this.getCharacterEncoding():qstrEncoding);
							}catch(Exception ex){}
						}
					}
				}
			}
			if(key!=null&&key.length()>0)
				key=key.toLowerCase();
			List<String> list;
			if((list=hm.get(key))==null){
				list=new ArrayList<String>();
				hm.put(key, list);
			}
			list.add(value);
			e=vb++;
		}
		return hm;
	}*/
	/*
	private String autoDecode(String str){
		if(qstrEncoding==null){
			qstrEncoding=HttpUtility.matchURLEncoding(str);
		}
		boolean encoded=HttpUtility.isURLEncoded(str);
		if(encoded){
			try {
				str=URLDecoder.decode(str, qstrEncoding==null?this.getCharacterEncoding():qstrEncoding);
			} catch (Exception e) {
				str=HttpUtility.unescape(str);
			}
		}else{
			try {
				str=new String(str.getBytes("iso-8859-1"),qstrEncoding==null?this.getCharacterEncoding():qstrEncoding);
			} catch (Exception e) {
			}
		}
		return str;
	}*/
	
	/**
	 * 
	 */
	/*@SuppressWarnings("unchecked")
	private void splitparams(){
		//pmSplited=true;
		this.queryStrings=new LinkedHashMap<String,String[]>();
		this.forms=new LinkedHashMap<String,String[]>();
		HashMap<String,List<String>> tpms=new LinkedHashMap<String,List<String>>();
		//提取GET参数
		String qstr=this.getQueryString();
		if(qstr!=null&&qstr.length()>0){
			Map<String,List<String>> hm=this.getparams(qstr,true);
			//this.queryStrings=new HashMap<String,String[]>();
			for(Entry<String,List<String>> entry:hm.entrySet()){
				String name=entry.getKey();
				List<String> vlist=entry.getValue();
				//如果包含非URL安全字符
				if(name!=null&&name.length()>0&&!HttpUtility.isSafeURLString(name)){
					name=this.autoDecode(name);
				}
				String[] values=new String[vlist.size()];
				for(int i=0;i<vlist.size();i++){
					values[i]=vlist.get(i);
//					if(values[i]!=null&&values[i].length()>0&&!HttpUtility.isSafeURLString(values[i]))
//					{
//						values[i]=this.autoDecode(values[i]);
//						vlist.set(i, values[i]);
//					}
				}
				this.queryStrings.put(name, values);
				tpms.put(name, vlist);
			}
		}
		//提取POST参数
		boolean post=this.getMethod().equals(HttpMethod.POST);
		String[] tmpv=super.getParameterValues("remark");
		if(tmpv!=null&&tmpv.length>0)
		{
		String tmp=super.getParameterValues("remark")[0];
		System.out.println(tmp);
		}
		if(post){
			String contentType=this.getContentType();
			if(contentType!=null)
				contentType=contentType.toLowerCase();
			//application/x-www-form-urlencoded模式
			String encoding=this.getCharacterEncoding();
			if(contentType.startsWith("application/x-www-form-urlencoded"))
			{
				//byte[] buffer=new byte[this.getContentLength()];
				String fstr=null;
				StringBuffer sb=new StringBuffer();
				byte[] buffer=new byte[1024];
				try {
					ServletInputStream inputStream=this.getInputStream();
					int a;
					while((a=inputStream.read(buffer))>0){
						if(a==buffer.length){
							sb.append(new String(buffer,"ISO-8859-1"));
						}
						else{
							byte[] buffer2=new byte[a];
							System.arraycopy(buffer, 0, buffer2, 0, a);
							sb.append(new String(buffer2,"ISO-8859-1"));
						}
						//sb.append(str)
					}
					//fstr=new String(buffer,"ISO-8859-1");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println(e);
				}
				if(sb.length()>0)
					fstr=sb.toString();
				//this.forms=new HashMap<String,String[]>();
				if(fstr!=null){
					Map<String,List<String>> hm=this.getparams(fstr,false);
					for(Entry<String,List<String>> entry:hm.entrySet()){
						String name=entry.getKey();
						List<String> vlist=entry.getValue();
						String[] values=new String[vlist.size()];
						try
						{
							name=URLDecoder.decode(name,encoding);
							List<String> vlist2=tpms.get(name);
							if(vlist2==null)
							{
								vlist2=new ArrayList<String>();
								tpms.put(name, vlist2);
							}
							for(int i=0;i<vlist.size();i++){
								values[i]=URLDecoder.decode(vlist.get(i),encoding);
								vlist2.add(values[i]);
							}
						}
						catch(Exception e){
							System.out.println(e);
						}
						this.forms.put(name, values);
					}
				}
			}
			else if(contentType.startsWith("multipart/form-data;")){
				DiskFileItemFactory factory = new DiskFileItemFactory(); 
				ServletFileUpload upload = new ServletFileUpload(factory);
				upload.setFileSizeMax(HttpContext.current().server().getConfigure().getUploadSet().getFileSizeMax());
				try
				{
					List <FileItem> items = upload.parseRequest(this);
					Map<String,List<String>> hm=new LinkedHashMap<String,List<String>>();
					for(FileItem itm:items){
						if(itm.isFormField()){
							String name=itm.getFieldName().toLowerCase();
							String value=itm.getString(encoding);
							List<String> list;
							if((list=hm.get(name))==null){
								list=new ArrayList<String>();
								hm.put(name, list);
							}
							list.add(value);
						}
						else{
							HttpUploadedFile file=new HttpUploadedFile(itm);
							this.fileCll.add(file);
						}
					}
					//this.forms=new HashMap<String,String[]>();
					for(Entry<String,List<String>> entry:hm.entrySet()){
						String name=entry.getKey();
						List<String> vlist=entry.getValue();
						String[] values=new String[vlist.size()];
						try
						{
							name=URLDecoder.decode(name,encoding);
						List<String> vlist2=tpms.get(name);
						if(vlist2==null)
						{
							vlist2=new ArrayList<String>();
							tpms.put(name, vlist2);
						}
						for(int i=0;i<vlist.size();i++){
							values[i]=vlist.get(i);//URLDecoder.decode(vlist.get(i),encoding);
							vlist2.add(values[i]);
						}
						}
						catch(Exception e){
						}
						this.forms.put(name, values);
					}
				}catch(Exception ex){
					System.out.println(ex);
				}
			}
		}
		this.paramters=new LinkedHashMap<String,String[]>();
		for(Entry<String,List<String>> entry:tpms.entrySet())
		{
			this.paramters.put(entry.getKey(), entry.getValue().toArray(new String[0]));
		}
		pmSplited=true;
	}*/
	/*@SuppressWarnings({ "unchecked", "rawtypes" })
	private void splitparams(){
		Map<String,List<String>> ht_get_1=null;
		Map<String,List<String>> ht_post_1=null;
		
		//提取GET参数
		String qstr=this.getQueryString();
		if(qstr!=null&&qstr.length()>0){
			ht_get_1=new HashMap<String,List<String>>();
			for(String itm:qstr.split("&")){
				String[] kv=itm.split("=",2);
				if(kv.length!=2)
					continue;
				List<String> list=ht_get_1.get(kv[0]);
				if(list==null){
					list=new ArrayList<String>();
					ht_get_1.put(kv[0], list);
				}
				//如果参数不为空且值字符串包含非URL安全字符
				if(kv[1].length()>0&&!HttpUtility.isSafeURLString(kv[1])){
					//如果encode为null则先识别URL编码方式
					if(qstrEncoding==null){
						Object[] r=HttpUtility.autoMatchURLEncoding(kv[1]);
						if(r!=null){
							urlEncoded=(Boolean)r[0];
							qstrEncoding=(String)r[1];
						}
						else{
							qstrEncoding=this.getCharacterEncoding();
						}
					}
					try{
						//如果浏览器已经对参数进行编码，则进行URL解码
						//否则按照ISO-8859-1格式转换为字节序列后生成新字符串
						if(urlEncoded){
							kv[1]=URLDecoder.decode(kv[1], qstrEncoding);
						}
						else{
							kv[1]=new String(kv[1].getBytes("iso-8859-1"),qstrEncoding);
						}
					}
					catch(Exception e){
					}
				}
				list.add(kv[1]);
			}
		}
		//提取POST参数
		boolean post=this.getMethod().equals(HttpMethod.POST);
		//获取Forms
		if(post){
			ht_post_1=new HashMap<String,List<String>>();
			
			Enumeration<String> enumers=this.getParameterNames();
			while(enumers.hasMoreElements()){
				String name=enumers.nextElement();
				String[] params=this.getParameterValues(name);
				if(params==null||params.length==0)
					continue;
				//如果query中包含有本名称的参数，如果长度等于params的长度认为都是GET参数
				//否则params扣除掉query参数长度后的项是POST参数
				//如果query不包含，则全部是POST参数
				List<String> querys=null;
				if(ht_get_1!=null)
					querys=ht_get_1.get(name);
				int a=querys==null?0:querys.size();
				List<String> values=new ArrayList<String>();
				for(int i=a;i<params.length;i++){
					values.add(params[i]);
				}
				ht_post_1.put(name, values);
			}
		}
		//对GET参数名统一大小写
		if(ht_get_1!=null){
			Map<String,List<String>> ht_get_ig=Utils.ignoreKeyUL(ht_get_1);
			this.queryStrings=new HashMap<String,String[]>();
			for(Object eo:ht_get_ig.entrySet()){
				Entry entry=(Entry)eo;
				String name=(String)entry.getKey();
				List<String> list=(List<String>)entry.getValue();
				String[] values=new String[list.size()];
				this.queryStrings.put(name, list.toArray(values));
			}
			Enumeration<String> nameEms=ht_get_ig.keys();
			while(nameEms.hasMoreElements()){
				String name=nameEms.nextElement();
				List<String> list=ht_get_ig.get(name);
				String[] values=new String[list.size()];
				this.queryStrings.put(name, list.toArray(values));
			}
		}
		//对POST参数名统一大小写
		if(ht_post_1!=null){
			Map<String,List<String>> ht_post_ig=Utils.ignoreKeyUL(ht_post_1);
			this.forms=new HashMap<String,String[]>();
			for(Object eo:ht_post_ig.entrySet()){
				Entry entry=(Entry)eo;
				String name=(String)entry.getKey();
				List<String> list=(List<String>)entry.getValue();
				String[] values=new String[list.size()];
				this.forms.put(name, list.toArray(values));
			}
			Enumeration<String> nameEms=ht_post_ig.keys();
			while(nameEms.hasMoreElements()){
				String name=nameEms.nextElement();
				List<String> list=ht_post_ig.get(name);
				String[] values=new String[list.size()];
				this.forms.put(name, list.toArray(values));
			}
		}
		pmSplited=true;
	}*/
	
	public Map<String,String[]> forms(){
		/*if(pmSplited)
			return forms;
		else{
			splitparams();*/
			return forms;
		/*}*/
			
	}
	public Map<String,String[]> queryStrings(){
		/*if(pmSplited)
			return queryStrings;
		else{
			splitparams();*/
			return queryStrings;
		/*}*/
	}
	public String getForm(String name){
		name=name.toLowerCase();
		if(this.forms()==null)
			return null;
		String[] arr= this.forms().get(name);
		if(arr==null||arr.length==0)
			return null;
		return arr[0];
	}
	public String getQueryString(String name){
		name=name.toLowerCase();
		if(this.queryStrings()==null)
			return null;
		String[] arr=this.queryStrings().get(name);
		if(arr==null||arr.length==0)
			return null;
		return arr[0];
	}
	@Override
	public String getParameter(String name){
		name=name.toLowerCase();
		Map<String,String[]> map1;
		Map<String,String[]> map2;
		if(this.method==HttpMethod.POST){
			map1=this.forms();
			map2=this.queryStrings();
		}
		else{
			map1=this.queryStrings();
			map2=this.forms();
		}
		String[] a=map1.get(name);
		if(a==null||a.length==0)
			a=map2.get(name);
		if(a==null||a.length==0)
			return super.getParameter(name);
		return a[0];
	}
	@Override
	public Map<String,String[]> getParameterMap(){
		/*if(this.paramters!=null)
			return this.paramters;
		this.splitparams();*/
		return this.paramters;
		
		/*Map<String,String[]> omap=super.getParameterMap();
		if((this.forms()==null||this.forms().size()==0)&&(this.queryStrings()==null||this.queryStrings().size()==0)){
			this.paramters=omap;
			return this.paramters;
		}
		this.paramters=new Hashtable<String,String[]>();
		for(Entry<String,String[]> entry:omap.entrySet()){
			String key=entry.getKey().toLowerCase();
			String[] tmp1=this.queryStrings().get(key);
			String[] tmp2=this.forms().get(key);
			if((tmp1==null||tmp1.length==0)&&(tmp2==null||tmp2.length==0)){
				this.paramters.put(entry.getKey(), entry.getValue());
				continue;
			}
			int a=tmp1==null?0:tmp1.length;
			int b=tmp2==null?0:tmp2.length;
			String[] replArry=new String[a+b];
			for(int i=0;i<a;i++){
				replArry[i]=tmp1[i];
			}
			for(int i=0;i<b;i++){
				replArry[i+a]=tmp2[i];
			}
			//替换
			this.paramters.put(key, replArry);
		}
		//如果是multipart/form-data，直接添加form中的key值到omap
		if(this.getContentType().startsWith("multipart/form-data;")){
			for(Entry<String,String[]> entry:this.forms().entrySet()){
				if(this.paramters.containsKey(entry.getKey()))
					continue;
				this.paramters.put(entry.getKey(), entry.getValue());
			}
		}
		return this.paramters;*/
	}
	@Override
	public String[] getParameterValues(String name){
		name=name.toLowerCase();
		return this.getParameterMap().get(name);
	}
	
	@Override
	public Enumeration<String> getParameterNames(){
		Map<String,String[]> pmmap=this.getParameterMap();
		java.util.Vector<String> v=new java.util.Vector<String>();
		v.addAll(pmmap.keySet());
		return v.elements();
		//return ((Hashtable)this.getParameterMap()).keys();
	}
	public Cookie getCookie(String name){
		name=name.toLowerCase();
		if(!this.cookiesgot){
			Cookie[] cooks=this.getCookies();
			if(cooks!=null){
				this.cookies=new HashMap<String,Cookie>();
				for(Cookie cook:this.getCookies()){
					this.cookies.put(cook.getName(), cook);
				}
			}
			this.cookiesgot=true;
		}
		if(this.cookies==null)
			return null;
		return this.cookies.get(name);
	}
	public Map<String,String[]> getCookieItems(String name){
		name=name.toLowerCase();
		Map<String,String[]> result=null;
		if(this.cookieItems.containsKey(name)){
			result=this.cookieItems.get(name);
			return result;
		}
		Cookie cookie=this.getCookie(name);
		if(cookie==null)
			return null;
		result=HttpUtility.getCookieItems(cookie, this.getCharacterEncoding());
		this.cookieItems.put(name, result);
		return result;
	}
	public String[] getCookieItemValues(String cookieName,String itemName){
		Map<String,String[]> items=this.getCookieItems(cookieName);
		if(items==null)
			return null;
		return items.get(itemName);
	}
	public String getAction(){
		if(actionName!=null&&!actionName.isEmpty())
			return actionName;
		/*if(method.equals(HttpMethod.POST)){
			actionName=this.getForm(ACTIONPARAME_NAME);
			if(actionName==null||actionName.isEmpty()){
				actionName=this.getQueryString(ACTIONPARAME_NAME);
			}
		}
		else{
			actionName=this.getQueryString(ACTIONPARAME_NAME);
		}*/
		actionName=super.getParameter(ACTIONPARAME_NAME);
		//如果没有指定Action,默认采用do+请求方式
		if(actionName==null||actionName.isEmpty()){
			actionName="do"+this.method;
			isDefaultActionName=true;
		}
		return actionName.toLowerCase();
	}
	public boolean isDefaultAction(){
		return this.isDefaultActionName;
	}
	public String getIfNoneMatch(){
		return this.getHeader("If-None-Match");
	}
	public Date getIfModifiedSince(){
		Date modifiedSince=null;
		try
		{
			String lmstr=this.getHeader("If-Modified-Since");
			if(lmstr!=null&&lmstr.length()>0){
				//将字符串转换为long
				long lmt=Long.parseLong(lmstr);
				modifiedSince= new Date(lmt);
			}
		}
		catch(Exception e){
			
		}
		return modifiedSince;
	}
}
