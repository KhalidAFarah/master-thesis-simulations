import math
import pandas as pd
import numpy as np

class Datacenter:
    def __init__(self, id, name, hosts, switches):
        self.id = id
        self.name = name
        self.hosts = hosts
        self.switches = switches
    
    def __dict__(self):
        host_dicts = []
        switch_dicts = []
        for host in self.hosts:
            host_dicts.append(host.__dict__())
        for switch in self.switches:
            switch_dicts.append(switch.__dict__())
        
        return {
            "datacenter_id": self.id,
            "datacenter_name": self.name,
            "hosts": host_dicts,
            "switches": switch_dicts,
        }

# bandwidth (Mbit)
class Host:
    def __init__(self, id, datacenter_name, datacenter_id, bandwidth):
        self.id = id
        self.datacenter_name = datacenter_name
        self.datacenter_id = datacenter_id
        self.port = None
        self.bandwidth = bandwidth
        self.available_bandwidth = bandwidth
        self.total_used_bandwidth = 0
        self.traffic_history = []
        self.active_duration = 0
        self.is_active = True
    
    def is_bandwidth_available(self, bandwidth):
        if (self.available_bandwidth - bandwidth) < 0:
            return False
        return True
    
    def send_traffic(self, bandwidth):
        if self.is_active == False:
            self.is_active = True
        
        self.total_used_bandwidth += bandwidth
        self.traffic_history.append(bandwidth)

        self.active_duration += (bandwidth / self.bandwidth)
        return self.port.send_traffic(bandwidth)
    # def send_traffic(self, bandwidth):
    #     if self.is_bandwidth_available(bandwidth):
    #         # print("Host has enough bandwidth")
    #         self.available_bandwidth -= bandwidth
    #         return self.port.send_traffic(bandwidth)
    #     print("Host does not have enough bandwidth")
    #     return False
    
    def __dict__(self):
        return {
            "host_id": self.id,
            "bandwidth": self.bandwidth,
            "available_bandwidth": self.available_bandwidth,
            "port": self.port.__dict__(),
            "datacenter_id": self.datacenter_id,
            "datacenter_name": self.datacenter_name
        }
    

# bandwidth (Mbit)
# https://www.cisco.com/c/en/us/products/collateral/switches/catalyst-2960-series-switches/product_data_sheet0900aecd806b0bd8.html
# Cisco Catalyst 2960S-24TS-S
class Switch:
    def __init__(self, id, datacenter_name, datacenter_id, ports, bandwidth, power_per_port, low_power, high_power, level ,core_switch = False, distribution_switch = False, access_switch = False):
        self.id = id
        self.datacenter_name = datacenter_name
        self.datacenter_id = datacenter_id
        self.ports = []
        for _ in range(ports):
            self.ports.append(None)
        self.bandwidth = bandwidth * len(self.ports)
        self.available_bandwidth = bandwidth * len(self.ports)
        self.total_used_bandwidth = 0
        self.traffic_history = []
        
        self.power_per_port = power_per_port
        self.low_power = low_power# %5 throughput
        self.high_power = high_power# %100 throughput
        
        self.is_active = True
        self.active_duration = 0
        
        self.level = level
        self.core_switch = core_switch
        self.distribution_switch = distribution_switch
        self.access_switch = access_switch
    
    def is_bandwidth_available(self, bandwidth, port_id):
        return self.ports[port_id].is_bandwidth_available(bandwidth)
    
    def send_traffic(self, bandwidth, port_id):
        if self.is_active == False:
            self.is_active = True
        if port_id > len(self.ports):
            return False

        self.total_used_bandwidth += bandwidth
        self.traffic_history.append(bandwidth)
        self.active_duration += (bandwidth / self.bandwidth)
        
        if self.core_switch == False:
            return self.ports[port_id].send_traffic(bandwidth)
        return True
    # def send_traffic(self, bandwidth, port_id):
    #     if self.is_bandwidth_available(bandwidth, port_id):
    #         # print("Switch's port has enough bandwidth")
    #         self.available_bandwidth -= bandwidth
    #         return self.ports[port_id].send_traffic(bandwidth)
    #     print("Switch's port does not enough bandwidth")
    #     return False
    
    def __dict__(self):
        port_dicts = []
        for port in self.ports:
            port_dicts.append(port.__dict__())
        
        return {
            "switch_id": self.id,
            "core_switch": self.core_switch,
            "distribution_switch": self.distribution_switch,
            "access_switch": self.access_switch,
            "port": port_dicts,
            "bandwidth": self.bandwidth,
            "available_bandwidth": self.available_bandwidth,
            "datacenter_id": self.datacenter_id,
            "datacenter_name": self.datacenter_name
        }

