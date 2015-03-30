package net.sf.jukebox.conf;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * This class reads UNIX-like config files and filters out the comments and
 * blank lines.
 * <p>
 * In the current implementation API users are strongly discouraged from using
 * any methods other than {@link #readLine readLine} - results are
 * unpredictable. <br>
 * Actually, the better idea would be to implement them properly, but I don't
 * have any time right now, and the implementation satisfies its primary goal:
 * read the UNIX config files.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim
 * Tkachenko</a> 1998
 * @author Significant influence from <a href="http://java.apache.org"
 * target=_top>Java-Apache Project</a> in general and code written by <a
 * href="mailto:mazzocch@systemy.it">Stefano Mazzocchi</a> in particular.
 * @version $Id: TextConfigurationReader.java,v 1.2 2007-06-14 04:32:09 vtt Exp $
 */
public class TextConfigurationReader extends LineNumberReader {

    /**
     * Create a new config reader, using the default input-buffer size.
     *
     * @param reader Reader to read the configuration from.
     */
    public TextConfigurationReader(Reader reader) {

        super(reader);
    }

    /**
     * Create a new config reader, reading characters into a buffer of the given
     * size.
     *
     * @param reader Reader to read the configuration from.
     *
     * @param bufferSize Buffer size to use.
     */
    public TextConfigurationReader(Reader reader, int bufferSize) {

        super(reader, bufferSize);
    }

    /**
     * Read a line of text. <br>
     * Processing order is as follows:
     * <ol>
     * <li>Part of the line beginning with '#' is trimmed, unless it's escaped
     * with a backslash.
     * <li>Leading and trailing spaces are trimmed.
     * <li>Empty lines are skipped.
     * </ol>
     * FIXME: continued lines, double backslash at the end of line.
     *
     * @return Filtered non-empty non-comment line.
     *
     * @exception IOException If the superclass throws it.
     */
    @Override
    public String readLine() throws IOException {

        String line = null;

        while (true) {

            line = super.readLine();

            if (line == null) {

                return null;
            }

            // Get rid of comments

            // VT: FIXME: not handling the escaped comments

            int hash = line.indexOf("#");

            if (hash != -1) {

                // Check if the '#' is escaped

                if (hash > 0 && line.indexOf("\\#") == hash - 1) {

                    // Yes, it is

                    // System.err.println( "Raw: '"+line+"'" );
                    line = line.substring(0, hash - 1) + line.substring(hash, line.length());
                    // System.err.println( "Unescaped: '"+line+"'" );

                } else {

                    line = line.substring(0, hash);
                }
            }

            // Trim it

            line = line.trim();

            if (line.length() != 0) {

                return line;
            }
        }
    }
}