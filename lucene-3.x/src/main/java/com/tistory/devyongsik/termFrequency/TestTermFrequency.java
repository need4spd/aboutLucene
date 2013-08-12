package com.tistory.devyongsik.termFrequency;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class TestTermFrequency {
  public static void main(String[] args) throws Exception, LockObtainFailedException, IOException {

		String a = "learning perl learning java";
		String b = "perl test learning";

		Directory dir = new RAMDirectory();
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36); //문서 내용을 분석 할 때 사용 될 Analyzer
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
		iwc.setOpenMode(OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(dir, iwc); //8. 드디어 IndexWriter를 생성합니다.
		
		Document doc1 = new Document();
		Field f1 = new Field("f", a, Store.NO, Index.ANALYZED, TermVector.YES);
		doc1.add(f1);
		
		Document doc2 = new Document();
		Field f2 = new Field("f", b, Store.NO, Index.ANALYZED, TermVector.YES);
		doc2.add(f2);
		
		writer.addDocument(doc1);
		writer.addDocument(doc2);
		
		writer.commit();
		writer.close();
		
		IndexReader ir = IndexReader.open(dir);
		TermFreqVector termFreqVector1 = ir.getTermFreqVector(0, "f");
		TermFreqVector termFreqVector2 = ir.getTermFreqVector(1, "f");
		
		String[] terms1 = termFreqVector1.getTerms();
		int[] freqs1 = termFreqVector1.getTermFrequencies();
		
		for(int i = 0; i < terms1.length; i++) {
			System.out.println(terms1[i] + " : " + freqs1[i]);
		}
		
		
		String[] terms2 = termFreqVector2.getTerms();
		int[] freqs2 = termFreqVector2.getTermFrequencies();
		
		
		for(int i = 0; i < terms2.length; i++) {
			System.out.println(terms2[i] + " : " + freqs2[i]);
		}
	}
}