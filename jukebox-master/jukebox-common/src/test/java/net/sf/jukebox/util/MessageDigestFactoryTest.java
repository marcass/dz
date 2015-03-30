package net.sf.jukebox.util;

import junit.framework.TestCase;

/**
 * 
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2001-2009
 */
public class MessageDigestFactoryTest extends TestCase {

    private final String message = "I know the word";

    public void testMD5() {
        
        String digest = new MessageDigestFactory().getMD5(message);
        assertEquals("084848e5ff80a02c58ced8d7307aa7b6", digest);
    }

    public void testSHA() {
        
        String digest = new MessageDigestFactory().getSHA(message);
        assertEquals("876d1e8893c76b77429faa02ee33d3312ce447c0", digest);
    }
}
