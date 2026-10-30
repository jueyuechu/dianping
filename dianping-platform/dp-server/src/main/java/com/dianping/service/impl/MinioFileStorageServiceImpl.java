package com.dianping.service.impl;

import com.dianping.common.exception.BaseException;
import com.dianping.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * MinIO 对象存储（dianping.upload.mode=minio 时启用）
 * 启动时自动建桶并设置公共读策略，便于前端直接访问图片 URL
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "dianping.upload.mode", havingValue = "minio")
public class MinioFileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "mp4");
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    @Value("${dianping.minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${dianping.minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${dianping.minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${dianping.minio.bucket:dianping}")
    private String bucket;

    private MinioClient client;

    @PostConstruct
    public void init() {
        try {
            client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO 桶已创建：{}", bucket);
            }
            setPublicReadPolicy();
            log.info("MinIO 初始化完成：{} / {}", endpoint, bucket);
        } catch (Exception e) {
            // 不阻断应用启动：上传时会返回明确错误
            log.warn("MinIO 初始化失败（上传将不可用）：{}", e.getMessage());
        }
    }

    @Override
    public String upload(MultipartFile file, String dir) {
        if (client == null) {
            throw new BaseException("MinIO 未就绪，请检查对象存储配置");
        }
        if (file == null || file.isEmpty()) {
            throw new BaseException("文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BaseException("文件不能超过 10MB");
        }
        String ext = extension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BaseException("仅支持 jpg/png/gif/webp/mp4 格式");
        }
        String objectName = sanitizeDir(dir) + "/"
                + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try (InputStream in = file.getInputStream()) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType() == null
                            ? "application/octet-stream" : file.getContentType())
                    .build());
            return endpoint + "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            log.error("MinIO 上传失败", e);
            throw new BaseException("文件上传失败");
        }
    }

    private void setPublicReadPolicy() throws Exception {
        String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [{
                    "Effect": "Allow",
                    "Principal": {"AWS": ["*"]},
                    "Action": ["s3:GetObject"],
                    "Resource": ["arn:aws:s3:::%s/*"]
                  }]
                }
                """.formatted(bucket);
        client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
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
