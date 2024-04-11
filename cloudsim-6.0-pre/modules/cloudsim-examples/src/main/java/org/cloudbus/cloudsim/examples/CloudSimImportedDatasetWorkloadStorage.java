package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class CloudSimImportedDatasetWorkloadStorage {
    private static List<Cloudlet> cloudlets;
    private static List<Vm> VMs;
    private static List<Host> hosts;
    private static List<Datacenter> datacenters;

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
    private static Datacenter createDatacenter(String name, int hosts){
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<Host> hostList = new ArrayList<>();

        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.
        List<Pe> peList1 = new ArrayList<>();

        for (int i = 0; i < hosts;i++){
            int mips = 1000;

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
            int ram = 16384; //host memory (MB)
            long storage = 1000000; //host storage
            int bw = 10000;

            hostList.add(
                    new Host(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList1,
                            new VmSchedulerTimeShared(peList1)
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
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static DatacenterBroker createBroker(String name){

        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker(name);
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

            }else{
                Log.printLine("FAILED");
            }
        }

    }

    /*static class MonitorController {
        public static boolean run = true;
    }*/

    public static List<Vm> DatasetVMPerformance(int broker_id) {
        List<Vm> vmFromDataset = new LinkedList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\NASA-iPSC-1993-3.1-cln.swf\\NASA-iPSC-1993-3.1-cln.swf"))) {
            String line;
            int vm_id = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if(!line.startsWith(";")) {
                    String[] features = line.trim().split("\\s+");
                    //Log.printLine(Arrays.toString(features));

                    int jobNumber = Integer.parseInt(features[0].trim());
                    int submitTime = Integer.parseInt(features[1].trim());
                    int waitTime = Integer.parseInt(features[2].trim());
                    int runTime = Integer.parseInt(features[3].trim());
                    int numberOfAllocatedProcessors = Integer.parseInt(features[4].trim());
                    int averageCPUTimeUsed = Integer.parseInt(features[5].trim());
                    int usedMemory = Integer.parseInt(features[6].trim());
                    int requestedNumberOfProcessors = Integer.parseInt(features[7].trim());
                    int requestedTime = Integer.parseInt(features[8].trim());
                    int requestedMemory = Integer.parseInt(features[9].trim());
                    int status = Integer.parseInt(features[10].trim());
                    int userID = Integer.parseInt(features[11].trim());
                    int groupID = Integer.parseInt(features[12].trim());
                    int executableNumber = Integer.parseInt(features[13].trim());
                    int queueNumber = Integer.parseInt(features[14].trim());
                    int partitionNumber = Integer.parseInt(features[15].trim());
                    int preceedingJobNumber = Integer.parseInt(features[16].trim());
                    int thinkTimeFromPreceedingJob = Integer.parseInt(features[17].trim());


                    //VM Parameters
                    long size = 10000; //image size (MB)
                    int ram = 512; //vm memory (MB)
                    int mips = 1000; // 250;
                    long bw = 1000;
                    int pesNumber = 1; //number of cpus
                    String vmm = "Xen"; //VMM name


                    Vm vm = new Vm(vm_id, broker_id, mips, requestedNumberOfProcessors, requestedNumberOfProcessors*ram, 0, size, vmm, new CloudletSchedulerTimeShared());
                    vmFromDataset.add(vm);
                    vm_id++;
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return vmFromDataset;
    }



    public static List<Cloudlet> DatasetJobs(int broker_id) {
        List<Cloudlet> cloudletsFromDataset = new LinkedList<>();

        // https://www.cs.huji.ac.il/labs/parallel/workload/logs.html
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\NASA-iPSC-1993-3.1-cln.swf\\NASA-iPSC-1993-3.1-cln.swf"))) {
            String line;
            bufferedReader.readLine();
            int cloudlet_id = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if(!line.startsWith(";")){
                    String[] features = line.trim().split("\\s+");
                    //Log.printLine(Arrays.toString(features));

                    int jobNumber = Integer.parseInt(features[0].trim());
                    int submitTime = Integer.parseInt(features[1].trim());
                    int waitTime = Integer.parseInt(features[2].trim());
                    int runTime = Integer.parseInt(features[3].trim());
                    int numberOfAllocatedProcessors = Integer.parseInt(features[4].trim());
                    int averageCPUTimeUsed = Integer.parseInt(features[5].trim());
                    int usedMemory = Integer.parseInt(features[6].trim());
                    int requestedNumberOfProcessors = Integer.parseInt(features[7].trim());
                    int requestedTime = Integer.parseInt(features[8].trim());
                    int requestedMemory = Integer.parseInt(features[9].trim());
                    int status = Integer.parseInt(features[10].trim());
                    int userID = Integer.parseInt(features[11].trim());
                    int groupID = Integer.parseInt(features[12].trim());
                    int executableNumber = Integer.parseInt(features[13].trim());
                    int queueNumber = Integer.parseInt(features[14].trim());
                    int partitionNumber = Integer.parseInt(features[15].trim());
                    int preceedingJobNumber = Integer.parseInt(features[16].trim());
                    int thinkTimeFromPreceedingJob = Integer.parseInt(features[17].trim());

                    UtilizationModel utilizationModel = new UtilizationModelFull();

                    long length = 40000;
                    long fileSize = 300;
                    long outputSize = 300;
                    int pesNumber = 1;

                    Cloudlet cloudlet = new Cloudlet(cloudlet_id, runTime, requestedNumberOfProcessors, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);

                    cloudlet.setVmId(cloudlet_id);
                    cloudlet.setUserId(broker_id);
                    cloudletsFromDataset.add(cloudlet);
                    cloudlet_id++;
                }



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

            datacenters = new ArrayList<Datacenter>();
            Datacenter datacenter1 = createDatacenter("Datacenter_1", 1000);
            //Datacenter datacenter2 = createDatacenter("Datacenter_2", 400);
            datacenters.add(datacenter1);
            //datacenters.add(datacenter2);

            DatacenterBroker broker = createBroker("Central");

            VMs = DatasetVMPerformance(broker.getId());//createVM(broker.getId(), 5, 0); //creating 5 vms

            cloudlets = DatasetJobs(broker.getId());// createCloudlet(broker.getId(), 10, 0); // creating 10 cloudlets

            broker.submitVmList(VMs);
            broker.submitCloudletList(cloudlets);

            /*final CloudsimTest.MonitorController runMonitorThread = new CloudsimTest.MonitorController();
            Runnable monitor = () -> {
                int interval = 1000;
                int i = 0;
                while (runMonitorThread.run){
                    try{
                        Thread.sleep(interval);

                        Log.printLine("");
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

                        }


                    }catch (Exception e){
                        Log.printLine("Error in monitor");
                        Log.printLine(e.getMessage());
                    }

                    i++;
                }
            };*/



            // Fifth step: Starts the simulation
            CloudSim.startSimulation();
            /*new Thread(monitor).start();
            Thread.sleep(10000);*/
            // Final step: Print results when simulation is over

            /*int  usedPes = 0;
            int  totalPes = 0;
            for(Datacenter datacenter : datacenters){
                Log.printLine("Datacenter: " + datacenter.getName());

                for (Host host : datacenter.getHostList()){
                    int used = 0;
                    for (Vm v : host.getVmList()){
                        used += v.getNumberOfPes();
                    }
                    Log.print("Host PEs (");
                    Log.print(host.getNumberOfPes() - used);
                    Log.print("/");
                    Log.print(host.getNumberOfPes());
                    Log.printLine(")");

                    usedPes += used;
                    totalPes += host.getNumberOfPes();
                }

            }

            Log.printLine("---------------");

            Log.print("Total PEs (");
            Log.print(totalPes - usedPes);
            Log.print("/");
            Log.print(totalPes);
            Log.printLine(")");

            Log.printLine("---------------");*/

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            //Log.printLine("---------------");

            CloudSim.stopSimulation();
            /*runMonitorThread.run = false;*/

            printCloudletList(newList);

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
