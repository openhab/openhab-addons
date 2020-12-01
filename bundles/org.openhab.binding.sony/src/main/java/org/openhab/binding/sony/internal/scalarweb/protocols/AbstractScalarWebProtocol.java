/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebContext;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebError;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebEvent;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.GeneralSetting;
import org.openhab.binding.sony.internal.scalarweb.models.api.GeneralSettingsCandidate;
import org.openhab.binding.sony.internal.scalarweb.models.api.GeneralSettingsRequest;
import org.openhab.binding.sony.internal.scalarweb.models.api.GeneralSettings_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.Notification;
import org.openhab.binding.sony.internal.scalarweb.models.api.Notifications;
import org.openhab.binding.sony.internal.scalarweb.models.api.NotifySettingUpdate;
import org.openhab.binding.sony.internal.scalarweb.models.api.NotifySettingUpdateApi;
import org.openhab.binding.sony.internal.scalarweb.models.api.NotifySettingUpdateApiMapping;
import org.openhab.binding.sony.internal.scalarweb.models.api.Source;
import org.openhab.binding.sony.internal.scalarweb.models.api.Target;
import org.openhab.binding.sony.internal.transports.SonyTransportListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the base of all scalar web protocols
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
public abstract class AbstractScalarWebProtocol<T extends ThingCallback<String>> implements ScalarWebProtocol<T> {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(AbstractScalarWebProtocol.class);

    // the property key to the setting type (boolean, number, etc)
    private static final String PROP_SETTINGTYPE = "settingType";

    // the property key to the device ui setting (slider, etc)
    private static final String PROP_DEVICEUI = "deviceUi";

    // following property only valid on device UI slider items
    // we need to save curr value if an increase/decrease type comes in
    private static final String PROP_CURRVALUE = "currValue";

    // The off value (used for the boolean off/false value and the current value of a number channel if mute was pressed
    // {saved when OFF, restored on ON})
    private static final String PROP_OFFVALUE = "offValue";

    // The on value (used for boolean on/true value)
    private static final String PROP_ONVALUE = "onValue";

    /** The context to use */
    private final ScalarWebContext context;

    /** The specific service for the protocol */
    protected final ScalarWebService service;

    /** The callback to use */
    protected final T callback;

    /** The factory used to for protocols */
    private final ScalarWebProtocolFactory<T> factory;

    /** The listener for transport events */
    private final Listener listener = new Listener();

    /** The API to category lookup for general settings (note: do we support version?) */
    private final Map<String, String> apiToCtgy = new ConcurrentHashMap<>();

    /**
     * Instantiates a new abstract scalar web protocol.
     *
     * @param factory the non-null factory to use
     * @param context the non-null context to use
     * @param service the non-null web service to use
     * @param callback the non-null callback to use
     */
    protected AbstractScalarWebProtocol(final ScalarWebProtocolFactory<T> factory, final ScalarWebContext context,
            final ScalarWebService service, final T callback) {
        Objects.requireNonNull(factory, "factory cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(service, "service cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        this.factory = factory;
        this.context = context;
        this.service = service;
        this.callback = callback;
    }

    /**
     * Helper method to enable a list of notifications
     * 
     * @param notificationEvents the list of notifications to enable
     * @return the notifications that were enabled/disabled as a result
     */
    protected Notifications enableNotifications(final String... notificationEvents) {
        if (service.hasMethod(ScalarWebMethod.SWITCHNOTIFICATIONS)) {
            try {
                final Notifications notifications = execute(ScalarWebMethod.SWITCHNOTIFICATIONS, new Notifications())
                        .as(Notifications.class);

                final Set<String> registered = new HashSet<>(Arrays.asList(notificationEvents));

                final List<Notification> newEnabled = new ArrayList<>(notifications.getEnabled());
                final List<Notification> newDisabled = new ArrayList<>(notifications.getDisabled());
                for (final Iterator<Notification> iter = newDisabled.listIterator(); iter.hasNext();) {
                    final Notification not = iter.next();
                    final String mthName = not.getName();

                    if (mthName != null && registered.contains(mthName)) {
                        newEnabled.add(not);
                        iter.remove();
                    }
                }

                if (newEnabled.isEmpty()) {
                    // return the original (since nothing changed)
                    return notifications;
                } else {
                    this.service.getTransport().addListener(listener);
                    // return the results rather than what we feed it since the server may reject some of ours
                    return execute(ScalarWebMethod.SWITCHNOTIFICATIONS, new Notifications(newEnabled, newDisabled))
                            .as(Notifications.class);
                }
            } catch (final IOException e) {
                logger.debug("switchNotifications doesn't exist - ignoring event processing");
                final List<Notification> disabled = Arrays.stream(notificationEvents)
                        .map(s -> new Notification(s, ScalarWebMethod.V1_0)).collect(Collectors.toList());
                return new Notifications(Collections.emptyList(), disabled);
            }
        }

        final List<Notification> disabled = Arrays.stream(notificationEvents)
                .map(s -> new Notification(s, ScalarWebMethod.V1_0)).collect(Collectors.toList());
        return new Notifications(Collections.emptyList(), disabled);
    }

    /**
     * Default implementation for the eventReceived and does nothing
     * 
     * @param event the event
     * @throws IOException never thrown by the default implementation
     */
    protected void eventReceived(final ScalarWebEvent event) throws IOException {
        // do nothing
    }

    /**
     * Returns the {@link ScalarWebService} for the give service name
     * 
     * @param serviceName a non-null, non-empty service name
     * @return a {@link ScalarWebService} or null if not found
     */
    protected @Nullable ScalarWebService getService(final String serviceName) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        if (StringUtils.equals(serviceName, service.getServiceName())) {
            return service;
        }

        final ScalarWebProtocol<T> protocol = factory.getProtocol(serviceName);
        return protocol == null ? null : protocol.getService();
    }

