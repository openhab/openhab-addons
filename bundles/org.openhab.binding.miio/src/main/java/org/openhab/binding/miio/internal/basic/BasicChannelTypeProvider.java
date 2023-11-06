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
package org.openhab.binding.miio.internal.basic;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.BINDING_ID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide channelTypes for Mi IO Basic devices
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, BasicChannelTypeProvider.class })
@NonNullByDefault
public class BasicChannelTypeProvider implements ChannelTypeProvider {
    private final Map<String, ChannelType> channelTypes = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(BasicChannelTypeProvider.class);

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (channelTypes.containsKey(channelTypeUID.getAsString())) {
            return channelTypes.get(channelTypeUID.getAsString());
        }
        return null;
    }

    public void addChannelType(MiIoBasicChannel miChannel, String model) {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID,
                model.toUpperCase().replace(".", "_") + "_" + miChannel.getChannel());
        logger.debug("Adding channel definitions for {} -> {}", channelTypeUID, miChannel.getFriendlyName());
        try {
            final StateDescriptionDTO stateDescription = miChannel.getStateDescription();
            StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder.state(channelTypeUID,
                    miChannel.getFriendlyName(), miChannel.getType()); //
            if (stateDescription != null) {
                StateDescriptionFragmentBuilder sdf = StateDescriptionFragmentBuilder.create();
                final BigDecimal maximum = stateDescription.getMaximum();
                if (maximum != null) {
                    sdf.withMaximum(maximum);
                }
                final BigDecimal minimum = stateDescription.getMinimum();
                if (minimum != null) {
                    sdf.withMinimum(minimum);
                }
                final BigDecimal step = stateDescription.getStep();
                if (step != null) {
                    sdf.withStep(step);
                }
                final String pattern = stateDescription.getPattern();
                if (pattern != null) {
                    sdf.withPattern(pattern);
                }
                final Boolean readOnly = stateDescription.getReadOnly();
                if (readOnly != null) {
                    sdf.withReadOnly(readOnly);
                }
                List<OptionsValueListDTO> optionList = stateDescription.getOptions();
                if (optionList != null) {
                    List<StateOption> options = new ArrayList<>();
                    for (OptionsValueListDTO option : optionList) {
                        String value = option.getValue();
                        if (value != null) {
                            options.add(new StateOption(value, option.getLabel()));
                        }
                    }
                    sdf.withOptions(options);
                }
                channelTypeBuilder.withStateDescriptionFragment(sdf.build());
                logger.debug("added stateDescription: {}", sdf);
            }
            final String category = miChannel.getCategory();
            if (category != null) {
                channelTypeBuilder.withCategory(category);
            }
            final Set<String> tags = miChannel.getTags();
            if (tags != null && !tags.isEmpty()) {
                channelTypeBuilder.withTags(tags);
            }
            channelTypes.put(channelTypeUID.getAsString(), channelTypeBuilder.build());
        } catch (Exception e) {
            logger.warn("Failed creating channelType {}: {} ", channelTypeUID, e.getMessage());
        }
    }
}
