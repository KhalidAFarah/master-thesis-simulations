package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
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

public class CloudSimImportedDatasetStorage {
    private static List<Cloudlet> cloudlets;
    private static List<Vm> VMs;
    private static List<PowerHostUtilizationHistory> hosts;
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
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];

        for(int i=0;i<cloudlets;i++){
            cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            // setting the owner of these Cloudlets
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }
    private static PowerDatacenter createDatacenter(String name, int hosts){
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<PowerHostUtilizationHistory> hostList = new ArrayList<>();

        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.

        for (int i = 0; i < hosts;i++){
            int mips = 1000;

            List<Pe> peList1 = new ArrayList<>();
            // 3. Create PEs and add these into the list.
            //for a quad-core machine, a list of 4 PEs is required:
            peList1.add(new Pe(1*i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
            peList1.add(new Pe(2*i, new PeProvisionerSimple(mips)));
            peList1.add(new Pe(3*i, new PeProvisionerSimple(mips)));
            peList1.add(new Pe(4*i, new PeProvisionerSimple(mips)));

            /*
            //Another list, for a dual-core machine
            List<Pe> peList2 = new ArrayList<>();

            peList2.add(new Pe(0*i, new PeProvisionerSimple(mips)));
            peList2.add(new Pe(1*i, new PeProvisionerSimple(mips)));
             */

            //4. Create Hosts with its id and list of PEs and add them to the list of machines
            int hostId = i; //425021
            int ram = 16384*4*1000; //host memory (MB)
            long storage = 1000000; //host storage
            int bw = 10000;

            hostList.add(
                    new PowerHostUtilizationHistory(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList1,
                            new VmSchedulerTimeShared(peList1),
                            new PowerModelSpecPowerHpProLiantMl110G5Xeon3075()

                    )
            ); // This is our first machine

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
        }


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
            datacenter = new PowerDatacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
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

    public static List<Vm> DatasetVMPerformance(int broker_id) {
        List<Vm> vmFromDataset = new LinkedList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\Bitbrains\\sample.csv"))) {
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
                Vm  vm = new Vm(vm_id, broker_id, mips, CPUCores, (int) memoryCapacityProvisioned, bw, size, vmm, new CloudletSchedulerTimeShared());
                vmFromDataset.add(vm);
                vm_id++;


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return vmFromDataset;
    }

    public static List<Cloudlet> DatasetJobs() {
        List<Cloudlet> cloudletsFromDataset = new LinkedList<>();
        HashMap<String, Integer> userIds = new HashMap<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\sample.csv"))) {
            String line;
            bufferedReader.readLine();
            int cloudlet_id = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] features = line.split(";");

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


                long length = 40000;
                long fileSize = 300;
                long outputSize = 300;
                int pesNumber = 1;

                Cloudlet cloudlet = new Cloudlet(cloudlet_id, length, CPUCores, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setVmId(cloudlet_id);
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

            datacenters = new ArrayList<PowerDatacenter>();
            PowerDatacenter datacenter1 = createDatacenter("Datacenter_1", 500);
            PowerDatacenter datacenter2 = createDatacenter("Datacenter_2", 500);
            datacenters.add(datacenter1);
            datacenters.add(datacenter2);

            PowerDatacenterBroker broker = createBroker("Central");

            VMs = DatasetVMPerformance(broker.getId());//createVM(broker.getId(), 5, 0); //creating 5 vms

            //cloudlets = DatasetJobs();// createCloudlet(broker.getId(), 10, 0); // creating 10 cloudlets

            broker.submitVmList(VMs);
            //broker.submitCloudletList(cloudlets);

            final CloudsimTest.MonitorController runMonitorThread = new CloudsimTest.MonitorController();
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


                    }catch (Exception e){
                        Log.printLine("Error in monitor");
                        Log.printLine(e.getMessage());
                    }

                    i++;
                }
            };



            // Fifth step: Starts the simulation
            CloudSim.startSimulation();
            //new Thread(monitor).start();
            //Thread.sleep(10000);
            // Final step: Print results when simulation is over

            java.io.File logfile = new java.io.File("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\logs\\logfile.txt");
            try {
                logfile.createNewFile();
            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
            String msg = "";



            for(PowerDatacenter datacenter : datacenters){
                for(Host host : datacenter.getHostList()){
                    /*int usedPe = 0;
                    int usedRam = 0;
                    for (Vm vm : host.getVmList()){
                        usedPe += vm.getNumberOfPes();
                        usedRam += vm.getCurrentAllocatedRam();
                    }*/

                    int mips = 0;
                    int availableMips = 0;
                    for (Pe pe : host.getPeList()){
                        mips += pe.getPeProvisioner().getMips();
                        availableMips += pe.getPeProvisioner().getAvailableMips();
                    }


                    msg += datacenter.getName() + ";" +  host.getId() + ";host;" + host.getNumberOfPes() + ";" + mips + ";" + availableMips + ";" + host.getRamProvisioner().getRam() + ";" + host.getRamProvisioner().getAvailableRam() + ";" + host.getBwProvisioner().getBw() + ";" + host.getBwProvisioner().getAvailableBw() + ";\n";
                }
            }

            try {
                FileWriter writer = new FileWriter("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\logs\\logfile.txt");
                writer.write(msg);
                writer.close();
            } catch (IOException e) {
                //throw new RuntimeException(e);
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
