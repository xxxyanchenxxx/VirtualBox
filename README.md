[![Build Status](https://travis-ci.org/tinycode1/VirtualBox.svg)](https://github.com/tinycode1/VirtualBox)

简介
-----
**VirtualBox** 是基于济宁市罗盒网络科技有限公司VirtualApp的2017年12月开源代码[VirtualApp](https://github.com/asLody/VirtualApp) 和 [FreeReflection](https://github.com/tiann/FreeReflection) 在**非ROOT**环境下实现App双开运行（支持5.0~11.0)。

本项目发布符合GPL3.0协议及任何符合第7条的附加条件

本项目同时也遵循[GPL-3.0 License](https://github.com/tinycode1/VirtualBox/blob/main/LICENSE)，进行开源

VirtualBox在你的App内创建一个`虚拟空间`，你可以在虚拟空间内任意的`安装`、`启动`和`卸载`APK，这一切都与外部隔离，如同一个`沙盒`。

运行在VirtualBox中的APK无需在外部安装，即支持**免安装运行APK**。


警告
-------
本项目使用的[VirtualApp](https://github.com/asLody/VirtualApp)开源版本已经过时，如果有需求，为了贵公司的长期稳定发展，请使用Virtual App商业版，联系Lody(imlody@foxmail.com)即可。


修改声明
---------
1. 修改包名以及部分类名
2. 优化后台启动Activity限制
3. 适配支持Android 10,11
4. 支持Split应用双开
5. 支持多开独占式多开，节省内存和电量
6. 修复双开环境中其他App通过微信登录问题
7. 优化64位插件不依赖主包代码
8. 优化机型模拟配置

修改日期
---------
2018年10月

致谢
------
1. [VirtualApp](https://github.com/asLody/VirtualApp)
2. [FreeReflection](https://github.com/tiann/FreeReflection)
