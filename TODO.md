# TODO
1. 构建实时生成的音频管线（已完成）
2. 使用440Hz正弦波测试实时音频管线（已完成）
3. 移植vvvf-sim算法层和线电压波形处理（已完成）
4. vvvf-sim列车属性映射机械动力列车属性（已完成）
5. 临时硬编码调制策略，保证最小可听实践（已完成）
6. 继续移植列车走行音滤波模块与齿轮/谐波声音（正在做）
7. 构建GUI，在游戏内配置vvvf策略
8. 继续移植原vvvf-sim更多可配置属性
9. 移植解析外部yaml配置模块，实现vvvf-sim配置互通
10. （Opt.）在列车站《组装列车》界面为每辆车单独配置策略
11. （Opt.）列车控制改为功率控制
12. （Opt.）修改其他生物的驾驶逻辑（GoA3/4）

一、由于实时音频管线走的不是Minecraft的声音注册系统<br>
因此需要主动同步实现一些Minecraft声音系统的相关特性<br>
二、vvvf包下的代码移植自vvvf-sim（目录结构，变量等保持一致）<br>
原项目相关文档可见[https://deepwiki.com/intel713/VVVF-Simulator](https://deepwiki.com/intel713/VVVF-Simulator)