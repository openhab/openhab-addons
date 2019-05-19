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
package org.openhab.binding.verisure.internal;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
import org.openhab.binding.verisure.internal.model.VerisureAlarmJSON;
import org.openhab.binding.verisure.internal.model.VerisureBroadbandConnectionJSON;
import org.openhab.binding.verisure.internal.model.VerisureClimateBaseJSON;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindowJSON;
import org.openhab.binding.verisure.internal.model.VerisureInstallationsJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartLockJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;
import org.openhab.binding.verisure.internal.model.VerisureUserPresenceJSON;
import org.openhab.binding.verisure.internal.model.VerisureInstallationsJSON.Owainstallation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class performs the communication with Verisure My Pages.
 *
 * @author Jarle Hjortland - Initial contribution
 * @author Jan Gustafsson - Re-design and support for several sites and update to new Verisure API
 *
 */
@NonNullByDefault
public class VerisureSession {

	@NonNullByDefault
	private final class VerisureInstallation {
		private @Nullable String installationName;
		private @Nullable BigDecimal installationId;
		
		public VerisureInstallation() {
		}

		public VerisureInstallation(@Nullable String installationName, @Nullable BigDecimal installationId) {
			this.installationName = installationName;
			this.installationId = installationId;
		}

		public @Nullable BigDecimal getInstallationId() {
			return installationId;
		}

		public @Nullable String getInstallationName() {
			return installationName;
		}

		public void setInstallationId(@Nullable BigDecimal installationId) {
			this.installationId = installationId;
		}
		
		public void setInstallationName(@Nullable String installationName) {
			this.installationName = installationName;
		}
	}

	private final HashMap<String, org.openhab.binding.verisure.internal.model.VerisureThingJSON> verisureThings = new HashMap<String, org.openhab.binding.verisure.internal.model.VerisureThingJSON>();
	private final Logger logger = LoggerFactory.getLogger(VerisureSession.class);
	private final Gson gson = new GsonBuilder().create();
	private final List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();
	private final Hashtable<@Nullable String, @Nullable VerisureInstallation> verisureInstallations = new Hashtable<@Nullable String, @Nullable VerisureInstallation>();

	private boolean areWeLoggedOut = true;
	private @Nullable String authstring;
	private @Nullable String csrf;
	private @Nullable BigDecimal pinCode;
	private HttpClient httpClient;
	private String userName = "jannegpriv@gmail.com";
	private String passwordName = "vid";
	private String password = "";

	public VerisureSession(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void initialize(@Nullable String authstring, @Nullable BigDecimal pinCode) {
		logger.debug("VerisureSession:initialize");
		if (authstring != null) {
			this.authstring = authstring.substring(0);
			this.pinCode = pinCode;
			// Try to login to Verisure
			if (logIn()) {
				setInstallations();
			} else {
				logger.warn("Failed to login to Verisure!");
			}
		}
	}

	public boolean refresh() {
		if (!areWeLoggedOut && areWeLoggedIn()) {
			updateStatus();
			return true;
		} else {
			if (logIn()) {
				updateStatus();
				areWeLoggedOut = false;
				return true;
			} else {
				areWeLoggedOut = true;
				return false;
			}
		}
	}

	public boolean sendCommand(String installationName, String url, String data) {
		logger.debug("Sending command with URL {} and data {} for installation {}", url, data, installationName);
		VerisureInstallation verisureInstallation = verisureInstallations.get(installationName);
		if (verisureInstallation != null) {
			BigDecimal instId = verisureInstallation.getInstallationId();
			configureInstallationInstance(instId);
			sendHTTPpost(url, data);
			return true;
		}
		return false;
	}
	
	public boolean sendCommand2(String url, String data) {
		logger.debug("Sending command with URL {} and data {}", url, data);
	
		sendHTTPpost2(AUTH_LOGIN, "empty");
		sendHTTPpost2(url, data);
		return true;
		
	}
	
	public boolean sendCommand3(BigDecimal installationId, String url, String data) {
		// Get CSRF from settings

		logger.debug("Sending command with URL {} and data {}", url, data);

		// sendHTTPpost2(AUTH_LOGIN, "empty");
		sendHTTPpost2(url, data);

		return true;

	}

	public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
		logger.debug("unregisterDeviceStatusListener for listener {}", deviceStatusListener);
		return deviceStatusListeners.remove(deviceStatusListener);
	}

