package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tistory.devyongsik.analyzer.attributes.MyAttribute;

public class DevysTokenizerTest {

	private static Set<String> tokenizedToken = new HashSet<String>();
	private StringReader sampleSentence = new StringReader(
			"퇴근하고. 집에갑니다. 애플스토어에서 아이패드32G를 구입하러 갑니다.");
	private DevysTokenizer tokenizer = new DevysTokenizer(sampleSentence);

	@BeforeClass
	public static void init() {
		tokenizedToken.add("퇴근하고");
		tokenizedToken.add("집에갑니다");
		tokenizedToken.add("애플스토어에서");
		tokenizedToken.add("아이패드");
		tokenizedToken.add("32");
		tokenizedToken.add("g");
		tokenizedToken.add("를");
		tokenizedToken.add("구입하러");
		tokenizedToken.add("갑니다");
		tokenizedToken.add(".");
	}

	@Test
	public void 토크나이저_테스트() {
		CharTermAttribute charTermAttr = tokenizer
				.getAttribute(CharTermAttribute.class);
		
		PositionIncrementAttribute posIncrAtt = tokenizer
				.addAttribute(PositionIncrementAttribute.class);
		
		OffsetAttribute offsetAtt = tokenizer
				.addAttribute(OffsetAttribute.class);
		
		MyAttribute myAtt = tokenizer.addAttribute(MyAttribute.class);

		try {
			while (tokenizer.incrementToken()) {
				int postIncrAttr = posIncrAtt.getPositionIncrement();
				int startOffSet = offsetAtt.startOffset();
				int endOffSet = offsetAtt.endOffset();

				System.out.println("text : " + charTermAttr.toString());
				System.out.println("postIncrAttr : " + postIncrAttr);
				System.out.println("startOffSet : " + startOffSet);
				System.out.println("endOffSet : " + endOffSet);
				System.out.println("myAtt : " + myAtt.getMyFlag());

				Assert.assertTrue(tokenizedToken.contains(charTermAttr
						.toString()));
				Assert.assertTrue(postIncrAttr > 0);
				Assert.assertTrue(startOffSet >= 0);
				Assert.assertTrue(endOffSet > 0);
			}
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Test Failed");
		}
	}
}
