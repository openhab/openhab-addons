/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto;

/**
 * Details of a bridge firmware update.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class SoftwareUpdate {
    private int updatestate;
    private String url;
    private String text;
    private boolean notify;

    /**
     * Returns the state of the update.
     *
     * <p>
     * Actual meaning currently undocumented
     *
     * @return state of update
     */
    public int getUpdateState() {
        return updatestate;
    }

    /**
     * Returns the url of the changelog.
     *
     * @return changelog url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns a description of the update.
     *
     * @return update description
     */
    public String getText() {
        return text;
    }

    /**
     * Returns if there will be a notification about this update.
     *
     * @return true for notification, false otherwise
     */
    public boolean isNotified() {
        return notify;
    }
}
