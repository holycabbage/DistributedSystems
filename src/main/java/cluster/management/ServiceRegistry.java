package cluster.management;

import java.util.List;

import org.apache.zookeeper.*;

public class ServiceRegistry implements Watcher {
    private static final String REGISTRY_ZNODE = "/service_registry";
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
            String workerZNode = REGISTRY_ZNODE + "/worker" + port;
            currentZnode = zooKeeperClient.createEphemeralSequentialNode(workerZNode, new byte[] {});
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
            System.out.println("List of workers: " + workerNodes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // --------END TODO ------

    // -------- TODO -------
    private void createServiceRegistryPZnode() {
        // Create a persistant znode /service_registry in zookeeper if it doesn't exist
        try {
            zooKeeperClient.createPersistantNode(REGISTRY_ZNODE, new byte[] {});
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
