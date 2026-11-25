# 超市进销存管理系统（MarketStudent）

一个基于 **Spring Boot 3 + Vue 3** 前后端分离的超市进销存管理系统，覆盖「商品 → 供应商 → 采购 → 入库/出库 → 库存预警 → 数据看板」的完整业务闭环，同时实现了基于 RBAC 的权限管理与 AOP 操作日志。

> 本项目为个人课程实践项目，独立完成数据库设计、后端接口、前端页面与部署调试。

---

## 技术栈

**后端**

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Spring Boot | 3.2.0 | 基础框架 |
| Java | 17 | 开发语言 |
| MyBatis-Plus | 3.5.7 | ORM / 分页插件 |
| MySQL | 8.0 | 数据库 |
| Spring Security + JWT（java-jwt） | 4.4.0 | 登录认证与接口鉴权 |
| Knife4j（OpenAPI 3） | 4.3.0 | 接口文档 |
| Spring AOP | — | 操作日志切面 |
| Lombok / Hutool | 5.8.25 | 简化开发 / HTTP 调用 |
| 大模型 API | DeepSeek（OpenAI 兼容协议） | AI 商品文案、库存补货建议 |

**前端**

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Vue | 3.2 | 视图框架（Composition API） |
| Vue CLI | 5.0 | 构建工具 |
| Element Plus | 2.9 | UI 组件库 |
| Vue Router / Pinia | 4 / 3 | 路由（动态菜单）/ 状态管理 |
| Axios | 1.7 | HTTP 请求封装 |
| ECharts | 5.6 | 数据看板图表 |
| Sass | 1.32 | 样式预处理 |

---

## 功能模块

| 模块 | 功能 |
| --- | --- |
| 认证与权限 | 登录、注册、找回密码、JWT 鉴权、动态菜单、按角色分配菜单权限 |
| 数据看板 | 商品/供应商/订单/库存预警统计卡片、采购趋势折线图、分类占比饼图、采购与库存对比柱状图（ECharts） |
| 系统管理 | 用户管理、角色管理、菜单管理、操作日志（AOP 自动记录） |
| 商品管理 | 商品列表（增删改查、上下架、按分类/名称检索）、商品分类管理 |
| 供应商管理 | 供应商维护、供应商评价（四维打分）、商品批次质量记录 |
| 采购管理 | 采购订单（多明细）、状态流转（待审核 → 已审核 → 已收货 / 已取消） |
| 库存管理 | 库存查询与预警、库存上下限设置、入库单、出库单 |
| AI 能力 | 商品文案自动生成（支持卖点关键词与风格）、基于低库存数据的补货建议 |

**AI 模块设计**：`AiClient` 只负责「发请求、取文本」，按 OpenAI 兼容协议调用 `/chat/completions`，因此换成任何兼容接口（DeepSeek、通义、Kimi、自建 vLLM）只改配置不改代码；提示词与输出约束（字数、禁用绝对化用语、禁止编造功效）统一收在 `AiService`，并对模型输出做引号与前缀清洗。未配置密钥时返回友好提示，不影响其它业务功能。

**采购收货联动**：确认收货时自动按明细增加库存、写入入库单，并把明细的已收货数量回填，避免手工二次录入。

---

## 目录结构

