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
package org.openhab.binding.telegram.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TelegramConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jens Runge - Initial contribution
 */
@NonNullByDefault
public class TelegramConfiguration {

    /**
     * Sample configuration parameter. Replace with your own.
     */
    private @Nullable String botUsername;
    private @Nullable String botToken;
    private @Nullable List<String> chatIds;
    private @Nullable String proxyHost;
    private @Nullable Integer proxyPort;
    private @Nullable String proxyType;
    private String parseMode = "";
    private int longPollingTime;

    public @Nullable String getBotUsername() {
        return botUsername;
    }

    public @Nullable String getBotToken() {
        return botToken;
    }

    public @Nullable List<String> getChatIds() {
        return chatIds;
    }

    public String getParseMode() {
        return parseMode;
    }

    public @Nullable String getProxyHost() {
        return proxyHost;
    }

    public @Nullable Integer getProxyPort() {
        return proxyPort;
    }

    public @Nullable String getProxyType() {
        return proxyType;
    }

    public int getLongPollingTime() {
        return longPollingTime;
    }
}
