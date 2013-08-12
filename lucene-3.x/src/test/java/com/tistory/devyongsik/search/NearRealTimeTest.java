package com.tistory.devyongsik.search;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 27.
 *
 */
public class NearRealTimeTest {
	@Test
	 public void testNearRealTime() throws Exception {
	        Directory dir = new RAMDirectory();
	        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36, new WhitespaceAnalyzer(Version.LUCENE_36));
			IndexWriter writer = new IndexWriter(dir, conf);
			
			for (int i = 0; i < 10; i++) {
	            Document doc = new Document();
	            doc.add(new Field("id", "" + i, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
	            doc.add(new Field("text", "aaa", Field.Store.NO, Field.Index.ANALYZED));
	            writer.addDocument(doc);
	        }
	        
			IndexReader reader = IndexReader.open(writer, true);
	        IndexSearcher searcher = new IndexSearcher(reader);

	        Query query = new TermQuery(new Term("text", "aaa"));
	        TopDocs docs = searcher.search(query, 1);
	        Assert.assertEquals(10, docs.totalHits);
	        Assert.assertEquals(1,docs.scoreDocs.length);

	        writer.deleteDocuments(new Term("id", "7"));
	        Document doc = new Document();
	        doc.add(new Field("id", "11", Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
	        doc.add(new Field("text", "bbb", Field.Store.NO, Field.Index.ANALYZED));
	        writer.addDocument(doc);
	        IndexReader newReader = IndexReader.openIfChanged(reader);
	        Assert.assertFalse(reader == newReader);

	        reader.close();
	        searcher = new IndexSearcher(newReader);
	        TopDocs hits = searcher.search(query, 10);
	        Assert.assertEquals(9, hits.totalHits);

	        query = new TermQuery(new Term("text", "bbb"));
	        hits = searcher.search(query, 1);
	        Assert.assertEquals(1, hits.totalHits);
	        newReader.close();
	        writer.close();
	    }
	
	@Test
	 public void testNearRealTimeRemoveDeprecated() throws Exception {
	        Directory dir = new RAMDirectory();
	        
	        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36, new WhitespaceAnalyzer(Version.LUCENE_36));
	        IndexWriter writer = new IndexWriter(dir, conf);
	        
	        for (int i = 0; i < 10; i++) {
	            Document doc = new Document();
	            doc.add(new Field("id", "" + i, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
	            doc.add(new Field("text", "aaa", Field.Store.NO, Field.Index.ANALYZED));
	            writer.addDocument(doc);
	        }
	        IndexReader reader = IndexReader.open(writer, true);
	        IndexSearcher searcher = new IndexSearcher(reader);

	        Query query = new TermQuery(new Term("text", "aaa"));
	        TopDocs docs = searcher.search(query, 1);
	        Assert.assertEquals(10, docs.totalHits);
	        Assert.assertEquals(1,docs.scoreDocs.length);

	        writer.deleteDocuments(new Term("id", "7"));
	        Document doc = new Document();
	        doc.add(new Field("id", "11", Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
	        doc.add(new Field("text", "bbb", Field.Store.NO, Field.Index.ANALYZED));
	        writer.addDocument(doc);
	        IndexReader newReader = IndexReader.openIfChanged(reader);
	        Assert.assertFalse(reader == newReader);

	        reader.close();
	        searcher = new IndexSearcher(newReader);
	        TopDocs hits = searcher.search(query, 10);
	        Assert.assertEquals(9, hits.totalHits);

	        query = new TermQuery(new Term("text", "bbb"));
	        hits = searcher.search(query, 1);
	        Assert.assertEquals(1, hits.totalHits);
	        newReader.close();
	        writer.close();
	    }

	@Test
	 public void testNearRealTimeRemoveDeprecatedDelete() throws Exception {
	        Directory dir = new RAMDirectory();
	        
	        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_33, new WhitespaceAnalyzer(Version.LUCENE_33));
	        IndexWriter writer = new IndexWriter(dir, conf);
	        
	        for (int i = 0; i < 10; i++) {
	            Document doc = new Document();
	            doc.add(new Field("id", "" + i, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
	            doc.add(new Field("text", "aaa", Field.Store.NO, Field.Index.ANALYZED));
	            writer.addDocument(doc);
	        }
	        IndexReader reader = IndexReader.open(writer, true);
	        IndexSearcher searcher = new IndexSearcher(reader);

	        Query query = new TermQuery(new Term("text", "aaa"));
	        TopDocs docs = searcher.search(query, 1);
	        Assert.assertEquals(10, docs.totalHits);
	        Assert.assertEquals(1,docs.scoreDocs.length);

	        writer.deleteDocuments(new Term("id", "7"));
	        Document doc = new Document();
	        doc.add(new Field("id", "11", Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
	        doc.add(new Field("text", "bbb", Field.Store.NO, Field.Index.ANALYZED));
	        writer.addDocument(doc);
	        IndexReader newReader = IndexReader.openIfChanged(reader);
	        Assert.assertFalse(reader == newReader);

	        reader.close();
	        searcher = new IndexSearcher(newReader);
	        TopDocs hits = searcher.search(query, 10);
	        Assert.assertEquals(9, hits.totalHits);

	        query = new TermQuery(new Term("text", "bbb"));
	        hits = searcher.search(query, 1);
	        Assert.assertEquals(1, hits.totalHits);
	         
	        writer.deleteDocuments(new Term("text","bbb"));
	        IndexReader newReader2 = IndexReader.openIfChanged(newReader);
	        Assert.assertFalse(newReader == newReader2);

	        newReader.close();
	        query = new TermQuery(new Term("text", "bbb"));
	        searcher = new IndexSearcher(newReader2);
	        hits = searcher.search(query, 1);
	        Assert.assertEquals(0, hits.totalHits);
	        newReader2.close();
	        writer.close();
	        
	    }
}