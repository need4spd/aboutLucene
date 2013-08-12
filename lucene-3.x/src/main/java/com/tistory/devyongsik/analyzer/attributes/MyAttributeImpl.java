package com.tistory.devyongsik.analyzer.attributes;

import java.io.Serializable;

import org.apache.lucene.util.AttributeImpl;

public class MyAttributeImpl extends AttributeImpl implements MyAttribute, Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	private String flag;
	
	@Override
	public void setMyFlag(String flag) {
		this.flag = flag;
	}

	@Override
	public String getMyFlag() {
		return this.flag;
	}

	@Override
	public void clear() {
		flag = null;
	}

	@Override
	public void copyTo(AttributeImpl target) {
		// TODO Auto-generated method stub
		
	}


}
