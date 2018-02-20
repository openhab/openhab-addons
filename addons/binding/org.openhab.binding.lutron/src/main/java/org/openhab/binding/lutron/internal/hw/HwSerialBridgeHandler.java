/**
 *
 */
package org.openhab.binding.lutron.internal.hw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 *
 * This is the main handler for HomeWorks RS232 Processors.
 *
 * @author Andrew Shilliday
 *
 */
public class HwSerialBridgeHandler extends BaseBridgeHandler implements SerialPortEventListener {
    private final Logger logger = LoggerFactory.getLogger(HwSerialBridgeHandler.class);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private String serialPortName = "";
    private int baudRate;
    private Boolean updateTime;
    private ScheduledFuture<?> updateTimeJob;

    private SerialPort serialPort = null;
    private OutputStreamWriter serialOutput = null;
    private BufferedReader serialInput = null;

    private HwDiscoveryService discService;
    private ServiceRegistration<DiscoveryService> discReg;

    public HwSerialBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the Lutron HomeWorks RS232 bridge handler");
        HwSerialBridgeConfig configuration = getConfigAs(HwSerialBridgeConfig.class);
        serialPortName = configuration.getSerialPort();
        updateTime = configuration.getUpdateTime();
        if (configuration.getBaudRate() == null) {
            baudRate = HwSerialBridgeConfig.DEFAULT_BAUD;
        } else {
            baudRate = configuration.getBaudRate().intValue();
        }

        this.discService = new HwDiscoveryService(this);
        this.discReg = bundleContext.registerService(DiscoveryService.class, discService, null);

        if (serialPortName == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port not specified");
            return;
        }

        logger.debug("Lutron HomeWorks RS232 Bridge Handler Initializing.");
        logger.debug("   Serial Port: {},", serialPortName);
        logger.debug("   Baud:        {},", baudRate);

        scheduler.execute(() -> openConnection());
    }

    private void openConnection() {
        try {
            logger.info("Connecting to Lutron HomeWorks Processor using {}.", serialPortName);
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
            serialPort = portIdentifier.open(this.getClass().getName(), 2000);

            logger.debug("Connection established using {}.  Configuring IO parameters. ", serialPortName);

            int db = SerialPort.DATABITS_8, sb = SerialPort.STOPBITS_1, p = SerialPort.PARITY_NONE;
            serialPort.setSerialPortParams(baudRate, db, sb, p);
            serialPort.enableReceiveThreshold(1);
            serialPort.disableReceiveTimeout();
            serialOutput = new OutputStreamWriter(serialPort.getOutputStream(), "US-ASCII");
            serialInput = new BufferedReader(new InputStreamReader(serialPort.getInputStream(), "US-ASCII"));

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            logger.debug("Sending monitoring commands.");
            sendCommand("PROMPTOFF");
            sendCommand("KBMOFF");
            sendCommand("KLMOFF");
            sendCommand("GSMOFF");
            sendCommand("DLMON"); // Turn on dimmer monitoring

            logger.info("Setting status to ONLINE");
            updateStatus(ThingStatus.ONLINE);

            if (updateTime) {
                startUpdateProcessorTimeJob();
            }

        } catch (NoSuchPortException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Invalid port: " + serialPortName);
        } catch (PortInUseException portInUseException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Port in use: " + serialPortName);
        } catch (UnsupportedCommOperationException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication error");
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error configuring serial port.");
        }
    }

    private void startUpdateProcessorTimeJob() {
        if (updateTimeJob != null) {
            logger.debug("Canceling old scheduled job");
            updateTimeJob.cancel(false);
            updateTimeJob = null;
        }

        logger.debug("Scheduling time update job for 2:00 AM");

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime twoAm = now.withHour(2).withMinute(0).withSecond(0);
        if (now.compareTo(twoAm) > 0) {
            twoAm = twoAm.plusDays(1);
        }

        Duration duration = Duration.between(now, twoAm);
        long initialDelay = duration.getSeconds();
        long delay = Duration.ofDays(1).getSeconds();
        TimeUnit unit = TimeUnit.SECONDS;

        logger.debug("Update Time job will run in {} and every 1 day thereafter.", duration);
        updateTimeJob = this.scheduler.scheduleWithFixedDelay(() -> updateProcessorTime(), initialDelay, delay, unit);

        // For good measure, we'll call it once now.
        updateProcessorTime();
    }

    private void updateProcessorTime() {
        Date date = new Date();
        String dateString = dateFormat.format(date.getTime());
        String timeString = timeFormat.format(date.getTime());
        logger.debug("Updating HomeWorks processor date and time to {} {}", dateString, timeString);

        if (!this.getBridge().getStatus().equals(ThingStatus.ONLINE)) {
            logger.warn("HomeWorks Bridge is offline and cannot update time on HomeWorks processor.");
            if (updateTimeJob != null) {
                updateTimeJob.cancel(false);
                updateTimeJob = null;
            }
            return;
        }

        sendCommand("SD, " + dateString);
        sendCommand("ST, " + timeString);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.error("Unexpected command for HomeWorks Bridge: {} - {}", channelUID, command);
    }

    private void handleIncomingMessage(String line) {
        if (line == null || line.isEmpty()) {
            return;
        }

        logger.info("Received message from HomeWorks processor: {}", line);
        String[] data = line.replaceAll("\\s", "").toUpperCase().split(",");
        if ("DL".equals(data[0])) {
            try {
                String address = data[1];
                Integer level = Integer.parseInt(data[2]);
                HwDimmerHandler handler = findHandler(address);
                if (handler == null) {
                    discService.declareUnknownDimmer(address);
                } else {
                    handler.handleLevelChange(level);
                }
            } catch (final Exception e) {
                logger.error("Error parsing incoming message", e);
            }
        }

    }

    private HwDimmerHandler findHandler(String address) {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof HwDimmerHandler) {
                HwDimmerHandler handler = (HwDimmerHandler) thing.getHandler();
                if (address.equals(handler.getAddress())) {
                    return handler;
                }
            }
        }
        return null;
    }

    /**
     * Receives Serial Port Events and reads Serial Port Data.
     *
     * @param serialPortEvent
     */
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                while (true) {
                    String messageLine = serialInput.readLine();
                    if (messageLine == null) {
                        break;
                    }
                    handleIncomingMessage(messageLine);
                }
            } catch (Exception e) {
                logger.error("Error reading from serial port.", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error reading from port");
            }
        }
    }

    public void sendCommand(String command) {
        try {
            logger.info("HomeWorks bridge sending command: {}", command);
            serialOutput.write(command.toString() + "\r");
            serialOutput.flush();
        } catch (IOException e) {
            logger.error("Error writing to serial port.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error writing to port");
        }
    }

    @Override
    public void dispose() {
        logger.info("HomeWorks bridge being disposed.");
        if (serialPort != null) {
            serialPort.close();
        }

        serialPort = null;
        serialInput = null;
        serialOutput = null;

        if (updateTimeJob != null) {
            updateTimeJob.cancel(false);
        }

        if (this.discReg != null) {
            this.discReg.unregister();
            this.discReg = null;
        }
        logger.debug("Finished disposing bridge.");
    }

}
