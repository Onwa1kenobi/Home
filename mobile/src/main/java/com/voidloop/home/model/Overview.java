package com.voidloop.home.model;

/**
 * Created by ameh on 13/06/2017.
 */

public class Overview {

    private final double ENERGY_COST_CONVERSION_RATE = 4;

    private double totalReferencePower = 0,
            todayPowerUsage = 0,
            todayPowerUsageCost = 0,
            aggregatePowerUsage = 0,
            aggregatePowerUsageCost = 0;

    private String securityPassword, userInstanceTokenID;

    private boolean isBillingActive, isHomeSecure;

    public Overview() {

    }

    public double getTotalReferencePower() {
        return totalReferencePower;
    }

    public void setTotalReferencePower(double totalReferencePower) {
        this.totalReferencePower = totalReferencePower;
    }

    public double getTodayPowerUsage() {
        return todayPowerUsage;
    }

    public void setTodayPowerUsage(double todayPowerUsage) {
        this.todayPowerUsage = todayPowerUsage;
    }

    public double getTodayPowerUsageCost() {
        return todayPowerUsageCost;
    }

    public void setTodayPowerUsageCost() {
        this.todayPowerUsageCost = getTodayPowerUsage() * ENERGY_COST_CONVERSION_RATE;
    }

    public double getAggregatePowerUsage() {
        return aggregatePowerUsage;
    }

    public void setAggregatePowerUsage(double aggregatePowerUsage) {
        this.aggregatePowerUsage = aggregatePowerUsage;
    }

    public double getAggregatePowerUsageCost() {
        return aggregatePowerUsageCost;
    }

    public void setAggregatePowerUsageCost() {
        this.aggregatePowerUsageCost = getAggregatePowerUsage() * ENERGY_COST_CONVERSION_RATE;
    }

    public String getSecurityPassword() {
        return securityPassword;
    }

    public void setSecurityPassword(String securityPassword) {
        this.securityPassword = securityPassword;
    }

    public boolean getIsBillingActive() {
        return isBillingActive;
    }

    public void setIsBillingActive(boolean billingActive) {
        isBillingActive = billingActive;
    }

    public boolean isHomeSecure() {
        return isHomeSecure;
    }

    public void setHomeSecure(boolean homeSecure) {
        isHomeSecure = homeSecure;
    }

    public String getUserInstanceTokenID() {
        return userInstanceTokenID;
    }

    public void setUserInstanceTokenID(String userInstanceTokenID) {
        this.userInstanceTokenID = userInstanceTokenID;
    }
}
