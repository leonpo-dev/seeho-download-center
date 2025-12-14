package com.seeho.downloadcenter.persistence.IService.impl;

import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import com.seeho.downloadcenter.persistence.mapper.DownloadLogMapper;
import com.seeho.downloadcenter.persistence.IService.DownloadLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * Default service implementation for {@link DownloadLogPO}.
 */
@Service
public class DownloadLogServiceImpl extends ServiceImpl<DownloadLogMapper, DownloadLogPO> implements DownloadLogService {

}
