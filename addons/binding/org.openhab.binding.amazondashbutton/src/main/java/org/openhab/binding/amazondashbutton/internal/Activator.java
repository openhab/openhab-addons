package org.openhab.binding.amazondashbutton.internal;

import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder;
import org.openhab.binding.amazondashbutton.internal.config.AmazonDashButtonConfigDescriptionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private ServiceRegistration<?> configDescriptionProviderReg;

    private ServiceBinder configDescriptionI18nProviderServiceBinder;

    @Override
    public void start(BundleContext context) throws Exception {
        AmazonDashButtonConfigDescriptionProvider configDescriptionProvider = new AmazonDashButtonConfigDescriptionProvider();
        this.configDescriptionI18nProviderServiceBinder = new ServiceBinder(context, configDescriptionProvider);
        this.configDescriptionI18nProviderServiceBinder.open();

        this.configDescriptionProviderReg = context.registerService(ConfigDescriptionProvider.class.getName(),
                configDescriptionProvider, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.configDescriptionProviderReg.unregister();
        this.configDescriptionProviderReg = null;

        this.configDescriptionI18nProviderServiceBinder.close();
    }

}