    /**
     * Returns the protocol factory used by this protocol
     * 
     * @return a non-null {@link ScalarWebProtocolFactory}
     */
    protected ScalarWebProtocolFactory<T> getFactory() {
        return factory;
    }

    /**
     * Returns the context used for the protocol
     * 
     * @return a non-null {@link ScalarWebContext}
     */
    protected ScalarWebContext getContext() {
        return context;
    }

    /**
     * Returns the service related to this protocol
     *
     * @return the non-null service
     */
    @Override
    public ScalarWebService getService() {
        return service;
    }

    /**
     * Execute the given method name with the specified parameters
     *
     * @param mthd a non-null non-empty method
     * @param getParms a non-null get parameters method
     * @return the scalar web result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ScalarWebResult execute(final String mthd, final GetParms getParms) throws IOException {
        Validate.notEmpty(mthd, "mthd cannot be empty");
        Objects.requireNonNull(getParms, "getParms cannot be empty");
        final String version = getService().getVersion(mthd);
        if (version == null || StringUtils.isEmpty(version)) {
            logger.debug("Can't find a version for method {} - ignoring", mthd);
            return ScalarWebResult.createNotImplemented(mthd);
        }
        final Object parms = getParms.getParms(version);
        return execute(mthd, parms == null ? new Object[0] : parms);
    }

    /**
     * Execute the given method name with the specified parameters
     *
     * @param mthd a non-null non-empty method
     * @param parms the parameters to use
     * @return the scalar web result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ScalarWebResult execute(final String mthd, final Object... parms) throws IOException {
        Validate.notEmpty(mthd, "mthd cannot be empty");
        final ScalarWebResult result = handleExecute(mthd, parms);
        if (result.isError()) {
            throw result.getHttpResponse().createException();
        }

        return result;
    }

    /**
     * Handles the execution of a method with parameters
     *
     * @param mthd a non-null non-empty method
     * @param getParms a non-null get parameters method
     * @return a non-null result
     */
    protected ScalarWebResult handleExecute(final String mthd, final GetParms getParms) {
        Validate.notEmpty(mthd, "mthd cannot be empty");
        Objects.requireNonNull(getParms, "getParms cannot be empty");

        final String version = getService().getVersion(mthd);
        if (version == null || StringUtils.isEmpty(version)) {
            logger.debug("Can't find a version for method {} - ignoring", mthd);
            return ScalarWebResult.createNotImplemented(mthd);
        }
        final Object parms = getParms.getParms(version);
        return handleExecute(mthd, parms == null ? new Object[0] : parms);
    }

    /**
     * Handles the execution of a method with parameters
     *
     * @param mthd the method name to execute
     * @param parms the parameters to use
     * @return the scalar web result
     */
    protected ScalarWebResult handleExecute(final String mthd, final Object... parms) {
        Validate.notEmpty(mthd, "mthd cannot be empty");
        final ScalarWebResult result = service.execute(mthd, parms);
        if (result.isError()) {
            switch (result.getDeviceErrorCode()) {
                case ScalarWebError.NOTIMPLEMENTED:
                    logger.debug("Method is not implemented on service {} - {}({}): {}", service.getServiceName(), mthd,
                            StringUtils.join(parms, ','), result.getDeviceErrorDesc());
                    break;

                case ScalarWebError.ILLEGALARGUMENT:
                    logger.debug("Method arguments are incorrect on service {} - {}({}): {}", service.getServiceName(),
                            mthd, StringUtils.join(parms, ','), result.getDeviceErrorDesc());
                    break;

                case ScalarWebError.ILLEGALSTATE:
                    logger.debug("Method state is incorrect on service {} - {}({}): {}", service.getServiceName(), mthd,
                            StringUtils.join(parms, ','), result.getDeviceErrorDesc());
                    break;

                case ScalarWebError.DISPLAYISOFF:
                    logger.debug("The display is off and command cannot be executed on service {} - {}({}): {}",
                            service.getServiceName(), mthd, StringUtils.join(parms, ','), result.getDeviceErrorDesc());
                    break;

                case ScalarWebError.FAILEDTOLAUNCH:
                    logger.debug("The application failed to launch (probably display is off) {} - {}({}): {}",
                            service.getServiceName(), mthd, StringUtils.join(parms, ','), result.getDeviceErrorDesc());
                    break;

                case ScalarWebError.HTTPERROR:
                    final IOException e = result.getHttpResponse().createException();
                    logger.debug("Communication error executing method {}({}) on service {}: {}", mthd,
                            StringUtils.join(parms, ','), service.getServiceName(), e.getMessage(), e);
                    callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    break;

                default:
                    logger.debug("Device error ({}) on service {} - {}({}): {}", result.getDeviceErrorCode(),
                            service.getServiceName(), mthd, StringUtils.join(parms, ','), result.getDeviceErrorDesc());
                    break;
            }
        }

        return result;
    }

