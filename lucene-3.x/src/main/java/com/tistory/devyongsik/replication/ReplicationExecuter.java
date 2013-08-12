package com.tistory.devyongsik.replication;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.store.FSDirectory;


/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 6. 30.
 *
 */
public class ReplicationExecuter {

	/*
	 * 
	SnapshotDeletionPolicy sdp = new SnapshotDeletionPolicy( new KeepOnlyLastCommitDeletionPolicy() );
	dir = FSDirectory.getDirectory( "index" );
	Backup backup = new Backup( dir, sdp );
	IndexWriter writer = new IndexWriter( dir, true, analyzer, true, sdp, MaxFieldLength.LIMITED );
	 * 
	 * 
	 */

	FSDirectory dir;
	SnapshotDeletionPolicy sdp;

	ReplicationExecuter( FSDirectory dir, SnapshotDeletionPolicy sdp ){
		this.dir = dir;
		this.sdp = sdp;
	}

	void backup( String postfix ) throws IOException{

		IndexCommit icp = sdp.snapshot(postfix);

		try{
			// 스냅샷의 세그먼트(segment) 파일을 취득
			Collection files = icp.getFileNames();
			System.out.println(files);
			// =================================
			// 여기서 백업을 작성
			// =================================
		}

		finally{
			sdp.release(postfix);
		}
	}
}