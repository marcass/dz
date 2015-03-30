package net.sf.jukebox.conf;

import java.net.URL;

import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Successor to the classical {@link Configuration Configuration} that
 * supports the XML configuration files and objects.
 *
 * <p>
 *
 * So far, there's one unresolved issue: how to add the values that do not
 * have an XML representation into the configuration. Therefore, this object
 * is read only, and will throw an exception on any attempt to modify it.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2000
 * @version $Id: XmlConfiguration.java,v 1.2 2007-06-14 04:32:09 vtt Exp $
 */
public class XmlConfiguration extends TextConfiguration {

    /**
     * The XML configuration object.
     */
    private Document conf;

    /**
     * The Hack.
     */
    private boolean unsafeAllowed = false;

    /**
     * Create the empty configuration.
     */
    public XmlConfiguration() {

        this(null, null);
    }

    /**
     * Create the configuration based on the given XML document.
     *
     * VT: FIXME: I want to have the DTD reference so this stays validated.
     *
     * @param conf XML document to use as a configuration source.
     */
    public XmlConfiguration(Document conf) {

        super(null, null);

        this.conf = conf;
        parse();
    }

    /**
     * Create the configuration based on the given XML document, based on a
     * given URL.
     *
     * VT: FIXME: I want to have the DTD reference so this stays validated.
     *
     * @param conf XML document to use as a configuration source.
     *
     * @param baseURL URL to base on. This value is declarative, it is never
     * used by the configuration itself, however, it is used by the {@link
     * ConfigurationWatcher ConfigurationWatcher} and {@link
     * ConfigurationChangeListener ConfigurationChangeListener}.
     */
    public XmlConfiguration(Document conf, URL baseURL) {

        super(null, baseURL);

        this.conf = conf;
        parse();
    }

    /**
     * @return The XML document object, for those who are not lazy to walk
     * it.
     *
     * <p>
     *
     * <strong>NOTE:</strong> we return a <strong>clone</strong>, not a reference.
     */
    public Document getDocument() {

        return (conf == null)
        		? null
        		: conf.getDocumentElement().cloneNode(true).getOwnerDocument();
    }

    /**
     * Get the configuration elements.
     *
     * @param path Dot delimited path to the element.
     *
     * @return A collection of the elements with the same path. If there are
     * no elements, the collection will be empty.
     */
    public List<Node> getElements(String path) {

        // Let's split the path into the components first.

        StringTokenizer st = new StringTokenizer(path, ".");
        List<String> vpath = new LinkedList<String>();

        while ( st.hasMoreTokens() ) {

            vpath.add(st.nextToken());
        }

//        logger.debug(CH_XC + "/getElements", "Path requested: " + vpath, null);

        // Now let's walk it

        Element root = conf.getDocumentElement();
        List<Node> result = new LinkedList<Node>();

        resolveElement(root, vpath, result);

        return result;
    }

    /**
     * Resolve the element by path.
     *
     * @param current Element to start resolving from.
     *
     * @param path Sequence of tag names to be found.
     *
     * <strong>NOTE:</strong> elements from this vector get removed as we
     * successfully walk the document.
     *
     * @param result The container that will hold the elements found.
     */
    private void resolveElement(Element current, List<String> path, List<Node> result) {

         if ( path.isEmpty() ) {

             // OK, we've finished.

//             logger.warn(CH_XC, "path exhausted", null);
             return;
         }

/*         logger.debug(CH_XC, "Resolving "
         	+ current.getTagName()
         	+ ", path "
         	+ path,
         	null);
 */
         String pathElement = path.remove(0);

         // Now, let's see who's got the name we want

         Node cursor = current;

         while ( cursor != null ) {

             String name = cursor.getNodeName();

//             logger.debug(CH_XC, "Analyzing: " + name, null);

             if ( name.equals(pathElement) ) {

                 if ( path.isEmpty() ) {

                     // Bingo!

                     result.add(cursor);
//                     logger.info(CH_XC, "Adding element: " + path + " " + name, null);

                 } else {

                     // We've got a component of the name right, but there
                     // are other elements left in the path.

//                     logger.info(CH_XC, "Found path element: " + name, null);

                     Node child = cursor.getFirstChild();

                     while ( child != null && child.getNodeType() != Node.ELEMENT_NODE ) {

                         child = child.getNextSibling();
                     }

                     if ( child == null ) {

//                         logger.info(CH_XC, "No more children @" + name, null);
                         return;
                     }

                     resolveElement((Element)child, new LinkedList<String>(path), result);
                 }
             }

             // Skip the cursor until the next sibling which is an Element


             do {

                 cursor = cursor.getNextSibling();

             } while ( cursor != null && cursor.getNodeType() != Node.ELEMENT_NODE );
         }
    }

