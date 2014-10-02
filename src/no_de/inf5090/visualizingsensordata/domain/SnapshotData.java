package no_de.inf5090.visualizingsensordata.domain;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class SnapshotData {
	private Bitmap mBitmap;
	private String name;
	private Date timestamp;
	
	public SnapshotData(String name, Bitmap mBitmap, Date timestamp) {
		setBitmap(mBitmap);
		setName(name);
		setTimestamp(timestamp);
	}
	
	public SnapshotData() {	};
	
	public Bitmap getBitmap() {
		return this.mBitmap;
	}
	public void setBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	

}
