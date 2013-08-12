package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 8.
 *
 */
public class DevysSynonymFilterTest {
	StringReader content = new StringReader("노트북");

	List<String> synonymWordList = new ArrayList<String>();

	@Before
	public void setUp() throws Exception {
		synonymWordList.add("노트북");
		synonymWordList.add("노트북pc");
		synonymWordList.add("노트북컴퓨터");
		synonymWordList.add("노트북피씨");
		synonymWordList.add("notebook");
	}

	@Test
	public void testSynonym() throws IOException {
		TokenStream stream = new DevysSynonymFilter(new DevysTokenizer(content));
		CharTermAttribute charTermAtt = stream.getAttribute(CharTermAttribute.class);
		OffsetAttribute offsetAtt = stream.getAttribute(OffsetAttribute.class);
		TypeAttribute typeAtt = stream.getAttribute(TypeAttribute.class);
		PositionIncrementAttribute positionAtt = stream.getAttribute(PositionIncrementAttribute.class);
		
		List<String> extractedSynonyms = new ArrayList<String>();
		
		while(stream.incrementToken()) {

			System.out.println("charTermAtt : " + charTermAtt.toString());
			System.out.println("offsetAtt start offset : " + offsetAtt.startOffset());
			System.out.println("offsetAtt end offset : " + offsetAtt.endOffset());
			System.out.println("typeAtt : " + typeAtt.type());
			System.out.println("positionAtt : " + positionAtt.getPositionIncrement());

			Assert.assertTrue(synonymWordList.contains(charTermAtt.toString()));
			
			extractedSynonyms.add(charTermAtt.toString());
		}
		
		for(String syn : synonymWordList) {
			Assert.assertTrue(extractedSynonyms.contains(syn));
		}
		
		stream.close();
	}

}
