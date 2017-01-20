/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.BINDING_ID;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.homie.HomieChannelTypeProvider;

public class HomieChannelTypeProviderImpl implements HomieChannelTypeProvider {

    private final Map<ChannelTypeUID, ChannelType> types = new HashMap<>();
    private final Map<ChannelGroupTypeUID, ChannelGroupType> groups = new HashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return types.values();
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        return types.get(channelTypeUID);
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return groups.get(channelGroupTypeUID);
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return groups.values();
    }

    @Override
    public void addChannelType(ChannelTypeUID uid, boolean readOnly) {

        String description = "";
        String itemType = "String";
        URI configDescriptionURI = null;
        ChannelKind kind = ChannelKind.STATE;
        BigDecimal state_min = null;
        BigDecimal state_max = null;
        BigDecimal state_step = null;
        String state_pattern = null;
        boolean state_readOnly = readOnly;
        List<StateOption> state_options = Collections.emptyList();
        StateDescription state = new StateDescription(state_min, state_max, state_step, state_pattern, state_readOnly,
                state_options);
        Set<String> tags = Collections.emptySet();
        String label = uid.getId();
        String category = "";
        boolean advanced = false;

        EventDescription event = null;
        ChannelType type = new ChannelType(uid, advanced, itemType, kind, label, description, category, tags, state,
                event, configDescriptionURI);
        types.put(uid, type);
    }

    @Override
    public void addChannelGroupType(ChannelGroupTypeUID uid, String label) {
        String description = "";
        boolean advanced = false;
        List<ChannelDefinition> channelDefinitions = Collections.emptyList();
        ChannelGroupType type = new ChannelGroupType(uid, advanced, label, description, channelDefinitions);
        groups.put(uid, type);

    }

    @Override
    public void addChannelToGroup(ChannelUID channelId, ChannelTypeUID channelTypeUid,
            ChannelGroupTypeUID channelGroupId) {
        ChannelGroupType chGrp = getChannelGroupType(channelGroupId, null);
        ChannelDefinition chDef = new ChannelDefinition(channelId.getId(), channelTypeUid);
        List<ChannelDefinition> channelDefinitions = new LinkedList<>(chGrp.getChannelDefinitions());
        channelDefinitions.add(chDef);
        ChannelGroupType newGroup = new ChannelGroupType(chGrp.getUID(), chGrp.isAdvanced(), chGrp.getLabel(),
                chGrp.getDescription(), channelDefinitions);
        groups.put(channelGroupId, newGroup);

    }

    @Override
    public ChannelTypeUID createChannelTypeBySettings(String unit, BigDecimal min, BigDecimal max, BigDecimal step,
            String itemType, boolean isReadonly, String category) {
        String settingsIdentifier = String.format("gen-", UUID.randomUUID());

        ChannelTypeUID uid = new ChannelTypeUID(BINDING_ID, settingsIdentifier);
        String description = "";
        URI configDescriptionURI = null;
        ChannelKind kind = ChannelKind.STATE;
        BigDecimal state_min = min;
        BigDecimal state_max = max;
        BigDecimal state_step = step;
        String state_pattern = "%s " + unit;
        boolean state_readOnly = isReadonly;
        List<StateOption> state_options = Collections.emptyList();
        StateDescription state = new StateDescription(state_min, state_max, state_step, state_pattern, state_readOnly,
                state_options);
        Set<String> tags = Collections.emptySet();
        String label = uid.getId();
        boolean advanced = false;

        EventDescription event = null;
        ChannelType type = new ChannelType(uid, advanced, itemType, kind, label, description, category, tags, state,
                event, configDescriptionURI);
        types.put(uid, type);
        return uid;
    }

}
