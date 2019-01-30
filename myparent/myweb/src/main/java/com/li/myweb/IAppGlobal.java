package com.li.myweb;

public interface IAppGlobal {
	void application_Start(Object sender,Object state);
	void application_BeginRequest(Object sender,Object state);
	void application_EndRequest(Object sender,Object state);
	void application_Error(Object sender,Object state);
	void application_End(Object sender,Object state);
}
