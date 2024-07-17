# 建表脚本
# @author >WL丶Night</a>
# @from

-- 创建库
create database if not exists nightbi;

-- 切换库
use nightbi;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '帐号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图表信息表
create table if not exists chart
(
    id           bigint auto_increment comment 'id' primary key,
    goal         text  null comment '分析目标',
    `name`         varchar(128) null comment '图表名称',
    chartData    text  null comment '图标数据',
    chartType    varchar(128) null comment '图表类型',
    genChart     text  null comment '生成的图表数据',
    genResult    text  null comment '生成的分析结论',
    `status`       tinyint      default 1                 not null comment '图表状态(1 - 等待, 2 - 正在执行, 3 - 执行完成, 0 - 失败)',
    execMessage  text null comment '执行信息',
    userId       bigint null comment '创建用户id',
    retry        int default 0 not null comment '图表分析重试次数',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;

-- 积分表
create table if not exists score
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                   comment '创建用户id',
    scoreTotal   bigint null  comment '总积分' default 0,
    isSign       tinyint      comment '0表示未签到，1表示已签到' default 0,
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '积分表' collate = utf8mb4_unicode_ci;


insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('Vglw', 'N02gI', '吕立辉', 'Sv', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('4qfM48Ljw', 'aY6O', '丁黎昕', 'cu', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('ZAo5JB', 'DfFY', '顾哲瀚', 'UXmbH', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('1NWY8o', '3Z', '马志泽', 'QTSVE', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('2507', 'p36T', '龙智宸', 'TQ8m', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('71phEj', 'AB8H', '钱天磊', 'x6F', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('bInA7V', 'B4MMq', '龙熠彤', 'ryM', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('3cZbNWe5', '2wx8g', '曹思远', '8IIe', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('TQuZ', 'aqju7', '赵立轩', 'Lhrzj', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('0H5ebeVJB082O', 'ihMWi', '蔡子默', 'r05', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('2kZmN', 'Xk', '许天磊', 'hWmy', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('6H8QBR', 'I9kIQ', '陆修杰', 'zS', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('1gHig6', '3I2jL', '程懿轩', 'ztWEJ', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('43hr', 'MA', '张越泽', '9fmJ', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('Ft6g8', 'qxLD', '任立辉', 'ee1Bl', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('6iM0Ug', '16', '沈明', 'TpSft', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('DicH', 'Uj5r', '韩修洁', 'K2wX', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('Ffb1', 'eH', '邱雨泽', 'dftgQ', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('5Hq4', 'uQ', '萧思淼', '1M2l', 'user');
insert into `user` (`userAccount`, `userPassword`, `userName`, `userAvatar`, `userRole`) values ('qmiO', 'mq', '袁浩宇', 'jHL', 'user');