# bandwidth (Mbit)
class Link:
    def __init__(self, id, datacenter_name, datacenter_id, host, switch, bandwidth):
        self.id = id
        self.datacenter_name = datacenter_name
        self.datacenter_id = datacenter_id
        self.host = host
        self.switch = switch
        self.bandwidth = bandwidth
        self.available_bandwidth = bandwidth
        self.total_used_bandwidth = 0
        self.traffic_history = []
        
        self.is_active = True
        self.active_duration = 0
        
    def is_bandwidth_available(self, bandwidth):
        if (self.available_bandwidth - bandwidth) < 0:
            return False
        return True
    
    def send_traffic(self, bandwidth):
        if self.is_active == False:
            self.is_active = True
        
        self.total_used_bandwidth += bandwidth
        self.traffic_history.append(bandwidth)
        self.active_duration += (bandwidth / self.bandwidth)
        return self.switch.send_traffic(bandwidth, 0)
        
    # def send_traffic(self, bandwidth):
    #     if self.is_bandwidth_available(bandwidth):
    #         # print("Link has enough bandwidth")
    #         self.available_bandwidth -= bandwidth
    #         return self.switch.send_traffic(bandwidth, 0)
    #     print("Link does not have enough bandwidth")
    #     return False
    
    def __dict__(self):
        return {
            "link_id": self.id,
            "host_id": self.host.id,
            "switch_id": self.switch.id,
            "bandwidth": self.bandwidth,
            "available_bandwidth": self.available_bandwidth,
            "datacenter_id": self.datacenter_id,
            "datacenter_name": self.datacenter_name,
        }

def create_datacenter(datacenter_id, datacenter_name, hosts):
    host_nodes = []
    switch_nodes = []
    # C2960-48TT-S
    sw = Switch(len(switch_nodes), datacenter_name, datacenter_id, 5, 1000, 15.4, 70, 71, 0,  access_switch=True)
    available_port = len(sw.ports) - 1
    links = 0
    
    for host in range(hosts):
        pm = Host(host, datacenter_name, datacenter_id, 1000)
        
        if available_port == 0:
            switch_nodes.append(sw)
            sw = Switch(len(switch_nodes), datacenter_name, datacenter_id, 5, 1000, 15.4, 70, 71, 0, access_switch=True)
            available_port = len(sw.ports) - 1
        
        link = Link(links, datacenter_name, datacenter_id, pm, sw, 1000)
        sw.ports[len(sw.ports) - available_port] = link
        pm.port = link
        
        host_nodes.append(pm)
        available_port -= 1
        links += 1
    
    switch_nodes.append(sw)
    level = 1
    sw = Switch(len(switch_nodes), datacenter_name, datacenter_id, 5, 1000, 15.4, 70, 71, level)
    available_ports = len(sw.ports) - 1
    
    prev_level_switch_nodes = []
    level_switch_nodes = switch_nodes
    next_level_switch_nodes = []
    prev_devices_per_level = hosts
    devices_per_level = len(switch_nodes)
    print("Level", level-1, "has", hosts, "Devices.")
    if len(switch_nodes) - len(sw.ports) <= 0:
        print("Root switch will be on level", level, "and will be connected to", devices_per_level, "devices.")
        sw.core_switch = True
        for index, switch in enumerate(level_switch_nodes):
            link = Link(links, datacenter_name, datacenter_id, switch, sw, 1000)
            switch.ports[0] = link
            sw.ports[index] = link
        switch_nodes.append(sw)
    else:
        sw.distribution_switch = True
        # next_level_switch_nodes.append(sw)
        while devices_per_level >= len(sw.ports):
            for switch in level_switch_nodes:
                if available_ports == 0:
                    next_level_switch_nodes.append(sw)
                    switch_nodes.append(sw)
                    sw = Switch(len(switch_nodes), datacenter_name, datacenter_id, 5, 1000, 15.4, 70, 71, level, distribution_switch=True)
                    available_ports = len(sw.ports)
                
                link = Link(links, datacenter_name, datacenter_id, switch, sw, 1000)
                switch.ports[0] = link
                sw.ports[len(sw.ports) - available_ports] = link
                
                links += 1
                available_ports -= 1
            next_level_switch_nodes.append(sw)
            prev_level_switch_nodes = level_switch_nodes
            level_switch_nodes = next_level_switch_nodes
            next_level_switch_nodes = []
            print("Level", level, "has", devices_per_level, "switches.")
            
            tmp = devices_per_level
            devices_per_level = math.ceil(tmp / (len(sw.ports)-1))
            prev_devices_per_level = tmp
            print("devices per level is now", devices_per_level)
            
            level += 1
        switch_nodes.append(sw)
        
        print("Root switch will be on level", level, "and will be connected to", devices_per_level, "switches.")
        sw = Switch(len(switch_nodes), datacenter_name, datacenter_id, 5, 1000, 15.4, 70, 71, level, core_switch=True)
        devices_per_level = math.ceil((prev_devices_per_level - len(switch_nodes)) / len(sw.ports))
        for index, switch in enumerate(level_switch_nodes):
            link = Link(links, datacenter_name, datacenter_id, switch, sw, 1000)
            switch.ports[0] = link
            sw.ports[index] = link
        switch_nodes.append(sw)
    datacenter = Datacenter(datacenter_id, datacenter_name, host_nodes, switch_nodes)
    return datacenter

