/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for display.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Display {
    private Integer brightness;
    private String brightnessMode;
    private Integer height;
    private Screensaver screensaver;
    private String type;
    private Integer width;

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Display withBrightness(Integer brightness) {
        this.brightness = brightness;
        return this;
    }

    public String getBrightnessMode() {
        return brightnessMode;
    }

    public void setBrightnessMode(String brightnessMode) {
        this.brightnessMode = brightnessMode;
    }

    public Display withBrightnessMode(String brightnessMode) {
        this.brightnessMode = brightnessMode;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Display withHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Screensaver getScreensaver() {
        return screensaver;
    }

    public void setScreensaver(Screensaver screensaver) {
        this.screensaver = screensaver;
    }

    public Display withScreensaver(Screensaver screensaver) {
        this.screensaver = screensaver;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Display withType(String type) {
        this.type = type;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Display withWidth(Integer width) {
        this.width = width;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Display [brightness=");
        builder.append(brightness);
        builder.append(", brightnessMode=");
        builder.append(brightnessMode);
        builder.append(", height=");
        builder.append(height);
        builder.append(", screensaver=");
        builder.append(screensaver);
        builder.append(", type=");
        builder.append(type);
        builder.append(", width=");
        builder.append(width);
        builder.append("]");
        return builder.toString();
    }
}
