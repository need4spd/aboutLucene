package semina.lecture1;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SearchLocalFiles {
	
	public static void main(String[] args) throws Exception {

		//String index = "/Users/need4spd/Programming/lucene_index/";
		
		String index = "d:/programming/lucene_index/"; //2. 색인 파일이 만들어질 경로
		
		String field = "contents";
		String queryString = "java";
		int hitsPerPage = 10;

		IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(index)));
		IndexSearcher searcher = new IndexSearcher(indexReader);

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);

		QueryParser parser = new QueryParser(Version.LUCENE_35, field, analyzer);
		Query query = parser.parse(queryString);

		System.out.println("Query String : " + queryString);
		System.out.println("Query : " + query.toString());
		System.out.println("Searching for: " + query.toString(field));

		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		for (int i = 0; i < hits.length; i++) {
			System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
			Document doc = searcher.doc(hits[i].doc);
			String path = doc.get("path");
			if (path != null) {
				System.out.println((i+1) + ". " + path);
				String title = doc.get("title");
				if (title != null) {
					System.out.println("   Title: " + doc.get("title"));
				}
			} else {
				System.out.println((i+1) + ". " + "No path for this document");
			}
		}

		searcher.close();
	}
}