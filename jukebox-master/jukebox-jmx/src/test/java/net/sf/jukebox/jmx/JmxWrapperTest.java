package net.sf.jukebox.jmx;

import java.util.Hashtable;
import java.util.Random;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2000-2009
 */
public class JmxWrapperTest extends TestCase {

    private final Logger logger = Logger.getLogger(getClass());

    private ObjectName getObjectName() throws MalformedObjectNameException {
        Hashtable<String, String> properties = new Hashtable<String, String>();

        properties.put("id", Double.toString(new Random().nextGaussian()));
        return new ObjectName("testDomain", properties);
    }

    public void testLiteral() throws Throwable {
        new JmxWrapper().expose(new LiteralAccessor(), getObjectName(), "Literal accessor");
        assertTrue("We've made it", true);
    }

    public void testGoodAccessor() throws Throwable {
        new JmxWrapper().expose(new GoodAccessor(), getObjectName(), "Properly named accessor");
        assertTrue("We've made it", true);
    }

    public void testAccessorMutator() throws Throwable {
        new JmxWrapper().expose(new AccessorMutator(), getObjectName(), "Accessor & mutator");
        assertTrue("We've made it", true);
    }

    public void testAccessorBadMutator() throws Throwable {
        new JmxWrapper().expose(new AccessorBadMutator(), getObjectName(), "Good accessor, bad mutator");
        assertTrue("We've made it", true);
    }

    public void testBadAccessorHasArguments() throws Throwable {
        try {
            new JmxWrapper().expose(new BadAccessorHasArguments(), getObjectName(), "Bad accessor signature - takes arguments");
        } catch (IllegalArgumentException e) {
            logger.info(e);
            assertTrue("Null exception message", e.getMessage() != null);
            assertEquals("Unexpected exception message", "name() is not an accessor (takes arguments)", e.getMessage());
        }
    }

    public void testBadAccessorReturnsVoid() throws Throwable {
        try {
            new JmxWrapper().expose(new BadAccessorReturnsVoid(), getObjectName(), "Bad accessor signature - returns void");
        } catch (IllegalArgumentException e) {
            logger.info(e);
            assertTrue("Null exception message", e.getMessage() != null);
            assertEquals("Unexpected exception message", "name() is not an accessor (returns void)", e.getMessage());
        }
    }

    public void testInterfaceDefined() throws Throwable {
        new JmxWrapper().expose(new TheImplementation(), getObjectName(), "Annotation on the interface");
        assertTrue("We've made it", true);
    }

    class LiteralAccessor {

        @JmxAttribute(description="just the name")
        public String name() {
            return "name";
        }
    }

    class GoodAccessor {

        @JmxAttribute(description="just the name")
        public String getName() {
            return "name";
        }
    }

    class AccessorMutator {

        @JmxAttribute(description="just the name")
        public String getName() {
            return "name";
        }

        public void setName(String name) {
        }
    }

    class AccessorBadMutator {

        @JmxAttribute(description="just the name")
        public String getName() {
            return "name";
        }

        public void setName(Set<?> name) {
        }
    }

    class BadAccessorHasArguments {

        @JmxAttribute(description="just the name")
        //@ConfigurableProperty(
        //  propertyName="name",
        //  description="name given"
        //)
        public String name(String key) {
            return "name";
        }
    }

    class BadAccessorReturnsVoid {

        @JmxAttribute(description="just the name")
        public void name() {
        }
    }

    interface TheInterface {

        @JmxAttribute(description="defined in the interface")
        String getInterfaceDefined();
    }

    class TheConcreteSuperclass {

        @JmxAttribute(description = "defined in the concrete superclass")
        public String getConcreteSuperclassDefined() {
            return "concrete superclass";
        }
    }

    abstract class TheAbstractSuperclass extends TheConcreteSuperclass {

        @JmxAttribute(description = "defined in the abstract superclass")
        public abstract String getAbstractSuperclassDefined();
    }

    class TheImplementation extends TheAbstractSuperclass implements TheInterface {

        public String getInterfaceDefined() {
            return "must be exposed though the annotation is present only on the interface";
        }

        @Override
        public String getAbstractSuperclassDefined() {
            return "must be exposed through the annotation is present only on the abstract superclass";
        }

        @Override
        public String getConcreteSuperclassDefined() {
            return "must be exposed through the annotation is present only on the concrete superclass";
        }
    }
}

