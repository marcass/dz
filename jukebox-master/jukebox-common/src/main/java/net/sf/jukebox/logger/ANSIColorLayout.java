package net.sf.jukebox.logger;

import java.util.HashMap;
import java.util.Map;

import net.sf.jukebox.util.ANSI;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Basic ANSI color layout.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2007
 * @version $Id: ANSIColorLayout.java,v 1.2 2007-06-14 04:32:15 vtt Exp $
 */
public class ANSIColorLayout extends PatternLayout {

  /**
   * Color map.
   */
  private Map<Level, ANSI> colorMap = new HashMap<Level, ANSI>();

  /**
   * {@inheritDoc}
   */
  public ANSIColorLayout() {
    this(DEFAULT_CONVERSION_PATTERN);
  }

  /**
   * {@inheritDoc}
   */
  public ANSIColorLayout(String pattern) {
    super(pattern);

    reset();
  }

  /**
   * Populate the {@link #colorMap} with default values.
   */
  private void reset() {

    colorMap.put(Level.ALL, ANSI.REVERSE_WHITE);
    colorMap.put(Level.FATAL, ANSI.REVERSE_RED);
    colorMap.put(Level.ERROR, ANSI.BRIGHT_RED);
    colorMap.put(Level.WARN, ANSI.BRIGHT_YELLOW);
    colorMap.put(Level.INFO, ANSI.WHITE);
    colorMap.put(Level.DEBUG, ANSI.CYAN);
    colorMap.put(Level.TRACE, ANSI.GRAY);
  }

  @Override
  public String format(LoggingEvent event) {

    StringBuilder sb = new StringBuilder();
    ANSI sequence = colorMap.get(event.getLevel());

    if (sequence == null) {
      sequence = ANSI.REVERSE_WHITE;
    }

    sb.append(sequence.toString()).append(super.format(event));

    formatException(sb, event);

    sb.append(ANSI.RESET.toString());

    return sb.toString();
  }

  /**
   * Format the logging event exception and append it to the buffer.
   * @param sb Buffer to append to.
   * @param event Event to extract the exception information from.
   */
  private void formatException(StringBuilder sb, LoggingEvent event) {

    String[] s = event.getThrowableStrRep();
    if (s != null) {
      int len = s.length;
      for(int i = 0; i < len; i++) {
        sb.append(s[i]);
        sb.append(Layout.LINE_SEP);
      }
    }
  }

  /**
   * Unless this is done, the exception trace will not get any color at all - this is not what I want.
   *
   * @return {@code false}.
   */
  @Override
  public boolean ignoresThrowable() {
    return false;
  }
}
