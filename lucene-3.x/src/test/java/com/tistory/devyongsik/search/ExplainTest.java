package com.tistory.devyongsik.search;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class ExplainTest {
	private String[] labels = {"시크릿 시크릿 가든"};

	private Directory directory = new RAMDirectory();

	private IndexWriter getWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_33, new WhitespaceAnalyzer(Version.LUCENE_33));
		IndexWriter indexWriter = new IndexWriter(directory, conf);

		return indexWriter;
	}

	@Before
	public void init() throws CorruptIndexException, LockObtainFailedException, IOException {

		IndexWriter indexWriter = getWriter();

		for(int i = 0; i < labels.length; i++) {
			Document doc = new Document();
			doc.add(new Field("label", labels[i], Field.Store.YES, Field.Index.ANALYZED));		

			indexWriter.addDocument(doc);
		}

		indexWriter.commit();
		
		for(int i = 0; i < labels.length; i++) {
			Document doc = new Document();
			doc.add(new Field("label", labels[i], Field.Store.YES, Field.Index.ANALYZED));		

			indexWriter.addDocument(doc);
		}
		
		//indexWriter.close();
	}

	@Test
	public void searchByTerm() throws CorruptIndexException, IOException, ParseException {
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));

		String querystr = "시크릿 가든 주연 여자 배우";
		Query q = new QueryParser(Version.LUCENE_33, "label", new WhitespaceAnalyzer(Version.LUCENE_33)).parse(querystr);
		TopScoreDocCollector collector = TopScoreDocCollector.create(20, true);
		indexSearcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		
		Similarity similarity = indexSearcher.getSimilarity();
		
		System.out.println(similarity.tf(1));
		
		for(int i=0;i<hits.length;++i) {
			int docId = hits[i].doc;
			//Document d = indexSearcher.doc(docId);
			Explanation explanation = indexSearcher.explain(q, docId);
			System.out.println(explanation.toString());
		}
	}
	
	@Test
	public void calIdf() {
		float idf = (float)(Math.log(1/(double)(1+1)) + 1.0);
		
		System.out.println("idf : " + idf);
	}
}