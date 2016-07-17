package com.citicbank.hz.prometheus;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TargetGroup {
	List<String> targets;
	Labels labels;

	public List<String> getTargets() {
		return targets;
	}
	public void setTargets(List<String> targets) {
		this.targets = targets;
	}

	public Labels getLabels() {
		return labels;
	}
	public void setLabels(Labels labels) {
		this.labels = labels;
	}
	
	public static String toJsonArray(Collection<TargetGroup> targetGroups) throws JsonProcessingException {
		ObjectMapper mapperObj = new ObjectMapper();
		return mapperObj.writerWithDefaultPrettyPrinter().writeValueAsString(targetGroups);
	}
}