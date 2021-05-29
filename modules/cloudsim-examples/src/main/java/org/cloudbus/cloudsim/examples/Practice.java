package org.cloudbus.cloudsim.examples;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CuckooCloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.CuckooDatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to create a data center with one host and run
 * one cloudlet on it.
 */
public class Practice {
    /** The cloudlet list. */
    private static List<CuckooCloudlet> cloudletList;
    /** The vmlist. */
    private static List<Vm> vmlist;

    /**
     * Creates main() to run this example.
     *
     * @param args the args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Log.printLine("Starting Practice...");

        try {
            // First step: Initialize the CloudSim package. It should be called before
            // creating any entities.
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current
                                                        // date and time.
            boolean trace_flag = false; // trace events

            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at
            // least one of them to run a CloudSim simulation
            Datacenter datacenter0 = createDatacenter("Main_Datacenter");

            // Third step: Create Broker
            CuckooDatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Fourth step: Create virtual machine
            vmlist = new ArrayList<Vm>();

            // VM description
            int vmid = 0;
            int mips = 10; // Million Instruction per sec
            long size = 2000; // image size (MB)
            int ram = 512; // vm memory (MB)
            long bw = 1000; // (MB)
            int pesNumber = 1; // number of cpus cores
            String vmm = "Xen"; // VMM name

            for (int i = 0; i < 15; i++) {
                // create VM
                Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());

                // add the VM to the vmList
                vmlist.add(vm);
            }
            // submit vm list to the broker
            broker.submitVmList(vmlist);

            // Fifth step: Create Cloudlet List
            cloudletList = new ArrayList<CuckooCloudlet>();

            // Cloudlet -> job properties
            int id = 0;
            long length = 400000; // (MI)
            long fileSize = 300; // (byte)
            long outputSize = 300; // (byte)
            UtilizationModel utilizationModel = new UtilizationModelFull();
            for (int i = 0; i < 20; i++) {
                id = i;
                vmid = i % 15;
                CuckooCloudlet cloudlet = new CuckooCloudlet(id, length, pesNumber, fileSize, outputSize,
                        utilizationModel, utilizationModel, utilizationModel, false, id);
                cloudlet.setUserId(brokerId);
                cloudlet.setVmId(vmid);
                // add the cloudlet to the list
                cloudletList.add(cloudlet);
            }
            // Creating a Cuckoo Job
            CuckooCloudlet cloudlet = new CuckooCloudlet(30, length, pesNumber, fileSize, outputSize, utilizationModel,
                    utilizationModel, utilizationModel, true, 30);
            cloudlet.setUserId(brokerId);
            cloudlet.setVmId(-1);
            cloudletList.add(cloudlet);
            fileSize = 450;
            cloudlet = new CuckooCloudlet(31, length, pesNumber, fileSize, outputSize, utilizationModel,
                    utilizationModel, utilizationModel, true, 31);
            cloudlet.setUserId(brokerId);
            cloudlet.setVmId(-1);
            // add the cloudlet to the list
            cloudletList.add(cloudlet);

            // submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            // Final step: Print results when simulation is over
            List<CuckooCloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);

            Log.printLine("Practice finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * Creates the datacenter.
     *
     * @param name the name
     *
     * @return the datacenter
     */
    private static Datacenter createDatacenter(String name) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        // our machine
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 1000;

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(1, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        // 4. Create Host with its id and list of PEs and add them to the list
        // of machines
        int hostId = 0;
        int ram = 8192; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;
        for (int i = 0; i < 4; i++) {
            hostId = i;
            hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
                    new VmSchedulerTimeShared(peList))); // This is our machine
        }
        // 5. Create a DatacenterCharacteristics object that stores the
        // properties of a data center: architecture, OS, list of
        // Machines, allocation policy: time- or space-shared, time zone
        // and its price (G$/Pe time unit).
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
                                       // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
                cost, costPerMem, costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    // We strongly encourage users to develop their own broker policies, to
    // submit vms and cloudlets according
    // to the specific rules of the simulated scenario
    /**
     * Creates the broker.
     *
     * @return the datacenter broker
     */
    private static CuckooDatacenterBroker createBroker() {
        CuckooDatacenterBroker broker = null;
        try {
            broker = new CuckooDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects.
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<CuckooCloudlet> list) {
        int size = list.size();
        CuckooCloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time"
                + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);
            if (cloudlet.isCuckoo() && cloudlet.getStatus() == CuckooCloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
                        + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(cloudlet.getFinishTime()) + indent + indent + "CUCKOO SUB JOB " + cloudlet.getJobNumber());
            } else if (cloudlet.getStatus() == CuckooCloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
                        + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}