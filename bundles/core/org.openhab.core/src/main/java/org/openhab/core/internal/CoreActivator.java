/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.eclipse.smarthome.model.rule.runtime.RuleEngine;
import org.openhab.core.OpenHAB;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the activator of the core openHAB bundle.
 *
 * @author Kai Kreuzer
 * @author Thomas.Eichstaedt-Engelen
 *
 */
public class CoreActivator implements BundleActivator {

    private static Logger logger = LoggerFactory.getLogger(CoreActivator.class);

    @Override
    public void start(BundleContext context) throws Exception {
        String version = OpenHAB.getVersion();
        String buildId = OpenHAB.getBuildId();

        if (buildId.equals("")) {
            logger.info("openHAB runtime has been started (v{}).", version);
        } else {
            logger.info("openHAB runtime has been started (v{}, build {}).", version, buildId);
        }

        if (logger.isDebugEnabled()) {
            RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
            long uptime = rb.getUptime();
            logger.debug("Startup took {} ms", uptime);
        }

        startRuleEngine(context);
    }

    private void startRuleEngine(BundleContext context) throws InterruptedException {
        // TODO: This is a workaround as long as we cannot determine the time when all models have been loaded
        Thread.sleep(2000);

        // we now request the RuleEngine, so that it is activated and starts processing the rules
        // TODO: This should probably better be moved in a new bundle, so that the core bundle does
        // not have a (optional) dependency on model.rule.runtime anymore.
        try {
            ServiceTracker<RuleEngine, RuleEngine> tracker = new ServiceTracker<RuleEngine, RuleEngine>(context,
                    RuleEngine.class, null);
            tracker.open();
            tracker.waitForService(10000);
        } catch (NoClassDefFoundError e) {
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("openHAB runtime has been terminated.");
    }

}
