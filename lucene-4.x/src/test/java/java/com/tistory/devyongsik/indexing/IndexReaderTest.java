package com.tistory.devyongsik.indexing;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IndexReaderTest {
	private String[] ids = {"1","2","3"};
	private String[] titles = {"lucene in action action","hadoop in action","ibatis in action"};
	private String[] contents = {"lucene is a search engine", "hadoop is a dist file system","ibatis is a db mapping tool"};
	
	private Directory directory = new RAMDirectory();
	
	@Before
	public void init() throws CorruptIndexException, LockObtainFailedException, IOException {
		
		IndexWriter indexWriter = getWriter();
		
		for(int i = 0; i < ids.length; i++) {
			Document doc = new Document();
			FieldType fieldType = new FieldType();
			fieldType.setIndexed(true);
			fieldType.setStored(true);
			fieldType.setTokenized(true);
			fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
			fieldType.setStoreTermVectors(true);
			
			doc.add(new Field("ids", ids[i], fieldType));		
			doc.add(new Field("titles", titles[i], fieldType));
			doc.add(new Field("contents", contents[i], fieldType));
			
			indexWriter.addDocument(doc);
		}
		
		indexWriter.commit();
		indexWriter.close();
	}
	
	private IndexWriter getWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_44, new WhitespaceAnalyzer(Version.LUCENE_44));
		IndexWriter indexWriter = new IndexWriter(directory, conf);
		
		return indexWriter;
	}

	
	@Test
	public void DirecoryReaderTest() throws CorruptIndexException, IOException {
		DirectoryReader directoryReader = DirectoryReader.open(directory);
		
		Assert.assertEquals(ids.length, directoryReader.numDocs());
		Assert.assertEquals(ids.length, directoryReader.maxDoc());
		
		directoryReader.close();
	}
	
	@Test
	public void tfidfScore() throws CorruptIndexException, IOException {
		DirectoryReader directoryReader = DirectoryReader.open(directory);
		int actionTermFreq = directoryReader.docFreq(new Term("titles","action"));
		
		System.out.println(actionTermFreq); //문서의 개수
		
		int totalDoc = directoryReader.numDocs();
		
		double idf = Math.log( (double)totalDoc / (double)actionTermFreq+1d);
		
		System.out.println(idf); //문서의 개수
		
		idf = Math.log( (double)totalDoc - (double)actionTermFreq / (double)actionTermFreq);
		
		System.out.println(idf); //문서의 개수
	}
}