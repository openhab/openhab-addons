/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.fsinternetradio.test;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.binding.fsinternetradio.handler.FSInternetRadioHandler;
import org.eclipse.smarthome.core.thing.Thing;

/**
 * A mock of FSInternetRadioHandler to enable testing.
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class MockedRadioHandler extends FSInternetRadioHandler {

    public MockedRadioHandler(Thing thing, HttpClient client) {
        super(thing, client);
    }

    @Override
    protected boolean isLinked(String channelUID) {
        return true;
    }
}
