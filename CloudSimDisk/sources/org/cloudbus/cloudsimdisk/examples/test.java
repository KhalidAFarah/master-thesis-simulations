package org.cloudbus.cloudsimdisk.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsimdisk.MyCloudlet;
import org.cloudbus.cloudsimdisk.MyDatacenter;
import org.cloudbus.cloudsimdisk.MyPowerDatacenterBroker;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHddHGSTUltrastarHUC109090CSS600;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHddSeagateEnterpriseST6000VN0001;
import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHddToshibaEnterpriseMG04SCA500E;
import org.cloudbus.cloudsimdisk.power.MyPowerDatacenter;
import org.cloudbus.cloudsimdisk.power.MyPowerHarddriveStorage;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModeHddHGSTUltrastarHUC109090CSS600;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModeHddSeagateEnterpriseST6000VN0001;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModeHddToshibaEnterpriseMG04SCA500E;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;
import org.cloudbus.cloudsimdisk.util.WriteToLogFile;
import org.cloudbus.cloudsimdisk.util.WriteToResultFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class test {
    public static MyPowerDatacenter createDatacenter(String datacenterName, int idShift, int HGSTDisks, int seagateDisks, int toshibaDisks, List<PowerHost> hostList, LinkedList<MyPowerHarddriveStorage> storageList) throws Exception {
        if(hostList == null)
            hostList = new ArrayList<PowerHost>();

        // helper.createPeList(1);
        List<Pe> peList = new ArrayList<Pe>();
        int PesNumber = 1*4;
        for (int i = 1; i <= PesNumber; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(MyConstants.HOST_MIPS)));
        }

        // //helper.createHostList(1);
        int hostsNumber = 1*400;//*50000;
        int hostRAM = 2048*4;
        long hostBW = 10000;
        long hostStorage = 1000000;
        PowerModel hostPowerModel = new PowerModelSpecPowerHpProLiantMl110G4Xeon3040();
        for (int i = 1; i <= hostsNumber; i++) {
            hostList.add(new PowerHost(i, new RamProvisionerSimple(hostRAM), new BwProvisionerSimple(hostBW),
                    hostStorage, peList, new VmSchedulerTimeSharedOverSubscription(peList), hostPowerModel));
        }

        // helper.createPersistentStorage(NumberOfDisk, hddModel, hddPowerModel);
        if (storageList == null)
            storageList = new LinkedList<MyPowerHarddriveStorage>();

        // http://www.storagereview.com/hgst_ultrastar_c10k900_review
        for (int i = 0; i < HGSTDisks; i++) {
            StorageModelHdd hddModel = new StorageModelHddHGSTUltrastarHUC109090CSS600();
            PowerModelHdd hddPowerModel = new PowerModeHddHGSTUltrastarHUC109090CSS600();
            storageList.add(new MyPowerHarddriveStorage(i + idShift, "HGST Ultrastar " + (i + idShift), hddModel, hddPowerModel));
        }

        // http://www.storagereview.com/seagate_enterprise_nas_hdd_review
        for (int i = 0; i < seagateDisks; i++) {
            StorageModelHdd hddModel = new StorageModelHddSeagateEnterpriseST6000VN0001();
            PowerModelHdd hddPowerModel = new PowerModeHddSeagateEnterpriseST6000VN0001();
            storageList.add(new MyPowerHarddriveStorage(i + idShift + HGSTDisks, "Seagate Enterprise " + (i + idShift + HGSTDisks), hddModel, hddPowerModel));
        }

        // http://www.storagereview.com/toshiba_mg04sca_enterprise_hdd_review
        for (int i = 0; i < toshibaDisks; i++) {
            StorageModelHdd hddModel = new StorageModelHddToshibaEnterpriseMG04SCA500E();
            PowerModelHdd hddPowerModel = new PowerModeHddToshibaEnterpriseMG04SCA500E();
            storageList.add(new MyPowerHarddriveStorage(i + idShift + HGSTDisks + seagateDisks, "Toshiba MG04SCA " + (i + idShift + HGSTDisks + seagateDisks), hddModel, hddPowerModel));
        }

        //helper.createDatacenterCharacteristics();
        //String datacenterName = "datacenter";
        String datacenterArchitecture = "x86";
        String datacenterOS = "Linux";
        String datacenterVMM = "Xen";
        double datacenterTimeZone = 10.0;
        double datacenterCostPerSec = 3.0;
        double datacenterCostPerMem = 0.05;
        double datacenterCostPerStorage = 0.001;
        double datacenterCostPerBW = 0.0;
        double datacenterSchedulingInterval = 100;
        DatacenterCharacteristics datacenterCharacteristics = new DatacenterCharacteristics(datacenterArchitecture,
                datacenterOS, datacenterVMM, hostList, datacenterTimeZone,
                datacenterCostPerSec, datacenterCostPerMem,
                datacenterCostPerStorage, datacenterCostPerBW);

        MyPowerDatacenter datacenter = new MyPowerDatacenter(datacenterName, datacenterCharacteristics, new VmAllocationPolicySimple(hostList), storageList, datacenterSchedulingInterval);

        return datacenter;
    }

    public static void main(String[] args){
        //my own
        ArrayList<String> addedfiles = new ArrayList<>();


        // Variables
        // The cloudlet list
        List<MyCloudlet> cloudletList = new ArrayList<MyCloudlet>();
        // The cloudlet required FileNames list.
        List<String> requiredFiles = new ArrayList<String>();
        // The cloudlet data Files list.
        List<File> dataFiles = new ArrayList<File>();
        // The power-VM List.
        List<PowerVm> vmlist	= new ArrayList<PowerVm>();


        String nameOfTheSimulation = "Energy efficiency simulation"; // name of the simulation
        String requestArrivalRateType = "Financial OLTP Applicatoin I/O traces"; // type of the workload
        String requestArrivalTimesSource = "Laboratory for Advanced Software System"; // https://traces.cs.umass.edu/index.php/storage/storage/
        Log.printLine("Starting simulation \"" + nameOfTheSimulation + "\"\n");
        WriteToLogFile.AddtoFile("Starting simulation \"" + nameOfTheSimulation + "\"\n");
        WriteToResultFile.init();

        // helper.initCloudSim();
        double	endTimeSimulation	= 0.0;
        int SIMULATION_LIMIT = 24*60*60;
        Calendar calendar = Calendar.getInstance();
        CloudSim.init(1, calendar, false);



        // The broker.
        MyPowerDatacenterBroker broker;
        // helper.createBroker(type, RequestArrivalDistri)
        try {
            broker = new MyPowerDatacenterBroker("Broker", requestArrivalRateType, requestArrivalTimesSource);
        } catch (Exception e) {
            e.printStackTrace();
            broker = null;
        }

        // The datacenter.
        //helper.createDatacenter();
        List<MyPowerDatacenter> datacenters = new ArrayList<>();
        int[] datacenter_hosts = {0,0,0,0};
        int[] datacenter_disks = {0,0,0,0};
        MyPowerDatacenter datacenter1;
        MyPowerDatacenter datacenter2;
        MyPowerDatacenter datacenter3;
        MyPowerDatacenter datacenter4;
        try {
            datacenter1 = createDatacenter("Datacenter_1", 0, 5, 5, 5, null, null);
            datacenters.add(datacenter1);
            datacenter_hosts[0] = 15;
        } catch (Exception e) {
            datacenter1 = null;
            System.err.println(e.getMessage());
        }
        try {
            datacenter2 = createDatacenter("Datacenter_2", 0, 5, 5, 5, null, null);
            datacenters.add(datacenter2);
            datacenter_hosts[1] = 15;
        } catch (Exception e) {
            datacenter2 = null;
            System.err.println(e.getMessage());
        }
        try {
            datacenter3 = createDatacenter("Datacenter_3", 0, 5, 5, 5, null, null);
            datacenters.add(datacenter3);
            datacenter_hosts[2] = 15;
        } catch (Exception e) {
            datacenter3 = null;
            System.err.println(e.getMessage());
        }
        try {
            datacenter4 = createDatacenter("Datacenter_4", 0, 5, 5, 5, null, null);
            datacenters.add(datacenter4);
            datacenter_hosts[3] = 15;
        } catch (Exception e) {
            datacenter4 = null;
            System.err.println(e.getMessage());
        }






        // helper.createVmList(1);
        int vmsNumber = 1;
        double vmMips = 50;
        int vmPesNumber = 1;
        int vmRAM = 512;
        long vmBW = 1000;
        long vmSize = 10000;
        int vmPriority = 1;
        String vmVMM = "Xen";
        CloudletScheduler vmCloudletScheduler = new CloudletSchedulerTimeShared();
        double vmSchedulingInterval = 1;







        // Files
        //helper.addFiles(startingFilesList);
        //helper.createRequiredFilesList(requiredFiles);
        //helper.createDataFilesList(dataFiles);
        int amount = 1000;//1000;
        try {
            // instantiate a reader
            BufferedReader input = new BufferedReader(new FileReader("files/datasets/Financial1.spc"));

            // read line by line
            String line;
            int i = 1;

            while ((line = input.readLine()) != null && i < amount) {

                // retrieve fileName and fileSize
                String[] lineSplited = line.split(",");
                String ApplicationSpecificUnit = lineSplited[0];
                String logicalBlockAddress = lineSplited[1];
                int size = Integer.parseInt(lineSplited[2]);
                String operationCode = lineSplited[3].toLowerCase();
                String timestamp = lineSplited[4];

                // create cloudlet
                long cloudletLength = 1;
                int cloudletPesNumber = 1;
                long cloudletFileSize = 300;
                long cloudletOutputSize = 300;
                UtilizationModel cloudletUtilizationModelCPU = new UtilizationModelFull();
                UtilizationModel cloudletUtilizationModelRAM = new UtilizationModelFull();
                UtilizationModel cloudletUtilizationModelBW = new UtilizationModelFull();
                PowerVm vm = new PowerVm(i, broker.getId(), vmMips, cloudletPesNumber, cloudletPesNumber*vmRAM, vmBW, vmSize, vmPriority,
                        vmVMM, vmCloudletScheduler, vmSchedulingInterval);
                vmlist.add(vm);

                if (operationCode.equals("r")){
                    boolean exists = false;
                    File file = new File(logicalBlockAddress, size);
                    for (MyPowerDatacenter datacenter : datacenters){
                        for (Storage storage : datacenter.getStorageList()){
                            for(String name : storage.getFileNameList()){
                                // Adding the read file if it is not already stored
                                if(!name.equals(logicalBlockAddress)){
                                    exists = true;
                                }
                            }
                        }
                    }

                    if(!exists){
                        for(MyPowerDatacenter datacenter : datacenters){
                            datacenter.addFile(file);
                        }
                        addedfiles.add(file.getName());
                    }

                    ArrayList<String>  tempRequiredFiles = new ArrayList<>();
                    tempRequiredFiles.add(logicalBlockAddress);

                    MyCloudlet cloudlet = new MyCloudlet(i, cloudletLength, cloudletPesNumber,
                            cloudletFileSize, cloudletOutputSize,
                            cloudletUtilizationModelCPU, cloudletUtilizationModelRAM,
                            cloudletUtilizationModelBW, tempRequiredFiles, null);
                    cloudlet.setUserId(broker.getId());
                    cloudlet.setVmId(vm.getId());
                    cloudletList.add(cloudlet);
                }else if(operationCode.equals("w")){


                    ArrayList<File> tempDataFiles = new ArrayList<>();
                    ArrayList<String>  tempRequiredFiles = new ArrayList<>();
                    File file = new File(logicalBlockAddress, size);

                    int exist = 0;
                    for(String filename : addedfiles){
                        // If the file exist we must update it as adding it will lead to the job failing
                        // either by removing it and adding it, skipping the updating of a file or finding a way for the simulation to update it.

                        // What i've decided is rather that updating a file it will write a new file

                        if (filename.equals(logicalBlockAddress)) {
                            exist++;
                        } else if (filename.endsWith("-"+logicalBlockAddress)) {
                            exist++;
                        }
                    }

                    if(exist > 0){
                        file.setName(exist + "-" + file.getName());

                    }
                    tempDataFiles.add(file);
                    addedfiles.add(file.getName());

                    MyCloudlet cloudlet = new MyCloudlet(i, cloudletLength, cloudletPesNumber,
                            cloudletFileSize, cloudletOutputSize,
                            cloudletUtilizationModelCPU, cloudletUtilizationModelRAM,
                            cloudletUtilizationModelBW, tempRequiredFiles, tempDataFiles);
                    cloudlet.setUserId(broker.getId());
                    cloudlet.setVmId(vm.getId());
                    cloudletList.add(cloudlet);
                }

                i++;
            }


            // close the reader
            input.close();

        } catch (IOException | NumberFormatException | ParameterException e) {
            System.err.println(e.getMessage());
        }

        // submit the list to the broker
        broker.submitVmList(vmlist.subList(0, amount-1));
        broker.submitCloudletList(cloudletList.subList(0, amount-1));

        // Logs
        //helper.printPersistenStorageDetails();
        for(MyPowerDatacenter datacenter : datacenters){
            List<MyPowerHarddriveStorage> tempList = datacenter.getStorageList();
            String msg = "";

            for (int i = 0; i < tempList.size(); i++) {
                msg += String
                        .format("OBSERVATION>> Initial persistent storage \n%d/%d %s\n\t%-16s-> %10.0f MB\n\t%-16s-> %10.0f MB\n\t%-16s-> %10.0f MB\n\t%-16s-> %10.6f s\n\t%-16s-> %10.6f s\n\t%-16s-> %10.3f MB/s\n",
                                (i + 1), tempList.size(), tempList.get(i).getName(), "Capacity", tempList.get(i)
                                        .getCapacity(), "UsedSpace", (tempList.get(i).getCapacity() - tempList.get(i)
                                        .getFreeSpace()), "FreeSpave", tempList.get(i).getFreeSpace(), "Latency", tempList
                                        .get(i).getAvgRotLatency(), "avgSeekTime", tempList.get(i).getAvgSeekTime(),
                                "maxTransferRate", tempList.get(i).getMaxInternalDataTransferRate());
            }

            WriteToLogFile.AddtoFile(msg);
        }


        //CloudSim.terminateSimulation(SIMULATION_LIMIT);
        endTimeSimulation = CloudSim.startSimulation();

        //helper.printResults(endTimeSimulation);
        for(MyPowerDatacenter datacenter : datacenters) {
            double TotalStorageEnergy = datacenter.getTotalStorageEnergy();
            List<MyPowerHarddriveStorage> tempList = datacenter.getStorageList();

            // PRINTOUT -----------------------------------------------------------------------
            Log.printLine();
            Log.printLine("*************************** RESULTS - " + datacenter.getName() + " ***************************");
            Log.printLine();
            Log.printLine("TIME SPENT IN IDLE/ACTIVE MODE FOR EACH STORAGE");
            for (int i = 0; i < tempList.size(); i++) {
                Log.printLine("Storage \"" + tempList.get(i).getName() + "\"");
                for (Double interval : tempList.get(i).getIdleIntervalsHistory()) {
                    Log.formatLine("%8sIdle intervale: %9.3f second(s)", "", interval);
                }
                Log.printLine();
                Log.formatLine("%8sTime in    Idle   mode: %9.3f second(s)", "", endTimeSimulation
                        - tempList.get(i).getInActiveDuration());
                Log.formatLine("%8sTime in   Active  mode: %9.3f second(s)", "", tempList.get(i).getInActiveDuration());
                Log.formatLine("%8sTime of the simulation: %9.3f second(s)", "", endTimeSimulation);
                Log.printLine();
                Log.formatLine("%8sEnergy consumed in  Idle   mode: %9.3f Joule(s)", "", tempList.get(i)
                        .getTotalEnergyIdle());
                Log.formatLine("%8sEnergy consumed in Active  mode: %9.3f Joule(s)", "", tempList.get(i)
                        .getTotalEnergyActive());
                Log.formatLine("%8sEnergy consumed in  total      : %9.3f Joule(s)", "", tempList.get(i)
                        .getTotalEnergyIdle() + tempList.get(i).getTotalEnergyActive());
                Log.printLine();
                Log.formatLine("%8sMaximum Queue size    : %10d operation(s)", "",
                        Collections.max(tempList.get(i).getQueueLengthHistory()));
                Log.printLine();
            }
            Log.printLine();
            Log.formatLine("Energy consumed by Persistent Storage: %.3f Joule(s)", TotalStorageEnergy);
            Log.printLine();


            // -----------------------------------------------------------------------

            // LOGS -----------------------------------------------------------------------
            WriteToLogFile.AddtoFile("\n");
            WriteToLogFile.AddtoFile("*************************** RESULTS ***************************");
            WriteToLogFile.AddtoFile("\n");
            WriteToLogFile.AddtoFile("TIME SPENT IN IDLE/ACTIVE MODE FOR EACH STORAGE");
            for (int i = 0; i < tempList.size(); i++) {
                WriteToLogFile.AddtoFile("Storage \"" + tempList.get(i).getName() + "\"");
                for (Double interval : tempList.get(i).getIdleIntervalsHistory()) {
                    WriteToLogFile.AddtoFile(String.format("%8sIdle intervale: %9.3f second(s)", "", interval));
                }
                WriteToLogFile.AddtoFile("\n");
                WriteToLogFile.AddtoFile(String.format("%8sTime in    Idle   mode: %9.3f second(s)", "", endTimeSimulation
                        - tempList.get(i).getInActiveDuration()));
                WriteToLogFile.AddtoFile(String.format("%8sTime in   Active  mode: %9.3f second(s)", "", tempList.get(i)
                        .getInActiveDuration()));
                WriteToLogFile
                        .AddtoFile(String.format("%8sTime of the simulation: %9.3f second(s)", "", endTimeSimulation));
                WriteToLogFile.AddtoFile("\n");
                WriteToLogFile.AddtoFile(String.format("%8sEnergy consumed in  Idle   mode: %9.3f Joule(s)", "", tempList
                        .get(i).getTotalEnergyIdle()));
                WriteToLogFile.AddtoFile(String.format("%8sEnergy consumed in Active  mode: %9.3f Joule(s)", "", tempList
                        .get(i).getTotalEnergyActive()));
                WriteToLogFile.AddtoFile(String.format("%8sEnergy consumed in  total      : %9.3f Joule(s)", "", tempList
                        .get(i).getTotalEnergyIdle() + tempList.get(i).getTotalEnergyActive()));
                WriteToLogFile.AddtoFile("\n");
                WriteToLogFile.AddtoFile(String.format("%8sMaximum Queue size    : %10d operation(s)", "",
                        Collections.max(tempList.get(i).getQueueLengthHistory())));
                WriteToLogFile.AddtoFile("\n");
            }
            WriteToLogFile.AddtoFile("\n");
            WriteToLogFile.AddtoFile(String.format("Energy consumed by Persistent Storage: %.3f Joule(s)",
                    TotalStorageEnergy));
            WriteToLogFile.AddtoFile("\n");

            /* // queue size WriteToLogFile.AddtoFile("QUEUE SIZE in Operation(s) (not sorted)"); for (int i = 0; i <
             * tempList.size(); i++) { WriteToLogFile.AddtoFile("For Disk" + tempList.get(i).getName()); for (int queue :
             * tempList.get(i).getQueueLengthHistory()) { WriteToLogFile.AddtoFile(String.format("%4d", queue)); } } //
             * ----------------------------------------------------------------------- */

            WriteToResultFile.end();
            Log.printLine("END !");
            // END
        }

        java.io.File logfile = new java.io.File("files/datasets/logs/logfile-storage.csv");
        try {
            logfile.createNewFile();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        String msg2 = "datacenter_id;datacenter_name;datacenter_storage_energy;storage_id;system;storage_capacity;capacity_used;active_duration;idle_intervals;duration;total_energy_active;total_energy_idle;average_rotation_latency;average_seek_Time;max_internal_data_transfer_rate;is_active;idle_power;active_power;files;\n";

        for (MyPowerDatacenter datacenter : datacenters){
            for(Storage storage : datacenter.getStorageList()){
                MyPowerHarddriveStorage powerstorage = (MyPowerHarddriveStorage) storage;

                String fileInfo = "";
                for(String filename : storage.getFileNameList()){
                    File file = storage.getFile(filename);
                    fileInfo += file.getName() + "," + file.getFileAttribute().getFileSize() + "," + file.getTransactionTime() + ":";
                }

                String idleIntervals = "";
                for(double interval : ((MyPowerHarddriveStorage) storage).getIdleIntervalsHistory()){
                    idleIntervals += interval + ":";
                }
                //powerstorage.IdleIntervalsHistory;
                //powerstorage.getQueueLengthHistory();
                msg2 += datacenter.getId() + ";" + datacenter.getName() + ";" + datacenter.getTotalStorageEnergy() + ";" + powerstorage.getId() + ";storage;" + powerstorage.getCapacity() + ";" + powerstorage.getUsedSpace() + ";" + powerstorage.getInActiveDuration() + ";" + idleIntervals + ";" + endTimeSimulation + ";" + powerstorage.getTotalEnergyActive() + ";" + powerstorage.getTotalEnergyIdle() + ";" + powerstorage.getAvgRotLatency() + ";" + powerstorage.getAvgSeekTime()  + ";" + powerstorage.getMaxInternalDataTransferRate() + ";" + powerstorage.isActive() + ";" + powerstorage.getPowerIdle() + ";" + powerstorage.getPowerActive() + ";" + fileInfo + ";\n";
            }
        }



        try {
            FileWriter writer = new FileWriter("files/datasets/logs/logfile-storage.csv");
            writer.write(msg2);
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
