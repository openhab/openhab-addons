/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.orvibo.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.github.tavalin.orvibo.OrviboClient;

/**
 * Bundle activator for the Orvibo Binding.
 *
 * @author Daniel Walters - Initial contribution
 *
 */
public class OrviboActivator implements BundleActivator {

    private OrviboClient client;

    @Override
    public void start(BundleContext context) throws Exception {
        client = OrviboClient.getInstance();
        client.connect();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        client.disconnect();
    }

}
