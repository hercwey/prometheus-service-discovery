package com.citicbank.hz.prometheus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.ZooKeeper;

public class ZooKeeperUtil implements Watcher {
	private ZooKeeper zk = null; 
	private CountDownLatch connectedSemaphore = new CountDownLatch( 1 ); 
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** 
	 * 创建ZK连接 
	 * @param connectString  ZK服务器地址列表 
	 * @param sessionTimeout   Session超时时间 
	 */ 
	public void createConnection( String connectString, int sessionTimeout ) { 
		this.releaseConnection(); 
		try { 
			zk = new ZooKeeper( connectString, sessionTimeout, this ); 
			connectedSemaphore.await(); 
		} catch ( InterruptedException e ) { 
			logger.info("连接创建失败，发生 InterruptedException"); 
			logger.info(e.getMessage());
		} catch ( IOException e ) { 
			logger.info("连接创建失败，发生 IOException"); 
			logger.info(e.getMessage());
		} 
	} 

	/** 
	 * 关闭ZK连接 
	 */ 
	public void releaseConnection() { 
		if ( !isBlank( this.zk ) ) { 
			try { 
				this.zk.close(); 
			} catch ( InterruptedException e ) { 
				logger.info(e.getMessage());
			} 
		} 
	} 

	/** 
	 *  创建节点 
	 * @param path 节点path 
	 * @param data 初始数据内容 
	 * @return 
	 */ 
	public boolean createPath( String path, String data ) { 
		try { 
			if (this.zk.exists(path, false) != null) {
				writeData(path, data);
			} else {
				logger.info("节点创建成功, Path:"
						+ this.zk.create( path, // 
								data.getBytes(), // 
								Ids.OPEN_ACL_UNSAFE, // 
								CreateMode.PERSISTENT ) 
						+", content:"+ data ); 
			}
		} catch ( KeeperException e ) { 
			logger.info("节点创建失败，发生KeeperException"); 
			logger.info(e.getMessage());
		} catch ( InterruptedException e ) { 
			logger.info("节点创建失败，发生 InterruptedException"); 
			logger.info(e.getMessage());
		} 
		return true; 
	} 

	/** 
	 *  创建节点 
	 * @param path 节点path 
	 * @return
	 */ 
	public List<Target> getNodes(String path) { 
		List<Target> result = new ArrayList<Target>();
		List<String> nodeList = new ArrayList<String>();
		try { 
			nodeList = this.zk.getChildren(path, false);
		} catch ( KeeperException e ) { 
			logger.info("获取节点列表失败，发生KeeperException"); 
			logger.info(e.getMessage());
		} catch (InterruptedException e) {
			logger.info("获取节点列表失败，发生InterruptedException"); 
			logger.info(e.getMessage());
		} 
		
		for (String node : nodeList) {
			Target t = new Target();
			t.setNode(node);
			t.setData(this.readData(path + "/" + node));
			result.add(t);
		}

		return result; 
	} 

	/** 
	 * 读取指定节点数据内容 
	 * @param path 节点path 
	 * @return 
	 */ 
	public String readData( String path ) { 
		try { 
			logger.info("获取数据成功，path："+ path ); 
			return new String( this.zk.getData( path, false, null ) ); 
		} catch ( KeeperException e ) { 
			logger.info("读取数据失败，发生KeeperException，path:"+ path  ); 
			logger.info(e.getMessage());
			return""; 
		} catch ( InterruptedException e ) { 
			logger.info("读取数据失败，发生 InterruptedException，path:"+ path  ); 
			logger.info(e.getMessage());
			return""; 
		} 
	} 

	/** 
	 * 更新指定节点数据内容 
	 * @param path 节点path 
	 * @param data  数据内容 
	 * @return 
	 */ 
	public boolean writeData( String path, String data ) { 
		try { 
			logger.info("更新数据成功，path："+ path +", stat:"+ 
					this.zk.setData( path, data.getBytes(), -1 ) ); 
		} catch ( KeeperException e ) { 
			logger.info("更新数据失败，发生KeeperException，path:"+ path  ); 
			logger.info(e.getMessage());
		} catch ( InterruptedException e ) { 
			logger.info("更新数据失败，发生 InterruptedException，path:"+ path  ); 
			logger.info(e.getMessage());
		} 
		return false; 
	} 

	/** 
	 * 删除指定节点 
	 * @param path 节点path 
	 */ 
	public void deleteNode( String path ) { 
		try { 
			if (this.zk.exists(path, false) != null) {
				this.zk.delete( path, -1 ); 
				logger.info("删除节点成功，path："+ path ); 
			}
		} catch ( KeeperException e ) { 
			logger.info("删除节点失败，发生KeeperException，path:"+ path  ); 
			logger.info(e.getMessage());
		} catch ( InterruptedException e ) { 
			System.out.println("删除节点失败，发生 InterruptedException，path:"+ path  ); 
			logger.info(e.getMessage());
		} 
	} 

	/** 
	 * 收到来自Server的Watcher通知后的处理。 
	 */ 
	@Override 
	public void process( WatchedEvent event ) { 
		logger.info("收到事件通知："+ event.getState() +"\n" ); 
		if ( KeeperState.SyncConnected == event.getState() ) { 
			connectedSemaphore.countDown(); 
		} 
	} 

	public static boolean isBlank( Object object ){
		return null == object;
	}

	public static boolean isBlank( Object... originalObjectArray ) {

		if ( null == originalObjectArray || 0 == originalObjectArray.length )
			return true;
		for ( int i = 0; i < originalObjectArray.length; i++ ) {
			if ( isBlank( originalObjectArray[i]))
				return true;
		}
		return false;
	}
}