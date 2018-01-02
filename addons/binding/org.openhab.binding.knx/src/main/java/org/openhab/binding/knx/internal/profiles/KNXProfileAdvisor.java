package org.openhab.binding.knx.internal.profiles;

import static java.util.stream.Collectors.toSet;
import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.profiles.Profile;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeProvider;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfileType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

@Component
@NonNullByDefault
public class KNXProfileAdvisor implements ProfileAdvisor, ProfileTypeProvider, ProfileFactory {

    private static final StateProfileType TYPE_DEFAULT = ProfileTypeBuilder
            .newState(KNXDefaultProfile.UID, "KNX Default").build();
    private static final StateProfileType TYPE_CONTROL = ProfileTypeBuilder
            .newState(KNXControlProfile.UID, "KNX Control").build();

    private static final Set<ProfileTypeUID> SUPPORTED_PROFILE_TYPE_UIDS = Stream
            .of(KNXDefaultProfile.UID, KNXControlProfile.UID).collect(toSet());

    private static final Set<ProfileType> SUPPORTED_PROFILE_TYPES = Stream.of(TYPE_DEFAULT, TYPE_CONTROL)
            .collect(Collectors.toSet());

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null || !channelTypeUID.getBindingId().equals(BINDING_ID)) {
            return null;
        }
        if (isControl(channelTypeUID)) {
            return KNXControlProfile.UID;
        } else {
            return KNXDefaultProfile.UID;
        }
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, @Nullable String itemType) {
        ChannelTypeUID channelTypeUID = channelType.getUID();
        if (isControl(channelTypeUID)) {
            return KNXControlProfile.UID;
        } else {
            return KNXDefaultProfile.UID;
        }
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        if (KNXDefaultProfile.UID.equals(profileTypeUID)) {
            return new KNXDefaultProfile(callback);
        } else if (KNXControlProfile.UID.equals(profileTypeUID)) {
            return new KNXControlProfile(callback);
        }
        return null;
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return SUPPORTED_PROFILE_TYPE_UIDS;
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return SUPPORTED_PROFILE_TYPES;
    }

    public static boolean isControl(ChannelTypeUID channelTypeUID) {
        return CONTROL_CHANNEL_TYPES.contains(channelTypeUID.getId());
    }

}
