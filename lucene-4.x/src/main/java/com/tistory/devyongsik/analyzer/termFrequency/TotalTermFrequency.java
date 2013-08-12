package com.tistory.devyongsik.analyzer.termFrequency;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class TotalTermFrequency {
	public static void main(String[] args) throws Exception, LockObtainFailedException, IOException {

		String a = "learning perl learning java learning ruby";
		String b = "perl test t";
		String c = "perl test t learning";

		Directory dir = new RAMDirectory();
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44); //문서 내용을 분석 할 때 사용 될 Analyzer
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		iwc.setOpenMode(OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(dir, iwc); //8. 드디어 IndexWriter를 생성합니다.
		
		Document doc1 = new Document();
		FieldType f1type = new FieldType();
		f1type.setIndexed(true);
		f1type.setStored(false);
		f1type.setTokenized(true);
		f1type.setStoreTermVectors(true);
		f1type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		
		Field f1 = new Field("f", a, f1type);
		doc1.add(f1);
		
		Document doc2 = new Document();
		Field f2 = new Field("f", b, f1type);
		doc2.add(f2);
		
		Document doc3 = new Document();
		Field f3 = new Field("f", c, f1type);
		doc3.add(f3);
		
		writer.addDocument(doc1);
		writer.addDocument(doc2);
		writer.addDocument(doc3);
		
		writer.commit();
		writer.close();
		
		IndexReader ir = IndexReader.open(dir);
		
		TermsEnum termEnum = MultiFields.getTerms(ir, "f").iterator(null);
		
		System.out.println(MultiFields.getTerms(ir, "f").getDocCount()); //3
		System.out.println(MultiFields.getTerms(ir, "f").getSumDocFreq()); //11
		System.out.println(MultiFields.getTerms(ir, "f").getSumTotalTermFreq()); //13
		
		BytesRef term1 = null;
		while((term1 = termEnum.next()) != null) {
			System.out.println("1 : " + termEnum.term().utf8ToString() + " : " + termEnum.docFreq() + " : " + termEnum.totalTermFreq());
		}
	}
}