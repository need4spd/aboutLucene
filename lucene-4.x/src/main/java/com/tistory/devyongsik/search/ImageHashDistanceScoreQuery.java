package com.tistory.devyongsik.search;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

public class ImageHashDistanceScoreQuery extends CustomScoreQuery {

	
	public ImageHashDistanceScoreQuery(Query subQuery) {
		super(subQuery);
	}
	
	@Override
	protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) {
		return new DistanceCalculator(context);
	}

	private class DistanceCalculator extends CustomScoreProvider {

		public DistanceCalculator(AtomicReaderContext context) {
			super(context);
		}
		
		@Override
		public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
			String fieldValue = context.reader().document(doc).get("hash");
			
			System.out.println("fieldValue : " + fieldValue + ", subQueryScore : " + subQueryScore);
			
			return -subQueryScore;
		}
	}
}
