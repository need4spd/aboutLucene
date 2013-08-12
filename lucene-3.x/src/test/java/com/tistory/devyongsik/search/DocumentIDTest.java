package com.tistory.devyongsik.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class DocumentIDTest {

	//private Directory directory = new RAMDirectory();
	
	private Directory directory = null;
	
	private IndexWriter getWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		directory = FSDirectory.open(new File("/Users/need4spd/Programming/Java/aboutLucene_index"));
		
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, new WhitespaceAnalyzer(Version.LUCENE_35));
		IndexWriter indexWriter = new IndexWriter(directory, conf);
		
		return indexWriter;
	}
	
	@Before
	public void init() throws CorruptIndexException, LockObtainFailedException, IOException {
		
		IndexWriter indexWriter = getWriter();
		
		for(int i = 0; i < 6; i++) {
			Document doc = new Document();
			doc.add(new Field("id", String.valueOf(i), Field.Store.YES, Field.Index.NOT_ANALYZED));
			indexWriter.addDocument(doc);
		}
		
		indexWriter.commit();
		indexWriter.close();
	}
	
	@Test
	public void getDocumentByDocID() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		for(int i = 0; i < 5; i++) {
			Document doc = indexSearcher.doc(i);
		
			System.out.println(doc);
		}
		
		indexSearcher.close();
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(0), Field.Store.YES, Field.Index.NOT_ANALYZED));
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		writer.commit();
		writer.close();
		
		indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		for(int i = 0; i < 6; i++) {
			Document doc2 = indexSearcher.doc(i);
		
			System.out.println(doc2);
		}
	}
}
