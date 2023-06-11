/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.dto.thermostat.summary;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.dto.AbstractResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SummaryResponseDTO} contains the
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SummaryResponseDTO extends AbstractResponseDTO {

    private final transient Logger logger = LoggerFactory.getLogger(SummaryResponseDTO.class);

    /*
     * Number of thermostats listed in the Revision List.
     */
    public @Nullable Integer thermostatCount;

    /*
     * The list of CSV revision values.
     */
    public @Nullable List<RevisionDTO> revisionList;

    /*
     * The list of CSV status values.
     */
    @SerializedName("statusList")
    public @Nullable List<RunningDTO> runningList;

    public boolean hasChanged(@Nullable SummaryResponseDTO previousSummary) {
        if (previousSummary == null) {
            logger.debug("SummaryResponse: No previous summary");
            return true;
        }

        boolean changed = false;
        if (revisionHasChanged(previousSummary.revisionList)) {
            changed = true;
        }
        if (runningHasChanged(previousSummary.runningList)) {
            changed = true;
        }
        return changed;
    }

    private boolean revisionHasChanged(@Nullable List<RevisionDTO> previousList) {
        List<RevisionDTO> currentList = revisionList;
        if (previousList == null || currentList == null) {
            logger.debug("SummaryResponse: Previous and/or current revision list is null");
            return true;
        }
        // Check to see if there are different thermostat Ids in current vs previous
        Set<String> previousIds = previousList.stream().map(RevisionDTO::getId).collect(Collectors.toSet());
        Set<String> currentIds = currentList.stream().map(RevisionDTO::getId).collect(Collectors.toSet());
        if (!previousIds.equals(currentIds)) {
            logger.debug("SummaryResponse: Thermostat id maps are different");
            logger.trace("               : Curr: {}", Arrays.toString(currentIds.toArray()));
            logger.trace("               : Prev: {}", Arrays.toString(previousIds.toArray()));
            return true;
        }
        // Create a map of each thermostat id with its RevisionDTO object
        Map<String, RevisionDTO> previousMap = previousList.stream()
                .collect(Collectors.toMap(RevisionDTO::getId, RevisionDTO::getThis));
        // Go through list of current RevisionDTOs to see if something has changed
        for (RevisionDTO current : currentList) {
            RevisionDTO previous = previousMap.get(current.getId());
            if (current.hasChanged(previous)) {
                logger.debug("SummaryResponse: Revisions has changed");
                logger.trace("               : Curr: {}", current.toString());
                logger.trace("               : Prev: {}", previous.toString());
                return true;
            }
        }
        return false;
    }

    private boolean runningHasChanged(@Nullable List<RunningDTO> previousList) {
        List<RunningDTO> currentList = runningList;
        if (previousList == null || currentList == null) {
            logger.debug("SummaryResponse: Previous and/or current running list is null");
            return true;
        }
        // Check to see if there are different thermostat Ids in current vs previous
        Set<String> previousIds = previousList.stream().map(RunningDTO::getId).collect(Collectors.toSet());
        Set<String> currentIds = currentList.stream().map(RunningDTO::getId).collect(Collectors.toSet());
        if (!previousIds.equals(currentIds)) {
            logger.debug("SummaryResponse: Thermostat id maps are different");
            logger.trace("               : Curr: {}", Arrays.toString(currentIds.toArray()));
            logger.trace("               : Prev: {}", Arrays.toString(previousIds.toArray()));
            return true;
        }
        // Create a map of each thermostat id with its RunningDTO object
        Map<String, RunningDTO> previousMap = previousList.stream()
                .collect(Collectors.toMap(RunningDTO::getId, RunningDTO::getThis));
        // Go through list of current RunningDTOs to see if something has changed
        for (RunningDTO current : currentList) {
            RunningDTO previous = previousMap.get(current.getId());
            if (current.hasChanged(previous)) {
                logger.debug("SummaryResponse: Running Equipment has changed");
                logger.trace("               : Curr: {}", current.toString());
                logger.trace("               : Prev: {}", previous.toString());
                return true;
            }
        }
        return false;
    }
}
