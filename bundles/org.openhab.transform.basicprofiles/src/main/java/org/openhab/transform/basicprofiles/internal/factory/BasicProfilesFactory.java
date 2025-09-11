/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.basicprofiles.internal.factory;

import static org.openhab.transform.basicprofiles.internal.BasicProfilesConstants.SCOPE;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocalizedKey;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileAdvisor;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.i18n.ProfileTypeI18nLocalizationService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.util.BundleResolver;
import org.openhab.transform.basicprofiles.internal.profiles.DebounceCountingStateProfile;
import org.openhab.transform.basicprofiles.internal.profiles.DebounceTimeStateProfile;
import org.openhab.transform.basicprofiles.internal.profiles.GenericCommandTriggerProfile;
import org.openhab.transform.basicprofiles.internal.profiles.GenericToggleSwitchTriggerProfile;
import org.openhab.transform.basicprofiles.internal.profiles.InactivityProfile;
import org.openhab.transform.basicprofiles.internal.profiles.InvertStateProfile;
import org.openhab.transform.basicprofiles.internal.profiles.RoundStateProfile;
import org.openhab.transform.basicprofiles.internal.profiles.StateFilterProfile;
import org.openhab.transform.basicprofiles.internal.profiles.ThresholdStateProfile;
import org.openhab.transform.basicprofiles.internal.profiles.TimeRangeCommandProfile;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BasicProfilesFactory} is responsible for creating profiles.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
@NonNullByDefault
public class BasicProfilesFactory implements ProfileFactory, ProfileTypeProvider, ProfileAdvisor {

    public static final ProfileTypeUID GENERIC_COMMAND_UID = new ProfileTypeUID(SCOPE, "generic-command");
    public static final ProfileTypeUID GENERIC_TOGGLE_SWITCH_UID = new ProfileTypeUID(SCOPE, "toggle-switch");
    public static final ProfileTypeUID DEBOUNCE_COUNTING_UID = new ProfileTypeUID(SCOPE, "debounce-counting");
    public static final ProfileTypeUID DEBOUNCE_TIME_UID = new ProfileTypeUID(SCOPE, "debounce-time");
    public static final ProfileTypeUID INVERT_UID = new ProfileTypeUID(SCOPE, "invert");
    public static final ProfileTypeUID ROUND_UID = new ProfileTypeUID(SCOPE, "round");
    public static final ProfileTypeUID THRESHOLD_UID = new ProfileTypeUID(SCOPE, "threshold");
    public static final ProfileTypeUID TIME_RANGE_COMMAND_UID = new ProfileTypeUID(SCOPE, "time-range-command");
    public static final ProfileTypeUID STATE_FILTER_UID = new ProfileTypeUID(SCOPE, "state-filter");
    public static final ProfileTypeUID INACTIVITY_UID = new ProfileTypeUID(SCOPE, "inactivity");

    private static final ProfileType PROFILE_TYPE_GENERIC_COMMAND = ProfileTypeBuilder
            .newTrigger(GENERIC_COMMAND_UID, "Generic Command") //
            .withSupportedItemTypes(CoreItemFactory.DIMMER, CoreItemFactory.NUMBER, CoreItemFactory.PLAYER,
                    CoreItemFactory.ROLLERSHUTTER, CoreItemFactory.SWITCH) //
            .build();
    private static final ProfileType PROFILE_TYPE_GENERIC_TOGGLE_SWITCH = ProfileTypeBuilder
            .newTrigger(GENERIC_TOGGLE_SWITCH_UID, "Generic Toggle Switch") //
            .withSupportedItemTypes(CoreItemFactory.COLOR, CoreItemFactory.DIMMER, CoreItemFactory.SWITCH) //
            .build();
    private static final ProfileType PROFILE_TYPE_DEBOUNCE_COUNTING = ProfileTypeBuilder
            .newState(DEBOUNCE_COUNTING_UID, "Debounce (Counting)").build();
    private static final ProfileType PROFILE_TYPE_DEBOUNCE_TIME = ProfileTypeBuilder
            .newState(DEBOUNCE_TIME_UID, "Debounce (Time)").build();
    private static final ProfileType PROFILE_TYPE_INVERT = ProfileTypeBuilder.newState(INVERT_UID, "Invert / Negate")
            .withSupportedItemTypes(CoreItemFactory.CONTACT, CoreItemFactory.DIMMER, CoreItemFactory.NUMBER,
                    CoreItemFactory.PLAYER, CoreItemFactory.ROLLERSHUTTER, CoreItemFactory.SWITCH) //
            .withSupportedItemTypesOfChannel(CoreItemFactory.CONTACT, CoreItemFactory.DIMMER, CoreItemFactory.NUMBER,
                    CoreItemFactory.PLAYER, CoreItemFactory.ROLLERSHUTTER, CoreItemFactory.SWITCH) //
            .build();
    private static final ProfileType PROFILE_TYPE_ROUND = ProfileTypeBuilder.newState(ROUND_UID, "Round")
            .withSupportedItemTypes(CoreItemFactory.NUMBER) //
            .withSupportedItemTypesOfChannel(CoreItemFactory.NUMBER) //
            .build();
    private static final ProfileType PROFILE_TYPE_THRESHOLD = ProfileTypeBuilder.newState(THRESHOLD_UID, "Threshold") //
            .withSupportedItemTypesOfChannel(CoreItemFactory.DIMMER, CoreItemFactory.NUMBER) //
            .withSupportedItemTypes(CoreItemFactory.SWITCH) //
            .build();
    private static final ProfileType PROFILE_TYPE_TIME_RANGE_COMMAND = ProfileTypeBuilder
            .newState(TIME_RANGE_COMMAND_UID, "Time Range Command") //
            .withSupportedItemTypes(CoreItemFactory.SWITCH) //
            .withSupportedChannelTypeUIDs(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_MOTION) //
            .build();
    private static final ProfileType PROFILE_STATE_FILTER = ProfileTypeBuilder
            .newState(STATE_FILTER_UID, "State Filter").build();

