package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 8.
 *
 */
public class DevysStopFilterTest {

	private static Set<String> tokens = new HashSet<String>();
	//불용어는 the와 .
	StringReader reader = new StringReader("the 개발하고 꼭 이것을 잘 해야합니다. 공백입니다.");

	@BeforeClass
	public static void setUp() {
		tokens.add("개발하고");
		tokens.add("이것을");
		tokens.add("해야합니다");
		tokens.add("공백입니다");
		tokens.add("꼭");
		tokens.add("잘");
	}
	
	
	@Test
	public void stopFilter() throws IOException {
		TokenStream stream = new DevysStopFilter(true, new DevysTokenizer(reader));
		CharTermAttribute charTermAttr = stream.getAttribute(CharTermAttribute.class);
		PositionIncrementAttribute positionAttr = stream.getAttribute(PositionIncrementAttribute.class);
		
		while(stream.incrementToken()) {
			System.out.println("charTermAttr : " + charTermAttr.toString());
			System.out.println("positionAttr : " + positionAttr.getPositionIncrement());

			Assert.assertTrue(tokens.contains(charTermAttr.toString()));
		}
		
		stream.close();
	}
}
