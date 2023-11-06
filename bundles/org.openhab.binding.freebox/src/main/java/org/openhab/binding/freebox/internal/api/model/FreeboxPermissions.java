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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxPermissions} is the Java class used to map the
 * structure used inside the response of the open session API to provide
 * the permissions
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxPermissions {
    private Boolean settings;
    private Boolean contacts;
    private Boolean calls;
    private Boolean explorer;
    private Boolean downloader;
    private Boolean parental;
    private Boolean pvr;
    private Boolean tv;

    public Boolean isSettingsAllowed() {
        return settings;
    }

    public Boolean isContactsAllowed() {
        return contacts;
    }

    public Boolean isCallsAllowed() {
        return calls;
    }

    public Boolean isExplorerAllowed() {
        return explorer;
    }

    public Boolean isDownloaderAllowed() {
        return downloader;
    }

    public Boolean isParentalAllowed() {
        return parental;
    }

    public Boolean isPvrAllowed() {
        return pvr;
    }

    public Boolean istTvAllowed() {
        return tv;
    }
}
