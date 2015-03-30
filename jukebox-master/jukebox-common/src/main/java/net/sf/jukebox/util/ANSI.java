package net.sf.jukebox.util;

/**
 * ANSI color ESCAPE sequence support.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2007
 * @version $Id: ANSI.java,v 1.2 2007-06-14 04:32:21 vtt Exp $
 */
public enum ANSI {

  RED   ("[0;40;31m"),
  GREEN ("[0;40;32m"),
  YELLOW("[0;40;33m"),
  BLUE  ("[0;40;34m"),
  PURPLE("[0;40;35m"),
  CYAN  ("[0;40;36m"),
  GRAY  ("[0;40;37m"),

  REVERSE_RED   ("[7;40;31m"),
  REVERSE_GREEN ("[7;40;32m"),
  REVERSE_YELLOW("[7;40;33m"),
  REVERSE_BLUE  ("[7;40;34m"),
  REVERSE_PURPLE("[7;40;35m"),
  REVERSE_CYAN  ("[7;40;36m"),
  REVERSE_WHITE  ("[7;40;37m"),

  BRIGHT_RED   ("[1;40;31m"),
  BRIGHT_GREEN ("[1;40;32m"),
  BRIGHT_YELLOW("[1;40;33m"),
  BRIGHT_BLUE  ("[1;40;34m"),
  BRIGHT_PURPLE("[1;40;35m"),
  BRIGHT_CYAN  ("[1;40;36m"),
  WHITE        ("[1;40;37m"),

  RESET        ("[0m");

  /**
   * Esc code (0x1b).
   */
  private static final String ESCAPE = "\u001b";

  /**
   * Color sequence.
   * <p/>
   * Responsible for rendering a required color.
   */
  private String sequence;

  /**
   * Create the ANSI sequence.
   * <p/>
   * The sequence is a parameter prepended by the <b>Esc</b> code
   * (<code>0x1b</code>).
   *
   * @param sequence Sequence without the leading <b>Esc</b> character.
   */
  ANSI(String sequence) {
    this.sequence = ESCAPE + sequence;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return sequence;
  }
}
