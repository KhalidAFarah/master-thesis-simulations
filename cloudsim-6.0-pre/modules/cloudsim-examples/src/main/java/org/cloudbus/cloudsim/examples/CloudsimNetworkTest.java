package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class CloudsimNetworkTest {
    private static List<NetworkDatacenter> networkDatacenters = new ArrayList<>();
    private static List<NetworkHost> networkHosts = new ArrayList<>();
    private static List<RootSwitch> rootSwitches = new ArrayList<>();
    private static List<AggregateSwitch> aggregateSwitches = new ArrayList<>();
    private static List<EdgeSwitch> edgeSwitches = new ArrayList<>();
    private static List<NetworkVm> networkVms = new ArrayList<>();
    private static List<NetworkCloudlet> networkCloudlets = new ArrayList<>();
    private static List<AppCloudlet> appCloudlets = new ArrayList<>();

    private static HashMap<String, Integer> links = new HashMap<>();

    public void CreateNetworkDatacenter(String datacenterName, String switchName){
        String systemArchitecture = "x86";
        String operatingsystem = "Linux";
        String virtualMachineManager = "Xen";
        double timezone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBandwidth = 0;
        LinkedList<Storage> storageList = new LinkedList<Storage>();
        //List<NetworkHost> hosts = new LinkedList<>();
        DatacenterCharacteristics datacenterCharacteristics = new DatacenterCharacteristics(systemArchitecture,
                operatingsystem, virtualMachineManager, networkHosts, timezone, cost, costPerMem, costPerStorage, costPerBandwidth);

        double schedulingInterval = 0;

        NetworkDatacenter networkDatacenter;
        try{
            networkDatacenter = new NetworkDatacenter(datacenterName, datacenterCharacteristics, new VmAllocationPolicySimple(networkHosts), storageList, schedulingInterval);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public EdgeSwitch CreateServerRack(String switchName, int level, NetworkDatacenter networkDatacenter, AggregateSwitch aggregateSwitch, int hosts, int shift){
        EdgeSwitch edgeSwitch = new EdgeSwitch(switchName, level, networkDatacenter);
        edgeSwitch.numport = 4;

        int mips = 1000;
        int ram = 2048*4; // memory (MB)
        long storage = 1000000; // storage
        int bw = 10000;

        ArrayList<Pe> peList = new ArrayList<>();
        for(int i = 0; i < hosts; i++){

            peList.add(new Pe(0, new PeProvisionerSimple(mips)));
            peList.add(new Pe(1, new PeProvisionerSimple(mips)));
            peList.add(new Pe(2, new PeProvisionerSimple(mips)));
            peList.add(new Pe(3, new PeProvisionerSimple(mips)));

            NetworkHost networkHost = new NetworkHost(i+shift, new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw), storage,peList, new VmSchedulerTimeShared(peList));

            networkHost.setDatacenter(networkDatacenter);
            networkHost.sw = edgeSwitch;
            networkHosts.add(networkHost);
        }
        edgeSwitches.add(edgeSwitch);
        return edgeSwitch;
    }

    public static NetworkDatacenter createNetworkedDatacenter(String datacenterName, int hosts){
        int mips = 1000;
        int ram = 2048*4; // memory (MB)
        long storage = 1000000; // storage
        int bw = 10000;

        ArrayList<Pe> peList = new ArrayList<>();
        for(int i = 0; i < hosts; i++){

            peList.add(new Pe(0, new PeProvisionerSimple(mips)));
            peList.add(new Pe(1, new PeProvisionerSimple(mips)));
            peList.add(new Pe(2, new PeProvisionerSimple(mips)));
            peList.add(new Pe(3, new PeProvisionerSimple(mips)));

            NetworkHost networkHost = new NetworkHost(i, new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw), storage,peList, new VmSchedulerTimeShared(peList));
            networkHost.bandwidth = bw;


            networkHosts.add(networkHost);
        }

        String systemArchitecture = "x86";
        String operatingsystem = "Linux";
        String virtualMachineManager = "Xen";
        double timezone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBandwidth = 0;
        LinkedList<Storage> storageList = new LinkedList<Storage>();
        DatacenterCharacteristics datacenterCharacteristics = new DatacenterCharacteristics(systemArchitecture,
                operatingsystem, virtualMachineManager, networkHosts, timezone, cost, costPerMem, costPerStorage, costPerBandwidth);

        double schedulingInterval = 0;

        NetworkDatacenter networkDatacenter;
        try{
            networkDatacenter = new NetworkDatacenter(datacenterName, datacenterCharacteristics, new VmAllocationPolicySimple(networkHosts), storageList, schedulingInterval);
        }catch (Exception e){
            networkDatacenter = null;
            e.printStackTrace();
        }

        int i = 0;
        int hostsPerEdgeSwitch = 4;
        EdgeSwitch edgeSwitch = null;
        //EdgeSwitch edgeSwitch = new EdgeSwitch(datacenterName + "-edge-" + i , 2, networkDatacenter);
        //edgeSwitch.numport = 4;
        //edgeSwitch.id = edgeSwitches.size();
        //edgeSwitches.add(edgeSwitch);
        while(i < networkHosts.size()){

            if(i % hostsPerEdgeSwitch == 0){
                edgeSwitch = new EdgeSwitch(datacenterName + "-edge-" + i/hostsPerEdgeSwitch, 2, networkDatacenter);
                edgeSwitch.numport = 4;
                edgeSwitch.id = edgeSwitches.size();
                networkDatacenter.Switchlist.put(edgeSwitch.id, edgeSwitch);
                edgeSwitches.add(edgeSwitch);
            }
            links.put(edgeSwitch.id+"-"+i,0);
            networkHosts.get(i).sw = edgeSwitch;
            edgeSwitch.hostlist.put(networkHosts.get(i).getId(), networkHosts.get(i));
            networkHosts.get(i).setDatacenter(networkDatacenter);
            networkDatacenter.HostToSwitchid.put(networkHosts.get(i).getId(), edgeSwitch.getId());

            i++;
        }

        i = 0;
        int edgeSwitchesPerAggregateSwitch = 4;
        AggregateSwitch aggregateSwitch = null;
        //AggregateSwitch aggregateSwitch = new AggregateSwitch(datacenterName + "-aggregate-" + i , 1, networkDatacenter);
        //aggregateSwitch.numport = 4;
        //aggregateSwitch.id = edgeSwitches.size() + aggregateSwitches.size();
        //aggregateSwitches.add(aggregateSwitch);
        while(i < edgeSwitches.size()){

            if(i % hostsPerEdgeSwitch == 0){
                aggregateSwitch = new AggregateSwitch(datacenterName + "-aggregate-" + i/edgeSwitchesPerAggregateSwitch, 1, networkDatacenter);
                aggregateSwitch.numport = 4;
                aggregateSwitch.id = edgeSwitches.size() + aggregateSwitches.size();
                networkDatacenter.Switchlist.put(aggregateSwitch.id, aggregateSwitch);
                aggregateSwitches.add(aggregateSwitch);
            }
            links.put(aggregateSwitch.id+"-"+i,0);
            aggregateSwitch.downlinkswitches.add(edgeSwitches.get(i));
            edgeSwitches.get(i).uplinkswitches.add(aggregateSwitch);

            i++;
        }

        i = 0;
        RootSwitch rootSwitch = new RootSwitch(datacenterName + "-root", 0, networkDatacenter);
        rootSwitch.numport = 4;
        rootSwitch.id = edgeSwitches.size() + aggregateSwitches.size();
        networkDatacenter.Switchlist.put(rootSwitch.id, rootSwitch);
        //rootSwitch.uplinkbandwidth = 1000;
        //rootSwitch.downlinkbandwidth = 1000;
        while(i < aggregateSwitches.size()){

            links.put(rootSwitch.id+"-"+i,0);
            rootSwitch.downlinkswitches.add(aggregateSwitches.get(i));
            aggregateSwitches.get(i).uplinkswitches.add(rootSwitch);

            i++;
        }
        rootSwitches.add(rootSwitch);
        return networkDatacenter;
    }

    public static void main(String[] args){
        Log.printLine("Starting test for the networked simulation...");

        try{
            CloudSim.init(1, Calendar.getInstance(), false);

            NetworkDatacenter networkDatacenter = createNetworkedDatacenter("Datacenter", 4*4*4);
            networkDatacenters.add(networkDatacenter);

            NetDatacenterBroker broker = new NetDatacenterBroker("Broker");

            //VM description
            int vmid = 0;
            int mips = 250;
            long size = 10000; //image size (MB)
            int ram = 512; //vm memory (MB)
            long bw = 1000;
            int pesNumber = 1; //number of cpus
            String vmm = "Xen"; //VMM name

            //create VM
            int num = 500;
            for(int i = 0; i < num; i++){
                NetworkVm vm = new NetworkVm(i, broker.getId(), mips, pesNumber, ram, bw, size, vmm, new NetworkCloudletSpaceSharedScheduler());
                networkVms.add(vm);
            }
            //NetworkVm vm1 = new NetworkVm(vmid, broker.getId(), mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            //vmid++;
            //NetworkVm vm2 = new NetworkVm(vmid, broker.getId(), mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

            //add the VM to the vmList
            //networkVms.add(vm1);
            //networkVms.add(vm2);

            //submit vm list to the broker
            broker.submitVmList(networkVms);




            //Cloudlet properties
            int id = 0;
            long length = 40000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            Random random = new Random();
            for(int i = 0; i < num*2; i+=2){
                int vm_from = random.nextInt(num);
                int vm_to = random.nextInt(num);
                TaskStage task_step1 = new TaskStage(NetworkConstants.WAIT_SEND, fileSize, length, 0, ram, vm_from, vm_to);
                TaskStage task_step2 = new TaskStage(NetworkConstants.WAIT_RECV, fileSize, length, 1, ram, vm_to, vm_from);
                TaskStage task_step3 = new TaskStage(NetworkConstants.EXECUTION, fileSize, length, 2, ram, vm_to, vm_to);
                NetworkCloudlet cloudlet_from = new NetworkCloudlet(id, length, pesNumber, fileSize, outputSize, ram, utilizationModel, utilizationModel, utilizationModel);
                id++;
                NetworkCloudlet cloudlet_to = new NetworkCloudlet(id, length, pesNumber, fileSize, outputSize, ram, utilizationModel, utilizationModel, utilizationModel);
                cloudlet_from.stages.add(task_step1);
                cloudlet_to.stages.add(task_step2);
                cloudlet_to.stages.add(task_step3);
                cloudlet_from.setUserId(broker.getId());
                cloudlet_from.setVmId(vm_from);
                cloudlet_to.setUserId(broker.getId());
                cloudlet_to.setVmId(vm_to);

                //add the cloudlet to the list
                networkCloudlets.add(cloudlet_from);
                networkCloudlets.add(cloudlet_to);
            }
            /*TaskStage task1 = new TaskStage(NetworkConstants.WAIT_SEND, fileSize, length, 0, ram, 0, 1);
            TaskStage task2 = new TaskStage(NetworkConstants.WAIT_RECV, fileSize, length, 1, ram, 1, 1);
            TaskStage task3 = new TaskStage(NetworkConstants.EXECUTION, fileSize, length, 2, ram, 1, 1);
            NetworkCloudlet cloudlet1 = new NetworkCloudlet(id, length, pesNumber, fileSize, outputSize, ram, utilizationModel, utilizationModel, utilizationModel);
            id++;
            NetworkCloudlet cloudlet2 = new NetworkCloudlet(id, length, pesNumber, fileSize, outputSize, ram, utilizationModel, utilizationModel, utilizationModel);
            cloudlet1.stages.add(task1);
            cloudlet2.stages.add(task2);
            cloudlet2.stages.add(task3);
            cloudlet1.setUserId(broker.getId());
            cloudlet2.setUserId(broker.getId());

            //add the cloudlet to the list
            networkCloudlets.add(cloudlet1);
            networkCloudlets.add(cloudlet2);*/

            //submit cloudlet list to the broker
            broker.submitCloudletList(networkCloudlets);

            //for(int i = 0; i < num; i++){
            //    AppCloudlet appCloudlet = new AppCloudlet(0, i, 9, i, broker.getId());
            //}
            //broker.setAppCloudletList(appCloudlets);

            //maps CloudSim entities to BRITE entities
            NetworkTopology.addLink(networkDatacenter.getId(), broker.getId(),1000.0,10);
            broker.setLinkDC(networkDatacenter);

            // Seventh step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();


            java.io.File logfile = new java.io.File("C:\\Users\\khali\\Documents\\Master Thesis\\cloudsim-6.0-pre\\modules\\cloudsim-examples\\src\\main\\resources\\datasets\\logs\\network-logfile.txt");
            try {
                logfile.createNewFile();
            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
            String msg = "";

            /*for(PowerDatacenter datacenter : datacenters){
                for(Host host : datacenter.getHostList()){
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
            }*/

            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine("NetworkExample4 finished!");

            //datacenterState(networkDatacenter);

            /*for(RootSwitch sw : rootSwitches){

            }
            for(AggregateSwitch sw : aggregateSwitches){

            }
            for(EdgeSwitch sw : edgeSwitches){
                if(sw.pktlist.size() > 0){
                    Log.printLine("test");
                }
            }*/
            HashMap<String, Integer> map = new HashMap<>();
            for(NetworkHost host : networkHosts){

                //Log.printLine("test");
                for (NetworkPacket packet : host.packetTosendGlobal){
                    Log.printLine("test");
                }
            }
            Log.printLine("Done");
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }

    }


    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet value : list) {
            cloudlet = value;
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                DecimalFormat dft = new DecimalFormat("###.##");
                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }

    public static void datacenterState(NetworkDatacenter datacenter){
        int i = 0;
        Log.printLine(datacenter.getHostList().get(i).getNumberOfPes());
        Log.printLine(datacenter.getHostList().get(i).getNumberOfFreePes());
        Log.printLine(datacenter.getHostList().get(i).getRam());
        Log.printLine(datacenter.getHostList().get(i).getBw());
    }
}
