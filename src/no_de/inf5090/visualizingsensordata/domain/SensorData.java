package no_de.inf5090.visualizingsensordata.domain;

import java.util.Date;

public class SensorData {
	private Object sensor;
	private float value;
	private Date timestamp;
	
	public SensorData() { };
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
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
