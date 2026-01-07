# 编译错误修复指南

## 问题分析

编译错误主要是：
1. ✅ **已修复**：`AdminApproveReportDto` 类重复定义（已删除重复定义）
2. 找不到各种 DTO/VO 的 getter/setter 方法（Lombok 注解处理器问题）

## 解决步骤

### 步骤 1：清理并重新编译项目

在 IntelliJ IDEA 中：

1. **清理项目**：
   - 菜单：`Build` → `Clean Project`
   - 或使用快捷键：`Ctrl + Shift + F9`（Windows）或 `Cmd + Shift + F9`（Mac）

2. **重新构建项目**：
   - 菜单：`Build` → `Rebuild Project`
   - 或使用快捷键：`Ctrl + Shift + F9`（Windows）或 `Cmd + Shift + F9`（Mac）

### 步骤 2：确保 Lombok 插件已安装并启用

1. **检查 Lombok 插件**：
   - `File` → `Settings` → `Plugins`
   - 搜索 "Lombok"
   - 确保已安装并启用

2. **启用注解处理器**：
   - `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
   - 勾选 `Enable annotation processing`

3. **应用并重启 IDE**：
   - 点击 `Apply` 和 `OK`
   - 重启 IntelliJ IDEA

### 步骤 3：使用 Maven 重新编译

在项目根目录（`concept-platform`）打开终端，执行：

```bash
# Windows PowerShell
mvn clean compile

# 或者完整构建
mvn clean install
```

### 步骤 4：如果仍然有问题，手动验证 Lombok

1. **检查 pom.xml**：
   确保包含以下依赖：
   ```xml
   <dependency>
       <groupId>org.projectlombok</groupId>
       <artifactId>lombok</artifactId>
       <optional>true</optional>
   </dependency>
   ```

2. **检查编译器配置**：
   在 `pom.xml` 的 `maven-compiler-plugin` 中应该有：
   ```xml
   <annotationProcessorPaths>
       <path>
           <groupId>org.projectlombok</groupId>
           <artifactId>lombok</artifactId>
       </path>
   </annotationProcessorPaths>
   ```

### 步骤 5：验证实体类

确认以下文件存在：
- ✅ `IncubationMilestoneReport.java`
- ✅ `IncubationProject.java`
- ✅ `IncubationMilestone.java`
- ✅ `IncubationResourceRequest.java`

### 步骤 6：如果问题仍然存在

**临时解决方案**：如果 Lombok 仍然不工作，可以手动添加 getter/setter 方法，或者：

1. **检查 IDE 设置**：
   - 确保 IntelliJ IDEA 版本支持 Lombok（2020.3+）
   - 确保 Java 版本为 JDK 21

2. **重新导入 Maven 项目**：
   - 右键项目根目录
   - `Maven` → `Reload Project`

3. **清除 IDE 缓存**：
   - `File` → `Invalidate Caches...`
   - 选择 `Invalidate and Restart`

## 验证修复

编译成功后，应该能看到：
```
BUILD SUCCESS
```

如果还有错误，请提供具体的错误信息。

