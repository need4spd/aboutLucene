package com.tistory.devyongsik.indexing;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 21.
 *
 */
public class IndexWriterTest {
	private String[] ids = {"1","2","3"};
	private String[] titles = {"lucene in action action","hadoop in action","ibatis in action"};
	private String[] contents = {"lucene is a search engine", "hadoop is a dist file system","ibatis is a db mapping tool"};
	
	private Directory directory = new RAMDirectory();
	
	private IndexWriter getWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_33, new WhitespaceAnalyzer(Version.LUCENE_33));
		IndexWriter indexWriter = new IndexWriter(directory, conf);
		
		return indexWriter;
	}
	
	@Before
	public void init() throws CorruptIndexException, LockObtainFailedException, IOException {
		
		IndexWriter indexWriter = getWriter();
		
		for(int i = 0; i < ids.length; i++) {
			Document doc = new Document();
			doc.add(new Field("ids", ids[i], Field.Store.YES, Field.Index.NOT_ANALYZED));		
			doc.add(new Field("titles", titles[i], Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("contents", contents[i], Field.Store.YES, Field.Index.ANALYZED, TermVector.YES));
			
			indexWriter.addDocument(doc);
		}
		
		//indexWriter.commit();
		indexWriter.close();
	}
	
	@Test
	public void testIndexWriterNumDoc() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter indexWriter = getWriter();
		Assert.assertEquals(ids.length, indexWriter.numDocs());
		indexWriter.close();
	}
	
	@Test
	public void testDeleteDocument() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter indexWriter = getWriter();
		Assert.assertEquals(ids.length, indexWriter.numDocs());
		
		indexWriter.deleteDocuments(new Term("ids", "1"));
		Assert.assertEquals(ids.length, indexWriter.numDocs());
		indexWriter.commit();
		
		Assert.assertEquals(ids.length-1, indexWriter.numDocs());
		indexWriter.close();
	}
	
	@Test
	public void testDeleteAndOptimizeDocument() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter indexWriter = getWriter();
		Assert.assertEquals(ids.length, indexWriter.numDocs());
		Assert.assertEquals(ids.length, indexWriter.maxDoc());
		
		indexWriter.deleteDocuments(new Term("ids", "1"));
		Assert.assertEquals(ids.length, indexWriter.numDocs());
		Assert.assertEquals(ids.length, indexWriter.maxDoc());
		indexWriter.commit();
		
		Assert.assertEquals(ids.length-1, indexWriter.numDocs());
		Assert.assertEquals(ids.length, indexWriter.maxDoc());
		
		Assert.assertEquals(ids.length-1, indexWriter.numDocs());
		Assert.assertEquals(ids.length-1, indexWriter.maxDoc());
		
		indexWriter.close();
	}
}