package net.sf.jukebox.datastream.logger.impl.trace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.NDC;

import net.sf.jukebox.conf.ConfigurableProperty;
import net.sf.jukebox.datastream.logger.impl.AbstractLogger;
import net.sf.jukebox.datastream.signal.model.DataSample;
import net.sf.jukebox.datastream.signal.model.DataSource;
import net.sf.jukebox.jmx.JmxAttribute;
import net.sf.jukebox.jmx.JmxDescriptor;

/**
 * Trace file logger. Writes data received into a trace file.
 * <p>
 * There will be many trace files, one per channel. The file format will is as
 * follows:
 * <ul>
 * <li> First line must start with a hash character ('#') followed by a space,
 * and then the channel signature.
 * <li> Second line must start with a hash character ('#') followed by a space,
 * and then the human readable channel name.
 * <li> Third line must be empty.
 * <li> Subsequent lines are formed as <code>${timestamp-millis}=${value}</code>.
 * </ul>
 * @param <E> Data type to log.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2005-2009
 */
public class TraceLogger<E extends Number> extends AbstractLogger<E> {

    /**
     * Base directory for trace files.
     */
    private File baseDir = null;

    /**
     * Channel signature to file mapping.
     */
    private final SortedMap<String, File> signature2file = new TreeMap<String, File>();

    /**
     * Create an instance with no listeners.
     * 
     * @param baseDir Base directory for trace files.
     */
    public TraceLogger(File baseDir) {
	
	this(null, baseDir);
    }

    /**
     * Create an instance listening to given data sources.
     * 
     * @param producers Data sources to listen to.
     * @param baseDir Base directory for trace files.
     */
    public TraceLogger(Set<DataSource<E>> producers, File baseDir) {
        super(producers);
        
        setTraceBase(baseDir);
    }

    @ConfigurableProperty(propertyName = "traceBase", description = "Base directory for trace files")
    public void setTraceBase(File target) {

	if (target == null) {
	    throw new IllegalArgumentException("target can't be null");
	}
	
	if (!target.isDirectory() || !target.canWrite()) {
	    throw new IllegalArgumentException(target
		    + ": not a directory or not writable");
	}

	baseDir = target;
	logger.info("Trace base: " + baseDir);
    }

    /**
     * @return List of files generated.
     */
    @JmxAttribute(description = "Files written to by this logger")
    public String[] getFiles() {

	String[] map = new String[signature2file.size()];

	int offset = 0;
	for (Iterator<File> i = signature2file.values().iterator(); i.hasNext();) {
	    map[offset++] = i.next().toString();
	}

	return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void createChannel(String name, String signature, long timestamp) {

	NDC.push("createChannel");
	
	try {

	    checkStatus();

	    File traceFile = new File(baseDir, "trace." + signature);

	    logger.info("Trace file for (" + name + "): " + traceFile);

	    signature2name.put(signature, name);
	    signature2file.put(signature, traceFile);

	    // If the file already exists, we won't try to create the header

	    if (!traceFile.exists()) {

		try {

		    PrintWriter pw = getWriter(signature);

		    pw.println("# " + signature);
		    pw.println("# " + name);
		    pw.println("");
		    pw.flush();
		    pw.close();

		} catch (IOException ex) {

		    // We've failed to create this channel, let's forget about
		    // it

		    signature2name.remove(signature);
		    signature2file.remove(signature);

		    logger.error("Failed to create the channel: '" + name
			    + "' (" + signature + "):", ex);
		}
	    }

	} finally {
	    NDC.pop();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final synchronized void consume(String signature, DataSample<E> value) {
	
	NDC.push("consume");
	
	try {

	    checkStatus();

	    try {

		PrintWriter pw = getWriter(signature);

		// We won't be able to store much error information in the
		// trace, because the error message may be multiline. Besides,
		// it will most probably screw up other scripts that may be
		// willing to read the trace. May reconsider it later, but so
		// far that's it.

		// Let's doublecheck: even though the sample may be present, its
		// signalValue may be NaN

		double signalValue = value.sample == null ? Double.NaN
			: value.sample.doubleValue();

		pw.println(signature
			+ ":"
			+ value.timestamp
			+ ":"
			+ (Double.isNaN(signalValue) ? "U" : Double
				.toString(signalValue)));
		pw.flush();
		pw.close();

	    } catch (IOException ex) {

		logger.warn("consume(" + signature + ", " + value + ") failed:", ex);
	    }

	} finally {
	    NDC.pop();
	}
    }

    /**
     * Get a writer for the given signature.
     * 
     * @param signature Signature to get the writer for.
     * @return A writer.
     * 
     * @throws IOException if a writer couldn't be obtained.
     */
    private PrintWriter getWriter(String signature) throws IOException {

	File f = signature2file.get(signature);

	if (f == null) {

	    throw new IllegalStateException("Supposed to have file name for "
		    + signature + ", but don't???");
	}

	// Create a FileWriter with 'append'
	return new PrintWriter(new FileWriter(f, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startup() throws Throwable {

	// Check if we're configured

	setTraceBase(baseDir);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdown() throws Throwable {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JmxDescriptor getJmxDescriptor() {

	JmxDescriptor d = super.getJmxDescriptor();
	return new JmxDescriptor("jukebox", d.name, d.instance, "Trace file logger");
    }
}
