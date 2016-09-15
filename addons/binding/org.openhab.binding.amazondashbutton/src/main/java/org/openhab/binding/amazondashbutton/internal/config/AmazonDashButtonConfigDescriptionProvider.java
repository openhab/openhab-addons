/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.internal.config;

import static org.openhab.binding.amazondashbutton.AmazonDashButtonBindingConstants.DASH_BUTTON_THING_TYPE;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.config.core.i18n.ConfigI18nLocalizationService;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Bind;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Unbind;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.amazondashbutton.internal.PcapUtil;
import org.osgi.framework.FrameworkUtil;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapNetworkInterface;

/**
 * The {@link AmazonDashButtonConfigDescriptionProvider} provides {@link ConfigDescription}s which can not be modeled by
 * using the thing-description XML schema.
 *
 * In particular it determines the available pcap network interfaces in order to provide an option list.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class AmazonDashButtonConfigDescriptionProvider implements ConfigDescriptionProvider {

    private ConfigI18nLocalizationService configI18nLocalizerService;

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        return null;
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        if ("thing-type".equals(uri.getScheme())) {
            ThingTypeUID thingtypeUID = new ThingTypeUID(uri.getSchemeSpecificPart());
            if (thingtypeUID.equals(DASH_BUTTON_THING_TYPE)) {
                return tryLocalization(createNetworkInterfaceConfigDescription(uri), locale);
            }
        }
        return null;
    }

    private String getLabel(PcapNetworkInterface pcapNetworkInterface) {
        StringBuilder sb = new StringBuilder(pcapNetworkInterface.getName());
        List<PcapAddress> addresses = pcapNetworkInterface.getAddresses();
        final String description = pcapNetworkInterface.getDescription();
        Set<String> paramStrings = new LinkedHashSet<>();
        if (description != null && !description.isEmpty()) {
            paramStrings.add(description);
        }
        for (PcapAddress address : addresses) {
            paramStrings.add(address.getAddress().toString().substring(1));

        }

        boolean hasParams = !paramStrings.isEmpty();
        if (hasParams) {
            sb.append(" (");
        }

        for (Iterator<String> paramIterator = paramStrings.iterator(); paramIterator.hasNext();) {
            String addressString = paramIterator.next();
            sb.append(addressString);
            if (paramIterator.hasNext()) {
                sb.append(", ");
            }
        }
        if (hasParams) {
            sb.append(")");
        }
        return sb.toString();
    }

    private ConfigDescription createNetworkInterfaceConfigDescription(URI uri) {
        List<PcapNetworkInterface> pcapNetworkInterfaces = PcapUtil.getAllNetworkInterfaces();
        List<ParameterOption> options = new ArrayList<>();
        for (PcapNetworkInterface pcapNetworkInterface : pcapNetworkInterfaces) {
            String name = pcapNetworkInterface.getName();

            options.add(new ParameterOption(name, getLabel(pcapNetworkInterface)));
        }
        ConfigDescriptionParameter configDescriptionParameter = ConfigDescriptionParameterBuilder
                .create("pcapNetworkInterfaceName", Type.TEXT).withLabel("@text/dashButtonNetworkInterfaceLabel")
                .withDescription("@text/dashButtonNetworkInterfaceDescription").withOptions(options).build();
        ConfigDescription configDescription = new ConfigDescription(uri,
                Collections.singletonList(configDescriptionParameter));
        return configDescription;
    }

    @Bind
    public void setConfigI18nLocalizerService(ConfigI18nLocalizationService configI18nLocalizerService) {
        this.configI18nLocalizerService = configI18nLocalizerService;
    }

    @Unbind
    public void unsetConfigI18nLocalizerService(ConfigI18nLocalizationService configI18nLocalizerService) {
        this.configI18nLocalizerService = null;
    }

    private ConfigDescription tryLocalization(final ConfigDescription configDescription, final Locale locale) {
        if (configI18nLocalizerService == null) {
            return configDescription;
        } else {
            return configI18nLocalizerService.getLocalizedConfigDescription(
                    FrameworkUtil.getBundle(AmazonDashButtonConfigDescriptionProvider.class), configDescription,
                    locale);
        }
    }

}
