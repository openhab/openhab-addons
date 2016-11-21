package org.openhab.binding.rf24.handler.channel;

import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.wifi.WifiOperator;

import com.google.common.collect.Sets;

import pl.grzeslowski.smarthome.common.io.id.HardwareId;
import pl.grzeslowski.smarthome.common.io.id.IdUtils;
import pl.grzeslowski.smarthome.proto.common.Basic.BasicMessage;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOff;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOffRequest;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOffResponse;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.RefreshOnOffRequest;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorRequest;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorResponse;

public class OnOffChannel extends AbstractChannel implements Channel {

    public OnOffChannel(IdUtils idUtils, WifiOperator wifiOperator, Updatable updatable,
            Supplier<Integer> messageIdSupplier, HardwareId hardwareId) {
        super(idUtils, wifiOperator, updatable, messageIdSupplier, hardwareId);
    }

    @Override
    public void process(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            OnOffType onOff = (OnOffType) command;
            SensorRequest cmdToSend = build(onOff);
            write(cmdToSend, channelUID);
        } else if (command instanceof RefreshType) {
            // @formatter:off
            SensorRequest request = newSensorRequest()
                    .setRefreshOnOffRequest(RefreshOnOffRequest.getDefaultInstance())
                    .build();
            // @formatter:on

            write(request, channelUID);
        }
    }

    @Override
    protected void processMessage(SensorResponse response) {
        ChannelUID channelUID = findChannelUID(response);
        OnOffResponse onOffResponse = response.getOnOffResponse();
        updatable.updateState(channelUID, findOnOffType(onOffResponse));
    }

    @Override
    protected boolean canHandleResponse(SensorResponse response) {
        return response.hasOnOffResponse();
    }

    private OnOffType findOnOffType(OnOffResponse onOffResponse) {
        switch (onOffResponse.getOnOff()) {
            case ON:
                return OnOffType.ON;
            case OFF:
                return OnOffType.OFF;
            default:
                throw new RuntimeException("This should never happened [" + onOffResponse + "]!");
        }
    }

    private SensorRequest build(OnOffType cmd) {
        BasicMessage basic = buildBasicMessage();

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
}
