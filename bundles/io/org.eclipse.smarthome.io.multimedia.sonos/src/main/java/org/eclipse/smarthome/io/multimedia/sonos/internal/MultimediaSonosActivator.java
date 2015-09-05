/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.multimedia.sonos.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.TextToSpeechCache;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.internal.TTSCacheImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the default OSGi bundle activator
 * 
 * @author Jochen Hiller - Initial contribution and API
 */
public class MultimediaSonosActivator implements BundleActivator {

	private static Logger logger = LoggerFactory
			.getLogger(MultimediaSonosActivator.class);

	private static BundleContext context;

	private final static String CACHE_FOLDER_NAME = "tts_cache";
	private static String fullCacheFolderName;

	/**
	 * Called whenever the OSGi framework starts our bundle
	 */
	public void start(BundleContext bc) throws Exception {
		logger.debug("Multimedia I/O TTS GoogleTranslate bundle has been started.");
		context = bc;
		fullCacheFolderName = initCacheFolder();
		prefillCacheFolder();
	}

	/**
	 * Called whenever the OSGi framework stops our bundle
	 */
	public void stop(BundleContext bc) throws Exception {
		fullCacheFolderName = null;
		context = null;
		logger.debug("Multimedia I/O TTS GoogleTranslate bundle has been stopped.");
	}

	/**
	 * Returns the bundle context of this bundle
	 * 
	 * @return the bundle context
	 */
	public static BundleContext getContext() {
		return context;
	}

	public static synchronized String getCacheFolder() {
		if (fullCacheFolderName == null) {
			fullCacheFolderName = initCacheFolder();
		}
		return fullCacheFolderName;
	}

	// private methods

	private static String initCacheFolder() {
		String userDataDir = System
				.getProperty(ConfigConstants.USERDATA_DIR_PROG_ARGUMENT);
		if (userDataDir == null) {
			// use current folder as default
			userDataDir = ".";
		}
		String folderName = userDataDir + File.separator + CACHE_FOLDER_NAME;
		File folder = new File(folderName);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folderName;
	}

	private void prefillCacheFolder() {
		TextToSpeechCache cache = new TTSCacheImpl(
				new File(fullCacheFolderName));
		String[] langs = cache.supportedLanguages();
		for (int i = 0; i < langs.length; i++) {
			String language = langs[i];
			File prefillFile = new File(fullCacheFolderName + File.separator
					+ "prefill" + File.separator + language + ".prefill");
			if (prefillFile.exists()) {
				try {
					List<String> listOfTexts = FileUtils.readLines(prefillFile);
					cache.fillCache(listOfTexts, language);
				} catch (IOException ex) {
					logger.error("Could not read prefill properties");
				}
			}
		}
	}
}