    private static final ProfileType PROFILE_TYPE_INACTIVITY = ProfileTypeBuilder
            .newState(INACTIVITY_UID, "No Input Activity").withSupportedItemTypes(CoreItemFactory.SWITCH).build();

    private static final Set<ProfileTypeUID> SUPPORTED_PROFILE_TYPE_UIDS = Set.of(GENERIC_COMMAND_UID,
            GENERIC_TOGGLE_SWITCH_UID, DEBOUNCE_COUNTING_UID, DEBOUNCE_TIME_UID, INVERT_UID, ROUND_UID, THRESHOLD_UID,
            TIME_RANGE_COMMAND_UID, STATE_FILTER_UID, INACTIVITY_UID);
    private static final Set<ProfileType> SUPPORTED_PROFILE_TYPES = Set.of(PROFILE_TYPE_GENERIC_COMMAND,
            PROFILE_TYPE_GENERIC_TOGGLE_SWITCH, PROFILE_TYPE_DEBOUNCE_COUNTING, PROFILE_TYPE_DEBOUNCE_TIME,
            PROFILE_TYPE_INVERT, PROFILE_TYPE_ROUND, PROFILE_TYPE_THRESHOLD, PROFILE_TYPE_TIME_RANGE_COMMAND,
            PROFILE_STATE_FILTER, PROFILE_TYPE_INACTIVITY);

    private final Map<LocalizedKey, ProfileType> localizedProfileTypeCache = new ConcurrentHashMap<>();

    private final ProfileTypeI18nLocalizationService profileTypeI18nLocalizationService;
    @Nullable
    private final Bundle bundle;
    private final ItemRegistry itemRegistry;
    private final ItemChannelLinkRegistry linkRegistry;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public BasicProfilesFactory(final @Reference ProfileTypeI18nLocalizationService profileTypeI18nLocalizationService,
            final @Reference BundleResolver bundleResolver, @Reference ItemRegistry itemRegistry,
            @Reference ItemChannelLinkRegistry linkRegistry, @Reference TimeZoneProvider timeZoneProvider) {
        this.profileTypeI18nLocalizationService = profileTypeI18nLocalizationService;
        this.bundle = bundleResolver.resolveBundle(BasicProfilesFactory.class);
        this.itemRegistry = itemRegistry;
        this.linkRegistry = linkRegistry;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext context) {
        if (GENERIC_COMMAND_UID.equals(profileTypeUID)) {
            return new GenericCommandTriggerProfile(callback, context);
        } else if (GENERIC_TOGGLE_SWITCH_UID.equals(profileTypeUID)) {
            return new GenericToggleSwitchTriggerProfile(callback, context);
        } else if (DEBOUNCE_COUNTING_UID.equals(profileTypeUID)) {
            return new DebounceCountingStateProfile(callback, context);
        } else if (DEBOUNCE_TIME_UID.equals(profileTypeUID)) {
            return new DebounceTimeStateProfile(callback, context);
        } else if (INVERT_UID.equals(profileTypeUID)) {
            return new InvertStateProfile(callback);
        } else if (ROUND_UID.equals(profileTypeUID)) {
            return new RoundStateProfile(callback, context);
        } else if (THRESHOLD_UID.equals(profileTypeUID)) {
            return new ThresholdStateProfile(callback, context);
        } else if (TIME_RANGE_COMMAND_UID.equals(profileTypeUID)) {
            return new TimeRangeCommandProfile(callback, context, timeZoneProvider);
        } else if (STATE_FILTER_UID.equals(profileTypeUID)) {
            return new StateFilterProfile(callback, context, itemRegistry);
        } else if (INACTIVITY_UID.equals(profileTypeUID)) {
            return new InactivityProfile(callback, context, linkRegistry);
        }
        return null;
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return SUPPORTED_PROFILE_TYPES.stream().map(p -> createLocalizedProfileType(p, locale)).toList();
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return SUPPORTED_PROFILE_TYPE_UIDS;
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, @Nullable String itemType) {
        return null;
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        return null;
    }

    private ProfileType createLocalizedProfileType(ProfileType profileType, @Nullable Locale locale) {
        final LocalizedKey localizedKey = new LocalizedKey(profileType.getUID(),
                locale != null ? locale.toLanguageTag() : null);

        final ProfileType cachedlocalizedProfileType = localizedProfileTypeCache.get(localizedKey);
        if (cachedlocalizedProfileType != null) {
            return cachedlocalizedProfileType;
        }

        final ProfileType localizedProfileType = profileTypeI18nLocalizationService.createLocalizedProfileType(bundle,
                profileType, locale);
        if (localizedProfileType != null) {
            localizedProfileTypeCache.put(localizedKey, localizedProfileType);
            return localizedProfileType;
        } else {
            return profileType;
        }
    }
}
