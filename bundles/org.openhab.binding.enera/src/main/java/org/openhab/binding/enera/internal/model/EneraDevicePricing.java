package org.openhab.binding.enera.internal.model;

/**
 * EneraDevicePricing
 */
public class EneraDevicePricing {
    private float EnergyRate;
    private float BasicRate;
    private float AdvancePayment;

    /**
     * @return the energyRate
     */
    public float getEnergyRate() {
        return EnergyRate;
    }

    /**
     * @param energyRate the energyRate to set
     */
    public void setEnergyRate(float energyRate) {
        EnergyRate = energyRate;
    }

    /**
     * @return the basicRate
     */
    public float getBasicRate() {
        return BasicRate;
    }

    /**
     * @param basicRate the basicRate to set
     */
    public void setBasicRate(float basicRate) {
        BasicRate = basicRate;
    }

    /**
     * @return the advancePayment
     */
    public float getAdvancePayment() {
        return AdvancePayment;
    }

    /**
     * @param advancePayment the advancePayment to set
     */
    public void setAdvancePayment(float advancePayment) {
        AdvancePayment = advancePayment;
    }
}
