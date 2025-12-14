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
 * Utilities for streaming files in HTTP responses.
 */
@Slf4j
public class FileUtils {

    public static void downloadFile(String filePath, HttpServletResponse response) throws BusinessException {
        Assert.hasText(filePath, "File path must not be empty");

        Assert.notNull(response, "Response must not be null");

        File file = new File(filePath);
        
        if (!file.exists()) {
            log.warn("File does not exist: {}", filePath);
            throw new BusinessException("File does not exist: " + filePath);
        }
        
        if (!file.isFile()) {
            log.warn("Path is not a file: {}", filePath);
            throw new BusinessException("Path is not a file: " + filePath);
        }

        String fileName = file.getName();
        
        try {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLengthLong(file.length());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));

            try (InputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (IOException e) {
            log.error("Failed to download file: {}", filePath, e);
            throw new BusinessException("Failed to download file: " + e.getMessage());
        }
    }
}
