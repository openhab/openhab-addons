/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vitaled.internal;

import static org.openhab.binding.vitaled.VitaLEDBindingConstants.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VitaLEDConnection} class contains fields mapping thing configuration parameters.
 *
 * @author Marcel Salein - Initial contribution
 */
public class VitaLEDConnection {
	private String ip;
	private int port;
	private BigDecimal refreshInterval;
	private int responseCode;
	private String url;
	private URL obj;
	private HttpURLConnection connection;

	private Logger logger = LoggerFactory.getLogger(VitaLEDConnection.class);
	public VitaLEDZone[] zones = new VitaLEDZone[8];
	public String[] scenes = new String[6];

	/**
	 * Costructor that set IP address and port of the vitaLED LAN master to the class attributes
	 * in order to establish a connection to the vitaLED LAN master
	 *
	 * @param ip IP address of the vita LED LAN master
	 * @param port port of the vita LED LAN master
	 */
	public VitaLEDConnection(String ip, int port) {
		this.setIp(ip);
		this.setPort(port);
	}

	/**
	 * Update state of channelUID with command by sending HTTP GET requests
	 * with URL parameter to vitaLED LAN master
	 *
	 * @param channelUID The channelUID for which the current state should be updated with the command
	 * @param command The command that is used to update the channelUID
	 * @return the current state of the requested channelUID
	 */
	public boolean updateState(ChannelUID channelUID, Command command) throws Exception {
		// check channelUID like room7#achromaticLight
		// get attribute like achromaticLight
		String attribute = channelUID.getIdWithoutGroup();
		// get zone number
		String zone = channelUID.getGroupId().substring(4, 5);
		// Array starts with 0, therefore calculate Index of array
		int zoneIndex = Integer.parseInt(zone) - 1;
		// referer
		String referer = "";
		// prepare urlParameter
		String urlParameter = "Room=" + zoneIndex;
		// return parameter that indicates full update of channel states
		boolean fullUpdate = false;
		switch (attribute) {
			/************************
			 *** Achromatic Light ***
			 ************************/
			case ACHROMATIC_LIGHT:
				referer = ACHROMATIC_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "&TK=" + command.toString();
				zones[zoneIndex].setAchromaticLight(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case INTENSITY:
				referer = ACHROMATIC_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "&Lightness=" + command.toString();
				zones[zoneIndex].setIntensity(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			/***********************
			 *** Chromatic Light ***
			 ***********************/
			case RED:
				// we have chromatic light use RGBW
				referer = CHROMATIC_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "&R=" + command.toString() + "&G=" + zones[zoneIndex].getGreen() + "&B="
						+ zones[zoneIndex].getBlue() + "&W=" + zones[zoneIndex].getWhite();
				zones[zoneIndex].setRed(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case GREEN:
				// we have chromatic light use RGBW
				referer = CHROMATIC_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "&R=" + zones[zoneIndex].getRed() + "&G=" + command.toString() + "&B="
						+ zones[zoneIndex].getBlue() + "&W=" + zones[zoneIndex].getWhite();
				zones[zoneIndex].setGreen(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case BLUE:
				// we have chromatic light use RGBW
				referer = CHROMATIC_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "&R=" + zones[zoneIndex].getRed() + "&G=" + zones[zoneIndex].getGreen() + "&B="
						+ command.toString() + "&W=" + zones[zoneIndex].getWhite();
				zones[zoneIndex].setBlue(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case WHITE:
				// we have chromatic light use RGBW
				referer = CHROMATIC_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "&R=" + zones[zoneIndex].getRed() + "&G=" + zones[zoneIndex].getGreen() + "B="
						+ zones[zoneIndex].getBlue() + "&W=" + command.toString();
				zones[zoneIndex].setWhite(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case COLOR:
				// Color
				// we have chromatic light use RGBW
				if (command instanceof HSBType) {
					HSBType hsbCommand = (HSBType) command;
					if (hsbCommand.getBrightness().intValue() == 0) {
						// switch off light
						zones[zoneIndex].setIntensity(0);
						fullUpdate = true;
					} else {
						// set Color
						zones[zoneIndex].setHSB(hsbCommand);
						fullUpdate = true;
					}
				} else if (command instanceof PercentType) {
					// set brightness / intensity
				} else if (command instanceof OnOffType) {
					// on / off
					// lightState = LightStateConverter.toOnOffLightState((OnOffType) command);
					OnOffType onOff = (OnOffType) command;
					if (onOff.equals(OnOffType.OFF)) {
						// switch off light
						zones[zoneIndex].setIntensity(0);
					} else {
						// switch on light
						zones[zoneIndex].setIntensity(100);
					}
					fullUpdate = true;
				} else if (command instanceof IncreaseDecreaseType) {
					// set brightness / intensity
				}

				// update bulbs
				referer = CHROMATIC_REFERER;
				urlParameter = "&R=" + zones[zoneIndex].getRed() + "&G=" + zones[zoneIndex].getGreen() + "&B="
						+ zones[zoneIndex].getBlue() + "&W=" + zones[zoneIndex].getWhite() + "&Lightness="
						+ zones[zoneIndex].getIntensity();
				sendCommand(urlParameter, referer);
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case COLOUR_SATURATION:
				// we have colour gradients
				referer = COLOUR_GRADIENTS_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "LEDSPARTYManual=1&Saturation=" + command.toString() + "&Speed="
						+ zones[zoneIndex].getSpeed() + "Lightness=" + zones[zoneIndex].getIntensity();
				zones[zoneIndex].setColourSaturation(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case SPEED:
				// we have colour gradients
				referer = COLOUR_GRADIENTS_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "LEDSPARTYManual=1&Saturation=" + zones[zoneIndex].getColourSaturation() + "&Speed="
						+ command.toString() + "Lightness=" + zones[zoneIndex].getIntensity();
				zones[zoneIndex].setSpeed(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case X_COORDINATE:
				// we have colour triangle
				referer = COLOUR_TRIANGLE_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "Lightness=" + zones[zoneIndex].getIntensity() + "&X=" + command.toString() + "&Y="
						+ zones[zoneIndex].getyCoord();
				zones[zoneIndex].setxCoord(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case Y_COORDINATE:
				// we have colour triangle
				referer = COLOUR_TRIANGLE_REFERER;
				sendCommand(urlParameter, referer);
				urlParameter = "Lightness=" + zones[zoneIndex].getIntensity() + "&X=" + zones[zoneIndex].getxCoord()
						+ "&Y=" + command.toString();
				zones[zoneIndex].setyCoord(Integer.parseInt(command.toString()));
				logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				break;
			case SCENE1:
				if (command.equals(OnOffType.ON)) {
					// Scene 1 SLOT1Manual=1
					referer = SCENE_REFERER;
					sendCommand(urlParameter, referer);
					urlParameter = "SLOT1Manual=1";
					// switch on scene
					zones[zoneIndex].setScene1(1);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
					fullUpdate = true;
				} else {
					zones[zoneIndex].setScene1(0);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				}
				break;
			case SCENE2:
				if (command.equals(OnOffType.ON)) {
					// Scene 2 SLOT2Manual=1
					referer = SCENE_REFERER;
					sendCommand(urlParameter, referer);
					urlParameter = "SLOT2Manual=1";
					// switch on scene
					zones[zoneIndex].setScene2(1);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
					fullUpdate = true;
				} else {
					zones[zoneIndex].setScene2(0);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				}
				break;
			case SCENE3:
				if (command.equals(OnOffType.ON)) {
					// Scene 3 SLOT3Manual=1
					referer = SCENE_REFERER;
					sendCommand(urlParameter, referer);
					urlParameter = "SLOT3Manual=1";
					// switch on scene
					zones[zoneIndex].setScene3(1);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
					fullUpdate = true;
				} else {
					zones[zoneIndex].setScene3(0);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				}
				break;
			case SCENE4:
				if (command.equals(OnOffType.ON)) {
					// Scene 4 SLOT4Manual=1
					referer = SCENE_REFERER;
					sendCommand(urlParameter, referer);
					urlParameter = "SLOT4Manual=1";
					// switch on scene
					zones[zoneIndex].setScene4(1);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
					fullUpdate = true;
				} else {
					zones[zoneIndex].setScene4(0);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				}
				break;
			case SCENE5:
				if (command.equals(OnOffType.ON)) {
					// Scene 5 SLOT5Manual=1
					referer = SCENE_REFERER;
					sendCommand(urlParameter, referer);
					urlParameter = "SLOT5Manual=1";
					// switch on scene
					zones[zoneIndex].setScene5(1);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
					fullUpdate = true;
				} else {
					zones[zoneIndex].setScene5(0);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				}
				break;
			case SCENE6:
				if (command.equals(OnOffType.ON)) {
					// Scene 6 SLOT6Manual=1
					referer = SCENE_REFERER;
					sendCommand(urlParameter, referer);
					urlParameter = "SLOT6Manual=1";
					// switch on scene
					zones[zoneIndex].setScene6(1);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
					fullUpdate = true;
				} else {
					zones[zoneIndex].setScene6(0);
					logger.debug("Update {} with {}", channelUID.getId(), command.toString());
				}
				break;
			default:
				return fullUpdate;
		}
		sendCommand(urlParameter, referer);
		return fullUpdate;
	}

	/**
	 * Send command via HTTP GET request to vitaLED LAN Master based on URL Parameter
	 * and referer URL
	 *
	 * @param channelUID The channel UID for which the current state should returned
	 */
	public void sendCommand(String urlParameter, String referer) throws Exception {
		url = "http://" + getIp() + ":" + getPort() + "/sample?" + urlParameter;
		obj = new URL(url);
		connection = (HttpURLConnection) obj.openConnection();
		// optional default is GET
		connection.setRequestMethod("GET");
		// add request header
		connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("Host", getIp());
		connection.setRequestProperty("Referer", "http://" + getIp() + "/" + referer);
		connection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
		logger.info("Sending 'GET' request to URL : {}", url);
		responseCode = connection.getResponseCode();
		logger.info("Response Code : {}", responseCode);
	}

	/**
	 * Get current state of VitaLED channel
	 *
	 * @param channelUID The channel UID for which the current state should returned
	 * @return the current state of the requested channelUID
	 */
	public DecimalType getCurrentState(ChannelUID channelUID) throws Exception {
		// channelUID like room7#achromaticLight
		// get attribute like achromaticLight
		String attribute = channelUID.getIdWithoutGroup();
		// get zone number for instance 7 out of room7#achromaticLight
		String zone = channelUID.getGroupId().substring(4, 5);
		// Array starts with 0, therefore calculate Index of array
		int zoneIndex = Integer.parseInt(zone) - 1;
		DecimalType result;
		switch (attribute) {
			case ACHROMATIC_LIGHT:
				result = new DecimalType(zones[zoneIndex].getAchromaticLight());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case RED:
				result = new DecimalType(zones[zoneIndex].getRed());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case GREEN:
				result = new DecimalType(zones[zoneIndex].getGreen());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case BLUE:
				result = new DecimalType(zones[zoneIndex].getBlue());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case WHITE:
				result = new DecimalType(zones[zoneIndex].getWhite());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case X_COORDINATE:
				result = new DecimalType(zones[zoneIndex].getxCoord());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case Y_COORDINATE:
				result = new DecimalType(zones[zoneIndex].getyCoord());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case INTENSITY:
				result = new DecimalType(zones[zoneIndex].getIntensity());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case SCENE1:
				result = new DecimalType(zones[zoneIndex].getScene1());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case SCENE2:
				result = new DecimalType(zones[zoneIndex].getScene2());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case SCENE3:
				result = new DecimalType(zones[zoneIndex].getScene3());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case SCENE4:
				result = new DecimalType(zones[zoneIndex].getScene4());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case SCENE5:
				result = new DecimalType(zones[zoneIndex].getScene5());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			case SCENE6:
				result = new DecimalType(zones[zoneIndex].getScene6());
				logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
				return result;
			default:
				return new DecimalType(0);
		}
	}

	/**
	 * Get current state of VitaLED channel
	 *
	 * @param channelUID The channel UID for which the current state should returned
	 * @return the current state of the requested channelUID
	 */
	public HSBType getCurrentColorState(ChannelUID channelUID) throws Exception {
		// get zone number for instance 7 out of room7#achromaticLight
		String zone = channelUID.getGroupId().substring(4, 5);
		// Array starts with 0, therefore calculate Index of array
		int zoneIndex = Integer.parseInt(zone) - 1;
		// get HSB value from RGB
		HSBType result = zones[zoneIndex].getHSB();
		logger.debug("Read value {} from VitaLED for {}", result.toString(), channelUID.getId());
		return result;
	}

	/**
	 * Get current state of VitaLED zone from LAN Master via HTTP GET Request
	 *
	 * @param zoneNumber The number of the zone for which the current state should be read from the
	 *            vitaLED LAN master. The number can only be between 0 and 7 representing the 8 zones.
	 */
	public void getCurrentStateOfZone(int zoneNumber) throws Exception {
		int line;
		int responseCode;
		String inputLine;
		BufferedReader in;

		// determine technical zone
		int i = zoneNumber;

		// get zone configuration
		url = "http://" + getIp() + ":" + getPort() + "/room" + i + ".txt";
		obj = new URL(url);
		connection = (HttpURLConnection) obj.openConnection();

		// set request to GET
		connection.setRequestMethod("GET");

		// add request header
		connection.setRequestProperty("Accept", "text/html, */*; q=0.01");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept-Language", "de,en-US;q=0.7,en;q=0.3");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("DNT", "1");
		connection.setRequestProperty("Host", getIp());
		connection.setRequestProperty("Referer", "http://" + getIp() + "/user-d.html");
		connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

		responseCode = connection.getResponseCode();
		logger.debug("Sending 'GET' request to URL : {}", url);
		logger.debug("Response Code : {}", responseCode);

		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		VitaLEDZone zone = new VitaLEDZone();
		zones[i] = zone;

		line = 1;
		while ((inputLine = in.readLine()) != null) {
			switch (line) {
				// set red light
				case 1:
					zones[i].setRed(Integer.parseInt(inputLine));
					break;
				// set green light
				case 2:
					zones[i].setGreen(Integer.parseInt(inputLine));
					break;
				// set blue light
				case 3:
					zones[i].setBlue(Integer.parseInt(inputLine));
					break;
				// set white light
				case 4:
					zones[i].setWhite(Integer.parseInt(inputLine));
					break;
				// set x-Coordinate
				case 5:
					zones[i].setxCoord(Integer.parseInt(inputLine));
					break;
				// set y-Coordinate
				case 6:
					zones[i].setyCoord(Integer.parseInt(inputLine));
					break;
				// set Achromatic Light
				case 7:
					zones[i].setAchromaticLight(Integer.parseInt(inputLine));
					break;
				// set Intensity
				case 8:
					zones[i].setIntensity(Integer.parseInt(inputLine));
					break;
				// set speed
				case 9:
					zones[i].setSpeed(Integer.parseInt(inputLine));
					break;
				// set colour saturation
				case 10:
					zones[i].setColourSaturation(Integer.parseInt(inputLine));
					break;
				// active Mode
				case 79:
					zones[i].setColourSaturation(Integer.parseInt(inputLine));
					break;
				// scene
				case 80:
					switch (Integer.parseInt(inputLine)) {
						case 0:
							// scene 1
							zones[i].setScene1(1);
							zones[i].setScene2(0);
							zones[i].setScene3(0);
							zones[i].setScene4(0);
							zones[i].setScene5(0);
							zones[i].setScene6(0);
							break;
						case 1:
							// scene 2
							zones[i].setScene1(0);
							zones[i].setScene2(1);
							zones[i].setScene3(0);
							zones[i].setScene4(0);
							zones[i].setScene5(0);
							zones[i].setScene6(0);
							break;
						case 2:
							// scene 3
							zones[i].setScene1(0);
							zones[i].setScene2(0);
							zones[i].setScene3(1);
							zones[i].setScene4(0);
							zones[i].setScene5(0);
							zones[i].setScene6(0);
							break;
						case 3:
							// scene 4
							zones[i].setScene1(0);
							zones[i].setScene2(0);
							zones[i].setScene3(0);
							zones[i].setScene4(1);
							zones[i].setScene5(0);
							zones[i].setScene6(0);
							break;
						case 4:
							// scene 5
							zones[i].setScene1(0);
							zones[i].setScene2(0);
							zones[i].setScene3(0);
							zones[i].setScene4(0);
							zones[i].setScene5(1);
							zones[i].setScene6(0);
							break;
						case 5:
							// scene 6
							zones[i].setScene1(0);
							zones[i].setScene2(0);
							zones[i].setScene3(0);
							zones[i].setScene4(0);
							zones[i].setScene5(0);
							zones[i].setScene6(1);
							break;
						default:
							// no scene active
							break;
					}
					break;
			}
			line = line + 1;
		}
		in.close();
	}

	/**
	 * Reads zone descriptions that are stored in the vitaLED LAN master
	 * for the 8 zones that can be defined in the vitaLED LAN master
	 *
	 */
	public void getZoneDescriptions() throws Exception {
		int line;
		String inputLine;
		BufferedReader in;

		// get zone description
		url = "http://" + getIp() + ":" + getPort() + "/rooms.txt";
		obj = new URL(url);
		connection = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		connection.setRequestMethod("GET");

		// add request header
		connection.setRequestProperty("Accept", "text/html, */*; q=0.01");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept-Language", "de,en-US;q=0.7,en;q=0.3");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("DNT", "1");
		connection.setRequestProperty("Host", getIp());
		connection.setRequestProperty("Referer", "http://" + getIp() + "/user-d.html");
		connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

		responseCode = connection.getResponseCode();
		logger.debug("Sending 'GET' request to URL : {}", url);
		logger.debug("Response Code : {}", responseCode);

		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		line = 0;
		while ((inputLine = in.readLine()) != null) {
			if (line < 8) {
				zones[line].setZoneDescription(inputLine);
				line = line + 1;
			}
		}
		in.close();
	}

	/**
	 * Reads scene descriptions that are stored in the vitaLED LAN master
	 * for the 6 scenes that can be defined in the vitaLED LAN master
	 *
	 */
	public void getSceneDescription() throws Exception {
		int line;
		String inputLine;
		BufferedReader in;

		// get scenes description
		url = "http://" + getIp() + ":" + getPort() + "/names.txt";
		obj = new URL(url);
		connection = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		connection.setRequestMethod("GET");

		// add request header
		connection.setRequestProperty("Accept", "text/html, */*; q=0.01");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept-Language", "de,en-US;q=0.7,en;q=0.3");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("DNT", "1");
		connection.setRequestProperty("Host", getIp());
		connection.setRequestProperty("Referer", "http://" + getIp() + "/user-d.html");
		connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

		responseCode = connection.getResponseCode();
		logger.debug("Sending 'GET' request to URL : {}", url);
		logger.debug("Response Code : {}", responseCode);

		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		line = 0;
		while ((inputLine = in.readLine()) != null) {
			if (line < 6) {
				scenes[line] = inputLine;
				line = line + 1;
			}
		}
		in.close();
	}

	/**
	 * Returns the IP address of the vitaLED LAN master
	 *
	 * @return the IP address of the vitaLED LAN master
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Set the IP address of the vitaLED LAN master
	 *
	 * @param ip IP address of the vitaLED LAN master
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Returns the port of the vitaLED LAN master
	 *
	 * @return the port of the vitaLED LAN master
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set the port of the vitaLED LAN master
	 *
	 * @param port port of the vitaLED LAN master
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns the refresh interval that is used to read current vitaLED settings
	 * from the vitaLED LAN master
	 *
	 * @return the port of the vitaLED LAN master
	 */
	public BigDecimal getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * Set the refresh interval that is used to read current vitaLED settings
	 * from the vitaLED LAN master
	 *
	 * @param refreshInterval Refresh interval of the vitaLED LAN master
	 */
	public void setRefreshInterval(BigDecimal refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

}
