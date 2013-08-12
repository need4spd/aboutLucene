package com.tistory.devyongsik.replication;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 6. 30.
 *
 */
public class ReplicationTest {

	private String indexPath = "d:/lucene_index_replacation/";

	
	public static void main(String[] args) throws IOException {
		ReplicationTest test = new ReplicationTest();
		
		//신규 인덱스 생성
		test.makeInitIndex();
		System.out.println("index init.....");
		
		//인덱스 udpate
		test.updateIndex();
		System.out.println("index updated.....");
		
		//replication
		
	}
	
	private void replication() {
		
	}
	
	private void updateIndex() throws IOException {
		Directory dir = FSDirectory.open(new File(indexPath));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
		iwc.setOpenMode(OpenMode.APPEND); 
		iwc.setIndexDeletionPolicy(new LastCommitDeletePolicy());
		
		IndexWriter writer = new IndexWriter(dir, iwc);

		Document doc1 = new Document();
		Field titleField = new Field("title", "퓨마", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
		titleField.setOmitTermFreqAndPositions(true);
		doc1.add(titleField);
		
		writer.addDocument(doc1);
		
		writer.commit();
		writer.close();		
	}
	
	private void makeInitIndex() throws IOException {

		System.out.println("Indexing to directory '" + indexPath + "'...");

		Directory dir = FSDirectory.open(new File(indexPath));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
		iwc.setOpenMode(OpenMode.CREATE); 
		iwc.setIndexDeletionPolicy(new LastCommitDeletePolicy());
		
		

		IndexWriter writer = new IndexWriter(dir, iwc);

		Document doc1 = new Document();
		Field titleField = new Field("title", "나이키", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
		titleField.setOmitTermFreqAndPositions(true);
		doc1.add(titleField);

		Document doc2 = new Document();
		titleField = new Field("title", "아디다스", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
		titleField.setOmitTermFreqAndPositions(true);
		doc2.add(titleField);

		if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
			writer.addDocument(doc1);
			writer.addDocument(doc2);
		} else {
			writer.updateDocument(new Term("title", "나이키"), doc1);
			writer.updateDocument(new Term("title", "아디다스"), doc2);
		}
		
		writer.commit();
		
		//IndexCommit indexCommit = writer.getConfig().getIndexCommit();
		
		//System.out.println("indexCommit : " + indexCommit.getSegmentsFileName());
		//System.out.println("indexCommit : " + indexCommit.getFileNames());
		
		
		writer.close();
	}
}