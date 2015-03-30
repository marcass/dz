package net.sf.jukebox.util;

/**
 * Utility class to strip the package components off a class name.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-1999
 */
public class PackageNameStripper {

    /**
     * Get the last part of the string after all dots, if any.
     *
     * @param target String to strip.
     * @return Substring after a last dot. If there was no dot, returns
     * {@code target} parameter.
     */
    public static final String stripPackage(String target) {

        int idx = target.lastIndexOf('.');

        if (idx == -1) {
            return target;
        }

        return target.substring(idx + 1);
    }
}