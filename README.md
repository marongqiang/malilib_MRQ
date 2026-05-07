arne branch warning
===================
**Do not base any external work on these arne branches!!
These are temporary features added for specific mods' needs, before the "upstream"
(via master branch and then ported to the later MC versions) starts supporting these features
properly. These arne branches can/will get rebased and force pushed without mercy when needed.**


malilib
==============
malilib is a library mod used by masa's LiteLoader mods. It contains some common code previously
duplicated in most of the mods, such as multi-key capable keybinds, configuration GUIs etc.

## MRQ 修复

本分支基于上游代码进行了以下 bug 修复和性能优化：

### 严重修复
- **Alpha 通道损坏**: `GuiColorEditorHSV` 编辑红色通道时掩码 `0x00FFFF` 修正为 `0xFF00FFFF`，避免 Alpha 被清零
- **文件浏览器路径遍历漏洞**: `WidgetFileBrowserBase.switchToParentDirectory()` 中 `contains()` 字符串匹配改为 `getCanonicalPath()` + `startsWith()` 严格路径校验
- **ConfigDouble 负值钳制**: 无界构造函数中 `Double.MIN_VALUE`（最小正数）改为 `-Double.MAX_VALUE`，修复负值被错误钳制为 0

### 安全修复
- **I/O 资源泄漏**: `DataDump.dumpDataToFile()` 的 BufferedWriter 和 `FileUtils.readNBTFile()` 的 FileInputStream 改为 try-with-resources
- **除零保护**: `GuiScrollBar.handleDrag()` 添加 `barTravel > 0` 检查
- **ShaderProgram**: InputStream/BufferedReader 改为 try-with-resources

### 性能优化
- `StringUtils.getColor()` 中 `Pattern.compile()` 提取为 `private static final PATTERN_COLOR_HEX`
- 多个 JsonUtils 空 catch 块添加 MaLiLib.logger 日志输出

Compiling
=========
* Clone the repository
* Open a command prompt/terminal to the repository directory
* run 'gradlew build'
* The built jar file will be in build/libs/