    /**
     * Creates a scalar web channel for the given id with potentially additional
     * paths
     *
     * @param id the non-null, non-empty channel identifier
     * @return the scalar web channel
     */
    protected ScalarWebChannel createChannel(final String id) {
        Validate.notEmpty(id, "id cannot be empty");
        return createChannel(id, id, new String[0]);
    }

    /**
     * Creates a scalar web channel for the given id with potentially additional
     * paths
     *
     * @param category the non-null, non-empty channel category
     * @param id the non-null, non-empty channel identifier
     * @param addtlPaths the potential other paths
     * @return the scalar web channel
     */
    protected ScalarWebChannel createChannel(final String category, final String id, final String... addtlPaths) {
        Validate.notEmpty(category, "category cannot be empty");
        Validate.notEmpty(id, "id cannot be empty");
        return new ScalarWebChannel(service.getServiceName(), category, id, addtlPaths);
    }

    /**
     * Creates the channel descriptor for the given channel, item type and channel
     * type
     *
     * @param channel the non-null channel
     * @param acceptedItemType the non-null, non-empty accepted item type
     * @param channelType the non-null, non-empty channel type
     * @return the scalar web channel descriptor
     */
    protected ScalarWebChannelDescriptor createDescriptor(final ScalarWebChannel channel, final String acceptedItemType,
            final String channelType) {
        Objects.requireNonNull(channel, "channel cannot be empty");
        Validate.notEmpty(acceptedItemType, "acceptedItemType cannot be empty");
        Validate.notEmpty(channelType, "channelType cannot be empty");
        return createDescriptor(channel, acceptedItemType, channelType, null, null);
    }

    /**
     * Creates the descriptor from the given parameters
     *
     * @param channel the non-null channel
     * @param acceptedItemType the non-null, non-empty accepted item type
     * @param channelType the non-null, non-empty channel type
     * @param label the potentially null, potentially empty label
     * @param description the potentially null, potentially empty description
     * @return the scalar web channel descriptor
     */
    protected ScalarWebChannelDescriptor createDescriptor(final ScalarWebChannel channel, final String acceptedItemType,
            final String channelType, final @Nullable String label, final @Nullable String description) {
        Objects.requireNonNull(channel, "channel cannot be empty");
        Validate.notEmpty(acceptedItemType, "acceptedItemType cannot be empty");
        Validate.notEmpty(channelType, "channelType cannot be empty");
        return new ScalarWebChannelDescriptor(channel, acceptedItemType, channelType, label, description);
    }

    /**
     * Helper method to issue a state changed for the simple id (where category=id)
     *
     * @param id the non-null, non-empty id
     * @param state the non-null new state
     */
    protected void stateChanged(final String id, final State state) {
        Validate.notEmpty(id, "id cannot be empty");
        Objects.requireNonNull(state, "state cannot be empty");
        stateChanged(id, id, state);
    }

    /**
     * Helper method to issue a state changed for the ctgy/id
     *
     * @param category the non-null, non-empty category
     * @param id the non-null, non-empty id
     * @param state the non-null new state
     */
    protected void stateChanged(final String category, final String id, final State state) {
        Validate.notEmpty(category, "category cannot be empty");
        Validate.notEmpty(id, "id cannot be empty");
        Objects.requireNonNull(state, "state cannot be empty");
        callback.stateChanged(
                SonyUtil.createChannelId(service.getServiceName(), ScalarWebChannel.createChannelId(category, id)),
                state);
    }

    /**
     * Returns the channel tracker
     *
     * @return the non-null channel tracker
     */
    protected ScalarWebChannelTracker getChannelTracker() {
        return context.getTracker();
    }

    /**
     * Helper method to return a method's latest version
     *
     * @param methodName a non-null, non-empty method name
     * @return a possibly null (if not found) method's latest version
     */
    protected @Nullable String getVersion(final String methodName) {
        Validate.notEmpty(methodName, "methodName cannot be empty");
        return getService().getVersion(methodName);
    }

