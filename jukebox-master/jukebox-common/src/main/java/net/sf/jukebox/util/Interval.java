package net.sf.jukebox.util;

/**
 * Provide different time-related conversions from the value range to the
 * string representations.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-1998
 */
public class Interval {

    /**
     * Convert the time elapsed in milliseconds to the form <code>[+days ]
     * HH:mm:ss.SSS</code>.
     *
     * @param elapsed Milliseconds to convert
     * @return String, formed as <code>[+days ]HH:mm:ss.SSS</code>.
     */
    public static final String toTimeInterval(long elapsed) {

        if ( elapsed < 0 ) {

            return "-(" + toTimeInterval(-elapsed) + ")";
        }

        StringBuffer result = new StringBuffer();
        long millis = elapsed % 1000,
             seconds = elapsed / 1000,
 	     minutes = seconds / 60,
 	     hours = minutes / 60,
 	     days = hours / 24;

 	// Normalize everything

 	seconds %= 60;
 	minutes %= 60;
 	hours %= 24;

 	if ( days > 0 ) {

 	    result.append("+").append(Long.toString(days)).append(" ");
 	}

 	if ( days != 0 || hours != 0 ) {

 	    result.append(Long.toString(hours)).append(":");
 	}

 	if ( minutes < 10 ) {

 	    result.append("0");
 	}

 	result.append(Long.toString(minutes)).append(":");

 	if ( seconds < 10 ) {

 	    result.append("0");
 	}

 	result.append(Long.toString(seconds)).append(".");

 	if ( millis < 100 ) {

 	    result.append("0");
 	}

 	if ( millis < 10 ) {

 	    result.append("0");
 	}

 	result.append(Long.toString(millis));

 	return result.toString();
    }
}