    /**
     * Parse the {@link #conf XML document} into the configuration.
     */
    private synchronized void parse() {

        parseEntry(conf.getDocumentElement(), "");
    }

    /**
     * Parse the XML node located at the given prefix.
     *
     * @param node Node to parse.
     * @param prefix Prefix the node is attached to.
     */
    private void parseEntry(Node node, String prefix) {

        boolean hasChildren = node.hasChildNodes();

/*        logger.warn(CH_XC, "Parsing entry: "
        	+ prefix
        	+ "/"
        	+ node.getNodeName()
        	+ (hasChildren?" (has children)":""),
        	null);
 */
        switch ( node.getNodeType() ) {

            case Node.TEXT_NODE:

                if ( !("".equals(node.getNodeValue().trim())) ) {

/*                    logger.info(CH_XC, prefix
                	+ " = '"
                	+ node.getNodeValue().trim()
                	+ "'",
                	null);
 */
                    super.put(prefix, node.getNodeValue().trim());
                }
                break;

            case Node.ELEMENT_NODE:

                if ( !("".equals(prefix)) ) {

                    prefix += ".";
                }

                prefix += node.getNodeName();

                String name = ((Element)node).getAttribute("name");

                if ( !("".equals(name)) ) {

                    super.put(prefix, name);
                    prefix += "." + name;
                }
                String enabled = ((Element)node).getAttribute("enabled");

                if ( !("".equals(enabled)) ) {

                    super.put(prefix + ".enabled", enabled);
                }

                break;
        }

        if ( hasChildren ) {

            Node cursor = node.getFirstChild();

            while ( cursor != null ) {

                parseEntry(cursor, prefix);
                cursor = cursor.getNextSibling();
            }

        }
    }

    //String CH_XC = "XmlConf";

    /*
    private void complain(String message, Throwable t) {

        System.err.println("XmlConfiguration: " + message);

        if ( t != null ) {

            t.printStackTrace();
        }
    }
    */

    /**
     * Add a mapping from the key to the value.
     *
     * <p>
     *
     * <strong>NOTE:</strong> Changing the XML document has not been yet
     * implemented, and since there's no real demand, is unlikely to be
     * implemented anytime soon. However, there <strong>is</strong> a need
     * to modify the configuration once in a while even though the
     * underlying XML object doesn't change, you just have to completely
     * understand what you're doing. If you think you do, call {@link
     * #allowUnsafe allowUnsafe()} and go ahead. Don't forget to read the
     * source and make sure you understand the implications.
     *
     * @param key The key.
     *
     * @param value The value.
     *
     * @throws IllegalAccessError unless you RTFM.
     *
     * @see #allowUnsafe
     */
    @Override
    public void put(String key, Object value) {

        if ( !unsafeAllowed ) {

            throw new IllegalAccessError("XmlConfiguration is read only unless you call allowUnsafe() first");
        }

        super.put(key, value);
    }

    /**
     * @see #put
     */
    public void allowUnsafe() {

        unsafeAllowed = true;
    }
}