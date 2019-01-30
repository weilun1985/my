package com.li.core;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class CompilerException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 3324401695154946491L;
    private List<Diagnostic<? extends JavaFileObject>> dialist;
    private String source;
    private String message;
    public CompilerException(String source,List<Diagnostic<? extends JavaFileObject>> dialist){
        this.dialist=dialist;
        this.source=source;
        StringBuffer res = new StringBuffer();
        res.append("\r\n*********Source Code*********\r\n");
        res.append(source);
        res.append("\r\n*********Message*********\r\n");
        for (@SuppressWarnings("rawtypes") Diagnostic diagnostic : dialist) {
            //res.append("Code:[" + diagnostic.getCode() + "]\n");
            res.append("Kind:[" + diagnostic.getKind() + "]\n");
            res.append("Message:[" + diagnostic.getMessage(null) + "]\n");
            res.append("LineNumber:[" + diagnostic.getLineNumber() + "]\n");
            res.append("ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n");
            //res.append("Position:[" + diagnostic.getPosition() + "]\n");
            res.append("Start Position:[" + diagnostic.getStartPosition() + "]\n");
            res.append("End Position:[" + diagnostic.getEndPosition() + "]\n");
            //res.append("Source:[" + diagnostic.getSource() + "]\n");
        }
        this.message=res.toString();
    }
    @Override
    public String getMessage(){
        return this.message;
    }
    public String getSourceCode(){
        return this.source;
    }
    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics(){
        return this.dialist;
    }
}
