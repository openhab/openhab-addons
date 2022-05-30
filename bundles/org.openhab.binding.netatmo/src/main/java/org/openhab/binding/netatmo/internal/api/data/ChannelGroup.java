package org.openhab.binding.netatmo.internal.api.data;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AirQualityChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.BatteryChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.EventChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.HumidityChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.LocationChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.NoiseChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.SignalChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.TemperatureChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.TimestampChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoThingTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class ChannelGroup {
    public static final ChannelGroup SIGNAL = new ChannelGroup(SignalChannelHelper.class, GROUP_SIGNAL);
    public static final ChannelGroup EVENT = new ChannelGroup(EventChannelHelper.class, GROUP_LAST_EVENT);
    public static final ChannelGroup MEASURE = new ChannelGroup(MeasuresChannelHelper.class);
    public static final ChannelGroup BATTERY = new ChannelGroup(BatteryChannelHelper.class, GROUP_BATTERY);
    public static final ChannelGroup LOCATION = new ChannelGroup(LocationChannelHelper.class, GROUP_LOCATION);
    public static final ChannelGroup BATTERY_EXT = new ChannelGroup(BatteryChannelHelper.class,
            GROUP_TYPE_BATTERY_EXTENDED);
    public static final ChannelGroup TSTAMP_EXT = new ChannelGroup(TimestampChannelHelper.class,
            GROUP_TYPE_TIMESTAMP_EXTENDED);
    public static final ChannelGroup TEMP_OUTSIDE = new ChannelGroup(TemperatureChannelHelper.class,
            MeasureClass.OUTSIDE_TEMPERATURE, GROUP_TYPE_TEMPERATURE_EXTENDED);
    public static final ChannelGroup TEMP_INSIDE = new ChannelGroup(TemperatureChannelHelper.class,
            MeasureClass.INSIDE_TEMPERATURE, GROUP_TYPE_TEMPERATURE_EXTENDED);
    public static final ChannelGroup AIR_QUALITY = new ChannelGroup(AirQualityChannelHelper.class, MeasureClass.CO2,
            GROUP_AIR_QUALITY);
    public static final ChannelGroup NOISE = new ChannelGroup(NoiseChannelHelper.class, MeasureClass.NOISE,
            GROUP_NOISE);
    public static final ChannelGroup HUMIDITY = new ChannelGroup(HumidityChannelHelper.class, MeasureClass.HUMIDITY,
            GROUP_HUMIDITY);

    private final Logger logger = LoggerFactory.getLogger(ChannelGroup.class);
    private final Class<? extends ChannelHelper> helper;
    public final Set<String> groupTypes;
    public final Set<String> extensions;

    ChannelGroup(Class<? extends ChannelHelper> helper, String... groupTypes) {
        this(helper, Set.of(), groupTypes);
    }

    ChannelGroup(Class<? extends ChannelHelper> helper, MeasureClass measureClass, String... groupTypes) {
        this(helper, measureClass.channels.keySet(), groupTypes);
    }

    private ChannelGroup(Class<? extends ChannelHelper> helper, Set<String> extensions, String... groupTypes) {
        this.helper = helper;
        this.groupTypes = Set.of(groupTypes);
        this.extensions = extensions;
    }

    public @Nullable ChannelHelper getHelperInstance() {
        ChannelHelper result = null;
        try {
            result = helper.getConstructor(Set.class).newInstance(
                    groupTypes.stream().map(NetatmoThingTypeProvider::toGroupName).collect(Collectors.toSet()));
        } catch (ReflectiveOperationException e) {
            logger.warn("Error creating or initializing helper class : {}", e.getMessage());
        }
        return result;
    }
}
