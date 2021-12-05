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

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.common.registry.AbstractProvider;
import org.openhab.core.common.registry.Provider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemProvider;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.model.item.BindingConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Item provider which binds items based on metadata they have attached
 *
 * @author Jonathan Gilbert
 */
@NonNullByDefault
public class BindingItemProviderDelegate extends AbstractProvider<Item> implements ItemProvider {

    private final Logger logger = LoggerFactory.getLogger(BindingItemProviderDelegate.class);

    private ItemProvider delegate;
    private String contextName;
    private boolean bound = false; // binds lazily

    private BindingSupport bindingSupport;

    BindingItemProviderDelegate(String contextName, ItemProvider delegate, BindingSupport bindingSupport) {
        this.delegate = delegate;
        this.contextName = contextName;
        this.bindingSupport = bindingSupport;
        delegate.addProviderChangeListener(new ProviderChangeListener<Item>() {
            @Override
            public void added(Provider<Item> provider, Item item) {
                bindAll();
                notifyListenersAboutAddedElement(item);
            }

            @Override
            public void removed(Provider<Item> provider, Item item) {
                bindAll();
                notifyListenersAboutRemovedElement(item);
            }

            @Override
            public void updated(Provider<Item> provider, Item oldItem, Item newItem) {
                bindAll();
                notifyListenersAboutUpdatedElement(oldItem, newItem);
            }
        });
    }

    private void bindAll() {
        for (BindingConfigReader reader : bindingSupport.getBindingConfigReaders().values()) {
            bindForReader(reader);
        }
    }

    private void bindForReader(BindingConfigReader reader) {
        reader.startConfigurationUpdate(contextName);

        for (Item item : delegate.getAll()) {
            Metadata metadata = bindingSupport.getMetadataRegistry()
                    .get(new MetadataKey(reader.getBindingType(), item.getName()));
            if (metadata != null) {
                bindItemForReader(reader, item, metadata.getValue());
            }
        }

        reader.stopConfigurationUpdate(contextName);
    }

    private void bindItemForReader(BindingConfigReader reader, Item item, String config) {
        try {
            reader.validateItemType(item.getType(), config);
            reader.processBindingConfiguration(contextName, item.getType(), item.getName(), config,
                    new Configuration());
        } catch (Exception e) {
            logger.error("Binding configuration of type '{}' of item '{}' could not be parsed correctly.",
                    reader.getBindingType(), item.getName(), e);
        } // Catch badly behaving binding exceptions and continue processing
    }

    @Override
    public Collection<Item> getAll() {

        if (!bound) {
            bindAll();
            bound = true;
        }

        return delegate.getAll();
    }
}
