/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal.utils;

import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Collection of methods to help retrieve HTTP data from a SqueezeServer
 * 
 * @author Dan Cunningham
 *
 */
public class HttpUtils {
	private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	private static int TIMEOUT = 5000;
	private static HttpClient client = new HttpClient();
	/**
	 * JSON request to get the CLI port from a Squeeze Server
	 */
	private static final String JSON_REQ = "{\"params\": [\"\", [\"pref\" ,\"plugin.cli:cliport\",\"?\"]], \"id\": 1, \"method\": \"slim.request\"}";

	/**
	 * Simple logic to perform a post request
	 * 
	 * @param url
	 * @param timeout
	 * @return
	 */
	public static String post(String url, String postData) throws Exception {
		PostMethod method = new PostMethod(url);
		method.setRequestBody(postData);
		method.getParams().setSoTimeout(TIMEOUT);
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("Method failed: {}", method.getStatusLine());
				throw new Exception("Method failed: " + method.getStatusLine());
			}
			byte[] responseBody = method.getResponseBody();
			return new String(responseBody);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Returns a byte array from a URL string
	 * 
	 * @param urlString
	 * @return byte array of data
	 */
	public static byte[] getData(String urlString) throws Exception {
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();
		return IOUtils.toByteArray(connection.getInputStream());
	}

	/**
	 * Retrieves the command line port (cli) from a SqueezeServer
	 * 
	 * @param ip
	 * @param webPort
	 * @return
	 * @throws Exception
	 */
	public static int getCliPort(String ip, int webPort) throws Exception {
		String url = "http://" + ip + ":" + webPort + "/jsonrpc.js";
		String json = HttpUtils.post(url, JSON_REQ);
		logger.trace("Recieved json from server {}", json);
		JsonElement resp = new JsonParser().parse(json);
		String cliPort = resp.getAsJsonObject().get("result").getAsJsonObject()
				.get("_p2").getAsString();
		return Integer.parseInt(cliPort);
	}

}
