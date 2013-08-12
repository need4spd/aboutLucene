package com.tistory.devyongsik.replication;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 7. 1.
 *
 */
public class LastCommitDeletePolicy implements IndexDeletionPolicy {

	@Override
	public void onCommit(List<? extends IndexCommit> arg0) throws IOException {
		
		System.out.println("on commit!!!");
		
		int size = arg0.size();

	    for(int i=0;i<size-1;i++) {
	    	System.out.println("DDD : " + arg0.get(i));
	    }
	}

	@Override
	public void onInit(List<? extends IndexCommit> arg0) throws IOException {
		System.out.println("init.... in policy");
	}
}

