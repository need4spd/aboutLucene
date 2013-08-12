package com.tistory.devyongsik.demo;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/** Simple command-line based search demo. */
public class SearchFiles {

  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {

    String index = "/Users/need4spd/Java/lucene_index/"; //1. 인덱스 파일이 있는 경로
    String field = "contents"; //2. 키워드로 검색 할 필
    String queryString = "java"; //3. 루씬에서 사용되는 검색쿼리
    int hitsPerPage = 10; //4. 한 페이지에 보여 줄 검색 결과 수
    
    //5. 인덱스 파일로부터 검색을 하기 위한 IndexSearcher를 생성합니다.
    IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(index)));
    
    System.out.println("####### : " + indexReader.docFreq(new Term("contents", "java")));
    
    IndexSearcher searcher = new IndexSearcher(indexReader);
    
    //6. 검색 키워드를 분석 할 Analyzer를 생성합니다.
    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);

    //7. 쿼리식으로부터 실제 루씬에서 사용 될 Query 객체를 만들어 반환하는 QueryParser를 생성합니다.
    QueryParser parser = new QueryParser(Version.LUCENE_36, field, analyzer);
    Query query = parser.parse(queryString);
    
    System.out.println("Query String : " + queryString);
    System.out.println("Query : " + query.toString());
    System.out.println("Searching for: " + query.toString(field));

    //8. 검색을 합니다. 이때의 랭킹은 루씬에서 제공되는 기본적은 점수입니다. 나중에 말씀드리겠지만 TF-IDF를 사용하는 것으로 알고 있습니다.
    TopDocs results = searcher.search(query, 5 * hitsPerPage);
    ScoreDoc[] hits = results.scoreDocs;

    int numTotalHits = results.totalHits;
    System.out.println(numTotalHits + " total matching documents");
    
    for (int i = 0; i < numTotalHits; i++) {
      System.out.println("doc="+hits[i].doc+" score="+hits[i].score); //9. 이렇게 내용을 확인 할 수도...
      
      Document doc = searcher.doc(hits[i].doc); //10. 이렇게 할 수도 있습니다.
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