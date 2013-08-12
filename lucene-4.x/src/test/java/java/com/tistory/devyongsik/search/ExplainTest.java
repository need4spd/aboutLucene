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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
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
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_44, new WhitespaceAnalyzer(Version.LUCENE_44));
		IndexWriter indexWriter = new IndexWriter(directory, conf);

		return indexWriter;
	}

	@Before
	public void init() throws CorruptIndexException, LockObtainFailedException, IOException {

		IndexWriter indexWriter = getWriter();

		for(int i = 0; i < labels.length; i++) {
			Document doc = new Document();

			FieldType fieldType = new FieldType();
			fieldType.setIndexed(true);
			fieldType.setStored(true);
			fieldType.setTokenized(true);
			fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
			fieldType.setStoreTermVectors(true);

			doc.add(new Field("label", labels[i], fieldType));		

			indexWriter.addDocument(doc);
		}

		//indexWriter.commit();
		indexWriter.close();
	}

	@Test
	public void searchByTerm() throws IOException, ParseException {
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));

		String querystr = "시크릿 가든 주연 여자 배우";

//		StandardQueryParser qpHelper = new StandardQueryParser();
//		StandardQueryConfigHandler config =  qpHelper.getQueryConfigHandler();
//		config.setAllowLeadingWildcard(true);
//		config.setAnalyzer(new WhitespaceAnalyzer());
//		Query query = qpHelper.parse("apache AND lucene", "defaultField");

		Query q = new QueryParser(Version.LUCENE_44, "label", new WhitespaceAnalyzer(Version.LUCENE_44)).parse(querystr);
		TopScoreDocCollector collector = TopScoreDocCollector.create(20, true);
		indexSearcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		for(int i=0;i<hits.length;++i) {
			int docId = hits[i].doc;
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