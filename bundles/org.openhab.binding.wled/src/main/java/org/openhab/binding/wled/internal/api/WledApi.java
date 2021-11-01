/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.wled.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;

/**
 * The {@link WledApi} is the json Api methods for different firmware versions
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public interface WledApi {
    public abstract void update() throws ApiException;

    public abstract void initialize() throws ApiException;

    public abstract int getFirmwareVersion() throws ApiException;

    public abstract void setGlobalOn(boolean bool) throws ApiException;

    public abstract void setMasterOn(boolean bool) throws ApiException;

    public abstract void setGlobalBrightness(PercentType percent) throws ApiException;

    public abstract void setMasterBrightness(PercentType percent) throws ApiException;

    public abstract void setMasterHSB(HSBType hsbType) throws ApiException;

    public abstract void setEffect(String string) throws ApiException;

    public abstract void setPreset(String string) throws ApiException;

    public abstract void setPalette(String string) throws ApiException;

    public abstract void setFxIntencity(PercentType percentType) throws ApiException;

    public abstract void setFxSpeed(PercentType percentType) throws ApiException;

    public abstract void setSleep(boolean b) throws ApiException;

    public abstract void setUdpSend(boolean bool) throws ApiException;

    public abstract void setUdpRecieve(boolean bool) throws ApiException;

    public abstract void setTransitionTime(int milliseconds) throws ApiException;

    public abstract void setPresetCycle(boolean bool) throws ApiException;

    public abstract void setPrimaryColor(HSBType hsbType) throws ApiException;

    public abstract void setSecondaryColor(HSBType hsbType) throws ApiException;

    public abstract void setTertiaryColor(HSBType hsbType) throws ApiException;

    public abstract String sendGetRequest(String string) throws ApiException;
}
