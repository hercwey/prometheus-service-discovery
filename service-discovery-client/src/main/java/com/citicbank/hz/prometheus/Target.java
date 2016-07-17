package com.citicbank.hz.prometheus;

public class Target {
	private String node;
	private String data;

	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "Target [node=" + node + ", data=" + data + "]";
	}
}
