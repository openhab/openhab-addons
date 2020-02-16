package org.openhab.binding.revogismartstripcontrol.internal.udp;

import java.util.Objects;

public class UdpResponse {
    private String answer;
    private String ipAddress;

    public UdpResponse(String answer, String ipAddress) {
        this.answer = answer;
        this.ipAddress = ipAddress;
    }

    public String getAnswer() {
        return answer;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UdpResponse that = (UdpResponse) o;
        return answer.equals(that.answer) &&
                ipAddress.equals(that.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answer, ipAddress);
    }
}
