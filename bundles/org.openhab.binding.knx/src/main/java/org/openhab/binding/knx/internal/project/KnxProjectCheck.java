/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.AddressTypes.AddressType;

/**
 * Compares the group addresses of configured channels with the project. Library code: the caller says what is
 * configured, this class says what differs.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class KnxProjectCheck {

    /** The cause of a difference. */
    public enum Kind {
        /** The configured address is not a group address of the project. */
        ADDRESS_NOT_IN_PROJECT,
        /** The address is in the project, but no object is linked to it. */
        ADDRESS_WITHOUT_OBJECT,
        /** The project's data type and the configured one differ. */
        DATAPOINT_TYPE_DIFFERS,
        /**
         * Read is configured, and every linked object is defined by the manufacturer files but none answers a read
         * on the address.
         */
        READ_WITHOUT_READ_FLAG
    }

    /**
     * @param kind the cause
     * @param configured the configured address
     * @param projectAddress the address in the project, {@code null} when it is not there
     * @param projectType the project's data type where it matters, {@code null} otherwise
     * @param noteOnly whether it is only worth a note: only the sub number of the types differs
     */
    public record Difference(Kind kind, KnxConfiguredAddress configured, @Nullable KnxGroupAddress projectAddress,
            @Nullable KnxDatapointType projectType, boolean noteOnly) {
    }

    private final KnxProject project;
    private final KnxChannelMapper mapper;

    public KnxProjectCheck(KnxProject project) {
        this.project = project;
        this.mapper = new KnxChannelMapper(project);
    }

    /** The differences, one per cause, addresses in the order given. */
    public List<Difference> check(List<KnxConfiguredAddress> configured) {
        List<Difference> differences = new ArrayList<>();
        for (KnxConfiguredAddress address : configured) {
            KnxGroupAddress inProject = project.groupAddress(address.rawAddress());
            if (inProject == null) {
                differences.add(new Difference(Kind.ADDRESS_NOT_IN_PROJECT, address, null, null, false));
                continue;
            }
            List<KnxGroupObject> linked = project.groupObjectsLinkedTo(inProject);
            if (linked.isEmpty() && project.objectsLinkedTo(inProject).isEmpty()) {
                differences.add(new Difference(Kind.ADDRESS_WITHOUT_OBJECT, address, inProject, null, false));
            }
            checkType(address, inProject, differences);
            if (address.read() && cannotRead(inProject, linked)) {
                differences.add(new Difference(Kind.READ_WITHOUT_READ_FLAG, address, inProject, null, false));
            }
        }
        return List.copyOf(differences);
    }

    private void checkType(KnxConfiguredAddress address, KnxGroupAddress inProject, List<Difference> differences) {
        AddressType resolved = AddressTypes.resolve(project, inProject);
        KnxDatapointType wanted = resolved.type();
        KnxDatapointType configured = KnxDatapointType.parse(address.datapointType());
        if (wanted == null || configured == null) {
            return;
        }
        Integer wantedSub = wanted.sub();
        Integer configuredSub = configured.sub();
        if (wanted.main() != configured.main()) {
            differences.add(new Difference(Kind.DATAPOINT_TYPE_DIFFERS, address, inProject, wanted, false));
        } else if (wantedSub != null && configuredSub != null && !wantedSub.equals(configuredSub)) {
            differences.add(new Difference(Kind.DATAPOINT_TYPE_DIFFERS, address, inProject, wanted, true));
        }
    }

    /**
     * Objects are linked, the manufacturer files define every one of them, and none answers a read on the address.
     * An object whose definition was not found may well answer, so then nothing is claimed.
     */
    private boolean cannotRead(KnxGroupAddress address, List<KnxGroupObject> linked) {
        if (linked.isEmpty()) {
            return false;
        }
        for (KnxGroupObject object : linked) {
            if (object.definition() != KnxObjectDefinition.FOUND) {
                return false;
            }
        }
        return !mapper.canRead(address);
    }
}
