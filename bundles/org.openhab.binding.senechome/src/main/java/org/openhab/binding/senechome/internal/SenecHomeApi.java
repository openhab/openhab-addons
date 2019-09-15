/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.senechome.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.senechome.internal.json.SenecHomeResponse;

import com.google.gson.Gson;

public class SenecHomeApi {
	private static final int HTTP_OK_CODE = 200;
	private static final String HTTP_PROTO_PREFIX = "http://";
	
	private String hostname;
	private HttpClient httpClient;
	private Gson gson;
	
	public SenecHomeApi(HttpClient httpClient, Gson gson, SenecHomeConfiguration config) {
		this.httpClient = httpClient;
		this.gson = gson;
		this.hostname = config.hostname;
	}

	/**
     * POST json with empty, but expected fields, to lala.cgi of Senec webinterface
     * the response will contain the same fields, but with the corresponding values
     *
     * To receive new values, just modify the Json objects and add them to the thing channels
     *
     * @param hostname Hostname or ip address of senec battery
     * @return Instance of SenecHomeResponse
     * @throws MalformedURLException Configuration/URL is wrong
     * @throws IOException           Communication failed
     */
	public SenecHomeResponse getStatistics() throws InterruptedException, TimeoutException, ExecutionException, IOException {
		String location = HTTP_PROTO_PREFIX + hostname;
		
		Request request = httpClient.newRequest(location);
		request.header(HttpHeader.ACCEPT, MimeTypes.Type.APPLICATION_JSON.asString());
		request.header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.FORM_ENCODED.asString());
		ContentResponse response = request.method(HttpMethod.POST)
				.content(new StringContentProvider(gson.toJson(new SenecHomeResponse())))
				.send();
		
		if (response.getStatus() == HTTP_OK_CODE) {
			return gson.fromJson(response.getContentAsString(), SenecHomeResponse.class);
		} else {
			throw new IOException("Got unexpected response code "+response.getStatus());
		}
	}
}