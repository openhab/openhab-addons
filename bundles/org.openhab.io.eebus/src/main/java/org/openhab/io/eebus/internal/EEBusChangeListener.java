/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.eebus.internal;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemRegistryChangeListener;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openmuc.jeebus.spine.api.Entity;
import org.openmuc.jeebus.usecase.powerlimitation.controllablesystem.ActiveLimit;
import org.openmuc.jeebus.usecase.powerlimitation.controllablesystem.lpc.LpcCs;
import org.openmuc.jeebus.usecase.powerlimitation.controllablesystem.lpp.LppCs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches the item and metadata registries for {@code eebus="lpc"}/{@code eebus="lpp"} tagged
 * items, and wires the corresponding EEBus use case to whichever item is tagged - pushing every
 * active-limit update from a paired CEM/CLS gateway directly onto that item as a command.
 * Analogous to {@code org.openhab.io.homekit.internal.HomekitChangeListener}, simplified since
 * EEBus's LPC/LPP are household-wide singleton limits (at most one tagged item each makes sense),
 * not a many-accessories registry.
 * <p>
 * v1 limitation: once LPC/LPP is bound to an item, un-tagging or re-tagging to a different item
 * is logged but not applied live - jeebus.spine's {@link Entity#addUseCase} has no runtime
 * counterpart for removing a use case, so rebinding needs a restart of this add-on.
 *
 * @author openHAB EEBus Add-on Contributors - Initial contribution
 */
@NonNullByDefault
public class EEBusChangeListener implements ItemRegistryChangeListener {

    static final String METADATA_NAMESPACE = "eebus";
    static final String LPC_VALUE = "lpc";
    static final String LPP_VALUE = "lpp";
    static final String COMMAND_SOURCE = "org.openhab.io.eebus";

    private final Logger logger = LoggerFactory.getLogger(EEBusChangeListener.class);

    private final ItemRegistry itemRegistry;
    private final MetadataRegistry metadataRegistry;
    private final EventPublisher eventPublisher;
    private final Entity entity;

    private final RegistryChangeListener<Metadata> metadataChangeListener;

    private @Nullable String lpcItemName;
    private @Nullable String lppItemName;

    public EEBusChangeListener(ItemRegistry itemRegistry, MetadataRegistry metadataRegistry,
            EventPublisher eventPublisher, Entity entity) {
        this.itemRegistry = itemRegistry;
        this.metadataRegistry = metadataRegistry;
        this.eventPublisher = eventPublisher;
        this.entity = entity;

        metadataChangeListener = new RegistryChangeListener<>() {
            @Override
            public void added(Metadata metadata) {
                onMetadataChanged(metadata);
            }

            @Override
            public void removed(Metadata metadata) {
                onMetadataRemoved(metadata);
            }

            @Override
            public void updated(Metadata oldMetadata, Metadata newMetadata) {
                onMetadataUpdated(newMetadata);
            }
        };

        itemRegistry.addRegistryChangeListener(this);
        metadataRegistry.addRegistryChangeListener(metadataChangeListener);

        for (Item item : itemRegistry.getItems()) {
            Metadata metadata = metadataRegistry.get(new MetadataKey(METADATA_NAMESPACE, item.getUID()));
            if (metadata != null) {
                bind(metadata);
            }
        }
    }

    public void stop() {
        itemRegistry.removeRegistryChangeListener(this);
        metadataRegistry.removeRegistryChangeListener(metadataChangeListener);
    }

    @Override
    public void added(Item item) {
        Metadata metadata = metadataRegistry.get(new MetadataKey(METADATA_NAMESPACE, item.getUID()));
        if (metadata != null) {
            bind(metadata);
        }
    }

    @Override
    public void removed(Item item) {
        // Handled via the metadata registry's own removed() callback, which fires when an item
        // (and its metadata with it) is removed.
    }

    @Override
    public void updated(Item oldItem, Item newItem) {
        // Item type/label changes don't affect EEBus binding; metadata changes are handled
        // separately.
    }

    @Override
    public void allItemsChanged(java.util.Collection<String> oldItemNames) {
        // A full registry reload - re-scan for tagged items still present. Deliberately NOT
        // resetting lpcItemName/lppItemName first: jeebus.spine's Entity#addUseCase has no runtime
        // counterpart for removing a use case (see class javadoc), so if the same item is still
        // tagged after the reload, bind() below must see it as already-bound and no-op rather than
        // registering a second LpcCs/LppCs for a use case the entity already has attached.
        for (Item item : itemRegistry.getItems()) {
            Metadata metadata = metadataRegistry.get(new MetadataKey(METADATA_NAMESPACE, item.getUID()));
            if (metadata != null) {
                bind(metadata);
            }
        }
    }

