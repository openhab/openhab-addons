/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.internal.config;

import static org.openhab.binding.amazondashbutton.AmazonDashButtonBindingConstants.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.amazondashbutton.AmazonDashButtonBindingConstants;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceWrapper;
import org.openhab.binding.amazondashbutton.internal.pcap.PcapNetworkInterfaceService;
import org.pcap4j.core.PcapAddress;

/**
 * The {@link AmazonDashButtonConfigOptionProvider} is responsible for providing options for the
 * {@link AmazonDashButtonBindingConstants#PROPERTY_NETWORK_INTERFACE_NAME} property.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class AmazonDashButtonConfigOptionProvider implements ConfigOptionProvider {

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if ("thing-type".equals(uri.getScheme())) {
            ThingTypeUID thingtypeUID = new ThingTypeUID(uri.getSchemeSpecificPart());
            if (thingtypeUID.equals(DASH_BUTTON_THING_TYPE) && PROPERTY_NETWORK_INTERFACE_NAME.equals(param)) {
                return getPcapNetworkInterfacesOptions();
            }
        }
        return Collections.emptyList();
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
