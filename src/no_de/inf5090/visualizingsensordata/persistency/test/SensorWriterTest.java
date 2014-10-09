package no_de.inf5090.visualizingsensordata.persistency.test;

import java.io.File;


import no_de.inf5090.visualizingsensordata.persistency.LocalStorageWriter;
import junit.framework.TestCase;

public class SensorWriterTest extends TestCase {

    static String FPATH = "test-sensor.xml";//"VideoCapture.appDir.getPath()+"/"+"test"+"-sensor.xml";
    LocalStorageWriter writer;

    protected void setUp() throws Exception {
        super.setUp();
        // TODO writer = new LocalStorageWriter();
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        File file = new File(FPATH);

        if(file.exists()) file.delete();
    }

    public void testUpdate() {
        /*SensorData sensorData = new SensorData();
        sensorData.setValue(1);
        sensorData.setTimestamp(new Date());

        try {
            writer.update(null, sensorData);
        } catch (Exception e) {
            // TODO: handle exception
            fail("Unable to register new sensor entry");
        }*/
    }

    public void testWriteXML() {
        /*SensorData sensorData = new SensorData();
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
        file.delete();*/
    }

}
