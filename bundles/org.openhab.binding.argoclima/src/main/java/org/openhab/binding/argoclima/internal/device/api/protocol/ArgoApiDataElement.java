/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal.device.api.protocol;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.IArgoCommandableElement;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.IArgoCommandableElement.IArgoElement;
import org.openhab.binding.argoclima.internal.device.api.types.ArgoDeviceSettingType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Wrapper for Argo API protocol knobs, providing an overlay functionality for converting them between framework values
 * and raw protocol values, as well as command confirmation support
 * <p>
 * Supports R/O (update-only), W/O (set-only) as well as R/W (update and set) knobs
 * <p>
 * Since the Status(query) and Command(send) commands have different syntax and item ordering, this class is tracking
 * respective position of an element in a protocol using {@link #queryResponseIndex} and
 * {@link #statusUpdateRequestIndex}, respectively
 *
 * @param <T> The underlying param type (with its internal logic of converting to/from Argo protocol
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoApiDataElement<T extends IArgoElement> implements IArgoCommandableElement {
    /**
     * Type of the data element
     *
     * @author Mateusz Bronk - Initial contribution
     */
    public enum DataElementType {
        READ_WRITE,
        READ_ONLY,
        WRITE_ONLY
    }

    /** The kind(type) of setting - aka. the *actual* thing it controls */
    public final ArgoDeviceSettingType settingType;

    /** The index of this API element in a device-side update */
    public final int queryResponseIndex;

    /** The index of this API element in a remote-side command */
    public final int statusUpdateRequestIndex;

    private DataElementType type;
    private T rawValue;

    /**
     * Private c-tor
     *
     * @param settingType Kind of this knob (what it controls)
     * @param rawValue The raw API protocol value
     * @param queryIndex The index of this element in a device-side status update (or {@code -1} if N/A)
     * @param updateIndex The index of this element in a cloud-side command (or {@code -1} if N/A)
     * @param type The direction of this element (R/O, R/W, W/O)
     */
    private ArgoApiDataElement(ArgoDeviceSettingType settingType, T rawValue, int queryIndex, int updateIndex,
            DataElementType type) {
        this.settingType = settingType;
        this.queryResponseIndex = queryIndex;
        this.statusUpdateRequestIndex = updateIndex;
        this.type = type;
        this.rawValue = rawValue;
    }

    /**
     * Named c-tor for a R/W element
     *
     * @param settingType Kind of this knob (what it controls)
     * @param rawValue The raw API protocol value
     * @param queryIndex The index of this element in a device-side status update
     * @param updateIndex The index of this element in a cloud-side command
     * @return The wrapped protocol API element
     */
    public static ArgoApiDataElement<IArgoElement> readWriteElement(ArgoDeviceSettingType settingType,
            IArgoElement rawValue, int queryIndex, int updateIndex) {
        return new ArgoApiDataElement<>(settingType, rawValue, queryIndex, updateIndex, DataElementType.READ_WRITE);
    }

    /**
     * Named c-tor for a R/O element
     *
     * @param settingType Kind of this knob (what it controls)
     * @param rawValue The raw API protocol value
     * @param queryIndex The index of this element in a device-side status update
     * @return The wrapped protocol API element
     */
    public static ArgoApiDataElement<IArgoElement> readOnlyElement(ArgoDeviceSettingType settingType,
            IArgoElement rawValue, int queryIndex) {
        return new ArgoApiDataElement<>(settingType, rawValue, queryIndex, -1, DataElementType.READ_ONLY);
    }

    /**
     * Named c-tor for a W/O element
     *
     * @param settingType Kind of this knob (what it controls)
     * @param rawValue The raw API protocol value
     * @param updateIndex The index of this element in a cloud-side command
     * @return The wrapped protocol API element
     */
    public static ArgoApiDataElement<IArgoElement> writeOnlyElement(ArgoDeviceSettingType settingType,
            IArgoElement rawValue, int updateIndex) {
        return new ArgoApiDataElement<>(settingType, rawValue, -1, updateIndex, DataElementType.WRITE_ONLY);
    }

    @Override
    public void abortPendingCommand() {
        this.rawValue.abortPendingCommand();
    }

    @Override
    public boolean isUpdatePending() {
        return this.rawValue.isUpdatePending();
    }

    @Override
    public final boolean hasInFlightCommand() {
        return this.rawValue.hasInFlightCommand();
    }

    @Override
    public void notifyCommandSent() {
        this.rawValue.notifyCommandSent();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Extended {@code toString()} method, allowing to also include the kind of knob
     *
     * @param includeType If true, includes the setting type (what it controls) in the string representation
     * @return String representation
     */
    public String toString(boolean includeType) {
        var prefix = "";
        if (includeType) {
            prefix = this.settingType.toString() + "=";
        }
        return prefix + rawValue.toString();
    }

    /**
     * Output parsed value of this element (reported in a new device-side update) in OH framework-compatible
     * representation
     * <p>
     * This call does not update internal representation of this element!
     *
     * @param responseElements All "state" response elements sent by the device (device always sends state of ALL knobs)
     * @return OH-compatible representation of current device state
     */
    public State fromDeviceResponse(List<String> responseElements) {
        if (this.type == DataElementType.READ_WRITE || this.type == DataElementType.READ_ONLY) {
            return this.rawValue.updateFromApiResponse(responseElements.get(queryResponseIndex));
        }
        return UnDefType.NULL; // Write-only elements do not have any state reported
    }

    public State fromDeviceCommand(List<String> responseElements) {
        if (this.type == DataElementType.READ_WRITE || this.type == DataElementType.WRITE_ONLY) {
            return this.rawValue.updateFromApiResponse(responseElements.get(statusUpdateRequestIndex));
        }
        return UnDefType.NULL; // Write-only elements do not have any state reported
    }

    /**
     * Output this element's currently-stored value in OH framework-compatible representation
     *
     * @return OH-compatible representation of current device state
     */
    public State getState() {
        return rawValue.toState();
    }

    /**
     * Handle framework-side command targeting this element
     *
     * @param command The command to handle
     * @return Status on whether the command has been handled (accepted). Note "handled" here doesn't mean
     *         sent and confirmed by the device, merely recognized by the framework and accepted for subsequent
     *         device-side communication (which happens asynchronously to this call)
     */
    public boolean handleCommand(Command command) {
        if (this.type != DataElementType.WRITE_ONLY && this.type != DataElementType.READ_WRITE) {
            return false; // attempting to write a R/O value
        }
        boolean waitForConfirmation = this.type != DataElementType.WRITE_ONLY;

        return rawValue.handleCommand(command, waitForConfirmation);
    }

    public record deviceCommandRequest(Integer updateIndex, String apiValue) {
    }

    /**
     * Convert this elements' current value to a device-compatible command request
     * <p>
     * Value is returned only if this item has a pending update (or is always sent fresh as part of protocol)
     *
     * @return A pair of (updateIndex, ApiValue) representing this element as a command (if it had update)
     */
    public Optional<deviceCommandRequest> toDeviceResponse() {
        if (this.rawValue.isUpdatePending() || this.rawValue.isAlwaysSent()) {
            return Optional
                    .of(new deviceCommandRequest(this.statusUpdateRequestIndex, this.rawValue.getDeviceApiValue()));
        }
        return Optional.empty();
    }

    /**
     * Check if this element should be sent to device (either has withstanding command or is always sent)
     *
     * @return True if the element needs sending to the device. False - otherwise
     */
    public boolean shouldBeSentToDevice() {
        return this.rawValue.isUpdatePending() || this.rawValue.isAlwaysSent();
    }

    /**
     * Check if this element can be read (either allows reading, or doesn't, but there's a cached value available
     * already)
     *
     * @return True if this element can be read. False - otherwise
     */
    public boolean isReadable() {
        return this.type == DataElementType.READ_ONLY || this.type == DataElementType.READ_WRITE
                || (this.type == DataElementType.WRITE_ONLY && this.rawValue.toState() != UnDefType.UNDEF);
    }
}
