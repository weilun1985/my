package com.li.myweb.supports;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
//import java.util.Date;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServlet;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.li.core.DynamicCompiler;
import com.li.myweb.ActionGroupImpl;
import com.li.myweb.HttpMethod;
import com.li.myweb.HttpRequest;
import com.li.myweb.HttpServerUtility;
import com.li.myweb.annotation.CSON;
import com.li.myweb.annotation.JSON;
import com.li.myweb.annotation.Model;

public class ActionCallerManager {
	public ActionCallerManager(HttpServerUtility server)
			throws UnsupportedEncodingException {
		this.compiler = new DynamicCompiler();
		/*
		 * String
		 * path=HttpServlet.class.getProtectionDomain().getCodeSource().getLocation
		 * ().getFile(); int i_a=path.lastIndexOf("/"); String
		 * root=path.substring(0, i_a+1);
		 * root=URLDecoder.decode(root,System.getProperty("file.encoding"));
		 * this.compiler.appendClassPath(root);
		 */
		this.appendClasspath(HttpServlet.class);
		this.appendClasspath(this.getClass());
		this.compiledTable = new ConcurrentHashMap<String, Class<?>>();
		this.server = server;
	}

	private final DynamicCompiler compiler;
	private final ConcurrentHashMap<String, Class<?>> compiledTable;
	private final HttpServerUtility server;

	private void appendClasspath(Class<?> cls) {
		ProtectionDomain pdomain = cls.getProtectionDomain();
		CodeSource codeSource;
		URL codeLocation;
		if (cls.isPrimitive() || pdomain == null
				|| (codeSource = pdomain.getCodeSource()) == null
				|| (codeLocation = codeSource.getLocation()) == null)
			return;
		String path = codeLocation.getFile();
		if (path == null)
			return;
		int i_a = path.lastIndexOf("/");
		String root = path.substring(0, i_a + 1);
		try {
			root = URLDecoder.decode(root, System.getProperty("file.encoding"));
			this.compiler.appendClassPath(root);
		} catch (UnsupportedEncodingException e) {
		}
	}

	public ActionCaller getCaller(HttpRequest req) throws Exception {
		String effecPath = this.server.getEffectivePath(req.getRequestURI())
				.toLowerCase();
		String actionName = req.getAction().toLowerCase();
		String acwLocKey = null;
		// 当前路径:方法名
		acwLocKey = effecPath + ":" + actionName;
		String clsFullName;
		Class<?> cls = this.compiledTable.get(acwLocKey);
		if (cls == null) {
			ActionWrapper actw = null;
			actw = this.server.getAction0((HttpRequest) req);
			clsFullName = String.format("stormweb.actions.%s_%s", actw.classi
					.getName().replace('.', '_'), actw.method.getName());
			cls = this.compiledTable.get(clsFullName);
			if (cls == null) {
				synchronized (this.compiledTable) {
					if (!this.compiledTable.containsKey(clsFullName)) {
						// 先编译master
						ActionWrapper mactw = actw.getMasterActionWrapper();
						if (mactw != null) {
							do {
								String mclsFname = String.format(
										"stormweb.actions.%s_%s", mactw.classi
												.getName().replace('.', '_'),
										mactw.method.getName());
								if (this.compiledTable.containsKey(mclsFname))
									break;
								try {
									Class<?> mcls = this.doComplie(mactw,
											mclsFname);
									mcls = this.compiledTable.putIfAbsent(
											mclsFname, mcls);
									if (mcls == null)
										break;
									mactw = mactw.getMasterActionWrapper();
								} catch (java.lang.LinkageError e) {
									break;
								}
							} while (mactw != null);
						}

						// String
						// clsFullName=String.format("stormweb.actions.%s_%s",actw.classi.getName().replace('.','_'),actw.method.getName());
						cls = this.doComplie(actw, clsFullName);

						Class<?> cls2 = this.compiledTable.putIfAbsent(
								acwLocKey, cls);
						if (cls2 != null) {
							cls = cls2;
						} else {
							this.compiledTable.put(clsFullName, cls);
							// 添加缓存策略
							this.server.RegisterActionOutCache(req, actw);
						}
					} else {
						cls = this.compiledTable.get(clsFullName);
					}
				}
			} else {
				this.compiledTable.putIfAbsent(acwLocKey, cls);
			}
		}
		return (ActionCaller) cls.newInstance();

	}

