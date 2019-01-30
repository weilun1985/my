package com.li.myweb;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

public class HttpApplication implements ServletContext {

	public HttpApplication(ServletContext context){
		this.context=context;
		try{
			Class<?> gcls=Class.forName("Global",true,Thread.currentThread().getContextClassLoader());
			global= (IAppGlobal) gcls.newInstance();
		}
		catch(ClassNotFoundException e){
		} 
		catch (InstantiationException e) {
			
		} 
		catch (IllegalAccessException e) {

		}
		this.context.setAttribute(HttpRuntime.APP_ATTR_APP, this);
	}
	private ServletContext context;
	private IAppGlobal global;
	
	@Override
	public Dynamic addFilter(String arg0, String arg1) {
		return this.context.addFilter(arg0, arg1);
	}

	@Override
	public Dynamic addFilter(String arg0, Filter arg1) {
		return this.context.addFilter(arg0, arg1);
	}

	@Override
	public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
		return this.context.addFilter(arg0, arg1);
	}

	@Override
	public void addListener(Class<? extends EventListener> arg0) {
	    this.context.addListener(arg0);
	}

	@Override
	public void addListener(String arg0) {
		this.context.addListener(arg0);
	}

	@Override
	public <T extends EventListener> void addListener(T arg0) {
		this.context.addListener(arg0);
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			String arg1) {
		return this.context.addServlet(arg0, arg1);
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Servlet arg1) {
		return this.context.addServlet(arg0, arg1);
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Class<? extends Servlet> arg1) {
		return this.context.addServlet(arg0, arg1);
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> arg0)
			throws ServletException {
		return this.context.createFilter(arg0);
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> arg0)
			throws ServletException {
		return this.context.createListener(arg0);
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> arg0)
			throws ServletException {
		return this.context.createServlet(arg0);
	}

	@Override
	public void declareRoles(String... arg0) {
		this.context.declareRoles(arg0);
	}

	@Override
	public Object getAttribute(String arg0) {
		return this.context.getAttribute(arg0);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return this.context.getAttributeNames();
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.context.getClassLoader();
	}

	@Override
	public ServletContext getContext(String arg0) {
		return this.context.getContext(arg0);
	}

	@Override
	public String getContextPath() {
		return this.context.getContextPath();
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return this.context.getDefaultSessionTrackingModes();
	}

	@Override
	public int getEffectiveMajorVersion() {
		return this.context.getEffectiveMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return this.context.getEffectiveMinorVersion();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return this.context.getEffectiveSessionTrackingModes();
	}

	@Override
	public FilterRegistration getFilterRegistration(String arg0) {
		return this.context.getFilterRegistration(arg0);
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return this.context.getFilterRegistrations();
	}

	@Override
	public String getInitParameter(String arg0) {
		return this.context.getInitParameter(arg0);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return this.context.getInitParameterNames();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return this.context.getJspConfigDescriptor();
	}

	@Override
	public int getMajorVersion() {
		return this.context.getMajorVersion();
	}

	@Override
	public String getMimeType(String arg0) {
		return this.context.getMimeType(arg0);
	}

	@Override
	public int getMinorVersion() {
		return this.context.getMinorVersion();
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return this.context.getNamedDispatcher(arg0);
	}

	@Override
	public String getRealPath(String arg0) {
		return this.context.getRealPath(arg0);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return this.context.getRequestDispatcher(arg0);
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		return this.context.getResource(arg0);
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		return this.context.getResourceAsStream(arg0);
	}

	@Override
	public Set<String> getResourcePaths(String arg0) {
		return this.context.getResourcePaths(arg0);
	}

	@Override
	public String getServerInfo() {
		return this.context.getServerInfo();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		return this.context.getServlet(arg0);
	}

	@Override
	public String getServletContextName() {
		return this.context.getServletContextName();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Enumeration<String> getServletNames() {
		return this.context.getServletNames();
	}

	@Override
	public ServletRegistration getServletRegistration(String arg0) {
		return this.context.getServletRegistration(arg0);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return this.context.getServletRegistrations();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Enumeration<Servlet> getServlets() {
		return this.context.getServlets();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return this.context.getSessionCookieConfig();
	}

	@Override
	public void log(String arg0) {
		this.context.log(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void log(Exception arg0, String arg1) {
		this.context.log(arg0,arg1);
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		this.context.log(arg0, arg1);
	}

	@Override
	public void removeAttribute(String arg0) {
		this.context.removeAttribute(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		this.context.setAttribute(arg0, arg1);
	}

	@Override
	public boolean setInitParameter(String arg0, String arg1) {
		return this.context.setInitParameter(arg0, arg1);
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> arg0)
			throws IllegalStateException, IllegalArgumentException {
		this.context.setSessionTrackingModes(arg0);
	}

	public ServletContext getServletContext(){
		return this.context;
	}
	
	public void start(Object state){
		if(this.global!=null)
			this.global.application_Start(this, state);
	}
	public void end(Object state){
		if(this.global!=null)
			this.global.application_End(this, state);
	}
	
	public void beginRequest(Object state){
		if(this.global!=null)
			this.global.application_BeginRequest(this, state);
	}
	public void endRequest(Object state){
		if(this.global!=null)
			this.global.application_EndRequest(this, state);
	}
	public void error(Object state){
		if(this.global!=null)
			this.global.application_Error(this, state);
	}
	
}
