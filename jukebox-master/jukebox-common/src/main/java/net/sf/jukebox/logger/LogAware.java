package net.sf.jukebox.logger;

import org.apache.log4j.Logger;

/**
 * Basic log aware object.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1998-2009
 */
public abstract class LogAware {

    /**
     * The logger to submit the log messages to.
     */
    protected final Logger logger = Logger.getLogger(getClass());

    protected LogAware() {
    }

    /**
     * @return The current logger.
     */
    public final Logger getLogger() {
        return logger;
    }
}