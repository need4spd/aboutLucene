package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

import com.tistory.devyongsik.analyzer.attributes.MyAttribute;

public class DevysTokenizer extends Tokenizer {

	private Log logger = LogFactory.getLog(DevysTokenizer.class);
	
	public DevysTokenizer(Reader in) {
		super(in);

		if(logger.isInfoEnabled()) {
			logger.info("DevysTokenizer....constructor");
		}
	}
	
	public DevysTokenizer(AttributeSource source, Reader input) {
		super(source, input);
	}

	public DevysTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
	}

	private int offset = 0, bufferIndex = 0, dataLen = 0;
	private static final int MAX_WORD_LEN = 255;
	private static final int IO_BUFFER_SIZE = 4096;
	private final char[] ioBuffer = new char[IO_BUFFER_SIZE];
	private char preChar = ' ';

	private int preCharType = 99;
	private int nowCharType = 99;

	private final int DIGIT_PERIOD = 0; //숫자
	private final int KOREAN = 1; //한글
	private final int ALPHA = 2; //영어
	
	private CharTermAttribute charTermAtt = addAttribute(CharTermAttribute.class);
	private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	private PositionIncrementAttribute positionIncAtt = addAttribute(PositionIncrementAttribute.class);
	//dummy로 속성을 추가해봅니다.
	private MyAttribute myAtt = addAttribute(MyAttribute.class);

	protected boolean isTokenChar(char c) {
		return (Character.isLetter(c) || Character.isDigit(c) || (c == '.'));
	}

	protected char normalize(char c) {
		return Character.toLowerCase(c);
	}
	
	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();

		if(logger.isInfoEnabled())
			logger.info("incrementToken");


		int length = 0;
		int start = bufferIndex;
		char[] buffer = charTermAtt.buffer();

		int posIncr = 1;
		
		while (true) {

			if (bufferIndex >= dataLen) {
				offset += dataLen;
				dataLen = input.read(ioBuffer);
				if (dataLen == -1) {
					if (length > 0)
						break;
					else
						return false;
				}
				bufferIndex = 0;
			}

			final char c = ioBuffer[bufferIndex++];

			if (isTokenChar(c)) {               // if it's a token char

				//전 문자와 현재 문자를 비교해서 속성이 다르면  분리해낸다.
				if (length > 0) {
					//이전문자의 속성 set
					if(Character.isDigit(preChar) || preChar == '.') preCharType = this.DIGIT_PERIOD;
					else if(preChar < 127) preCharType = this.ALPHA;
					else preCharType = this.KOREAN;

					//현재문자의 속성set
					if(Character.isDigit(c) || c == '.') nowCharType = this.DIGIT_PERIOD;
					else if(c < 127) nowCharType = this.ALPHA;
					else nowCharType = this.KOREAN;

					if(preCharType != nowCharType) { //앞뒤 Character가 서로 다른 형식
						bufferIndex--;

						//여기서 토큰을 하나 끊어야 함
						charTermAtt.setLength(length);
					    offsetAtt.setOffset(correctOffset(start), correctOffset(start+length));
					    typeAtt.setType("word");
					    myAtt.setMyFlag("devys");
					    positionIncAtt.setPositionIncrement(posIncr);

					    if(logger.isInfoEnabled()) {
							logger.info("return Token type different");
							logger.info("charTermAtt : " + charTermAtt.toString());
						}
					    
						return true;
					}
				}
				preChar = c;

				if (length == 0)			           // start of token
					start = offset + bufferIndex - 1;
				else if (length == buffer.length)
					buffer = charTermAtt.resizeBuffer(1+length);

				buffer[length++] = normalize(c); // buffer it, normalized

				if (length == MAX_WORD_LEN)		   // buffer overflow!
					break;

			} else if (length > 0)             // at non-Letter w/ chars
				break;                           // return 'em
		}
	
		charTermAtt.setLength(length);
	    offsetAtt.setOffset(correctOffset(start), correctOffset(start+length));
	    typeAtt.setType("word");
	    positionIncAtt.setPositionIncrement(posIncr);
	    myAtt.setMyFlag("devys last");
	    
		if(logger.isInfoEnabled()) {
			logger.info("return Token");
			logger.info("charTermAtt : " + charTermAtt.toString());
		}	
		
		return true;

	}

}