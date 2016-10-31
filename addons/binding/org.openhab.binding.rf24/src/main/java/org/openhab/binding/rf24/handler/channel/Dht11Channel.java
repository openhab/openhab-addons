package org.openhab.binding.rf24.handler.channel;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.wifi.WiFi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import pl.grzeslowski.smarthome.proto.common.Basic.BasicMessage;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.Dht11Response;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.RefreshDht11Request;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorRequest;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorResponse;
import pl.grzeslowski.smarthome.rpi.wifi.help.Pipe;

public class Dht11Channel implements Channel {
    private static final Logger logger = LoggerFactory.getLogger(Dht11Channel.class);

    private final WiFi wifi;
    private final AtomicInteger messageIdSupplier;
    private final Pipe pipe;
    private int deviceId;

    public Dht11Channel(WiFi wifi, AtomicInteger messageIdSupplier, int deviceId, Pipe pipe) {
        this.wifi = Preconditions.checkNotNull(wifi);
        this.messageIdSupplier = Preconditions.checkNotNull(messageIdSupplier);
        this.pipe = Preconditions.checkNotNull(pipe);
        this.deviceId = deviceId;
    }

    @Override
    public Set<String> whatChannelIdCanProcess() {
        return Sets.newHashSet(rf24BindingConstants.DHT11_HUMIDITY_CHANNEL,
                rf24BindingConstants.DHT11_TEMPERATURE_CHANNEL);
    }

    @Override
    public Optional<Consumer<Updatable>> process(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            BasicMessage basic = BasicMessage.newBuilder().setDeviceId(deviceId).setLinuxTimestamp(new Date().getTime())
                    .setMessageId(messageIdSupplier.incrementAndGet()).build();

            SensorRequest request = SensorRequest.newBuilder().setBasic(basic)
                    .setRefreshDht11Request(RefreshDht11Request.getDefaultInstance()).build();

            wifi.write(pipe, request);
            return Optional.of(new Consumer<Updatable>() {

                @Override
                public void accept(Updatable updatable) {
                    // TODO here might be needed some delay!!!
                    wifi.read(pipe).ifPresent(new Consumer<SensorResponse>() {

                        @Override
                        public void accept(SensorResponse response) {
                            if (response.hasDht11Response()) {
                                findDecimalTypeForChannel(channelUID, response.getDht11Response())
                                        .ifPresent(new Consumer<DecimalType>() {

                                            @Override
                                            public void accept(DecimalType decimalType) {
                                                updatable.updateState(channelUID, decimalType);
                                            }
                                        });
                            } else {
                                logger.warn(String.format(
                                        "SensorResponse for pipe %s should have Dht11Response! Response:%n%s", pipe,
                                        response));
                            }
                        }
                    });
                }
            });
        } else {
            return Optional.empty();
        }
    }

    private Optional<DecimalType> findDecimalTypeForChannel(ChannelUID channelUID, Dht11Response dht11) {
        String id = channelUID.getId();
        if (Objects.equal(id, rf24BindingConstants.DHT11_HUMIDITY_CHANNEL)) {
            if (dht11.hasHumidity()) {
                return Optional.of(new DecimalType(dht11.getHumidity()));
            } else {
                logger.warn(String.format("Channel is for humidity, but there is no humidity in Dht11Response!%n%s",
                        dht11));
                return Optional.empty();
            }
        } else if (Objects.equal(id, rf24BindingConstants.DHT11_TEMPERATURE_CHANNEL)) {
            if (dht11.hasTemperature()) {
                return Optional.of(new DecimalType(dht11.getTemperature()));
            } else {
                logger.warn(String.format(
                        "Channel is for temperature, but there is no temperature in Dht11Response!%n%s", dht11));
                return Optional.empty();
            }
        } else {
            throw new RuntimeException(String.format(
                    "I only support humidity channel (%s) and tempertature channel (%s). Got this channel ID %s. ",
                    rf24BindingConstants.DHT11_HUMIDITY_CHANNEL, rf24BindingConstants.DHT11_TEMPERATURE_CHANNEL, id));
        }
    }

    @Override
    public String toString() {
        return Dht11Channel.class.getSimpleName();
    }
}
