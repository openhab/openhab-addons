/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
