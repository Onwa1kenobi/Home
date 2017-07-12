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
}
