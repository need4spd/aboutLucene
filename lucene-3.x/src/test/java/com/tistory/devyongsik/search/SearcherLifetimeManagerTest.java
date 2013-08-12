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
import org.apache.lucene.search.SearcherLifetimeManager;
import org.apache.lucene.search.SearcherLifetimeManager.PruneByAge;
import org.apache.lucene.search.SearcherLifetimeManager.Pruner;
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

public class SearcherLifetimeManagerTest {
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
	public void SearcherLifetimeSimpleTest() throws CorruptIndexException, LockObtainFailedException, IOException {
		SearcherManager searcherManager = new SearcherManager(directory, new SearcherFactory());
		
		SearcherLifetimeManager lifetimeManager = new SearcherLifetimeManager();
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		IndexSearcher newSearcher = lifetimeManager.acquire(1);
		Assert.assertNull(newSearcher);
		
		long token = lifetimeManager.record(indexSearcher);
		
		Assert.assertTrue(token > 0L);
		
		newSearcher = lifetimeManager.acquire(token);
		Assert.assertNotNull(newSearcher);
		
		docs = newSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
	}
	
	@Test
	public void differentSearcherTest() throws CorruptIndexException, LockObtainFailedException, IOException {
		SearcherManager searcherManager = new SearcherManager(directory, new SearcherFactory());
		SearcherLifetimeManager lifetimeManager = new SearcherLifetimeManager();
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);

		long firstSearcherToken = lifetimeManager.record(indexSearcher);
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		writer.commit();
		writer.close();
		
		searcherManager.maybeRefresh();
		
		IndexSearcher newSearcher = searcherManager.acquire();
		docs = newSearcher.search(q, 10);
		
		Assert.assertEquals(2, docs.totalHits);
		
		long newSearcherToken = lifetimeManager.record(newSearcher);
		
		//searcher release...
		searcherManager.release(indexSearcher);
		searcherManager.release(newSearcher);
		indexSearcher = null;
		newSearcher = null;
		
		indexSearcher = lifetimeManager.acquire(firstSearcherToken);
		newSearcher = lifetimeManager.acquire(newSearcherToken);
		
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		docs = newSearcher.search(q, 10);
		
		Assert.assertEquals(2, docs.totalHits);
		
	}
	
	
	@Test
	public void customPrunerTest() throws CorruptIndexException, LockObtainFailedException, IOException, InterruptedException {
		SearcherManager searcherManager = new SearcherManager(directory, new SearcherFactory());
		SearcherLifetimeManager lifetimeManager = new SearcherLifetimeManager();
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		long firstSearcherToken = lifetimeManager.record(indexSearcher);
		
		
		IndexSearcher newSearcher = lifetimeManager.acquire(firstSearcherToken);
		
		Assert.assertEquals(indexSearcher, newSearcher);
		Assert.assertTrue(indexSearcher == newSearcher);

		lifetimeManager.prune(new Pruner() {

			@Override
			public boolean doPrune(double arg0, IndexSearcher arg1) {
				return true;
			}
			
		}); //sec
		
		IndexSearcher thirdSearcher = lifetimeManager.acquire(firstSearcherToken);
		
		Assert.assertNull(thirdSearcher);
	}
	
	@Test
	public void prunerByAgeTest() throws CorruptIndexException, LockObtainFailedException, IOException, InterruptedException {
		SearcherManager searcherManager = new SearcherManager(directory, new SearcherFactory());
		SearcherLifetimeManager lifetimeManager = new SearcherLifetimeManager();
		
		IndexSearcher indexSearcher = searcherManager.acquire();
		long firstSearcherToken = lifetimeManager.record(indexSearcher);
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		writer.commit();
		writer.close();
		
		searcherManager.maybeRefresh();
		
		
		IndexSearcher newSearcher = searcherManager.acquire();
		long secondSearcherToken = lifetimeManager.record(newSearcher);
		
		Thread.sleep(2000);
		
		IndexSearcher thirdSearcher = lifetimeManager.acquire(firstSearcherToken);
		Assert.assertNotNull(thirdSearcher);
		
		lifetimeManager.prune(new PruneByAge(1.0));
		thirdSearcher = lifetimeManager.acquire(firstSearcherToken);
		Assert.assertNull(thirdSearcher);
		
		thirdSearcher = lifetimeManager.acquire(secondSearcherToken);
		Assert.assertNotNull(thirdSearcher);
		Assert.assertTrue(newSearcher == thirdSearcher);
	}
}
