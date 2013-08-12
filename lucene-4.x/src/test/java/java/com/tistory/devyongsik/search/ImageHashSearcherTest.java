package com.tistory.devyongsik.search;

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
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.BytesRefFieldSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class ImageHashSearcherTest {
	private String[] ids = {"1","2","3"};
	private String[] titles = {"lucene","lucene","lucene"};
	private String[] hashes = {"12345","abcde","12345"};
	
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
			fieldType.setTokenized(false);
			fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
			fieldType.setStoreTermVectors(true);
			
			doc.add(new Field("ids", ids[i], fieldType));		
			doc.add(new Field("title", titles[i], fieldType));
			doc.add(new Field("hash", hashes[i], fieldType));
			
			indexWriter.addDocument(doc);
		}
		
		indexWriter.commit();
		indexWriter.close();
	}
	
	@Test
	public void searchByTerm() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		
		Term t = new Term("title", "lucene");
		Query q = new TermQuery(t);
		
		CustomScoreQuery customScoreQuery = new ImageHashDistanceScoreQuery(q);
		TopDocs docs = indexSearcher.search(customScoreQuery, 10);
		
		//Assert.assertEquals(1, docs.totalHits);
		
		
		//Assert.assertEquals(3, docs.totalHits);
		
		ScoreDoc[] hits = docs.scoreDocs;
		
		for(int i = 0; i < hits.length; i++) {
			Document resultDoc = indexSearcher.doc(hits[i].doc);
			System.out.println("id : " + resultDoc.get("id") + ", hash : " + resultDoc.get("hash") + ", score : " + hits[i].score);
		}
	}
	
	@Test
	public void searchFunctionDistance() throws CorruptIndexException, IOException {
		ValueSource source = new ImageHashDistanceFunction(new BytesRefFieldSource("hash"), "12345");
		
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		
		Term t = new Term("title", "lucene");
		Query q = new FunctionQuery(source);
		
		CustomScoreQuery customScoreQuery = new ImageHashDistanceScoreQuery(q);
		TopDocs docs = indexSearcher.search(customScoreQuery, 10);
		
		ScoreDoc[] hits = docs.scoreDocs;
		
		for(int i = 0; i < hits.length; i++) {
			Document resultDoc = indexSearcher.doc(hits[i].doc);
			System.out.println("id : " + resultDoc.get("id") + ", hash : " + resultDoc.get("hash") + ", score : " + hits[i].score);
		}
	}
}
