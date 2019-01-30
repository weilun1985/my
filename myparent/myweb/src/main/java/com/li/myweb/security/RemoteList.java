package com.li.myweb.security;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import com.li.myweb.Utils;

public class RemoteList implements InterecptorList {
	public RemoteList(String url){
		this.url=url;
		this.load();
	}
	private String url;
	private String etag;
	private long lastCheckTime;
	private volatile boolean changedChecking;
	private HashSet<Long> set;
	
	private boolean isChanged(){
		try
		{
			//如果已经有线程在检查，则按照没有发生变化的逻辑执行
			if(changedChecking)
				return false;
			changedChecking=true;
			//如果当前时间没有超过上次检查时间3秒，认为没有修改
			if((System.currentTimeMillis()-lastCheckTime)/3000<2)
				return false;
			HttpClient httpclient =new DefaultHttpClient();
			HttpHead httphead = new HttpHead(url);
			httphead.setHeader("If-None-Match", this.etag);
			HttpResponse response=httpclient.execute(httphead);
			//如果Etag没有发生变化
			if(response.getStatusLine().getStatusCode()!=304){
				this.lastCheckTime=System.currentTimeMillis();
				this.etag=response.getFirstHeader("Etag").getValue();
				return true;
			}
		}
		catch(Exception e){
			
		}
		finally{
			changedChecking=false;
		}
		return false;
	}
	private void load(){
		InputStreamReader inputReader=null;
		BufferedReader reader=null;
		InputStream inputStream=null;
		HashSet<Long> temp=new HashSet<Long>();
		try
		{
			HttpClient httpclient =new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response=httpclient.execute(httpget);
			int code=response.getStatusLine().getStatusCode();
			if(code==200){
				inputStream=response.getEntity().getContent();
				inputReader=new InputStreamReader(inputStream,"utf-8");
				reader=new BufferedReader(inputReader);
				String line;
				while((line=reader.readLine())!=null){
					if(line.charAt(0)==65279)
						line=line.substring(1);
					if(!(line=line.trim()).isEmpty()){
						InetAddress ip=InetAddress.getByName(line);
						long ipLong=Utils.readLong(ip.getAddress());
						temp.add(ipLong);
					}
				}
				this.etag=response.getFirstHeader("Etag").getValue();
			}
			this.set=temp;
		}
		catch(Exception e){
			
		}
		finally{
			try{
				if(inputStream!=null)
					inputStream.close();
				if(inputReader!=null)
					inputReader.close();
				if(reader!=null)
					reader.close();
			}
			catch(Exception e){
				
			}
		}
	}
	
	public void checkUpdate(){
		if(this.isChanged()){
			this.load();
		}
	}
	public boolean contains(long ip){
		return this.set.contains(ip);
	}
}
