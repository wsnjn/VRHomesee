use homesee;


CREATE TABLE user (
    -- 核心标识信息
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，主键',

    -- 账户核心信息（注册时填写）
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号，唯一，用于登录',
    email VARCHAR(100) UNIQUE COMMENT '邮箱，可选',
    password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    user_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '用户类型：1-租客，2-房东，3-管理员',

    -- 时间信息
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    -- 基本信息（后续完善）
    real_name VARCHAR(50) COMMENT '真实姓名',
    gender TINYINT(1) COMMENT '性别：0-未知，1-男，2-女',
    birthday DATE COMMENT '生日',
    id_card VARCHAR(18) UNIQUE COMMENT '身份证号',
    avatar VARCHAR(255) COMMENT '头像URL',
    wechat VARCHAR(50) COMMENT '微信号',
    qq VARCHAR(20) COMMENT 'QQ号',

    -- 租房相关信息
    job VARCHAR(50) COMMENT '职业',
    company VARCHAR(100) COMMENT '工作单位',
    monthly_income DECIMAL(10,2) COMMENT '月收入',
    rental_budget_min DECIMAL(8,2) COMMENT '最低租房预算（元/月）',
    rental_budget_max DECIMAL(8,2) COMMENT '最高租房预算（元/月）',
    preferred_districts VARCHAR(500) COMMENT '偏好区域（JSON格式存储）',
    house_requirements TEXT COMMENT '租房需求描述',

    -- 账户状态信息
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账户状态：0-禁用，1-正常，2-未激活',
    credit_score TINYINT NOT NULL DEFAULT 100 COMMENT '信用分（0-100）',
    is_certified TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否实名认证：0-未认证，1-已认证',
    certification_time DATETIME COMMENT '实名认证时间',

    -- 索引优化
    INDEX idx_phone (phone),
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_user_type (user_type),
    INDEX idx_status (status),
    INDEX idx_register_time (register_time),
    INDEX idx_credit_score (credit_score),
    INDEX idx_is_certified (is_certified)
) COMMENT='用户信息表，支持信息逐步完善';



CREATE TABLE user_verification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    id_card_front VARCHAR(255) COMMENT '身份证正面照',
    id_card_back VARCHAR(255) COMMENT '身份证反面照',
    live_photo VARCHAR(255) COMMENT '活体照片',
    verification_status TINYINT DEFAULT 0 COMMENT '认证状态',
    submitted_time DATETIME,
    verified_time DATETIME,
    verifier_id BIGINT COMMENT '审核人ID',
    verification_remark VARCHAR(500) COMMENT '审核备注',
    FOREIGN KEY (user_id) REFERENCES user(id)
);




CREATE TABLE user_preference (
    user_id BIGINT PRIMARY KEY,
    -- 租房偏好
    min_area DECIMAL(6,2) COMMENT '最小面积',
    max_area DECIMAL(6,2) COMMENT '最大面积',
    has_elevator_pref TINYINT COMMENT '电梯偏好',
    rental_type_pref TINYINT COMMENT '租赁类型偏好',
    orientation_pref VARCHAR(50) COMMENT '朝向偏好',
    -- 通知偏好
    sms_notification TINYINT DEFAULT 1,
    email_notification TINYINT DEFAULT 0,
    wechat_notification TINYINT DEFAULT 1,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);




CREATE TABLE room_info (
    -- 主键标识
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，唯一标识每条记录',

    -- 地址信息
    province VARCHAR(50) NOT NULL COMMENT '省份名称（如：广东省、北京市）',
    city VARCHAR(50) NOT NULL COMMENT '城市名称（如：深圳市、广州市）',
    district VARCHAR(50) NOT NULL COMMENT '区/县名称（如：南山区、天河区）',
    street VARCHAR(100) NOT NULL COMMENT '街道名称（如：科技园街道、珠江新城街道）',
    community_name VARCHAR(100) NOT NULL COMMENT '小区/楼盘名称（如：万科城、碧桂园）',
    building_unit VARCHAR(20) COMMENT '单元号（如：1单元、A单元）',
    door_number VARCHAR(20) NOT NULL COMMENT '门牌号（如：101号、201号）',
    room_number VARCHAR(20) COMMENT '房间号（如：301、502室）',

    -- 房屋基本信息
    has_elevator TINYINT(1) NOT NULL DEFAULT 0 COMMENT '有无电梯：0-无，1-有',
    room_area DECIMAL(6,2) COMMENT '房间面积（平方米）',
    floor_info VARCHAR(20) COMMENT '楼层信息（如：5/18，表示5楼共18层）',
    orientation VARCHAR(10) COMMENT '房屋朝向（如：南、南北通透）',
    decoration TINYINT(1) COMMENT '装修程度：1-毛坯，2-简装，3-精装，4-豪装',

    -- 价格信息
    rent_price DECIMAL(10,2) NOT NULL COMMENT '月租金价格（单位：元）',
    water_price DECIMAL(8,2) COMMENT '水费单价（元/吨），NULL表示包含在租金内',
    electric_price DECIMAL(8,2) COMMENT '电费单价（元/度），NULL表示包含在租金内',

    -- 租赁信息
    rental_type TINYINT(1) NOT NULL DEFAULT 0 COMMENT '租赁类型：0-整租，1-合租，2-单间',
    landlord_phone VARCHAR(20) NOT NULL COMMENT '房东联系电话',
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '房源状态：0-可租，1-已租，2-下架，3-预租',

    -- 描述信息
    description TEXT COMMENT '房源详细描述（装修、设施、周边等）',

    -- 时间戳
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    -- 索引优化
    INDEX idx_location (province, city, district),
    INDEX idx_community (community_name),
    INDEX idx_rent_price (rent_price),
    INDEX idx_rental_type (rental_type),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    UNIQUE INDEX idx_unique_house (province, city, district, community_name, door_number, room_number)
) COMMENT='房间信息表，存储房源详细信息';