    /**
     * Adds one or more general settings descriptors based on the general settings retrived from the given menthod
     * 
     * @param descriptors a non-null, possibly empty list of descriptors
     * @param getMethodName a non-null, non-empty get method name to retrieve settings from
     * @param ctgy a non-null, non-empty openhab category
     * @param prefix a non-null, non-empty prefix for descriptor names/labels
     */
    protected void addGeneralSettingsDescriptor(final List<ScalarWebChannelDescriptor> descriptors,
            final String getMethodName, final String ctgy, final String prefix) {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");
        Validate.notEmpty(getMethodName, "getMethodName cannot be empty");
        Validate.notEmpty(ctgy, "ctgy cannot be empty");
        Validate.notEmpty(prefix, "prefix cannot be empty");

        try {
            // TODO support versioning of the getMethodName?
            final GeneralSettings_1_0 ss = execute(getMethodName, new Target()).as(GeneralSettings_1_0.class);

            for (final GeneralSetting set : ss.getSettings()) {
                // Seems isAvailable is not a reliable call (many false when in fact it's true)
                // if (!set.isAvailable()) {
                // logger.debug("{} isn't available {} - ignoring", prefix, set);
                // continue;
                // }
                final String target = set.getTarget();
                if (target == null || StringUtils.isEmpty(target)) {
                    logger.debug("Target not valid for {} {} - ignoring", prefix, set);
                    continue;
                }

                final String uri = StringUtils.defaultIfEmpty(set.getUri(), null);
                final String uriLabel = uri == null ? null
                        : StringUtils.defaultIfEmpty(Source.getSourcePart(uri), null);

                final String label = textLookup(set.getTitle(), target)
                        + (uriLabel == null ? "" : String.format(" (%s)", uriLabel));
                final String id = getGeneralSettingChannelId(target, uri);
                final List<GeneralSettingsCandidate> candidates = SonyUtil.convertNull(set.getCandidate());

                String settingType = set.getType();
                // -- no explicit type - try guessing it from the value
                if (settingType == null || StringUtils.isEmpty(settingType)) {
                    String currValue = set.getCurrentValue();
                    if (currValue == null || StringUtils.isEmpty(currValue)) {
                        // No value - get the first non-null/non-empty one from the candidates
                        currValue = candidates.stream().map(e -> e.getValue()).filter(e -> StringUtils.isNotEmpty(e))
                                .findFirst().orElse(null);
                    }

                    if (BooleanUtils.toBooleanObject(currValue) != null) {
                        // we'll further validate the boolean target candidates below
                        settingType = GeneralSetting.BOOLEANTARGET;
                    } else if (NumberUtils.isNumber(currValue)) {
                        settingType = StringUtils.isNumeric(currValue) ? GeneralSetting.INTEGERTARGET
                                : GeneralSetting.DOUBLETARGET;
                    } else {
                        settingType = candidates.size() > 0 ? GeneralSetting.ENUMTARGET : GeneralSetting.STRINGTARGET;
                    }
                }

                final ScalarWebChannel channel = createChannel(ctgy, id, target, uri);

                final String ui = set.getDeviceUIInfo();
                if (ui == null || StringUtils.isEmpty(ui)) {
                    if (candidates.size() > 0) {
                        final GeneralSettingsCandidate candidate = candidates.get(0);
                        if (candidate != null && candidate.getMax() != null && candidate.getMin() != null
                                && candidate.getStep() != null) {
                            channel.addProperty(PROP_DEVICEUI, GeneralSetting.SLIDER);
                        }
                    }
                } else {
                    channel.addProperty(PROP_DEVICEUI, ui);
                }
                channel.addProperty(PROP_SETTINGTYPE, settingType);

                StateDescriptionFragmentBuilder bld = StateDescriptionFragmentBuilder.create();
                if (candidates.size() == 0) {
                    bld = bld.withReadOnly(Boolean.TRUE);
                }

                // Make sure we actually have a boolean target:
                // 1. Must be 2 in size
                // 2. Both need to be a valid Boolean (must be on/off, true/false, yes/no)
                // 3. Both cannot be the same (must be true/false or false/true)
                // If all three aren't true - revert to an enum target
                // If they are true - save the actual value used for the boolean (on/off or true/false or yes/no)
                if (StringUtils.equals(settingType, GeneralSetting.BOOLEANTARGET) && candidates.size() > 0) {
                    if (candidates.size() != 2) {
                        settingType = GeneralSetting.ENUMTARGET;
                    } else {
                        final @Nullable String value1 = candidates.get(0).getValue();
                        final @Nullable String value2 = candidates.get(1).getValue();
                        final @Nullable Boolean bool1 = BooleanUtils.toBooleanObject(value1);
                        final @Nullable Boolean bool2 = BooleanUtils.toBooleanObject(value2);

                        if (value1 == null || value2 == null || bool1 == null || bool2 == null || bool1.equals(bool2)) {
                            settingType = GeneralSetting.ENUMTARGET;
                        } else {
                            channel.addProperty(PROP_OFFVALUE, bool1 == Boolean.FALSE ? value1 : value2);
                            channel.addProperty(PROP_ONVALUE, bool1 == Boolean.TRUE ? value1 : value2);
                        }
                    }
                }

                switch (settingType) {
                    case GeneralSetting.BOOLEANTARGET:
                        descriptors.add(createDescriptor(channel, "Switch", "scalargeneralsettingswitch",
                                prefix + " " + label, prefix + " for " + label));

                        break;
                    case GeneralSetting.DOUBLETARGET:
                        if (set.isUiSlider()) {
                            descriptors.add(createDescriptor(channel, "Dimmer", "scalargeneralsettingdimmer",
                                    prefix + " " + label, prefix + " for " + label));

                        } else {
                            descriptors.add(createDescriptor(channel, "Number", "scalargeneralsettingnumber",
                                    prefix + " " + label, prefix + " for " + label));
                        }

                        if (candidates.size() > 0) {
                            final GeneralSettingsCandidate candidate = candidates.get(0);

                            final Double min = candidate.getMin(), max = candidate.getMax(), step = candidate.getStep();
                            if (min != null || max != null || step != null) {
                                final List<StateOption> options = new ArrayList<>();
                                if (set.isUiPicker()) {
                                    final double dmin = min == null || min.isInfinite() || min.isNaN() ? 0
                                            : min.doubleValue();
                                    final double dmax = max == null || max.isInfinite() || max.isNaN() ? 100
                                            : max.doubleValue();
                                    final double dstep = step == null || step.isInfinite() || step.isNaN() ? 1
                                            : step.doubleValue();
                                    for (double p = dmin; p <= dmax; p += (dstep == 0 ? 1 : dstep)) {
                                        final String opt = Double.toString(p);
                                        options.add(new StateOption(opt, opt));
                                    }
                                }
                                if (min != null) {
                                    bld = bld.withMinimum(new BigDecimal(min));
                                }
                                if (max != null) {
                                    bld = bld.withMaximum(new BigDecimal(max));
                                }
                                if (step != null) {
                                    bld = bld.withStep(new BigDecimal(step));
                                }
                                if (!options.isEmpty()) {
                                    bld = bld.withOptions(options);
                                }
                            }
                        }

                        break;
                    case GeneralSetting.INTEGERTARGET:
                        if (set.isUiSlider()) {
                            descriptors.add(createDescriptor(channel, "Dimmer", "scalargeneralsettingdimmer",
                                    prefix + " " + label, prefix + " for " + label));

                        } else {
                            descriptors.add(createDescriptor(channel, "Number", "scalargeneralsettingnumber",
                                    prefix + " " + label, prefix + " for " + label));
                        }

                        if (candidates.size() > 0) {
                            final GeneralSettingsCandidate candidate = candidates.get(0);

                            final Double min = candidate.getMin(), max = candidate.getMax(), step = candidate.getStep();
                            if (min != null || max != null || step != null) {
                                final List<StateOption> options = new ArrayList<>();
                                if (set.isUiPicker()) {
                                    final int imin = min == null || min.isInfinite() || min.isNaN() ? 0
                                            : min.intValue();
                                    final int imax = max == null || max.isInfinite() || max.isNaN() ? 100
                                            : max.intValue();
                                    final int istep = step == null || step.isInfinite() || step.isNaN() ? 1
                                            : step.intValue();
                                    for (int p = imin; p <= imax; p += (istep == 0 ? 1 : istep)) {
                                        final String opt = Double.toString(p);
                                        options.add(new StateOption(opt, opt));
                                    }
                                }

                                if (min != null) {
                                    bld = bld.withMinimum(new BigDecimal(min));
                                }
                                if (max != null) {
                                    bld = bld.withMaximum(new BigDecimal(max));
                                }
                                if (step != null) {
                                    bld = bld.withStep(new BigDecimal(step));
                                }
                                if (!options.isEmpty()) {
                                    bld = bld.withOptions(options);
                                }
                            }
                        }

                        break;

                    case GeneralSetting.ENUMTARGET:
                        descriptors.add(createDescriptor(channel, "String", "scalargeneralsettingstring",
                                prefix + " " + label, prefix + " for " + label));

                        final List<StateOption> stateInfo = candidates.stream().map(c -> {
                            if (c == null) {
                                return null;
                            }
                            final String stateVal = c.getValue();
                            if (stateVal == null || StringUtils.isEmpty(stateVal)) {
                                return null;
                            }
                            final String stateTitle = textLookup(c.getTitle(), null);
                            if (stateTitle == null || StringUtils.isEmpty(stateTitle)) {
                                return null;
                            }
                            return new StateOption(stateVal, stateTitle);
                        }).filter(c -> c != null).collect(Collectors.toList());

                        if (!stateInfo.isEmpty()) {
                            bld = bld.withOptions(stateInfo);
                        }

                        break;
                    default:
                        descriptors.add(createDescriptor(channel, "String", "scalargeneralsettingstring",
                                prefix + " " + label, prefix + " for " + label));
                        break;
                }

                final StateDescription sd = bld.build().toStateDescription();
                if (sd != null) {
                    getContext().getStateProvider().addStateOverride(getContext().getThingUID(), channel.getChannelId(),
                            sd);
                }
            }

            apiToCtgy.put(getMethodName, ctgy);

        } catch (final IOException e) {
            // ignore - probably not handled
        }
    }

