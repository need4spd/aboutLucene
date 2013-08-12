package com.tistory.devyongsik.advanced.categoryvector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 26.
 *
 */

public class SuggestMostRecommendCategory {

	private String indexPath = "d:/luceneindex/category";
	private String rawDataFilePath = "d:/work.log";
	//private String rawDataFilePath = "d:/prdnm_category.txt";

	private Map<String, Map<String, Integer>> categoryMap = new TreeMap<String, Map<String, Integer>>();

	public static void main(String[] args) throws IOException {
		SuggestMostRecommendCategory c = new SuggestMostRecommendCategory();
		//		c.indexingData();

		c.buildCategoryVectors();

		//		System.out.println(c.getCategory("d700"));
		//		System.out.println("-----------------------------------------------------------");
		//		System.out.println(c.getCategory("메모리카드"));
		//		System.out.println("-----------------------------------------------------------");
		System.out.println(c.getCategory("캐논 DSLR 카메라"));
		//		System.out.println("-----------------------------------------------------------");
		//		System.out.println(c.getCategory("호루스벤누 렌즈변환어댑터 A8267 (펜탁스/삼성K바디-라이카R렌즈) ★오후 7시이전 주문시 당일발송 가능★"));
		//		System.out.println("-----------------------------------------------------------");
		//		System.out.println(c.getCategory("캐논 익서스860IS 전용 호환 배터리 + 충전기 퍼스트팜 ☞PL보험가입(NB-5L)"));
		//		System.out.println("-----------------------------------------------------------");
		//		System.out.println(c.getCategory("신상품 DTR-2000 지속광 2000W"));
		//		System.out.println("-----------------------------------------------------------");
		//		System.out.println(c.getCategory("[정품]슈나이더 B+W 007 Neutral  XS-PRO 86mm+슈나이더 크리너융 (XS-PRO 86mm)"));
		//		System.out.println("-----------------------------------------------------------");
		//		c.readIndex();

	}

	private void readIndex() throws CorruptIndexException, IOException {
		IndexReader ir = IndexReader.open(FSDirectory.open(new File(indexPath)));

		int maxDoc = ir.maxDoc();

		for(int i = 0; i < maxDoc; i++) {
			Document doc = ir.document(i);
			String category = doc.get("category");
			System.out.println(category);
			System.out.println(doc.get("prdnm"));
		}
	}

