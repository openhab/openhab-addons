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
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openhab.binding.verisure.internal.model.VerisureAlarmsJSON;
import org.openhab.binding.verisure.internal.model.VerisureBroadbandConnectionsJSON;
import org.openhab.binding.verisure.internal.model.VerisureClimatesJSON;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindowsJSON;
import org.openhab.binding.verisure.internal.model.VerisureInstallationsJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartLockJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartLocksJSON;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugsJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;
import org.openhab.binding.verisure.internal.model.VerisureUserPresencesJSON;
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
		private @Nullable BigDecimal pinCode;
		
		public @Nullable BigDecimal getPinCode() {
			return pinCode;
		}

		public void setPinCode(@Nullable BigDecimal pinCode) {
			this.pinCode = pinCode;
		}

		public VerisureInstallation() {
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

	private final HashMap<String, VerisureThingJSON> verisureThings = new HashMap<String, VerisureThingJSON>();
	private final Logger logger = LoggerFactory.getLogger(VerisureSession.class);
	private final Gson gson = new GsonBuilder().create();
	private final List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();
	private final Hashtable<@Nullable BigDecimal, @Nullable VerisureInstallation> verisureInstallations = new Hashtable<@Nullable BigDecimal, @Nullable VerisureInstallation>();

	private boolean areWeLoggedOut = true;
	private @Nullable String authstring;
	private @Nullable String csrf;
	private @Nullable String pinCode;
	private HttpClient httpClient;
	private @Nullable String userName = "";
	private String passwordName = "vid";
	private @Nullable String password = "";

	public VerisureSession(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void initialize(@Nullable String authstring, @Nullable String pinCode, @Nullable String userName) {
		logger.debug("VerisureSession:initialize");
		if (authstring != null) {
			this.authstring = authstring.substring(0);
			this.pinCode = pinCode;
			this.userName = userName;
			// Try to login to Verisure
			if (logIn()) {
				getInstallations();
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
	
	public void sendCommand(String url, String data, BigDecimal installationId) {
		logger.debug("Sending command with URL {} and data {}", url, data);
		configureInstallationInstance(installationId);
		setSessionCookieAuthLogin();
		sendHTTPpost(url, data);
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

	public @Nullable String getPinCode() {
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
				CookieStore c = httpClient.getCookieStore();
				List<HttpCookie> cookies = c.get(URI.create("http://verisure.com"));
				Iterator<HttpCookie> cookiesIterator = cookies.iterator();
				while (cookiesIterator.hasNext()) {
					HttpCookie theCookie = cookiesIterator.next();
					logger.debug("Response Cookie: ", theCookie.toString());
					if (theCookie.getName().equals(passwordName)) {
						password = theCookie.getValue();
						logger.debug("Fetching vid {} from cookie", password);
					}
				}
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
		
		logger.debug("HTTP POST: " + url);
				
		if (data != null) {
			try {
				logger.debug("sendHTTPpost URL: {} Data:{}", url, data);
				org.eclipse.jetty.client.api.Request request = httpClient.newRequest(url).method(HttpMethod.POST);
				request.header("content-type", "application/json");
				if (!data.equals("empty")) {
					request.content(new BytesContentProvider(data.getBytes("UTF-8")),
							"application/x-www-form-urlencoded; charset=UTF-8");
				} else {
					logger.debug("Setting cookie with username {} and vid {}", userName, password);
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
	
	private void setSessionCookieAuthLogin() {
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
				logger.debug("Response Cookie: ", theCookie.toString());
				if (theCookie.getName().equals(passwordName)) {
					password = theCookie.getValue();
					logger.debug("Fetching vid {} from cookie", password);
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
		String source = sendHTTPpost(url, "empty");
		if (source == null) {
			logger.warn("Failed to get installation instances");
		} else {
			logger.debug("Login result: {}" + source);
		}
	}

	private void getInstallations() {
		logger.debug("Attempting to get all installations");

		setSessionCookieAuthLogin();
		String url = START_GRAPHQL;
		
		String queryQLAccountInstallations = "[{\"operationName\":\"AccountInstallations\",\"variables\":{\"email\":\"" + userName + "\"},\"query\":\"query AccountInstallations($email: String!) {\\n  account(email: $email) {\\n    owainstallations {\\n      giid\\n      alias\\n      type\\n      subsidiary\\n      dealerId\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}]";
		Class<VerisureInstallationsJSON> jsonClass = VerisureInstallationsJSON.class;
		VerisureInstallationsJSON installations =  callJSONRestPost(url, queryQLAccountInstallations, jsonClass);
		
		if (installations == null) {
			logger.debug("Failed to get installations");
		} else {
			logger.debug("Installation: {}" + installations.toString());
			List<Owainstallation> owaInstList = installations.getData().getAccount().getOwainstallations();
			List<String> pinCodes = Arrays.asList(pinCode.split(","));
			Boolean pinCodesMatchInstallations = true;
			if (owaInstList.size() != pinCodes.size()) {
				logger.debug("Number of installations {} does not match number of pin codes configured {}", owaInstList.size(), pinCodes.size());
				pinCodesMatchInstallations = false;
			}
			for (int i = 0; i < owaInstList.size(); i++) {
				VerisureInstallation vInst = new VerisureInstallation();
				if (owaInstList.get(i).getAlias() != null && owaInstList.get(i).getGiid() != null) {
					vInst.setInstallationId(new BigDecimal(owaInstList.get(i).getGiid()));
					vInst.setInstallationName(owaInstList.get(i).getAlias());
					if (pinCodesMatchInstallations) {
						vInst.setPinCode(new BigDecimal(pinCodes.get(i)));
						logger.debug("Setting pincode {} to installation ID {}", pinCodes.get(i), owaInstList.get(i).getGiid());
					} else {
						vInst.setPinCode(new BigDecimal(pinCodes.get(0)));
						logger.debug("Setting pincode {} to installation ID {}", pinCodes.get(0), owaInstList.get(i).getGiid());
					}
					verisureInstallations.put(new BigDecimal(owaInstList.get(i).getGiid()), vInst);
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
	
	public @Nullable BigDecimal getPinCode(@Nullable BigDecimal installationId) {
		return verisureInstallations.get(installationId).getPinCode();
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
				logger.debug("sendHTTPpost2 URL: {} Data:{}", urlString, data);
				org.eclipse.jetty.client.api.Request request = httpClient.newRequest(urlString).method(HttpMethod.POST);
				request.header("X-CSRF-TOKEN", csrf).header("Accept", "application/json");
				if (!data.equals("empty")) {
					request.content(new BytesContentProvider(data.getBytes("UTF-8")),
							"application/x-www-form-urlencoded; charset=UTF-8");
				} else {
					logger.debug("Setting cookie with username {} and vid {}", userName, password);
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
				setSessionCookieAuthLogin();
				updateAlarmStatus(VerisureAlarmsJSON.class, vInst);
				updateSmartLockStatus(VerisureSmartLocksJSON.class, vInst);
				updateClimateStatus(VerisureClimatesJSON.class, vInst);
				updateDoorWindowStatus(VerisureDoorWindowsJSON.class, vInst);
				updateUserPresenceStatus(VerisureUserPresencesJSON.class, vInst);
				updateSmartPlugStatus(VerisureSmartPlugsJSON.class, vInst);
				updateBroadbandConnectionStatus(VerisureBroadbandConnectionsJSON.class, vInst);
			}
		}
	}
	
	private synchronized void updateAlarmStatus(Class<? extends VerisureThingJSON> jsonClass,
			@Nullable VerisureInstallation installation) {
		if (installation != null) {
			BigDecimal installationId = installation.getInstallationId();
			String url = START_GRAPHQL;

			String queryQLAlarmStatus = "[{\"operationName\":\"ArmState\",\"variables\":{\"giid\":\"" + installationId
					+ "\"},\"query\":\"query ArmState($giid: String!) {\\n  installation(giid: $giid) {\\n    armState {\\n      type\\n      statusType\\n      date\\n      name\\n      changedVia\\n      allowedForFirstLine\\n      allowed\\n      errorCodes {\\n        value\\n        message\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}]";
			logger.debug("Trying to get alarm status with URL {} and data {}", url, queryQLAlarmStatus);
			VerisureThingJSON thing = callJSONRestPost(url, queryQLAlarmStatus, jsonClass);
			logger.debug("REST Response ({})", thing);

			if (thing != null) {
				// Set unique deviceID
				String deviceId = "alarm" + installationId.toString();
				deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
				thing.setDeviceId(deviceId);
				VerisureThingJSON oldObj = verisureThings.get(thing.getDeviceId());
				if (oldObj == null || !oldObj.equals(thing)) {
					thing.setSiteId(installation.getInstallationId());
					thing.setSiteName(installation.getInstallationName());
					verisureThings.put(deviceId, thing);
					notifyListeners(thing);
				}
			}
		}
	}
	
	private synchronized void updateSmartLockStatus(Class<? extends VerisureThingJSON> jsonClass,
			@Nullable VerisureInstallation installation) {
		if (installation != null) {
			BigDecimal installationId = installation.getInstallationId();
			String url = START_GRAPHQL;

			String queryQLSmartLock = "[{\"operationName\":\"DoorLock\",\"variables\":{\"giid\":\"" + installationId
					+ "\"},\"query\":\"query DoorLock($giid: String!) {\\n  installation(giid: $giid) {\\n    doorlocks {\\n      device {\\n        deviceLabel\\n        area\\n        __typename\\n      }\\n      currentLockState\\n      eventTime\\n      secureModeActive\\n      motorJam\\n      userString\\n      method\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}]\n"
					+ "";
			logger.debug("Trying to get smart lock status with URL {} and data {}", url, queryQLSmartLock);
			VerisureSmartLocksJSON thing = (VerisureSmartLocksJSON) callJSONRestPost(url, queryQLSmartLock, jsonClass);
			logger.debug("REST Response ({})", thing);

			if (thing != null) {
				List<VerisureSmartLocksJSON.Doorlock> doorLockList = thing.getData().getInstallation().getDoorlocks();
				for (VerisureSmartLocksJSON.Doorlock doorLock : doorLockList) {
					VerisureSmartLocksJSON slThing = new VerisureSmartLocksJSON();
					VerisureSmartLocksJSON.Installation inst = new VerisureSmartLocksJSON.Installation();
					List<VerisureSmartLocksJSON.Doorlock> list = new ArrayList<VerisureSmartLocksJSON.Doorlock>();
					list.add(doorLock);
					inst.setDoorlocks(list);
					VerisureSmartLocksJSON.Data data = new VerisureSmartLocksJSON.Data();
					data.setInstallation(inst);
					slThing.setData(data);
					// Set unique deviceID
					String deviceId = doorLock.getDevice().getDeviceLabel();
					deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
					slThing.setDeviceId(deviceId);
					// Set location
					slThing.setLocation(doorLock.getDevice().getArea());
					// Fetch more info from old endpoint
					VerisureSmartLockJSON smartLockThing = callJSONRest(SMARTLOCK_PATH + deviceId,
							VerisureSmartLockJSON.class);
					logger.debug("REST Response ({})", smartLockThing);
					slThing.setSmartLockJSON(smartLockThing);
					VerisureThingJSON oldObj = verisureThings.get(deviceId);
					if (oldObj == null || !oldObj.equals(slThing)) {
						slThing.setSiteId(installation.getInstallationId());
						slThing.setSiteName(installation.getInstallationName());
						verisureThings.put(deviceId, slThing);
						notifyListeners(slThing);
					}

				}
			}
		}
	}
	
	private synchronized void updateSmartPlugStatus(Class<? extends VerisureThingJSON> jsonClass,
			@Nullable VerisureInstallation installation) {
		if (installation != null) {
			BigDecimal installationId = installation.getInstallationId();
			String url = START_GRAPHQL;

			String queryQLSmartPlug = "{\"operationName\":\"SmartPlug\",\"variables\":{\"giid\":\""+ installationId + "\"},\"query\":\"query SmartPlug($giid: String!) {\\n installation(giid: $giid) {\\n smartplugs {\\n device {\\n deviceLabel\\n area\\n __typename\\n }\\n currentState\\n icon\\n isHazardous\\n __typename\\n }\\n __typename\\n }\\n}\\n\"}";
			logger.debug("Trying to get smart plug status with URL {} and data {}", url, queryQLSmartPlug);
			VerisureSmartPlugsJSON thing = (VerisureSmartPlugsJSON) callJSONRestPost(url, queryQLSmartPlug, jsonClass);
			logger.debug("REST Response ({})", thing);

			if (thing != null) {
				List<VerisureSmartPlugsJSON.Smartplug> smartPlugList = thing.getData().getInstallation().getSmartplugs();
				for (VerisureSmartPlugsJSON.Smartplug smartPlug : smartPlugList) {
					VerisureSmartPlugsJSON slThing = new VerisureSmartPlugsJSON();
					VerisureSmartPlugsJSON.Installation inst = new VerisureSmartPlugsJSON.Installation();
					List<VerisureSmartPlugsJSON.Smartplug> list = new ArrayList<VerisureSmartPlugsJSON.Smartplug>();
					list.add(smartPlug);
					inst.setSmartplugs(list);
					VerisureSmartPlugsJSON.Data data = new VerisureSmartPlugsJSON.Data();
					data.setInstallation(inst);
					slThing.setData(data);
					// Set unique deviceID
					String deviceId = smartPlug.getDevice().getDeviceLabel();
					deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
					slThing.setDeviceId(deviceId);
					// Set location
					slThing.setLocation(smartPlug.getDevice().getArea());
					VerisureThingJSON oldObj = verisureThings.get(deviceId);
					if (oldObj == null || !oldObj.equals(slThing)) {
						slThing.setSiteId(installation.getInstallationId());
						slThing.setSiteName(installation.getInstallationName());
						verisureThings.put(deviceId, slThing);
						notifyListeners(slThing);
					}

				}
			}
		}
	}
	
	private synchronized void updateClimateStatus(Class<? extends VerisureThingJSON> jsonClass,
			@Nullable VerisureInstallation installation) {
		if (installation != null) {
			BigDecimal installationId = installation.getInstallationId();
			String url = START_GRAPHQL;

			String queryQLClimates = "{\"operationName\":\"Climate\",\"variables\":{\"giid\":\"" + installationId + "\"},\"query\":\"query Climate($giid: String!) {\\n installation(giid: $giid) {\\n climates {\\n device {\\n deviceLabel\\n area\\n gui {\\n label\\n __typename\\n }\\n __typename\\n }\\n humidityEnabled\\n humidityTimestamp\\n humidityValue\\n temperatureTimestamp\\n temperatureValue\\n __typename\\n }\\n __typename\\n }\\n}\\n\"}";
			logger.debug("Trying to get climate status with URL {} and data {}", url, queryQLClimates);
			VerisureClimatesJSON thing = (VerisureClimatesJSON) callJSONRestPost(url, queryQLClimates, jsonClass);
			logger.debug("REST Response ({})", thing);

			if (thing != null) {
				List<VerisureClimatesJSON.Climate> climateList = thing.getData().getInstallation().getClimates();
				for (VerisureClimatesJSON.Climate climate : climateList) {
					VerisureClimatesJSON cThing = new VerisureClimatesJSON();
					VerisureClimatesJSON.Installation inst = new VerisureClimatesJSON.Installation();
					List<VerisureClimatesJSON.Climate> list = new ArrayList<VerisureClimatesJSON.Climate>();
					list.add(climate);
					inst.setClimates(list);
					VerisureClimatesJSON.Data data = new VerisureClimatesJSON.Data();
					data.setInstallation(inst);
					cThing.setData(data);
					// Set unique deviceID
					String deviceId = climate.getDevice().getDeviceLabel();
					deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
					cThing.setDeviceId(deviceId);
					// Set location
					cThing.setLocation(climate.getDevice().getArea());
					VerisureThingJSON oldObj = verisureThings.get(deviceId);
					if (oldObj == null || !oldObj.equals(cThing)) {
						cThing.setSiteId(installation.getInstallationId());
						cThing.setSiteName(installation.getInstallationName());
						verisureThings.put(deviceId, cThing);
						notifyListeners(cThing);
					}

				}
			}
		}
	}
	
	private synchronized void updateDoorWindowStatus(Class<? extends VerisureThingJSON> jsonClass,
			@Nullable VerisureInstallation installation) {
		if (installation != null) {
			BigDecimal installationId = installation.getInstallationId();
			String url = START_GRAPHQL;

			String queryQLDoorWindow = "{\"operationName\":\"DoorWindow\",\"variables\":{\"giid\":\"" + installationId + "\"},\"query\":\"query DoorWindow($giid: String!) {\\n installation(giid: $giid) {\\n doorWindows {\\n device {\\n deviceLabel\\n area\\n __typename\\n }\\n type\\n state\\n wired\\n reportTime\\n __typename\\n }\\n __typename\\n }\\n}\\n\"}";
			logger.debug("Trying to get climate status with URL {} and data {}", url, queryQLDoorWindow);
			VerisureDoorWindowsJSON thing = (VerisureDoorWindowsJSON) callJSONRestPost(url, queryQLDoorWindow, jsonClass);
			logger.debug("REST Response ({})", thing);

			if (thing != null) {
				List<VerisureDoorWindowsJSON.DoorWindow> doorWindowList = thing.getData().getInstallation().getDoorWindows();
				for (VerisureDoorWindowsJSON.DoorWindow doorWindow : doorWindowList) {
					VerisureDoorWindowsJSON dThing = new VerisureDoorWindowsJSON();
					VerisureDoorWindowsJSON.Installation inst = new VerisureDoorWindowsJSON.Installation();
					List<VerisureDoorWindowsJSON.DoorWindow> list = new ArrayList<VerisureDoorWindowsJSON.DoorWindow>();
					list.add(doorWindow);
					inst.setDoorWindows(list);
					VerisureDoorWindowsJSON.Data data = new VerisureDoorWindowsJSON.Data();
					data.setInstallation(inst);
					dThing.setData(data);
					// Set unique deviceID
					String deviceId = doorWindow.getDevice().getDeviceLabel();
					deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
					dThing.setDeviceId(deviceId);
					// Set location
					dThing.setLocation(doorWindow.getDevice().getArea());
					VerisureThingJSON oldObj = verisureThings.get(deviceId);
					if (oldObj == null || !oldObj.equals(dThing)) {
						dThing.setSiteId(installation.getInstallationId());
						dThing.setSiteName(installation.getInstallationName());
						verisureThings.put(deviceId, dThing);
						notifyListeners(dThing);
					}

				}
			}
		}
	}

	private synchronized void updateBroadbandConnectionStatus(Class<? extends VerisureThingJSON> jsonClass,
			@Nullable VerisureInstallation inst) {
		if (inst != null) {
			BigDecimal installationId = inst.getInstallationId();
			String url = START_GRAPHQL;

			String queryQLBroadbandConnection = "{\"operationName\":\"Broadband\",\"variables\":{\"giid\":\""
					+ installationId
					+ "\"},\"query\":\"query Broadband($giid: String!) {\\n installation(giid: $giid) {\\n broadband {\\n testDate\\n isBroadbandConnected\\n __typename\\n }\\n __typename\\n }\\n}\\n\"}";
			logger.debug("Trying to get alarm status with URL {} and data {}", url, queryQLBroadbandConnection);
			VerisureThingJSON thing = callJSONRestPost(url, queryQLBroadbandConnection, jsonClass);
			logger.debug("REST Response ({})", thing);

			if (thing != null) {
				// Set unique deviceID
				String deviceId = "bc" + installationId.toString();
				deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
				thing.setDeviceId(deviceId);
				VerisureThingJSON oldObj = verisureThings.get(deviceId);
				if (oldObj == null || !oldObj.equals(thing)) {
					thing.setSiteId(inst.getInstallationId());
					thing.setSiteName(inst.getInstallationName());
					verisureThings.put(deviceId, thing);
					notifyListeners(thing);
				}
			}
		}
	}
	
	
	private synchronized void updateUserPresenceStatus(Class<? extends VerisureThingJSON> jsonClass,
			@Nullable VerisureInstallation installation) {
		if (installation != null) {
			BigDecimal installationId = installation.getInstallationId();
			String url = START_GRAPHQL;

			String queryQLUserPresence = "{\"operationName\":\"userTrackings\",\"variables\":{\"giid\":\""
					+ installationId
					+ "\"},\"query\":\"query userTrackings($giid: String!) {\\n  installation(giid: $giid) {\\n    userTrackings {\\n      isCallingUser\\n      webAccount\\n      status\\n      xbnContactId\\n      currentLocationName\\n      deviceId\\n      name\\n      currentLocationTimestamp\\n      deviceName\\n      currentLocationId\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}";
			logger.debug("Trying to get user presence status with URL {} and data {}", url, queryQLUserPresence);
			VerisureUserPresencesJSON thing = (VerisureUserPresencesJSON) callJSONRestPost(url, queryQLUserPresence,
					jsonClass);
			logger.debug("REST Response ({})", thing);

			if (thing != null) {
				List<VerisureUserPresencesJSON.UserTracking> userTrackingList = thing.getData().getInstallation()
						.getUserTrackings();
				for (VerisureUserPresencesJSON.UserTracking userTracking : userTrackingList) {
					if (userTracking.getStatus().equals("ACTIVE")) {
						VerisureUserPresencesJSON upThing = new VerisureUserPresencesJSON();
						VerisureUserPresencesJSON.Installation inst = new VerisureUserPresencesJSON.Installation();
						List<VerisureUserPresencesJSON.UserTracking> list = new ArrayList<VerisureUserPresencesJSON.UserTracking>();
						list.add(userTracking);
						inst.setUserTrackings(list);
						VerisureUserPresencesJSON.Data data = new VerisureUserPresencesJSON.Data();
						data.setInstallation(inst);
						upThing.setData(data);
						// Set unique deviceID
						String deviceId = "up" + userTracking.getWebAccount() + installationId.toString();
						deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
						upThing.setDeviceId(deviceId);
						VerisureThingJSON oldObj = verisureThings.get(deviceId);
						if (oldObj == null || !oldObj.equals(upThing)) {
							upThing.setSiteId(installation.getInstallationId());
							upThing.setSiteName(installation.getInstallationName());
							verisureThings.put(deviceId, upThing);
							notifyListeners(upThing);
						}
					}
				}
			}
		}
	}	
}
