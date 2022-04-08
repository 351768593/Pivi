# Pivi (Picture Viewer)

## 此为何轮

这是一个简简单单图片查看器的轮子.

<table>
<tr>
    <td rowspan="4"><img src="doc/icon_pivi.png" alt="icon"/></td>
    <td><a href="doc/manual.md">manual</a></td>
</tr>
<tr>
    <td><a href="doc/changelog.md">changelog</a></td>
</tr>
<tr>
    <td><a href="doc/roadmap.md">roadmap</a></td>
</tr>
<tr>
    <td><sub>&lt;&ensp;&ensp;<i>你看这算不算个logo?</i></sub></td>
</tr>
</table>


## 何出此轮

Windows 自带的 Photos 和 Telegram 自带的图片查看器在查看大尺寸图片的时候 **巨 糊 无 比**,

也不想专门下个什么软件, 就顺手做了这个.

~~其实主要还是造轮子欲望高涨~~

## 使辙何来

先进 Java 17 + 中古 Swing, 很奇妙吧?

.jar 版本使用 GraalVM 编译打包; .exe 版本使用对应的 jpackage 打包.

~~本来想用 GraalVM 的 native-image 打包, 但是那玩意只能用 fallback 模式打包基于 Swing 的程序, 没法做到完全脱离 JDK, 哭哩哭哩~~

## 使辙何往

自己想怎么用就怎么改.

~~咕咕咕 咕咕咕~~
