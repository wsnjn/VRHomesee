-- Maintenance Request Table
CREATE TABLE IF NOT EXISTS maintenance_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_management_id BIGINT NOT NULL COMMENT '关联的租约ID',
    request_title VARCHAR(100) NOT NULL COMMENT '报修标题',
    request_description TEXT NOT NULL COMMENT '报修详情描述',
    request_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    request_status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待处理，1-处理中，2-已完成，3-已关闭',
    expected_fix_date DATE COMMENT '期望维修日期',
    actual_fix_date DATE COMMENT '实际维修日期',
    repair_cost DECIMAL(8,2) COMMENT '维修费用',
    cost_bearer TINYINT COMMENT '费用承担方：1-房东，2-租客，3-分摊',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant_mgmt (tenant_management_id),
    INDEX idx_status (request_status)
) COMMENT='维修报修表';

-- Community: Chat Groups
CREATE TABLE IF NOT EXISTS chat_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(100) NOT NULL COMMENT '群组名称',
    group_type TINYINT NOT NULL DEFAULT 0 COMMENT '0-普通群，1-全局大群，2-租客大群',
    owner_id BIGINT COMMENT '群主ID，全局群可为空',
    announcement TEXT COMMENT '群公告',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT='聊天群组表';

-- Community: Group Members
CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role TINYINT DEFAULT 0 COMMENT '0-成员，1-管理员，2-群主',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_group_user (group_id, user_id)
) COMMENT='群组成员表';

-- Community: Friendships
CREATE TABLE IF NOT EXISTS friendship (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-申请中，1-已通过，2-已拒绝',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_friend (user_id, friend_id)
) COMMENT='好友关系表';

-- Community: Chat Messages
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT COMMENT '私聊接收者ID，群聊时为空',
    group_id BIGINT COMMENT '群聊ID，私聊时为空',
    content TEXT NOT NULL COMMENT '消息内容',
    msg_type TINYINT DEFAULT 0 COMMENT '0-文本，1-图片，2-视频',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sender (sender_id),
    INDEX idx_group (group_id),
    INDEX idx_receiver (receiver_id)
) COMMENT='聊天消息表';

-- Community: Social Posts (Daily Feed)
CREATE TABLE IF NOT EXISTS social_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    content TEXT COMMENT '文字内容',
    media_urls TEXT COMMENT '媒体链接，JSON数组',
    visibility TINYINT DEFAULT 0 COMMENT '0-公开，1-仅好友',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) COMMENT='社区动态表';

-- Community: Comments
CREATE TABLE IF NOT EXISTS social_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post (post_id)
) COMMENT='动态评论表';

-- Community: Likes
CREATE TABLE IF NOT EXISTS social_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_post_user (post_id, user_id)
) COMMENT='动态点赞表';

-- Initialize Default Groups
INSERT INTO chat_group (group_name, group_type, announcement) 
SELECT '幸福家园大家庭', 1, '欢迎来到幸福家园！' 
WHERE NOT EXISTS (SELECT 1 FROM chat_group WHERE group_type = 1);

INSERT INTO chat_group (group_name, group_type, announcement) 
SELECT '租客交流群', 2, '租客互助交流' 
WHERE NOT EXISTS (SELECT 1 FROM chat_group WHERE group_type = 2);
