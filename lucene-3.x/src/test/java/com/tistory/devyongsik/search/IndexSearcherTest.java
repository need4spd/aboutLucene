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
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IndexSearcherTest {
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
		
		indexWriter.commit();
		indexWriter.close();
	}
	
	@Test
	public void searchAfterAddDoc() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		Term t = new Term("ids", "1");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		t = new Term("titles", "action");
		q = new TermQuery(t);
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(3, docs.totalHits);
		
		Document doc = new Document();
		doc.add(new Field("ids", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));		
		doc.add(new Field("titles", "title", Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("titles2", "title2", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field("contents", "contents", Field.Store.YES, Field.Index.ANALYZED, TermVector.YES));
		
		NumericField numField = new NumericField("price", Field.Store.YES, true);
		numField.setIntValue(1000);
		doc.add(numField);
		
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		
		Term t2 = new Term("ids", "1");
		Query q2 = new TermQuery(t2);
		TopDocs docs2 = indexSearcher.search(q2, 10);
		
		Assert.assertEquals(1, docs2.totalHits);
		
		writer.commit();
		writer.close();
		
		docs2 = indexSearcher.search(q2, 10);
		
		Assert.assertEquals(1, docs2.totalHits);
	}
	
	@Test
	public void searchAfterAddDocReCreateSearcher() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		Term t = new Term("ids", "1");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		t = new Term("titles", "action");
		q = new TermQuery(t);
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(3, docs.totalHits);
		
		Document doc = new Document();
		doc.add(new Field("ids", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));		
		doc.add(new Field("titles", "title", Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("titles2", "title2", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field("contents", "contents", Field.Store.YES, Field.Index.ANALYZED, TermVector.YES));
		
		NumericField numField = new NumericField("price", Field.Store.YES, true);
		numField.setIntValue(1000);
		doc.add(numField);
		
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		
		Term t2 = new Term("ids", "1");
		Query q2 = new TermQuery(t2);
		TopDocs docs2 = indexSearcher.search(q2, 10);
		
		Assert.assertEquals(1, docs2.totalHits);
		
		writer.commit();
		writer.close();
		
		indexSearcher = new IndexSearcher(IndexReader.open(directory));
		docs2 = indexSearcher.search(q2, 10);
		
		Assert.assertEquals(2, docs2.totalHits);
	}
	
	
	/*
	 * commit을 하지 않으면 IndexSearcher를 다시 만들어도 검색에 반영되지 않는다.
	 */
	@Test
	public void searchAfterAddDocReCreateSearcherWithNoCommit() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		Term t = new Term("ids", "1");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		t = new Term("titles", "action");
		q = new TermQuery(t);
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(3, docs.totalHits);
		
		Document doc = new Document();
		doc.add(new Field("ids", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));		
		doc.add(new Field("titles", "title", Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("titles2", "title2", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field("contents", "contents", Field.Store.YES, Field.Index.ANALYZED, TermVector.YES));
		
		NumericField numField = new NumericField("price", Field.Store.YES, true);
		numField.setIntValue(1000);
		doc.add(numField);
		
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		
		Term t2 = new Term("ids", "1");
		Query q2 = new TermQuery(t2);
		TopDocs docs2 = indexSearcher.search(q2, 10);
		
		Assert.assertEquals(1, docs2.totalHits);
		
		//writer.commit();
		//writer.close();
		
		indexSearcher = new IndexSearcher(IndexReader.open(directory));
		docs2 = indexSearcher.search(q2, 10);
		
		Assert.assertEquals(1, docs2.totalHits);
		
		writer.commit();
		writer.close();
	}
	
	/*
	 * commit을 하지 않으면 IndexReader.openIfChanged도 null을 반환한다.
	 */
	@Test
	public void searchAfterAddDocReCreateReaderAndSearcherWithNoCommit() throws CorruptIndexException, IOException {
		IndexReader reader = IndexReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		
		Term t = new Term("ids", "1");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		t = new Term("titles", "action");
		q = new TermQuery(t);
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(3, docs.totalHits);
		
		Document doc = new Document();
		doc.add(new Field("ids", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));		
		doc.add(new Field("titles", "title", Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("titles2", "title2", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field("contents", "contents", Field.Store.YES, Field.Index.ANALYZED, TermVector.YES));
		
		NumericField numField = new NumericField("price", Field.Store.YES, true);
		numField.setIntValue(1000);
		doc.add(numField);
		
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		
		Term t2 = new Term("ids", "1");
		Query q2 = new TermQuery(t2);
		TopDocs docs2 = indexSearcher.search(q2, 10);
		
		Assert.assertEquals(1, docs2.totalHits);
		
		//writer.commit();
		//writer.close();
		
		IndexReader newReader = IndexReader.openIfChanged(reader);
		
		Assert.assertNull(newReader);
		
		writer.commit();
		writer.close();
	}
	
	/*
	 * commit을 해야 IndexReader.openIfChanged가 새로운 reader를 반환한다.
	 */
	@Test
	public void searchAfterAddDocReCreateReaderAndSearcherWithCommit() throws CorruptIndexException, IOException {
		IndexReader reader = IndexReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		
		Term t = new Term("ids", "1");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		t = new Term("titles", "action");
		q = new TermQuery(t);
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(3, docs.totalHits);
		
		Document doc = new Document();
		doc.add(new Field("ids", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));		
		doc.add(new Field("titles", "title", Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("titles2", "title2", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field("contents", "contents", Field.Store.YES, Field.Index.ANALYZED, TermVector.YES));
		
		NumericField numField = new NumericField("price", Field.Store.YES, true);
		numField.setIntValue(1000);
		doc.add(numField);
		
		IndexWriter writer = getWriter();
		writer.addDocument(doc);
		
		Term t2 = new Term("ids", "1");
		Query q2 = new TermQuery(t2);
		TopDocs docs2 = indexSearcher.search(q2, 10);
		
		Assert.assertEquals(1, docs2.totalHits);
		
		writer.commit();
		//writer.close();
		
		IndexReader newReader = IndexReader.openIfChanged(reader);
		
		indexSearcher = new IndexSearcher(newReader);
		docs2 = indexSearcher.search(q2, 10);
		
		Assert.assertEquals(2, docs2.totalHits);
		
		writer.commit();
		writer.close();
	}
	
	@Test
	public void searchByTerm() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		Term t = new Term("ids", "1");
		Query q = new TermQuery(t);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(1, docs.totalHits);
		
		t = new Term("titles", "action");
		q = new TermQuery(t);
		docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(3, docs.totalHits);
		
		ScoreDoc[] hits = docs.scoreDocs;
		
		for(int i = 0; i < hits.length; i++) {
			System.out.println(hits[i].doc);
			System.out.println(hits[i].score);
			
			Document resultDoc = indexSearcher.doc(hits[i].doc);
			System.out.println(resultDoc.get("titles"));
		}
	}
	
	@Test
	public void searchByBooleanQuery() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		BooleanQuery resultQuery = new BooleanQuery();
		
		Term t = new Term("ids", "1");
		Query q = new TermQuery(t);
		
		resultQuery.add(q, Occur.SHOULD);
		
		Term t2 = new Term("contents", "ibatis");
		Query q2 = new TermQuery(t2);
		
		resultQuery.add(q2, Occur.SHOULD);
		
		TopDocs docs = indexSearcher.search(resultQuery, 10);
		
		Assert.assertEquals(2, docs.totalHits);
	}
	
	@Test
	public void searchByTermRangeQuery() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		Query q = new TermRangeQuery("titles2", "h", "j", true, true);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(2, docs.totalHits);
	}
	
	@Test
	public void searchByNumericRangeQuery() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
		
		Query q = NumericRangeQuery.newIntRange("price", 2000, 4000, true, true);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(2, docs.totalHits);
	}
}