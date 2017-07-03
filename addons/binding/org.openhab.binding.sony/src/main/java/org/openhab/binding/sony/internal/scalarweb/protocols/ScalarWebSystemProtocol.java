/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.ircc.models.IrccState;
import org.openhab.binding.sony.internal.net.Header;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConfig;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.scalarweb.models.api.CurrentTime;
import org.openhab.binding.sony.internal.scalarweb.models.api.Language;
import org.openhab.binding.sony.internal.scalarweb.models.api.LedIndicatorStatus;
import org.openhab.binding.sony.internal.scalarweb.models.api.PostalCode;
import org.openhab.binding.sony.internal.scalarweb.models.api.PowerSavingMode;
import org.openhab.binding.sony.internal.scalarweb.models.api.PowerStatus;
import org.openhab.binding.sony.internal.scalarweb.models.api.SystemInformation;
import org.openhab.binding.sony.internal.scalarweb.models.api.WolMode;
import org.openhab.binding.sony.internal.upnp.models.UpnpService;
import org.openhab.binding.sony.internal.upnp.models.UpnpServiceActionDescriptor;
import org.openhab.binding.sony.internal.upnp.models.UpnpServiceDescriptor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebSystemProtocol.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
class ScalarWebSystemProtocol<T extends ThingCallback<ScalarWebChannel>> extends AbstractScalarWebProtocol<T> {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebSystemProtocol.class);

    /** The Constant CURRENTTIME. */
    private final static String CURRENTTIME = "currenttime";

    /** The Constant LEDINDICATORSTATUS. */
    private final static String LEDINDICATORSTATUS = "ledindicatorstatus";

    /** The Constant POWERSAVINGMODE. */
    private final static String POWERSAVINGMODE = "powersavingmode";

    /** The Constant POWERSTATUS. */
    private final static String POWERSTATUS = "powerstatus";

    /** The Constant WOLMODE. */
    private final static String WOLMODE = "wolmode";

    /** The Constant LANGUAGE. */
    private final static String LANGUAGE = "language";

    /** The Constant REBOOT. */
    private final static String REBOOT = "reboot";

    /** The Constant SYSCMD. */
    private final static String SYSCMD = "sysCmd";

    /** The Constant POSTALCODE. */
    private final static String POSTALCODE = "postalcode";

    /** The config. */
    private final ScalarWebConfig config;

    /** The transform service. */
    private final TransformationService transformService;

    /** The http request. */
    private final HttpRequest httpRequest;

    /**
     * Instantiates a new scalar web system protocol.
     *
     * @param tracker the tracker
     * @param state the state
     * @param service the service
     * @param config the config
     * @param bundleContext the bundle context
     * @param callback the callback
     * @throws Exception the exception
     */
    ScalarWebSystemProtocol(ScalarWebChannelTracker tracker, ScalarWebState state, ScalarWebService service,
            ScalarWebConfig config, BundleContext bundleContext, T callback) throws Exception {
        super(tracker, state, service, callback);

        this.config = config;
        transformService = TransformationHelper.getTransformationService(bundleContext, "MAP");
        httpRequest = NetUtilities.createHttpRequest(config.getAccessCodeNbr());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#getChannelDescriptors()
     */
    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors() {

        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        try {
            execute(ScalarWebMethod.GetCurrentTime);
            descriptors.add(createDescriptor(createChannel(CURRENTTIME), "DateTime", "scalarsystemcurrenttime"));
        } catch (IOException e) {
            // not implemented
        }

        try {
            execute(ScalarWebMethod.GetSystemInformation);
            descriptors.add(createDescriptor(createChannel(LANGUAGE), "String", "scalarsystemlanguage"));
        } catch (IOException e) {
            // not implemented
        }

        try {
            execute(ScalarWebMethod.GetLedIndicatorStatus);
            descriptors.add(
                    createDescriptor(createChannel(LEDINDICATORSTATUS), "String", "scalarsystemledindicatorstatus"));
        } catch (IOException e) {
            // not implemented
        }

        try {
            execute(ScalarWebMethod.GetPowerSavingMode);
            descriptors.add(createDescriptor(createChannel(POWERSAVINGMODE), "String", "scalarsystempowersavingmode"));
        } catch (IOException e) {
            // not implemented
        }

        try {
            execute(ScalarWebMethod.GetPowerStatus);
            descriptors.add(createDescriptor(createChannel(POWERSTATUS), "Switch", "scalarsystempowerstatus"));
        } catch (IOException e) {
            // not implemented
        }

        try {
            execute(ScalarWebMethod.GetWolMode);
            descriptors.add(createDescriptor(createChannel(WOLMODE), "Switch", "scalarsystemwolmode"));
        } catch (IOException e) {
            // not implemented
        }

        try {
            execute(ScalarWebMethod.GetPostalCode);
            descriptors.add(createDescriptor(createChannel(POSTALCODE), "String", "scalarsystempostalcode"));
        } catch (IOException e) {
            // not implemented
        }

        // don't test the following to see if it's implemented - just assume it is if defined!
        if (service.getMethod(ScalarWebMethod.RequestReboot) != null) {
            descriptors.add(createDescriptor(createChannel(REBOOT), "Switch", "scalarsystemreboot"));
        }

        // ONLY if
        if (state.getIrccState() != null) {
            descriptors.add(createDescriptor(createChannel(SYSCMD), "String", "scalarsystemircc"));
        }

        return descriptors;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshState()
     */
    @Override
    public void refreshState() {
        if (isLinked(CURRENTTIME)) {
            refreshCurrentTime();
        }
        if (isLinked(LEDINDICATORSTATUS)) {
            refreshLedIndicator();
        }
        if (isLinked(LANGUAGE)) {
            refreshLanguage();
        }
        if (isLinked(POWERSAVINGMODE)) {
            refreshPowerSavingsMode();
        }
        if (isLinked(POWERSTATUS)) {
            refreshPowerStatus();
        }
        if (isLinked(WOLMODE)) {
            refreshWolMode();
        }
        if (isLinked(REBOOT)) {
            refreshReboot();
        }
        if (isLinked(POSTALCODE)) {
            refreshPostalCode();
        }
        if (isLinked(SYSCMD)) {
            refreshSysCmd();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel)
     */
    @Override
    public void refreshChannel(ScalarWebChannel channel) {
        switch (channel.getId()) {
            case CURRENTTIME:
                refreshCurrentTime();
                break;

            case LANGUAGE:
                refreshLanguage();
                break;

            case LEDINDICATORSTATUS:
                refreshLedIndicator();
                break;

            case POWERSAVINGMODE:
                refreshPowerSavingsMode();
                break;

            case POWERSTATUS:
                refreshPowerStatus();
                break;

            case WOLMODE:
                refreshWolMode();
                break;

            case REBOOT:
                refreshReboot();
                break;

            case POSTALCODE:
                refreshPostalCode();
                break;

            case SYSCMD:
                refreshSysCmd();
                break;

            default:
                logger.debug("Unknown refresh channel: {}", channel);
                break;
        }
    }

    /**
     * Refresh current time.
     */
    private void refreshCurrentTime() {
        try {
            final CurrentTime ct = execute(ScalarWebMethod.GetCurrentTime).as(CurrentTime.class);
            callback.stateChanged(createChannel(CURRENTTIME),
                    new DateTimeType(ct.getDateTime().toCalendar(Locale.getDefault())));
        } catch (IOException e) {
            // do nothing
        }

    }

    /**
     * Refresh language.
     */
    private void refreshLanguage() {
        try {
            final SystemInformation sysInfo = execute(ScalarWebMethod.GetSystemInformation).as(SystemInformation.class);
            callback.stateChanged(createChannel(LANGUAGE), new StringType(sysInfo.getLanguage()));
        } catch (IOException e) {
            // do nothing
        }

    }

    /**
     * Refresh led indicator.
     */
    private void refreshLedIndicator() {
        try {
            final LedIndicatorStatus ledStatus = execute(ScalarWebMethod.GetLedIndicatorStatus)
                    .as(LedIndicatorStatus.class);
            callback.stateChanged(createChannel(LEDINDICATORSTATUS), new StringType(ledStatus.getMode()));
        } catch (IOException e) {
            // do nothing
        }

    }

    /**
     * Refresh power savings mode.
     */
    private void refreshPowerSavingsMode() {
        try {
            final PowerSavingMode mode = execute(ScalarWebMethod.GetPowerSavingMode).as(PowerSavingMode.class);
            callback.stateChanged(createChannel(POWERSAVINGMODE), new StringType(mode.getMode()));
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Refresh postal code.
     */
    private void refreshPostalCode() {
        try {
            final PostalCode postalCode = execute(ScalarWebMethod.GetPostalCode).as(PostalCode.class);
            callback.stateChanged(createChannel(POSTALCODE), new StringType(postalCode.getPostalCode()));
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Refresh power status.
     */
    private void refreshPowerStatus() {
        try {
            final PowerStatus status = execute(ScalarWebMethod.GetPowerStatus).as(PowerStatus.class);
            callback.stateChanged(createChannel(POWERSTATUS), status.getStatus() ? OnOffType.ON : OnOffType.OFF);
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Refresh wol mode.
     */
    private void refreshWolMode() {
        try {
            final WolMode mode = execute(ScalarWebMethod.GetWolMode).as(WolMode.class);
            callback.stateChanged(createChannel(WOLMODE), mode.isEnabled() ? OnOffType.ON : OnOffType.OFF);
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Refresh reboot.
     */
    private void refreshReboot() {
        callback.stateChanged(createChannel(REBOOT), OnOffType.OFF);
    }

    /**
     * Refresh sys cmd.
     */
    private void refreshSysCmd() {
        callback.stateChanged(createChannel(SYSCMD), StringType.EMPTY);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#setChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel, org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void setChannel(ScalarWebChannel channel, Command command) {
        switch (channel.getId()) {
            case LEDINDICATORSTATUS:
                if (command instanceof StringType) {
                    setLedIndicatorStatus(command.toString());
                } else {
                    logger.debug("LEDINDICATORSTATUS command not an StringType: {}", command);
                }

                break;

            case LANGUAGE:
                if (command instanceof StringType) {
                    setLanguage(command.toString());
                } else {
                    logger.debug("LANGUAGE command not an StringType: {}", command);
                }

                break;

            case POWERSAVINGMODE:
                if (command instanceof StringType) {
                    setPowerSavingMode(command.toString());
                } else {
                    logger.debug("POWERSAVINGMODE command not an StringType: {}", command);
                }

                break;

            case POWERSTATUS:
                if (command instanceof OnOffType) {
                    setPowerStatus(command == OnOffType.ON);
                } else {
                    logger.debug("POWERSTATUS command not an OnOffType: {}", command);
                }

                break;

            case WOLMODE:
                if (command instanceof OnOffType) {
                    setWolMode(command == OnOffType.ON);
                } else {
                    logger.debug("WOLMODE command not an OnOffType: {}", command);
                }

                break;

            case REBOOT:
                if (command instanceof OnOffType && command == OnOffType.ON) {
                    requestReboot();
                } else {
                    logger.debug("REBOOT command not an OnOffType: {}", command);
                }

                break;

            case POSTALCODE:
                if (command instanceof StringType) {
                    setPostalCode(command.toString());
                } else {
                    logger.debug("POSTALCODE command not an StringType: {}", command);
                }

                break;

            case SYSCMD:
                if (command instanceof StringType) {
                    sendCommand(command.toString());
                } else {
                    logger.debug("SYSCMD command not an StringType: {}", command);
                }

                break;

            default:
                logger.debug("Unhandled channel command: {} - {}", channel, command);
                break;
        }
    }

    /**
     * Sets the led indicator status.
     *
     * @param mode the new led indicator status
     */
    private void setLedIndicatorStatus(String mode) {
        handleExecute(ScalarWebMethod.SetLedIndicatorStatus, new LedIndicatorStatus(mode, ""));
    }

    /**
     * Sets the language.
     *
     * @param language the new language
     */
    private void setLanguage(String language) {
        handleExecute(ScalarWebMethod.SetLanguage, new Language(language));
    }

    /**
     * Sets the power saving mode.
     *
     * @param mode the new power saving mode
     */
    private void setPowerSavingMode(String mode) {
        handleExecute(ScalarWebMethod.SetPowerSavingsMode, new PowerSavingMode(mode));
    }

    /**
     * Sets the power status.
     *
     * @param status the new power status
     */
    private void setPowerStatus(boolean status) {
        handleExecute(ScalarWebMethod.SetPowerStatus, new PowerStatus(status));
    }

    /**
     * Sets the wol mode.
     *
     * @param enabled the new wol mode
     */
    private void setWolMode(boolean enabled) {
        handleExecute(ScalarWebMethod.SetWolMode, new WolMode(enabled));
    }

    /**
     * Sets the postal code.
     *
     * @param postalCode the new postal code
     */
    private void setPostalCode(String postalCode) {
        handleExecute(ScalarWebMethod.SetPostalCode, new PostalCode(postalCode));
    }

    /**
     * Request reboot.
     */
    private void requestReboot() {
        handleExecute(ScalarWebMethod.RequestReboot);
    }

    /**
     * Send command.
     *
     * @param cmd the cmd
     */
    public void sendCommand(String cmd) {

        if (StringUtils.isEmpty(cmd)) {
            return;
        }

        final IrccState irccState = state.getIrccState();

        if (irccState == null) {
            logger.warn("IRCC service wasn't found");
            return;
        }

        final String cmdMap = config.getCommandsMapFile();

        if (transformService != null) {
            String code;
            try {
                code = transformService.transform(cmdMap, cmd);

                if (!StringUtils.isEmpty(code)) {
                    logger.debug("Transformed {} with map file '{}' to {}", cmd, cmdMap, code);

                    try {
                        cmd = URLDecoder.decode(code, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        cmd = code;
                    }
                }
            } catch (TransformationException e) {
                logger.error("Failed to transform {} using map file '{}', exception={}", cmd, cmdMap, e.getMessage());
                return;
            }
        }

        final UpnpService service = irccState.getService(IrccState.SRV_IRCC);
        if (service == null) {
            logger.error("IRCC Service was not found");
            return;
        }

        final UpnpServiceDescriptor desc = service.getServiceDescriptor();
        if (desc == null) {
            logger.error("IRCC Service Descriptor was not found");
            return;
        }

        final UpnpServiceActionDescriptor actionDescriptor = desc.getActionDescriptor(IrccState.SRV_ACTION_SENDIRCC);
        if (actionDescriptor == null) {
            logger.error("IRCC Service Action Descriptior was not found");
            return;
        }

        final String soap = actionDescriptor.getSoap(cmd);
        handleExecuteXml(service.getControlUrl(), soap,
                new Header("SOAPACTION", "\"" + service.getServiceType() + "#" + IrccState.SRV_ACTION_SENDIRCC + "\""));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.AbstractScalarWebProtocol#close()
     */
    @Override
    public void close() {
        super.close();
        httpRequest.close();
    }
}
