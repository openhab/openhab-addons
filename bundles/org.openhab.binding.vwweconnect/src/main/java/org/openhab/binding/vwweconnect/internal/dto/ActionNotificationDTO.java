/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.vwweconnect.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Action Notification representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class ActionNotificationDTO {

    private String errorCode = "";
    private List<ActionNotificationListDTO> actionNotificationList = new ArrayList<>();

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    public List<ActionNotificationListDTO> getActionNotificationList() {
        return actionNotificationList;
    }

    @Override
    public String toString() {
        return "ActionNotificationDTO [errorCode=" + errorCode + ", actionNotificationList=" + actionNotificationList
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + actionNotificationList.hashCode();
        result = prime * result + errorCode.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActionNotificationDTO other = (ActionNotificationDTO) obj;
        if (!actionNotificationList.equals(other.actionNotificationList)) {
            return false;
        }
        if (!errorCode.equals(other.errorCode)) {
            return false;
        }
        return true;
    }

    public class ActionNotificationListDTO {

        private String actionState = "";
        private String actionType = "";
        private String serviceType = "";
        private String errorTitle = "";
        private String errorMessage = "";

        public String getActionState() {
            return actionState;
        }

        public String getActionType() {
            return actionType;
        }

        public String getServiceType() {
            return serviceType;
        }

        public String getErrorTitle() {
            return errorTitle;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return "ActionNotificationList [actionState=" + actionState + ", actionType=" + actionType
                    + ", serviceType=" + serviceType + ", errorTitle=" + errorTitle + ", errorMessage=" + errorMessage
                    + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + actionState.hashCode();
            result = prime * result + actionType.hashCode();
            result = prime * result + errorMessage.hashCode();
            result = prime * result + errorTitle.hashCode();
            result = prime * result + serviceType.hashCode();
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ActionNotificationListDTO other = (ActionNotificationListDTO) obj;
            if (!actionType.equals(other.actionType)) {
                return false;
            }
            if (!errorMessage.equals(other.errorMessage)) {
                return false;
            }
            if (!errorTitle.equals(other.errorTitle)) {
                return false;
            }
            if (!serviceType.equals(other.serviceType)) {
                return false;
            }
            return true;
        }

        private ActionNotificationDTO getEnclosingInstance() {
            return ActionNotificationDTO.this;
        }
    }
}
