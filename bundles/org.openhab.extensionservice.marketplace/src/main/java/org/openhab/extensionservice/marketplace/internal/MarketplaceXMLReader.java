/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.extensionservice.marketplace.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.openhab.extensionservice.marketplace.internal.model.Category;
import org.openhab.extensionservice.marketplace.internal.model.Marketplace;
import org.openhab.extensionservice.marketplace.internal.model.Node;

import com.thoughtworks.xstream.XStream;

/**
 * This is the XML reader for the marketplace content.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public class MarketplaceXMLReader extends XmlDocumentReader<Marketplace> {

    public MarketplaceXMLReader() {
        super.setClassLoader(Marketplace.class.getClassLoader());
    }

    @Override
    public void registerConverters(@NonNullByDefault({}) XStream xstream) {
    }

    @Override
    public void registerAliases(@NonNullByDefault({}) XStream xstream) {
        xstream.alias("marketplace", Marketplace.class);
        xstream.addImplicitArray(Marketplace.class, "categories");
        xstream.alias("category", Category.class);
        xstream.addImplicitArray(Category.class, "nodes");
        xstream.alias("node", Node.class);
        xstream.aliasAttribute(Node.class, "id", "id");
        xstream.aliasAttribute(Node.class, "name", "name");

        // ignore what we do not know
        xstream.ignoreUnknownElements();
    }
}