    private void onMetadataChanged(Metadata metadata) {
        if (METADATA_NAMESPACE.equalsIgnoreCase(metadata.getUID().getNamespace())) {
            bind(metadata);
        }
    }

    private void onMetadataUpdated(Metadata metadata) {
        if (!METADATA_NAMESPACE.equalsIgnoreCase(metadata.getUID().getNamespace())) {
            return;
        }
        String itemName = metadata.getUID().getItemName();
        if (itemName.equals(lpcItemName) || itemName.equals(lppItemName)) {
            // bind() would silently no-op here (existing.equals(itemName) branch) since jeebus.spine
            // has no API to reconfigure an already-registered use case - warn instead of pretending
            // the new nominalMax/failsafeLimit/failsafeDuration took effect.
            logger.warn(
                    "EEBus: metadata configuration for item {} changed, but the use case is already bound - restart this add-on to apply the new nominalMax/failsafeLimit/failsafeDuration",
                    itemName);
            return;
        }
        bind(metadata);
    }

    private void onMetadataRemoved(Metadata metadata) {
        MetadataKey uid = metadata.getUID();
        if (!METADATA_NAMESPACE.equalsIgnoreCase(uid.getNamespace())) {
            return;
        }
        String itemName = uid.getItemName();
        if (itemName.equals(lpcItemName)) {
            logger.warn(
                    "EEBus: item {} was untagged from eebus=\"lpc\" - LPC stays bound to it until this add-on restarts",
                    itemName);
        }
        if (itemName.equals(lppItemName)) {
            logger.warn(
                    "EEBus: item {} was untagged from eebus=\"lpp\" - LPP stays bound to it until this add-on restarts",
                    itemName);
        }
    }

    private synchronized void bind(Metadata metadata) {
        String itemName = metadata.getUID().getItemName();
        String value = metadata.getValue();
        Map<String, Object> config = metadata.getConfiguration();

        if (LPC_VALUE.equalsIgnoreCase(value)) {
            bindLpc(itemName, config);
        } else if (LPP_VALUE.equalsIgnoreCase(value)) {
            bindLpp(itemName, config);
        } else {
            logger.warn("EEBus: item {} has metadata value '{}', expected \"lpc\" or \"lpp\" - ignoring", itemName,
                    value);
        }
    }

    private void bindLpc(String itemName, Map<String, Object> config) {
        String existing = lpcItemName;
        if (existing != null) {
            if (!existing.equals(itemName)) {
                logger.warn(
                        "EEBus: item {} tagged eebus=\"lpc\" but LPC is already bound to {} - only one LPC item is supported at a time (restart to rebind)",
                        itemName, existing);
            }
            return;
        }
        LpcCs cs = new LpcCs(EEBusLimitationConfigFactory.lpc(config));
        cs.addListener((event, state, limit) -> pushLimit(itemName, limit));
        entity.addUseCase(cs);
        lpcItemName = itemName;
        logger.info("EEBus: LPC bound to item {}", itemName);
    }

    private void bindLpp(String itemName, Map<String, Object> config) {
        String existing = lppItemName;
        if (existing != null) {
            if (!existing.equals(itemName)) {
                logger.warn(
                        "EEBus: item {} tagged eebus=\"lpp\" but LPP is already bound to {} - only one LPP item is supported at a time (restart to rebind)",
                        itemName, existing);
            }
            return;
        }
        LppCs cs = new LppCs(EEBusLimitationConfigFactory.lpp(config));
        cs.addListener((event, state, limit) -> pushLimit(itemName, limit));
        entity.addUseCase(cs);
        lppItemName = itemName;
        logger.info("EEBus: LPP bound to item {}", itemName);
    }

    private void pushLimit(String itemName, ActiveLimit limit) {
        Double value = limit.getResultingValue();
        if (value == null) {
            return;
        }
        if (!"W".equals(limit.getUnit())) {
            logger.debug("EEBus: unexpected active-limit unit '{}' for item {}, expected W", limit.getUnit(), itemName);
        }
        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            logger.warn("EEBus: item {} no longer exists, cannot push limit", itemName);
            return;
        }
        Command command = new QuantityType<>(value, Units.WATT);
        eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command, COMMAND_SOURCE));
    }

    Optional<String> getLpcItemName() {
        return Optional.ofNullable(lpcItemName);
    }

    Optional<String> getLppItemName() {
        return Optional.ofNullable(lppItemName);
    }
}
