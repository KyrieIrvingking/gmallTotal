package com.example.gmall.manage.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {
    public static String uploadImage(MultipartFile multipartFile)  {
        String imgUrl= "http://10.0.6.126";
        String file = PmsUploadUtil.class.getResource("/tracker.conf").getPath();
        try {
            ClientGlobal.init(file);
        }catch (Exception e) {
            e.printStackTrace();
        }
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StorageClient storageClient = new StorageClient(trackerServer,null);
        try {
            byte[] bytes=multipartFile.getBytes();
            String originalFileName=multipartFile.getOriginalFilename();
            int index=originalFileName.lastIndexOf(".");
            String extName=originalFileName.substring(index+1);
            String[]  upload_file = storageClient.upload_file(bytes,extName,null);
            for (String s : upload_file) {
                imgUrl+="/"+s;
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }
         System.out.println(imgUrl);
         return imgUrl;
    }
}
