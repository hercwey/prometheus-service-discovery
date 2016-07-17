package com.citicbank.hz.prometheus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSocket {
	private final static Logger logger = LoggerFactory.getLogger(CheckSocket.class);

	public static boolean isSocketAlive(String hostname, int port) {
		boolean isAlive = false;
		SocketAddress socketAddress = new InetSocketAddress(hostname, port);
		Socket socket = new Socket();
		int timeout = 2000;
		try {
			socket.connect(socketAddress, timeout);
			socket.close();
			isAlive = true;
		} catch (SocketTimeoutException exception) {
			logger.info("SocketTimeoutException" + hostname + ":" + port);
		} catch (IOException exception) {
			logger.info("IOException - Unable to connect to " + hostname + ":" + port);
		}
		
		return isAlive;
	}

}
