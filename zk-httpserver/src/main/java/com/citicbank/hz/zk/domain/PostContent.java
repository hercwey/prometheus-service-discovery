package com.citicbank.hz.zk.domain;

public class PostContent {
  String port;
  String data;

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "PostContent [port=" + port + ", data=" + data + "]";
  }
}
