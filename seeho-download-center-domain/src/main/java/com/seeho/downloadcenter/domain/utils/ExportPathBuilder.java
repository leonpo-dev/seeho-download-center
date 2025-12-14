package com.seeho.downloadcenter.domain.utils;

import com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum;
import com.seeho.downloadcenter.base.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Builds export file prefixes using the pattern
 * {base-path}/{date}/{business}/{downloadName} and eagerly creates directories.
 */
@Slf4j
@Component
public class ExportPathBuilder {

    @Value("${download.export.base-path:/data/export}")
    private String basePath;

    @Value("${download.export.date-format:yyyy-MM}")
    private String dateFormat;

    public String buildFilePathPrefix(DownloadRefServiceEnum downloadEnum, String downloadName) {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));

        String businessDir = downloadEnum.getDlCode()
                .toLowerCase()
                .replace("download_", "")
                .replace("_", "-");

        String directoryPath = String.format("%s/%s/%s", basePath, dateDir, businessDir);

        ensureDirectoryExists(directoryPath);

        String filePathPrefix = String.format("%s/%s", directoryPath, downloadName);

        log.info("[ExportPathBuilder] Built file path prefix: {}", filePathPrefix);
        return filePathPrefix;
    }

    private void ensureDirectoryExists(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("[ExportPathBuilder] Created directory: {}", directoryPath);
            }
        } catch (IOException e) {
            log.error("[ExportPathBuilder] Failed to create directory: {}", directoryPath, e);
            throw new BusinessException("Failed to create export directory: " + directoryPath, e);
        }
    }

    public String getBasePath() {
        return basePath;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
