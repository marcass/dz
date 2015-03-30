package net.sf.jukebox.conf;

/**
 * An object that implements the basic requirements of {@link Configurable
 * Configurable}.
 *
 * <p>
 *
 * Implementation of a configurable object is quite tedious, and it has to
 * be uniform across multiple classes implementing the
 * <code>Configurable</code> interface.
 *
 * <p>
 *
 * Since Java doesn't have multiple inheritance, ordinarily the
 * implementation has to be copied and pasted into multiple locations,
 * making it error prone. This class is supposed to make life easier,
 * providing a safety net for all the classes that have to implement the
 * <code>Configurable</code> interface. In order to utilize the features
 * provided, it is necessary to include this object as a member into the
 * class implementing the <code>Configurable</code> interface and
 * short-circuit all the methods of <code>Configurable</code> to call this
 * object's methods.
 *
 * <p>
 *
 * This way is not ideal either. Another way would be to copy and paste this
 * class into the target class, or, if there are multiple classes having the
 * same base class, copy this implementation into that base class and make
 * {@link #configure() configure()} an abstract method, or make it final and
 * introduce another abstract method called from within
 * <code>configure</code>.
 *
 * <p>
 *
 * Honestly, all the ways suck, but given the absence of multiple
 * inheritance (this is one of those rare cases where I would have really,
 * really welcomed it), there's no viable option.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2000
 * @version $Id: ConfigurableHelper.java,v 1.2 2007-06-14 04:32:08 vtt Exp $
 */
public class ConfigurableHelper implements Configurable {

    /**
     * Configuration root to use.
     */
    private String configurationRoot;

    /**
     * Configuration to use.
     */
    private Configuration conf;

    /**
     * {@inheritDoc}
     */
    public final synchronized void configure(final String configurationRoot, final Configuration conf) {

        if (configurationRoot == null) {
            throw new IllegalArgumentException("configurationRoot can't be null");
        }

        if (conf == null) {
            throw new IllegalArgumentException("conf can't be null");
        }

        // The non-null configuration root is deemed to be sufficient to
        // determine if the object is already configured because we don't
        // accept neither null configuration root nor null configuration.
        final boolean alreadyConfigured = this.configurationRoot != null;

        this.configurationRoot = configurationRoot;
        this.conf = (new ConfigurationFactory()).clone(conf);

        boolean ok = false;
        try {

            if (alreadyConfigured) {

                reconfigure();

            } else {

                configure();
            }

            ok = true;

        } catch (UnsupportedOperationException ex) {

            throw ex;

        } catch (Throwable t) {

            throw (IllegalArgumentException) new IllegalArgumentException("Unexpected exception").initCause(t);

        } finally {

            if (!ok) {

                this.configurationRoot = null;
                this.conf = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String getConfigurationRoot() {

        if ( configurationRoot == null ) {

            throw new IllegalStateException("Not configured yet");
        }

        return configurationRoot;
    }

    /**
     * {@inheritDoc}
     */
    public final Configuration getConfiguration() {

        if ( conf == null ) {

            throw new IllegalStateException("Not configured yet");
        }

        return conf;
    }

    /**
     * Stub.
     *
     * @exception UnsupportedOperationException with the message about what
     * needs to be done.
     * @exception Throwable not actually thrown, but preserved to allow
     * subclasses to throw it.
     */
    protected void configure() throws Throwable {

        throw new UnsupportedOperationException(getClass().getName() + ": you have to override configure() in order to make this object useful");
    }

    /**
     * Stub.
     *
     * @exception UnsupportedOperationException with the message about what
     * needs to be done.
     * @exception Throwable not actually thrown, but preserved to allow
     * subclasses to throw it.
     */
    protected void reconfigure() throws Throwable {

        throw new UnsupportedOperationException(getClass().getName() + ": you have to override reconfigure() in order to make this object useful");
    }
}