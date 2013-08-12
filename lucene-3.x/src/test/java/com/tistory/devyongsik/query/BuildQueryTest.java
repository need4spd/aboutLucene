package com.tistory.devyongsik.query;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.Assert;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.junit.Test;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 27.
 *
 */
public class BuildQueryTest {
	private String keyword = "lucene in action";

	@Test
	public void buildQueryWithAnalyzer() throws IOException {
		Analyzer a = new WhitespaceAnalyzer(Version.LUCENE_33);
		
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
	}
	
	@Test
	public void queryParseTest() throws ParseException, IOException {
		String field = "contents";
		String keyword = "자바 초보 강의";
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		QueryParser parser = new QueryParser(Version.LUCENE_36, field, analyzer);
	    Query query = parser.parse(keyword);
	    
	    System.out.println(query);
	    
	    //contents:자바 +title:초보
	    
	    TokenStream ts = analyzer.tokenStream("dummy", new StringReader(keyword));
	    ts.addAttribute(CharTermAttribute.class);
	    
	    String[] fieldList = {"title", "contents"};
	    
	    BooleanQuery bq = new BooleanQuery();
	    
	    while(ts.incrementToken()) {
	    	CharTermAttribute charterm = ts.getAttribute(CharTermAttribute.class);
	    	System.out.println(charterm.toString());
	    	
	    	Query q1 = new TermQuery(new Term(fieldList[0], charterm.toString()));
	    	q1.setBoost(5.0F);
	    	System.out.println("q1 : " + q1);
	    	Query q2 = new TermQuery(new Term(fieldList[1], charterm.toString()));
	    	System.out.println("q2 : " + q2);
	    	
	    	bq.add(q1, Occur.MUST);
	    	bq.add(q2, Occur.SHOULD);
	    	
	    	System.out.println("result query : " + bq);
	    }
	    
	    String field2 = "last_modified";
		long start = 20120101L;
		long end = 20121231L;
		
		
		NumericRangeQuery<Long> nrq = NumericRangeQuery.newLongRange(field2, start, end, true, false);
		System.out.println("nrq : " + nrq);
		
		bq.add(nrq, Occur.MUST);
		
		System.out.println(bq);
	}
	
	@Test
	public void rangeQueryTest() throws ParseException, IOException {
		String field = "last_modified";
		long start = 20120101L;
		long end = 20121231L;
		
		
		NumericRangeQuery<Long> nrq = NumericRangeQuery.newLongRange(field, start, end, true, false);
		System.out.println("nrq : " + nrq);
	}
	
	@Test
	public void queryParserTest2() throws ParseException {
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		QueryParser parser = new QueryParser(Version.LUCENE_36, "title", analyzer);
	    Query query = parser.parse("title:\"자바 초보 강의\" +contents:문법 price:\\[10 to 10000\\]");
	    
	    System.out.println(query);
	}
}