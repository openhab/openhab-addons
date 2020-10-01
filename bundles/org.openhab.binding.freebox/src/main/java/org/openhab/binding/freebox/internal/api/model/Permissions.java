/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link Permissions} is the Java class used to map the
 * structure used inside the response of the open session API to provide
 * the permissions
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class Permissions {
    private boolean settings;
    private boolean contacts;
    private boolean calls;
    private boolean explorer;
    private boolean downloader;
    private boolean parental;
    private boolean pvr;
    private boolean tv;

    public boolean isSettingsAllowed() {
        return settings;
    }

    public boolean isContactsAllowed() {
        return contacts;
    }

    public boolean isCallsAllowed() {
        return calls;
    }

    public boolean isExplorerAllowed() {
        return explorer;
    }

    public boolean isDownloaderAllowed() {
        return downloader;
    }

    public boolean isParentalAllowed() {
        return parental;
    }

    public boolean isPvrAllowed() {
        return pvr;
    }

    public boolean istTvAllowed() {
        return tv;
    }
}
