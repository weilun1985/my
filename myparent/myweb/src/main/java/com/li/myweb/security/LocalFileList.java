package com.li.myweb.security;

import java.io.File;
//import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;

import eagle.web.Utils;

public class LocalFileList implements InterecptorList {
	public LocalFileList(String path){
		file=new File(path);
		this.load();
		this.lastCheckTime=System.currentTimeMillis();
	}
	private File file;
	private long lastModified;
	private long lastCheckTime;
	private volatile boolean changedChecking;
	private HashSet<Long> set;
	private boolean isChanged(){
		//System.out.println(String.format("check changed:%d",Thread.currentThread().hashCode()));
		try
		{
			//如果已经有线程在检查，则按照没有发生变化的逻辑执行
			if(changedChecking)
				return false;
			changedChecking=true;
			//如果当前时间没有超过上次检查时间3秒，认为没有修改
			if((System.currentTimeMillis()-lastCheckTime)/3000<2)
				return false;
			long lmd=file.lastModified();
			//如果文件的最后修改时间没有变化，就直接返回没有修改
			if(lmd!=lastModified){
				this.lastCheckTime=System.currentTimeMillis();
				return true;
			}
		}
		catch(Exception e){
			//e.printStackTrace();
		}
		finally{
			changedChecking=false;
		}
		return false;
	}
	private void load(){
		//System.out.println(String.format("reload list:%d",Thread.currentThread().hashCode()));
		try
		{
			List<String> lines;
			lines=Utils.readTextFileAllLine(this.file);
			HashSet<Long> temp=new HashSet<Long>();
			for(String s:lines){
				if(!s.isEmpty()){
					InetAddress ip=InetAddress.getByName(s);
					long ipLong=Utils.readLong(ip.getAddress());
					temp.add(ipLong);
				}
			}
			this.set=temp;
			//设置最后修改时间
			this.lastModified=this.file.lastModified();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
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
