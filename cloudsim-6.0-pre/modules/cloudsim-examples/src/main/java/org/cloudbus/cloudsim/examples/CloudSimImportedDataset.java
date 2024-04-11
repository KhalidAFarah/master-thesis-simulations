package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.mychanges.*;
import org.cloudbus.cloudsim.power.*;
import org.cloudbus.cloudsim.power.models.PowerModelSpecHpProLiantDl165G7AMDOpteron6276;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G3PentiumD930;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class CloudSimImportedDataset {
    private static List<Cloudlet> cloudlets;
    private static List<MyPowerVm> VMs;
    private static List<MyPowerHost> hosts;
    private static List<PowerDatacenter> datacenters;

    private static List<Vm> createVM(int userId, int vms, int idShift) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 250;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];

        for(int i=0;i<vms;i++){
            vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            list.add(vm[i]);
        }

        return list;
    }


    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift){
        // Creates a container to store Cloudlets
        LinkedList<Cloudlet> list = new LinkedList<>();

        //cloudlet parameters
        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelNull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];

        for(int i=0;i<cloudlets;i++){
            cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            // setting the owner of these Cloudlets
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }
    private static PowerDatacenter createDatacenter(String name, int Ml110G3Hosts, int Ml110G4Hosts, int Ml110G5Hosts, List<MyPowerHost> list, int idShift){
        // https://www.spec.org/power_ssj2008/results/res2011q4/power_ssj2008-20111018-00401.html

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines

        List<MyPowerHost> hostList;
        if(list != null){
            hostList = list;
        }else{

            hostList = new ArrayList<>();
        }


        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.

        //http://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110127-00342.html
        for (int i = 0; i < Ml110G3Hosts;i++){
            int mips = 3000;
            int coresPerHost = 2;
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < coresPerHost; j++){
                peList.add(new Pe(i*j, new PeProvisionerSimple(mips)));
            }

            int hostId = i + idShift;
            int ram = 4000;
            int storage = 160000;
            int bandwidth = 1000;
            hostList.add(
                    new MyPowerHost(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bandwidth),
                            storage,
                            peList,
                            new VmSchedulerTimeSharedOverSubscription(peList),
                            new PowerModelSpecPowerHpProLiantMl110G3PentiumD930()
                    )
            );
        }

        // https://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110124-00338.html
        for (int i = 0; i < Ml110G4Hosts;i++){
            int mips = 1860;
            int coresPerHost = 2;
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < coresPerHost; j++){
                peList.add(new Pe(i*j, new PeProvisionerSimple(mips)));
            }

            int hostId = i + Ml110G4Hosts + idShift;
            int ram = 4000;
            int storage = 160000;
            int bandwidth = 1000;
            hostList.add(
                    new MyPowerHost(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bandwidth),
                            storage,
                            peList,
                            new VmSchedulerTimeSharedOverSubscription(peList),
                            new PowerModelSpecPowerHpProLiantMl110G4Xeon3040()
                    )
            );
        }

        // https://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110124-00339.html
        for (int i = 0; i < Ml110G5Hosts;i++){
            int mips = 2660;
            int coresPerHost = 2;
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < coresPerHost; j++){
                peList.add(new Pe(i*j, new PeProvisionerSimple(mips)));
            }

            int hostId = i + Ml110G5Hosts + idShift;
            int ram = 4000;
            int storage = 146000;
            int bandwidth = 1000;
            hostList.add(
                    new MyPowerHost(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bandwidth),
                            storage,
                            peList,
                            new VmSchedulerTimeSharedOverSubscription(peList),
                            new PowerModelSpecPowerHpProLiantMl110G5Xeon3075()
                    )
            );
        }




        /*for (int i = 0; i < hosts;i++){
            int mips = PowerModelSpecHpProLiantDl165G7AMDOpteron6276.MIPS; // 2500 // 1000

            List<Pe> peList1 = new ArrayList<>();
            // 3. Create PEs and add these into the list.
            //for a quad-core machine, a list of 4 PEs is required:
            int coresPerHost = PowerModelSpecHpProLiantDl165G7AMDOpteron6276.CORES; // 4
            for (int j = 0; j < coresPerHost; j++){
                peList1.add(new Pe(j*i, new PeProvisionerSimple(mips)));
            }
            /*peList1.add(new Pe(1*i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
            peList1.add(new Pe(2*i, new PeProvisionerSimple(mips)));
            peList1.add(new Pe(3*i, new PeProvisionerSimple(mips)));
            peList1.add(new Pe(4*i, new PeProvisionerSimple(mips)));*/

            /*
            //Another list, for a dual-core machine
            List<Pe> peList2 = new ArrayList<>();

            peList2.add(new Pe(0*i, new PeProvisionerSimple(mips)));
            peList2.add(new Pe(1*i, new PeProvisionerSimple(mips)));
             */

        /*
            //4. Create Hosts with its id and list of PEs and add them to the list of machines
            int hostId = i; //425021
            int ram = PowerModelSpecHpProLiantDl165G7AMDOpteron6276.RAM;//16384*4*1000; //host memory (MB)
            long storage = PowerModelSpecHpProLiantDl165G7AMDOpteron6276.DISK_SPACE;//1000000; //host storage
            int bw = PowerModelSpecHpProLiantDl165G7AMDOpteron6276.BW; //10000;

            hostList.add(
                    new MyPowerHost(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList1,
                            new VmSchedulerTimeSharedOverSubscription(peList1),
                            new PowerModelSpecHpProLiantDl165G7AMDOpteron6276()

                    )
            ); // This is our first machine
            //PowerModelSpecPowerHpProLiantMl110G5Xeon3075
            /*
            hostId = 1*i;

            hostList.add(
                    new Host(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList2,
                            new VmSchedulerTimeShared(peList2)
                    )
            ); // Second machine

             */

        /*
        }

         */


        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        PowerDatacenter datacenter = null;
        try {
            //datacenter = new PowerDatacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
            datacenter = new PowerDatacenter(name, characteristics, new PowerVmAllocationPolicySimple(hostList), storageList, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static PowerDatacenterBroker createBroker(String name){

        PowerDatacenterBroker broker = null;
        try {
            broker = new PowerDatacenterBroker(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects
     * @param list  list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (Cloudlet value : list) {
            cloudlet = value;
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
                        indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }

    /*static class MonitorController {
        public static boolean run = true;
    }*/

    public static List<MyPowerVm> DatasetVMPerformance(int broker_id) {
        List<MyPowerVm> vmFromDataset = new LinkedList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\Bitbrains\\3.csv"))) {
            String line;
            bufferedReader.readLine();
            int vm_id = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] features = line.split(";\t");

                UtilizationModel utilizationModel = new UtilizationModelFull();

                /*System.out.println(features[0]);
                System.out.println(features[1]);
                System.out.println(features[2]);
                System.out.println(features[3]);
                System.out.println(features[4]);
                System.out.println(features[5]);
                System.out.println(features[6]);
                System.out.println(features[7]);
                System.out.println(features[8]);
                System.out.println(features[9]);*/

                String timestamp = features[0];
                int CPUCores = Integer.parseInt(features[1]);
                double CPUCapacityProvisioned = Double.parseDouble(features[2]);
                double CPUUsage = Double.parseDouble(features[3]);
                double CPUUsagePercent = Double.parseDouble(features[4]);
                double memoryCapacityProvisioned = Double.parseDouble(features[5]);
                double memoryUsage = Double.parseDouble(features[6]);
                double diskReadThroughput = Double.parseDouble(features[7]);
                double diskWriteThroughput = Double.parseDouble(features[8]);
                double networkReceivedThroughput = Double.parseDouble(features[9]);
                double networkTransmittedThroughput = Double.parseDouble(features[10]);


                //VM Parameters
                long size = 10000; //image size (MB)
                int ram = 512; //vm memory (MB)
                int mips = 250;
                long bw = 1000;
                int pesNumber = 1; //number of cpus
                String vmm = "Xen"; //VMM name

                if (networkReceivedThroughput >= networkTransmittedThroughput){
                    bw = (long) networkReceivedThroughput;
                }else{
                    bw = (long) networkTransmittedThroughput;
                }
                //Vm  vm = new Vm(vm_id, broker_id, mips*CPUCores, CPUCores, (int) (memoryCapacityProvisioned/1000), bw, size, vmm, new CloudletSchedulerTimeShared());
                //MyPowerVm vm = new MyPowerVm(vm_id, broker_id, CPUUsage, CPUCores,  (int) (memoryUsage / 1000), (long) ((networkReceivedThroughput + networkTransmittedThroughput) / 1000), size, 1, vmm, new CloudletSchedulerDynamicWorkload(CPUUsage, CPUCores), 300);
                MyPowerVm vm = new MyPowerVm(vm_id, broker_id, CPUUsage, CPUCores,  (int) (memoryUsage / 1000), (long) ((networkReceivedThroughput + networkTransmittedThroughput) / 1000), size, 1, vmm, new CloudletSchedulerDynamicWorkload(CPUUsage, CPUCores), 300);
                vmFromDataset.add(vm);
                vm_id++;


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return vmFromDataset;
    }

    public static List<Cloudlet> DatasetJobs(int broker_id, int simulation_limit) {
        List<Cloudlet> cloudletsFromDataset = new LinkedList<>();
        HashMap<String, Integer> userIds = new HashMap<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\Bitbrains\\3.csv"))) {
            String line;
            bufferedReader.readLine();
            int cloudlet_id = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] features = line.split(";\t");

                UtilizationModel utilizationModel = new UtilizationModelFull();

                String timestamp = features[0];
                int CPUCores = Integer.parseInt(features[1]);
                double CPUCapacityProvisioned = Double.parseDouble(features[2]);
                double CPUUsage = Double.parseDouble(features[3]);
                double CPUUsagePercent = Double.parseDouble(features[4]);
                double memoryCapacityProvisioned = Double.parseDouble(features[5]);
                double memoryUsage = Double.parseDouble(features[6]);
                double diskReadThroughput = Double.parseDouble(features[7]);
                double diskWriteThroughput = Double.parseDouble(features[8]);
                double networkReceivedThroughput = Double.parseDouble(features[9]);
                double networkTransmittedThroughput = Double.parseDouble(features[10]);


                long length = 24 * simulation_limit;
                long fileSize = 300;
                long outputSize = 300;
                int pesNumber = 1;

                Cloudlet cloudlet = new Cloudlet(cloudlet_id, length, CPUCores, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setVmId(cloudlet_id);
                cloudlet.setUserId(broker_id);
                cloudletsFromDataset.add(cloudlet);
                cloudlet_id++;

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cloudletsFromDataset;
    }


    public static void main(String[] args){
        try{
            int users = 1;
            Calendar calender = Calendar.getInstance();
            boolean trace_flags = false;

            CloudSim.init(users, calender, trace_flags);

            int[] hosts_datacenter = {150*3, 145*3, 120*3, 150*3};
            datacenters = new ArrayList<PowerDatacenter>();
            PowerDatacenter datacenter1 = createDatacenter("Datacenter_1", 100*3, 25*3, 25*3, null, 0);
            PowerDatacenter datacenter2 = createDatacenter("Datacenter_2", 10*3, 100*3, 35*3, datacenter1.getHostList(), hosts_datacenter[0]);
            PowerDatacenter datacenter3 = createDatacenter("Datacenter_3", 0*3, 20*3, 100*3, datacenter2.getHostList(), hosts_datacenter[1]);
            PowerDatacenter datacenter4 = createDatacenter("Datacenter_4", 50*3, 50*3, 50*3, datacenter3.getHostList(), hosts_datacenter[2]);

            datacenter1.setDisableMigrations(false);
            datacenter2.setDisableMigrations(false);
            datacenter3.setDisableMigrations(false);
            datacenter4.setDisableMigrations(false);
            datacenters.add(datacenter1);
            datacenters.add(datacenter2);
            datacenters.add(datacenter3);
            datacenters.add(datacenter4);

            int SIMULATION_LIMIT = 24*60*60;

            PowerDatacenterBroker broker = createBroker("Central");

            VMs = DatasetVMPerformance(broker.getId());//createVM(broker.getId(), 5, 0); //creating 5 vms
            cloudlets = DatasetJobs(broker.getId(), SIMULATION_LIMIT);// createCloudlet(broker.getId(), 10, 0); // creating 10 cloudlets

            broker.submitVmList(VMs);
            broker.submitCloudletList(cloudlets);

            /*final CloudsimTest.MonitorController runMonitorThread = new CloudsimTest.MonitorController();
            Runnable monitor = () -> {
                int interval = 1000;
                int i = 0;
                while (runMonitorThread.run){
                    try{
                        Thread.sleep(interval);

                        java.io.File logfile = new java.io.File("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\logs\\logfile"+i+".txt");
                        try {
                            logfile.createNewFile();
                        } catch (IOException e) {
                            //throw new RuntimeException(e);
                        }
                        String msg = "";



                        for(PowerDatacenter datacenter : datacenters){
                            for(Host host : datacenter.getHostList()){
                                int usedPe = 0;
                                int usedRam = 0;
                                for (Vm vm : host.getVmList()){
                                    usedPe += vm.getNumberOfPes();
                                    usedRam += vm.getCurrentAllocatedRam();
                                }

                                msg += datacenter.getName() + ";" +  host.getId() + ";" + host.getNumberOfPes() + ";" + (host.getNumberOfPes()+usedPe) + ";" + host.getRamProvisioner().getRam() + ";" + (host.getRamProvisioner().getAvailableRam()+usedRam) + ";\n";
                            }
                        }

                        try {
                            FileWriter writer = new FileWriter("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\logs\\logfile"+i+".txt");
                            writer.write(msg);
                            writer.close();
                        } catch (IOException e) {
                            //throw new RuntimeException(e);
                        }

                        /*Log.printLine("");
                        Log.printLine("-----------------");
                        for(Datacenter datacenter : datacenters){
                            Log.printLine("Datacenter: " + datacenter.getName());
                            for (Host host : datacenter.getHostList()){

                                Log.print("Host: ");
                                Log.printLine(host.getId());

                                Log.print("PEs (");
                                Log.print(host.getNumberOfFreePes());
                                Log.print("/");
                                Log.print(host.getNumberOfPes());
                                Log.printLine(")");
                            }

                        }*/
                    /*

                    }catch (Exception e){
                        Log.printLine("Error in monitor");
                        Log.printLine(e.getMessage());
                    }

                    i++;
                }
            };*/



            // Fifth step: Starts the simulation

            CloudSim.terminateSimulation(SIMULATION_LIMIT);
            CloudSim.startSimulation();
            //new Thread(monitor).start();
            //Thread.sleep(10000);
            // Final step: Print results when simulation is over

            System.out.println("Logging results...");
            java.io.File logfile = new java.io.File("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\logs\\logfile.csv");
            try {
                logfile.createNewFile();
            } catch (IOException e) {
                //throw new RuntimeException(e);
                System.err.println("Can't create file");
            }
            String msg = "time;datacenter_name;host_id;type;active;number_of_pes;available_pes;mips;available_mips;utilization_per_pe;ram;available_ram;bw;available_bw;power_model;vms;\n"; // frequencies;mips_per_frequency;cpu_idle_power_per_frequency;cpu_full_power_per_frequency;


            int datacenter_number = 0;
            for(PowerDatacenter datacenter : datacenters){
                int start_index = 0;
                int end_index = 0;
                if (datacenter_number == 0){
                    start_index = 0;
                    end_index = hosts_datacenter[datacenter_number] - 1;
                }else {
                    start_index = 0;
                    end_index = hosts_datacenter[datacenter_number] - 1;

                    for (int j = datacenter_number-1; j >= 0; j--){
                        start_index += hosts_datacenter[datacenter_number - 1];
                        end_index += hosts_datacenter[datacenter_number - 1];
                    }
                }
                /*else if(datacenter_number == 1) {
                    start_index = hosts_datacenter[datacenter_number - 1];
                    end_index = start_index + hosts_datacenter[datacenter_number] - 1;
                } else if (datacenter_number == 2) {
                    start_index = hosts_datacenter[datacenter_number - 1];
                    end_index = start_index + hosts_datacenter[datacenter_number] - 1;
                }*/
                for(Host host : datacenter.getHostList().subList(start_index, end_index)){ // .subList(start_index, end_index)
                    if(host instanceof MyPowerHost){

                        /*String frequencies = "";
                        String mipsPerFrequency = "";
                        String cpuIdlePerFrequency = "";
                        String cpuFullPerFrequency = "";
                        if(((MyPowerHost) host).getPowerModel() instanceof PowerModelSpecHpProLiantDl165G7AMDOpteron6276) {
                            for (int i = 0; i < 4; i++) {
                                frequencies += PowerModelSpecHpProLiantDl165G7AMDOpteron6276.AVAILABLE_FREQUENCIES[i] + ",";
                                mipsPerFrequency += PowerModelSpecHpProLiantDl165G7AMDOpteron6276.AVAILABLE_FREQUENCIES_AS_MIPS[i] + ",";
                                cpuIdlePerFrequency += PowerModelSpecHpProLiantDl165G7AMDOpteron6276.CPU_IDLE_POWER_PER_FREQUENCY[i] + ",";
                                cpuFullPerFrequency += PowerModelSpecHpProLiantDl165G7AMDOpteron6276.CPU_FULL_POWER_PER_FREQUENCY[i] + ",";
                            }
                        }*/

                        String powermodel = "";
                        double[] utilizations = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
                        for(double utilization : utilizations){
                            powermodel += ((MyPowerHost) host).getPowerModel().getPower(utilization) + ",";
                        }

                        int index = 0;
                        for(HostStateHistoryEntry entry : ((MyPowerHost) host).getStateHistory()){
                            if(entry instanceof  MyPowerHostEntry){

                                String vmInfo = "";
                                for (Vm vm : ((MyPowerHostEntry) entry).getVms()){
                                    //vmInfo += vm.getNumberOfPes() + "," + vm.getMips() + "," + vm.getRam() + "," + vm.getBw() + ":";
                                    if (vm instanceof MyPowerVm){
                                        vmInfo += vm.getNumberOfPes() + "," + ((MyPowerVmEntry) vm.getStateHistory().get(index)).getAllocatedMips() + "," + ((MyPowerVmEntry) vm.getStateHistory().get(index)).getAllocatedRam() + "," + ((MyPowerVmEntry) vm.getStateHistory().get(index)).getAllocatedBw() + ":";
                                    }
                                }

                                String peUtilizationInfo = "";
                                int freePes = 0;
                                for (PeEntry peEntry : ((MyPowerHostEntry) entry).getPeEntries()){
                                    if(peEntry.getAvailableMIPS() == 0){
                                        freePes++;
                                    }
                                    peUtilizationInfo += peEntry.getMaxMIPS() + "," + peEntry.getAvailableMIPS() + ":";
                                }



                                msg += entry.getTime() + ";" + datacenter.getName() + ";" +  host.getId() + ";host;" + entry.isActive() + ";" + host.getNumberOfPes() + ";" +  freePes + ";" + host.getTotalMips() + ";" + (host.getTotalMips() - entry.getAllocatedMips()) + ";" + peUtilizationInfo + ";" + host.getRamProvisioner().getRam() + ";" + ((MyPowerHostEntry) entry).getAllocatedRam() + ";" + host.getBwProvisioner().getBw() + ";" + ((MyPowerHostEntry) entry).getAllocatedBw() + ";" + powermodel + ";" + vmInfo + ";\n"; // + ";" + frequencies + ";" + mipsPerFrequency + ";" + cpuIdlePerFrequency + ";" + cpuFullPerFrequency

                            }else{
                                System.err.println("Err");
                            }
                            index++;
                        }

                        /*int mips = 0;
                        int availableMips = 0;
                        int availablePes = 0;
                        for (Pe pe : host.getPeList()){
                            mips += pe.getPeProvisioner().getMips();
                            availableMips += pe.getPeProvisioner().getAvailableMips();
                            if(pe.getPeProvisioner().getMips() == pe.getPeProvisioner().getAvailableMips()){
                                availablePes++;
                            }
                        }

                        String vmInfo = "";
                        for (Vm vm : host.getVmList()){
                            vmInfo += vm.getNumberOfPes() + "," + vm.getMips() + "," + vm.getRam() + "," + vm.getBw() + ":";
                        }


                        msg += datacenter.getName() + ";" +  host.getId() + ";host;" + host.getNumberOfPes() + ";" + availablePes + ";" + mips + ";" + availableMips + ";" + host.getRamProvisioner().getRam() + ";" + host.getRamProvisioner().getAvailableRam() + ";" + host.getBwProvisioner().getBw() + ";" + host.getBwProvisioner().getAvailableBw() + ";" + powermodel + ";" + vmInfo + ";\n";
                        */
                    }
                }
                datacenter_number++;
            }

            try {
                FileWriter writer = new FileWriter("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\logs\\logfile.csv");
                writer.write(msg);
                writer.close();
            } catch (IOException e) {
                //throw new RuntimeException(e);
                System.err.println("Can't write to file");
            }

            //List<Cloudlet> newList = broker.getCloudletReceivedList();

            Log.printLine("---------------");

            CloudSim.stopSimulation();
            /*runMonitorThread.run = false;*/

            //printCloudletList(newList);

            Log.printLine("Test finished!");


            /*Log.printLine("--------------------------");
            for(Cloudlet cloudlet : cloudlets){
                Log.printLine(cloudlet.get);
            }*/
        }catch (Exception e){
            Log.printLine("Error:");
            Log.printLine(e.getMessage());
        }


    }
}
