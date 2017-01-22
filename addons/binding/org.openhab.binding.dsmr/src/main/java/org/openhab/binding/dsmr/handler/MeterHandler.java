package org.openhab.binding.dsmr.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.dsmr.device.cosem.CosemDate;
import org.openhab.binding.dsmr.device.cosem.CosemInteger;
import org.openhab.binding.dsmr.device.cosem.CosemObject;
import org.openhab.binding.dsmr.device.cosem.CosemString;
import org.openhab.binding.dsmr.device.cosem.CosemValue;
import org.openhab.binding.dsmr.meter.DSMRMeter;
import org.openhab.binding.dsmr.meter.DSMRMeterConstants;
import org.openhab.binding.dsmr.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.meter.DSMRMeterListener;
import org.openhab.binding.dsmr.meter.DSMRMeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeterHandler extends BaseThingHandler implements DSMRMeterListener {
    public static final Logger logger = LoggerFactory.getLogger(MeterHandler.class);

    private static final String KEY_METERTYPE = "meterType";
    private static final String KEY_CHANNEL = "channel";
    private static final String KEY_IDSTRING = "idString";

    private static final DateFormat FAILURE_FORMAT = new SimpleDateFormat("d MMM yyyy HH:mm:ss");

    private DSMRMeter meter = null;
    private long lastValuesReceivedTs = 0;
    private Timer valueReceivedTimer;

    public MeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No comments can be handled
    }

    @Override
    public void initialize() {
        logger.debug("Initialize MeterHandler for Thing {}", getThing());
        Configuration config = getThing().getConfiguration();

        DSMRMeterDescriptor meterDescriptor = null;
        if (config.containsKey(KEY_METERTYPE) && config.containsKey(KEY_CHANNEL) && config.containsKey(KEY_IDSTRING)) {
            DSMRMeterType meterType = null;
            Integer channel = null;
            String idString = null;
            try {
                meterType = DSMRMeterType.valueOf((String) config.get(KEY_METERTYPE));
            } catch (Exception exception) {
                logger.error("Invalid meterType in configuration", exception);
            }
            try {
                channel = ((Integer) config.get(KEY_CHANNEL)).intValue();
            } catch (Exception exception) {
                logger.error("Invalid channel in configuration", exception);
            }
            try {
                idString = (String) config.get(KEY_METERTYPE);
            } catch (Exception exception) {
                logger.error("Invalid idString in configuration", exception);
            }

            if (meterType != null && channel != null && idString != null) {
                meterDescriptor = new DSMRMeterDescriptor(meterType, channel, idString);
            }
        }
        if (meterDescriptor != null) {
            meter = new DSMRMeter(meterDescriptor, this);

            // Initialize timeout timer
            valueReceivedTimer = new Timer(meter.toString(), true);
            valueReceivedTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    if (System.currentTimeMillis()
                            - lastValuesReceivedTs > DSMRMeterConstants.METER_VALUES_RECEIVED_TIMEOUT) {
                        if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                            updateStatus(ThingStatus.OFFLINE);
                        }
                    }
                }

            }, DSMRMeterConstants.METER_VALUES_TIMER_PERIOD, DSMRMeterConstants.METER_VALUES_TIMER_PERIOD);

            updateStatus(ThingStatus.OFFLINE);
        } else {
            logger.error("Invalid configuration for this thing. Delete Thing if the problem persists.");
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid configuration for this thing. Delete Thing if the problem persists.");
        }
    }

    @Override
    public void dispose() {
        if (valueReceivedTimer != null) {
            valueReceivedTimer.cancel();
        }
    }

    @Override
    public void handleRemoval() {
        // Stop the timeout timer
        if (valueReceivedTimer != null) {
            valueReceivedTimer.cancel();
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    @SuppressWarnings("incomplete-switch")
    public void meterValueReceived(CosemObject obj) {
        List<? extends CosemValue<? extends Object>> cosemValues = obj.getCosemValues();

        State newState = null;

        // Update the internal states
        if (cosemValues.size() > 0) {
            lastValuesReceivedTs = System.currentTimeMillis();

            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        }

        if (cosemValues.size() == 1) {
            // Regular CosemOBject just send the value
            newState = obj.getCosemValue(0).getOpenHABValue();
        } else if (cosemValues.size() > 1) {
            CosemValue<? extends Object> cosemValue = null;
            // Special CosemObjects need special handling
            switch (obj.getType()) {
                case EMETER_VALUE:
                    cosemValue = obj.getCosemValue(1);
                    break;
                case EMETER_POWER_FAILURE_LOG:
                    // TODO: We now only supports last log entry
                    CosemDate endDate = (CosemDate) obj.getCosemValue(2);
                    CosemInteger duration = (CosemInteger) obj.getCosemValue(3);

                    if (endDate != null && duration != null) {
                        cosemValue = new CosemString("",
                                FAILURE_FORMAT.format(endDate.getValue()) + ", " + duration.getValue() + " seconds");
                    } else {
                        cosemValue = new CosemString("", "No failures");
                    }

                    break;
                case GMETER_24H_DELIVERY_V2:
                    cosemValue = obj.getCosemValue(0);
                    break;
                case GMETER_24H_DELIVERY_COMPENSATED_V2:
                    cosemValue = obj.getCosemValue(0);
                    break;
                case GMETER_VALUE_V3:
                    cosemValue = obj.getCosemValue(6);
                    break;
                case HMETER_VALUE_V2:
                    cosemValue = obj.getCosemValue(0);
                    break;
                case CMETER_VALUE_V2:
                    cosemValue = obj.getCosemValue(0);
                    break;
                case WMETER_VALUE_V2:
                    cosemValue = obj.getCosemValue(0);
                    break;
                case M3METER_VALUE:
                    cosemValue = obj.getCosemValue(1);
                    break;
                case GJMETER_VALUE_V4:
                    cosemValue = obj.getCosemValue(1);
                    break;
            }
            if (cosemValue != null) {
                newState = cosemValue.getOpenHABValue();
            }
        } else {
            logger.warn("Invalid CosemObject size ({}) for CosemObject {}", cosemValues.size(), obj);
        }
        if (newState != null) {
            updateState(obj.getType().name().toLowerCase(), newState);
        }
    }

    /**
     * @return the meter
     */
    public DSMRMeter getDSMRMeter() {
        return meter;
    }
}
