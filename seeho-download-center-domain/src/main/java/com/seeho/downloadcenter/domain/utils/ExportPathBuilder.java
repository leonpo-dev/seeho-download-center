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
 * 导出文件路径构建工具
 * <p>
 * 负责拼接导出文件的完整路径前缀，支持按日期和业务分目录，自动创建目录
 * </p>
 * <p>
 * 路径结构：{base-path}/{yyyy-MM}/{业务code}/{downloadName}
 * 示例：/data/export/2025-11/zto-bills/download-zto-bills-user123
 *
 * @author Leonpo
 * @since 2025-11-27
 */
@Slf4j
@Component
public class ExportPathBuilder {

    /**
     * 导出文件根目录（从配置文件读取）
     */
    @Value("${download.export.base-path:/data/export}")
    private String basePath;

    /**
     * 日期格式（从配置文件读取，默认按月分目录）
     */
    @Value("${download.export.date-format:yyyy-MM}")
    private String dateFormat;

    /**
     * 构建导出文件路径前缀
     * <p>
     * 根据业务枚举和下载名称，生成完整的文件路径前缀（不含时间戳后缀和扩展名）
     * </p>
     *
     * @param downloadEnum 下载业务枚举
     * @param downloadName 下载名称（业务标识，如 "download-zto-bills-user123"）
     * @return 完整路径前缀（如 "/data/export/2025-11/zto-bills/download-zto-bills-user123"）
     * @throws BusinessException 当目录创建失败时
     */
    public String buildFilePathPrefix(DownloadRefServiceEnum downloadEnum, String downloadName) {
        // 1. 获取当前日期目录（按配置格式）
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));

        // 2. 获取业务目录（使用枚举的 dlCode，转小写并去掉 DOWNLOAD_ 前缀）
        String businessDir = downloadEnum.getDlCode()
                .toLowerCase()
                .replace("download_", "")
                .replace("_", "-");

        // 3. 拼接完整目录路径
        String directoryPath = String.format("%s/%s/%s", basePath, dateDir, businessDir);

        // 4. 确保目录存在（自动创建）
        ensureDirectoryExists(directoryPath);

        // 5. 拼接文件路径前缀
        String filePathPrefix = String.format("%s/%s", directoryPath, downloadName);

        log.info("[ExportPathBuilder] Built file path prefix: {}", filePathPrefix);
        return filePathPrefix;
    }

    /**
     * 确保目录存在，不存在则自动创建
     *
     * @param directoryPath 目录路径
     * @throws BusinessException 当目录创建失败时
     */
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

    /**
     * 获取配置的根目录（用于测试或监控）
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * 获取配置的日期格式（用于测试或监控）
     */
    public String getDateFormat() {
        return dateFormat;
    }
}
