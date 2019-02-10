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
package org.openhab.binding.nanoleaf.internal.model;

import java.util.List;

/**
 * Represents write command to set solid color effect
 *
 * @author Martin Raepple - Initial contribution
 */
public class Write {

    private String command;
    private String animType;
    private String animName;
    private List<Palette> palette = null;
    private String colorType;
    private String animData;
    private Boolean loop;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getAnimType() {
        return animType;
    }

    public void setAnimType(String animType) {
        this.animType = animType;
    }

    public List<Palette> getPalette() {
        return palette;
    }

    public void setPalette(List<Palette> palette) {
        this.palette = palette;
    }

    public String getColorType() {
        return colorType;
    }

    public void setColorType(String colorType) {
        this.colorType = colorType;
    }

    public String getAnimData() {
        return animData;
    }

    public void setAnimData(String animData) {
        this.animData = animData;
    }

    public Boolean getLoop() {
        return loop;
    }

    public void setLoop(Boolean loop) {
        this.loop = loop;
    }

    public String getAnimName() {
        return animName;
    }

    public void setAnimName(String animName) {
        this.animName = animName;
    }
}
