package com.citicbank.hz.zk.util;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZooKeeperUtil implements Watcher {
  private ZooKeeper zk = null;
  private CountDownLatch connectedSemaphore = new CountDownLatch(1);
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * Create ZK Connection.
   * 
   * @param connectString list of zk server
   * @param sessionTimeout session timeout
   */

  public void createConnection(String connectString, int sessionTimeout) {
    this.releaseConnection();
    try {
      zk = new ZooKeeper(connectString, sessionTimeout, this);
      connectedSemaphore.await();
    } catch (InterruptedException exception) {
      logger.info("Error Create connection，InterruptedException occured");
      logger.info(exception.getMessage());
    } catch (IOException exception) {
      logger.info("Error Create Connection, IOException occured");
      logger.info(exception.getMessage());
    }
  }

  /**
   * Close ZK connection.
   */
  public void releaseConnection() {
    if (!isBlank(this.zk)) {
      try {
        this.zk.close();
      } catch (InterruptedException exception) {
        logger.info(exception.getMessage());
      }
    }
  }

  /**
   * Create ZK Node.
   * 
   * @param path zk path
   * @param data content of zk path
   * @return true
   */
  public boolean createPath(String path, String data) {
    try {
      if (this.zk.exists(path, false) != null) {
        writeData(path, data);
      } else {
        logger.info("create path successfully, Path:" + this.zk.create(path, //
            data.getBytes(), //
            Ids.OPEN_ACL_UNSAFE, //
            CreateMode.PERSISTENT) + ", content:" + data);
      }
    } catch (KeeperException exception) {
      logger.info("Create path failed，KeeperException occured");
      logger.info(exception.getMessage());
    } catch (InterruptedException exception) {
      logger.info("Create path failed，InterruptedException occured");
      logger.info(exception.getMessage());
    }
    return true;
  }

  /**
   * Get node list, like{"1.1.1.1:9100", "1.1.1.2:9100").
   * 
   * @param path ZK path
   * @return node list
   */
  public List<String> getNodes(String path) {
    List<String> result = new ArrayList<String>();
    try {
      result = this.zk.getChildren(path, false);
    } catch (KeeperException exception) {
      logger.info("Error getNodes，KeeperException occured");
      logger.info(exception.getMessage());
    } catch (InterruptedException exception) {
      logger.info("Error getNodes，InterruptedException occured");
      logger.info(exception.getMessage());
    }

    return result;
  }

  /**
   * Get node data.
   * 
   * @param path ZK path
   * @return data of path
   */
  public String readData(String path) {
    try {
      logger.info("Get data successfully，path：" + path);
      return new String(this.zk.getData(path, false, null));
    } catch (KeeperException exception) {
      logger.info("Error read data，KeeperException，path:" + path);
      logger.info(exception.getMessage());
      return "";
    } catch (InterruptedException exception) {
      logger.info("Error read data，InterruptedException，path:" + path);
      logger.info(exception.getMessage());
      return "";
    }
  }

  /**
   * update data of zk path.
   * 
   * @param path ZK path
   * @param data content
   * @return true if success, false if get exception
   */
  public boolean writeData(String path, String data) {
    try {
      logger.info("Write data successfully，path：" + path + ", "
          + "stat:" + this.zk.setData(path, data.getBytes(), -1));
    } catch (KeeperException exception) {
      logger.info("Write data failed，KeeperException occured，path:" + path);
      logger.info(exception.getMessage());
      return false;
    } catch (InterruptedException exception) {
      logger.info("Write data failed，InterruptedException occured，path:" + path);
      logger.info(exception.getMessage());
      return false;
    }
    return true;
  }

  /**
   * delete node.
   * 
   * @param path ZK path
   */
  public void deleteNode(String path) {
    try {
      if (this.zk.exists(path, false) != null) {
        this.zk.delete(path, -1);
        logger.info("delete Node successfully，path：" + path);
      }
    } catch (KeeperException exception) {
      logger.info("delete node failed，KeeperException，path:" + path);
      logger.info(exception.getMessage());
    } catch (InterruptedException exception) {
      System.out.println("delete node failed，InterruptedException，path:" + path);
      logger.info(exception.getMessage());
    }
  }

  /**
   * processor when receive Watcher from server.
   */
  @Override
  public void process(WatchedEvent event) {
    logger.info("Receive event：" + event.getState());
    if (KeeperState.SyncConnected == event.getState()) {
      connectedSemaphore.countDown();
    }
  }

  public static boolean isBlank(Object object) {
    return null == object;
  }

  /**
   * test if originalObjectArray is blank.
   * 
   * @param originalObjectArray array to test
   * @return true if blank
   */
  public static boolean isBlank(Object... originalObjectArray) {
    if (null == originalObjectArray || 0 == originalObjectArray.length) {
      return true;
    }
    for (int i = 0; i < originalObjectArray.length; i++) {
      if (isBlank(originalObjectArray[i])) {
        return true;
      }
    }
    return false;
  }
}
