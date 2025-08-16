/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.exception.MercedesMeAuthException;
import org.openhab.binding.mercedesme.internal.handler.AccountHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.storage.Storage;

/**
 * {@link WebsocketMock} to mock API behavior for testing
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class WebsocketMock extends Websocket {

    public WebsocketMock(AccountHandler atrl, HttpClient hc, AccountConfiguration ac, LocaleProvider l,
            Storage<String> store) {
        super(atrl, hc, ac, l, store);
    }

    @Override
    public void login() throws MercedesMeAuthException {
        // deny login for unit tests
        throw new MercedesMeAuthException(
                "Login not allowed for test user " + config.email + ". Use mock handler instead.");
    }
}
