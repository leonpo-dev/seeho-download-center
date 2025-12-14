-- ============================================
-- 数据库初始化数据脚本（可选）
-- 如需初始化测试数据，可在此文件中添加
-- ============================================

-- 示例：插入测试数据（可根据需要修改或删除）
INSERT INTO download_log (
    download_name,
    user_id,
    download_type,
    download_status,
    retry_count,
    remark,
    create_time,
    update_time
)
SELECT
    '测试下载任务',
    1,
    'DOWNLOAD_ZTO_BILLS',
    0,
    0,
    '示例初始化数据，可自主修改或删除',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM download_log);
