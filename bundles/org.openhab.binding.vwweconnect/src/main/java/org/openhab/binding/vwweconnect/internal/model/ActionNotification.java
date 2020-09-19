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
package org.openhab.binding.vwweconnect.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Action Notification representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class ActionNotification {

    private @Nullable String errorCode;
    private List<ActionNotificationList> actionNotificationList = new ArrayList<>();

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    public List<ActionNotificationList> getActionNotificationList() {
        return actionNotificationList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("errorCode", errorCode)
                .append("actionNotificationList", actionNotificationList).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorCode).append(actionNotificationList).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ActionNotification) == false) {
            return false;
        }
        ActionNotification rhs = ((ActionNotification) other);
        return new EqualsBuilder().append(errorCode, rhs.errorCode)
                .append(actionNotificationList, rhs.actionNotificationList).isEquals();
    }

    public class ActionNotificationList {

        private @Nullable String actionState;
        private @Nullable String actionType;
        private @Nullable String serviceType;
        private @Nullable String errorTitle;
        private @Nullable String errorMessage;

        public @Nullable String getActionState() {
            return actionState;
        }

        public @Nullable String getActionType() {
            return actionType;
        }

        public @Nullable String getServiceType() {
            return serviceType;
        }

        public @Nullable String getErrorTitle() {
            return errorTitle;
        }

        public @Nullable String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("actionState", actionState).append("actionType", actionType)
                    .append("serviceType", serviceType).append("errorTitle", errorTitle)
                    .append("errorMessage", errorMessage).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(serviceType).append(errorMessage).append(actionType).append(errorTitle)
                    .append(actionState).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if ((other instanceof ActionNotificationList) == false) {
                return false;
            }
            ActionNotificationList rhs = ((ActionNotificationList) other);
            return new EqualsBuilder().append(serviceType, rhs.serviceType).append(errorMessage, rhs.errorMessage)
                    .append(actionType, rhs.actionType).append(errorTitle, rhs.errorTitle)
                    .append(actionState, rhs.actionState).isEquals();
        }
    }
}
