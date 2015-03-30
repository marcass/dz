package net.sf.jukebox.conf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * This class reads UNIX-like config files and loads the properties into the
 * given configuration object.
 * <p>
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1998
 * @author Significant influence from <a href="http://java.apache.org" target=_top>Java-Apache
 * Project</a> in general and code written by <a
 * href="mailto:mazzocch@systemy.it">Stefano Mazzocchi</a> in particular.
 *
 * @version $Id: PropertiesReader.java,v 1.2 2007-06-14 04:32:08 vtt Exp $
 */
public class PropertiesReader extends TextConfigurationReader {

    /**
     * Create the instance.
     *
     * @param fileName Name of the file to read the properties from.
     *
     * @throws IOException when thrown by underlying <code>java.io.*</code> method calls.
     */
    public PropertiesReader( String fileName ) throws IOException {
        this( new InputStreamReader( new FileInputStream( fileName ) ) );
    }

    /**
     * Create the instance.
     *
     * @param reader The stream reader to read the properties from.
     */
    public PropertiesReader( Reader reader ) {
        super( reader );
    }

    /**
     * Read the properties into the given target.
     * <p>
     *
     * This method reads the input stream lines one by one, and tries to
     * parse them according to the rules at the top of the page.
     *
     * @param target Configuration object to load.
     * @exception IOException if there was a read error.
     */
    public void load( Configuration target ) throws IOException {
        while ( true ) {
            String line = readLine();

            if ( line == null ) {
                break;		//	We're finished here
            }

            parseString( line,target );
        }
    }

    /**
     * Parse one line and load the results, if any, into the target.
     *
     * @param line Line to parse
     *
     * @param target Configuration to load the results to.
     *
     * @throws IOException if there's a malformed line (empty configuration
     * keywords or lines without any 'keyword=value' pairs at all).
     */
    protected void parseString(String line, Configuration target) throws IOException {

        int idx = line.indexOf( "=" );

        // I don't allow the empty keys, as well as no keys at all.

        if ( idx <= 0 ) {

            throw new IOException( Integer.toString( getLineNumber() )
             	+ ": malformed line: equal sign is missing or at first position: '"+line+"'" );
        }

        String	key = line.substring(0, idx).trim(),
 	 	value = line.substring(idx + 1).trim();

 	target.put(unescape(key), unescape(value));
    }

    /**
     * Get rid of the quotes, unless they're escaped.
     *
     * @param source Original string, possibly escaped.
     *
     * @return The original string stripped from the quotes.
     *
     * @exception IOException if the double quote is not balanced.
     */
    protected String unescape(String source) throws IOException {

//        System.err.println("Source: '" + source + "'");

        StringBuffer target = new StringBuffer();

        // True if the current character is within the double quote

        // VT: FIXME: what I was thinking about?
        //boolean inQuote = false;

        // True if the previous character is a backslash (escape)

        boolean escaped = false;

        for ( int idx = 0; idx < source.length(); idx++ ) {

            char ch = source.charAt(idx);

            if ( ch == '"' ) {

                if ( escaped ) {

                    // This double quote was escaped

                    target.append(ch);
                    escaped = false;

                } else {

                    // This double quote ends the escape

                    escaped = false;
                }

            } else {

                // Escape is good for one symbol only. If some other character
                // except a double quote was escaped, we don't care and pass the
                // escape on. In any case, escape flag is cleared.

                if ( escaped ) {

                    target.append('\\');
                    target.append(ch);
                    escaped = false;

                } else if ( ch == '\\' ) {

                    escaped = true;

                } else {

                    target.append(ch);
                    escaped = false;
                }
            }
        }

        if ( escaped ) {

            throw new IOException("Unbalanced double quote: '" + source + "'");
        }

        String result = target.toString();
//        System.out.println("Target: '" + result + "'");
        return result;
    }
}