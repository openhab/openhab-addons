package org.openhab.binding.revogismartstripcontrol.internal.api;

import java.util.Objects;

/**
 * The class {@link SwitchResponse} describes the response when you switch a plug
 */
public class SwitchResponse {
        private int response;
        private int code;

        public SwitchResponse() {
        }

        public SwitchResponse(int response, int code) {
                this.response = response;
                this.code = code;
        }

        public int getResponse() {
                return response;
        }

        public int getCode() {
                return code;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                SwitchResponse that = (SwitchResponse) o;
                return response == that.response &&
                        code == that.code;
        }

        @Override
        public int hashCode() {
                return Objects.hash(response, code);
        }
}