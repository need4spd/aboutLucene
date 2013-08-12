package com.tistory.devyongsik.analyzer.attributes;

import org.apache.lucene.util.Attribute;

public interface MyAttribute extends Attribute {
	void setMyFlag(String flag);
	String getMyFlag();
}
