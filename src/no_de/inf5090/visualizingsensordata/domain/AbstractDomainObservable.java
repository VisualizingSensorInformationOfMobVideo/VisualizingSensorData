package no_de.inf5090.visualizingsensordata.domain;

import java.util.Observable;

/**
 * Act as an "interface" for the sensor observers, so that we can generalize onResume and onPause
 *
 * The name should probably be refactored
 */
public abstract class AbstractDomainObservable extends Observable {
    /**
     * Free resources when pausing/not using sensor
     */
    abstract public void onPause();

    /**
     * Acquire resources when resuming/starting
     */
    abstract public void onResume();

    /**
     * Get name of this sensor
     */
    abstract public String getName();
}
