package no_de.inf5090.visualizingsensordata.userInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import no_de.inf5090.visualizingsensordata.domain.AbstractLogicalSensorData;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.application.Utils;
import no_de.inf5090.visualizingsensordata.domain.AccelerationSensorObserver;
import no_de.inf5090.visualizingsensordata.domain.RotationVectorObserver;
import no_de.inf5090.visualizingsensordata.domain.SpeedSensorObserver;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Displays sensor data
 * Note: achartengine is licensed under apache license v2.0. Need to investigate what requirements exist here.
 * @author aage
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SensorDataGraphFragment extends Fragment implements Observer {

	private View fragmentView;
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    //private List<List<SensorData>> sensorDataLists = new ArrayList<List<SensorData>>();
    
	private XYSeries mAzimuthSeries = new XYSeries("Azimuth");
	private XYSeriesRenderer mAzimuthRenderer = new XYSeriesRenderer();
	private XYSeries mPitchSeries = new XYSeries("Pitch");
	private XYSeriesRenderer mPitchRenderer = new XYSeriesRenderer();
	private XYSeries mRollSeries = new XYSeries("Roll");
	private XYSeriesRenderer mRollRenderer = new XYSeriesRenderer();
	
	private XYSeries mShakeSeries = new XYSeries("Shake");
	private XYSeriesRenderer mShakeRenderer = new XYSeriesRenderer();
	private XYSeries mSpeedSeries = new XYSeries("Speed");
	private XYSeriesRenderer mSpeedRenderer = new XYSeriesRenderer();

		
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // connect to sensors
        VideoCapture.getSelf().connectSensors(this);
    	
    	initGraph();

    	// Double axis title size
    	mRenderer.setYAxisMax(0.5);
    	mRenderer.setYAxisMin(-0.5);

    	// Set default legend size in pixels (a bit smaller than button text)
    	float textSize = new Button(inflater.getContext()).getTextSize()*2/3;
    	//mRenderer.setLegendTextSize(mRenderer.getLegendTextSize() + 15);
    	mRenderer.setLegendTextSize(textSize);
    	    	
    	// Minimize margins
    	mRenderer.setMargins(new int[] {0,0,0,0});
    	
    	// Get fragment
    	fragmentView = inflater.inflate(R.layout.graph_sensor_data_fragment, container, false);
    	return fragmentView;
    }
    
    /**
     * Plots data to SensorDataGraphFragment
     * @param sensorDataList: List of sensor data to plot
     */
    /*public void setSensorDataList(List<SensorData> sensorDataList) {
    	sensorDataLists.add(sensorDataList);
    }*/
    
    private void initGraph() {
    	
    	// Set graph color
    	mAzimuthRenderer.setColor(Color.MAGENTA);
    	mPitchRenderer.setColor(Color.GREEN);
    	mRollRenderer.setColor(Color.BLUE);
    	mShakeRenderer.setColor(Color.WHITE);
    	mSpeedRenderer.setColor(Color.RED);
    	
    	// Add graphs
    	mDataset.addSeries(mAzimuthSeries);
    	mDataset.addSeries(mPitchSeries);
    	mDataset.addSeries(mRollSeries);
    	mDataset.addSeries(mShakeSeries);
    	mDataset.addSeries(mSpeedSeries);
    	mRenderer.addSeriesRenderer(mAzimuthRenderer);
    	mRenderer.addSeriesRenderer(mPitchRenderer);
    	mRenderer.addSeriesRenderer(mRollRenderer);
    	mRenderer.addSeriesRenderer(mShakeRenderer);
    	mRenderer.addSeriesRenderer(mSpeedRenderer);
    }

    /**
     * Adds a line plot to the graph
     * @param sensorDataList	Data to generate line plot from
     * @param linePlotName		Name of line plot
     * @param color				Color of line plot
     */
    /*public void addSensorDataList(List<SensorData> sensorDataList, String linePlotName, int color) {
    	    	
    	// Collect all readings
    	sensorDataLists.add(sensorDataList);
    	
    	// Prepare graph
    	XYSeries mCurrentSeries = new XYSeries(linePlotName);
    	XYSeriesRenderer mCurrentRenderer = new XYSeriesRenderer();
    	
    	mCurrentRenderer.setColor(color);
    	// Add graph
        mDataset.addSeries(mCurrentSeries);
        mRenderer.addSeriesRenderer(mCurrentRenderer);

        // Populate graph
    	for (SensorData sensorData : sensorDataList) {
    		mCurrentSeries.add(sensorData.getTimestamp().getTime() - Utils.lastRecordingStar.getTime(), sensorData.getValue());
    	}
    	
    	// Double axis title size
    	mRenderer.setYAxisMax(1);
    	mRenderer.setYAxisMin(-1);

    }*/
    
    public void onResume() {
        super.onResume();
        LinearLayout layout = (LinearLayout) fragmentView.findViewById(R.id.chart);
        if (mChart == null) {
            mChart = ChartFactory.getCubeLineChartView(fragmentView.getContext(), mDataset, mRenderer, 0.3f);
            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
    }

	public void update(Observable observable, Object data) {
		if(!(data instanceof AbstractLogicalSensorData))
			return;
		
		AbstractLogicalSensorData sensorData = (AbstractLogicalSensorData) data;
		
		// rotation?
        if (sensorData.getSensorID() == RotationVectorObserver.ID) {
            RotationVectorObserver.LogicalSensorData logData = (RotationVectorObserver.LogicalSensorData) sensorData;
            mAzimuthSeries.add(sensorData.getTimestamp().getTime() - Utils.lastRecordingStar.getTime(), logData.getAzimuth() / Math.PI);
            mPitchSeries.add(sensorData.getTimestamp().getTime() - Utils.lastRecordingStar.getTime(), logData.getPitch() / Math.PI);
            mRollSeries.add(sensorData.getTimestamp().getTime() - Utils.lastRecordingStar.getTime(), logData.getRoll() / Math.PI);
        }

        // acceleration?
        else if (sensorData.getSensorID() == AccelerationSensorObserver.ID) {
			mShakeSeries.add(sensorData.getTimestamp().getTime() - Utils.lastRecordingStar.getTime(), ((AccelerationSensorObserver.LogicalSensorData)sensorData).getAcceleration()/10);
		}

        // speed?
        else if (sensorData.getSensorID() == SpeedSensorObserver.ID) {
            mSpeedSeries.add(sensorData.getTimestamp().getTime() - Utils.lastRecordingStar.getTime(), ((SpeedSensorObserver.LogicalSensorData)sensorData).getSpeed());
		}
		
		// Set new range - last 2 seconds
		mRenderer.setXAxisMax(sensorData.getTimestamp().getTime() - Utils.lastRecordingStar.getTime());
		mRenderer.setXAxisMin(sensorData.getTimestamp().getTime() - Utils.lastRecordingStar.getTime() - 2000);
		
		// Redraw
		mChart.repaint();
	}
}
