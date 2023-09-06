import org.apache.zookeeper.*;

public class Client {
    private static ZooKeeper zooKeeper;

    public static void main(String[] args) {
        try {
            zooKeeper = new ZooKeeper("localhost:2181", 3000, null);
            byte[] leaderData = zooKeeper.getData("/pointOfContact", false, null);
            String leaderAddress = new String(leaderData);
            System.out.println("Leader Node Address: " + leaderAddress);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
