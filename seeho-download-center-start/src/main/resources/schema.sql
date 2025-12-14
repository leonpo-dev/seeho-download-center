-- ============================================
-- 数据库表结构初始化脚本
-- 适用于 H2 和 MySQL 数据库（H2 兼容 MySQL 模式）
-- ============================================

-- 下载日志表
CREATE TABLE IF NOT EXISTS download_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    download_name VARCHAR(255) NOT NULL,
    user_id BIGINT,
    download_type VARCHAR(100) NOT NULL,
    download_status TINYINT NOT NULL DEFAULT 0,
    fail_reason TEXT,
    retry_count INT DEFAULT 0,
    download_condition TEXT,
    titles TEXT,
    remark VARCHAR(500),
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    message_key VARCHAR(255),
    create_user_id BIGINT,
    create_time DATETIME,
    update_user_id BIGINT,
    update_time DATETIME,
    PRIMARY KEY (id)
);

-- 创建索引（H2 和 MySQL 兼容）
CREATE INDEX IF NOT EXISTS idx_user_id ON download_log(user_id);
CREATE INDEX IF NOT EXISTS idx_download_status ON download_log(download_status);
CREATE INDEX IF NOT EXISTS idx_message_key ON download_log(message_key);
CREATE INDEX IF NOT EXISTS idx_create_time ON download_log(create_time);

