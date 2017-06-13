package com.moofMonkey;

import javassist.CtClass;
import javassist.CtField;

public class FieldRename {
	private CtClass cl;
	private CtField fd;
	private String newName;

	public FieldRename(CtClass cl, CtField fd, String newName) {
		this.cl = cl;
		this.fd = fd;
		this.newName = newName;
	}

	public CtClass getCl() {
		return cl;
	}

	public CtField getFd() {
		return fd;
	}

	public String getNewName() {
		return newName;
	}
}
