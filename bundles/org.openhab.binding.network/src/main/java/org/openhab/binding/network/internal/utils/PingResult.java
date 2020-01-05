package org.openhab.binding.network.internal.utils;

import java.util.Optional;

/**
 * Information about the ping result.
 *
 * @author Andreas Hirsch - Initial contribution
 */
public class PingResult {

    private boolean success;
    private Double responseTimeInMS;
    private double executionTimeInMS;

    /**
     * @param success     <code>true</code> if the device was reachable, <code>false</code> if not.
     * @param executionTimeInMS Execution time of the ping command in ms.
     */
    public PingResult(boolean success, double executionTimeInMS) {
        this.success = success;
        this.executionTimeInMS = executionTimeInMS;
    }

    /**
     * @return <code>true</code> if the device was reachable, <code>false</code> if not.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return Response time in ms which was returned by the ping command. Optional is empty if response time provided
     * by ping command is not available.
     */
    public Optional<Double> getResponseTimeInMS() {
        return responseTimeInMS == null ? Optional.empty() : Optional.of(responseTimeInMS);
    }

    /**
     * @param responseTimeInMS Response time in ms which was returned by the ping command.
     */
    public void setResponseTimeInMS(double responseTimeInMS) {
        this.responseTimeInMS = responseTimeInMS;
    }

    @Override
    public String toString() {
        return "PingResult{" +
                "success=" + success +
                ", responseTimeInMS=" + responseTimeInMS +
                ", executionTimeInMS=" + executionTimeInMS +
                '}';
    }

    /**
     * @return Execution time of the ping command in ms.
     */
    public double getExecutionTimeInMS() {
        return executionTimeInMS;
    }
}