def log_results(datacenters, duration):
    data = "datacenter_id;datacenter_name;id;type;role;level;bandwidth;total_used_bandwidth;traffic_history;ports;duration;active_duration;power_per_port;low_power;high_power;is_active;\n"
    for datacenter in datacenters:
        for switch in datacenter.switches:
            ports = ""
            for port in switch.ports:
                if port is not None:
                    traffic_history_port = ""
                    for bandwidth in port.traffic_history:
                        traffic_history_port += str(bandwidth) + "-"
                    ports += "" + str(port.id) + "," + str(port.is_active) + "," + str(port.active_duration) + "," + str(port.switch.id) + "," + str(port.host.id) + "," + str(port.bandwidth) + "," + str(port.total_used_bandwidth) + "," + traffic_history_port + ":"
                else:
                    ports += ":"
            
            traffic_history = ""
            for bandwidth in switch.traffic_history:
                traffic_history += str(bandwidth) + ":"
            
            role = ""
            if switch.core_switch:
                role = "core switch"
            elif switch.distribution_switch:
                role = "distribution switch"
            elif switch.access_switch:
                role = "access switch"
            data += str(datacenter.id) + ";" + datacenter.name + ";" + str(switch.id) + ";switch;" + role + ";" + str(switch.level) + ";" + str(switch.bandwidth) + ";" + str(switch.total_used_bandwidth) + ";" + traffic_history + ";" + ports + ";" + str(duration) + ";" + str(switch.active_duration) + ";" + str(switch.power_per_port) + ";" + str(switch.low_power) + ";" + str(switch.high_power) + ";" + str(switch.is_active) + ";\n"
    return data

# Get data
dataset = pd.read_csv("./logfile-v5.csv", delimiter=";", dtype={"time":float, "datacenter_name":"string", "host_id":int, "type":"string", "active":bool, "number_of_pes":int, "available_pes":int, "mips":int, "available_mips":float, "utilization_per_pe":"string", "ram":int, "available_ram":float, "bw":int, "available_bw":float, "power_model":"string", "vms":"string", "Unnamed":"string"})
datacenters = []
datacenter_names = np.unique(dataset['datacenter_name'])
for index_datacenter, datacenter_name in enumerate(datacenter_names):
    datacenter_hosts = 0
    for index_host, sample in dataset[(dataset['datacenter_name'] == datacenter_name) & (dataset['time'] == 300.01)].iterrows():
        datacenter_hosts += 1
    datacenter = create_datacenter(index_datacenter, datacenter_name, datacenter_hosts)
    datacenters.append(datacenter)
    print("Number of switches in the network", len(datacenter.switches), f"of data center '{datacenter_name}'")

# Send traffic for each host
for index_datacenter, datacenter_name in enumerate(datacenter_names):
    index_host = 0
    for _, sample in dataset[(dataset['datacenter_name'] == datacenter_name) & (dataset['time'] == 300.01)].iterrows():
        result = datacenters[index_datacenter].hosts[index_host].send_traffic(sample['bw'] - sample['available_bw'])
        if result == False:
            print(f"Host '{index_host}' at data center '{datacenter_name}' failed to send data.")
        index_host += 1

# Simulation duration
duration = 0
for datacenter in datacenters:
    for switch in datacenter.switches:
        temp_duration = switch.total_used_bandwidth / (switch.bandwidth / len(switch.ports))
        if temp_duration > duration:
            duration = temp_duration 

# Log results
with open("./logfile-network.csv", "w") as file:
    file.write(log_results(datacenters, duration))

# # Example case...
# datacenters = []
# datacenter = create_datacenter(0, "datacenter", 25)
# datacenters.append(datacenter)
# for host in datacenter.hosts:
#     result = host.send_traffic(500)
#     if result == False:
#         print("Fail")

# duration = 0
# for datacenter in datacenters:
#     for switch in datacenter.switches:
#         temp_duration = switch.total_used_bandwidth / (switch.bandwidth / len(switch.ports))
#         if temp_duration > duration:
#             duration = temp_duration 
        

# with open("./logfile-network.csv", "w") as file:
#     file.write(log_results(datacenters, duration))