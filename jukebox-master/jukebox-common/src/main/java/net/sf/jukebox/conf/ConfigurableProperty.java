package net.sf.jukebox.conf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used to denote configurable properties.
 *
 * The value addressed by this annotation will be applied to the method the annotation is found on.
 *
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfigurableProperty {

  /**
   * Name of the context variable to take the value from.
   *
   * The definition of "context" is left as an excercise to the reader.
   *
   * First, the variable with the literal name is taken from the context.
   * If it is missing, the class name of the class being introspected is prepended to the name
   * of the property, dot delimited, and looked up. If it is still missing, the {@link #defaultValue()}
   * is used. If it is missing, the implementation is supposed to blow up with {@link IllegalStateException}.
   *
   * @return Property name.
   */
  String propertyName();

  /**
   * Human readable description of what it is. Will be provided as an exception message
   * if there are any problems associated with this property.
   *
   * @return Human readable description.
   */
  String description();

  /**
   * The default value.
   *
   * Will be invoked if the property can't be resolved.
   *
   * VT: FIXME: It would be interesting to see if the property can be treated as an XML representation of a JAXB capable object.
   *
   * @return Default value.
   */
  String defaultValue() default "";
}
