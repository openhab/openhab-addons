package org.openhab.binding.insteonplm.internal.config;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handlers the configuration of the insteon system.
 */
public class InsteonConfigProvider implements ConfigDescriptionProvider, ConfigOptionProvider {
    private static Logger logger = LoggerFactory.getLogger(InsteonConfigProvider.class);

    private static ThingRegistry thingRegistry;
    private static ThingTypeRegistry thingTypeRegistry;
    private static ConfigDescriptionRegistry configDescriptionRegistry;

    private static List<InsteonProduct> productIndex;

    private static Set<ThingTypeUID> insteonThingTypeUIDList = new HashSet<ThingTypeUID>();

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        InsteonConfigProvider.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        InsteonConfigProvider.thingRegistry = null;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        InsteonConfigProvider.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        InsteonConfigProvider.thingTypeRegistry = null;
    }

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        InsteonConfigProvider.configDescriptionRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        InsteonConfigProvider.configDescriptionRegistry = null;
    }

    public static synchronized List<InsteonProduct> getProductIndex() {
        if (InsteonConfigProvider.productIndex == null) {
            loadProductIndex();
        }
        return InsteonConfigProvider.productIndex;
    }

    private static void loadProductIndex() {
        synchronized (InsteonConfigProvider.productIndex) {
            Collection<ThingType> thingTypes = thingTypeRegistry.getThingTypes();
            for (ThingType thingType : thingTypes) {
                // Make sure it belongs to us.
                if (!InsteonPLMBindingConstants.BINDING_ID.equals(thingType.getBindingId())) {
                    continue;
                }

                // Add to the list of things we support.
                insteonThingTypeUIDList.add(thingType.getUID());

                Map<String, String> thingProperties = thingType.getProperties();
                String productkey = thingProperties.get(InsteonPLMBindingConstants.PROPERTY_INSTEON_PRODUCT_KEY);
                if (productkey == null) {
                    logger.error("Invalid insteon thing in registry {}", thingType.getUID().toString());
                    continue;
                }

                String model = thingProperties.get(InsteonPLMBindingConstants.PROPERTY_INSTEON_MODEL);
                if (model == null) {
                    logger.error("Invalid model for thing in registry {}", thingType.getUID().toString());
                    continue;
                }
                InsteonProduct product = new InsteonProduct(thingType.getUID(), productkey, model);
                InsteonConfigProvider.productIndex.add(product);
            }
        }
    }

    public static Set<ThingTypeUID> getSupportedThingTypes() {
        return insteonThingTypeUIDList;
    }

    public static ThingType getThingType(ThingTypeUID thingTypeUID) {
        if (thingTypeRegistry == null) {
            return null;
        }
        return thingTypeRegistry.getThingType(thingTypeUID);
    }

    public static InsteonProduct getInsteonProduct(String productKey) {
        for (InsteonProduct product : productIndex) {
            if (product.match(productKey)) {
                return product;
            }
        }
        return null;
    }

    public static ThingType getThingType(String productKey) {
        if (thingTypeRegistry == null) {
            return null;
        }
        InsteonProduct product = getInsteonProduct(productKey);
        if (product != null) {
            return thingTypeRegistry.getThingType(product.getThingTypeUID());
        }
        return null;
    }

    public static Thing getThing(ThingUID thingUID) {
        if (thingRegistry == null) {
            return null;
        }
        return thingRegistry.get(thingUID);
    }
}
