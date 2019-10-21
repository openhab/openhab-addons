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
package org.openhab.binding.telegram.internal;

import java.util.List;

/**
 * The {@link TelegramConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jens Runge - Initial contribution
 */
public class TelegramConfiguration {

    /**
     * Sample configuration parameter. Replace with your own.
     */
    private String botUsername, botToken;
    private List<String> chatIds;
    private String parseMode;

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotToken() {
        return botToken;
    }

    public List<String> getChatIds() {
        return chatIds;
    }

    public String getParseMode() {
        return parseMode;
    }

}
