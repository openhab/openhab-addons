package org.openhab.binding.fronius.handler;

import static org.openhab.binding.fronius.FroniusBindingConstants.*;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.fronius.internal.model.InverterRealtimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FroniusInverterRealtimeDataHandler extends FroniusDeviceThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusInverterRealtimeDataHandler.class);
    private final JsonParser parser = new JsonParser();

    private String url;

    public FroniusInverterRealtimeDataHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getServiceDescription() {
        return INVERTER_REALTIME_DATA_DESCRIPTION;
    }

    @Override
    protected String getServiceUrl() {
        if (null == url) {
            StringBuilder sb = new StringBuilder();
            sb.append("http://");
            sb.append(getHostname());
            sb.append(INVERTER_REALTIME_DATA_URL);
            sb.append("?Scope=Device");
            sb.append("&DeviceId=");
            sb.append(getDevice());
            sb.append("&DataCollection=CommonInverterData");
            url = sb.toString();
        }

        logger.debug("{} URL: {}", getServiceDescription(), url);

        return url;
    }

    @Override
    protected void updateData(String data) {
        logger.debug("Refresh data {}", data);
        JsonObject json = parser.parse(data).getAsJsonObject();
        InverterRealtimeData model = InverterRealtimeData.createInverterRealtimeData(json);
        if (model.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } else {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_INVERTER_DAY_ENERGY), model.getDayEnergy());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_INVERTER_YEAR_ENERGY), model.getYearEnergy());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_INVERTER_TOTAL_ENERGY), model.getTotalEnergy());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_INVERTER_PAC), model.getPac());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_INVERTER_IAC), model.getIac());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_INVERTER_UAC), model.getUac());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_INVERTER_FAC), model.getFac());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_INVERTER_IDC), model.getIdc());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_INVERTER_UDC), model.getUdc());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_STATUS_CODE), model.getCode());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIMESTAMP), model.getTimestamp());
        }
    }
}
