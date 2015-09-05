/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.multimedia.sonos.internal.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.io.multimedia.sonos.internal.MultimediaSonosActivator;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.TextToSpeechCache;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.TextToSpeechService;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.internal.TTSCacheImpl;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers a servlet that serves audio content.
 * 
 * @author Jochen Hiller - Initial contribution
 *
 */
public class AudioServlet extends HttpServlet {

	private static final long serialVersionUID = -7345087931950359183L;

	private static final Logger logger = LoggerFactory
			.getLogger(AudioServlet.class);

	private static final String SERVLET_NAME = "/audio";

	private static final String MESSAGE_URL_NOT_SUPPORTED = "Url not supported";

	private static final String MESSAGE_TEXT_PARAMETER_MISSING = "text parameter is required";

	private static final String MESSAGE_AUDIO_NOT_FOUND = "Audio resource not found";

	private static final String MESSAGE_AUDIO_CONTENT_WRONG = "Audio content must be valid mp3 resource";

	protected HttpService httpService;
	protected TextToSpeechCache cache;

	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	public void unsetHttpService(HttpService httpService) {
		this.httpService = null;
	}

	protected void activate() {
		try {
			logger.debug("Starting up audio servlet at " + SERVLET_NAME);

			Hashtable<String, String> props = new Hashtable<String, String>();
			httpService.registerServlet(SERVLET_NAME, this, props,
					createHttpContext());

			final String cacheFolderName = MultimediaSonosActivator
					.getCacheFolder();
			this.cache = new TTSCacheImpl(new File(cacheFolderName));
		} catch (NamespaceException e) {
			logger.error("Error during servlet startup", e);
		} catch (ServletException e) {
			logger.error("Error during servlet startup", e);
		}
	}

	/**
	 * Creates a {@link HttpContext}
	 * 
	 * @return a {@link HttpContext}
	 */
	protected HttpContext createHttpContext() {
		HttpContext defaultHttpContext = httpService.createDefaultHttpContext();
		return defaultHttpContext;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("Processing URL {}", req.getRequestURI());
		if (req.getRequestURI().startsWith("/audio/content")) {
			processAudioContent(req, resp);
		} else if (req.getRequestURI().startsWith("/audio/tts")) {
			processTTS(req, resp);
		} else {
			// Neither /audio/content nor /audio/tts
			resp.sendError(HttpServletResponse.SC_NOT_FOUND,
					MESSAGE_URL_NOT_SUPPORTED);
		}
	}

	// private methods

	/**
	 * Will get audio from given part of URL.
	 */
	protected void processAudioContent(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		// TODO handle security, only host correct URLs
		String uniqueName = StringUtils.substringAfterLast(req.getRequestURI(),
				"/");
		if (!uniqueName.endsWith(".mp3")) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					MESSAGE_AUDIO_CONTENT_WRONG);
		}
		// get from cache
		File mp3 = cache.textToSpeech(uniqueName);
		copyFileToResponse(resp, mp3);
	}

	protected void processTTS(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		final String text = req.getParameter("text");
		if (text == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					MESSAGE_TEXT_PARAMETER_MISSING);
			return;
		}
		String language = req.getParameter("language");
		if (language == null) {
			// EN by default
			language = TextToSpeechService.LANGUAGE_ENGLISH;
			// TODO check for unsupported languages
		}
		File fileToStream = cache.textToSpeech(text, language);

		copyFileToResponse(resp, fileToStream);
	}

	private void copyFileToResponse(HttpServletResponse resp, File fileToStream)
			throws IOException, FileNotFoundException {
		// return file as http response as MP3 media
		if (fileToStream != null) {
			resp.setContentType("audio/mpeg");
			long size = fileToStream.length();
			resp.setContentLength((int) size);

			ServletOutputStream os = resp.getOutputStream();
			InputStream is = new FileInputStream(fileToStream);
			IOUtils.copy(is, os);
			resp.flushBuffer();
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND,
					MESSAGE_AUDIO_NOT_FOUND + "(" + fileToStream + ")");
		}
	}
}
