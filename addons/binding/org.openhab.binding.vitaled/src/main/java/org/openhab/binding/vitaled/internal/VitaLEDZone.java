/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vitaled.internal;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * The {@link VitaLEDZone} is responsible for handling a zone of the vitaLED LAN master.
 *
 * @author Marcel Salein - Initial contribution
 */
public class VitaLEDZone {
	private String zoneDescription = new String();
	private int red;
	private int blue;
	private int green;
	private int white;
	private int xCoord;
	private int yCoord;
	private int achromaticLight;
	private int intensity;
	private int speed;
	private int colourSaturation;
	private int activeMode;
	private int scene1;
	private int scene2;
	private int scene3;
	private int scene4;
	private int scene5;
	private int scene6;

	/**
	 * Get current value of red light for vitaLED zone
	 *
	 * @return The current value of red light for vitaLED zone
	 */
	public int getRed() {
		return red;
	}

	/**
	 * Set new value of red light for vitaLED zone
	 *
	 * @param red Set new value of red light for vitaLED zone
	 */
	public void setRed(int red) {
		this.red = red;
	}

	/**
	 * Get current value of blue light for vitaLED zone
	 *
	 * @return The current value of blue light for vitaLED zone
	 */
	public int getBlue() {
		return blue;
	}

	/**
	 * Set new value of blue light for vitaLED zone
	 *
	 * @param blue Set new value of blue light for vitaLED zone
	 */
	public void setBlue(int blue) {
		this.blue = blue;
	}

	/**
	 * Get current value of green light for vitaLED zone
	 *
	 * @return The current value of green light for vitaLED zone
	 */
	public int getGreen() {
		return green;
	}

	/**
	 * Set new value of green light for vitaLED zone
	 *
	 * @param green Set new value of green light for vitaLED zone
	 */
	public void setGreen(int green) {
		this.green = green;
	}

	/**
	 * Get current value of white light for vitaLED zone
	 *
	 * @return The current value of white light for vitaLED zone
	 */
	public int getWhite() {
		return white;
	}

	/**
	 * Get current value of color for vitaLED zone
	 *
	 * @return The current value of color for vitaLED zone
	 */
	public HSBType getHSB() {
		int red = (int) (getRed() * 2.55);
		int green = (int) (getGreen() * 2.55);
		int blue = (int) (getBlue() * 2.55);
		HSBType result = HSBType.fromRGB(red, green, blue);
		return result;
	}

	/**
	 * Set current value of color for vitaLED zone in HSB
	 *
	 * @return The current value of color for vitaLED zone
	 */
	public void setHSB(HSBType hsbCommand) {
		PercentType[] rgb = hsbCommand.toRGB();
		setRed(rgb[0].intValue());
		setGreen(rgb[1].intValue());
		setBlue(rgb[2].intValue());
		setWhite(0);
		setIntensity(hsbCommand.getBrightness().intValue());
	}

	/**
	 * Set new value of white light for vitaLED zone
	 *
	 * @param white Set new value of white light for vitaLED zone
	 */
	public void setWhite(int white) {
		this.white = white;
	}

	/**
	 * Get current value of x coordinate for vitaLED zone
	 *
	 * @return The current value of x coordinate for vitaLED zone
	 */
	public int getxCoord() {
		return xCoord;
	}

	/**
	 * Set new value of x coordinate for vitaLED zone
	 *
	 * @param xCoord Set new value of x coordinate for vitaLED zone
	 */
	public void setxCoord(int xCoord) {
		this.xCoord = xCoord;
	}

	/**
	 * Get current value of y coordinate for vitaLED zone
	 *
	 * @return The current value of y coordinate for vitaLED zone
	 */
	public int getyCoord() {
		return yCoord;
	}

	/**
	 * Set new value of y coordinate for vitaLED zone
	 *
	 * @param yCoord Set new value of y coordinate for vitaLED zone
	 */
	public void setyCoord(int yCoord) {
		this.yCoord = yCoord;
	}

	/**
	 * Get current value of achromatic light for vitaLED zone
	 *
	 * @return The current value of achromatic light for vitaLED zone
	 */
	public int getAchromaticLight() {
		return achromaticLight;
	}

	/**
	 * Set new value of achromatic light for vitaLED zone
	 *
	 * @param achromaticLight Set new value of achromatic light for vitaLED zone
	 */
	public void setAchromaticLight(int achromaticLight) {
		this.achromaticLight = achromaticLight;
	}

