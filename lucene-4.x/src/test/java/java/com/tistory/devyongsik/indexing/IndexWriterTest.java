package com.tistory.devyongsik.indexing;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
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
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_44, new WhitespaceAnalyzer(Version.LUCENE_44));
		IndexWriter indexWriter = new IndexWriter(directory, conf);
		
		return indexWriter;
	}
	
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
		
		//ids 1¹ø ¹®¼­ »èÁ¦
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
		
		//ids 1¹ø ¹®¼­ »èÁ¦
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