package org.midnightas.majc;

public abstract class BuiltinFunction {
	
	public String clazz, name, varname;
	
	public BuiltinFunction(String clazz, String name, String var) {
		this.clazz = clazz;
		this.name = name;
		this.varname = var;
	}
	
	public BuiltinFunction register(Majc hashmap) {
		hashmap.functions.put(varname, this);
		hashmap.scope.put(clazz + "." + name, varname);
		return this;
	}
	
	public abstract void call(Majc hashmap);
	
}
