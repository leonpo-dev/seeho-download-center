package com.seeho.downloadcenter.domain.dotask.sendtask;

import com.seeho.downloadcenter.persistence.po.DownloadLogPO;

public interface SendTaskToMQService {
    void sendTaskToMQ(DownloadLogPO downloadLogPO);
}
