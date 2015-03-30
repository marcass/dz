package net.sf.jukebox.datastream.logger.impl.udp.xpl;

import java.util.Set;

import net.sf.jukebox.datastream.logger.impl.udp.UdpLogger;
import net.sf.jukebox.datastream.signal.model.DataSample;
import net.sf.jukebox.datastream.signal.model.DataSource;
import net.sf.jukebox.jmx.JmxDescriptor;

/**
 * The <a link href="http://www.xplproject.org.uk/">xPL</a> logger. Listens to
 * the notifications and broadcasts them using xPL.
 *
 * @param <E> Data type to log.
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2005-2009
 */
public class XplLogger<E extends Number> extends UdpLogger<E> {

    /**
     * Create an instance with no listeners.
     * 
     * @param port Port to bind to.
     */
    public XplLogger(int port) {
        this(null, port);
    }

    /**
     * Create an instance listening to given data sources.
     * 
     * @param producers Data sources to listen to.
     * @param port Port to bind to.
     */
    public XplLogger(Set<DataSource<E>> producers, int port) {
        super(producers, port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getDefaultPort() {

	return 3865;
    }

    /**
     * Write an xPL header.
     * 
     * @param sb String buffer to write the header to.
     */
    @Override
    protected final void writeHeader(StringBuffer sb) {

	sb.append("xpl-trig\n");
	sb.append("{\n");
	sb.append("hop=1\n");
	sb.append("source=DZ.logger.").append(getSource()).append("\n");
	sb.append("target=*\n");
	sb.append("}\n");
    }

    /**
     * Write xPL body.
     * 
     * @param sb String buffer to append to.
     * @param signature Signature to use.
     * @param value Data sample value.
     */
    @Override
    protected final void writeData(StringBuffer sb, String signature,
	    DataSample<E> value) {

	sb.append("sensor.basic\n");
	sb.append("{\n");

	sb.append("device=").append(signature).append("\n");

	String type = "unknown";

	if (signature.startsWith("T")) {

	    type = "temp";

	} else if (signature.startsWith("H")) {

	    type = "relative-humidity";

	} else if (signature.startsWith("P")) {

	    type = "pressure";
	}

	sb.append("type=").append(type).append("\n");
	sb.append("current=").append(getValueString(value)).append("\n");

	if (value.isError()) {

	    sb.append("error=").append(getErrorString(value)).append("\n");
	}

	sb.append("timestamp=").append(getTimestamp(value.timestamp)).append("\n");
	sb.append("}\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDescription() {

	return "xPL Logger";
    }

    @Override
    public JmxDescriptor getJmxDescriptor() {

	JmxDescriptor d = super.getJmxDescriptor();
	return new JmxDescriptor("jukebox", d.name, d.instance,
		"Broadcasts sensor readings via xPL protocol");
    }
}
