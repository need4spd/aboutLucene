package com.tistory.devyongsik.search;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.FloatDocValues;


/**
 *
 *
 **/
public class ImageHashDistanceFunction extends ValueSource {
	protected ValueSource str1;
	protected String hashCode;

	public ImageHashDistanceFunction(ValueSource str1, String hashCode) {
		this.str1 = str1;
		this.hashCode = hashCode;
	}

	@Override
	public FunctionValues getValues(Map context, AtomicReaderContext readerContext) throws IOException {
		final FunctionValues str1DV = str1.getValues(context, readerContext);
		
		return new FloatDocValues(this) {

			@Override
			public float floatVal(int doc) {
				float distance = distance(str1DV.strVal(doc), hashCode);
				
				System.out.println("hashCode : " + hashCode + ", str1 : " + str1DV.strVal(doc) + ", distance  : " + distance);
				
				return distance;
			}

			@Override
			public String toString(int doc) {
				StringBuilder sb = new StringBuilder();
				sb.append("strdist").append('(');
				sb.append(str1DV.toString(doc)).append(',').append(hashCode);
				sb.append(')');
				return sb.toString();
			}
		};
	}

	@Override
	public String description() {
		StringBuilder sb = new StringBuilder();
		sb.append("strdist").append('(');
		sb.append(str1).append(',').append(hashCode);
		sb.append(')');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ImageHashDistanceFunction)) return false;

		ImageHashDistanceFunction that = (ImageHashDistanceFunction) o;

		if (!str1.equals(that.str1)) return false;
		if (!hashCode.equals(that.hashCode)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = str1.hashCode();
		result = 31 * result + hashCode.hashCode();
		return result;
	}

	private float distance(String s1, String s2) {
		int counter = 0;
		for (int k = 0; k < s1.length();k++) {
			if(s1.charAt(k) != s2.charAt(k)) {
				counter++;
			}
		}
		return counter;
	}
}