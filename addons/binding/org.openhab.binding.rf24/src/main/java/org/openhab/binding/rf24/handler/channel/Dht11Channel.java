package org.openhab.binding.rf24.handler.channel;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.internal.serial.ArduinoSerial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import pl.grzeslowski.smarthome.common.io.id.HardwareId;
import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.Dht11Response;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.RefreshDht11Request;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorRequest;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorResponse;

public class Dht11Channel extends AbstractChannel implements Channel {
    private static final Logger logger = LoggerFactory.getLogger(Dht11Channel.class);

    public Dht11Channel(IdUtils idUtils, ArduinoSerial arduinoSerial, Updatable updatable,
            Supplier<Integer> messageIdSupplier, HardwareId hardwareId) {
        super(idUtils, arduinoSerial, updatable, messageIdSupplier, hardwareId);
    }

    @Override
    public Set<String> whatChannelIdCanProcess() {
        return Sets.newHashSet(rf24BindingConstants.DHT11_HUMIDITY_CHANNEL,
                rf24BindingConstants.DHT11_TEMPERATURE_CHANNEL);
    }

    @Override
    public void process(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // @formatter:off
            SensorRequest request = newSensorRequest()
                    .setRefreshDht11Request(RefreshDht11Request.getDefaultInstance())
                    .build();
            // @formatter:on

            write(request, channelUID);
        }
    }

    @Override
    protected void processMessage(SensorResponse response) {
        ChannelUID channelUID = findChannelUID(response);
        Optional<DecimalType> decimalType = findDecimalTypeForChannel(channelUID, response.getDht11Response());
        decimalType.ifPresent(d -> updatable.updateState(channelUID, d));
    }

    @Override
    protected boolean canHandleResponse(SensorResponse response) {
        return response.hasDht11Response();
    }

    private Optional<DecimalType> findDecimalTypeForChannel(ChannelUID channelUID, Dht11Response dht11) {
        String id = channelUID.getId();
        if (Objects.equal(id, rf24BindingConstants.DHT11_HUMIDITY_CHANNEL)) {
            if (dht11.hasHumidity()) {
                return Optional.of(new DecimalType(dht11.getHumidity()));
            } else {
                // @formatter:off
                logger.warn(String.format("Channel is for humidity, but there is no humidity in Dht11Response!%n%s", dht11));
                // @formatter:on
                return Optional.empty();
            }
        } else if (Objects.equal(id, rf24BindingConstants.DHT11_TEMPERATURE_CHANNEL)) {
            if (dht11.hasTemperature()) {
                return Optional.of(new DecimalType(dht11.getTemperature()));
            } else {
                // @formatter:off
                logger.warn(String.format("Channel is for temperature, but there is no temperature in Dht11Response!%n%s", dht11));
                // @formatter:on
                return Optional.empty();
            }
        } else {
            throw new RuntimeException(String.format(
                    "I only support humidity channel (%s) and tempertature channel (%s). Got this channel ID %s. ",
                    rf24BindingConstants.DHT11_HUMIDITY_CHANNEL, rf24BindingConstants.DHT11_TEMPERATURE_CHANNEL, id));
        }
    }
}
