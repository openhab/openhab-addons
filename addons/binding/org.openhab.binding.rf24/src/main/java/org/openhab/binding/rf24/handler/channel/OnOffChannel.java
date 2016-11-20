package org.openhab.binding.rf24.handler.channel;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.wifi.WiFi;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import pl.grzeslowski.smarthome.proto.common.Basic.BasicMessage;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOff;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOffRequest;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOffResponse;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.RefreshOnOffRequest;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorRequest;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

public class OnOffChannel implements Channel {
    private final WiFi wifi;
    private final AtomicInteger messageIdSupplier;
    private final Pipe pipe;
    private int deviceId;

    public OnOffChannel(WiFi wifi, AtomicInteger messageIdSupplier, int deviceId, Pipe pipe) {
        this.wifi = Preconditions.checkNotNull(wifi);
        this.messageIdSupplier = Preconditions.checkNotNull(messageIdSupplier);
        this.pipe = Preconditions.checkNotNull(pipe);
        this.deviceId = deviceId;
    }

    @Override
    public Optional<Consumer<Updatable>> process(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            OnOffType onOff = (OnOffType) command; // Czasami moze byc refresh type
            SensorRequest cmdToSend = build(onOff);
            wifi.write(pipe, cmdToSend);

            return Optional.of(Channel.DOING_NOTHING_CONSUMER);
        } else if (command instanceof RefreshType) {
            BasicMessage basic = BasicMessage.newBuilder().setDeviceId(deviceId).setLinuxTimestamp(new Date().getTime())
                    .setMessageId(messageIdSupplier.incrementAndGet()).build();

            SensorRequest request = SensorRequest.newBuilder().setBasic(basic)
                    .setRefreshOnOffRequest(RefreshOnOffRequest.getDefaultInstance()).build();

            wifi.write(pipe, request);
            return Optional.of(updatable -> wifi.read(pipe).ifPresent(response -> {
                if (response.hasOnOffResponse()) {
                    OnOffResponse onOffResponse = response.getOnOffResponse();
                    updatable.updateState(channelUID, findOnOffType(onOffResponse));
                }
            }));
        } else {
            return Optional.empty();
        }
    }

    private OnOffType findOnOffType(OnOffResponse onOffResponse) {
        if (onOffResponse.getOnOff() == OnOff.ON) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }

    private SensorRequest build(OnOffType cmd) {
        BasicMessage basic = BasicMessage.newBuilder().setDeviceId(deviceId).setLinuxTimestamp(new Date().getTime())
                .setMessageId(messageIdSupplier.incrementAndGet()).build();

        OnOff onOff;
        switch (cmd) {
            case ON:
                onOff = OnOff.ON;
                break;

            case OFF:
                onOff = OnOff.OFF;
                break;
            default:
                throw new RuntimeException("This should never happened [" + cmd + "]!");
        }

        OnOffRequest onOffCommand = OnOffRequest.newBuilder().setOnOff(onOff).build();

        return SensorRequest.newBuilder().setBasic(basic).setOnOffRequest(onOffCommand).build();
    }

    @Override
    public Set<String> whatChannelIdCanProcess() {
        return Sets.newHashSet(rf24BindingConstants.RF24_ON_OFF_CHANNEL);
    }

    @Override
    public String toString() {
        return OnOffChannel.class.getSimpleName();
    }
}
