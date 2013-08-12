package com.tistory.devyongsik.commit;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class CommitTest {

	@Test
	public void commitTest() throws IOException {
		String a = "learning perl learning java learning ruby";
		String b = "perl test t";
		String c = "perl test t learning";

		Directory dir = new RAMDirectory();
		//Directory dir = FSDirectory.open(new File("/Users/need4spd/Programming/Java/workspace/aboutLucene_4/tempindex"));
		
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
		writer.addDocument(doc1);
		
		writer.commit();
		
		DirectoryReader directoryReader = DirectoryReader.open(dir);
		IndexCommit indexCommit = directoryReader.getIndexCommit();
		
		System.out.println(indexCommit.getGeneration());
		System.out.println(indexCommit.getSegmentCount());
		System.out.println(indexCommit.getSegmentsFileName());
		System.out.println(indexCommit.getFileNames());
		System.out.println(indexCommit.isDeleted());
		
		System.out.println("############################################");
		
		Document doc2 = new Document();
		Field f2 = new Field("f", b, f1type);
		doc2.add(f2);
		writer.addDocument(doc2);
		writer.commit();
		
		DirectoryReader directoryReader2 = DirectoryReader.open(dir);
		IndexCommit indexCommit2 = directoryReader2.getIndexCommit();
		
		System.out.println(indexCommit2.getGeneration());
		System.out.println(indexCommit2.getSegmentCount());
		System.out.println(indexCommit2.getSegmentsFileName());
		System.out.println(indexCommit2.getFileNames());
		System.out.println(indexCommit2.isDeleted());
		
		System.out.println("############################################");
		
		Document doc3 = new Document();
		Field f3 = new Field("f", c, f1type);
		doc3.add(f3);
		writer.addDocument(doc3);
		
		writer.commit();
		
		DirectoryReader directoryReader3 = DirectoryReader.open(dir);
		IndexCommit indexCommit3 = directoryReader3.getIndexCommit();
		
		System.out.println(indexCommit3.getGeneration());
		System.out.println(indexCommit3.getSegmentCount());
		System.out.println(indexCommit3.getSegmentsFileName());
		System.out.println(indexCommit3.getFileNames());
		System.out.println(indexCommit3.isDeleted());
		
		System.out.println("############################################");
		
		//delete
		Term t = new Term("f", "java");
		writer.deleteDocuments(t);
		
		DirectoryReader directoryReader4 = DirectoryReader.open(dir);
		IndexCommit indexCommit4 = directoryReader4.getIndexCommit();
		
		System.out.println(indexCommit4.getGeneration());
		System.out.println(indexCommit4.getSegmentCount());
		System.out.println(indexCommit4.getSegmentsFileName());
		System.out.println(indexCommit4.getFileNames());
		System.out.println(indexCommit4.isDeleted());
		
		writer.commit();
		
		System.out.println("############################################");
		
		DirectoryReader directoryReader5 = DirectoryReader.open(dir);
		IndexCommit indexCommit5 = directoryReader5.getIndexCommit();
		
		System.out.println(indexCommit5.getGeneration());
		System.out.println(indexCommit5.getSegmentCount());
		System.out.println(indexCommit5.getSegmentsFileName());
		System.out.println(indexCommit5.getFileNames());
		System.out.println(indexCommit5.isDeleted());
		
		System.out.println("############################################");
		
		writer.deleteDocuments(new Term("f", "perl"));
		writer.commit();
		
		DirectoryReader directoryReader6 = DirectoryReader.open(dir);
		IndexCommit indexCommit6 = directoryReader6.getIndexCommit();
		
		System.out.println(indexCommit6.getGeneration());
		System.out.println(indexCommit6.getSegmentCount());
		System.out.println(indexCommit6.getSegmentsFileName());
		System.out.println(indexCommit6.getFileNames());
		System.out.println(indexCommit6.isDeleted());
		
		writer.close();	
	}
	
	@Test
	public void commitMergSegmentTest() throws IOException {
		String a = "learning perl learning java learning ruby";
		String b = "perl test t";
		String c = "perl test t learning";

		Directory dir = new RAMDirectory();
		//Directory dir = FSDirectory.open(new File("/Users/need4spd/Programming/Java/workspace/aboutLucene_4/tempindex"));
		
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
		writer.addDocument(doc1);
		
		Document doc2 = new Document();
		Field f2 = new Field("f", b, f1type);
		doc2.add(f2);
		writer.addDocument(doc2);
		writer.commit();
		
		DirectoryReader directoryReader2 = DirectoryReader.open(dir);
		IndexCommit indexCommit2 = directoryReader2.getIndexCommit();
		
		System.out.println(indexCommit2.getGeneration());
		System.out.println(indexCommit2.getSegmentCount());
		System.out.println(indexCommit2.getSegmentsFileName());
		System.out.println(indexCommit2.getFileNames());
		System.out.println(indexCommit2.isDeleted());
		
		System.out.println("############################################");
		
		Document doc3 = new Document();
		Field f3 = new Field("f", c, f1type);
		doc3.add(f3);
		writer.addDocument(doc3);
		
		writer.commit();
		
		DirectoryReader directoryReader3 = DirectoryReader.open(dir);
		IndexCommit indexCommit3 = directoryReader3.getIndexCommit();
		
		System.out.println(indexCommit3.getGeneration());
		System.out.println(indexCommit3.getSegmentCount());
		System.out.println(indexCommit3.getSegmentsFileName());
		System.out.println(indexCommit3.getFileNames());
		System.out.println(indexCommit3.isDeleted());
		
		System.out.println("############################################");
		
		//delete
		Term t = new Term("f", "java");
		writer.deleteDocuments(t);
		
		DirectoryReader directoryReader4 = DirectoryReader.open(dir);
		IndexCommit indexCommit4 = directoryReader4.getIndexCommit();
		
		System.out.println(indexCommit4.getGeneration());
		System.out.println(indexCommit4.getSegmentCount());
		System.out.println(indexCommit4.getSegmentsFileName());
		System.out.println(indexCommit4.getFileNames());
		System.out.println(indexCommit4.isDeleted());
		
		writer.commit();
		
		System.out.println("############################################");
		
		DirectoryReader directoryReader5 = DirectoryReader.open(dir);
		IndexCommit indexCommit5 = directoryReader5.getIndexCommit();
		
		System.out.println(indexCommit5.getGeneration());
		System.out.println(indexCommit5.getSegmentCount());
		System.out.println(indexCommit5.getSegmentsFileName());
		System.out.println(indexCommit5.getFileNames());
		System.out.println(indexCommit5.isDeleted());
		
		System.out.println("############################################");
		
		writer.deleteDocuments(new Term("f", "perl"));
		writer.commit();
		
		DirectoryReader directoryReader6 = DirectoryReader.open(dir);
		IndexCommit indexCommit6 = directoryReader6.getIndexCommit();
		
		System.out.println(indexCommit6.getGeneration());
		System.out.println(indexCommit6.getSegmentCount());
		System.out.println(indexCommit6.getSegmentsFileName());
		System.out.println(indexCommit6.getFileNames());
		System.out.println(indexCommit6.isDeleted());
		
		writer.close();	
	}
}