```
MarketStudent-boot-3.x/            后端
├── src/main/java/org/example/springboot/
│   ├── ai/                        大模型客户端与配置（AiClient / AiProperties）
│   ├── annotation/                自定义注解（@OperationLog）
│   ├── aspect/                    AOP 切面（操作日志）
│   ├── common/                    统一返回 Result、结果码
│   ├── config/                    安全、JWT 拦截器、MyBatis-Plus、Knife4j 配置
│   ├── controller/                接口层（用户/角色/菜单/日志/商品/分类/供应商/采购/库存/质量/看板）
│   ├── entity/                    实体（对应 15 张表）
│   ├── exception/                 自定义异常与全局异常处理
│   ├── mapper/                    MyBatis-Plus Mapper
│   ├── service/                   业务层
│   └── util/                      JWT、文件、日期、IP 工具
├── src/main/resources/application.properties
└── sql/supermarket_management.sql 建库建表 + 初始化数据

MarketStudent-Vue3/                前端
├── src/
│   ├── api/                       接口（如需拆分可放这里）
│   ├── components/                通用组件（分页、导航栏、侧边栏）
│   ├── layouts/                   后台/前台布局
│   ├── router/                    路由 + 动态菜单加载
│   ├── store/                     Pinia（用户、应用状态）
│   ├── utils/request.js           Axios 封装（统一 token、错误提示）
│   └── views/backend/             业务页面
│       ├── product/{list,category}
│       ├── supplier/{index,evaluation,quality}
│       ├── purchase/order
│       ├── inventory/{index,stockin,stockout}
│       └── system/{user,role,menu,log}
└── vue.config.js                  开发代理 /api → localhost:1234
```

---

## 快速启动

### 1. 初始化数据库

```bash
mysql -uroot -p < sql/supermarket_management.sql
```

脚本会创建 `supermarket_management` 库、15 张表，并写入菜单、角色、用户与示例业务数据。

默认账号（密码均为 `123456`）：

| 账号 | 角色 |
| --- | --- |
| admin | 管理员 |
| user1 / user2 / test1 | 普通用户 |

### 2. 启动后端

修改 `src/main/resources/application.properties` 中的数据库账号密码：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/supermarket_management?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的密码
```

如需使用 AI 功能（商品文案生成 / 补货建议），在同一个文件末尾填写密钥；不填也能正常启动，只是调用 AI 接口时会提示未配置：

```properties
ai.api-base-url=https://api.deepseek.com/v1
ai.model=deepseek-chat
ai.api-key=你的密钥
```

```bash
mvn spring-boot:run
```

- 服务端口：`1234`
- 接口文档（Knife4j）：http://localhost:1234/doc.html

### 3. 启动前端

```bash
npm install
npm run serve
```

访问 http://localhost:8080 ，跨域由 `vue.config.js` 的 `/api` 代理转发到 1234 端口。

---

## 接口约定

- 所有 `@RestController` 统一加 `/api` 前缀（`WebConfig.configurePathMatch`）。
- 统一返回体：

```json
{ "code": "200", "msg": "操作成功", "data": {} }
```

- 除登录、注册、找回密码、静态资源外，其余接口需在请求头携带 `token`（`JwtInterceptor` 校验）。
- 分页接口统一返回 MyBatis-Plus 的 `Page`：`{ records: [], total: 0, current: 1, size: 10 }`，入参用 `currentPage` / `size`。

**AI 相关接口**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/ai/status` | 返回是否启用、是否已配置密钥、当前模型 |
| POST | `/api/ai/product-description` | 入参 `productName / categoryName / features / tone / wordLimit`，返回 `{ description }` |
| GET | `/api/ai/stock-advice` | 读取低于安全库存的商品，返回 `{ advice, items, count }` |

---

## 已知可优化点

- 密码目前以 BCrypt 存储，但 `application.properties` 中的数据库密码为明文，生产环境应改为环境变量注入。
- 采购单号、入库/出库单号按时间戳生成，高并发下建议改为「业务前缀 + 日期 + 自增序列」或使用分布式 ID。
- 库存增减目前依赖事务内的逐条更新，数据量大时可考虑引入乐观锁（`version` 字段）或数据库行锁。
- 前端尚未拆分 `src/api` 模块，接口调用直接写在页面里，可按模块抽取以便复用与维护。
- AI 接口目前是同步调用，模型响应慢时会占住请求线程，生产环境建议改成异步任务 + 轮询或 SSE 流式返回。
- 接入真实用户输入后，需要在提示词层做注入防护（如限制长度、过滤指令性内容），并对单用户调用做频率限制，避免密钥额度被刷。
