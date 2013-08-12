package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 18.
 *
 */
public class DevysNounFilter extends TokenFilter {
	private Log logger = LogFactory.getLog(DevysNounFilter.class);

	private Stack<State> nouns = new Stack<State>();
	private Engine engine;

	public DevysNounFilter(TokenStream in) {
		super(in);
		this.engine = DevysNounEngine.getInstance();
	}

	@Override
	public boolean incrementToken() throws IOException {
		if(logger.isDebugEnabled())
			logger.debug("incrementToken DevysNounFilter");


		if (nouns.size() > 0) {
			if(logger.isDebugEnabled())
				logger.debug("명사 Stack에서 토큰 리턴함");

			State synState = nouns.pop();
			restoreState(synState); //#3. 현재의 stream 즉 AttributeSource를 저장해놨던 놈으로 바꿔치기한다.

			return true;
		}

		if (!input.incrementToken())
			return false;
		
		try {
			nouns = engine.getAttributeSources(input.cloneAttributes());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		//원본 Token 리턴
		if(logger.isDebugEnabled()) {
			CharTermAttribute charTermAttr = input.getAttribute(CharTermAttribute.class);
			logger.debug("원본 termAttr 리턴 : [" + charTermAttr.toString() + "]");
		}
		
		return true;
	}
}