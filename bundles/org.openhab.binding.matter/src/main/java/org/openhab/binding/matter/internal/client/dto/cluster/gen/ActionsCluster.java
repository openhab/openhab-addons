/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * Actions
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ActionsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0025;
    public static final String CLUSTER_NAME = "Actions";
    public static final String CLUSTER_PREFIX = "actions";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_ACTION_LIST = "actionList";
    public static final String ATTRIBUTE_ENDPOINT_LISTS = "endpointLists";
    public static final String ATTRIBUTE_SETUP_URL = "setupUrl";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * The ActionList attribute holds the list of actions. Each entry shall have an unique ActionID, and its
     * EndpointListID shall exist in the EndpointLists attribute.
     */
    public List<ActionStruct> actionList; // 0 list R V
    /**
     * The EndpointLists attribute holds the list of endpoint lists. Each entry shall have an unique EndpointListID.
     */
    public List<EndpointListStruct> endpointLists; // 1 list R V
    /**
     * The SetupURL attribute (when provided) shall indicate a URL; its syntax shall follow the syntax as specified in
     * RFC 1738, max. 512 ASCII characters and shall use the https scheme. The location referenced by this URL shall
     * provide additional information for the actions provided:
     * • When used without suffix, it shall provide information about the various actions which the cluster provides.
     * ◦ Example: SetupURL could take the value of example://Actions or https://domain.example/ Matter/bridgev1/Actions
     * for this generic case (access generic info how to use actions provided by this cluster).
     * • When used with a suffix of &quot;/?a&#x3D;&quot; and the decimal value of ActionID for one of the actions, it
     * may provide information about that particular action. This could be a deeplink to manufacturer-app/website
     * (associated somehow to the server node) with the information/edit-screen for this action so that the user can
     * view and update details of the action, e.g. edit the scene, or change the wake-up experience time period.
     * ◦ Example of SetupURL with suffix added: example://Actions/?a&#x3D;12345 or
     * https://domain.example/Matter/bridgev1/Actions/?a&#x3D;12345 for linking to specific info/editing of the action
     * with ActionID 0x3039.
     */
    public String setupUrl; // 2 string R V

    // Structs
    /**
     * This event shall be generated when there is a change in the State of an ActionID during the execution of an
     * action and the most recent command using that ActionID used an InvokeID data field.
     * It provides feedback to the client about the progress of the action.
     * Example: When InstantActionWithTransition is invoked (with an InvokeID data field), two StateChanged events will
     * be generated:
     * • one when the transition starts (NewState&#x3D;Active)
     * • one when the transition completed (NewState&#x3D;Inactive)
     */
    public static class StateChanged {
        /**
         * This field shall be set to the ActionID of the action which has changed state.
         */
        public Integer actionId; // uint16
        /**
         * This field shall be set to the InvokeID which was provided to the most recent command referencing this
         * ActionID.
         */
        public Integer invokeId; // uint32
        /**
         * This field shall be set to state that the action has changed to.
         */
        public ActionStateEnum newState; // ActionStateEnum

        public StateChanged(Integer actionId, Integer invokeId, ActionStateEnum newState) {
            this.actionId = actionId;
            this.invokeId = invokeId;
            this.newState = newState;
        }
    }

    /**
     * This event shall be generated when there is some error which prevents the action from its normal planned
     * execution and the most recent command using that ActionID used an InvokeID data field.
     * It provides feedback to the client about the non-successful progress of the action.
     * Example: When InstantActionWithTransition is invoked (with an InvokeID data field), and another controller
     * changes the state of one or more of the involved endpoints during the transition, thus interrupting the
     * transition triggered by the action, two events would be generated:
     * • StateChanged when the transition starts (NewState&#x3D;Active)
     * • ActionFailed when the interrupting command occurs (NewState&#x3D;Inactive, Error&#x3D;interrupted)
     * Example: When InstantActionWithTransition is invoked (with an InvokeID data field &#x3D; 1), and the same client
     * invokes an InstantAction with (the same or another ActionId and) InvokeID &#x3D; 2, and this second command
     * interrupts the transition triggered by the first command, these events would be generated:
     * • StateChanged (InvokeID&#x3D;1, NewState&#x3D;Active) when the transition starts
     * • ActionFailed (InvokeID&#x3D;2, NewState&#x3D;Inactive, Error&#x3D;interrupted) when the second command
     * interrupts the transition
     * • StateChanged (InvokeID&#x3D;2, NewState&#x3D;Inactive) upon the execution of the action for the second command
     */
    public static class ActionFailed {
        /**
         * This field shall be set to the ActionID of the action which encountered an error.
         */
        public Integer actionId; // uint16
        /**
         * This field shall be set to the InvokeID which was provided to the most recent command referencing this
         * ActionID.
         */
        public Integer invokeId; // uint32
        /**
         * This field shall be set to state that the action is in at the time of generating the event.
         */
        public ActionStateEnum newState; // ActionStateEnum
        /**
         * This field shall be set to indicate the reason for non-successful progress of the action.
         */
        public ActionErrorEnum error; // ActionErrorEnum

        public ActionFailed(Integer actionId, Integer invokeId, ActionStateEnum newState, ActionErrorEnum error) {
            this.actionId = actionId;
            this.invokeId = invokeId;
            this.newState = newState;
            this.error = error;
        }
    }

    /**
     * This data type holds the details of a single action, and contains the data fields below.
     */
    public static class ActionStruct {
        /**
         * This field shall provide an unique identifier used to identify an action.
         */
        public Integer actionId; // uint16
        /**
         * This field shall indicate the name (as assigned by the user or automatically by the server) associated with
         * this action. This can be used for identifying the action to the user by the client. Example: &quot;my
         * colorful scene&quot;.
         */
        public String name; // string
        /**
         * This field shall indicate the type of action. The value of Type of an action, along with its
         * SupportedCommands can be used by the client in its UX or logic to determine how to present or use such
         * action. See ActionTypeEnum for details and examples.
         */
        public ActionTypeEnum type; // ActionTypeEnum
        /**
         * This field shall provide a reference to the associated endpoint list, which specifies the endpoints on this
         * Node which will be impacted by this ActionID.
         */
        public Integer endpointListId; // uint16
        /**
         * This field is a bitmap which shall be used to indicate which of the cluster’s commands are supported for this
         * particular action, with a bit set to 1 for each supported command according to the table below. Other bits
         * shall be set to 0.
         */
        public CommandBits supportedCommands; // CommandBits
        /**
         * This field shall indicate the current state of this action.
         */
        public ActionStateEnum state; // ActionStateEnum

        public ActionStruct(Integer actionId, String name, ActionTypeEnum type, Integer endpointListId,
                CommandBits supportedCommands, ActionStateEnum state) {
            this.actionId = actionId;
            this.name = name;
            this.type = type;
            this.endpointListId = endpointListId;
            this.supportedCommands = supportedCommands;
            this.state = state;
        }
    }

    /**
     * This data type holds the details of a single endpoint list, which relates to a set of endpoints that have some
     * logical relation, and contains the data fields below.
     */
    public static class EndpointListStruct {
        /**
         * This field shall provide an unique identifier used to identify the endpoint list.
         */
        public Integer endpointListId; // uint16
        /**
         * This field shall indicate the name (as assigned by the user or automatically by the server) associated with
         * the set of endpoints in this list. This can be used for identifying the action to the user by the client.
         * Example: &quot;living room&quot;.
         */
        public String name; // string
        /**
         * This field shall indicate the type of endpoint list, see EndpointListTypeEnum.
         */
        public EndpointListTypeEnum type; // EndpointListTypeEnum
        /**
         * This field shall provide a list of endpoint numbers.
         */
        public List<Integer> endpoints; // list

        public EndpointListStruct(Integer endpointListId, String name, EndpointListTypeEnum type,
                List<Integer> endpoints) {
            this.endpointListId = endpointListId;
            this.name = name;
            this.type = type;
            this.endpoints = endpoints;
        }
    }

    // Enums
    public enum ActionTypeEnum implements MatterEnum {
        OTHER(0, "Other"),
        SCENE(1, "Scene"),
        SEQUENCE(2, "Sequence"),
        AUTOMATION(3, "Automation"),
        EXCEPTION(4, "Exception"),
        NOTIFICATION(5, "Notification"),
        ALARM(6, "Alarm");

        public final Integer value;
        public final String label;

        private ActionTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Note that some of these states are applicable only for certain actions, as determined by their SupportedCommands.
     */
    public enum ActionStateEnum implements MatterEnum {
        INACTIVE(0, "Inactive"),
        ACTIVE(1, "Active"),
        PAUSED(2, "Paused"),
        DISABLED(3, "Disabled");

        public final Integer value;
        public final String label;

        private ActionStateEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ActionErrorEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        INTERRUPTED(1, "Interrupted");

        public final Integer value;
        public final String label;

        private ActionErrorEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * The Room and Zone values are provided for the cases where a user (or the system on behalf of the user) has
     * created logical grouping of the endpoints (e.g. bridged devices) based on location.
     */
    public enum EndpointListTypeEnum implements MatterEnum {
        OTHER(0, "Other"),
        ROOM(1, "Room"),
        ZONE(2, "Zone");

        public final Integer value;
        public final String label;

        private EndpointListTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    /**
     * Note - The bit allocation of this bitmap shall follow the ID’s of the Commands of this cluster.
     */
    public static class CommandBits {
        public boolean instantAction;
        public boolean instantActionWithTransition;
        public boolean startAction;
        public boolean startActionWithDuration;
        public boolean stopAction;
        public boolean pauseAction;
        public boolean pauseActionWithDuration;
        public boolean resumeAction;
        public boolean enableAction;
        public boolean enableActionWithDuration;
        public boolean disableAction;
        public boolean disableActionWithDuration;

        public CommandBits(boolean instantAction, boolean instantActionWithTransition, boolean startAction,
                boolean startActionWithDuration, boolean stopAction, boolean pauseAction,
                boolean pauseActionWithDuration, boolean resumeAction, boolean enableAction,
                boolean enableActionWithDuration, boolean disableAction, boolean disableActionWithDuration) {
            this.instantAction = instantAction;
            this.instantActionWithTransition = instantActionWithTransition;
            this.startAction = startAction;
            this.startActionWithDuration = startActionWithDuration;
            this.stopAction = stopAction;
            this.pauseAction = pauseAction;
            this.pauseActionWithDuration = pauseActionWithDuration;
            this.resumeAction = resumeAction;
            this.enableAction = enableAction;
            this.enableActionWithDuration = enableActionWithDuration;
            this.disableAction = disableAction;
            this.disableActionWithDuration = disableActionWithDuration;
        }
    }

    public ActionsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 37, "Actions");
    }

    protected ActionsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command triggers an action (state change) on the involved endpoints, in a &quot;fire and forget&quot;
     * manner. Afterwards, the action’s state shall be Inactive.
     * Example: recall a scene on a number of lights.
     */
    public static ClusterCommand instantAction(Integer actionId, Integer invokeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        return new ClusterCommand("instantAction", map);
    }

    /**
     * It is recommended that, where possible (e.g., it is not possible for attributes with Boolean data type), a
     * gradual transition SHOULD take place from the old to the new state over this time period. However, the exact
     * transition is manufacturer dependent.
     * This command triggers an action (state change) on the involved endpoints, with a specified time to transition
     * from the current state to the new state. During the transition, the action’s state shall be Active. Afterwards,
     * the action’s state shall be Inactive.
     * Example: recall a scene on a number of lights, with a specified transition time.
     */
    public static ClusterCommand instantActionWithTransition(Integer actionId, Integer invokeId,
            Integer transitionTime) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        return new ClusterCommand("instantActionWithTransition", map);
    }

    /**
     * This command triggers the commencement of an action on the involved endpoints. Afterwards, the action’s state
     * shall be Active.
     * Example: start a dynamic lighting pattern (such as gradually rotating the colors around the setpoints of the
     * scene) on a set of lights.
     * Example: start a sequence of events such as a wake-up experience involving lights moving through several
     * brightness/color combinations and the window covering gradually opening.
     */
    public static ClusterCommand startAction(Integer actionId, Integer invokeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        return new ClusterCommand("startAction", map);
    }

    /**
     * This command triggers the commencement of an action on the involved endpoints, and shall change the action’s
     * state to Active. After the specified Duration, the action will stop, and the action’s state shall change to
     * Inactive.
     * Example: start a dynamic lighting pattern (such as gradually rotating the colors around the setpoints of the
     * scene) on a set of lights for 1 hour (Duration&#x3D;3600).
     */
    public static ClusterCommand startActionWithDuration(Integer actionId, Integer invokeId, Integer duration) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        if (duration != null) {
            map.put("duration", duration);
        }
        return new ClusterCommand("startActionWithDuration", map);
    }

    /**
     * This command stops the ongoing action on the involved endpoints. Afterwards, the action’s state shall be
     * Inactive.
     * Example: stop a dynamic lighting pattern which was previously started with StartAction.
     */
    public static ClusterCommand stopAction(Integer actionId, Integer invokeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        return new ClusterCommand("stopAction", map);
    }

    /**
     * This command pauses an ongoing action, and shall change the action’s state to Paused.
     * Example: pause a dynamic lighting effect (the lights stay at their current color) which was previously started
     * with StartAction.
     */
    public static ClusterCommand pauseAction(Integer actionId, Integer invokeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        return new ClusterCommand("pauseAction", map);
    }

    /**
     * This command pauses an ongoing action, and shall change the action’s state to Paused. After the specified
     * Duration, the ongoing action will be automatically resumed. which shall change the action’s state to Active.
     * Example: pause a dynamic lighting effect (the lights stay at their current color) for 10 minutes
     * (Duration&#x3D;600).
     * The difference between Pause/Resume and Disable/Enable is on the one hand semantic (the former is more of a
     * transitionary nature while the latter is more permanent) and on the other hand these can be implemented slightly
     * differently in the implementation of the action (e.g. a Pause would be automatically resumed after some hours or
     * during a nightly reset, while an Disable would remain in effect until explicitly enabled again).
     */
    public static ClusterCommand pauseActionWithDuration(Integer actionId, Integer invokeId, Integer duration) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        if (duration != null) {
            map.put("duration", duration);
        }
        return new ClusterCommand("pauseActionWithDuration", map);
    }

    /**
     * This command resumes a previously paused action, and shall change the action’s state to Active.
     * The difference between ResumeAction and StartAction is that ResumeAction will continue the action from the state
     * where it was paused, while StartAction will start the action from the beginning.
     * Example: resume a dynamic lighting effect (the lights&#x27; colors will change gradually, continuing from the
     * point they were paused).
     */
    public static ClusterCommand resumeAction(Integer actionId, Integer invokeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        return new ClusterCommand("resumeAction", map);
    }

    /**
     * This command enables a certain action or automation. Afterwards, the action’s state shall be Active.
     * Example: enable a motion sensor to control the lights in an area.
     */
    public static ClusterCommand enableAction(Integer actionId, Integer invokeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        return new ClusterCommand("enableAction", map);
    }

    /**
     * This command enables a certain action or automation, and shall change the action’s state to be Active. After the
     * specified Duration, the action or automation will stop, and the action’s state shall change to Disabled.
     * Example: enable a &quot;presence mimicking&quot; behavior for the lights in your home during a vacation; the
     * Duration field is used to indicated the length of your absence from home. After that period, the presence
     * mimicking behavior will no longer control these lights.
     */
    public static ClusterCommand enableActionWithDuration(Integer actionId, Integer invokeId, Integer duration) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        if (duration != null) {
            map.put("duration", duration);
        }
        return new ClusterCommand("enableActionWithDuration", map);
    }

    /**
     * This command disables a certain action or automation, and shall change the action’s state to Inactive.
     * Example: disable a motion sensor to no longer control the lights in an area.
     */
    public static ClusterCommand disableAction(Integer actionId, Integer invokeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        return new ClusterCommand("disableAction", map);
    }

    /**
     * This command disables a certain action or automation, and shall change the action’s state to Disabled. After the
     * specified Duration, the action or automation will re-start, and the action’s state shall change to either
     * Inactive or Active, depending on the actions (see examples 4 and 6).
     * Example: disable a &quot;wakeup&quot; experience for a period of 1 week when going on holiday (to prevent them
     * from turning on in the morning while you’re not at home). After this period, the wakeup experience will control
     * the lights as before.
     */
    public static ClusterCommand disableActionWithDuration(Integer actionId, Integer invokeId, Integer duration) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (actionId != null) {
            map.put("actionId", actionId);
        }
        if (invokeId != null) {
            map.put("invokeId", invokeId);
        }
        if (duration != null) {
            map.put("duration", duration);
        }
        return new ClusterCommand("disableActionWithDuration", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "actionList : " + actionList + "\n";
        str += "endpointLists : " + endpointLists + "\n";
        str += "setupUrl : " + setupUrl + "\n";
        return str;
    }
}
