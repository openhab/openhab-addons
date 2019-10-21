/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.voice.opennlp.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.eclipse.smarthome.core.voice.text.ItemNamedAttribute;
import org.eclipse.smarthome.core.voice.text.ItemNamedAttribute.AttributeSource;
import org.eclipse.smarthome.core.voice.text.ItemResolver;
import org.eclipse.smarthome.core.voice.text.UnsupportedLanguageException;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class retrieves and caches sets of {@link ItemNamedAttribute} mapped to items.
 * It uses semantic tags and additional monikers defined in metadata ("habot" namespace).
 * The named attributes sourced from tags will be translated to the current language as
 * specified. Inheritance is always applied for metadata, while for tags it can be prevented
 * with the "inheritTags" configuration property in the "habot" metadata namespace.
 *
 * @author Yannick Schaus - Initial contribution
 */
// @Component(service = ItemResolver.class, immediate = true)
public class NamedAttributesItemResolver implements ItemResolver {

    private final Logger logger = LoggerFactory.getLogger(NamedAttributesItemResolver.class);

    private static final Set<String> LOCATION_CATEGORIES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("cellar", "livingroom", "kitchen", "bedroom", "bath", "toilet",
                    "closet", "dressing", "office", "groundfloor", "firstfloor", "attic", "corridor", "garage",
                    "garden", "terrace", "greenhouse", "pantry")));

    private ItemRegistry itemRegistry;
    private MetadataRegistry metadataRegistry;
    private Map<Item, Set<ItemNamedAttribute>> itemAttributes;
    private Locale currentLocale = null;
    ResourceBundle tagAttributes;

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.ui.habot.nlp.internal.ItemResolver#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale locale) {
        if (!locale.equals(currentLocale)) {
            this.currentLocale = locale;
            logger.debug("Language set to: {} - invalidating cached item named attributes", locale.getLanguage());
            this.itemAttributes = null;
            this.tagAttributes = ResourceBundle.getBundle("tagattributes", locale,
                    ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));

        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.ui.habot.nlp.internal.ItemResolver#getAllItemNamedAttributes()
     */
    @Override
    public Map<Item, Set<ItemNamedAttribute>> getAllItemNamedAttributes() throws UnsupportedLanguageException {
        if (currentLocale == null) {
            throw new UnsupportedLanguageException(currentLocale);
        }

        if (itemAttributes == null) {
            updateItemNamedAttributes();
        }

        return itemAttributes;
    }

    /**
     * Returns the set of named attributes associated to the provided item
     *
     * @param item the item to look up
     * @return a set of named attributes or null if the item doesn't have any named attributes
     * @throws UnsupportedLanguageException
     */
    public Set<ItemNamedAttribute> getItemNamedAttributes(Item item) throws UnsupportedLanguageException {
        if (currentLocale == null) {
            throw new UnsupportedLanguageException(currentLocale);
        }

        if (itemAttributes == null) {
            updateItemNamedAttributes();
        }

        return itemAttributes.get(item);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.ui.habot.nlp.internal.ItemResolver#getMatchingItems(java.lang.String, java.lang.String)
     */
    @Override
    public Stream<Item> getMatchingItems(String object, String location) {
        return itemAttributes.entrySet().stream().filter(entry -> {
            boolean objectMatch = false;
            boolean locationMatch = false;

            if (object != null && entry.getValue().stream().anyMatch(a -> a.getValue().equalsIgnoreCase(object))) {
                objectMatch = true;
            }
            if (location != null && entry.getValue().stream().anyMatch(a -> a.getValue().equalsIgnoreCase(location))) {
                locationMatch = true;
            }

            return (object != null && location != null) ? objectMatch && locationMatch : objectMatch || locationMatch;
        }).map(entry -> entry.getKey());
    }

    private void updateItemNamedAttributes() {
        itemAttributes = new HashMap<Item, Set<ItemNamedAttribute>>();
        for (Item item : itemRegistry.getAll()) {

            Metadata metadata = metadataRegistry.get(new MetadataKey("habot", item.getName()));
            boolean inheritAttributes = true;
            if (metadata != null && metadata.getConfiguration().containsKey("inheritAttributes")) {
                inheritAttributes = (boolean) metadata.getConfiguration().get("inheritAttributes");
            }

            // look for semantic tags
            if (!item.getTags().isEmpty()) {
                for (String tag : item.getTags()) {
                    if (tag.split(":").length != 2) {
                        continue;
                    }

                    String type = tag.startsWith("location:") ? "location" : "object";

                    String semanticTagNamedAttributes;
                    try {
                        semanticTagNamedAttributes = this.tagAttributes.getString(tag.split(":")[1].toLowerCase());
                        for (String tagAttribute : semanticTagNamedAttributes.split(",")) {
                            addItemAttribute(item, type, tagAttribute.trim(), AttributeSource.TAG, false,
                                    inheritAttributes);
                        }
                    } catch (MissingResourceException e) {
                        logger.debug("No named attributes found for tag {}", tag);
                    }
                }
            } else {
                String category = item.getCategory();
                if (category != null) {
                    if (metadata != null && metadata.getConfiguration().containsKey("useCategory")
                            && metadata.getConfiguration().get("useCategory").equals(false)) {
                        logger.info("Ignoring category for item {}", item.getName());
                    } else {
                        category = category.toLowerCase();
                        String categoryNamedAttributes;
                        try {
                            categoryNamedAttributes = this.tagAttributes.getString(category);
                            for (String tagAttribute : categoryNamedAttributes.split(",")) {
                                addItemAttribute(item, LOCATION_CATEGORIES.contains(category) ? "location" : "object",
                                        tagAttribute.trim(), AttributeSource.CATEGORY, false, inheritAttributes);
                            }
                        } catch (MissingResourceException e) {
                            logger.debug("No named attributes found for category {}", category);
                        }
                    }
                }
            }

            // look for additional comma-separated item monikers in the "habot" metadata namespace
            if (metadata != null && !(metadata.getValue().isEmpty())) {
                String type = "object";

                if (item instanceof GroupItem && item.getTags().stream().anyMatch(t -> t.startsWith("location:"))) {
                    type = "location";
                }
                if (metadata.getConfiguration().containsKey("type")) {
                    type = metadata.getConfiguration().get("type").toString();
                }

                for (String moniker : metadata.getValue().split(",")) {
                    addItemAttribute(item, type, moniker.trim(), AttributeSource.METADATA, false, inheritAttributes);
                }
            }
        }
    }

    private void addItemAttribute(Item item, String type, String value, AttributeSource source, boolean isInherited,
            boolean inheritToGroupMembers) {
        Set<ItemNamedAttribute> attributes = itemAttributes.get(item);
        if (attributes == null) {
            attributes = new HashSet<ItemNamedAttribute>();
        }

        ItemNamedAttribute attribute = new ItemNamedAttribute(type, value, isInherited, source);
        attributes.add(attribute);

        if (item instanceof GroupItem && inheritToGroupMembers) {
            for (Item groupMemberItem : ((GroupItem) item).getMembers(i -> true)) {
                addItemAttribute(groupMemberItem, type, value, source, true, true);
            }
        }

        itemAttributes.put(item, attributes);
    }

    @Reference
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        if (this.itemRegistry == null) {
            this.itemRegistry = itemRegistry;
            this.itemRegistry.addRegistryChangeListener(registryChangeListener);
        }
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        if (itemRegistry == this.itemRegistry) {
            this.itemRegistry.removeRegistryChangeListener(registryChangeListener);
            this.itemRegistry = null;
        }
    }

    @Reference
    protected void setMetadataRegistry(MetadataRegistry metadataRegistry) {
        if (this.metadataRegistry == null) {
            this.metadataRegistry = metadataRegistry;
            this.metadataRegistry.addRegistryChangeListener(metadataRegistryChangeListener);
        }
    }

    protected void unsetMetadataRegistry(MetadataRegistry metadataRegistry) {
        if (metadataRegistry == this.metadataRegistry) {
            this.metadataRegistry.removeRegistryChangeListener(metadataRegistryChangeListener);
            this.metadataRegistry = null;
        }
    }

    private @NonNull RegistryChangeListener<Item> registryChangeListener = new RegistryChangeListener<Item>() {
        @Override
        public void added(Item element) {
            logger.debug("Invalidating cached item named attributes");
            itemAttributes = null;
        }

        @Override
        public void removed(Item element) {
            logger.debug("Invalidating cached item named attributes");
            itemAttributes = null;
        }

        @Override
        public void updated(Item oldElement, Item element) {
            logger.debug("Invalidating cached item named attributes");
            itemAttributes = null;
        }
    };

    private @NonNull RegistryChangeListener<Metadata> metadataRegistryChangeListener = new RegistryChangeListener<Metadata>() {
        @Override
        public void added(Metadata element) {
            if (element.getUID().getNamespace() == "habot") {
                logger.debug("Invalidating cached item named attributes");
                itemAttributes = null;
            }
        }

        @Override
        public void removed(Metadata element) {
            if (element.getUID().getNamespace() == "habot") {
                logger.debug("Invalidating cached item named attributes");
                itemAttributes = null;
            }
        }

        @Override
        public void updated(Metadata oldElement, Metadata element) {
            if (element.getUID().getNamespace() == "habot") {
                logger.debug("Invalidating cached item named attributes");
                itemAttributes = null;
            }
        }
    };
}
