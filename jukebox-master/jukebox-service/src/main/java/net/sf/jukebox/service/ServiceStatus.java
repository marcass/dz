package net.sf.jukebox.service;

/**
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a>
 */
public interface ServiceStatus {
  boolean isActive();
  boolean isEnabled();
  boolean isReady();
  long getUptimeMillis();
  String getUptime();
}