	private Object getSpringBean(Class<?> cls) {
		String beanName = null;
		org.springframework.stereotype.Service spring_service;
		org.springframework.stereotype.Component spring_component;
		org.springframework.stereotype.Controller spring_controller;
		org.springframework.stereotype.Repository spring_repository;
		spring_service = cls
				.getAnnotation(org.springframework.stereotype.Service.class);
		spring_component = cls
				.getAnnotation(org.springframework.stereotype.Component.class);
		spring_controller = cls
				.getAnnotation(org.springframework.stereotype.Controller.class);
		spring_repository = cls
				.getAnnotation(org.springframework.stereotype.Repository.class);
		if (spring_service != null) {
			beanName = spring_service.value();
		} else if (spring_component != null) {
			beanName = spring_component.value();
		} else if (spring_controller != null) {
			beanName = spring_controller.value();
		} else if (spring_repository != null) {
			beanName = spring_repository.value();
		}else{
			return null;
		}
		if(beanName==null||beanName.length()==0)
			return cls;
		return beanName;
		 /*if(bean!=null){ WebApplicationContext
		 app=WebApplicationContextUtils.getWebApplicationContext
		 (eagle.web.HttpContext.current().application().getServletContext());
		 app.getBean(bean); }*/

	}

	private Class<?> doComplie(ActionWrapper aw, String fullName) {
		String packageName = null;
		String simpleName = null;
		int mi = fullName.lastIndexOf('.');
		if (mi != -1) {
			packageName = fullName.substring(0, mi);
			simpleName = fullName.substring(mi + 1);
		}
		Class<?> cls = aw.classi;
		this.appendClasspath(cls);

		StringBuffer srcsb = new StringBuffer();
		srcsb.append("package " + packageName + ";\r\n");
		srcsb.append("import java.io.IOException;\r\n");
		srcsb.append("import java.util.*;\r\n");
		srcsb.append("import java.io.Writer;\r\n");
		srcsb.append("import java.io.StringWriter;\r\n");
		srcsb.append("import com.li.myweb.exceptions.*;\r\n");
		srcsb.append("import com.li.myweb.*;\r\n");
		srcsb.append("import com.li.myweb.supports.*;\r\n");
		//srcsb.append("import com.li.myweb.exceptions.ActionFlushImme;\r\n");
		//srcsb.append("import com.li.myweb.exceptions.ActionInterrupt;\r\n");
		//srcsb.append("import com.li.myweb.HttpRequest;\r\n");
		//srcsb.append("import com.li.myweb.HttpResponse;\r\n");
		//srcsb.append("import com.li.myweb.HttpContext;\r\n");
		//srcsb.append("import com.li.myweb.supports.ActionCaller;\r\n");
		srcsb.append("import com.li.cson.CSON2;\r\n");
		//srcsb.append("import org.springframework.web.context.WebApplicationContext;\r\n");
		//srcsb.append("import org.springframework.web.context.support.WebApplicationContextUtils;\r\n");
		srcsb.append("\r\n");
		srcsb.append("public class " + simpleName
				+ " extends ActionCaller {\r\n");

		// 判断是否是CSON、JSON输出
		CSON cson = aw.method.getAnnotation(CSON.class);
		JSON json = aw.method.getAnnotation(JSON.class);
		boolean dataonly = (cson != null || json != null);
		// 如果是数据输出且无返回值或者是ActionGroupImpl的派生类直接抛出
		if (dataonly
				&& (ActionGroupImpl.class.isAssignableFrom(aw.classi) || aw.method
				.getReturnType() == void.class)) {
			if (cson != null || json != null) {
				throw new RuntimeException(
						"ActionGroupImpl或ActionGroupImplWithMaster的派生类及无返回值的Action无法应用@CSON或@JSON注解，请移除。action="
								+ aw.method.toString());
			}
		}

		// 变量定义部分
		if (aw.templateout && !dataonly) {
			srcsb.append("private static final String template_path=\""
					+ aw.templatePath.replace("\\", "\\\\") + "\";\r\n");
		}
		srcsb.append(cls.getName() + " inst;\r\n");
		String master;
		ActionWrapper mactw = aw.getMasterActionWrapper();
		if (mactw != null) {
			master = "stormweb.actions."
					+ mactw.classi.getName().replace('.', '_') + "_"
					+ mactw.method.getName();
			srcsb.append(master + " _master=new " + master + "();\r\n");
		}

		// 构造函数部分
		srcsb.append("public " + simpleName + "(){\r\n");
		srcsb.append("}\r\n");

		// Action执行部分
		srcsb.append("//transfer " + aw.method.getName() + "\r\n");
		srcsb.append("public void call() throws Throwable{\r\n");
		srcsb.append("this.resp.setCurrentCaller(this);\r\n");
		srcsb.append(String
				.format("this.req.setAttribute(HttpRuntime.REQATTR_EXECCURRENT,\"%s@%s\");\r\n",
						aw.classi.getName(), aw.method.getName()));
		if (!aw.useAsMaster()) {
			// 添加Http请求方法验证
			srcsb.append("if(!(");
			int a = srcsb.length();
			if (aw.allow(HttpMethod.GET)) {
				srcsb.append("_http_method.equalsIgnoreCase(\""
						+ HttpMethod.GET + "\")");
			}
			if (aw.allow(HttpMethod.POST)) {
				if (srcsb.length() > a)
					srcsb.append("||");
				srcsb.append("_http_method.equalsIgnoreCase(\""
						+ HttpMethod.POST + "\")");
			}
			if (aw.allow(HttpMethod.HEAD)) {
				if (srcsb.length() > a)
					srcsb.append("||");
				srcsb.append("_http_method.equalsIgnoreCase(\""
						+ HttpMethod.HEAD + "\")");
			}
			if (aw.allow(HttpMethod.PUT)) {
				if (srcsb.length() > a)
					srcsb.append("||");
				srcsb.append("_http_method.equalsIgnoreCase(\""
						+ HttpMethod.PUT + "\")");
			}
			if (aw.allow(HttpMethod.TRACE)) {
				if (srcsb.length() > a)
					srcsb.append("||");
				srcsb.append("_http_method.equalsIgnoreCase(\""
						+ HttpMethod.TRACE + "\")");
			}
			if (aw.allow(HttpMethod.OPTIONS)) {
				if (srcsb.length() > a)
					srcsb.append("||");
				srcsb.append("_http_method.equalsIgnoreCase(\""
						+ HttpMethod.OPTIONS + "\")");
			}
			if (aw.allow(HttpMethod.DELETE)) {
				if (srcsb.length() > a)
					srcsb.append("||");
				srcsb.append("_http_method.equalsIgnoreCase(\""
						+ HttpMethod.DELETE + "\")");
			}
			srcsb.append(")){\r\n");
			srcsb.append("resp.sendError(405);\r\n");
			srcsb.append("return;\r\n");
			srcsb.append("}\r\n");
		}
		srcsb.append("inst=new " + cls.getName() + "();\r\n");
//		// Spring
//		Object beanObj=this.getSpringBean(cls);
//		if (beanObj == null) {
//			// 实例化与调用动作
//			srcsb.append("inst=new " + cls.getName() + "();\r\n");
//		} else {
//			// 调用Spring getBean()
//			srcsb.append("WebApplicationContext app=WebApplicationContextUtils.getWebApplicationContext(HttpContext.current().application().getServletContext());\r\n");
//			if(beanObj instanceof String){
//				srcsb.append("inst=(" + cls.getName() + ")app.getBean(\"" + beanObj.toString()
//						+ "\");\r\n");
//			}else{
//				srcsb.append("inst=(" + cls.getName() + ")app.getBean(" + cls.getName()
//						+ ".class);\r\n");
//			}
//		}

		if (!aw.useAsMaster()) {
			// 判断缓存
			if (aw.supportEtag || aw.supportLastModifiy) {
				srcsb.append("if(_http_method.equalsIgnoreCase(\""
						+ HttpMethod.GET
						+ "\")||_http_method.equalsIgnoreCase(\""
						+ HttpMethod.HEAD + "\")){\r\n");
				if (aw.supportEtag) {
					srcsb.append("String etag1=this.req.getIfNoneMatch();\r\n");
					srcsb.append("String etag2=inst.getETag();\r\n");
					srcsb.append("if(etag1!=null&&etag1.length()>0&&etag2!=null&&etag2.length()>0&&etag2.equals(etag1)){\r\n");
					srcsb.append("resp.setStatus(304);\r\n");
					srcsb.append("resp.setHeader(\"Etag\",etag2);\r\n");
					srcsb.append("return;\r\n");
					srcsb.append("}\r\n");
					srcsb.append("if(etag2!=null&&etag2.length()>0){\r\n");
					srcsb.append("resp.setHeader(\"Etag\",etag2);\r\n");
					srcsb.append("}\r\n");
				}
				if (aw.supportLastModifiy) {
					srcsb.append("Date lastModified1=req.getIfModifiedSince();\r\n");
					srcsb.append("Date lastModified2=inst.getLastModified();\r\n");
					srcsb.append("if(lastModified1!=null&&lastModified2!=null&&lastModified2.getTime()==lastModified1.getTime()){\r\n");
					srcsb.append("resp.setStatus(304);\r\n");
					srcsb.append("resp.setLastModified(lastModified2);\r\n");
					srcsb.append("return;\r\n");
					srcsb.append("}\r\n");
					srcsb.append("if(lastModified2!=null){\r\n");
					srcsb.append("resp.setLastModified(lastModified2);\r\n");
					srcsb.append("}\r\n");
				}
				srcsb.append("}\r\n");
			}
		}

		srcsb.append("boolean _flushImme=false;\r\n");

		// 如果是ActionGroupImpl的子类，执行init()
		if (ActionGroupImpl.class.isAssignableFrom(cls)) {
			srcsb.append("inst.init();\r\n");
		}
		getActionExcuteSC(aw, srcsb);
		// 如果有母版，执行母版action
		if (mactw != null) {
			srcsb.append("_master.call();\r\n");
		}
		srcsb.append("}\r\n");

		// 输出部分方法代码
		srcsb.append("public void render() throws Throwable{\r\n");
		// 获取模板
		if (aw.templateout && !dataonly) {
			if (mactw != null) {
				srcsb.append("_writer=new java.io.StringWriter();\r\n");
			} else {
				srcsb.append("_writer=this.resp.getWriter();\r\n");
			}
			if (ActionGroupImpl.class.isAssignableFrom(aw.classi)) {
				srcsb.append("inst.mergeOut(this.server.getTemplate(template_path),_writer);\r\n");
			} else {
				srcsb.append("this.server.getTemplate(template_path).render(this._outmap,_writer);\r\n");
			}
			if (mactw != null) {
				srcsb.append("String context=((StringWriter)_writer).getBuffer().toString();\r\n");
				srcsb.append(String.format("_master.add(\"%s\",context);\r\n",
						aw.getMasterContextLabel()));
				srcsb.append("_master.render();\r\n");
			}
		} else {

			srcsb.append("String accept=this.req.getHeader(\"Accept\");\r\n");
			srcsb.append("if(accept!=null&&accept.equalsIgnoreCase(\"CSONB\")){\r\n");
			srcsb.append("this.resp.setHeader(\"Binding\",\"csonb\");\r\n");
			srcsb.append("CSON2.serialize(_result,this.resp.getOutputStream(),true);\r\n");
			srcsb.append("}else if(accept!=null&&accept.equalsIgnoreCase(\"CSONS\")){\r\n");
			srcsb.append("this.resp.setHeader(\"Binding\",\"csons\");\r\n");
			srcsb.append("this.resp.write(CSON2.serializeBase64(_result,true));\r\n");
			srcsb.append("}else if(accept!=null&&accept.equalsIgnoreCase(\"JSON\")){\r\n");
			srcsb.append("this.resp.setHeader(\"Binding\",\"json\");\r\n");
			srcsb.append("this.resp.write(Utils.writeJson(_result));\r\n");
			srcsb.append("}else{\r\n");
			if (dataonly) {
				if (cson != null) {
					if (cson.binary()) {
						srcsb.append("this.resp.setContentType(\"text/html\");\r\n");
						srcsb.append("this.resp.setHeader(\"Binding\",\"csonb\");\r\n");
						srcsb.append("CSON2.serialize(_result,this.resp.getOutputStream(),"
								+ (cson.compressed() ? "true" : "false")
								+ ");\r\n");
					} else {
						srcsb.append("this.resp.setContentType(\"text/html\");\r\n");
						srcsb.append("this.resp.setHeader(\"Binding\",\"csons\");\r\n");
						srcsb.append("this.resp.write(CSON2.serializeBase64(_result,"
								+ (cson.compressed() ? "true" : "false")
								+ "));\r\n");
					}
				} else if (json != null) {
					srcsb.append("this.resp.setContentType(\"text/html\");\r\n");
					srcsb.append("this.resp.setHeader(\"Binding\",\"json\");\r\n");
					srcsb.append("this.resp.write(Utils.writeJson(_result));\r\n");
				}
			} else {

				srcsb.append("if(_result!=null) this.resp.writeln(_result.toString());\r\n");
			}
			srcsb.append("}\r\n");
		}
		srcsb.append("}\r\n");
		srcsb.append("}\r\n");
		// 动态编译
		try {
			Object result = this.compiler
					.doCompiler(fullName, srcsb.toString());
			// if(result instanceof String){
			// throw new RuntimeException(result.toString());
			// }
			// else{
			Class<?> scls = (Class<?>) result;
			return scls;
			// }
			// Class<?> scls=this.compiler.doCompile(callerFName,
			// srcsb.toString());
			// return scls;
		} catch (Throwable ex) {
			if (ex instanceof RuntimeException)
				throw (RuntimeException) ex;
			throw new RuntimeException("编译失败：" + fullName, ex);
		}
	}

