# malilib — Masa Mod 公共基础库

**版本**: 0.16.0 | **Minecraft**: 1.20.1 Fabric | **Java**: 17

malilib 是 masa 系列模组（Litematica、MiniHUD、Tweakeroo、Item Scroller 等）的共同依赖库。它提供了配置管理、热键绑定、GUI 组件、渲染工具、文件操作、网络通信等基础功能。

---

## 目录

- [功能模块](#功能模块)
  - [1. 配置系统](#1-配置系统)
  - [2. 热键系统](#2-热键系统)
  - [3. GUI 组件库](#3-gui-组件库)
  - [4. 渲染工具](#4-渲染工具)
  - [5. 事件系统](#5-事件系统)
  - [6. 工具类集合](#6-工具类集合)
  - [7. 网络系统](#7-网络系统)
  - [8. 数据导出](#8-数据导出)
- [编译与安装](#编译与安装)
- [配置说明](#配置说明)
- [MRQ 分支改进](#mrq-分支改进)
- [许可证](#许可证)

---

## 功能模块

### 1. 配置系统

完整的 JSON 持久化配置框架，所有 masa 模组共用。

**支持的配置类型**:
| 类型 | 说明 | 示例 |
|------|------|------|
| `ConfigBoolean` | 布尔开关 | 功能启用/禁用 |
| `ConfigInteger` | 整数（支持滑块） | 数值范围 0-64 |
| `ConfigDouble` | 浮点数（支持滑块） | Gamma 值 0-16 |
| `ConfigString` | 文本输入 | 自定义格式字符串 |
| `ConfigStringList` | 字符串列表 | 黑白名单 |
| `ConfigColor` | 颜色值（#RRGGBBAA） | 覆盖层颜色 |
| `ConfigOptionList` | 枚举选项（循环切换） | 模式选择 |
| `ConfigHotkey` | 热键绑定 | 快捷键绑定 |

**配置接口层次**: `IConfigBase` → `IConfigValue` → 具体类型接口（`IConfigBoolean`, `IConfigInteger` 等），支持值变更回调、重置、热键切换等扩展能力。

**ConfigManager**: 全局配置管理器，按 `modId` 注册处理器，统一加载/保存所有模组配置到 `config/<modid>.json`。

### 2. 热键系统

多键组合热键绑定框架。

**KeybindMulti — 多键绑定**:
- 支持多个按键同时按下的组合键
- 按键顺序敏感/不敏感模式
- 独占模式（激活时阻止其他热键）
- 取消模式（阻止进一步事件传递）
- 长按检测

**KeybindSettings — 行为配置**:
| 属性 | 可选值 | 说明 |
|------|--------|------|
| Context | INGAME / GUI / ANY | 生效上下文 |
| KeyAction | PRESS / RELEASE / BOTH | 触发时机 |
| allowExtraKeys | true/false | 是否允许额外按键 |
| orderSensitive | true/false | 按键顺序敏感 |
| exclusive | true/false | 独占模式 |
| cancel | true/false | 取消事件传递 |

**预定义回调**: `KeyCallbackToggleBoolean`（切换开关）、`KeyCallbackToggleBooleanConfigWithMessage`（切换+消息）、`KeyCallbackAdjustable`（可调节值）。

**InputEventHandler**: 全局输入事件调度器，管理所有热键的按键码映射，处理键盘/鼠标事件分发。

### 3. GUI 组件库

完整的 Minecraft GUI 控件框架。

**基础类**:
- `GuiBase` — 所有 GUI 的基类，管理按钮/控件/消息渲染
- `GuiConfigsBase` — 配置界面基类，支持配置列表、热键设置、脏状态追踪
- `GuiListBase` — 列表界面基类，支持滚动、搜索、选择

**按钮**: `ButtonGeneric`（通用）、`ButtonOnOff`（开关）、`ConfigButtonBoolean`（布尔切换）、`ConfigButtonOptionList`（选项循环）、`ConfigButtonKeybind`（热键捕获）。

**控件**: `WidgetCheckBox`（复选框）、`WidgetSlider`（滑块）、`WidgetDropDownList`（下拉列表）、`WidgetSearchBar`（搜索栏）、`WidgetColorIndicator`（颜色指示器）、`WidgetFileBrowserBase`（文件浏览器）。

**专用 GUI**: `GuiColorEditorHSV`（HSV 颜色编辑器，支持 RGB 输入和 Alpha 通道）、`GuiStringListEdit`（字符串列表编辑）、`GuiKeybindSettings`（热键高级设置）。

### 4. 渲染工具

`RenderUtils` 提供全面的 2D/3D 渲染辅助：
- 矩形绘制（填充/描边/渐变）
- 纹理矩形绘制
- 悬停工具提示渲染
- 方块碰撞箱轮廓/面绘制
- 线条批量绘制
- HUD 文本渲染
- 物品/物品栈渲染

`InventoryOverlay`: 多容器类型背景渲染（箱子、熔炉、酿造台、漏斗等），支持空槽位和物品堆叠效果。

`MessageRenderer`: 多消息显示管理，支持背景/边框颜色、位置、生命周期配置。

`ShaderProgram`: GLSL 着色器加载和编译工具。

### 5. 事件系统

| 处理器 | 接口 | 用途 |
|--------|------|------|
| `InitializationHandler` | `IInitializationHandler` | 模组初始化调度 |
| `RenderEventHandler` | `IRenderer` | 游戏覆盖层/世界后期渲染 |
| `TickHandler` | `IClientTickHandler` | 每游戏刻回调 |
| `WorldLoadHandler` | `IWorldLoadListener` | 世界加载/卸载前后回调 |
| `InputEventHandler` | `IKeybindManager` | 键盘/鼠标输入分发 |

### 6. 工具类集合

| 类 | 功能 |
|----|------|
| `StringUtils` | 颜色解析（0x/#前缀）、CamelCase 转换、翻译键查找 |
| `JsonUtils` | JSON 文件读写、安全类型获取、嵌套对象操作 |
| `BlockUtils` | 方块状态属性获取、全立方体检查 |
| `PositionUtils` | 坐标类型枚举、方向判断、包围盒计算 |
| `InventoryUtils` | 物品堆叠比较、容器物品列表、背包计数 |
| `FileUtils` | 配置目录获取、NBT 文件读取、文件名校验 |
| `Color4f` | RGBA 浮点颜色类，支持 int/float/hex 转换 |
| `LayerRange` | Y 轴层级范围管理（单层/范围/全部以上/以下） |
| `IntBoundingBox` / `SubChunkPos` | 坐标包围盒 |
| `UsageRestriction` | 黑白名单限制机制（Block/Item） |

### 7. 网络系统

- `ClientPacketChannelHandler` — 客户端自定义数据包通道注册
- `PacketSplitter` — 大包自动分割/重组（客户端→服务端最大 32KB，服务端→客户端最大 1MB）

### 8. 数据导出

`DataDump` — 多列数据表格工具，支持 CSV/ASCII 格式导出，列对齐、排序。

---

## 编译与安装

```bash
git clone https://github.com/marongqiang/malilib_MRQ.git
cd malilib_MRQ
./gradlew build
# 编译产物位于 build/libs/
```

**依赖**: Fabric Loader 0.14.21+, Fabric Loom 1.2

---

## 配置说明

配置文件位于 `config/malilib.json`。这个文件主要管理 malilib 自身的调试设置，一般不直接编辑。各 masa 模组的独立配置文件在 `config/<模组名>.json`。

---

## MRQ 分支改进

本分支基于上游 `LTS/1.20.1`，进行了系统性的 bug 修复和性能优化：

### 严重修复
| 问题 | 文件 | 修复内容 |
|------|------|---------|
| Alpha 通道损坏 | `GuiColorEditorHSV.java` | 编辑红色通道时掩码 `0x00FFFF` 修正为 `0xFF00FFFF`，避免 Alpha 被意外清零 |
| 文件浏览器路径遍历漏洞 | `WidgetFileBrowserBase.java` | `contains()` 字符串匹配改为 `getCanonicalPath() + startsWith()` 严格路径校验，防止 `../` 逃逸 |
| ConfigDouble 负值钳制 | `ConfigDouble.java` | 无界构造函数 `Double.MIN_VALUE`（最小正数）改为 `-Double.MAX_VALUE`，修复负值被错误钳制为 0 |

### 安全修复
| 问题 | 文件 | 修复内容 |
|------|------|---------|
| BufferedWriter 泄漏 | `DataDump.java` | try-with-resources |
| FileInputStream 泄漏 | `FileUtils.java` | try-with-resources |
| 除零风险 | `GuiScrollBar.java` | `handleDrag` 添加 `barTravel > 0` 检查 |
| InputStream 泄漏 | `ShaderProgram.java` | try-with-resources |

### 性能优化
| 优化 | 文件 | 效果 |
|------|------|------|
| 颜色正则缓存 | `StringUtils.java` | `Pattern.compile()` → `private static final PATTERN_COLOR_HEX` |
| 空 catch 日志 | `JsonUtils.java` 等 14 处 | 添加日志输出便于调试 |

---

## 许可证

LGPL-3.0

---

## 相关链接

- [Litematica MRQ](https://github.com/marongqiang/litematica_MRQ)
- [MiniHUD MRQ](https://github.com/marongqiang/minihud_MRQ)
- [Tweakeroo MRQ](https://github.com/marongqiang/tweakeroo_MRQ)
- [Item Scroller MRQ](https://github.com/marongqiang/itemscroller_MRQ)
- [Hotkey Wheel](https://github.com/marongqiang/hotkeywheel)
