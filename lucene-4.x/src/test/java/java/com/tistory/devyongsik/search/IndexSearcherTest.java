package com.tistory.devyongsik.search;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo.IndexOptions;
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
import org.apache.lucene.util.BytesRef;
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
			
			fieldType.setTokenized(false);
			doc.add(new Field("titles2", titles[i], fieldType));
			
			fieldType.setTokenized(true);
			doc.add(new Field("contents", contents[i], fieldType));
			
			FieldType numFieldType = new FieldType();
			numFieldType.setIndexed(true);
			numFieldType.setStored(true);
			numFieldType.setTokenized(false);
			numFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
			numFieldType.setStoreTermVectors(true);
			numFieldType.setNumericType(NumericType.INT);
			
			IntField intField = new IntField("price", prices[i], numFieldType);
			
			doc.add(intField);
			
			indexWriter.addDocument(doc);
		}
		
		//indexWriter.commit();
		indexWriter.close();
	}
	
	@Test
	public void searchByTerm() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		
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
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		
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
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		Query q = new TermRangeQuery("titles2", new BytesRef("h"), new BytesRef("j"), true, true);
		
		System.out.println(q);
		
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(2, docs.totalHits);
	}
	
	@Test
	public void searchByNumericRangeQuery() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		
		Query q = NumericRangeQuery.newIntRange("price", 2000, 4000, true, true);
		TopDocs docs = indexSearcher.search(q, 10);
		
		Assert.assertEquals(2, docs.totalHits);
	}
}