	public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
		logger.debug("registerDeviceStatusListener for listener {}", deviceStatusListener);
		return deviceStatusListeners.add(deviceStatusListener);
	}

	public void dispose() {
	}

	public @Nullable VerisureThingJSON getVerisureThing(String key) {
		return verisureThings.get(key);
	}

	public HashMap<String, org.openhab.binding.verisure.internal.model.VerisureThingJSON> getVerisureThings() {
		return verisureThings;
	}

	public @Nullable String getCsrf() {
		return csrf;
	}

	public @Nullable BigDecimal getPinCode() {
		return pinCode;
	}

	private boolean areWeLoggedIn() {
		logger.debug("areWeLoggedIn() - Checking if we are logged in");
		String url = STATUS;
		try {
			ContentResponse response = httpClient.newRequest(url).method(HttpMethod.HEAD).send();
			logger.debug("HTTP HEAD response: " + response.getContentAsString());
			switch (response.getStatus()) {
			case 200:
				// OK
				logger.debug("Status code 200. Probably logged in");
				return true;
				//return getHtmlPageType().contains("start-page");
			case 302:
				// Redirection
				logger.debug("Status code 302. Redirected. Probably not logged in");
				return false;
			case 404:
				// not found
				logger.debug("Status code 404. Probably logged on too");
				//return getHtmlPageType().contains("start-page");
				return false;
			default:
				logger.info("Status code {} body {}", response.getStatus(), response.getContentAsString());
				break;
			}
		} catch (ExecutionException e) {
			logger.warn("ExecutionException: {}", e);
		} catch (InterruptedException e) {
			logger.warn("InterruptedException: {}", e);
		} catch (TimeoutException e) {
			logger.warn("TimeoutException: {}", e);
		}
		return false;
	}

	private @Nullable <T> T callJSONRest(String url, Class<T> jsonClass) {
		T result = null;
		logger.debug("HTTP GET: " + BASEURL + url);
		try {
			ContentResponse httpResult = httpClient.GET(BASEURL + url + "?_=" + System.currentTimeMillis());
			logger.debug("HTTP Response ({}) Body:{}", httpResult.getStatus(),
					httpResult.getContentAsString().replaceAll("\n+", "\n"));
			if (httpResult.getStatus() == HttpStatus.OK_200) {
				result = gson.fromJson(httpResult.getContentAsString(), jsonClass);
			}
			return result;
		} catch (ExecutionException e) {
			logger.warn("Caught ExecutionException {} for URL string {}", e, url);
		} catch (InterruptedException e) {
			logger.warn("Caught InterruptedException {} for URL string {}", e, url);
		} catch (TimeoutException e) {
			logger.warn("Caught TimeoutException {} for URL string {}", e, url);
		}
		return null;
	}
	
	private @Nullable <T> T callJSONRestPost(String url, String data, Class<T> jsonClass) {
		T result = null;
		
		logger.debug("HTTP PUT: " + url);
				
		if (data != null) {
			try {
				logger.debug("sendHTTPpost URL: {} Data:{}", url, data);
				org.eclipse.jetty.client.api.Request request = httpClient.newRequest(url).method(HttpMethod.POST);
				request.header("content-type", "application/json");
				if (!data.equals("empty")) {
					request.content(new BytesContentProvider(data.getBytes("UTF-8")),
							"application/x-www-form-urlencoded; charset=UTF-8");
				} else {
					request.cookie(new HttpCookie("username", userName));
					request.cookie(new HttpCookie("vid", password));
				}
				logger.debug("HTTP POST Request {}.", request.toString());
				ContentResponse response = request.send();
				String content = response.getContentAsString();
				//String contentUTF8 = new String(content.getBytes("UTF-8"), "ISO-8859-1");
				String contentChomped = StringUtils.chomp(content);
				logger.debug("HTTP Response ({}) Body:{}", response.getStatus(), contentChomped);
				if (response.getStatus() == HttpStatus.OK_200) {	
					result = gson.fromJson(contentChomped, jsonClass);
				}
				return result;
			} catch (ExecutionException e) {
				logger.warn("Caught ExecutionException {}", e);
			} catch (UnsupportedEncodingException e) {
				logger.warn("Caught UnsupportedEncodingException {}", e);
			} catch (InterruptedException e) {
				logger.warn("Caught InterruptedException {}", e);
			} catch (TimeoutException e) {
				logger.warn("Caught TimeoutException {}", e);
			} catch (RuntimeException e) {
				logger.warn("Caught RuntimeException {}", e);
			}
		}
		return null;
	}

	public void configureInstallationInstance(@Nullable BigDecimal installationId) {
		logger.debug("Attempting to fetch CSRF and configure installation instance");
		try {
			String csrf = getCsrfToken(installationId);
			logger.debug("Got CSRF: {}", csrf);
			// Set installation
			String url = SET_INSTALLATION + installationId.toString();
			logger.debug("Set installation URL: " + url);
			httpClient.GET(url);			
		} catch (ExecutionException e) {
			logger.warn("Caught ExecutionException {}", e);
		} catch (InterruptedException e) {
			logger.warn("Caught InterruptedException {}", e);
		} catch (TimeoutException e) {
			logger.warn("Caught TimeoutException {}", e);
		}
	}

	private void setInstallations() {
		logger.debug("Attempting to get all installations");

		// URL to set status which will give us 2 cookies with username and password used for the session
		String url = STATUS;
		
		try {
			ContentResponse response = httpClient.GET(url);
			logger.trace("HTTP Response ({}) Body:{}", response.getStatus(),
					response.getContentAsString().replaceAll("\n+", "\n"));
			CookieStore c = httpClient.getCookieStore();
			List<HttpCookie> cookies = c.get(URI.create("http://verisure.com"));
			Iterator<HttpCookie> cookiesIterator = cookies.iterator();
			while (cookiesIterator.hasNext()) {
				HttpCookie theCookie = cookiesIterator.next();
				logger.debug("Cookie: ", theCookie.toString());
				if (theCookie.getName().equals(passwordName)) {
					password = theCookie.getValue();
				}
			}
		} catch (ExecutionException e) {
			logger.warn("ExecutionException: {}", e);
		} catch (InterruptedException e) {
			logger.warn("InterruptedException: {}", e);
		} catch (TimeoutException e) {
			logger.warn("TimeoutException: {}", e);
		}
		
		url = AUTH_LOGIN;
		String source = sendHTTPpost2(url, "empty");
		if (source == null) {
			logger.warn("Failed to get installation instances");
		} else {
			logger.debug("Login result: {}" + source);
		}
		
		url = START_GRAPHQL;
		
		String queryQLAccountInstallations = "[{\"operationName\":\"AccountInstallations\",\"variables\":{\"email\":\"jannegpriv@gmail.com\"},\"query\":\"query AccountInstallations($email: String!) {\\n  account(email: $email) {\\n    owainstallations {\\n      giid\\n      alias\\n      type\\n      subsidiary\\n      dealerId\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}]";
		Class<VerisureInstallationsJSON> jsonClass = VerisureInstallationsJSON.class;
		VerisureInstallationsJSON installations =  callJSONRestPost(START_GRAPHQL, queryQLAccountInstallations, jsonClass);
		
		if (installations == null) {
			logger.debug("Failed to get installations");
		} else {
			logger.debug("Installation: {}" + installations.toString());
			List<Owainstallation> owaInstList = installations.getData().getAccount().getOwainstallations();
			for (Owainstallation owaInst : owaInstList) {
				VerisureInstallation vInst = new VerisureInstallation();
				if (owaInst.getAlias() != null && owaInst.getGiid() != null) {
					vInst.setInstallationId(new BigDecimal(owaInst.getGiid()));
					vInst.setInstallationName(owaInst.getAlias());
					verisureInstallations.put(owaInst.getAlias(), vInst);
				} else {
					logger.warn("Failed to get alias and/or giid");
				}
			}
		}
	}

	public @Nullable String getCsrfToken(@Nullable BigDecimal installationId) {
//		Document htmlDocument = Jsoup.parse(htmlText);
//		Element nameInput = htmlDocument.select("input[name=_csrf]").first();
//		return nameInput.attr("value");
		String subString = null;
		String source = null;
		String url = SETTINGS + installationId.toString();
		logger.debug("Settings URL: " + url);
		try {
			ContentResponse resp = httpClient.GET(url);
			source = resp.getContentAsString();
			logger.trace("{} html: {}", url, source);
		} catch (ExecutionException e) {
			logger.warn("Caught ExecutionException {}", e);
		} catch (InterruptedException e) {
			logger.warn("Caught InterruptedException {}", e);
		} catch (TimeoutException e) {
			logger.warn("Caught TimeoutException {}", e);
		}

		try {
			int labelIndex = source.indexOf("_csrf\" value=");
			subString = source.substring(labelIndex + 14, labelIndex + 78);
			logger.debug("csrf-token: {}", subString);
		} catch (IndexOutOfBoundsException e) {
			logger.debug("QA test", "Parsing Error = {}", e.toString(), e);
		}
		return subString;
	}

	private synchronized boolean logIn() {
		logger.debug("Attempting to log in to mypages.verisure.com");
		//String pagetype = getHtmlPageType();
		String url = LOGON_SUF;
		logger.debug("Login URL: {}", url);
		String source = sendHTTPpost(url, authstring);
		if (source == null) {
			logger.debug("Failed to login");
			return false;
		} else {
			logger.debug("Login result: {}" + source);
			return true;
		}
	}

	private void notifyListeners(VerisureThingJSON thing) {
		for (DeviceStatusListener listener : deviceStatusListeners) {
			listener.onDeviceStateChanged(thing);
		}
	}

	@Nullable
	private String sendHTTPpost(String urlString, @Nullable String data) {
		if (data != null) {
			try {
				logger.debug("sendHTTPpost URL: {} Data:{}", urlString, data);
				org.eclipse.jetty.client.api.Request request = httpClient.newRequest(urlString).method(HttpMethod.POST);
				request.header("x-csrf-token", csrf).header("Accept", "application/json");
				request.content(new BytesContentProvider(data.getBytes("UTF-8")),
						"application/x-www-form-urlencoded; charset=UTF-8");
				logger.debug("HTTP POST Request {} Headers {}.", request.toString(), request.getHeaders().toString());
				ContentResponse response = request.send();
				String content = response.getContentAsString();
				String contentUTF8 = new String(content.getBytes("UTF-8"), "ISO-8859-1");
				logger.debug("HTTP Response ({}) Body:{}", response.getStatus(), contentUTF8);
				return contentUTF8;
			} catch (ExecutionException e) {
				logger.warn("Caught ExecutionException {}", e);
			} catch (UnsupportedEncodingException e) {
				logger.warn("Caught UnsupportedEncodingException {}", e);
			} catch (InterruptedException e) {
				logger.warn("Caught InterruptedException {}", e);
			} catch (TimeoutException e) {
				logger.warn("Caught TimeoutException {}", e);
			}
		}
		return null;
	}

	@Nullable
	public String sendHTTPpost2(String urlString, @Nullable String data) {
		if (data != null) {
			try {
				logger.debug("sendHTTPpost2 URL: {} Data:{}", urlString, data);
				org.eclipse.jetty.client.api.Request request = httpClient.newRequest(urlString).method(HttpMethod.POST);
				request.header("X-CSRF-TOKEN", csrf).header("Accept", "application/json");
				if (!data.equals("empty")) {
					request.content(new BytesContentProvider(data.getBytes("UTF-8")),
							"application/x-www-form-urlencoded; charset=UTF-8");
				} else {
					request.cookie(new HttpCookie("username", userName));
					request.cookie(new HttpCookie("vid", password));
				}
				logger.debug("HTTP POST Request {}.", request.toString());
				ContentResponse response = request.send();
				String content = response.getContentAsString();
				String contentUTF8 = new String(content.getBytes("UTF-8"), "ISO-8859-1");
				logger.debug("HTTP Response ({}) Body:{}", response.getStatus(), contentUTF8);
				return contentUTF8;
			} catch (ExecutionException e) {
				logger.warn("Caught ExecutionException {}", e);
			} catch (UnsupportedEncodingException e) {
				logger.warn("Caught UnsupportedEncodingException {}", e);
			} catch (InterruptedException e) {
				logger.warn("Caught InterruptedException {}", e);
			} catch (TimeoutException e) {
				logger.warn("Caught TimeoutException {}", e);
			}
		}
		return null;
	}

	private void updateStatus() {
		logger.debug("VerisureSession:updateStatus");
		
		VerisureInstallation vInst = null;
		for (Enumeration<@Nullable VerisureInstallation> num = verisureInstallations.elements(); num
				.hasMoreElements();) {
			vInst = num.nextElement();
			if (vInst != null) {
				configureInstallationInstance(vInst.getInstallationId());
				updateVerisureThings(ALARMSTATUS_PATH, VerisureAlarmJSON[].class, vInst);
				updateVerisureThings(CLIMATEDEVICE_PATH, VerisureClimateBaseJSON[].class, vInst);
				updateVerisureThings(DOORWINDOW_PATH, VerisureDoorWindowJSON[].class, vInst);
				updateVerisureThings(USERTRACKING_PATH, VerisureUserPresenceJSON[].class, vInst);
				updateVerisureThings(SMARTPLUG_PATH, VerisureSmartPlugJSON[].class, vInst);
				updateVerisureBroadbandStatus(ETHERNETSTATUS_PATH, VerisureBroadbandConnectionJSON.class, vInst);
			}
		}
	
	}

	private synchronized void updateVerisureBroadbandStatus(String urlString,
			Class<? extends VerisureThingJSON> jsonClass, VerisureInstallation verisureInstallation) {
		VerisureThingJSON thing = callJSONRest(urlString, jsonClass);
		logger.debug("REST Response ({})", thing);
		if (thing != null) {
			BigDecimal instId = verisureInstallation.getInstallationId();
			thing.setDeviceId(instId.toString());
			VerisureThingJSON oldObj = verisureThings.get(thing.getDeviceId());
			if (oldObj == null || !oldObj.equals(thing)) {
				thing.setSiteId(verisureInstallation.getInstallationId());
				thing.setSiteName(verisureInstallation.getInstallationName());
				String deviceId = thing.getDeviceId();
				if (deviceId != null) {
					verisureThings.put(deviceId, thing);
					notifyListeners(thing);
				}
			}
		}
	}

	private synchronized void updateVerisureThings(String urlString, Class<? extends VerisureThingJSON[]> jsonClass,
			@Nullable VerisureInstallation inst) {
		if (inst != null) {
			VerisureThingJSON[] things = callJSONRest(urlString, jsonClass);
			logger.debug("REST Response ({})", (Object[]) things);
			if (things != null) {
				for (VerisureThingJSON thing : things) {
					BigDecimal instId = inst.getInstallationId();
					if (thing instanceof VerisureUserPresenceJSON) {
						String deviceId = ((VerisureUserPresenceJSON) thing).getWebAccount()
								+ instId.toString();
						thing.setDeviceId(deviceId.replaceAll("[^a-zA-Z0-9]+", ""));
					} else if (thing instanceof VerisureAlarmJSON) {
						String type = ((VerisureAlarmJSON) thing).getType();
						if ("ARM_STATE".equals(type)) {
							thing.setDeviceId(instId.toString());
						} else if ("DOOR_LOCK".equals(type)) {
							// Then we know it is a SmartLock, lets get some more info on SmartLock Status
							thing = updateSmartLockThing((VerisureAlarmJSON) thing, type);
						} else {
							logger.warn("Unknown alarm/lock type {}.", type);
						}
					} else {
						String deviceId = thing.getDeviceId();
						if (deviceId != null) {
							thing.setDeviceId(deviceId.replaceAll("[^a-zA-Z0-9]+", ""));
						}
					}
					VerisureThingJSON oldObj = verisureThings.get(thing.getDeviceId());
					if (oldObj == null || !oldObj.equals(thing)) {
						thing.setSiteId(inst.getInstallationId());
						thing.setSiteName(inst.getInstallationName());
						String deviceId = thing.getDeviceId();
						if (deviceId != null) {
							verisureThings.put(deviceId, thing);
							notifyListeners(thing);
						}
					}
				}
			}
		}
	}

	private VerisureSmartLockJSON updateSmartLockThing(VerisureAlarmJSON thing, @Nullable String type) {
		VerisureSmartLockJSON smartLockThing = callJSONRest(SMARTLOCK_PATH + thing.getDeviceId(),
				VerisureSmartLockJSON.class);
		logger.debug("REST Response ({})", smartLockThing);
		if (smartLockThing == null) {
			// Fix if doorlock query gives empty JSON
			smartLockThing = new VerisureSmartLockJSON();
			String deviceId = thing.getDeviceId();
			// In original JSON the Device Id lacks a space after first 4 characters,
			// hence we insert a space at position 4
			if (deviceId != null) {
				StringBuilder sb = new StringBuilder(deviceId);
				sb.insert(4, " ");
				smartLockThing.setDeviceId(sb.toString());
			}
		}
		String date = thing.getDate();
		if (date != null) {
			smartLockThing.setDate(date);
		}
		String notAllowedReason = thing.getNotAllowedReason();
		if (notAllowedReason != null) {
			smartLockThing.setNotAllowedReason(notAllowedReason);
		}
		Boolean changeAllowed = thing.getChangeAllowed();
		if (changeAllowed != null) {
			smartLockThing.setChangeAllowed(changeAllowed);
		}
		String label = thing.getLabel();
		if (label != null) {
			smartLockThing.setLabel(label);
		}
		if (type != null) {
			smartLockThing.setType(type);
		}
		String name = thing.getName();
		if (name != null) {
			smartLockThing.setName(name);
		}
		String location = thing.getLocation();
		if (location != null) {
			smartLockThing.setLocation(location);
		}
		String status = thing.getStatus();
		if (status != null) {
			smartLockThing.setStatus(status);
		}
		String deviceId = smartLockThing.getDeviceId();
		if (deviceId != null) {
			smartLockThing.setDeviceId(deviceId.replaceAll("[^a-zA-Z0-9]+", ""));
		}
		return smartLockThing;
	}
}
