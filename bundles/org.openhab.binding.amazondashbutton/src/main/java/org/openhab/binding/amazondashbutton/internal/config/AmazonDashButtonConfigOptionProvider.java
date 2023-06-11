/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.amazondashbutton.internal.config;

import static org.openhab.binding.amazondashbutton.internal.AmazonDashButtonBindingConstants.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazondashbutton.internal.AmazonDashButtonBindingConstants;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceService;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceWrapper;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.component.annotations.Component;
import org.pcap4j.core.PcapAddress;

/**
 * The {@link AmazonDashButtonConfigOptionProvider} is responsible for providing options for the
 * {@link AmazonDashButtonBindingConstants#PROPERTY_NETWORK_INTERFACE_NAME} property.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
@Component(service = ConfigOptionProvider.class)
@NonNullByDefault
public class AmazonDashButtonConfigOptionProvider implements ConfigOptionProvider {

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if ("thing-type".equals(uri.getScheme())) {
            ThingTypeUID thingtypeUID = new ThingTypeUID(uri.getSchemeSpecificPart());
            if (thingtypeUID.equals(DASH_BUTTON_THING_TYPE) && PROPERTY_NETWORK_INTERFACE_NAME.equals(param)) {
                return getPcapNetworkInterfacesOptions();
            }
        }
        return null;
    }

    private Collection<ParameterOption> getPcapNetworkInterfacesOptions() {
        Set<PcapNetworkInterfaceWrapper> pcapNetworkInterfaces = PcapNetworkInterfaceService.instance()
                .getNetworkInterfaces();
        List<ParameterOption> options = new ArrayList<>();
        for (PcapNetworkInterfaceWrapper pcapNetworkInterface : pcapNetworkInterfaces) {
            String name = pcapNetworkInterface.getName();

            options.add(new ParameterOption(name, getLabel(pcapNetworkInterface)));
        }
        return options;
    }

    private String getLabel(PcapNetworkInterfaceWrapper pcapNetworkInterface) {
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
}
