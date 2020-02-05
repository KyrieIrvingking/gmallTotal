package com.example.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sun.font.FontRunIterator;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Test
    public void contextLoads() throws IOException, MyException {
        String file = DemoApplicationTests.class.getResource("/tracker.conf").getPath();
        ClientGlobal.init(file);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer =trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer,null);
        String originalFilename="D:\\360download\\pic\\1.JPG";
        String[] upload_file=storageClient.upload_file(originalFilename,"JPG",null);
        String url="http://10.0.6.126";
        for (String s : upload_file) {
            url+="/"+s;
        }
        System.out.println(url);
    }
}
