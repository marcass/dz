package net.sf.dz3.device.model.impl;

import net.sf.dz3.device.model.Thermostat;
import net.sf.dz3.device.model.ThermostatSignal;
import net.sf.jukebox.datastream.logger.impl.DataBroadcaster;
import net.sf.jukebox.datastream.logger.model.DataLogger;
import net.sf.jukebox.datastream.signal.model.DataSample;
import net.sf.jukebox.datastream.signal.model.DataSink;
import net.sf.jukebox.datastream.signal.model.DataSource;
import net.sf.jukebox.util.MessageDigestFactory;

import org.apache.log4j.NDC;

/**
 * Receives a complex {@link ThermostatSignal} signal and converts it into several simpler
 * {@link DataSample} signals suitable for consumption by {@link DataLogger}.
 * 
 * Add this object as a listener to the thermostat, and add the data logger as a listener to this object,
 * to record the data stream.
 * 
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2009-2010
 */
public class ThermostatSignalSplitter implements DataSink<ThermostatSignal>, DataSource<Double> {

    private final DataBroadcaster<Double> dataBroadcaster = new DataBroadcaster<Double>();
   
    /**
     * Create an instance not attached to anything.
     */
    public ThermostatSignalSplitter() {
        
    }
    
    /**
     * Create an instance attached to a thermostat.
     * 
     * @param ts Thermostat to listen to.
     */
    public ThermostatSignalSplitter(Thermostat ts) {
        ts.addConsumer(this);
    }
    
    public synchronized void consume(DataSample<ThermostatSignal> signal) {
        
        NDC.push("consume");
        
        try {
         
            {
                // Whether this thermostat is enabled
                String sourceName = signal.sourceName + ".enabled";
                String signature = new MessageDigestFactory().getMD5(sourceName).substring(0, 19);
                DataSample<Double> calling = new DataSample<Double>(signal.timestamp, sourceName, signature, signal.sample.enabled ? 1.0 : 0.0, null);
                dataBroadcaster.broadcast(calling);
            }
            
            {
                // Whether this thermostat is on hold
                String sourceName = signal.sourceName + ".hold";
                String signature = new MessageDigestFactory().getMD5(sourceName).substring(0, 19);
                DataSample<Double> calling = new DataSample<Double>(signal.timestamp, sourceName, signature, signal.sample.onHold ? 1.0 : 0.0, null);
                dataBroadcaster.broadcast(calling);
            }
            
            {
                // Whether this thermostat is calling
                String sourceName = signal.sourceName + ".calling";
                String signature = new MessageDigestFactory().getMD5(sourceName).substring(0, 19);
                DataSample<Double> calling = new DataSample<Double>(signal.timestamp, sourceName, signature, signal.sample.calling ? 1.0 : 0.0, null);
                dataBroadcaster.broadcast(calling);
            }
            
            {
                // Whether this thermostat is voting
                String sourceName = signal.sourceName + ".voting";
                String signature = new MessageDigestFactory().getMD5(sourceName).substring(0, 19);
                DataSample<Double> calling = new DataSample<Double>(signal.timestamp, sourceName, signature, signal.sample.voting ? 1.0 : 0.0, null);
                dataBroadcaster.broadcast(calling);
            }
            
            // The demand sent to the zone controller
            dataBroadcaster.broadcast(signal.sample.demand);
            
        } finally {
            NDC.pop();
        }
    }

    public void addConsumer(DataSink<Double> consumer) {
        
        dataBroadcaster.addConsumer(consumer);
    }

    public void removeConsumer(DataSink<Double> consumer) {
        
        dataBroadcaster.removeConsumer(consumer);
    }

}
