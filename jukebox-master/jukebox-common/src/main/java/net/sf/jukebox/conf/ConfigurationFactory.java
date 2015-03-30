package net.sf.jukebox.conf;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * This object provides the means necessary to instantiate the configuration
 * object given the URL to read it from.
 * <p>
 * Strictly speaking, this object is a singleton, but given all the troubles
 * with the singletons in the application server environment, I'd rather leave
 * it an instance entity - little overhead, great flexibility.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2000-2008
 */
public class ConfigurationFactory {

    /**
     * Create the instance.
     */
    public ConfigurationFactory() {

    }

    /**
     * Instantiate the configuration object from the given URL.
     *
     * @param targetURL The URL to read the configuration from.
     * @return The configuration object.
     * @exception IOException if there's a problem with getting the data.
     */
    public Configuration getConfiguration(URL targetURL) throws IOException {

        URLConnection conn = targetURL.openConnection();

        if (conn == null) {

            throw new java.io.FileNotFoundException(targetURL.toString() + ": null connection");
        }

        String contentType = conn.getContentType();

        // System.err.println("Content-Type: " + contentType);

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        return (contentType.toLowerCase().indexOf("/xml") != -1) ? getXmlConfiguration(br, targetURL)
                : getTextConfiguration(br, targetURL);
    }

    /**
     * Create the configuration chain from the given set of URLs. Each
     * configuration in the set is a default configuration for the next, and the
     * last configuration is the one returned.
     * <p>
     * If the configuration can't be created from the set entry, it is ignored
     * unless all of the entries resulted in a failure.
     *
     * @param urlSet Set of URLs to create the configuration chain from.
     * @return The configuration created from the last entry in the set, with
     * the rest being its defaults.
     * @exception IllegalArgumentException if the set is empty, or resulted in
     * the empty set of configurations (i.e. all of the entries failed to
     * produce the configuration object).
     */
    public Configuration createChain(URL urlSet[]) {

        Configuration current = null;
        Configuration trailer = null;
        List<Throwable> cause = null;

        for (int idx = 0; idx < urlSet.length; idx++) {

            try {

                current = getConfiguration(urlSet[idx]);

                if (current != null) {

                    if (trailer != null) {

                        current.setDefaultConfiguration(trailer);
                    }

                    trailer = current;
                }

            } catch (Throwable t) {

                // Oh well, we can't do anything here except collect the
                // problems.

                if (cause == null) {

                    cause = new LinkedList<Throwable>();
                }

                cause.add(t);
            }
        }

        if (current == null) {

            throw new ConfigurationFactoryException("Empty configuration set produced by URL list: " + urlSet, cause);
        }

        return current;
    }

    /**
     * Instantiate the configuration object from the given reader.
     *
     * @param reader The reader to read the configuration from.
     * @return The configuration object.
     * @exception IOException if nested method throws it.
     */
    public Configuration getConfiguration(Reader reader) throws IOException {

        return getConfiguration(reader, null);
    }

    /**
     * Instantiate the configuration object from the given reader, based on a
     * given URL.
     *
     * @param reader The reader to read the configuration from.
     * @param targetURL URL to base on. This value is declarative, it is never
     * used by the configuration itself, however, it is used by the {@link
     * ConfigurationWatcher ConfigurationWatcher} and {@link
     * ConfigurationChangeListener ConfigurationChangeListener}.
     * @return The configuration object.
     * @exception IOException if nested method throws it.
     */
    public Configuration getConfiguration(Reader reader, URL targetURL) throws IOException {

        // Read the chunk of the configuration and decide if it is XML or
        // text.

        boolean isXML = false;
        PushbackReader pr = new PushbackReader(reader);

        // So far, let's make this simple: if the first character of the
        // stream is '<', then it is XML. If it doesn't work, let's think
        // about it later.

        int c = pr.read();

        if ((char) c == '<') {

            isXML = true;
        }

        pr.unread(c);

        return isXML ? getXmlConfiguration(pr, targetURL) : getTextConfiguration(pr, targetURL);
    }

    /**
     * Instantiate the text configuration from the given reader.
     *
     * @param reader The reader to read the text configuration from.
     * @return The configuration object.
     * @exception IOException if nested method throws it.
     */
    protected TextConfiguration getTextConfiguration(Reader reader) throws IOException {

        return getTextConfiguration(reader, null);
    }

    /**
     * Instantiate the text configuration from the given reader, based on a
     * given URL.
     *
     * @param reader The reader to read the text configuration from.
     * @param targetURL URL to base on. This value is declarative, it is never
     * used by the configuration itself, however, it is used by the {@link
     * ConfigurationWatcher ConfigurationWatcher} and {@link
     * ConfigurationChangeListener ConfigurationChangeListener}.
     * @return The configuration object.
     * @exception IOException if nested method throws it.
     */
    protected TextConfiguration getTextConfiguration(Reader reader, URL targetURL) throws IOException {

        PropertiesReader r = new PropertiesReader(reader);
        TextConfiguration conf = new TextConfiguration(null, targetURL);

        r.load(conf);

        return conf;
    }

    /**
     * Instantiate the XML configuration from the given reader.
     *
     * @param reader The reader to read the text configuration from.
     * @return The configuration object.
     * @exception IOException if nested method throws it.
     */
    protected XmlConfiguration getXmlConfiguration(Reader reader) throws IOException {

        return getXmlConfiguration(reader, null);
    }

