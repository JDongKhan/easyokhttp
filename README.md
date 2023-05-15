# easyokhttp

早些年搞B端物联网项目，使用了Android原生开发，所以就简单实现了一个快速使用okhttp的库。

虽然现在主要工作内容是跨平台，由于这个使用的场景比较多，在做跨平台组件的时候也用的着，所以整理库存代码时就上传上来作为一个备份以免丢失。


### 使用方式

```java

EasyHttp.post().url("https://baidu.com").retryCount(2).params(map).build().enqueue();

```
