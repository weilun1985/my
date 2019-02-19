package com.li.cson.MetaDatas;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class Property {
	public Property(String name,Object impl){
		this.name=name;
		this.impl=impl;
		if(impl instanceof Field){
			this.protype=((Field)impl).getType();
		}
		else{
			/*if(((Method)impl).getName().startsWith("s"))
				this.protype=((Method)impl).getParameterTypes()[0];
			else
				this.protype=((Method)impl).getReturnType();*/
			Object[] methods=(Object[])impl;
			if(methods[1]!=null){
				this.protype=((Method)methods[1]).getReturnType();
			}
			else{
				this.protype=((Method)methods[2]).getParameterTypes()[0];
			}
		}
	}
	private String name;
	private Object impl;
	private Class<?> protype;
	public boolean isField(){
		return this.impl instanceof Field;
	}
	public String getName(){
		return this.name;
	}
	public Object get(Object inst){
		try{
			if(impl instanceof Field){
				return ((Field)impl).get(inst);
			}
			else{
				//return ((Method)impl).invoke(inst, new Object[0]);
				Object[] methods=(Object[])impl;
				if(methods[1]==null)
					return null;
				return ((Method)methods[1]).invoke(inst, new Object[0]);
			}
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
	public void set(Object inst,Object value){
		try
		{
			if(impl instanceof Field){
				((Field)impl).set(inst, value);
			}
			else{
				//((Method)impl).invoke(inst, value);
				Object[] methods=(Object[])impl;
				if(methods[2]==null)
					return;
				((Method)methods[2]).invoke(inst, value);
			}
		}
		catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
	public Class<?> getPropertyType(){
		return this.protype;
	}
	@Override
	public String toString(){
		return this.impl.toString();
	}
}
