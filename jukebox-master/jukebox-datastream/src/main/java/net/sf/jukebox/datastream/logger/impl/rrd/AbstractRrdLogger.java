package net.sf.jukebox.datastream.logger.impl.rrd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.jukebox.conf.ConfigurableProperty;
import net.sf.jukebox.datastream.logger.impl.AbstractLogger;
import net.sf.jukebox.datastream.signal.model.DataSource;
import net.sf.jukebox.jmx.JmxAttribute;

import org.apache.log4j.NDC;

/**
 * An abstract class supporting RRD logging implementations.
 * 
 * @param <E> Data type to log.
 * @see JRobinLogger
 * @see RrdLogger
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2005-2009
 */
public abstract class AbstractRrdLogger<E extends Number, RRD> extends AbstractLogger<E> {

    /**
     * Index file name.
     */
    public static final String INDEX_FILE_NAME = "signature.properties";

    /**
     * Signature to RRD instance mapping.
     */
    private final Map<String, RRD> signature2rrd = new HashMap<String, RRD>();

    /**
     * Base directory for RRD database files.
     */
    private File rrdBase;
    
    /**
     * Create an instance with no listeners.
     * 
     * @param rrdBase Base directory for RRD database files.
     */
    public AbstractRrdLogger(File rrdBase) {
	
	this(null, rrdBase);
    }

    /**
     * Create an instance listening to given data sources.
     * 
     * @param producers Data sources to listen to.
     * @param rrdBase Base directory for RRD database files.
     */
    public AbstractRrdLogger(Set<DataSource<E>> producers, File rrdBase) {
        super(producers);
        
        setRrdBase(rrdBase);
    }

    @ConfigurableProperty(
	    propertyName = "rrdBase",
	    description = "Root directory for RRD files"
	    )
    public void setRrdBase(File target) {
	
	if (target == null) {
	    throw new IllegalArgumentException("target can't be null");
	}
	
	if (!target.isDirectory() || !target.canWrite()) {
	    throw new IllegalArgumentException(target
		    + ": not a directory or not writable");
	}

	rrdBase = target;
	logger.info("RRD base: " + rrdBase);
    }

    /**
     * Get the RRD directory.
     * 
     * @return Directory to put RRD files into.
     */
    @JmxAttribute(description = "Directory RRD files are written into")
    public final File getRrdBase() {

	if (rrdBase == null) {
	    // Need this for JMX in case we're not initialized yet. Let the
	    // other methods choke on this if they feel like
	    return null;
	}
	// Let's be paranoid and create a clone
	return new File(rrdBase.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void startup() throws Throwable {

	// Make sure we're configured
	setRrdBase(rrdBase);
	
	// Let the concrete subclass a chance to check integrity
	startup2();
    }
    
    abstract protected void startup2();

    /**
     * Consume the RRD object.
     * 
     * @param signature Channel signature.
     * @param name Channel name.
     * @param rrd Channel RRD object.
     */
    protected final synchronized void consume(String signature, String name, RRD rrd) {
	
	NDC.push("consume");

	try {
	    
	    signature2rrd.put(signature, rrd);
	    signature2name.put(signature, name);

	    logger.debug(signature + " (" + name + "): " + rrd);

	    // Let's store signature2name into an externally readable file, so
	    // whoever wants to put together a presentation can use human
	    // readable names instead of signatures

	    {
		// VT: This is rather redundant (properties aren't cached, so
		// they're read every time a new channel arrives), but since it
		// is not a bottleneck (new signal channels arrive mostly at
		// startup time), the hell with it. Who wants to optimize this,
		// be my guest. Keep in mind, though, that you'll be paying
		// memory for performance. Besides, *this* way allows to
		// manually add/edit/override channel names without stopping DZ.

		Properties p = readIndex();

		// Since we're updating the index file each time we add
		// something we
		// haven't known about, all we have to do is check whether the
		// value
		// that has just arrived is present here. If it is, we don't
		// have to
		// do anything.

		// VT: NOTE: Note that if the signature is already present, it
		// will *not* be updated.

		if (!p.containsKey(signature)) {

		    // But if it is not, we'll persist it
		    p.put(signature, name);

		    storeIndex(p);
		}
	    }

	} finally {
	    NDC.pop();
	}
    }

    /**
     * Read the property file containing a signature to name mapping.
     * 
     * @return Signature to name mapping properties object.
     */
    private Properties readIndex() {
	
	NDC.push("readIndex");
	
	try {

	    File indexFile = new File(getRrdBase(), INDEX_FILE_NAME);

	    if (!indexFile.exists()) {
		logger.warn("No index exists at " + indexFile);
		return new Properties();
	    }

	    try {

		Properties p = new Properties();

		p.load(new FileInputStream(indexFile));
		logger.debug("Loaded: " + indexFile);

		return p;

	    } catch (IOException ioex) {

		logger.warn("Unable to read index, ignored: " + indexFile, ioex);

		return new Properties();
	    }

	} finally {
	    NDC.pop();
	}
    }

    /**
     * Store the properties containing {@link #signature2name signature to name
     * mapping}.
     * 
     * @param p Properties to store.
     */
    private void storeIndex(Properties p) {
	
	NDC.push("storeIndex");
	
	try {

	    File indexFile = new File(getRrdBase(), INDEX_FILE_NAME);

	    // We won't make any backups, since just about the only purpose for
	    // this file is to integrate the RRDs with external utilities

	    try {

		OutputStream out = new FileOutputStream(indexFile);

		p.store(out,
			" This file contains signal source signature to human readable description mapping.\n"
			+ "#\n"
			+ "# You can modify the content, just keep in mind that your changes override autogenerated entries.\n"
			+ "# If you don't like your change, but don't remember the autogenerated content, just remove the line.\n"
			+ "#");

		// The stream is flushed, but still open

		out.close();

		logger.debug("Stored: " + indexFile);

	    } catch (IOException ioex) {

		logger.warn("Failed to store index, ignored: " + indexFile,
			ioex);
	    }

	} finally {
	    NDC.pop();
	}
    }

    /**
     * Get the RRD object corresponding to the channel signature.
     * 
     * @param signature Signature to get the object for.
     * @return RRD object corresponding to the given channel signature.
     */
    protected final Object getRrd(String signature) {
	return signature2rrd.get(signature);
    }

    /**
     * Get the iterator on RRD objects.
     * 
     * @return Iterator on RRD objects.
     */
    protected final Iterator<RRD> iterator() {
	// VT: NOTE: Worse is better: this iterator should really be
	// immutable, but no big deal
	return signature2rrd.values().iterator();
    }

    /**
     * Get a human readable description of the class functionality.
     * 
     * @return Description string.
     */
    @JmxAttribute(description = "Human readable description")
    public abstract String getDescription();

    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("RrdLogger(base=").append(getRrdBase()).append(")");
        
        return sb.toString();
        
    }
}
