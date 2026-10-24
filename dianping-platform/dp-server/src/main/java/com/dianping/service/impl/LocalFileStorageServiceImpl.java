package com.dianping.service.impl;

import com.dianping.common.exception.BaseException;
import com.dianping.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储（开发环境默认）
 * 生产可替换为 MinIO 实现（S3 兼容），只需更换本类实现，接口不变
 */
@Slf4j
@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "mp4");
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    @Value("${dianping.upload.local-path:./uploads}")
    private String localPath;

    @Value("${dianping.upload.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Override
    public String upload(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new BaseException("文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BaseException("文件不能超过 10MB");
        }
        String safeDir = sanitizeDir(dir);
        String ext = extension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BaseException("仅支持 jpg/png/gif/webp/mp4 格式");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        try {
            Path targetDir = Paths.get(localPath, safeDir).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);
            file.transferTo(target.toFile());
            return publicBaseUrl + "/uploads/" + safeDir + "/" + filename;
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BaseException("文件上传失败");
        }
    }

    private String sanitizeDir(String dir) {
        if (dir == null || dir.isBlank()) {
            return "common";
        }
        String cleaned = dir.replaceAll("[^a-zA-Z0-9_-]", "");
        return cleaned.isBlank() ? "common" : cleaned;
    }

    private String extension(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}