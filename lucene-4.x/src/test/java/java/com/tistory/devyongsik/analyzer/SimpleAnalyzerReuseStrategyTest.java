package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Assert;
import org.junit.Test;

public class SimpleAnalyzerReuseStrategyTest {
	@Test
	public void analysisTest() throws IOException {
		
		StringReader stringReader1 = new StringReader("abdass asdad");
		StringReader stringReader2 = new StringReader("abdass asdad");
		//StringReader stringReader2 = new StringReader("b아디다스 운동화입니다.");
		StringReader stringReader3 = new StringReader("c무궁화 꽃이 피었습니다!.");
		
		StringReader[] strs = new StringReader[3];
		strs[0] = stringReader1;
		strs[1] = stringReader2;
		strs[2] = stringReader3;
		
		Analyzer analyzer = new SimpleAnalyzerReuseStrategy();
		//Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_40);
		
		for(int i = 0; i < 3; i++) {
			
			System.out.println("______________________________________");
			TokenStream tokenStream = analyzer.tokenStream("title", strs[i]);
			
			CharTermAttribute termAtt = tokenStream.getAttribute(CharTermAttribute.class);
			PositionIncrementAttribute posIncrAtt = tokenStream.addAttribute(PositionIncrementAttribute.class);
			OffsetAttribute offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);
		   
		    while (tokenStream.incrementToken()) {
		      String text = termAtt.toString();
		      int postIncrAttr = posIncrAtt.getPositionIncrement();
		      int startOffSet = offsetAtt.startOffset();
		      int endOffSet = offsetAtt.endOffset();
		      
		      System.out.println("text : "+ text);
		      System.out.println("postIncrAttr : " + postIncrAttr);
		      System.out.println("startOffSet : " + startOffSet);
		      System.out.println("endOffSet : " + endOffSet);
		      
		      Assert.assertNotNull(text);
		      Assert.assertTrue(postIncrAttr > 0);
		      Assert.assertTrue(startOffSet >= 0);
		      Assert.assertTrue(endOffSet > 0);
		    }
		}
		
		analyzer.close();
	}
}
