package com.citicbank.hz.prometheus;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringPrometheusTargetGroupsApplication {
	
	@Value("${zookeeper.connection.string}")
	private String connectString;

	@Value("${zookeeper.session.timeout}")
	private int sessionTimeout;

	@Value("${zookeeper.path}")
	private String zkpath;

	@Value("${tgroups.dir}")
	private String tgroupsDir;

	public static void main(String[] args) {
		SpringApplication.run(SpringPrometheusTargetGroupsApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner run() {
		return new CommandLineRunner(){
			@Override
			public void run(String... args) throws Exception {
				ZooKeeperUtil util = new ZooKeeperUtil();
				util.createConnection(connectString, sessionTimeout);
				List<Target> targetList = util.getNodes(zkpath);
				Set<String> labelSet = new HashSet<String>();
				for (Target t : targetList) {
					labelSet.add(t.getData());
				}
				List<TargetGroup> targetGroups = new ArrayList<TargetGroup>();
				for (String l : labelSet) {
					Labels labels = new Labels();
					labels.setJob(l);
					TargetGroup tGroup = new TargetGroup();
					tGroup.targets = new ArrayList<String>();
					for (Target t: targetList) {
						if (l.equals(t.getData())) {
							tGroup.setLabels(labels);
							String node = t.getNode();
							String[] parts = node.split(":");
							String hostname = parts[0];
							int port = Integer.parseInt(parts[1]);
							if (CheckSocket.isSocketAlive(hostname, port)) {
								tGroup.targets.add(t.getNode());
							}
						}
					}
					if (tGroup.getTargets().size() > 0) {
						targetGroups.add(tGroup);
					}
				}
				util.releaseConnection();
				
				String targetGroupJson = TargetGroup.toJsonArray(targetGroups);
				PrintWriter printWriter = new PrintWriter(tgroupsDir + "/" + "tgroups.json");
				printWriter.print(targetGroupJson);
				printWriter.close();

			}
		};
	}
}
