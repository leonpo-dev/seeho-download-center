package com.seeho.downloadcenter.utils;

import com.seeho.downloadcenter.base.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Leonpo
 * @since 2025-12-02
 */
@Slf4j
public class FileUtils {

    /**
     * 下载文件到客户端
     *
     * @param filePath 文件在服务器上的绝对路径
     * @param response HTTP响应对象
     * @throws BusinessException 业务异常
     */
    public static void downloadFile(String filePath, HttpServletResponse response) throws BusinessException {
        // 检查文件路径是否为空
        Assert.hasText(filePath, "文件路径不能为空");

        // 检查response是否为空
        Assert.notNull(response, "HTTP响应对象不能为空");

        File file = new File(filePath);
        
        // 检查文件是否存在
        if (!file.exists()) {
            log.warn("文件不存在: {}", filePath);
            throw new BusinessException("文件不存在: " + filePath);
        }
        
        // 检查是否为文件而非目录
        if (!file.isFile()) {
            log.warn("指定路径不是文件: {}", filePath);
            throw new BusinessException("指定路径不是文件: " + filePath);
        }

        // 获取文件名
        String fileName = file.getName();
        
        try {
            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLengthLong(file.length());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));

            // 将文件内容写入响应输出流
            try (InputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192]; // 使用8KB缓冲区
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (IOException e) {
            log.error("文件下载失败: {}", filePath, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }
}
