package com.li.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class DynamicCompiler {
	private final URLClassLoader parentClassLoader;
    //private String classpath;
    //private final StringBuilder classpathBuilder;
    //private final Map<String,Class> compileds;
    private final JavaCompiler compiler;
    private final DynamicClassLoader dynamicClassLoader;
    private final DiagnosticCollector<JavaFileObject> diagnostics;
    private final ClassFileManager fileManager;
    private final List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
    private final HashSet<String> classpathes;
    private final HashSet<String> classpathsrces;
    public DynamicCompiler() {
        this.parentClassLoader = (URLClassLoader) this.getClass().getClassLoader();
        //this.buildClassPath();
        /*classpathBuilder=new StringBuilder();
        for (URL url : this.parentClassLoader.getURLs()) {
            String p = url.getFile();
            classpathBuilder.append(p).append(File.pathSeparator);
        }*/
        classpathes=new HashSet<String>();
        classpathsrces=new HashSet<String>();
        for (URL url : this.parentClassLoader.getURLs()) {
            String p = url.getFile();
            classpathes.add(p);
        }
        compiler = ToolProvider.getSystemJavaCompiler();
        //compileds=new ConcurrentHashMap<String,Class>();
        dynamicClassLoader=new DynamicClassLoader(this.parentClassLoader);
        diagnostics = new DiagnosticCollector<JavaFileObject>();
        fileManager = new ClassFileManager(compiler.getStandardFileManager(diagnostics, null, null));
    }
   /* private void buildClassPath() {
        this.classpath = null;
        StringBuilder sb = new StringBuilder();
        for (URL url : this.parentClassLoader.getURLs()) {
            String p = url.getFile();
            sb.append(p).append(File.pathSeparator);
        }
        this.classpath = sb.toString();
    }*/
    java.io.FileFilter filter=new java.io.FileFilter(){
    	@Override
		public boolean accept(File pathname) {
			// TODO Auto-generated method stub
			if(pathname.isFile()&&pathname.getName().toLowerCase().endsWith(".jar"))
				return true;
			return false;
		}
		};
    public synchronized void appendClassPath(String ... paths){
    	for(String path:paths){
    		if(this.classpathsrces.contains(path))
    			continue;
    		File file=new File(path);
    		if(file.isFile()&&(path.endsWith(".jar")||path.endsWith(".class"))){
    			//classpathBuilder.append(path).append(File.pathSeparator);
    			this.classpathes.add(path);
    		}
    		else if(file.isDirectory()){
    			File[] jars=file.listFiles(filter);
                for(File jar:jars){
                	//classpathBuilder.append(jar).append(File.pathSeparator);
                	this.classpathes.add(jar.getPath());
                }
                this.classpathes.add(path);
    		}
    		this.classpathsrces.add(path);
    	}
    }
    
    @SuppressWarnings("rawtypes")
	public Class doCompiler(String fullClassName, String javaCode) 
    throws IllegalAccessException, InstantiationException, CompilerException {
    	
        List<String> options = new ArrayList<String>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-classpath");
        StringBuffer classpath=new StringBuffer();
        for(String str:this.classpathes){
        	classpath.append(str).append(File.pathSeparator);
        }
        //options.add(this.classpathBuilder.toString());
        options.add(classpath.toString());
        boolean success;
        List<Diagnostic<? extends JavaFileObject>> dialist;
        Class clazz=null;
        synchronized(this.jfiles){
        	dialist=this.diagnostics.getDiagnostics();
        	int dia_0=dialist.size();
            jfiles.add(new CharSequenceJavaFileObject(fullClassName, javaCode));
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, jfiles);
            success = task.call();
            if(!success){
            	jfiles.remove(jfiles.size()-1);
            	int dia_1=dialist.size();
            	dialist=this.diagnostics.getDiagnostics().subList(dia_0, dia_1);
            }
            else{
            	JavaClassObject jco = fileManager.getJavaClassObject();
                clazz = dynamicClassLoader.loadClass(fullClassName,jco);
            }
        }
        //String error=null;
        if (success) {
            return clazz;
        } else {
            /*error = "";
            for (Diagnostic diagnostic : dialist) {
                error = error + compilePrint(diagnostic);
            }
            //diagnostics.getDiagnostics().clear();
            return error;*/
        	throw new CompilerException(javaCode,dialist);
        }
    	
    }
    /*private String compilePrint(@SuppressWarnings("rawtypes") Diagnostic diagnostic) {
        StringBuffer res = new StringBuffer();
        res.append("Code:[" + diagnostic.getCode() + "]\n");
        res.append("Kind:[" + diagnostic.getKind() + "]\n");
        res.append("Position:[" + diagnostic.getPosition() + "]\n");
        res.append("Start Position:[" + diagnostic.getStartPosition() + "]\n");
        res.append("End Position:[" + diagnostic.getEndPosition() + "]\n");
        res.append("Source:[" + diagnostic.getSource() + "]\n");
        res.append("Message:[" + diagnostic.getMessage(null) + "]\n");
        res.append("LineNumber:[" + diagnostic.getLineNumber() + "]\n");
        res.append("ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n");
        return res.toString();
    }*/
    
    private class CharSequenceJavaFileObject extends SimpleJavaFileObject {
    	private CharSequence content; 
    	public CharSequenceJavaFileObject(String className,CharSequence content) { 
    		super(URI.create("string:///" + className.replace('.', '/') 
    				+ JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE); 
    		this.content = content; 
    	} 
    	@Override 
    	public CharSequence getCharContent( 
    			boolean ignoreEncodingErrors) { 
    			return content; 
    	} 
    }
    @SuppressWarnings("rawtypes")
	private class ClassFileManager extends ForwardingJavaFileManager {
        public JavaClassObject getJavaClassObject() {
            return jclassObject;
        }
     
        private JavaClassObject jclassObject;
        @SuppressWarnings("unchecked")
		public ClassFileManager(StandardJavaFileManager
            standardManager) {
            super(standardManager);
        }
        @Override
        public JavaFileObject getJavaFileForOutput(Location location,
            String className, JavaFileObject.Kind kind, FileObject sibling)
                throws IOException {
                jclassObject = new JavaClassObject(className, kind);
            return jclassObject;
        }
    }
    private class DynamicClassLoader extends java.net.URLClassLoader {
    	public DynamicClassLoader(ClassLoader parent) { 
    			super(new URL[0], parent); 
    	} 
    	@SuppressWarnings("unused")
		public Class<?> findClassByClassName(String className) throws ClassNotFoundException { 
    			return this.findClass(className); 
    	} 
    	@SuppressWarnings("rawtypes")
		public Class loadClass(String fullName, JavaClassObject jco) { 
    		byte[] classData = jco.getBytes(); 
    		return this.defineClass(fullName, classData, 0, classData.length); 
    	}
    }
    private class JavaClassObject extends SimpleJavaFileObject {
    	protected final ByteArrayOutputStream bos =new ByteArrayOutputStream(); 
    	public JavaClassObject(String name, JavaFileObject.Kind kind) { 
        	  super(URI.create("string:///" + name.replace('.', '/') 
            		  + kind.extension), kind); 
          } 
          public byte[] getBytes() { 
        	  return bos.toByteArray(); 
          } 
          @Override 
          public OutputStream openOutputStream() throws IOException { 
        	  return bos; 
          }
    }
}
