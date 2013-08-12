package com.tistory.devyongsik.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeSource.State;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 18.
 *
 */
public class DevysNounEngine implements Engine {

	private Log logger = LogFactory.getLog(DevysNounEngine.class);

	private static DevysNounEngine nounEngineInstance = new DevysNounEngine();

	//문서에서 읽어는 것으로 나중에 대체하면 됩니다.
	private List<String> nounDic = new ArrayList<String>();


	public static DevysNounEngine getInstance() {
		return nounEngineInstance;
	}

	private DevysNounEngine() {
		if(logger.isInfoEnabled())
			logger.info("사전을 읽습니다.");

		nounDic.add("자바");
		nounDic.add("스프링");
		nounDic.add("하둡");
		nounDic.add("파일");
		nounDic.add("시스템");
	}

	@Override
	public Stack<State> getAttributeSources(AttributeSource attributeSource) throws Exception {

		//선행 필터로부터 추출된 Token의 정보들을 얻습니다.
		CharTermAttribute termAttr = attributeSource.getAttribute(CharTermAttribute.class);
		TypeAttribute typeAttr = attributeSource.getAttribute(TypeAttribute.class);
		//OffsetAttribute offSetAttr = attributeSource.getAttribute(OffsetAttribute.class);

		//word 타입만 분석한다.
		boolean isNotWord = false;
		if(typeAttr.type().equals("word")) isNotWord = false;

		char[] term = termAttr.buffer();

		if(logger.isDebugEnabled()) {
			logger.debug("char : " + new String(term));
			logger.debug("char length : " + term.length);
		}

		Stack<State> nounsStack = new Stack<State>();
		String comparedWord = "";
		
		if(isNotWord) {
			if(logger.isDebugEnabled()) {
				logger.debug("명사 분석 대상이 아닙니다.");
			}
			
			return nounsStack;
		}
		
		for(int startIndex = 0 ; startIndex < term.length; startIndex++) {
			for(int endIndex = 0; endIndex < term.length - startIndex; endIndex++) {
				comparedWord = new String(term, startIndex, endIndex);

				//매칭될 때 State 저장
				if(nounDic.contains(comparedWord)) {
					CharTermAttribute attr = attributeSource.addAttribute(CharTermAttribute.class); //원본을 복사한 AttributeSource의 Attribute를 받아옴
					attr.setEmpty();
					attr.append(comparedWord);

					PositionIncrementAttribute positionAttr = attributeSource.addAttribute(PositionIncrementAttribute.class); 
					positionAttr.setPositionIncrement(1);  //추출된 명사이기 때문에 위치정보를 1로 셋팅

					TypeAttribute typeAtt = attributeSource.addAttribute(TypeAttribute.class); 
					//타입을 synonym으로 설정한다. 나중에 명사추출 시 동의어 타입은 건너뛰기 위함
					typeAtt.setType("noun"); 

					//offset도 계산해주어야 합니다. 그래야 하이라이팅이 잘 됩니다.
					
					nounsStack.push(attributeSource.captureState()); //추출된 동의어에 대한 AttributeSource를 Stack에 저장
				}
			}
		}

		return nounsStack;
	}

}