    /**
     * Helper method to create a general settings channel id
     * 
     * @param target a non-null, non-empty target
     * @param uri a possibly null, possibly empty uri
     * @return
     */
    private @Nullable String getGeneralSettingChannelId(String target, @Nullable String uri) {
        Validate.notEmpty(target, "target cannot be empty");
        return SonyUtil.createValidChannelUId(target + (uri == null || StringUtils.isEmpty(uri) ? "" : "-" + uri));
    }

    /**
     * Refreshs the general sttings for a list of channels
     * 
     * @param channels a non-null, possibly empty set of {@link ScalarWebChannel}
     * @param getMethodName a non-null, non-empty method name to get settings from
     */
    protected void refreshGeneralSettings(final Set<ScalarWebChannel> channels, final String getMethodName) {
        Objects.requireNonNull(channels, "channels cannot be null");
        Validate.notEmpty(getMethodName, "getMethodName cannot be empty");

        try {
            final GeneralSettings_1_0 ss = handleExecute(getMethodName, new Target()).as(GeneralSettings_1_0.class);
            refreshGeneralSettings(ss.getSettings(), channels);
        } catch (final IOException e) {
            logger.debug("Error in refreshing general settings: {}", e.getMessage(), e);
        }
    }

    /**
     * Refreshs the general settings for a list of channels and their openHAB category
     * 
     * @param settings a non-null, possibly empty list of {@link GeneralSetting}
     * @param channels a non-null, possibly empty set of {@link ScalarWebChannel}
     */
    protected void refreshGeneralSettings(final List<GeneralSetting> settings, final Set<ScalarWebChannel> channels) {
        Objects.requireNonNull(settings, "settings cannot be null");
        Objects.requireNonNull(channels, "channels cannot be null");

        final Map<Map.Entry<String, String>, GeneralSetting> settingValues = new HashMap<>();
        for (final GeneralSetting set : settings) {
            final String target = set.getTarget();
            if (target == null || StringUtils.isEmpty(target)) {
                continue;
            }
            final Map.Entry<String, String> key = new AbstractMap.SimpleEntry<>(target,
                    StringUtils.defaultIfEmpty(set.getUri(), ""));
            settingValues.put(key, set);
        }

        for (final ScalarWebChannel chl : channels) {
            final String target = chl.getPathPart(0);
            if (target == null) {
                logger.debug("Cannot refresh general setting {} - has no target", chl);
                continue;
            }

            final String uri = StringUtils.defaultIfEmpty(chl.getPathPart(1), "");
            final Map.Entry<String, String> key = new AbstractMap.SimpleEntry<>(target, uri);

            final GeneralSetting setting = settingValues.get(key);
            if (setting == null) {
                logger.debug("Could not find a setting for {} ({})", target, uri);
                continue;
            }

            final String currentValue = setting.getCurrentValue();

            final String settingType = chl.getProperty(PROP_SETTINGTYPE,
                    StringUtils.defaultIfEmpty(setting.getType(), GeneralSetting.STRINGTARGET));
            final String ui = chl.getProperty(PROP_DEVICEUI, StringUtils.defaultIfEmpty(setting.getDeviceUIInfo(), ""));

            switch (settingType) {
                case GeneralSetting.BOOLEANTARGET:
                    final String onValue = StringUtils.defaultIfEmpty(chl.getProperty(PROP_ONVALUE),
                            GeneralSetting.DEFAULTON);

                    stateChanged(chl.getCategory(), chl.getId(),
                            StringUtils.equalsIgnoreCase(currentValue, onValue) ? OnOffType.ON : OnOffType.OFF);
                    break;

                case GeneralSetting.DOUBLETARGET:
                case GeneralSetting.INTEGERTARGET:
                    if (StringUtils.containsIgnoreCase(ui, GeneralSetting.SLIDER)) {
                        final StateDescription sd = getContext().getStateProvider()
                                .getStateDescription(getContext().getThingUID(), chl.getChannelId());
                        final BigDecimal min = sd == null ? BigDecimal.ZERO : sd.getMinimum();
                        final BigDecimal max = sd == null ? SonyUtil.BIGDECIMAL_HUNDRED : sd.getMaximum();
                        try {
                            final BigDecimal currVal = currentValue == null ? BigDecimal.ZERO
                                    : new BigDecimal(currentValue);
                            final BigDecimal val = SonyUtil.scale(currVal, min, max);
                            chl.addProperty(PROP_CURRVALUE, currVal.toString());

                            if (settingType.equals(GeneralSetting.INTEGERTARGET)) {
                                stateChanged(chl.getCategory(), chl.getId(),
                                        SonyUtil.newPercentType(val.setScale(0, RoundingMode.FLOOR)));
                            } else {
                                stateChanged(chl.getCategory(), chl.getId(), SonyUtil.newPercentType(val));
                            }
                        } catch (final NumberFormatException e) {
                            logger.debug("Current value {} was not a valid integer", currentValue);
                        }
                    } else {
                        stateChanged(chl.getCategory(), chl.getId(), SonyUtil.newDecimalType(currentValue));
                    }
                    break;

                default:
                    stateChanged(chl.getCategory(), chl.getId(), SonyUtil.newStringType(currentValue));
                    break;
            }
        }
    }

