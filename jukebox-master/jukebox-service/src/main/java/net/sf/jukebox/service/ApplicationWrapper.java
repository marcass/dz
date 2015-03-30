package net.sf.jukebox.service;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;

/**
 * Wrapper to run the descendants of {@link PassiveService}.
 * 
 * {@link #main(String[])} method is invoked, first argument is parsed as the name class to instantiate and run, the rest are treated as a command line for the target.
 * 
 * @since Jukebox v2
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2009
 */
public class ApplicationWrapper {
  
  /**
   * @param args
   */
  public static void main(String[] args) {

    final Logger logger = Logger.getLogger(ApplicationWrapper.class);

    NDC.push("main");
    try {

      logger.info("Complete, exitCode=" + new ApplicationWrapper().execute(logger, args));
      
    } catch (Throwable t) {

      logger.fatal("Unexpected exception", t);

    } finally {
      NDC.pop();
    }
  }

  private boolean execute(final Logger logger, final String[] args) throws Throwable {
    
    initLogWatcher(logger);

    NDC.push("env");
    for (Iterator<Object> env = System.getProperties().keySet().iterator(); env.hasNext();) {
      String key = env.next().toString();
      logger.debug(key + ": " + System.getProperty(key));
    }
    NDC.pop();

    PassiveService service = createService(logger, args);

    service.start();

    logger.info("Waiting for the server startup completion");

    // Try to start the service

    try {

      if (!service.getSemUp().waitFor()) {
        throw new IllegalStateException("Failed to start");
      }

    } catch (InterruptedException iex) {

      terminate(logger, iex);
    }

    // Try to attach the shutdown handler
    initShutdownHandler(logger, service);

    // Wait for the service to complete execution

    boolean exitCode = false;

    try {

      logger.info("Waiting for the service completion");
      exitCode = service.getSemStopped().waitFor();
      // stoppedAt = System.currentTimeMillis();
      // logger.info(CH_WRAPPER, "Service is being shut down");

      service.getSemDown().waitFor();
      logger.info("Service has been shut down.");

    } catch (InterruptedException iex) {
      terminate(logger, iex);
    }

    return exitCode;
  }

  /**
   * Try to make sure that the log file gets watched, if possible.
   */
  private void initLogWatcher(Logger logger) {
    
    NDC.push("initLogWatcher");
    try {
      String log4jProperties = System.getProperty("log4j.configuration");

      if (log4jProperties == null || "".equals(log4jProperties)) {

        // The logger is possibly initialized already
        logger.info("log4j.configuration environment variable is not defined, nothing to watch");
        return;
      }

      logger.info("log4j.configuration=" + log4jProperties);
      
      URL log4jPropertiesURL = new URL(log4jProperties);

      if (!log4jPropertiesURL.getProtocol().equals("file")) {
        logger.warn("Don't know how to watch " + log4jPropertiesURL);
        return;
      }

      PropertyConfigurator.configureAndWatch(log4jPropertiesURL.getFile(), 10000);

      logger.info("Watching " + log4jPropertiesURL.getFile());

    } catch (Throwable t) {
      // There's nothing we can do but complain
      logger.error("Unrecoverable exception trying to initialize log watcher", t);
    } finally {
      NDC.pop();
    }
  }

  /**
   * Create the service to run.
   *
   * @param logger Logger to use.
   * @param args <ul>
   * <li>(First item) Class name for the service to run
   * <li>(Command tail) parameters from {@code main()}.
   * </ul>
   *
   * @return Service to run.
   */
  PassiveService createService(Logger logger, final String[] args) {


    NDC.push("createService");
    try {

      logger.info("Instantiating " + args[0]);

      Class<?> classDef = Class.forName((String) args[0]);
      Object target = classDef.newInstance();

      if (!(target instanceof PassiveService)) {
        terminate(logger, "Target class (" + target.getClass().getName() + ") is not a subclass of PassiveService");
      }
      
      // If we're here, the object was successfully instantiated
      
      if (args.length < 2) {
        
        // Nothing to initialize with
        return (PassiveService) target;
      }

      List<String> argumentList = new LinkedList<String>();

      for (int offset = 1; offset < args.length; offset++) {
        argumentList.add(args[offset]);
      }

      ((PassiveService) target).setUserObject(argumentList);

      return (PassiveService) target;

    } catch (Throwable t) {

      terminate(logger, t);

      // This is unreachable
      throw new IllegalStateException("Wasn't supposed to get here");
    
    } finally {
      NDC.pop();
    }
  }

  /**
   * Terminate. Flush the log, print diagnostic message, if any, and
   * terminate.
   *
   * @param message Message to supply as a termination reason.
   */
  private void terminate(final Logger logger, final String message) {

    logger.error("Service has been terminated");
    logger.info("Post-mortem: " + message);

    System.exit(1);
  }

  /**
   * Terminate. Flush the log, print exception stack trace and terminate.
   *
   * @param logger Logger to use.
   * @param t Exception to supply as a termination reason.
   */
  private void terminate(final Logger logger, final Throwable t) {

    logger.error("Terminated on exception:", t);

    System.exit(1);
  }

  /**
   * Try to instantiate and attach the shutdown wrapper to the service.
   *
   * @param service Service to attach to the shutdown handler.
   */
  void initShutdownHandler(final Logger logger, final StoppableService service) {

    // Since this happens in parallel with the service execution, no big
    // deal if we just try to instantiate the class anyway

    try {

      Class<?> c = Class.forName("net.sf.jukebox.service.ShutdownHandler13");
      ShutdownHandler sh = (ShutdownHandler) c.newInstance();

      sh.setTarget(service);

    } catch (Throwable t) {

      // Oh well, didn't work. The hell with it, then

      logger.warn("Couldn't install the shutdown handler, cause:", t);
    }
  }
}
