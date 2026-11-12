# AI 错题本

> 记录这个项目里 AI 每一次真实的失误：现象 / 根因 / 怎么被抓到 / 怎么修。
> 分类固定五类：① 编造 API ② 改了 A 忘了 B ③ 路径 / 大小写 / 编码 ④ 过度自信 ⑤ 无视已说过的约束。

---

## E01 · ③ 路径 —— PowerShell 变量名大小写不敏感，`$D` 被循环变量覆盖

**现象**：`mkdir /c/mcdev/mods/gleanings` 之后，`Copy-Item` 报错
`未能找到路径 "...\default-workspace\docs\gradle\wrapper"` —— 文件被复制到工作区里去了。

**根因**：脚本里先写了 `$D='C:\mcdev\mods\gleanings'`，后面又写
`foreach ($d in @(...))`。PowerShell 变量名**大小写不敏感**，`$d` 直接覆盖了 `$D`，
于是所有 `Join-Path $D ...` 都变成了循环里最后一个元素 `docs`。

**怎么被抓到**：报错信息里的路径是工作区相对路径，而不是 `C:\mcdev\...`，一眼不对。

**修法**：变量名不要用单字母。改成 `$Src` / `$Dest` / `$sub`，并且循环变量和路径变量永不同名。

---

## E02 · ④ 过度自信 —— 假设「danger-full-access」对所有执行通道都生效

**现象**：`ops_bash` 里 `mkdir /c/mcdev/mods/gleanings` → `Permission denied`；
逐目录探测后发现只有工作区可写。

**根因**：把「会话的文件策略」当成了「每个工具的沙箱策略」。
`ops_bash` 有自己的沙箱，只写工作区；而 `write` / `pwsh` 走另一条通道，写 `C:\mcdev` 没问题。

**怎么被抓到**：`mkdir` 直接失败；随后用一个 `touch` 探测矩阵确认了边界。

**修法**：先花一次调用探清「哪个通道能写哪里」，再决定用哪个工具。
本项目最终分工：**只读探测用 `ops_bash`，写文件用 `write`，跑命令 / 拷二进制 / git 用 `pwsh`。**

---

## E03 · ④ 过度自信 —— 假设 `AllRight-dev` 是 GitHub 组织

**现象**：`POST /orgs/AllRight-dev/repos` → `404 未找到`。

**根因**：看到仓库地址是 `AllRight-dev/irons-addon`，就假设它是组织。
实际上 `AllRight-dev` 是**用户账号**，仓库挂在用户下。

**怎么被抓到**：API 直接 404，而不是 403（403 才是权限问题，404 通常是「这个路径不存在」）。

**修法**：建仓先试 `/orgs/...`，失败立刻 fallback 到 `/user/repos`，并把结果里的
`full_name` 打出来确认归属，而不是硬编码。

---

## E04 · ⑤ 无视约束 —— 把「我用 AI 做 mod」读成「在 mod 里做 AI」

**现象**：连续两轮方案（《夜邮》村民书信 + LLM 回信）整个建立在错误前提上，被用户一句话推翻。

**根因**：用户说「我真实目的是结合我的兴趣学习 AI」，AI 把它解成了
「在游戏内容里加入 AI 功能」；用户的真实意思是
**「让 AI 来做这个 mod，我在旁边顺便摸清 AI 是怎么回事」** —— AI 是作者，不是内容。

**怎么被抓到**：用户直接纠正：「我的意思不是让玩家学 ai，而是我让 ai 做 mod 顺便让我熟悉 ai」。

**修法**：**听到「学习 X」这类元目标时，先复述一遍再动手。**
「学习 X」至少有三种读法：① 把 X 做成内容 ② 用 X 干活 ③ 研究 X 本身。先问一句是哪一种，比做完再推翻便宜一百倍。

---

## E05 · ③ 编码 —— Windows `core.autocrlf=true` 会把 `gradlew` 搞成 CRLF，Linux CI 直接挂

**现象**：`git add` 时满屏 `LF will be replaced by CRLF` 警告。

**根因**：Windows 上 `core.autocrlf=true`，检出时 CRLF。`gradlew` 是 shell 脚本，
到 Linux CI 上会报 `env: 'sh\r': No such file or directory` —— 这是最常见的一种「本地好好的，CI 全红」。

**怎么被抓到**：**没有翻车** —— 在推送前主动加了 `.gitattributes` 拦住：
`gradlew text eol=lf` / `*.bat text eol=crlf` / `*.png binary`。

**这个案例的意义**：错题本不只记翻车，也记「差一点翻车」。**提前拦住的坑，价值和方法论一样高。**

---

# 待观察（还没翻，但我知道它可能翻）

- `data/gleanings/recipe/*.json` 里 `"result": {"id": ..., "count": 1}` 是 1.20.5+ 的写法。
  如果 1.21.1 的 `SmokingRecipe` 还认旧格式，游戏里这条配方会静默失效（不崩，但烟熏炉不认）。
- `Level#getMoonPhase()` / `Level#isThundering()` 的签名是凭记忆写的，CI 编译通过说明存在，
  但**编译通过 ≠ 语义对**（比如月相 0 到底是满月还是新月）。
