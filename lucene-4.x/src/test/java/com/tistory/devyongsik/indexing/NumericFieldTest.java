package com.tistory.devyongsik.indexing;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class NumericFieldTest {
	private int[] contents = {1,2,3};
	
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
			fieldType.setTokenized(false);
			fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
			fieldType.setStoreTermVectors(false);
			fieldType.setNumericType(NumericType.INT);
			
			doc.add(new IntField("contents", contents[i], fieldType));
			
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
				System.out.println("doc Freq of ["+term.bytes+"] : " + termsEnum.docFreq());
				System.out.println("total term Freq of ["+term.utf8ToString()+"] : " + termsEnum.totalTermFreq());
				
				System.out.println("------------------------------------");
			}
		}
		
		System.out.println("##########################################");
	}
}
