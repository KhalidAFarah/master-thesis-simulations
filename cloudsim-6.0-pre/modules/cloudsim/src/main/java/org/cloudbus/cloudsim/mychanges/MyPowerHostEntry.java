package org.cloudbus.cloudsim.mychanges;

import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerVm;

import java.util.List;

public class MyPowerHostEntry extends HostStateHistoryEntry {

    private double allocatedRam;
    private double requestedRam;
    private double allocatedBw;
    private double requestedBw;
    private List<PeEntry> peEntries;
    private List<PowerVm> vms;

    /**
     * Instantiates a new host state history entry.
     *
     * @param time          the time
     * @param allocatedMips the allocated mips
     * @param requestedMips the requested mips
     * @param isActive      the is active
     */
    public MyPowerHostEntry(double time, double allocatedMips, double requestedMips, boolean isActive,
                            double allocatedRam, double requestedRam, double allocatedBw, double requestedBw, List<PeEntry> peEntries, List<PowerVm> vms) {
        super(time, allocatedMips, requestedMips, isActive);

        this.allocatedRam = allocatedRam;
        this.requestedRam = requestedRam;
        this.allocatedBw = allocatedBw;
        this.requestedBw = requestedBw;
        this.peEntries = peEntries;
        this.vms = vms;
    }

    public double getAllocatedRam() {
        return allocatedRam;
    }
    public void setAllocatedRam(double allocatedRam) {
        this.allocatedRam = allocatedRam;
    }

    public double getRequestedRam() {
        return requestedRam;
    }
    public void setRequestedRam(double requestedRam) {
        this.requestedRam = requestedRam;
    }

    public double getAllocatedBw() {
        return allocatedBw;
    }
    public void setAllocatedBw(double allocatedBw) {
        this.allocatedBw = allocatedBw;
    }

    public double getRequestedBw() {
        return requestedBw;
    }
    public void setRequestedBw(double requestedBw) {
        this.requestedBw = requestedBw;
    }

    public List<PeEntry> getPeEntries() {
        return peEntries;
    }
    public void setPeEntries(List<PeEntry> peEntries) {
        this.peEntries = peEntries;
    }

    public List<PowerVm> getVms() {
        return vms;
    }
    public void setVms(List<PowerVm> vms) {
        this.vms = vms;
    }


}
