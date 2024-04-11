package org.cloudbus.cloudsim.mychanges;

public class PeEntry {
    private double maxMIPS;
    private double availableMIPS;

    public PeEntry(double maxMIPS, double availableMIPS){
        this.maxMIPS = maxMIPS;
        this.availableMIPS = availableMIPS;
    }

    public double getMaxMIPS() {
        return maxMIPS;
    }
    public void setMaxMIPS(double maxMIPS) {
        this.maxMIPS = maxMIPS;
    }

    public double getAvailableMIPS() {
        return availableMIPS;
    }
    public void setAvailableMIPS(double availableMIPS) {
        this.availableMIPS = availableMIPS;
    }
}