	/**
	 * Get current value of intensity for vitaLED zone
	 *
	 * @return The current value of intensity for vitaLED zone
	 */
	public int getIntensity() {
		return intensity;
	}

	/**
	 * Set new value of intensity for vitaLED zone
	 *
	 * @param intensity Set new value of intensity for vitaLED zone
	 */
	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}

	/**
	 * Get current value of speed for vitaLED zone
	 *
	 * @return The current value of speed for vitaLED zone
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * Set new value of speed for vitaLED zone
	 *
	 * @param speed Set new value of speed for vitaLED zone
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	/**
	 * Get current value of colour saturation for vitaLED zone
	 *
	 * @return The current value of colour saturation for vitaLED zone
	 */
	public int getColourSaturation() {
		return colourSaturation;
	}

	/**
	 * Set new value of colour saturation for vitaLED zone
	 *
	 * @param colourSaturation Set new value of colour saturation for vitaLED zone
	 */
	public void setColourSaturation(int colourSaturation) {
		this.colourSaturation = colourSaturation;
	}

	/**
	 * Get current value of active mode for vitaLED zone
	 *
	 * @return The current value of active mode for vitaLED zone
	 */
	public int getActiveMode() {
		return activeMode;
	}

	/**
	 * Set new value of active mode for vitaLED zone
	 *
	 * @param activeMode Set new value of active mode for vitaLED zone
	 */
	public void setActiveMode(int activeMode) {
		this.activeMode = activeMode;
	}

	/**
	 * Get current value of scene1 for vitaLED zone
	 *
	 * @return The current value of scene1 for vitaLED zone
	 */
	public int getScene1() {
		return scene1;
	}

	/**
	 * Set new value of scene1 for vitaLED zone
	 *
	 * @param scene1 Set new value of scene1 for vitaLED zone
	 */
	public void setScene1(int scene1) {
		this.scene1 = scene1;
	}

	/**
	 * Get current value of scene2 for vitaLED zone
	 *
	 * @return The current value of scene2 for vitaLED zone
	 */
	public int getScene2() {
		return scene2;
	}

	/**
	 * Set new value of scene2 for vitaLED zone
	 *
	 * @param scene2 Set new value of scene2 for vitaLED zone
	 */
	public void setScene2(int scene2) {
		this.scene2 = scene2;
	}

	/**
	 * Get current value of scene3 for vitaLED zone
	 *
	 * @return The current value of scene3 for vitaLED zone
	 */
	public int getScene3() {
		return scene3;
	}

	/**
	 * Set new value of scene3 for vitaLED zone
	 *
	 * @param scene3 Set new value of scene3 for vitaLED zone
	 */
	public void setScene3(int scene3) {
		this.scene3 = scene3;
	}

	/**
	 * Get current value of scene4 for vitaLED zone
	 *
	 * @return The current value of scene4 for vitaLED zone
	 */
	public int getScene4() {
		return scene4;
	}

	/**
	 * Set new value of scene4 for vitaLED zone
	 *
	 * @param scene4 Set new value of scene4 for vitaLED zone
	 */
	public void setScene4(int scene4) {
		this.scene4 = scene4;
	}

	/**
	 * Get current value of scene5 for vitaLED zone
	 *
	 * @return The current value of scene5 for vitaLED zone
	 */
	public int getScene5() {
		return scene5;
	}

	/**
	 * Set new value of scene5 for vitaLED zone
	 *
	 * @param scene5 Set new value of scene5 for vitaLED zone
	 */
	public void setScene5(int scene5) {
		this.scene5 = scene5;
	}

	/**
	 * Get current value of scene6 for vitaLED zone
	 *
	 * @return The current value of scene6 for vitaLED zone
	 */
	public int getScene6() {
		return scene6;
	}

	/**
	 * Set new value of scene6 for vitaLED zone
	 *
	 * @param scene6 Set new value of scene6 for vitaLED zone
	 */
	public void setScene6(int scene6) {
		this.scene6 = scene6;
	}

	/**
	 * Get zone description of the vitaLED zone
	 *
	 * @return The zone description of the vitaLED zone
	 */
	public String getZoneDescription() {
		return zoneDescription;
	}

	/**
	 * Set zone description for vitaLED zone
	 *
	 * @param zoneDescription New zone description for vitaLED zone
	 */
	public void setZoneDescription(String zoneDescription) {
		this.zoneDescription = zoneDescription;
	}
}