    /**
     * Instantiate the XML configuration from the given reader, based on a given
     * URL.
     *
     * @param reader The reader to read the text configuration from.
     * @param targetURL URL to base on. This value is declarative, it is never
     * used by the configuration itself, however, it is used by the {@link
     * ConfigurationWatcher ConfigurationWatcher} and {@link
     * ConfigurationChangeListener ConfigurationChangeListener}.
     * @return The configuration object.
     * @exception IOException if nested method throws it.
     */
    protected XmlConfiguration getXmlConfiguration(Reader reader, URL targetURL) throws IOException {

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbFactory.newDocumentBuilder();
            Document confDOM = db.parse(new InputSource(reader));
            XmlConfiguration result = new XmlConfiguration(confDOM, targetURL);

            return result;

        } catch (Throwable t) {

            throw (IOException) new IOException("Failed to get the XML configuration from " + targetURL).initCause(t);
        }
    }

    /**
     * Clone the source configuration.
     *
     * @param conf Configuration to clone.
     * @return A clone of the given configuration.
     */
    public Configuration clone(Configuration conf) {

        if (conf == null) {

            return null;
        }

        if (conf instanceof XmlConfiguration) {

            return cloneXmlConfiguration((XmlConfiguration) conf);

        } else if (conf instanceof TextConfiguration) {

            return cloneTextConfiguration((TextConfiguration) conf);

        } else {

            throw new Error("Not Implemented: cloning " + conf.getClass().getName());
        }
    }

    /**
     * Clone the text configuration.
     *
     * @param conf Source configuration.
     * @return The clone.
     */
    private Configuration cloneTextConfiguration(TextConfiguration conf) {

        TextConfiguration target = new TextConfiguration(null, conf.getURL());

        for (Iterator<String> i = conf.keySet().iterator(); i.hasNext();) {

            String key = i.next();
            Object value = conf.get(key);

            target.put(key, value);
        }

        return target;
    }

    /**
     * Clone the XML configuration.
     *
     * @param conf Source configuration.
     * @return The clone.
     */
    private Configuration cloneXmlConfiguration(XmlConfiguration conf) {

        return new XmlConfiguration(conf.getDocument(), conf.getURL());
    }

    /**
     * Store the configuration into the given URL.
     *
     * @param conf Configuration to store.
     * @param target URL to store the configuration into.
     * @exception IOException if there was an I/O error.
     * @exception UnsupportedOperationException if the protocol of the URL given
     * as a parameter is not supported. Currently, the only one supported is
     * {@code>file:} protocol.
     * @exception UnsupportedOperationException if the kind of configuration is
     * not supported. Unless noted otherwise, this method will try to store the
     * configuration in the same way that it currently exists, i.e.
     * {@link TextConfiguration TextConfiguration} will get stored as a flat
     * file, while {@link XmlConfiguration XmlConfiguration} will result in an
     * XML file. To store the configuration in a different form, use the
     * corresponding explicit method.
     * @see #storeText
     * @see #storeXml
     */
    public void store(Configuration conf, URL target) throws IOException {

        if (!"file".equals(target.getProtocol())) {

            throw new UnsupportedOperationException("Protocol not supported: '" + target.getProtocol() + "'");
        }

        if (conf instanceof TextConfiguration) {

            storeText(conf, target);

        } else if (conf instanceof XmlConfiguration) {

            storeXml(conf, target);
        }
    }

    /**
     * Store the configuration into the given URL.
     *
     * @param conf Configuration to store.
     * @param target URL to store the configuration into.
     * @exception IOException if there was an I/O error.
     * @exception UnsupportedOperationException if the protocol of the URL given
     * as a parameter is not supported. Currently, the only one supported is
     * {@codefile:} protocol.
     * @see #store
     * @see #storeXml
     */
    public void storeText(Configuration conf, URL target) throws IOException {

        PrintWriter pw = new PrintWriter(new FileWriter(target.getFile()));

        for (Iterator<String> i = conf.keySet().iterator(); i.hasNext();) {

            String key = i.next();

            pw.println(key + "=" + conf.getString(key));
        }

        pw.flush();
        pw.close();
    }

    /**
     * Store the configuration into the given URL.
     *
     * @param conf Configuration to store.
     * @param target URL to store the configuration into.
     * @exception UnsupportedOperationException if the protocol of the URL given
     * as a parameter is not supported. Currently, the only one supported is
     * {@codefile:} protocol.
     * @see #store
     * @see #storeText
     */
    public void storeXml(Configuration conf, URL target) {

        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Configuration specific exception.
     */
    protected class ConfigurationFactoryException extends IllegalArgumentException {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * The payload.
         */
        private Object payload;

        /**
         * Create an instance.
         *
         * @param message Exception message.
         * @param payload Exception payload.
         */
        public ConfigurationFactoryException(String message, Object payload) {

            super(message);

            this.payload = payload;
        }

        /**
         * Return a string representation.
         *
         * @return String representation.
         */
        @Override
        public String toString() {

            if (payload == null) {

                return super.toString();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            if (payload instanceof Throwable) {

                ((Throwable) payload).printStackTrace(pw);

            } else {

                pw.println(payload.toString());
            }

            pw.flush();

            return super.toString() + ", root cause:\n" + baos.toString();
        }
    }
}