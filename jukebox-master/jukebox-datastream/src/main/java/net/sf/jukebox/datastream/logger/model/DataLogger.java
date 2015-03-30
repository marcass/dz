package net.sf.jukebox.datastream.logger.model;

import net.sf.jukebox.datastream.signal.model.DataSink;

/**
 * A universal data logger.
 * <p>
 * Unlike the {@link AbstractLogger previous incarnation}, this logger is source agnostic. All it needs to function is a
 * signal signature and a signal value.
 * <p>
 * Also unlike the previous incarnation, this logger is not responsible for creating graphs - a different module will be
 * doing that.
 *
 * @param <E> Data type to log.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2005-2008
 */
public interface DataLogger<E extends Number> extends DataSink<E> {

}
