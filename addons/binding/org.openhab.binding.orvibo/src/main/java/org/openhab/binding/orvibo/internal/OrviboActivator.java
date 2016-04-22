package org.openhab.binding.orvibo.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.github.tavalin.s20.S20Client;

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
