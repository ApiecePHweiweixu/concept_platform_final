# 后端启动问题排查指南

## 常见错误及解决方案

### 1. 数据库表结构不匹配错误

**错误信息示例**：
```
Table 'concept_platform.incubation_milestone_report' doesn't have column 'expert_score'
```

**原因**：实体类中定义了字段，但数据库表中缺少对应字段。

**解决方案**：
1. 确认已按顺序执行所有 SQL 脚本：
   ```sql
   -- 1. 基础数据库
   SOURCE mysql/db_init.sql;
   
   -- 2. 孵化平台扩展
   SOURCE mysql/incubation_extension.sql;
   
   -- 3. 审批流程扩展（重要！）
   SOURCE mysql/incubation_approval_extension.sql;
   
   -- 4. 成功案例扩展
   SOURCE mysql/success_case_extension.sql;
   ```

2. 检查表结构：
   ```sql
   USE concept_platform;
   DESC incubation_milestone_report;
   ```
   应该看到以下字段：
   - `expert_score` (int)
   - `expert_feedback` (text)
   - `admin_approved` (int)
   - `admin_feedback` (text)

3. 如果缺少字段，执行：
   ```sql
   SOURCE mysql/incubation_approval_extension.sql;
   ```

### 2. 数据库连接失败

**错误信息示例**：
```
Communications link failure
Access denied for user 'root'@'localhost'
```

**解决方案**：
1. 检查 MySQL 服务是否启动
2. 检查 `application.properties` 中的配置：
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/concept_platform?...
   spring.datasource.username=root
   spring.datasource.password=你的密码  # 修改这里
   ```
3. 确认数据库 `concept_platform` 已创建：
   ```sql
   SHOW DATABASES LIKE 'concept_platform';
   ```

### 3. 表不存在错误

**错误信息示例**：
```
Table 'concept_platform.incubation_project' doesn't exist
```

**解决方案**：
1. 确认已执行 `mysql/incubation_extension.sql`
2. 检查表是否存在：
   ```sql
   USE concept_platform;
   SHOW TABLES LIKE 'incubation_%';
   ```
3. 如果表不存在，重新执行：
   ```sql
   SOURCE mysql/incubation_extension.sql;
   ```

### 4. 字段类型不匹配

**错误信息示例**：
```
Incorrect column specifier for column 'admin_approved'
```

**解决方案**：
1. 检查数据库表结构：
   ```sql
   DESC incubation_milestone_report;
   ```
2. 如果字段类型不对，删除表后重新创建：
   ```sql
   DROP TABLE IF EXISTS incubation_milestone_report;
   -- 然后重新执行 incubation_extension.sql 和 incubation_approval_extension.sql
   ```

### 5. MyBatis-Plus 映射错误

**错误信息示例**：
```
Could not set property 'expertScore'
```

**解决方案**：
1. 确认实体类字段名与数据库列名匹配（MyBatis-Plus 会自动转换驼峰）
   - 实体类：`expertScore` → 数据库：`expert_score`
   - 实体类：`adminApproved` → 数据库：`admin_approved`

2. 如果自动映射失败，可以在实体类字段上添加 `@TableField` 注解：
   ```java
   @TableField("expert_score")
   private Integer expertScore;
   ```

### 6. 编译错误

**错误信息示例**：
```
cannot find symbol: class QueryWrapper
```

**解决方案**：
1. 确认 Maven 依赖已下载：
   - 在 IntelliJ IDEA 中，右键项目 → Maven → Reload Project
   - 或命令行执行：`mvn clean install`

2. 检查 `pom.xml` 中是否包含 MyBatis-Plus 依赖

### 7. 端口占用错误

**错误信息示例**：
```
Port 8080 is already in use
```

**解决方案**：
1. 修改 `application.properties` 中的端口：
   ```properties
   server.port=8081
   ```
2. 或关闭占用 8080 端口的程序

## 快速诊断步骤

### 步骤 1：检查数据库
```sql
USE concept_platform;

-- 检查所有表是否存在
SHOW TABLES;

-- 检查关键表结构
DESC incubation_milestone_report;
DESC incubation_project;
DESC incubation_milestone;
```

### 步骤 2：检查后端配置
1. 打开 `src/main/resources/application.properties`
2. 确认数据库连接信息正确
3. 确认数据库密码已修改

### 步骤 3：清理并重新编译
```bash
# 在项目根目录执行
cd concept-platform
mvn clean
mvn compile
```

### 步骤 4：查看详细错误日志
启动后端时，查看控制台的完整错误信息，特别是：
- 错误类型（ClassNotFoundException, SQLException 等）
- 错误位置（哪个类、哪一行）
- 错误原因（具体说明）

## 如果问题仍未解决

请提供以下信息：
1. **完整的错误堆栈信息**（从控制台复制）
2. **数据库表结构**（执行 `DESC table_name;` 的结果）
3. **已执行的 SQL 脚本列表**
4. **后端配置文件内容**（隐藏密码）

