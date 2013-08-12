package com.tistory.devyongsik.analyzer.termFrequency;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class TestTermFrequency {
	public static void main(String[] args) throws Exception, LockObtainFailedException, IOException {

		String a = "learning perl learning java learning ruby";
		String b = "perl test t";

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
		
		writer.addDocument(doc1);
		writer.addDocument(doc2);
		
		writer.commit();
		writer.close();
		
		IndexReader ir = IndexReader.open(dir);
		Terms terms1 = ir.getTermVector(0, "f");
		Terms terms2 = ir.getTermVector(1, "f");
		
		System.out.println(terms1.getDocCount());
		System.out.println(terms1.getSumDocFreq());
		System.out.println(terms1.getSumTotalTermFreq());
		
		
		TermsEnum termsEnum1 = terms1.iterator(null);
		
		//termsEnum1.seekExact(new BytesRef("java"), true);
		//System.out.println("GDGDG : " + termsEnum1.totalTermFreq());
		
		BytesRef term1 = null;
		while((term1 = termsEnum1.next()) != null) {
			
			System.out.println("1 : " + termsEnum1.term().utf8ToString() + " : " + termsEnum1.docFreq() + " : " + termsEnum1.totalTermFreq());
		}
		
		TermsEnum termsEnum2 = terms2.iterator(null);
		
		
		BytesRef term2 = null;
		while((term2 = termsEnum2.next()) != null) {
			
			System.out.println("2 : " + termsEnum2.term().utf8ToString() + " : " + termsEnum2.docFreq() + " : " + termsEnum2.totalTermFreq());

		}
	}
}