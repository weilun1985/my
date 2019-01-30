package com.li.myweb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.fileupload.FileItem;

public class HttpUploadedFile {
	public HttpUploadedFile(FileItem file){
		this.file=file;
	}
	private FileItem file;
	public String getFileName(){
		return this.file.getName();
	}
	public String getFieldName(){
		return this.file.getFieldName();
	}
	public long getSize(){
		return this.file.getSize();
	}
	public OutputStream getOutputStream() throws IOException{
		return this.file.getOutputStream();
	}
	public InputStream getInputStream() throws IOException{
		return this.file.getInputStream();
	}
	public String getContentType(){
		return this.file.getContentType();
	}
	public void save(String path){
		File diskfile=new File(path);
		try {
			this.file.write(diskfile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
