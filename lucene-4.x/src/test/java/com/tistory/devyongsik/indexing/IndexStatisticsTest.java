package com.tistory.devyongsik.indexing;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class IndexStatisticsTest {

	private String[] contents = {"sir lion", "tiger", "the wolf lion"};
	private String[] contents2 = {"the hadoop", "hadoop drive hadoop", "tool"};

	private Directory directory = new RAMDirectory();

	private IndexWriter getWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_44, new WhitespaceAnalyzer(Version.LUCENE_44));
		IndexWriter indexWriter = new IndexWriter(directory, conf);

		return indexWriter;
	}

	@Before
	public void init() throws CorruptIndexException, LockObtainFailedException, IOException {

		IndexWriter indexWriter = getWriter();

		for(int i = 0; i < contents.length; i++) {
			Document doc = new Document();

			FieldType fieldType = new FieldType();
			fieldType.setIndexed(true);
			fieldType.setStored(true);
			fieldType.setTokenized(true);
			fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
			fieldType.setStoreTermVectors(true);


			fieldType.setTokenized(true);
			doc.add(new Field("contents", contents[i], fieldType));
			doc.add(new Field("contents2", contents2[i], fieldType));

			indexWriter.addDocument(doc);
		}

		indexWriter.commit();
		indexWriter.close();
	}

	@Test
	public void commonStatisticsFromTermsEnum() throws IOException {
		DirectoryReader directoryReader = DirectoryReader.open(directory);
		Fields fields = MultiFields.getFields(directoryReader);
		for (String fieldName : fields) {
			Terms termsInField = fields.terms(fieldName);

			TermsEnum termsEnum = termsInField.iterator(null);

			System.out.println("fieldName : " + fieldName);
			BytesRef term = null;
			while ((term = termsEnum.next()) != null) {
				System.out.println("doc Freq of ["+term.utf8ToString()+"] : " + termsEnum.docFreq());
				System.out.println("total term Freq of ["+term.utf8ToString()+"] : " + termsEnum.totalTermFreq());
				
				System.out.println("------------------------------------");
			}
		}
		
		System.out.println("##########################################");
	}
	
	@Test
	public void commonStatisticsFromTerms() throws IOException {
		DirectoryReader directoryReader = DirectoryReader.open(directory);
		Fields fields = MultiFields.getFields(directoryReader);

		for (String fieldName : fields) {
			Terms termsInField = fields.terms(fieldName);
			//size
			System.out.println("size : " + termsInField.size());
			//document count having this field
			System.out.println("doc count : " + fieldName + " : " + termsInField.getDocCount());
			//all terms in this field
			System.out.println("sum of doc freq : " + fieldName + " : " + termsInField.getSumDocFreq());
			//total term freq
			System.out.println("sum of total term freq : " + fieldName + " : " + termsInField.getSumTotalTermFreq());
			System.out.println("------------------------------------");
		}
		
		System.out.println("##########################################");
	}

	@Test
	public void commonStatisticsFromDocEnums() throws IOException {
		DirectoryReader directoryReader = DirectoryReader.open(directory);
		DocsEnum docsEnumForContents = MultiFields.getTermDocsEnum(directoryReader, MultiFields.getLiveDocs(directoryReader), "contents2", new BytesRef("hadoop"));

		@SuppressWarnings("unused")
		int doc;
		while((doc = docsEnumForContents.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
			System.out.println("freq from docsEnum : " + docsEnumForContents.freq());
		}
		
		System.out.println("##########################################");
	}
	
	@Test
	public void commonStatisticsFromSegment() throws IOException {
		DirectoryReader directoryReader = DirectoryReader.open(directory);
		
		System.out.println("max doc : " + directoryReader.maxDoc());
		System.out.println("num docs : " + directoryReader.numDocs());
		System.out.println("deleted docs : " + directoryReader.numDeletedDocs());
		System.out.println("sum of doc freq : " + directoryReader.getSumDocFreq("contents"));
		System.out.println("sum of total term freq : " + directoryReader.getSumTotalTermFreq("contents"));
		System.out.println("doc freq having this term in field : " + directoryReader.docFreq(new Term("contents", "lion")));
		
		System.out.println("##########################################");
		
	}
}
