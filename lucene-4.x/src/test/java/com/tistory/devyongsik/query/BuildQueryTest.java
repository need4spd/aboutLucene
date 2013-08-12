package com.tistory.devyongsik.query;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 27.
 *
 */
public class BuildQueryTest {
	private String keyword = "lucene in action";

	@Test
	public void buildQueryWithAnalyzer() throws IOException {
		Analyzer a = new WhitespaceAnalyzer(Version.LUCENE_44);
		
		TokenStream stream = a.tokenStream("not use", new StringReader(keyword));
		CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
		
		BooleanQuery resultQuery = new BooleanQuery();
		
		while(stream.incrementToken()) {
			String t = term.toString();
			Query q = new TermQuery(new Term("field", t));
			
			resultQuery.add(q, Occur.MUST);
		}
		
		System.out.println(resultQuery);
		
		Assert.assertEquals("+field:lucene +field:in +field:action", resultQuery.toString());
		
		a.close();
	}
}
