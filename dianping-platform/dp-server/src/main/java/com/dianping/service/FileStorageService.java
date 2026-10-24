package com.dianping.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * 上传文件并返回可访问 URL
     *
     * @param file 文件
     * @param dir  业务目录：review / blog / shop 等
     */
    String upload(MultipartFile file, String dir);
}