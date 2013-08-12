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
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 18.
 *
 */
public class DevysNounFilterTest {
	
	List<String> nounsList = new ArrayList<String>();
	StringReader reader = new StringReader("자바프로그래밍 스프링을 사용하고 파일 시스템은 하둡 파일 시스템을 사용하여 하둡에 대해 알아봅니다.");
	
	@Before
	public void setUp() throws Exception {
		nounsList.add("자바");
		nounsList.add("자바프로그래밍");
		nounsList.add("스프링");
		nounsList.add("스프링을");
		nounsList.add("사용하고");
		nounsList.add("파일");
		nounsList.add("시스템은");
		nounsList.add("시스템");
		nounsList.add("하둡");
		nounsList.add("시스템을");
		nounsList.add("사용하여");
		nounsList.add("하둡에");
		nounsList.add("대해");
		nounsList.add("알아봅니다");
		nounsList.add(".");
	}
	
	@Test
	public void testTokenStream() throws IOException {
		TokenStream stream = new DevysNounFilter(new DevysTokenizer(reader));
		CharTermAttribute charTermAtt = stream.getAttribute(CharTermAttribute.class);
		OffsetAttribute offsetAtt = stream.getAttribute(OffsetAttribute.class);
		TypeAttribute typeAtt = stream.getAttribute(TypeAttribute.class);
		PositionIncrementAttribute positionAtt = stream.getAttribute(PositionIncrementAttribute.class);
		
		List<String> extractedNouns = new ArrayList<String>();
		
		while(stream.incrementToken()) {

			System.out.println("charTermAtt : " + charTermAtt.toString());
			System.out.println("offsetAtt start offset : " + offsetAtt.startOffset());
			System.out.println("offsetAtt end offset : " + offsetAtt.endOffset());
			System.out.println("typeAtt : " + typeAtt.type());
			System.out.println("positionAtt : " + positionAtt.getPositionIncrement());

			Assert.assertTrue(nounsList.contains(charTermAtt.toString()));
			
			extractedNouns.add(charTermAtt.toString());
		}
		
		for(String syn : nounsList) {
			Assert.assertTrue(extractedNouns.contains(syn));
		}
	}
}