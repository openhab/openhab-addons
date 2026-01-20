/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.interfaces;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.thing.Thing;

/**
 * {@link BaseDevice} interface for common handling of Dirigera devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface BaseDevice {

    Thing getThing();

    boolean checkHandler();

    void handleUpdate(JSONObject data);

    void setDebug(boolean debugFlag, boolean all);

    void updateLinksStart();

    List<String> getLinks();

    void addSoftlink(String linkSourceId, String linkTargetId);

    void updateLinksDone();

    default String getNameForId(String deviceId) {
        return "";
    }
}
