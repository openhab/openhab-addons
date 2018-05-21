/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.zoneminder.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link ZoneMinderConnectionStatus} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public enum ZoneMinderConnectionStatus {
    GENERAL_ERROR(-99),
    BINDING_CONFIG_INVALID(-98),
    BINDING_CONNECTION_INVALID(-97),
    BINDING_SESSION_INVALID(-96),

    SERVER_CREDENTIALS_INVALID(-89),
    SERVER_API_DISABLED(-88),
    SERVER_OPT_TRIGGERS_DISABLED(-86),
    ERROR_RECOVERABLE(-20),
    SERVER_DAEMON_NOT_RUNNING(-19),
    BINDING_CONNECTION_TIMEOUT(-1),
    UNINITIALIZED(0),
    BINDING_CONFIG_LOAD_PASSED(1),
    BINDING_CONFIG_VALIDATE_PASSED(2),
    ZONEMINDER_CONNECTION_CREATED(3),
    ZONEMINDER_API_ACCESS_PASSED(4),
    ZONEMINDER_SESSION_CREATED(5),
    ZONEMINDER_SERVER_CONFIG_PASSED(6),
    INITIALIZED(10);

    private int value;
    private static Map map = new HashMap<>();

    private ZoneMinderConnectionStatus(int value) {
        this.value = value;
    }

    public static ZoneMinderConnectionStatus valueOf(int pageType) {
        return (ZoneMinderConnectionStatus) map.get(pageType);
    }

    public boolean lessThan(ZoneMinderConnectionStatus reference) {
        return (getValue() < reference.getValue()) ? true : false;
    }

    public boolean greatherThan(ZoneMinderConnectionStatus reference) {
        return (getValue() > reference.getValue()) ? true : false;
    }

    public boolean greatherThanEqual(ZoneMinderConnectionStatus reference) {
        return (getValue() >= reference.getValue()) ? true : false;
    }

    public boolean isErrorState() {
        return lessThan(UNINITIALIZED);
    }

    public boolean hasUnrecoverableError() {
        return lessThan(ERROR_RECOVERABLE);
    }

    public boolean hasPassed(ZoneMinderConnectionStatus reference) {
        return greatherThanEqual(reference);
    }

    public int getValue() {
        return value;
    }

    public boolean hasRecoverableError() {
        return isErrorState() && !hasUnrecoverableError();
    }
}
