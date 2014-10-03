package no_de.inf5090.visualizingsensordata.domain;

import java.util.Date;

public class SensorData {
	private Object sensor;
	private float[] values;
	private Date timestamp;
	
	public SensorData() { };
	public SensorData(Object sensor, float value) {
		setSensor(sensor);
		setValue(value);
		setTimestamp(new Date());
	}
	public SensorData(Object sensor, float[] values) {
		setSensor(sensor);
		setValues(values);
		setTimestamp(new Date());
	}
	public SensorData(Object sensor, float value, Date timestamp) {
		setSensor(sensor);
		setValue(value);
		setTimestamp(timestamp);
	}
	
	public Object getSensor() {
		return sensor;
	}
	public void setSensor(Object sensor) {
		this.sensor = sensor;
	}
	public float getValue() {
		return values[0];
	}
	public float[] getValues() {
		return values;
	}
	public void setValue(float value) {
		this.values = new float[]{value};
	}
	public void setValues(float[] values) {
		this.values = values;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
