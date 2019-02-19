package com.li.myweb.supports;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.UUID;

import com.li.cson.CSON2;

public class HttpParamterParser {
	// private StringBuffer source;
	private static final Pattern _regex_set = Pattern.compile(
			"^set([A-Za-z]\\w*|_\\w+)$", Pattern.CASE_INSENSITIVE
					| Pattern.UNICODE_CASE);
	private String mapInst;
	private boolean arrayMode;
	private boolean paramOrderd;

	public static Date StrToDate(String time) {
		SimpleDateFormat formatter;
		int tempPos = time.indexOf("AD");
		time = time.trim();
		formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
		if (tempPos > -1) {
			time = time.substring(0, tempPos) + "公元"
					+ time.substring(tempPos + "AD".length());// china
			formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
		}
		tempPos = time.indexOf("-");
		if (tempPos > -1 && (time.indexOf(" ") < 0)) {
			formatter = new SimpleDateFormat("yyyyMMddHHmmssZ");
		} else if ((time.indexOf("/") > -1) && (time.indexOf(" ") > -1)) {
			formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		} else if ((time.indexOf("-") > -1) && (time.indexOf(" ") > -1)) {
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		} else if ((time.indexOf("/") > -1) && (time.indexOf("am") > -1)
				|| (time.indexOf("pm") > -1)) {
			formatter = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a");
		} else if ((time.indexOf("-") > -1) && (time.indexOf("am") > -1)
				|| (time.indexOf("pm") > -1)) {
			formatter = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss a");
		}
		ParsePosition pos = new ParsePosition(0);
		java.util.Date ctime = formatter.parse(time, pos);

		return ctime;
	}

	@SuppressWarnings("rawtypes")
	private String getBasicExpress(Class c) {
		String express = null;
		if (c == String.class || c == Object.class) {
			express = "tmp_pmvalue";
		} else if (c == int.class || c == Integer.class) {
			express = "Integer.parseInt(tmp_pmvalue)";
		} else if (c == short.class || c == Short.class) {
			express = "Short.parseShort(tmp_pmvalue)";
		} else if (c == long.class || c == Long.class) {
			express = "Long.parseLong(tmp_pmvalue)";
		} else if (c == float.class || c == Float.class) {
			express = "Float.parseFloat(tmp_pmvalue)";
		} else if (c == double.class || c == Double.class) {
			express = "Double.parseDouble(tmp_pmvalue)";
		} else if (c == boolean.class || c == Boolean.class) {
			express = "Boolean.parseBoolean(tmp_pmvalue)";
		} else if (c == byte.class || c == Byte.class) {
			express = "Byte.parseByte(tmp_pmvalue)";
		} else if (c == char.class || c == Character.class) {
			express = "tmp_pmvalue.charAt(0)";
		} else if (c == UUID.class) {
			express = "UUID.fromString(tmp_pmvalue)";
		} else if (c == InetAddress.class) {
			express = "InetAddress.getByName(tmp_pmvalue)";
		} else if (c.isEnum()) {
			express = "java.lang.Enum.valueOf(c, tmp_pmvalue)";
		} else if (c == Date.class) {
			express = "com.li.myweb.supports.HttpParamterParser.StrToDate(tmp_pmvalue)";
		} else if (c == CSON2.class) {
			express = "CSON2.read0(tmp_pmvalue)";
		} else {
			try {
				@SuppressWarnings("unchecked")
				Method m = c.getMethod("parse", String.class);
				if (m != null)
					express = c.getName() + ".parse(tmp_pmvalue)";
			} catch (NoSuchMethodException e) {
			}
		}
		return express;
	}

