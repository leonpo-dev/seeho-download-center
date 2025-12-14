package com.seeho.downloadcenter.persistence.IService.impl;

import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import com.seeho.downloadcenter.persistence.mapper.DownloadLogMapper;
import com.seeho.downloadcenter.persistence.IService.DownloadLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件下载日志记录表 服务实现类
 * </p>
 *
 * @author Leonpo
 * @since 2025-12-02
 */
@Service
public class DownloadLogServiceImpl extends ServiceImpl<DownloadLogMapper, DownloadLogPO> implements DownloadLogService {

}
