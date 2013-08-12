package com.tistory.devyongsik.analyzer;

import java.util.Stack;

import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeSource.State;

public interface Engine {
	Stack<State> getAttributeSources(AttributeSource attributeSource) throws Exception;
}
