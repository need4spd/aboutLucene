package com.tistory.devyongsik.search;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SearcherManagerTest {
	private Directory directory = new RAMDirectory();
	
	private IndexWriter getWriter() throws CorruptIndexException, LockObtainFailedException, IOException {

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
	public void acquireWithIndexWriterCommit() throws IOException {
//		SearcherManager searcherManager = new SearcherManager(directory, new SearcherWarmer(){
//
//			@Override
//			public void warm(IndexSearcher arg0) throws IOException {
//				System.out.println("nothing to do");
//				
//			}
//			
//		}, null);
		
		SearcherManager searcherManager = new SearcherManager(directory, new SearcherFactory());
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		searcherManager.release(indexSearcher);
		indexSearcher = null;
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		writer.commit();
		writer.close();
		
		indexSearcher = searcherManager.acquire();
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
	}
	
	@Test
	public void acquireWithIndexWriterNoCommit() throws IOException {
//		SearcherManager searcherManager = new SearcherManager(directory, new SearcherWarmer(){
//
//			@Override
//			public void warm(IndexSearcher arg0) throws IOException {
//				System.out.println("nothing to do");
//				
//			}
//			
//		}, null);
		
		SearcherManager searcherManager = new SearcherManager(directory, new SearcherFactory());
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		searcherManager.release(indexSearcher);
		indexSearcher = null;
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		//writer.commit();
		//writer.close();
		
		indexSearcher = searcherManager.acquire();
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
	}
	
	@Test
	public void maybeRefreshWithNoCommit() throws Exception {
//		SearcherManager searcherManager = new SearcherManager(directory, new SearcherWarmer(){
//
//			@Override
//			public void warm(IndexSearcher arg0) throws IOException {
//				System.out.println("nothing to do");
//				
//			}
//			
//		}, null);
		
		SearcherManager searcherManager = new SearcherManager(directory, new SearcherFactory());
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		searcherManager.release(indexSearcher);
		indexSearcher = null;
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		//writer.commit();
		//writer.close();
		
		searcherManager.maybeRefresh();
		
		indexSearcher = searcherManager.acquire();
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
	}
	
	@Test
	public void maybeRefreshWithCommit() throws Exception {
//		SearcherManager searcherManager = new SearcherManager(directory, new SearcherWarmer(){
//
//			@Override
//			public void warm(IndexSearcher arg0) throws IOException {
//				System.out.println("nothing to do");
//				
//			}
//			
//		}, null);
		
		SearcherManager searcherManager = new SearcherManager(directory, new SearcherFactory());
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		searcherManager.release(indexSearcher);
		indexSearcher = null;
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		writer.commit();
		writer.close();
		
		//searcherManager.maybeReopen();
		searcherManager.maybeRefresh();
		
		indexSearcher = searcherManager.acquire();
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(2, docs.totalHits);
	}
	
	@Test
	public void acquireIndexWriterWithNoCommit() throws Exception {
//		SearcherManager searcherManager = new SearcherManager(directory, new SearcherWarmer(){
//
//			@Override
//			public void warm(IndexSearcher arg0) throws IOException {
//				System.out.println("nothing to do");
//				
//			}
//			
//		}, null);
		
		IndexWriter indexWriter = getWriter();
		SearcherManager searcherManager = new SearcherManager(indexWriter, true, new SearcherFactory());
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		searcherManager.release(indexSearcher);
		indexSearcher = null;
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		indexWriter.addDocument(doc);
		
		indexSearcher = searcherManager.acquire();
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
	}
	
	@Test
	public void maybeRefreshIndexWriterWithNoCommit() throws Exception {
//		SearcherManager searcherManager = new SearcherManager(directory, new SearcherWarmer(){
//
//			@Override
//			public void warm(IndexSearcher arg0) throws IOException {
//				System.out.println("nothing to do");
//				
//			}
//			
//		}, null);
		
		IndexWriter indexWriter = getWriter();
		SearcherManager searcherManager = new SearcherManager(indexWriter, true, new SearcherFactory());
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		searcherManager.release(indexSearcher);
		indexSearcher = null;
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		indexWriter.addDocument(doc);
		
		searcherManager.maybeRefresh();
		
		indexSearcher = searcherManager.acquire();
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(2, docs.totalHits);
	}
}