    /**
     * Sets a general setting. This method will take a method and channel and execute a command against it.
     * 
     * @param method a non-null, non-empty method
     * @param chl a non-null channel describing the setting
     * @param cmd a non-null command to execute
     */
    protected void setGeneralSetting(final String method, final ScalarWebChannel chl, final Command cmd) {
        Validate.notEmpty(method, "method cannot be empty");
        Objects.requireNonNull(chl, "chl cannot be null");
        Objects.requireNonNull(cmd, "cmd cannot be null");

        final String target = StringUtils.defaultIfEmpty(chl.getPathPart(0), null);
        if (target == null) {
            logger.debug("Cannot set general setting {} for channel {} because it has no target: {}", method, chl, cmd);
            return;
        }

        final String uri = StringUtils.defaultIfEmpty(chl.getPathPart(1), null);

        final String settingType = chl.getProperty(PROP_SETTINGTYPE, GeneralSetting.STRINGTARGET);
        final String deviceUi = chl.getProperty(PROP_DEVICEUI);

        final StateDescription sd = getContext().getStateProvider().getStateDescription(getContext().getThingUID(),
                chl.getChannelId());
        if (sd != null & sd.isReadOnly()) {
            logger.debug("Method {} ({}) is readonly - ignoring: {}", method, target, cmd);
            return;
        }

        switch (settingType) {
            case GeneralSetting.BOOLEANTARGET:
                if (cmd instanceof OnOffType) {
                    final String onValue = StringUtils.defaultIfEmpty(chl.getProperty(PROP_ONVALUE),
                            GeneralSetting.DEFAULTON);
                    final String offValue = StringUtils.defaultIfEmpty(chl.getProperty(PROP_OFFVALUE),
                            GeneralSetting.DEFAULTOFF);

                    handleExecute(method,
                            new GeneralSettingsRequest(target, cmd == OnOffType.ON ? onValue : offValue, uri));
                } else {
                    logger.debug("{} command not an OnOffType: {}", method, cmd);
                }
                break;

            case GeneralSetting.DOUBLETARGET:
            case GeneralSetting.INTEGERTARGET:
                if (StringUtils.contains(deviceUi, GeneralSetting.SLIDER)) {
                    final BigDecimal sdMin = sd == null ? null : sd.getMinimum();
                    final BigDecimal sdMax = sd == null ? null : sd.getMaximum();
                    final BigDecimal min = sdMin == null ? BigDecimal.ZERO : sdMin;
                    final BigDecimal max = sdMax == null ? SonyUtil.BIGDECIMAL_HUNDRED : sdMax;

                    final String propVal = chl.getProperty(PROP_CURRVALUE, "0");
                    final String offPropVal = chl.removeProperty(PROP_OFFVALUE);

                    try {
                        final BigDecimal currVal = SonyUtil.guard(new BigDecimal(propVal), min, max);

                        BigDecimal newVal;
                        if (cmd instanceof OnOffType) {
                            if (cmd == OnOffType.OFF) {
                                chl.addProperty(PROP_OFFVALUE, propVal);
                                newVal = min;
                            } else {
                                // if no prior off value, go with min instead
                                newVal = offPropVal == null ? min
                                        : SonyUtil.guard(new BigDecimal(offPropVal), min, max);
                            }

                        } else if (cmd instanceof PercentType) {
                            newVal = SonyUtil.unscale(((PercentType) cmd).toBigDecimal(), min, max);
                        } else if (cmd instanceof IncreaseDecreaseType) {
                            newVal = SonyUtil.guard(cmd == IncreaseDecreaseType.INCREASE ? currVal.add(BigDecimal.ONE)
                                    : currVal.subtract(BigDecimal.ONE), min, max);
                        } else {
                            logger.debug("{} command not an dimmer type: {}", method, cmd);
                            return;
                        }
                        if (settingType.equals(GeneralSetting.INTEGERTARGET)) {
                            handleExecute(method,
                                    new GeneralSettingsRequest(target, Integer.toString(newVal.intValue()), uri));
                        } else {
                            handleExecute(method,
                                    new GeneralSettingsRequest(target, Double.toString(newVal.doubleValue()), uri));
                        }
                    } catch (final NumberFormatException e) {
                        logger.debug("{} command current/off value not a valid number - either {} or {}: {}", method,
                                propVal, offPropVal, e.getMessage());
                    }
                } else {
                    if (cmd instanceof DecimalType) {
                        if (settingType.equals(GeneralSetting.INTEGERTARGET)) {
                            handleExecute(method, new GeneralSettingsRequest(target,
                                    Integer.toString(((DecimalType) cmd).intValue()), uri));
                        } else {
                            handleExecute(method, new GeneralSettingsRequest(target,
                                    Double.toString(((DecimalType) cmd).doubleValue()), uri));
                        }
                    } else {
                        logger.debug("{} command not an DecimalType: {}", method, cmd);
                    }
                }
                break;

            // handles both String and Enum types
            default:
                if (cmd instanceof StringType) {
                    handleExecute(method, new GeneralSettingsRequest(target, ((StringType) cmd).toString(), uri));
                } else {
                    logger.debug("{} command not an StringType: {}", method, cmd);
                }
                break;
        }
    }

