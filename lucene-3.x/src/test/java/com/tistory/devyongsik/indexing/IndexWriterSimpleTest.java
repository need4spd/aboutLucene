package com.tistory.devyongsik.indexing;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

/**
 * author : need4spd, need4spd@naver.com, 2013. 3. 13.
 */
public class IndexWriterSimpleTest {

	@Test
	public void updateTest() throws CorruptIndexException, LockObtainFailedException, IOException {
		Directory directory = new RAMDirectory();
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36, new WhitespaceAnalyzer(Version.LUCENE_36));
		IndexWriter indexWriter = new IndexWriter(directory, conf);

		Document doc = new Document();
		doc.add(new Field("id", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("title", "document 1", Field.Store.YES, Field.Index.NOT_ANALYZED));		
		indexWriter.addDocument(doc);
		indexWriter.commit();


		IndexReader reader = IndexReader.open(indexWriter, true);
		IndexSearcher indexSearcher = new IndexSearcher(reader);

		Term t = new Term("id", "1");
		TopDocs topDocs = indexSearcher.search(new TermQuery(t), 10);

		Assert.assertTrue(topDocs.totalHits == 1);

		//update by id=1
		Term updateTerm = new Term("id", "1");
		Document updateDoc = new Document();
		updateDoc.add(new Field("id", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));
		updateDoc.add(new Field("title", "document 1 update", Field.Store.YES, Field.Index.NOT_ANALYZED));

		indexWriter.updateDocument(updateTerm, updateDoc);
		indexWriter.commit();

		IndexReader newReader = IndexReader.openIfChanged(reader);
		indexSearcher = new IndexSearcher(newReader);

		topDocs = indexSearcher.search(new TermQuery(updateTerm), 10);

		Assert.assertTrue(topDocs.totalHits == 1);
		Assert.assertEquals("document 1 update", indexSearcher.doc(topDocs.scoreDocs[0].doc).get("title"));
		
		//update by id=2
		Term updateTerm2 = new Term("id", "2");
		Document updateDoc2 = new Document();
		updateDoc2.add(new Field("id", "2", Field.Store.YES, Field.Index.NOT_ANALYZED));
		updateDoc2.add(new Field("title", "document 2 update", Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		indexWriter.updateDocument(updateTerm2, updateDoc2);
		indexWriter.commit();
		
		IndexReader newReader2 = IndexReader.openIfChanged(reader);
		indexSearcher = new IndexSearcher(newReader2);

		topDocs = indexSearcher.search(new TermQuery(updateTerm2), 10);

		Assert.assertTrue(topDocs.totalHits == 1);
		Assert.assertEquals("document 2 update", indexSearcher.doc(topDocs.scoreDocs[0].doc).get("title"));
		
		topDocs = indexSearcher.search(new MatchAllDocsQuery(), 10);
		Assert.assertTrue(topDocs.totalHits == 2);
	}
}