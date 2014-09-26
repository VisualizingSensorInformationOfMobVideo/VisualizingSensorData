package no_de.inf5090.hqvideocapture.persistency.test;

import java.io.File;
import java.util.Date;


import no_de.inf5090.hqvideocapture.domain.SensorData;
import no_de.inf5090.hqvideocapture.persistency.SensorWriter;
import junit.framework.TestCase;

public class SensorWriterTest extends TestCase {

	static String FPATH = "test-sensor.xml";//"VideoCapture.appDir.getPath()+"/"+"test"+"-sensor.xml";
	SensorWriter writer;

	protected void setUp() throws Exception {
		super.setUp();
		writer = new SensorWriter();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		File file = new File(FPATH);
		
		if(file.exists()) file.delete();
	}

	public void testUpdate() {
		SensorData sensorData = new SensorData();
		sensorData.setValue(1);
		sensorData.setTimestamp(new Date());
		
		try {
			writer.update(null, sensorData);
		} catch (Exception e) {
			// TODO: handle exception
			fail("Unable to register new sensor entry");
		}
	}

	public void testWriteXML() {
		SensorData sensorData = new SensorData();
		sensorData.setValue(1);
		sensorData.setTimestamp(new Date());
		
		try {
			writer.update(null, sensorData);
		} catch (Exception e) {
			// TODO: handle exception
			fail("Unable to register new sensor entry");
		}
		writer.writeXML(FPATH);
		File file = new File(FPATH);
		assertTrue(file.exists());
		
		// Cleanup
		file.delete();
	}

}
