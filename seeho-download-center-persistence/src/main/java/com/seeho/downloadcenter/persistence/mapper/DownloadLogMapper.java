package com.seeho.downloadcenter.persistence.mapper;

import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件下载日志记录表 Mapper 接口
 * </p>
 *
 * @author Leonpo
 * @since 2025-12-02
 */
@Mapper
public interface DownloadLogMapper extends BaseMapper<DownloadLogPO> {

}
