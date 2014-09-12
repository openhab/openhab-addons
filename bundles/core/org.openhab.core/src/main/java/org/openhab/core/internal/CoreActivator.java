/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.model.rule.runtime.RuleEngine;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * @author Kai Kreuzer
 * @author Thomas.Eichstaedt-Engelen
 * 
 * @since 0.1.0
 */
public class CoreActivator implements BundleActivator {

	private static Logger logger = LoggerFactory.getLogger(CoreActivator.class);

	private static final String STATIC_CONTENT_DIR = "id";

	private static final String UUID_FILE_NAME = "uuid";

	private static final String VERSION_FILE_NAME = "version";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		createUUIDFile();

		String versionString = context.getBundle().getVersion().toString();
		String buildString = "";
		// if the version string contains a qualifier, remove it!
		if (StringUtils.countMatches(versionString, ".") == 3) {
			buildString = StringUtils.substringAfterLast(versionString, ".");
			if (buildString.equals("qualifier")) {
				buildString = "";
			}
			versionString = StringUtils.substringBeforeLast(versionString, ".");
		}
		createVersionFile(versionString);
		
		if (buildString.equals("")) {
			logger.info("openHAB runtime has been started (v{}).",
					versionString);
		} else {
			logger.info("openHAB runtime has been started (v{}, build {}).",
					versionString, buildString);
		}

		if(logger.isDebugEnabled()) {
			RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
			long uptime = rb.getUptime();
			logger.debug("Startup took {} ms", uptime);
		}
		
		java.util.logging.Logger rootLogger = java.util.logging.LogManager
				.getLogManager().getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		for (Handler handler : handlers) {
			rootLogger.removeHandler(handler);
		}

		SLF4JBridgeHandler.install();

		startRuleEngine(context);
	}

	private void startRuleEngine(BundleContext context) throws InterruptedException {
		// TODO: This is a workaround as long as we cannot determine the time when all models have been loaded
		Thread.sleep(2000);
		
		// we now request the RuleEngine, so that it is activated and starts processing the rules
		// TODO: This should probably better be moved in a new bundle, so that the core bundle does
		// not have a dependency on model.rule.runtime anymore.
		ServiceTracker<RuleEngine, RuleEngine> tracker = new ServiceTracker<RuleEngine, RuleEngine>(context, RuleEngine.class, null);
		tracker.open();
		tracker.waitForService(10000);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		logger.info("openHAB runtime has been terminated.");
	}

	/**
	 * Creates a unified unique id and writes it to the
	 * <code>webapps/static</code> directory. An existing <code>uuid</code> file
	 * won't be overwritten.
	 */
	private String createUUIDFile() {
		File file = new File(getUserDataDir() + File.separator + STATIC_CONTENT_DIR + File.separator
				+ UUID_FILE_NAME);
		String uuidString = "";

		if (!file.exists()) {
			uuidString = UUID.randomUUID().toString();
			writeFile(file, uuidString);
		} else {
			uuidString = readFirstLine(file);
			logger.debug("UUID file already exists at '{}' with content '{}'",
					file.getAbsolutePath(), uuidString);
		}

		return uuidString;
	}

	/**
	 * Creates a file with given <code>version</code>. The file will be
	 * overwritten each time openHAB has been started.
	 * 
	 * @param version
	 *            the version number to write to the version file
	 */
	private void createVersionFile(String version) {
		File file = new File(getUserDataDir() + File.separator + STATIC_CONTENT_DIR + File.separator
				+ VERSION_FILE_NAME);
		writeFile(file, version);
	}

	private void writeFile(File file, String content) {
		// create intermediary directories
		file.getParentFile().mkdirs();
		try {
			IOUtils.write(content, new FileOutputStream(file));
			logger.debug("Created file '{}' with content '{}'",
					file.getAbsolutePath(), content);
		} catch (FileNotFoundException e) {
			logger.error("Couldn't create file '" + file.getPath() + "'.", e);
		} catch (IOException e) {
			logger.error("Couldn't write to file '" + file.getPath() + "'.", e);
		}
	}

	private String readFirstLine(File file) {
		List<String> lines = null;
		try {
			lines = IOUtils.readLines(new FileInputStream(file));
		} catch (IOException ioe) {
			// no exception handling - we just return the empty String
		}
		return lines != null && lines.size() > 0 ? lines.get(0) : "";
	}

	private String getUserDataDir() {
		String progArg = System.getProperty(ConfigConstants.USERDATA_DIR_PROG_ARGUMENT);
		if (progArg != null) {
			return progArg;
		} else {
			return "userdata";
		}
		
	}
}
