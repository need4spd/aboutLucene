package com.tistory.devyongsik.sort;

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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 28.
 *
 */
public class SortTest {
	private String[] ids = {"1","2","3"};
	private String[] titles = {"lucene in action action","hadoop in action","ibatis in action"};
	private String[] contents = {"java search lucene is a search engine made by java", "hadoop is a dist file system with java","ibatis is a db mapping tool"};
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
			doc.add(new Field("titles2", titles[i], fieldType));
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
	public void sortByField() throws IOException {
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		
		Query q = new MatchAllDocsQuery();
		SortField sortField = new SortField("titles2", SortField.Type.STRING, true);
		Sort sort = new Sort(sortField);
		
		TopFieldDocs docs = indexSearcher.search(q, 10, sort);
		ScoreDoc[] hits = docs.scoreDocs;
		
		for(int i = 0; i < hits.length; i++) {
			System.out.println(hits[i]);
		}
	}
	
	@Test
	public void sortByPriceField() throws IOException {
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		
		Query q = new MatchAllDocsQuery();
		SortField sortField = new SortField("price", SortField.Type.INT, true);
		Sort sort = new Sort(sortField);
		
		TopFieldDocs docs = indexSearcher.search(q, 10, sort);
		ScoreDoc[] hits = docs.scoreDocs;
		
		for(int i = 0; i < hits.length; i++) {
			System.out.println(hits[i]);
		}
	}
}
