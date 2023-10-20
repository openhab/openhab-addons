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
package org.openhab.misc.addonsuggestionfinder.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.addon.AddonDiscoveryMethod;
import org.openhab.core.addon.AddonInfo;
import org.openhab.core.addon.AddonMatchProperty;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Serializer/deserializer for addon suggestion finder.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class AddonListSerializer {

    private final XStream xstream;

    public AddonListSerializer() {
        xstream = new XStream(new StaxDriver());

        xstream.ignoreUnknownElements();
        xstream.allowTypesByWildcard(new String[] { "org.openhab.**" });

        xstream.alias("addons", AddonInfoList.class);
        xstream.addImplicitCollection(AddonInfoList.class, "addonInfos", "addon", AddonInfo.class);

        xstream.alias("addon", AddonInfo.class);
        xstream.useAttributeFor(AddonInfo.class, "id");
        xstream.addImplicitCollection(AddonInfo.class, "discoveryMethods", "discovery-method",
                AddonDiscoveryMethod.class);

        xstream.alias("discovery-method", AddonDiscoveryMethod.class);
        xstream.aliasField("service-type", AddonDiscoveryMethod.class, "serviceType");
        xstream.aliasField("mdns-service-type", AddonDiscoveryMethod.class, "mdnsServiceType");
        xstream.addImplicitCollection(AddonDiscoveryMethod.class, "matchProperties", "match-property",
                AddonMatchProperty.class);
    }

    public AddonInfoList fromXML(String xml) {
        return (AddonInfoList) xstream.fromXML(xml);
    }

    public String toXML(AddonInfoList addons) {
        return xstream.toXML(addons);
    }
}
