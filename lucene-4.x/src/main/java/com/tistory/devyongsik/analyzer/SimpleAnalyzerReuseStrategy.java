package com.tistory.devyongsik.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;

public class SimpleAnalyzerReuseStrategy extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(final String fieldName, Reader reader) {
		//return new TokenStreamComponents(new LowerCaseTokenizer(Version.LUCENE_40, reader));
		return new TokenStreamComponents(new DevysTokenizer(reader));
	}
}