	/*
	 * @SuppressWarnings("unused") private String
	 * getArrayElementClassName(Class<?> arr){ String name=arr.getName();
	 * if(name.startsWith("[L")){ return name.substring(2,name.length()-1);
	 * }else if(name.startsWith("[")){ String tag=name.substring(1);
	 * if(tag.equals("B")){ return "byte"; } else if(tag.equals("S")){ return
	 * "short"; } else if(tag.equals("I")){ return "int"; } else
	 * if(tag.equals("J")){ return "long"; } else if(tag.equals("Z")){ return
	 * "boolean"; } else if(tag.equals("C")){ return "char"; } } return null; }
	 */
	private Class<?> getArrayElementClass(Class<?> arr) {
		try {
			String name = arr.getName();
			if (name.startsWith("[L")) {
				String className = name.substring(2, name.length() - 1);
				ClassLoader loader = arr.getClassLoader();// Thread.currentThread().getContextClassLoader();
				return Class.forName(className, true, loader);
			} else if (name.startsWith("[")) {
				String tag = name.substring(1);
				if (tag.equals("B")) {
					return byte.class;
				} else if (tag.equals("S")) {
					return short.class;
				} else if (tag.equals("I")) {
					return int.class;
				} else if (tag.equals("J")) {
					return long.class;
				} else if (tag.equals("Z")) {
					return boolean.class;
				} else if (tag.equals("C")) {
					return char.class;
				}
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private String getDefinedExpress(Class<?> c, String varname,int a) {
		String def;
		String clsStr=c.getName();
		if (c.isArray()) {
			clsStr=this.getArrayElementClass(c).getName() + "[]";
			def = clsStr+" " + varname
					+ "=null;\r\n";
		} else {
			def = clsStr + " " + varname + "=";
			if (c == int.class || c == short.class || c == long.class
					|| c == float.class || c == double.class || c == byte.class
					|| c == char.class)
				def += "0;\r\n";
			else
				def += "null;\r\n";
			
		}
		def+="_pm_types["+a+"]="+clsStr+".class;\r\n";
		return def;
	}
	private String getPackingClass(Class<?> c){
		String cname=null;
		if(c.isPrimitive()){
			if (c == int.class) {
				cname = "Integer";
			} else if (c == short.class) {
				cname = "Short";
			} else if (c == long.class) {
				cname = "Long";
			} else if (c == float.class) {
				cname = "Float";
			} else if (c == double.class) {
				cname = "Double";
			} else if (c == boolean.class) {
				cname = "Boolean";
			} else if (c == byte.class) {
				cname = "Byte";
			} else if (c == char.class) {
				cname = "Character";
			}
		}else{
			return cname=c.getName();
		}
		return cname;
	}

	/*
	 * private String getBlockExpress(){ StringBuffer srcsb=new StringBuffer();
	 * //srcsb.append("tmp_pmvalues="+mapInst+".get(varexp);\r\n");
	 * if(arrayMode){
	 * srcsb.append("if(tmp_pmvalues!=null&&tmp_pmvalues.length>0){\r\n");
	 * srcsb.append("for(int i=0;i<tmp_pmvalues.length;i++){\r\n");
	 * srcsb.append("tmp_pmvalue=tmp_pmvalues[i];\r\n%s\r\n");
	 * srcsb.append("}\r\n}\r\n"); }else{ srcsb.append(
	 * "if(tmp_pmvalues!=null&&tmp_pmvalues.length>0)\r\ntmp_pmvalue=tmp_pmvalues[0];\r\n"
	 * );
	 * srcsb.append("if(tmp_pmvalue!=null&&tmp_pmvalue.length()>0){\r\n%s\r\n}\r\n"
	 * ); } return srcsb.toString(); }
	 */
	private StringBuffer getSimpleParseSrc(Class<?> cls, String varexp) {
		String express;
		StringBuffer sb = null;
		if (arrayMode)
			cls = this.getArrayElementClass(cls);
		express = this.getBasicExpress(cls);

		if (express != null) {
			sb = new StringBuffer();
			if (this.paramOrderd) {
				sb.append("tmp_pmvalues=_allValues[_i_opm++];\r\n");
			} else {
				sb.append("tmp_pmvalues=" + mapInst + ".get(\"" + varexp
						+ "\");\r\n");
			}
			if (arrayMode) {
				sb.append("if(tmp_pmvalues!=null&&tmp_pmvalues.length>0){\r\n");
				sb.append(varexp + "=new " + cls.getName()
						+ "[tmp_pmvalues.length];\r\n");
				sb.append("for(int _i=0;_i<" + varexp + ".length;_i++){\r\n");
				sb.append("tmp_pmvalue=tmp_pmvalues[_i];\r\n");
				sb.append("if(tmp_pmvalues!=null&&tmp_pmvalues.length>0&&(tmp_pmvalue=tmp_pmvalues[_i])!=null&&tmp_pmvalue.length()>0)\r\n");
				sb.append(varexp + "[_i]=" + express + ";\r\n");
				sb.append("}\r\n");
				sb.append("}\r\n");
			} else {
				sb.append("if(tmp_pmvalues!=null&&tmp_pmvalues.length>0&&(tmp_pmvalue=tmp_pmvalues[0])!=null&&tmp_pmvalue.length()>0)\r\n");
				sb.append(varexp + "=" + express + ";\r\n");
			}
		}
		return sb;
	}

	@SuppressWarnings("rawtypes")
	private StringBuffer getModelParseSrc(Class<?> cls, String varexp) {
		StringBuffer srcsb = new StringBuffer();
		if (arrayMode)
			cls = this.getArrayElementClass(cls);
		String clsname = cls.getName();
		String pclsvar = null;
		if (cls.isMemberClass()) {
			clsname = clsname.replace('$', '.');
			pclsvar = "_p_" + cls.getSimpleName().replace('$', '_');
			Class<?> pcls = cls.getDeclaringClass();
			srcsb.append(pcls.getName() + " _p_" + pclsvar + "=new "
					+ pcls.getName() + "();\r\n");
		}
		// 获取属性列表
		HashMap<String, Object> hm = new HashMap<String, Object>();
		for (Field f : cls.getFields()) {
			hm.put(f.getName().toLowerCase(), f);
		}
		for (Method m : cls.getMethods()) {
			String mname = m.getName();
			if (mname.length() > 3 && _regex_set.matcher(mname).find()
					&& m.getParameterTypes().length == 1
					&& m.getReturnType() == void.class) {
				String name = mname.substring(3).toLowerCase();
				if (!hm.containsKey(name)) {
					hm.put(name, m);
				}
			}
		}
		// 构建表达式

		// 如果传入参数是JSON或CSON情况源码
		if (this.paramOrderd) {
			srcsb.append("tmp_pmvalues=_allValues[_i_opm++];\r\n");
		} else {
			srcsb.append("tmp_pmvalues=" + mapInst + ".get(\"" + varexp
					+ "\");\r\n");
		}
		srcsb.append("if(tmp_pmvalues!=null&&tmp_pmvalues.length>0&&(tmp_pmvalue=tmp_pmvalues[0])!=null&&tmp_pmvalue.length()>0){\r\n");
		srcsb.append("if(tmp_pmvalue.charAt(0)=='{'||tmp_pmvalue.charAt(0)=='['){\r\n");
		// JSON参数
		srcsb.append(varexp + "=(" + clsname + (arrayMode ? "[]" : "")
				+ ")com.li.myweb.Utils.readJson(tmp_pmvalue," + clsname
				+ (arrayMode ? "[].class" : ".class") + ");\r\n");
		srcsb.append("}\r\n");
		// CSON参数
		srcsb.append("else{\r\n");
		srcsb.append(varexp + "=(" + clsname + (arrayMode ? "[]" : "")
				+ ")CSON2.deserialize(tmp_pmvalue," + clsname
				+ (arrayMode ? "[].class" : ".class") + ");\r\n");
		srcsb.append("}\r\n");
		srcsb.append("}\r\n");
		if (!this.paramOrderd) {
			// 如果传入参数非JSON或CSON情况源码
			srcsb.append("else{\r\n");
			for (Entry<String, Object> itm : hm.entrySet()) {
				Object set = itm.getValue();
				Class f_cls;
				if (set instanceof Field) {
					f_cls = ((Field) set).getType();
				} else {
					Method m = (Method) set;
					f_cls = m.getParameterTypes()[0];
				}
				String express = this.getBasicExpress(f_cls);
				if (express != null) {
					srcsb.append("tmp_pmvalues=" + mapInst + ".get(\"" + varexp
							+ "." + itm.getKey() + "\");\r\n");
					if (arrayMode) {
						srcsb.append("if(tmp_pmvalues!=null&&tmp_pmvalues.length>0){\r\n");
						srcsb.append("if(" + varexp + "==null){\r\n");
						srcsb.append(varexp + "=new " + clsname
								+ "[tmp_pmvalues.length];\r\n");
						srcsb.append("}\r\n");
						srcsb.append("for(int _i=0;_i<tmp_pmvalues.length;_i++){\r\n");
						srcsb.append("if((tmp_pmvalue=tmp_pmvalues[_i])!=null){\r\n");
						srcsb.append("if(" + varexp + "[_i]==null)\r\n");
						if (cls.isMemberClass()) {
							srcsb.append(varexp + "[_i]=" + pclsvar + ".new "
									+ cls.getSimpleName() + "();\r\n");
						} else {
							srcsb.append(varexp + "[_i]=new " + clsname
									+ "();\r\n");
						}
						if (set instanceof Field)
							srcsb.append(varexp + "[_i]."
									+ ((Field) set).getName() + "=" + express
									+ ";\r\n");
						else
							srcsb.append(varexp + "[_i]."
									+ ((Method) set).getName() + "(" + express
									+ ");\r\n");
						srcsb.append("}\r\n");
						srcsb.append("}\r\n");
						srcsb.append("}\r\n");
					} else {
						srcsb.append("if(tmp_pmvalues!=null&&tmp_pmvalues.length>0&&(tmp_pmvalue=tmp_pmvalues[0])!=null&&tmp_pmvalue.length()>0){\r\n");
						srcsb.append("if(" + varexp + "==null)\r\n");
						if (cls.isMemberClass()) {
							srcsb.append(varexp + "=" + pclsvar + ".new "
									+ cls.getSimpleName() + "();\r\n");
						} else {
							srcsb.append(varexp + "=new " + clsname + "();\r\n");
						}
						if (set instanceof Field)
							srcsb.append(varexp + "." + ((Field) set).getName()
									+ "=" + express + ";\r\n");
						else
							srcsb.append(varexp + "."
									+ ((Method) set).getName() + "(" + express
									+ ");\r\n");
						srcsb.append("}\r\n");
					}
				}
			}
			srcsb.append("}\r\n");
		}
		return srcsb;

	}

	/*
	 * private String getArrayParseSrc(Class<?> arr,String varexp){ Class<?>
	 * ecls=this.getArrayElementClass(arr); String express;
	 * if((express=this.getSimpleParseSrc(ecls, varexp+"[i]"))==null)
	 * express=this.getModelParseSrc(ecls, varexp+"[i]"); return express; }
	 */
	public String getSourceCode(Class<?> cls, String name, String mapname) {
		return this.getSourceCode(new Class<?>[] { cls },
				new String[] { name }, mapname);
	}

	public String getSourceCode(Class<?>[] clsList, String[] nameList,
			String mapname) {
		StringBuffer source = new StringBuffer();
		this.mapInst = mapname;
		source.append("Class<?>[] _pm_types=new Class<?>["+clsList.length+"];\r\n");
		for (int i = 0; i < clsList.length; i++) {
			source.append(this.getDefinedExpress(clsList[i], nameList[i],i));
		}
		// 定义变量
		source.append("String tmp_pmvalue=null;\r\n");
		source.append("String[] tmp_pmvalues=null;\r\n");
		// 处理CSON|JSON整体参数
		source.append("if((tmp_pmvalues="+mapname+".get(\"_csonpm\"))!=null&&tmp_pmvalues.length>0&&(tmp_pmvalue=tmp_pmvalues[0])!=null&&tmp_pmvalue.length()>0){\r\n");
		source.append("String _csonpm=tmp_pmvalue;\r\n");
		source.append("Object[] _pm_objs=Utils.readCson(_csonpm,_pm_types);\r\n");
		for(int i=0;i<nameList.length;i++){
			source.append(nameList[i]+"=("+(clsList[i].isArray()?this.getArrayElementClass(clsList[i]).getName() + "[]":this.getPackingClass(clsList[i]))+")_pm_objs["+i+"];\r\n");
		}
		source.append("}else if((tmp_pmvalues="+mapname+".get(\"_jsonpm\"))!=null&&tmp_pmvalues.length>0&&(tmp_pmvalue=tmp_pmvalues[0])!=null&&tmp_pmvalue.length()>0){\r\n");
		source.append("String _jsonpm=tmp_pmvalue;\r\n");
		source.append("Object[] _pm_objs=Utils.readJson(_jsonpm,_pm_types);\r\n");
		for(int i=0;i<nameList.length;i++){
			source.append(nameList[i]+"=("+(clsList[i].isArray()?this.getArrayElementClass(clsList[i]).getName() + "[]":this.getPackingClass(clsList[i]))+")_pm_objs["+i+"];\r\n");
		}
		source.append("}else{\r\n");
		
		this.paramOrderd=nameList[0].equals("_orderdpm0");
		if (this.paramOrderd) {
			source.append("String[][] _allValues=new String[" + nameList.length
					+ "][];\r\n");
			source.append("int _i_opm=0;\r\n");
			source.append("for(java.util.Map.Entry<String,String[]> itm:"
					+ this.mapInst + ".entrySet()){\r\n");
			source.append("if(itm.getKey().equalsIgnoreCase(\"_action\"))\r\ncontinue;\r\n");
			source.append("_allValues[_i_opm++]=itm.getValue();\r\n");
			source.append("}\r\n");
			source.append("_i_opm=0;\r\n");
		}
		
		// 获取变量赋值源码
		StringBuffer express = null;
		for (int i = 0; i < clsList.length; i++) {
			Class<?> cls = clsList[i];
			String name = nameList[i];
			if (cls.isArray()) {
				this.arrayMode = true;
			} else {
				this.arrayMode = false;
			}

			if ((express = this.getSimpleParseSrc(cls, name)) == null
					|| express.length() == 0) {
				express = this.getModelParseSrc(cls, name);
			}
			source.append(express);
		}
		source.append("}\r\n");
		return source.toString();
	}
}
