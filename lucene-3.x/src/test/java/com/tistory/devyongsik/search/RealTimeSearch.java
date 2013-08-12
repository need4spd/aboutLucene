package com.tistory.devyongsik.search;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 27.
 *
 */
public class RealTimeSearch {
	private String[] ids = {"1","2","3"};
	private String[] titles = {"lucene in action action","hadoop in action","ibatis in action"};
	private String[] contents = {"lucene is a search engine", "hadoop is a dist file system","ibatis is a db mapping tool"};
	private int[] prices = {4000, 5000, 2000};
	
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
			doc.add(new Field("titles2", titles[i], Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
			doc.add(new Field("contents", contents[i], Field.Store.YES, Field.Index.ANALYZED, TermVector.YES));
			
			NumericField numField = new NumericField("price", Field.Store.YES, true);
			numField.setIntValue(prices[i]);
			doc.add(numField);
			
			indexWriter.addDocument(doc);
		}
		
		//indexWriter.commit();
		indexWriter.close();
	}
	
	private void deleteDocument() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter indexWriter = getWriter();
		indexWriter.deleteDocuments(new Term("ids", "1"));
		indexWriter.commit();
		indexWriter.close();
	}
	
	private void addDocument() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter indexWriter = getWriter();
		
		Document doc = new Document();
		doc.add(new Field("ids", "4", Field.Store.YES, Field.Index.NOT_ANALYZED));		
		doc.add(new Field("titles", "computer", Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("titles2", "computer", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		
		indexWriter.addDocument(doc);
		indexWriter.commit();
		indexWriter.close();
	}

	@Test
	public void searchAfterDocumentDeleted() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		Term t = new Term("ids", "1");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		//삭제 후 다시 검색 해 본다.
		deleteDocument();
		
		docs = indexSearcher.search(q, 10);
		Assert.assertEquals(1, docs.totalHits);
	}
	
	
	@Test
	public void searchAfterDocumentAdded() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		Term t = new Term("ids", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(0, docs.totalHits);
		
		//추가 후 다시 검색 해 본다.
		addDocument();
		
		docs = indexSearcher.search(q, 10);
		Assert.assertEquals(0, docs.totalHits);
	}
	
	@Test
	public void searchAfterDocumentDeleteIndexReaderReopen() throws CorruptIndexException, IOException {
		//IndexReader를 얻어온다.
		IndexReader indexReader = IndexReader.open(directory);		
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		
		Assert.assertTrue(indexReader.isCurrent());
		
		Term t = new Term("ids", "1");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		//삭제 후 다시 검색 해 본다.
		deleteDocument();
		
		Assert.assertFalse(indexReader.isCurrent());
		
		IndexReader newReader = IndexReader.openIfChanged(indexReader);
		if(newReader != indexReader) {
			indexSearcher = new IndexSearcher(newReader);
			indexReader.close();
		}
		
		docs = indexSearcher.search(q, 10);
		Assert.assertEquals(0, docs.totalHits);
	}
	
	@Test
	public void searchAfterDocumentAddedIndexReaderReopen() throws CorruptIndexException, IOException {
		//IndexReader를 얻어온다.
		IndexReader indexReader = IndexReader.open(directory);		
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		
		Assert.assertTrue(indexReader.isCurrent());
		
		Term t = new Term("ids", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(0, docs.totalHits);
		
		//삭제 후 다시 검색 해 본다.
		addDocument();
		
		Assert.assertFalse(indexReader.isCurrent());
		
		IndexReader newReader = IndexReader.openIfChanged(indexReader);
		if(newReader != indexReader) {
			indexSearcher = new IndexSearcher(newReader);
			indexReader.close();
		}
		
		docs = indexSearcher.search(q, 10);
		Assert.assertEquals(1, docs.totalHits);
	}
}