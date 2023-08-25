package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

public class LeaderElection implements Watcher {
    private static final String ELECTION_ZNODE_NAME = "/leader_election";
    private static final String ZNODE_PREFIX = "/guide-n_";
    private String currentZnodeName;
    private final ZookeeperClient zooKeeperClient;
    private final ServiceRegistry serviceRegistry;
    private final int currentServerPort;

    public LeaderElection(ZookeeperClient zooKeeperClient, ServiceRegistry serviceRegistry, int port) {
        this.zooKeeperClient = zooKeeperClient;
        this.serviceRegistry = serviceRegistry;
        this.currentServerPort = port;

        // Invoke this method to create a persistant znode /leader_election in zookeeper
        // if it doesn't exist
        createElectionRegistryPZnode();
    }

    // -------- TODO -------
    public void registerCandidacyForLeaderElection() throws KeeperException, InterruptedException {

        // Create a ephermeral sequential node under election znode and assign it to the
        // string currentZnodeName
        String znodeFullPath = ELECTION_ZNODE_NAME + ZNODE_PREFIX;
        String createdZNode = zooKeeperClient.createEphemeralSequentialNode(znodeFullPath, new byte[] {});
        System.out.println("Znode Name:" + createdZNode);
        this.currentZnodeName = createdZNode.replace(ELECTION_ZNODE_NAME + "/", "");
        participateInLeaderElection();
    }
    // --------END TODO ------

    // -------- TODO -------
    private void participateInLeaderElection() throws KeeperException, InterruptedException {

        // Participate in leader election and determine if the current node is the
        // leader.

        boolean isLeader = zooKeeperClient.isLeaderNode(ELECTION_ZNODE_NAME, currentZnodeName);

        updateServiceRegistry(isLeader);
    }
    // --------END TODO ------

    private void updateServiceRegistry(boolean isLeader) {
        if (isLeader) {
            onElectedToBeLeader();
        } else {
            onWorker();
        }
    }

    // -------- TODO -------
    private void createElectionRegistryPZnode() {
        // Create a persistant znode /leader_election in zookeeper if it doesn't exist
        try {
            Stat electionZnodeStat = zooKeeperClient.getZookeeper().exists(ELECTION_ZNODE_NAME, false);
            if (electionZnodeStat == null) {
                zooKeeperClient.createPersistantNode(ELECTION_ZNODE_NAME, new byte[] {});
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    // --------END TODO ------

    // -------- TODO -------
    public void onElectedToBeLeader() {
        // Display appropriate message on console - "I am the leader"
        // Invoke necessary methods in ServiceRegistry.java to display the list of
        // worker nodes (ServiceRegistry.registerForUpdates() method)

        // ------ FAULT TOLERANCE ------
        // Unregister from the service registry (ServiceRegistry.unregisterFromCluster()
        // method)
        // Watch for any changes(a new worker joining/an existing worker failing) in the
        // cluster through /service_registry znode (ServiceRegistry.registerForUpdates()
        // method)
        try {
            System.out.println("I am the leader");
            //serviceRegistry.registerForUpdates();

            serviceRegistry.unregisterFromCluster();
            serviceRegistry.registerForUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // --------END TODO ------

    // -------- TODO -------
    public void onWorker() {

        // Print an appropriate message on console - "I am a worker".
        // Register as a worker in /service_registry znode
        // (ServiceRegistry.registerToCluster(int port) method)

        // ------ FAULT TOLERANCE ------
        // Watch for any changes to the predecessor node, in which case rerun the leader
        // election
        try {
            System.out.println("I am a worker");
            serviceRegistry.registerToCluster(currentServerPort);
            String predecessorNode = zooKeeperClient.getPredecessorNode(ELECTION_ZNODE_NAME, currentZnodeName);

            if (predecessorNode != null) {
                Stat predecessorStat = zooKeeperClient.getZookeeper().exists(ELECTION_ZNODE_NAME + "/" + predecessorNode, this);             
                
                System.out.println("Watching znode " + predecessorNode);

                if (predecessorStat == null) {
                    participateInLeaderElection();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    // --------END TODO ------

    @Override
    public void process(WatchedEvent event) {
        // -------- TODO ------
        switch (event.getType()) {
            // if a node fails re-run the participateInLeaderElection()
            case NodeDeleted:
                try {
                    participateInLeaderElection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
        // ------- END TODO ------
    }
}
