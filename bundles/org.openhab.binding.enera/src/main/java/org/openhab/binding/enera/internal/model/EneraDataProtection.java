package org.openhab.binding.enera.internal.model;

import java.util.Date;

public class EneraDataProtection {
    private boolean RegulationsAccepted;
    private int Version;
    private Date AcceptedAt;

    /**
     * @return the regulationsAccepted
     */
    public boolean isRegulationsAccepted() {
        return RegulationsAccepted;
    }

    /**
     * @param regulationsAccepted the regulationsAccepted to set
     */
    public void setRegulationsAccepted(boolean regulationsAccepted) {
        RegulationsAccepted = regulationsAccepted;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return Version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        Version = version;
    }

    /**
     * @return the acceptedAt
     */
    public Date getAcceptedAt() {
        return AcceptedAt;
    }

    /**
     * @param acceptedAt the acceptedAt to set
     */
    public void setAcceptedAt(Date acceptedAt) {
        AcceptedAt = acceptedAt;
    }

    
}