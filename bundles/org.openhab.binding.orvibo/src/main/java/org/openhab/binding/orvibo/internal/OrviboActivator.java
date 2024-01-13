/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.orvibo.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.github.tavalin.s20.S20Client;

/**
 * Bundle activator for the Orvibo Binding.
 *
 * @author Daniel Walters - Initial contribution
 *
 */
public class OrviboActivator implements BundleActivator {

    private S20Client s20Client;

    @Override
    public void start(BundleContext context) throws Exception {
        s20Client = S20Client.getInstance();
        s20Client.connect();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        s20Client.disconnect();
    }
}