	private void indexingData() throws IOException {
		Directory dir = FSDirectory.open(new File(indexPath));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_33); //문서 내용을 분석 할 때 사용 될 Analyzer
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_33, analyzer);
		iwc.setOpenMode(OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(dir, iwc); //8. 드디어 IndexWriter를 생성합니다.

		System.out.println("색인시작");

		indexDocs(writer, rawDataFilePath);

		System.out.println("색인종료");

		writer.close();
	}

	private void indexDocs(IndexWriter writer, String rawDataFilePath) throws IOException {
		File rawDataFile = new File(rawDataFilePath);
		FileInputStream fis = new FileInputStream(rawDataFile);
		InputStreamReader isr = new InputStreamReader(fis, "euc-kr");
		BufferedReader br = new BufferedReader(isr);

		String text = "";

		while((text = br.readLine()) != null) {
			String[] split = text.split("@!");

			if(split.length != 2) {
				continue;
			}

			Document doc = new Document();
			Field prdnm = new Field("prdnm", split[0], Store.YES, Index.ANALYZED, TermVector.YES);
			Field category = new Field("category", split[1], Store.YES, Index.NOT_ANALYZED_NO_NORMS);

			doc.add(prdnm);
			doc.add(category);

			writer.addDocument(doc);
		}
	}

	private void buildCategoryVectors() throws CorruptIndexException, IOException {
		//키는 카테고리... , Value는 Map<String, Integer>에서 "키워드":"freq"
		IndexReader ir = IndexReader.open(FSDirectory.open(new File(indexPath)));

		int maxDoc = ir.maxDoc();

		for(int i = 0; i < maxDoc; i++) {

			Document doc = ir.document(i);
			String category = doc.get("category").trim();

			Map<String, Integer> vectorMap = categoryMap.get(category);
			if(vectorMap == null) {
				vectorMap = new TreeMap<String, Integer>();
				categoryMap.put(category, vectorMap);
			}

			TermFreqVector termFreqVector = ir.getTermFreqVector(i, "prdnm");

			try {
				addTermFreqToMap(vectorMap, termFreqVector);
			} catch (Exception e) {
				System.out.println(e);
				System.out.println(doc);
			}
		}

		System.out.println("categoryMap : " + categoryMap.size());
	}

	private void addTermFreqToMap(Map<String, Integer> vectorMap, TermFreqVector termFreqVector) {
		String[] terms = termFreqVector.getTerms();
		int[] freqs = termFreqVector.getTermFrequencies();

		for(int i = 0; i < terms.length; i++) {
			String term = terms[i];

			if(vectorMap.containsKey(term)) {
				Integer value = vectorMap.get(term);

				vectorMap.put(term, new Integer(value.intValue() + freqs[i]));
			} else {

				vectorMap.put(term, freqs[i]);
			}
		}
	}

	private List<Category> getCategory(String keyword) throws IOException {
		List<String> words = new ArrayList<String>();
		List<Category> recommendCategories = new ArrayList<Category>();

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_33);
		TokenStream tokenStream = analyzer.reusableTokenStream("title", new StringReader(keyword));
		CharTermAttribute termAtt = tokenStream.getAttribute(CharTermAttribute.class);

		while (tokenStream.incrementToken()) {
			String text = termAtt.toString();
			words.add(text);
		}

		Iterator<String> categoryIterator = categoryMap.keySet().iterator();
		double bestAngle = Double.MAX_VALUE;
		String bestCategory = null;

		while(categoryIterator.hasNext()) {
			String category = categoryIterator.next();

			if(category.equals("DSLR/디카/액세서리##액세서리(DSLR/디카/일반)##필터/주변용품##") || category.equals("DSLR/디카/액세서리##DSLR카메라##캐논##")) {
				System.out.println("a");
			}
			Category c = computeAngle(words, category);
			recommendCategories.add(c);

			//			if(angle < bestAngle) {
			//				bestAngle = angle;
			//				bestCategory = category;
			//			}
		}

		Collections.sort(recommendCategories);
		recommendCategories = recommendCategories.subList(0, 10);

		return recommendCategories;

	}

	private Category computeAngle(List<String> words, String category) {
		Map<String, Integer> vectorMap = categoryMap.get(category);

		//키워드의 횟수를 30으로 고정
		List<Integer> wordFreq = new ArrayList<Integer>();
		List<Float> boostFactor = new ArrayList<Float>();

		int dotProduct = 0;
		int sumOfSquares = 0;
		int sumOfWordSquares = 0;

		int i = 0;
		for (String word : words) {

			wordFreq.add(1);

			int categoryWordFreq = 0;

			if(vectorMap.containsKey(word)) {
				categoryWordFreq = vectorMap.get(word);
			}

			dotProduct += categoryWordFreq * wordFreq.get(i);
			sumOfSquares += categoryWordFreq * categoryWordFreq;
			sumOfWordSquares += wordFreq.get(i) * wordFreq.get(i);

			if(category.indexOf(word) >= 0) {
				boostFactor.add(5f);
			} else {
				boostFactor.add(1f);
			}

			i++;
		}

		double denominator;

		if(sumOfSquares == words.size()) {
			denominator = sumOfSquares;
		} else {
			denominator = Math.sqrt(sumOfSquares) * Math.sqrt(sumOfWordSquares);
		}

		double ratio;

		if(denominator == 0) {
			ratio = 0;
		} else {
			ratio = dotProduct / denominator;
		}

		for(float boost : boostFactor) {
			ratio = ratio * boost;
		}


		//double sim = Math.acos(ratio);


		Category resultC = new Category();
		resultC.setCategory(category);
		resultC.setSimiliarity(ratio);

		return resultC;

	}

	private class Category implements Comparable<Category>{
		private double similiarity;
		private String category;
		/**
		 * @return the similiarity
		 */
		public double getSimiliarity() {
			return similiarity;
		}
		/**
		 * @param similiarity the similiarity to set
		 */
		public void setSimiliarity(double similiarity) {
			this.similiarity = similiarity;
		}
		/**
		 * @return the category
		 */
		public String getCategory() {
			return category;
		}
		/**
		 * @param category the category to set
		 */
		public void setCategory(String category) {
			this.category = category;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Category [similiarity=" + similiarity + ", category="
					+ category + "]" + "\n";
		}

		public int compareTo(Category o) {

			if(similiarity - o.getSimiliarity() > 0) {
				return -1;
			} else if (similiarity - o.getSimiliarity() < 0) {
				return 1;
			} else {
				return 0;
			}
		}		
	}
}