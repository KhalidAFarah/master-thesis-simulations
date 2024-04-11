package org.cloudbus.cloudsim.mychanges;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.List;

public class MyPowerHost extends PowerHost {
    /**
     * Instantiates a new PowerHost.
     *
     * @param id             the id of the host
     * @param ramProvisioner the ram provisioner
     * @param bwProvisioner  the bw provisioner
     * @param storage        the storage capacity
     * @param peList         the host's PEs list
     * @param vmScheduler    the VM scheduler
     * @param powerModel
     */
    public MyPowerHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
    }


    @Override
    public double updateVmsProcessing(double currentTime) {
        double smallerTime = super.updateVmsProcessing(currentTime);
        setPreviousUtilizationMips(getUtilizationMips());
        setUtilizationMips(0);
        double hostTotalRequestedMips = 0;
        double hostTotalRequestedRam = 0;
        double hostTotalRequestedBw = 0;

        for (Vm vm : getVmList()) {
            getVmScheduler().deallocatePesForVm(vm);
        }

        for (Vm vm : getVmList()) {
            getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
        }

        for (Vm vm : getVmList()) {
            //int pes = vm.getNumberOfPes();
            double totalRequestedMips = vm.getCurrentRequestedTotalMips();
            double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

            double totalRequestedRam = vm.getCurrentRequestedRam();
            double totalAllocatedRam = vm.getCurrentAllocatedRam();//getVmScheduler().getTotalAllocatedMipsForVm(vm);

            double totalRequestedBw = vm.getCurrentRequestedBw();
            double totalAllocatedBw = vm.getCurrentAllocatedBw();//getVmScheduler().getTotalAllocatedMipsForVm(vm);

            if (!Log.isDisabled()) {
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
                                + " (Host #" + vm.getHost().getId()
                                + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                        CloudSim.clock(),
                        totalAllocatedMips,
                        totalRequestedMips,
                        vm.getMips(),
                        totalRequestedMips / vm.getMips() * 100);

                List<Pe> pes = getVmScheduler().getPesAllocatedForVM(vm);
                StringBuilder pesString = new StringBuilder();
                for (Pe pe : pes) {
                    pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
                            .getTotalAllocatedMipsForVm(vm)));
                }
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
                                + getNumberOfPes() + " * " + getVmScheduler().getPeCapacity() + ")."
                                + pesString,
                        CloudSim.clock());
            }

            if (getVmsMigratingIn().contains(vm)) {
                Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + vm.getId()
                        + " is being migrated to Host #" + getId(), CloudSim.clock());
            } else {
                if (totalAllocatedMips + 0.1 < totalRequestedMips) {
                    Log.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + vm.getId()
                            + ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
                }
                if(vm instanceof MyPowerVm){
                    ((MyPowerVm) vm).addStateHistoryEntry(
                            currentTime,
                            totalAllocatedMips,
                            totalRequestedMips,
                            totalRequestedRam,
                            totalAllocatedRam,
                            totalRequestedBw,
                            totalAllocatedBw,
                            (vm.isInMigration() && !getVmsMigratingIn().contains(vm)));
                }else{
                    vm.addStateHistoryEntry(
                            currentTime,
                            totalAllocatedMips,
                            totalRequestedMips,
                            (vm.isInMigration() && !getVmsMigratingIn().contains(vm)));
                }

                if (vm.isInMigration()) {
                    Log.formatLine(
                            "%.2f: [Host #" + getId() + "] VM #" + vm.getId() + " is in migration",
                            CloudSim.clock());
                    totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
                }
            }

            setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
            hostTotalRequestedMips += totalRequestedMips;
            hostTotalRequestedRam += totalRequestedRam;
            hostTotalRequestedBw += totalRequestedBw;
        }
        List<PowerVm> copy = new ArrayList<PowerVm>();
        copy.addAll(getVmList());
        List<PeEntry> peEntries = new ArrayList<>();
        for (Pe pe : getPeList()){
            peEntries.add(
                    new PeEntry(
                            pe.getMips(),
                            pe.getPeProvisioner().getAvailableMips()
                    )
            );
        }

        addStateHistoryEntry(
                currentTime,
                getUtilizationMips(),
                hostTotalRequestedMips,
                getUtilizationOfRam(),
                hostTotalRequestedRam,
                getUtilizationOfBw(),
                hostTotalRequestedBw,
                (getUtilizationMips() > 0),
                peEntries,
                copy
                );



        return smallerTime;
    }

    /**
     * Adds a host state history entry.
     *
     * @param time the time
     * @param allocatedMips the allocated mips
     * @param requestedMips the requested mips
     * @param isActive the is active
     */
    public
    void
    addStateHistoryEntry(double time, double allocatedMips, double requestedMips, double allocatedRam, double requestedRam, double allocatedBw, double requestedBw, boolean isActive, List<PeEntry> peEntries, List<PowerVm> vmList) {

        MyPowerHostEntry newState = new MyPowerHostEntry(
                time,
                allocatedMips,
                requestedMips,
                isActive,
                allocatedRam,
                requestedRam,
                allocatedBw,
                requestedBw,
                peEntries,
                vmList);
        if (!getStateHistory().isEmpty()) {
            HostStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, newState);
                return;
            }
        }
        getStateHistory().add(newState);
    }
}
