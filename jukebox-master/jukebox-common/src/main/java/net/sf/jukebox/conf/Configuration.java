package net.sf.jukebox.conf;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Abstract notion of a configuration with a reasonable minimum of access methods.
 * <p>
 * This interface denotes a concept of a configuration chaining ("defaults") to
 * improve the flexibility and support the discretionary control inside of the
 * complex application.
 * <p>
 * This interface deliberately does not define any references to the
 * configuration persistence.
 * <p>
 * Another difference between this package and the other configuration packages
 * is that it is not tolerant to missing configuration values - it will not
 * return defaults, but will throw <code>NoSuchElementException</code>
 * instead. If your code is tolerant to missing configuration values, use the
 * methods that will return the default value in case when the configuration
 * value is missing from the source.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim
 * Tkachenko</a> 1998-2002
 * @version $Id: Configuration.java,v 1.3 2007-06-14 21:58:04 vtt Exp $
 */
public interface Configuration {

  /**
   * Set the default configuration.
   * <p>
   * If the configuration value for any specific item is not found in this
   * object, it will be searched for in the default configuration,
   * recursively, until a configuration object with no default will be found.
   *
   * @param defaultConf The default configuration. Becomes new top-priority
   * default, shifting the rest of the default configuration chain to the back
   * of this object's default chain.
   * @exception IllegalArgumentException if the value of the new default
   * configuration is {@code null}. To disable the defaults, use
   * {@link #clearDefaults clearDefaults()}.
   */
  void setDefaultConfiguration(Configuration defaultConf);

  /**
   * Get this configuration's base URL.
   * <p>
   * Without the default configurations URLs, that is.
   *
   * @return Configuration base URL.
   */
  URL getURL();

  /**
   * Get this configuration's URLs.
   *
   * @return The array of configuration URLs that were used to create this
   * configuration object, first element being this configuration's URL, last
   * element being the furthest default configuration's URL. If this
   * configuration was not created from the URL, return empty array.
   */
  URL[] getUrlChain();

  /**
   * Get the configuration object that serves as a default.
   *
   * @return The configuration object that is used to retrieve the values if
   * they are not found in this instance. {@code null} if it is not
   * present.
   */
  Configuration getDefaultConfiguration();

  /**
   * Clear the default configuration. The existing default chain is
   * disconnected and left to be garbage collected.
   */
  void clearDefaults();

  /**
   * Store a value.
   *
   * @param key Key to store the value under.
   * @param value Value to store. May be only {@code String} or {@code List<String>}.
   */
  void put(String key, Object value);

  /**
   * Get the object stored at the given key.
   *
   * @param key Key to extract the object for.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at the given key.
   * @return The value. May be either {@code String} or {@code List<String>}.
   */
  Object get(String key);

  /**
   * Get the object stored at the given key, or default value if there's no object.
   *
   * @param key Key to extract the object for.
   * @param defaultValue Default value to return.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value. May be either {@code String} or {@code List<String>}.
   */
  Object get(String key, Object defaultValue);

  /**
   * Get the object stored at the given key.
   *
   * @param key Key to extract the object for.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  String getString(String key);

  /**
   * Get the object stored at the given key, or default value if there's no object.
   *
   * @param key Key to extract the object for.
   * @param defaultValue Default value to return.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  String getString(String key, String defaultValue);

  /**
   * Get the object stored at the given key.
   *
   * @param key Key to extract the object for.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  boolean getBoolean(String key);

  /**
   * Get the object stored at the given key, or default value if there's no object.
   *
   * @param key Key to extract the object for.
   * @param defaultValue Default value to return.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  boolean getBoolean(String key, boolean defaultValue);

  /**
   * Get the object stored at the given key.
   *
   * @param key Key to extract the object for.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  int getInteger(String key);

  /**
   * Get the object stored at the given key, or default value if there's no object.
   *
   * @param key Key to extract the object for.
   * @param defaultValue Default value to return.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  int getInteger(String key, int defaultValue);

  /**
   * Get the object stored at the given key.
   *
   * @param key Key to extract the object for.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  long getLong(String key);

  /**
   * Get the object stored at the given key, or default value if there's no object.
   *
   * @param key Key to extract the object for.
   * @param defaultValue Default value to return.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  long getLong(String key, long defaultValue);

  /**
   * Get the object stored at the given key.
   *
   * @param key Key to extract the object for.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  double getDouble(String key);

  /**
   * Get the object stored at the given key, or default value if there's no object.
   *
   * @param key Key to extract the object for.
   * @param defaultValue Default value to return.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  double getDouble(String key, double defaultValue);

  /**
   * Get the object stored at the given key.
   *
   * @param key Key to extract the object for.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  List<String> getList(String key);

  /**
   * Get the object stored at the given key, or default value if there's no object.
   *
   * @param key Key to extract the object for.
   * @param defaultValue Default value to return.
   * @exception IllegalArgumentException if the key is null.
   * @exception NoSuchElementException if there's nothing stored at
   * the given key.
   * @return The value.
   */
  List<String> getList(String key, List<String> defaultValue);

  /**
   * Get all the configuration keys.
   *
   * @return Set containing all configuration keys.
   */
  Set<String> keySet();
}