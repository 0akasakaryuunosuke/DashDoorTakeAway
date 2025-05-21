package com.example.controller;//package com.example.controller;
//
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.thread.ThreadUtil;
//import cn.hutool.core.util.StrUtil;
//import com.example.common.Result;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.OutputStream;
//import java.net.URLEncoder;
//import java.util.List;
//
///**
// * 文件接口
// */
//@RestController
//@RequestMapping("/files")
//public class FileController {
//
//    // 文件上传存储路径
//    private static final String filePath = System.getProperty("user.dir") + "/files/";
//
//    @Value("${server.port:9090}")
//    private String port;
//
//    @Value("${ip:localhost}")
//    private String ip;
//
//    /**
//     * 文件上传
//     */
//    @PostMapping("/upload")
//    public Result upload(MultipartFile file) {
//        String flag;
//        synchronized (FileController.class) {
//            flag = System.currentTimeMillis() + "";
//            ThreadUtil.sleep(1L);
//        }
//        String fileName = file.getOriginalFilename();
//        try {
//            if (!FileUtil.isDirectory(filePath)) {
//                FileUtil.mkdir(filePath);
//            }
//            // 文件存储形式：时间戳-文件名
//            FileUtil.writeBytes(file.getBytes(), filePath + flag + "-" + fileName);  // ***/manager/files/1697438073596-avatar.png
//            System.out.println(fileName + "--上传成功");
//
//        } catch (Exception e) {
//            System.err.println(fileName + "--文件上传失败");
//        }
//        String http = "http://" + ip + ":" + port + "/files/";
//        return Result.success(http + flag + "-" + fileName);  //  http://localhost:9090/files/1697438073596-avatar.png
//    }
//
//
//    /**
//     * 获取文件
//     *
//     * @param flag
//     * @param response
//     */
//    @GetMapping("/{flag}")   //  1697438073596-avatar.png
//    public void avatarPath(@PathVariable String flag, HttpServletResponse response) {
//        OutputStream os;
//        try {
//            if (StrUtil.isNotEmpty(flag)) {
//                response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(flag, "UTF-8"));
//                response.setContentType("application/octet-stream");
//                byte[] bytes = FileUtil.readBytes(filePath + flag);
//                os = response.getOutputStream();
//                os.write(bytes);
//                os.flush();
//                os.close();
//            }
//        } catch (Exception e) {
//            System.out.println("文件下载失败");
//        }
//    }
//
//    /**
//     * 删除文件
//     *
//     * @param flag
//     */
//    @DeleteMapping("/{flag}")
//    public void delFile(@PathVariable String flag) {
//        List<String> fileNames = FileUtil.listFileNames(filePath);
//        FileUtil.del(filePath + flag);
//        System.out.println("删除文件" + flag + "成功");
//    }
//
//
//}
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${alioss.endpoint}")
    private String endpoint;

    @Value("${alioss.access-key-id}")
    private String accessKeyId;

    @Value("${alioss.access-key-secret}")
    private String accessKeySecret;

    @Value("${alioss.bucket-name}")
    private String bucketName;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        try {
            // 获取文件的原始文件名
            String originalFilename = file.getOriginalFilename();
            // 获取文件的后缀
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 使用 UUID 生成唯一的文件名
            String objectName = UUID.randomUUID().toString() + fileExtension;

            // 创建 OSS 客户端
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 上传文件到阿里云 OSS
            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(bucketName, objectName, inputStream);
            }

            // 构建文件的访问 URL
            String fileUrl = "https://" + bucketName + "." + endpoint + "/" + objectName;

            // 关闭 OSS 客户端
            ossClient.shutdown();
        return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return  Result.error(ResultCodeEnum.valueOf("PARAM_LOST_ERROR"));
        }
    }

    /**
     * 获取文件
     *
     * @param flag
     * @param response
     */
    @GetMapping("/{flag}")
    public void download(@PathVariable String flag, HttpServletResponse response) {
        try {
            // 创建 OSS 客户端
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 获取文件的输入流
            InputStream inputStream = ossClient.getObject(bucketName, flag).getObjectContent();

            // 设置下载响应头
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(flag, "UTF-8"));
            response.setContentType("application/octet-stream");

            // 写文件到输出流
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.getOutputStream().flush();
            inputStream.close();
            ossClient.shutdown();

        } catch (IOException e) {
            System.out.println("文件下载失败");
        }
    }

    /**
     * 删除文件
     *
     * @param flag
     */
    @DeleteMapping("/{flag}")
    public String deleteFile(@PathVariable String flag) {
        try {
            // 创建 OSS 客户端
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 删除文件
            ossClient.deleteObject(bucketName, flag);
            ossClient.shutdown();
            return "删除文件 " + flag + " 成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "删除文件失败";
        }
    }
}
