package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.FilteringTokenFilter;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 8.
 *
 */
public class DevysStopFilter extends FilteringTokenFilter {
	
	private Log logger = LogFactory.getLog(DevysStopFilter.class);
	
	private final CharTermAttribute charTermAtt = addAttribute(CharTermAttribute.class);
	
	private List<String> stopWords = new ArrayList<String>();

	private void initStopWord() {
		stopWords.add("the");
		stopWords.add(".");
	}
	
	public DevysStopFilter(boolean enablePositionIncrements, TokenStream input) {
		super(enablePositionIncrements, input);
		initStopWord();
		
		
		if(logger.isDebugEnabled())
			logger.debug("initailize...");
	}

	@Override
	protected boolean accept() throws IOException {
		boolean isAccept = !stopWords.contains(charTermAtt.toString());
		
		return isAccept;
	}
}
