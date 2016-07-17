package com.citicbank.hz.zk.controller;

import com.citicbank.hz.domain.PostContent;
import com.citicbank.hz.domain.ZkNode;
import com.citicbank.hz.zk.util.ZooKeeperUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/zk")
public class HomeController {
  @Value("${zookeeper.session.timeout}")
  private int sessionTimeout;

  @Value("${zookeeper.connection.string}")
  private String connectionString;

  @Value("${zookeeper.path}")
  private String zkPath;

  /**
   * create node in ZK, ip address is extracted from client's source.
   * @param content PostContent which contain port and data
   * @param uriBuilder UriComponentBuilder
   * @param request HttpServletRequest
   * @return HttpHeaders and HttpStatus, if error return exception message
   */

  @RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
  public ResponseEntity<String> create(@RequestBody PostContent content, 
      UriComponentsBuilder uriBuilder, HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    try {
      ZooKeeperUtil util = new ZooKeeperUtil();
      String ip = getSourceIp(request);
      String path = ip + ":" + content.getPort();
      ZkNode node = new ZkNode();
      node.setPath(path);
      node.setData(content.getData());
      util.createConnection(connectionString, sessionTimeout);
      util.createPath(zkPath + "/" + node.getPath(), node.getData());
      util.releaseConnection();
      return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    } catch (Exception exception) {
      exception.printStackTrace();
      return new ResponseEntity<String>("{\"ERROR\":" + exception.getMessage() + "\"}", headers,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * delete node by path, ip address is extracted from client's source.
   * @param content PostContent which contain port and data
   * @param uriBuilder UriComponentBuilder
   * @param request HttpServletRequest
   * @return HttpHeaders and HttpStatus
   */

  @RequestMapping(method = RequestMethod.DELETE, headers = "Accept=application/json")
  public ResponseEntity<String> delete(@RequestBody PostContent content, 
      UriComponentsBuilder uriBuilder, HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    try {
      ZooKeeperUtil util = new ZooKeeperUtil();
      String ip = getSourceIp(request);
      String path = ip + ":" + content.getPort();
      ZkNode node = new ZkNode();
      node.setPath(path);
      util.createConnection(connectionString, sessionTimeout);
      util.deleteNode(zkPath + "/" + node.getPath());
      util.releaseConnection();
      return new ResponseEntity<String>(headers, HttpStatus.GONE);
    } catch (Exception exception) {
      exception.printStackTrace();
      return new ResponseEntity<String>("{\"ERROR\":" + exception.getMessage() + "\"}", headers,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * if client visit server through reverse proxy, get x-forwarded-for else return
   * HttpServletRequest.getRemoteAddr()
   * 
   * @param request HttpServletRequest to get source ip
   * @return src ip address in String format
   */
  private static String getSourceIp(HttpServletRequest request) {
    String src = request.getHeader("X-Forwarded-For");
    if (src != null) {
      return src;
    } else {
      return request.getRemoteAddr();
    }
  }
}
