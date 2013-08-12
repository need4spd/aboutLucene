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
import org.apache.lucene.search.NRTManager;
import org.apache.lucene.search.NRTManager.WaitingListener;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NRTManagerTest {
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
	public void NRTManagerSimpleTest() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = getWriter();
		NRTManager.TrackingIndexWriter trackingIndexWriter = new NRTManager.TrackingIndexWriter(writer);
		NRTManager nrtManager = new NRTManager(trackingIndexWriter, new SearcherFactory());
		
		IndexSearcher indexSearcher = nrtManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
	}
	
	@Test
	public void NRTManagerWithIndexWriterTest() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = getWriter();
		NRTManager.TrackingIndexWriter trackingIndexWriter = new NRTManager.TrackingIndexWriter(writer);
		NRTManager nrtManager = new NRTManager(trackingIndexWriter, new SearcherFactory());
		
		IndexSearcher indexSearcher = nrtManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		trackingIndexWriter.getIndexWriter().close();
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		trackingIndexWriter.addDocument(doc);
	}
	
	@Test
	public void NRTManagerAddDocument() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = getWriter();
		NRTManager.TrackingIndexWriter trackingIndexWriter = new NRTManager.TrackingIndexWriter(writer);
		NRTManager nrtManager = new NRTManager(trackingIndexWriter, new SearcherFactory());
		
		IndexSearcher indexSearcher = nrtManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		//Document 추
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		trackingIndexWriter.addDocument(doc);
		
		//기존 indexSearcher로는 반영안됨.
		docs = indexSearcher.search(q, 10);
		Assert.assertEquals(1, docs.totalHits);
		
		//새로 indexSearcher를 얻어와도 반영안됨.
		indexSearcher = nrtManager.acquire();
		
		docs = indexSearcher.search(q, 10);
		Assert.assertEquals(1, docs.totalHits);
		
		//maybeRefresh 실행 후 반영됨.
		nrtManager.maybeRefresh();
		
		indexSearcher = nrtManager.acquire();
		
		docs = indexSearcher.search(q, 10);
		Assert.assertEquals(2, docs.totalHits);
	}
	
	@Test
	public void NRTManagerGenerationTokenObtain() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = getWriter();
		NRTManager.TrackingIndexWriter trackingIndexWriter = new NRTManager.TrackingIndexWriter(writer);
		NRTManager nrtManager = new NRTManager(trackingIndexWriter, new SearcherFactory());
		
		long generation = nrtManager.getCurrentSearchingGen();
		Assert.assertEquals(0, generation);
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		long newGeneration = trackingIndexWriter.addDocument(doc);
		Assert.assertEquals(1, newGeneration);
		
		generation = nrtManager.getCurrentSearchingGen();
		Assert.assertEquals(0, generation);
		
		nrtManager.maybeRefresh();
		
		generation = nrtManager.getCurrentSearchingGen();
		Assert.assertEquals(1, generation);
	}
	
	@Test
	public void NRTManagerGenerationTokenObtainAfterSecondAdd() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = getWriter();
		NRTManager.TrackingIndexWriter trackingIndexWriter = new NRTManager.TrackingIndexWriter(writer);
		NRTManager nrtManager = new NRTManager(trackingIndexWriter, new SearcherFactory());
		
		Assert.assertEquals(0, nrtManager.getCurrentSearchingGen());
		
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		Assert.assertEquals(1, trackingIndexWriter.addDocument(doc));
		
		Document doc1 = new Document();
		doc1.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		Assert.assertEquals(1, trackingIndexWriter.addDocument(doc1));
		Assert.assertEquals(0, nrtManager.getCurrentSearchingGen());
		
		nrtManager.maybeRefresh();
		
		Assert.assertEquals(1, nrtManager.getCurrentSearchingGen());
		
		doc1.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		Assert.assertEquals(2, trackingIndexWriter.addDocument(doc1));
		
		nrtManager.maybeRefresh();
		
		Assert.assertEquals(2, nrtManager.getCurrentSearchingGen());
	}
	
	@Test
	public void NRTManagerDiffGenIndexSearcher() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = getWriter();
		NRTManager.TrackingIndexWriter trackingIndexWriter = new NRTManager.TrackingIndexWriter(writer);
		NRTManager nrtManager = new NRTManager(trackingIndexWriter, new SearcherFactory());
		
		long generation = nrtManager.getCurrentSearchingGen();
		Assert.assertEquals(0, generation);
		
		IndexSearcher indexSearcher = nrtManager.acquire();
		Term t = new Term("id", "4");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		//Document추가 후 refresh... increment generation
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		trackingIndexWriter.addDocument(doc);
		
		nrtManager.maybeRefresh();
		
		//기존 indexSearcher는 1개
		docs = indexSearcher.search(q, 10);	
		Assert.assertEquals(1, docs.totalHits);
		
		docs = nrtManager.acquire().search(q, 10);
		Assert.assertEquals(2, docs.totalHits);
	}
	
	@Test
	public void NRTManagerWaitForGeneration() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = getWriter();
		NRTManager.TrackingIndexWriter trackingIndexWriter = new NRTManager.TrackingIndexWriter(writer);
		NRTManager nrtManager = new NRTManager(trackingIndexWriter, new SearcherFactory());
		
		long generation = nrtManager.getCurrentSearchingGen();
		Assert.assertEquals(0, generation);
		
		//Document추가 후 refresh... increment generation
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		trackingIndexWriter.addDocument(doc);
		
		nrtManager.maybeRefresh();
		
		//기존 indexSearcher는 1개
		generation = nrtManager.getCurrentSearchingGen();
		Assert.assertEquals(1, generation);
		
		nrtManager.waitForGeneration(1);
		
	}
	
	@Test
	public void NRTManagerWaitingListener() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = getWriter();
		NRTManager.TrackingIndexWriter trackingIndexWriter = new NRTManager.TrackingIndexWriter(writer);
		NRTManager nrtManager = new NRTManager(trackingIndexWriter, new SearcherFactory());
		
		nrtManager.addWaitingListener(new WaitingListener() {

			@Override
			public void waiting(long arg0) {
				System.out.println("wait generation : " + arg0);
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				System.exit(-1);
			}
			
		});
		
		long generation = nrtManager.getCurrentSearchingGen();
		Assert.assertEquals(0, generation);
		
		//Document추가 후 refresh... increment generation
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		trackingIndexWriter.addDocument(doc);
		
		nrtManager.maybeRefresh();
		
		generation = nrtManager.getCurrentSearchingGen();
		Assert.assertEquals(1, generation);
		
		nrtManager.waitForGeneration(2);
		
	}
}