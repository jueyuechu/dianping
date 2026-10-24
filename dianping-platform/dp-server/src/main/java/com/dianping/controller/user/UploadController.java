package com.dianping.controller.user;

import com.dianping.common.result.Result;
import com.dianping.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片/视频上传（用户端与商户端）
 */
@RestController
@RequestMapping("/api")
public class UploadController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 用户端上传：评价配图 / 笔记配图
     */
    @PostMapping("/user/upload")
    public Result<Map<String, String>> userUpload(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(defaultValue = "common") String dir) {
        return Result.success(buildResult(fileStorageService.upload(file, dir)));
    }

    /**
     * 商户端上传：门店/菜品/团购图片
     */
    @PostMapping("/merchant/upload")
    public Result<Map<String, String>> merchantUpload(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(defaultValue = "common") String dir) {
        return Result.success(buildResult(fileStorageService.upload(file, dir)));
    }

    private Map<String, String> buildResult(String url) {
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return data;
    }
}