CREATE TABLE tenant_management (
    -- 基础信息
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '租约ID，主键',
    contract_number VARCHAR(50) NOT NULL UNIQUE COMMENT '合同编号（唯一）',
    room_id BIGINT NOT NULL COMMENT '房间ID，外键关联room_info',
    landlord_id BIGINT NOT NULL COMMENT '房东用户ID，外键关联user',
    tenant_id BIGINT NOT NULL COMMENT '租客用户ID，外键关联user',

    -- 租约信息
    contract_start_date DATE NOT NULL COMMENT '合同开始日期',
    contract_end_date DATE NOT NULL COMMENT '合同结束日期',
    actual_move_in_date DATE COMMENT '实际入住日期',
    actual_move_out_date DATE COMMENT '实际退租日期',
    monthly_rent DECIMAL(10,2) NOT NULL COMMENT '月租金',
    deposit_amount DECIMAL(10,2) NOT NULL COMMENT '押金金额',
    payment_cycle TINYINT(1) NOT NULL DEFAULT 1 COMMENT '付款周期：1-月付，2-季付，3-年付',

    -- 租约状态
    contract_status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '合同状态：0-待签约，1-已签约，2-履行中，3-已到期，4-提前解约，5-已退租',
    rent_status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '租金状态：0-未付款，1-已付款，2-逾期，3-部分付款',
    deposit_status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '押金状态：0-未付，1-已付，2-已退，3-抵扣中',

    -- 费用信息
    water_initial_reading DECIMAL(8,2) COMMENT '水表初始读数',
    electric_initial_reading DECIMAL(8,2) COMMENT '电表初始读数',
    last_water_reading DECIMAL(8,2) COMMENT '上次水表读数',
    last_electric_reading DECIMAL(8,2) COMMENT '上次电表读数',

    -- 联系信息
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',

    -- 时间戳
    contract_signed_time DATETIME COMMENT '合同签订时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 外键约束
    FOREIGN KEY (room_id) REFERENCES room_info(id),
    FOREIGN KEY (landlord_id) REFERENCES user(id),
    FOREIGN KEY (tenant_id) REFERENCES user(id),

    -- 索引优化
    INDEX idx_contract_number (contract_number),
    INDEX idx_room_id (room_id),
    INDEX idx_landlord_id (landlord_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_contract_status (contract_status),
    INDEX idx_rent_status (rent_status),
    INDEX idx_contract_dates (contract_start_date, contract_end_date),
    INDEX idx_created_time (created_time)
) COMMENT='租客管理表，记录租赁关系和管理信息';





CREATE TABLE rent_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_management_id BIGINT NOT NULL COMMENT '租约ID',
    payment_period VARCHAR(20) NOT NULL COMMENT '缴费期间（如：2024-01）',
    payable_amount DECIMAL(10,2) NOT NULL COMMENT '应缴金额',
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '实缴金额',
    payment_date DATE COMMENT '缴费日期',
    payment_method TINYINT COMMENT '支付方式：1-微信，2-支付宝，3-银行卡，4-现金',
    payment_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-部分支付',
    due_date DATE NOT NULL COMMENT '缴费截止日期',
    late_fee DECIMAL(8,2) DEFAULT 0 COMMENT '滞纳金',
    remark VARCHAR(500) COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_management_id) REFERENCES tenant_management(id),
    INDEX idx_tenant_management_id (tenant_management_id),
    INDEX idx_payment_period (payment_period),
    INDEX idx_payment_status (payment_status)
) COMMENT='租金支付记录表';




CREATE TABLE maintenance_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_management_id BIGINT NOT NULL,
    request_title VARCHAR(100) NOT NULL,
    request_description TEXT NOT NULL,
    request_date DATETIME NOT NULL,
    request_status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待处理，1-处理中，2-已完成，3-已关闭',
    expected_fix_date DATE,
    actual_fix_date DATE,
    repair_cost DECIMAL(8,2),
    cost_bearer TINYINT COMMENT '费用承担方：1-房东，2-租客，3-分摊',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_management_id) REFERENCES tenant_management(id)
) COMMENT='维修报修表';




CREATE TABLE contract_change (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_management_id BIGINT NOT NULL,
    change_type TINYINT NOT NULL COMMENT '变更类型：1-续租，2-退租，3-转租，4-租金调整',
    change_reason VARCHAR(500),
    old_value VARCHAR(500),
    new_value VARCHAR(500),
    change_date DATE NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_management_id) REFERENCES tenant_management(id)
) COMMENT='合同变更记录表';



CREATE TABLE viewing_appointment (
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