    /**
     * Called when notifying the protocol of a settings update
     * 
     * @param setting a non-null notify
     */
    public void notifySettingUpdate(final NotifySettingUpdate notify) {
        Objects.requireNonNull(notify, "notify cannot be null");

        final @Nullable NotifySettingUpdateApiMapping apiMappingUpdate = notify.getApiMappingUpdate();
        if (apiMappingUpdate == null) {
            logger.debug("Received a notifySettingUpdate with no api mapping - ignoring: {}", notify);
            return;
        }

        final String serviceName = apiMappingUpdate.getService();
        if (serviceName == null || StringUtils.isEmpty(serviceName)) {
            logger.debug("Received a notifySettingUpdate with no service name - ignoring: {}", notify);
            return;
        }

        if (StringUtils.equalsIgnoreCase(serviceName, getService().getServiceName())) {
            internalNotifySettingUpdate(notify);
        } else {
            final @Nullable ScalarWebProtocol<T> protocol = getFactory().getProtocol(serviceName);
            if (protocol == null) {
                logger.debug("Received a notifySettingUpdate for an unknown service: {} - {}", serviceName, notify);
                return;
            }
            protocol.notifySettingUpdate(notify);
        }
    }

    /**
     * Internal application of the settings update notification.
     * 
     * @param setting a non-null setting
     */
    private void internalNotifySettingUpdate(final NotifySettingUpdate setting) {
        Objects.requireNonNull(setting, "setting cannot be null");

        final NotifySettingUpdateApiMapping apiMapping = setting.getApiMappingUpdate();
        if (apiMapping == null) {
            logger.debug("Trying to update setting but apiMapping was null: {}", setting);
            return;
        }

        final String target = apiMapping.getTarget();
        if (target == null || StringUtils.isEmpty(target)) {
            logger.debug("Trying to update setting but target is empty: {}", setting);
            return;
        }

        // Create our settings from the notification (only need a few of the attributes - see refreshGeneralSettings)
        final List<GeneralSetting> settings = Collections.singletonList(
                new GeneralSetting(target, apiMapping.getUri(), setting.getType(), apiMapping.getCurrentValue()));

        final NotifySettingUpdateApi getApi = apiMapping.getGetApi();
        if (getApi == null) {
            logger.debug("Trying to update setting but getAPI was missing: {}", setting);
            return;
        }

        final String getMethodName = getApi.getName();
        if (getMethodName == null || StringUtils.isEmpty(getMethodName)) {
            logger.debug("Trying to update setting but getAPI had no name: {}", setting);
            return;
        }

        final String ctgy = apiToCtgy.get(getMethodName);
        if (ctgy == null || StringUtils.isEmpty(ctgy)) {
            logger.debug("Trying to update setting but couldn't find the category for the getAPI {}: {}", getMethodName,
                    setting);
            return;

        }

        final String id = getGeneralSettingChannelId(target, apiMapping.getUri());

        // Get the channels linked to that category and for that identifier
        final Set<ScalarWebChannel> channels = getChannelTracker().getLinkedChannelsForCategory(ctgy).stream()
                .filter(e -> StringUtils.equalsIgnoreCase(e.getId(), id)).collect(Collectors.toSet());

        refreshGeneralSettings(settings, channels);
    }

