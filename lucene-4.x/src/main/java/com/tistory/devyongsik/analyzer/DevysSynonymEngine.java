package com.tistory.devyongsik.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeSource.State;
import org.apache.lucene.util.Version;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 8.
 *
 */
public class DevysSynonymEngine implements Engine {

	private Log logger = LogFactory.getLog(DevysSynonymEngine.class);

	private static RAMDirectory directory;
	private static DirectoryReader directoryReader;
	private static IndexSearcher searcher;
	private static DevysSynonymEngine synonymEngineInstance = new DevysSynonymEngine();

	//문서에서 읽어는 것으로 나중에 대체하면 됩니다.
	private String synonymWord = new String("노트북,노트북pc,노트북컴퓨터,노트북피씨,notebook");

	public static DevysSynonymEngine getInstance() {
		return synonymEngineInstance;
	}

	private DevysSynonymEngine() {
		if(logger.isInfoEnabled())
			logger.info("동의어 색인을 실시합니다.");

		createSynonymIndex();

		if(logger.isInfoEnabled())
			logger.info("동의어 색인 완료");

		try {
			directoryReader = DirectoryReader.open(directory);
			searcher = new IndexSearcher(directoryReader);
		} catch (CorruptIndexException e) {
			logger.error("동의어 색인에 대한 Searcher 생성 중 에러 발생함 : " + e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("동의어 색인에 대한 Searcher 생성 중 에러 발생함 : " + e);
			e.printStackTrace();
		}
	}

	private void createSynonymIndex() {
		directory = new RAMDirectory();

		try {

			Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_44); //문서 내용을 분석 할 때 사용 될 Analyzer
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);

			IndexWriter ramWriter = new IndexWriter(directory, iwc);
			String[] synonymWords = synonymWord.split(",");


			int recordCnt = 0;
			//동의어들을 ,로 잘라내어 색인합니다.
			//하나의 document에 syn이라는 이름의 필드를 여러개 추가합니다.
			//나중에 syn=노트북 으로 검색한다면 그때 나온 결과 Document로부터 
			//모든 동의어 리스트를 얻을 수 있습니다.
			Document doc = new Document();
			for(int i = 0, size = synonymWords.length; i < size ; i++) {
				

				String fieldValue = synonymWords[i];
				//Field field = new Field("syn",fieldValue,Store.YES,Index.NOT_ANALYZED);
				FieldType fieldType = new FieldType();
				fieldType.setIndexed(true);
				fieldType.setStored(true);
				
				Field field = new Field("syn", fieldValue, fieldType);
				doc.add(field);

				recordCnt++;
			}//end for
			ramWriter.addDocument(doc);
			ramWriter.close();


			if(logger.isInfoEnabled())
				logger.info("동의어 색인 단어 갯수 : " + recordCnt);

		} catch (CorruptIndexException e) {
			logger.error("동의어 색인 중 에러 발생함 : " + e);
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			logger.error("동의어 색인 중 에러 발생함 : " + e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("동의어 색인 중 에러 발생함 : " + e);
			e.printStackTrace();
		}
	}

	private List<String> getWords(String word) throws Exception {
		List<String> synWordList = new ArrayList<String>();
		if(logger.isDebugEnabled()) {
			logger.debug("동의어 탐색 : " + word);
		}

		Query query = new TermQuery(new Term("syn",word));
		
		if(logger.isDebugEnabled()) {
			logger.debug("query : " + query);
		}
		
		TopScoreDocCollector collector = TopScoreDocCollector.create(5 * 5, false);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		if(logger.isDebugEnabled()) {
			logger.debug("대상 word : " + word);
			//검색된 document는 하나이므로..
			logger.debug("동의어 갯수 : " + hits.length);
		}

		for(int i = 0; i < hits.length; i++) {
			Document doc = searcher.doc(hits[i].doc);

			String[] values = doc.getValues("syn");

			for(int j = 0; j < values.length; j++) {
				if(logger.isDebugEnabled())
					logger.debug("대상 word : " + "["+word+"]" + " 추출된 동의어 : " + values[j]);

				if(!word.equals(values[j])) {
					synWordList.add(values[j]);
				}
			}
		}
		return synWordList;
	}

	@Override
	public Stack<State> getAttributeSources(AttributeSource attributeSource) throws Exception {
		CharTermAttribute charTermAttr = attributeSource.getAttribute(CharTermAttribute.class);
		
		if(logger.isDebugEnabled())
			logger.debug("넘어온 Term : " + charTermAttr.toString());
		
		Stack<State> synonymsStack = new Stack<State>();

		List<String> synonyms = getWords(charTermAttr.toString());

		if (synonyms.size() == 0) new Stack<State>(); //동의어 없음

		for (int i = 0; i < synonyms.size(); i++) {
			
			//#1. 동의어는 키워드 정보와 Type정보, 위치증가정보만 변경되고 나머지 속성들은 원본과 동일하기 때문에
			//attributeSource로부터 변경이 필요한 정보만 가져와서 필요한 정보를 변경한다.
			//offset은 원본과 동일하기 때문에 건드리지 않는다.
			CharTermAttribute attr = attributeSource.addAttribute(CharTermAttribute.class); //원본을 복사한 AttributeSource의 Attribute를 받아옴
			attr.setEmpty();
			attr.append(synonyms.get(i));
			PositionIncrementAttribute positionAttr = attributeSource.addAttribute(PositionIncrementAttribute.class); //원본 AttributeSource의 Attribute를 받아옴
			positionAttr.setPositionIncrement(0);  //동의어이기 때문에 위치정보 변하지 않음
			TypeAttribute typeAtt = attributeSource.addAttribute(TypeAttribute.class); //원본 AttributeSource의 Attribute를 받아옴
			//타입을 synonym으로 설정한다. 나중에 명사추출 시 동의어 타입은 건너뛰기 위함
			typeAtt.setType("synonym"); 
			
			synonymsStack.push(attributeSource.captureState()); //추출된 동의어에 대한 AttributeSource를 Stack에 저장
		}
		
		return synonymsStack;
	}
}