-- Optional init data script for local testing.
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
    'Sample download task',
    1,
    'DOWNLOAD_ZTO_BILLS',
    0,
    0,
    'Seed data for demo purposes',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM download_log);
