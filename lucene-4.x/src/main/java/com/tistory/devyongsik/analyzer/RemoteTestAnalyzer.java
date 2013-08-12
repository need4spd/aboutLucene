package com.tistory.devyongsik.analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 12. 21.
 *
 */
public class RemoteTestAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(final String fieldName,
			Reader reader) {

		//String result = "무궁화 꽃";
		//reader = new StringReader(result);

		String sourceStr = new String();
		char[] arr = new char[8*1024]; // 8K at a time
		StringBuffer buf = new StringBuffer();
		int numChars;
		try {
			while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
				buf.append(arr, 0, numChars);
			}
			sourceStr = buf.toString();

			System.out.println("sourceStr : " + sourceStr);
		}
		catch(Exception e){
			e.printStackTrace();
		} finally {
			//
		}


		Socket socketK = null;
		BufferedReader readerK = null;
		BufferedWriter writerK = null;

		try {
			String IP = ""; // Server IP
			int port=Integer.parseInt("0"); // Port Number
			socketK = new Socket(IP, port); // 지정된 IP와 port를 이용하여 Socket Object 생성
			readerK = new BufferedReader(new InputStreamReader(socketK.getInputStream()));
			writerK = new BufferedWriter(new OutputStreamWriter(socketK.getOutputStream()));
			writerK.write(sourceStr);
			writerK.newLine();
			writerK.flush();

			String result;
			result = readerK.readLine();

			System.out.println("result : " + result);
			reader = new StringReader(result);
			
			final Tokenizer source = new StandardTokenizer(Version.LUCENE_40, reader);

			CharArraySet charArraySet = StopFilter.makeStopSet(Version.LUCENE_40, new ArrayList<String>());

			return new TokenStreamComponents(source, new StopFilter(Version.LUCENE_40, source, charArraySet));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
		
		return null;
	}
}