    /**
     * Helper function to lookup common sony text and translate to a better name
     * 
     * @param text a possibly null, possibly empty text string
     * @param target a posssibly null, possibly empty target (as a text backup)
     * @return the translated text or text if not recognized
     */
    private static @Nullable String textLookup(final @Nullable String text, final @Nullable String target) {
        if (StringUtils.equalsIgnoreCase("IDMR_TEXT_FOOTBALL_STRING", text)) {
            return "Football";
        }

        if (StringUtils.equalsIgnoreCase("IDMR_TEXT_NARRATION_OFF_STRING", text)) {
            return "Narration Off";
        }

        if (StringUtils.equalsIgnoreCase("IDMR_TEXT_NARRATION_ON_STRING", text)) {
            return "Narration On";
        }

        if (StringUtils.equalsIgnoreCase("IDMR_TEXT_XMBSETUP_GRACENOTESETTING_STRING", text)) {
            return "Gracenote";
        }

        if (StringUtils.equalsIgnoreCase("IDMR_TEXT_SETUP_PULLDOWN_MANUAL_STRING", text)) {
            return "Manual";
        }

        if (StringUtils.equalsIgnoreCase("IDMR_TEXT_COMMON_AUTO_STRING", text)) {
            return "Auto";
        }

        if (StringUtils.equalsIgnoreCase("IDMR_TEXT_XMBSETUP_REMOTE_START_STRING", text)) {
            return "Remote Start";
        }

        if (StringUtils.equalsIgnoreCase("IDMR_TEXT_BT_DEVICE_NAME_STRING", text)) {
            return "Device Name";
        }

        if (text != null && StringUtils.isNotEmpty(text)) {
            return text;
        }

        // If we have a target, un-camel case it
        return target == null || StringUtils.isEmpty(target) ? "Unknown"
                : target.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
    }

    @Override
    public void close() {
        service.getTransport().close();
    }

    /**
     * This class represents the listener to sony events and will forward those
     * events on to the protocol implementation if they have a method of the same
     * name as the event
     *
     * @author Tim Roberts - Initial contribution
     */
    @NonNullByDefault
    private class Listener implements SonyTransportListener {
        @Override
        public void onEvent(final ScalarWebEvent event) {
            Objects.requireNonNull(event, "event cannot be null");
            context.getScheduler().execute(() -> {
                try {
                    eventReceived(event);
                } catch (final IOException e) {
                    logger.debug("IOException during event notification: {}", e.getMessage(), e);
                }
            });
        }

        @Override
        public void onError(final Throwable t) {
        }
    }

    /**
     * Functional interface to get parameters
     */
    @NonNullByDefault
    protected interface GetParms {
        /**
         * Gets the parameters for a specific version
         * 
         * @param version a non-null, non-empty version
         * @return a parameter
         */
        @Nullable
        Object getParms(String version);
    }
}
