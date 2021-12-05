/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.automation.jsscripting.internal.scope.binding;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.ItemProvider;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.model.item.BindingConfigReader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to allowing wrapping of basic ItemProviders to add functionality for them to bind their items.
 *
 * @author Jonathan Gilbert
 */
@Component(service = BindingItemProviderDelegateFactory.class)
@NonNullByDefault
public class BindingItemProviderDelegateFactory {

    private final Logger logger = LoggerFactory.getLogger(BindingItemProviderDelegateFactory.class);

    private Map<String, BindingConfigReader> bindingConfigReaders = new HashMap<>();
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;

    private BindingSupport bindingSupport = new BindingSupport() {
        @Override
        public MetadataRegistry getMetadataRegistry() {
            return metadataRegistry;
        }

        @Override
        public Map<String, BindingConfigReader> getBindingConfigReaders() {
            return bindingConfigReaders;
        }
    };

    public BindingItemProviderDelegate create(String contextName, ItemProvider delegate) {
        return new BindingItemProviderDelegate(contextName, delegate, bindingSupport);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    public void setMetadataRegistry(MetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addBindingConfigReader(BindingConfigReader reader) {
        if (!bindingConfigReaders.containsKey(reader.getBindingType())) {
            bindingConfigReaders.put(reader.getBindingType(), reader);
        } else {
            logger.warn("Attempted to register a second BindingConfigReader of type '{}'."
                    + " The primary reader will remain active!", reader.getBindingType());
        }
    }

    public void removeBindingConfigReader(BindingConfigReader reader) {
        if (bindingConfigReaders.get(reader.getBindingType()).equals(reader)) {
            bindingConfigReaders.remove(reader.getBindingType());
        }
    }
}
