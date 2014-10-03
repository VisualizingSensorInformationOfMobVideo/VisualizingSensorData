package no_de.inf5090.visualizingsensordata.userInterface;

import java.util.ArrayList;
import java.util.List;

import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.application.Utils;
import no_de.inf5090.visualizingsensordata.domain.AccelerationSensorObserver;
import no_de.inf5090.visualizingsensordata.domain.RotationVectorObserver;
import no_de.inf5090.visualizingsensordata.domain.SensorData;
import no_de.inf5090.visualizingsensordata.domain.SpeedSensorObserver;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Color;
import android.view.Menu;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GraphDrawActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph_draw);
		drawGraphs();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.graph_draw, menu);
		return true;
	}
	
	private void drawGraphs() {
		
		// Check for null
		if(Utils.sensorDatas == null)
			return;
		
		// Draw graphs
		
		//List<SensorData> rotateDatas = new ArrayList<SensorData>();
		//List<SensorData> tiltDatas = new ArrayList<SensorData>();
		List<SensorData> shakeDatas = new ArrayList<SensorData>();
		List<SensorData> speedDatas = new ArrayList<SensorData>();
		
		// Sort all readings
		for (SensorData sensorData : Utils.sensorDatas) {
			/*if(sensorData.getSensor() instanceof RotationVectorObserver)
				rotateDatas.add(sensorData);
			else if(sensorData.getSensor() instanceof TiltSensorObserver)
				tiltDatas.add(sensorData);
			else */
			if(sensorData.getSensor() instanceof AccelerationSensorObserver)
				shakeDatas.add(sensorData);
			else if(sensorData.getSensor() instanceof SpeedSensorObserver)
				speedDatas.add(sensorData);
		}

        FragmentManager fragmentManager = getFragmentManager();
        SensorDataGraphFragment sensorGraphFragment = (SensorDataGraphFragment)fragmentManager.findFragmentById(R.id.rotateGraphFragment);
        //sensorGraphFragment.addSensorDataList(rotateDatas, getResources().getString(R.string.graph_rotation_text), Color.MAGENTA);
        //sensorGraphFragment.addSensorDataList(tiltDatas, getResources().getString(R.string.graph_tilt_text), Color.GREEN);
        sensorGraphFragment.addSensorDataList(shakeDatas, getResources().getString(R.string.graph_shake_text), Color.WHITE);
        sensorGraphFragment.addSensorDataList(speedDatas, getResources().getString(R.string.graph_speed_text), Color.RED);

	}
}
