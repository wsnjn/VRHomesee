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

-- Viewing Appointment Table
CREATE TABLE IF NOT EXISTS viewing_appointment (
    -- 基础信息
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID，主键',
    appointment_number VARCHAR(50) NOT NULL UNIQUE COMMENT '预约编号（唯一）',
    room_id BIGINT NOT NULL COMMENT '房间ID，外键关联room_info',
    user_id BIGINT NOT NULL COMMENT '用户ID，外键关联user',

    -- 预约信息
    preferred_date DATE NOT NULL COMMENT '期望看房日期',
    preferred_time_slot VARCHAR(20) NOT NULL COMMENT '期望时间段（如：09:00-10:00）',
    actual_date DATE COMMENT '实际看房日期',
    actual_time_slot VARCHAR(20) COMMENT '实际看房时间段',
    appointment_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '预约类型：1-现场看房，2-视频看房',

    -- 用户信息
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    wechat_id VARCHAR(50) COMMENT '微信号（可选）',
    tenant_count TINYINT COMMENT '租客人数',
    expected_move_in_date DATE COMMENT '期望入住日期',
    rental_intention TEXT COMMENT '租赁意向描述',

    -- 预约状态
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '预约状态：0-待确认，1-已确认，2-已完成，3-已取消，4-已过期，5-用户爽约',
    cancellation_reason VARCHAR(200) COMMENT '取消原因',

    -- 管理信息
    assigned_agent_id BIGINT COMMENT '分配的业务员ID',
    agent_notes TEXT COMMENT '业务员备注',
    admin_notes TEXT COMMENT '管理员备注',
    follow_up_required TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要跟进：0-否，1-是',
    follow_up_date DATE COMMENT '下次跟进日期',

    -- 时间戳
    appointment_created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预约创建时间',
    confirmed_time DATETIME COMMENT '确认时间',
    completed_time DATETIME COMMENT '完成时间',
    cancelled_time DATETIME COMMENT '取消时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 外键约束
    FOREIGN KEY (room_id) REFERENCES room_info(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (assigned_agent_id) REFERENCES user(id),

    -- 索引优化
    INDEX idx_appointment_number (appointment_number),
    INDEX idx_room_id (room_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_preferred_date (preferred_date),
    INDEX idx_assigned_agent_id (assigned_agent_id),
    INDEX idx_appointment_created_time (appointment_created_time)
) COMMENT='预约看房表，记录用户预约看房信息';

-- Initialize Default Groups
INSERT INTO chat_group (group_name, group_type, announcement) 
SELECT '幸福家园大家庭', 1, '欢迎来到幸福家园！' 
WHERE NOT EXISTS (SELECT 1 FROM chat_group WHERE group_type = 1);

INSERT INTO chat_group (group_name, group_type, announcement) 
SELECT '租客交流群', 2, '租客互助交流' 
WHERE NOT EXISTS (SELECT 1 FROM chat_group WHERE group_type = 2);
