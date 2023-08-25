package cluster.management;

import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.*;

public class ServiceRegistry implements Watcher {
    private static final String REGISTRY_ZNODE = "/service_registry";
    private static final String ZNODE_PREFIX = "/guide-n_";
    private final ZookeeperClient zooKeeperClient;
    private String currentZnode = null;

    public ServiceRegistry(ZookeeperClient zooKeeperClient) {
        this.zooKeeperClient = zooKeeperClient;
        createServiceRegistryPZnode();
    }

    // -------- TODO -------
    public void registerToCluster(int port) throws KeeperException, InterruptedException {

        // Register as a worker in /service_registry znode by adding IP address and Port
        // number
        try {
            String workerZNode = REGISTRY_ZNODE + ZNODE_PREFIX;
            String address = "http://host.docker.internal:" + port;
            currentZnode = zooKeeperClient.createEphemeralSequentialNode(workerZNode, address.getBytes());
            System.out.println("Registered to service registry");
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return;
    }
    // --------END TODO ------

    // -------- TODO -------
    public void registerForUpdates() {
        // Get and print the list of all the workers
        try {
            List<String> workerNodes = zooKeeperClient.getSortedChildren(REGISTRY_ZNODE);
            List<String> workAddressList = new ArrayList<>();

            for (String workerNode : workerNodes) {
                String workerFullPath = REGISTRY_ZNODE + "/" + workerNode;
                byte[] addressBytes = zooKeeperClient.getZookeeper().getData(workerFullPath, false, null);
                String workerAddress = new String(addressBytes);
                workAddressList.add(workerAddress);
            }

            System.out.println("The clusters addresses are: " + workAddressList);
            // Set a new watch
            zooKeeperClient.getZookeeper().getChildren(REGISTRY_ZNODE, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // --------END TODO ------

    // -------- TODO -------
    private void createServiceRegistryPZnode() {
        // Create a persistant znode /service_registry in zookeeper if it doesn't exist
        try {
            if (zooKeeperClient.getZookeeper().exists(REGISTRY_ZNODE, false) == null){
                zooKeeperClient.createPersistantNode(REGISTRY_ZNODE, new byte[] {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // --------END TODO ------

    // -------- TODO -------
    public void unregisterFromCluster() {
        // Unregister znode from the cluster
        try {
            if (currentZnode != null && zooKeeperClient.getZookeeper().exists(currentZnode, false) != null) {
                zooKeeperClient.getZookeeper().delete(currentZnode, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // --------END TODO ------

    // -------- TODO -------
    @Override
    public void process(WatchedEvent event) {
        // In the case of node addition or deletion retrieve the updated list of workers
        switch (event.getType()) {
            case NodeChildrenChanged:
                try {
                    registerForUpdates();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
    // --------END TODO ------
}
