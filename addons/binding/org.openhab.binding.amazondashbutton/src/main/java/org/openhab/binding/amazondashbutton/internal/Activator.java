package org.openhab.binding.amazondashbutton.internal;

import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder;
import org.openhab.binding.amazondashbutton.internal.config.AmazonDashButtonConfigDescriptionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link Activator} is responsible building a {@link ServiceBinder} which binds the
 * {@link AmazonDashButtonConfigDescriptionProvider}.
 * 
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class Activator implements BundleActivator {

    private ServiceRegistration<?> configDescriptionProviderReg;

    private ServiceBinder configDescriptionProviderServiceBinder;

    @Override
    public void start(BundleContext context) throws Exception {
        AmazonDashButtonConfigDescriptionProvider configDescriptionProvider = new AmazonDashButtonConfigDescriptionProvider();
        this.configDescriptionProviderServiceBinder = new ServiceBinder(context, configDescriptionProvider);
        this.configDescriptionProviderServiceBinder.open();

        this.configDescriptionProviderReg = context.registerService(ConfigDescriptionProvider.class.getName(),
                configDescriptionProvider, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.configDescriptionProviderReg.unregister();
        this.configDescriptionProviderReg = null;

        this.configDescriptionProviderServiceBinder.close();
    }

}
