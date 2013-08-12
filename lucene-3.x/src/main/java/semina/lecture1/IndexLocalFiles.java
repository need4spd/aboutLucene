package semina.lecture1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexLocalFiles {


	public static void main(String[] args) throws Exception {

		//String docsPath = "/Users/need4spd/Programming"; //1. 색인 대상 문서가 있는 경로 
		//String indexPath = "/Users/need4spd/Programming/lucene_index/"; //2. 색인 파일이 만들어질 경로

		String docsPath = "D:/programming/eulerproject/"; //1. 색인 대상 문서가 있는 경로 
		String indexPath = "d:/programming/lucene_index/"; //2. 색인 파일이 만들어질 경로
		
		final File docDir = new File(docsPath);
		if (!docDir.exists() || !docDir.canRead()) {
			System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();

		System.out.println("Indexing to directory '" + indexPath + "'...");

		Directory dir = FSDirectory.open(new File(indexPath));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35); //문서 내용을 분석 할 때 사용 될 Analyzer
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
		iwc.setOpenMode(OpenMode.CREATE); 

		IndexWriter writer = new IndexWriter(dir, iwc);
		indexDocs(writer, docDir);
		writer.close();

		Date end = new Date();
		System.out.println(end.getTime() - start.getTime() + " total milliseconds");
	}

	private static void indexDocs(IndexWriter writer, File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {

				FileInputStream fis = new FileInputStream(file);
				Document doc = new Document();
				Field pathField = new Field("path", file.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
				doc.add(pathField);

				NumericField modifiedField = new NumericField("modified");
				modifiedField.setLongValue(file.lastModified());
				doc.add(modifiedField);
				doc.add(new Field("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));

				writer.addDocument(doc);

				fis.close();
			}
		}
	}
}