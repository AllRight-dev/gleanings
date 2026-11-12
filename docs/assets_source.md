# 资产来源与许可

> 政策：只允许 A / B / D 三类来源。C 类（其他模组的贴图或美术）一律不用。
> 每新增一张图，在这里加一行。**没有记录 = 不能发布。**

| 级别 | 来源 | 状态 |
|---|---|---|
| **A** | vanilla 贴图派生（改编色板 / 结构），工具 `tools/recolor.py`、`tools/gen_texture.py` | ✅ 允许 |
| **B** | CC0 / CC-BY 素材包（OpenGameArt、Kenney 等） | ✅ 允许，必须记作者与许可 |
| **C** | 其他 mod 的贴图或美术 | ❌ 禁止，哪怕只是"派生" |
| **D** | 生成模型出图（即梦、Nano Banana 等） | ✅ 允许，必须记提示词与模型 |

> A 类的注记：Mojang 的资产条款不允许原样再分发，但「导入 vanilla 贴图 → 改色板/改结构 → 作为 mod 的独立美术」是社区长期通行的做法。
> 若将来要上架对资产要求严格的平台，A 类也需要替换为 B 类（CC0）或自绘。

---

## 记录

| 文件 | 级别 | 来源 / 派生脚本 | 备注 |
|---|---|---|---|
| `textures/item/washed_rotten_flesh.png` | A | vanilla `item/rotten_flesh.png` → `tools/gen_texture.py`（去饱和 42% + 顶部冷色 + 三颗水珠） | — |
| `textures/item/resonance_core.png` | A | vanilla `item/ender_pearl.png` → `tools/recolor.py`，目标色板取自 `item/amethyst_shard.png` | 早期版本派生自 Iron's Spellbooks（C 类），已废弃 |
| `textures/item/resonance_tome.png` | A | vanilla `item/enchanted_book.png` → `tools/recolor.py --colors "#33124f,#5a2190,#8a3ad0,#b45cf0,#d89bff,#f4e0ff"` | 六档显式色阶，比取色板更鲜艳 |
| `textures/item/resonance_dregs.png` | **D** | 豆包（doubao.com，2026-11-12）生成 8 张候选，取第 4 张 → `tools/img2mc.py --size 16 --colors 10 --key-bg --crop` | 提示词：`画一个 Minecraft 物品图标：一堆紫水晶碎渣。不是晶体、不是水晶簇、不是方块，是被砸碎后的不规则碎粒和碎屑，七八块大小混杂、边缘参差不齐，随意堆成一小撮。深紫到浅紫，个别碎粒有细小反光点。斜向摆放，占画面约 45%，四周留白，不对称。像素画风格，16x16 像素格，纯色块，无渐变、无锯齿、无描边、无投影，白色背景。` 原始候选在 `tools/ref/doubao/` |
| ~~`textures/item/resonance_dregs.png`（旧）~~ | ~~A~~ | `tools/compose_pile.py` 拼贴 `flint` + `amethyst_shard` | 废弃：拼接感太强、碎粒间距过大，不如生成版 |
| `tools/ref/**` | 参考 | vanilla 客户端 jar + 本地模组 jar | **不进分发 jar**，仅作派生输入与回归基线 |
| `tools/*.png` 对照图 | 工具产物 | 脚本生成 | 不进分发 jar |

## 复现

```bash
python tools/recolor.py --src tools/ref/v_ender_pearl.png --palette tools/ref/amethyst_shard.png \
    --out src/main/resources/assets/gleanings/textures/item/resonance_core.png --darken 1.1
python tools/recolor.py --src tools/ref/candidates/v_gunpowder.png --palette tools/ref/amethyst_shard.png \
    --out src/main/resources/assets/gleanings/textures/item/resonance_dregs.png --darken 0.95
```

`tools/ref/v_ender_pearl.png` 与 `tools/ref/candidates/v_gunpowder.png` 是从 vanilla 客户端 jar 里直接取出的，
不是模组资产 —— 所以它们属于 A 类输入的合法来源。
