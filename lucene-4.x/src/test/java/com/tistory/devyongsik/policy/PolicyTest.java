package com.tistory.devyongsik.policy;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class PolicyTest {

	@Test
	public void keepOnlyLastCommitDeletionPolicyTest() throws IOException {
		String a = "learning perl learning java learning ruby";
		String b = "perl test t";
		String c = "perl test t learning";

		Directory dir = new RAMDirectory();

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44); //문서 내용을 분석 할 때 사용 될 Analyzer
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexDeletionPolicy myPolicy = new MyPolicy();
		iwc.setIndexDeletionPolicy(myPolicy);

		IndexWriter writer = new IndexWriter(dir, iwc); //8. 드디어 IndexWriter를 생성합니다.

		Document doc1 = new Document();
		FieldType f1type = new FieldType();
		f1type.setIndexed(true);
		f1type.setStored(false);
		f1type.setTokenized(true);
		f1type.setStoreTermVectors(true);
		f1type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

		Field f1 = new Field("f", a, f1type);
		doc1.add(f1);
		writer.addDocument(doc1);

		writer.commit();

		DirectoryReader directoryReader = DirectoryReader.open(dir);
		IndexCommit indexCommit = directoryReader.getIndexCommit();

		System.out.println(indexCommit.getGeneration());
		System.out.println(indexCommit.getSegmentCount());
		System.out.println(indexCommit.getSegmentsFileName());
		System.out.println(indexCommit.getFileNames());
		System.out.println(indexCommit.isDeleted());


		Document doc2 = new Document();
		Field f2 = new Field("f", b, f1type);
		doc2.add(f2);
		writer.addDocument(doc2);
		writer.commit();

		DirectoryReader directoryReader2 = DirectoryReader.open(dir);
		IndexCommit indexCommit2 = directoryReader2.getIndexCommit();

		System.out.println(indexCommit2.getGeneration());
		System.out.println(indexCommit2.getSegmentCount());
		System.out.println(indexCommit2.getSegmentsFileName());
		System.out.println(indexCommit2.getFileNames());
		System.out.println(indexCommit2.isDeleted());

		Document doc3 = new Document();
		Field f3 = new Field("f", c, f1type);
		doc3.add(f3);
		writer.addDocument(doc3);

		writer.commit();

		DirectoryReader directoryReader3 = DirectoryReader.open(dir);
		IndexCommit indexCommit3 = directoryReader3.getIndexCommit();

		System.out.println(indexCommit3.getGeneration());
		System.out.println(indexCommit3.getSegmentCount());
		System.out.println(indexCommit3.getSegmentsFileName());
		System.out.println(indexCommit3.getFileNames());
		System.out.println(indexCommit3.isDeleted());

		//delete
		Term t = new Term("f", "java");
		writer.deleteDocuments(t);
		writer.commit();

		DirectoryReader directoryReader4 = DirectoryReader.open(dir);
		IndexCommit indexCommit4 = directoryReader4.getIndexCommit();

		System.out.println(indexCommit4.getGeneration());
		System.out.println(indexCommit4.getSegmentCount());
		System.out.println(indexCommit4.getSegmentsFileName());
		System.out.println(indexCommit4.getFileNames());
		System.out.println(indexCommit4.isDeleted());
		
		writer.close();
	}

	private class MyPolicy extends IndexDeletionPolicy {

		/** Sole constructor. */
		public MyPolicy() {
		}

		/**
		 * Deletes all commits except the most recent one.
		 */
		@Override
		public void onInit(List<? extends IndexCommit> commits) {
			// Note that commits.size() should normally be 1:
			onCommit(commits);
		}

		/**
		 * Deletes all commits except the most recent one.
		 */
		@Override
		public void onCommit(List<? extends IndexCommit> commits) {
			// Note that commits.size() should normally be 2 (if not
			// called by onInit above)
		
			System.out.println("commits size : " + commits.size());
			System.out.println("commits : " + commits);
			
			int size = commits.size();
			for(int i=0;i<size-1;i++) {
				
				System.out.println("commits.get(i) : " + commits.get(i));
				System.out.println("seg count : " + commits.get(i).getSegmentCount());
				//commits.get(i).delete();	
			}
		}
	}
	
	@Test
	public void snapShotDeletionPolicyTest() throws IOException {
		String a = "learning perl learning java learning ruby";
		String b = "perl test t";
		String c = "perl test t learning";

		Directory dir = new RAMDirectory();

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44); //문서 내용을 분석 할 때 사용 될 Analyzer
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexDeletionPolicy primaryPolicy = new KeepOnlyLastCommitDeletionPolicy();
		SnapshotDeletionPolicy snapShotPolicy = new SnapshotDeletionPolicy(primaryPolicy);
		iwc.setIndexDeletionPolicy(snapShotPolicy);

		IndexWriter writer = new IndexWriter(dir, iwc); //8. 드디어 IndexWriter를 생성합니다.

		Document doc1 = new Document();
		FieldType f1type = new FieldType();
		f1type.setIndexed(true);
		f1type.setStored(false);
		f1type.setTokenized(true);
		f1type.setStoreTermVectors(true);
		f1type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

		Field f1 = new Field("f", a, f1type);
		doc1.add(f1);
		writer.addDocument(doc1);

		writer.commit();

		DirectoryReader directoryReader = DirectoryReader.open(dir);
		IndexCommit indexCommit = directoryReader.getIndexCommit();

		System.out.println(indexCommit.getGeneration());
		System.out.println(indexCommit.getSegmentCount());
		System.out.println(indexCommit.getSegmentsFileName());
		System.out.println(indexCommit.getFileNames());
		System.out.println(indexCommit.isDeleted());
		
		//snapShotPolicy.snapshot();

		Document doc2 = new Document();
		Field f2 = new Field("f", b, f1type);
		doc2.add(f2);
		writer.addDocument(doc2);
		writer.commit();

		DirectoryReader directoryReader2 = DirectoryReader.open(dir);
		IndexCommit indexCommit2 = directoryReader2.getIndexCommit();

		System.out.println(indexCommit2.getGeneration());
		System.out.println(indexCommit2.getSegmentCount());
		System.out.println(indexCommit2.getSegmentsFileName());
		System.out.println(indexCommit2.getFileNames());
		System.out.println(indexCommit2.isDeleted());

		//snapShotPolicy.snapshot();
		
		System.out.println("snapShot : " + snapShotPolicy.getSnapshots());
		
		System.out.println("segment1 : " + snapShotPolicy.getSnapshots());
		System.out.println("segment1 : " + snapShotPolicy.getSnapshots());
		
		System.out.println("segment2 : " + snapShotPolicy.getSnapshots());
		System.out.println("segment2 : " + snapShotPolicy.getSnapshots());
		
		writer.close();
	}
}