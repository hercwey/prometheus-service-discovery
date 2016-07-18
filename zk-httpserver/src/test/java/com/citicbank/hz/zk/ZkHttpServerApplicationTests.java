package com.citicbank.hz.zk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.citicbank.hz.zk.ZkHttpServerApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ZkHttpServerApplication.class)
@WebAppConfiguration
public class ZkHttpServerApplicationTests {

  @Test
  public void contextLoads() {}

}