	private void getActionExcuteSC(ActionWrapper action, StringBuffer srcsb) {
		Class<?> rcls = action.method.getReturnType();

		/*
		 * if(rcls!=void.class){
		 * srcsb.append("Map _outmap=new java.util.HashMap<String,Object>();\r\n"
		 * ); }
		 */
		srcsb.append("if(_flushImme){\r\n");
		srcsb.append("return;\r\n");
		srcsb.append("}\r\n");
		// 添加参数声明
		StringBuffer pmexpress = new StringBuffer();
		String[] paramNames1 = action.paramNames;
		boolean orderdpm = false;
		// 如果paramNames1为null，且paramTypes不为null，表示不设置@ParamName注解，应用参数传入顺序匹配原则
		if (paramNames1 == null && action.paramTypes != null
				&& action.paramTypes.length > 0) {
			paramNames1 = new String[action.paramTypes.length];
			for (int i = 0; i < paramNames1.length; i++) {
				paramNames1[i] = "_orderdpm" + i;
			}
			orderdpm = true;
		}
		if (paramNames1 != null && paramNames1.length > 0) {
			for (int i = 0; i < action.paramTypes.length; i++) {
				if (pmexpress.length() > 0)
					pmexpress.append(",");
				pmexpress.append(paramNames1[i]);
				this.appendClasspath(action.paramTypes[i]);
			}
			// 参数转换源码
			HttpParamterParser pmparser = new HttpParamterParser();
			String csrc = pmparser.getSourceCode(action.paramTypes,
					paramNames1, "_pm_map");
			srcsb.append(csrc);
		}

		/*
		 * //判断是否是CSON、JSON输出 CSON cson=action.method.getAnnotation(CSON.class);
		 * JSON json=action.method.getAnnotation(JSON.class);
		 */

		// 如果是ActionGroupImpl的子类
		if (ActionGroupImpl.class.isAssignableFrom(action.classi)
				|| rcls == void.class) {
			srcsb.append("inst." + action.method.getName() + "(" + pmexpress
					+ ");\r\n");
			/*
			 * if(cson!=null||json!=null){ throw new RuntimeException(
			 * "ActionGroupImpl或ActionGroupImplWithMaster的派生类及无返回值的Action无法应用@CSON或@JSON注解，请移除。action="
			 * +action.method.toString()); }
			 */
		} else {
			this.appendClasspath(rcls);
			srcsb.append("_result=inst." + action.method.getName() + "("
					+ pmexpress + ");\r\n");
			/*
			 * if(cson!=null||json!=null){ if(cson!=null){ if(cson.binary())
			 * srcsb.append(
			 * "eagle.cson.CSON2.serialize(_result,this.resp.getOutputStream(),"
			 * +(cson.compressed()?"true":"false")+");\r\n"); else srcsb.append(
			 * "this.resp.write(eagle.cson.CSON2.serializeBase64(_result,"
			 * +(cson.compressed()?"true":"false")+"));\r\n"); } else
			 * if(json!=null){ srcsb.append(
			 * "this.resp.write(eagle.web.Utils.writeJson(_result));\r\n"); }
			 * //srcsb.append("this.resp.end();\r\n"); } else{
			 */
			if (action.templateout) {
				if (java.util.Map.class.isAssignableFrom(rcls)) {
					srcsb.append("if(_result!=null){\r\n");
					srcsb.append("this.addAll((Map)_result);\r\n");
					srcsb.append("}\r\n");
				} else {
					// 判断是否有@Model注解
					Model model = action.method.getAnnotation(Model.class);
					if (model == null)
						srcsb.append("this.add(\"" + Model.DEFAULT
								+ "\", _result);\r\n");
					else
						srcsb.append("this.add(\"" + model.value()
								+ "\", _result);\r\n");
				}
			}
			/* } */
		}
	}
}
