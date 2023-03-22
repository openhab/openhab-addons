/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents write command to set solid color effect
 *
 * @author Martin Raepple - Initial contribution
 * @author Stefan Höhn - Made colorType nullable
 */
@NonNullByDefault
public class Write {

    private String command = "";
    private String animType = "";
    private String animName = "";
    private List<Palette> palette = new ArrayList<>();
    @Nullable
    private String colorType; // is required to be null if not set!
    private String animData = "";
    private boolean loop = false;

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

    public @Nullable String getColorType() {
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

    public boolean getLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public String getAnimName() {
        return animName;
    }

    public void setAnimName(String animName) {
        this.animName = animName;
    }
}
