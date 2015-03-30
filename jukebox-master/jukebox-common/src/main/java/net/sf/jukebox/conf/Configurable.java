package net.sf.jukebox.conf;

/**
 * General definition of a configurable object.
 *
 * <p>
 *
 * Generally speaking, there's one serious problem with the configuration
 * per se: the chicken and egg problem, or the bootstrap sequence.
 *
 * <p>
 *
 * For example, every configurable object has to be able to tell what
 * exactly part of the original configuration is responsible for its
 * configuration, and it is not easy to determine. Thus, in order to
 * facilitate this, every configurable object has to export its own {@link
 * #getConfigurationRoot identifier} within a configuration name space.
 *
 * <p>
 *
 * For the flexibility reasons, this is an interface, not a class, despite
 * the fact that there's a lot of logic embedded into this interface.
 * Unfortunately, lack of multiple inheritance in Java dictates this
 * decision. In order to make it more or less reliable, another object,
 * {@link ConfigurableHelper ConfigurableHelper} provides the implementation
 * of this interface and can be used to make the configurable object less
 * cumbersome.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1998-2000
 * @version $Id: Configurable.java,v 1.2 2007-06-14 04:32:08 vtt Exp $
 * 
 * @deprecated Self-aware object configuration model is being retired in favor of dependency injection.
 */
public interface Configurable {

    /**
     * Configure the object.
     *
     * @param configurationRoot Dot delimited string defining the way from
     * the configuration top to the configuration element defining the
     * configuration for this object.
     *
     * @param conf Configuration to configure the object with.
     *
     * @exception IllegalStateException if the object has already been
     * configured.
     *
     * @exception IllegalArgumentException if the configuration values are
     * unusable.
     */
    public void configure(String configurationRoot, Configuration conf);

    /**
     * Get the path to the root configuration element.
     *
     * @return Dot delimited string defining the way from the configuration
     * top to the configuration element defining the configuration for this
     * object.
     *
     * @exception IllegalStateException if the object has not been {@link
     * #configure configured} yet.
     */
    public String getConfigurationRoot();

    /**
     * Get the configuration itself.
     *
     * @return The configuration.
     *
     * @exception IllegalStateException if the object has not been {@link
     * #configure configured} yet.
     */
    public Configuration getConfiguration();
}