package no_de.inf5090.visualizingsensordata.userInterface;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import no_de.inf5090.visualizingsensordata.domain.AbstractDomainData;
import no_de.inf5090.visualizingsensordata.domain.AccelerationObserver;
import no_de.inf5090.visualizingsensordata.domain.LocationObserver;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import no_de.inf5090.visualizingsensordata.R;
import no_de.inf5090.visualizingsensordata.domain.RotationVectorObserver;
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
 * @author aage et al.
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SensorDataGraphFragment extends Fragment implements Observer {

    private View fragmentView;
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

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

    private Date graphStartDate = new Date();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);

        // connect to sensors
        VideoCapture.getSelf().connectSensors(this);
    }

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
        if(!(data instanceof AbstractDomainData))
            return;

        AbstractDomainData sensorData = (AbstractDomainData) data;

        // rotation?
        if (observable instanceof RotationVectorObserver) {
            RotationVectorObserver.DomainData logData = (RotationVectorObserver.DomainData) sensorData;
            mAzimuthSeries.add(sensorData.getTimestamp().getTime() - graphStartDate.getTime(), logData.getAzimuth() / Math.PI);
            mPitchSeries.add(sensorData.getTimestamp().getTime() - graphStartDate.getTime(), logData.getPitch() / Math.PI);
            mRollSeries.add(sensorData.getTimestamp().getTime() - graphStartDate.getTime(), logData.getRoll() / Math.PI);
        }

        // acceleration?
        else if (observable instanceof AccelerationObserver) {
            mShakeSeries.add(sensorData.getTimestamp().getTime() - graphStartDate.getTime(), ((AccelerationObserver.DomainData)sensorData).getAcceleration()/10);
        }

        // speed?
        else if (observable instanceof LocationObserver) {
            mSpeedSeries.add(sensorData.getTimestamp().getTime() - graphStartDate.getTime(), ((LocationObserver.DomainData)sensorData).getSpeed());
        }

        // Set new range - last 2 seconds
        mRenderer.setXAxisMax(sensorData.getTimestamp().getTime() - graphStartDate.getTime());
        mRenderer.setXAxisMin(sensorData.getTimestamp().getTime() - graphStartDate.getTime() - 2000);

        // Redraw
        mChart.repaint();
    }
}
