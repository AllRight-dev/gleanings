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
| `textures/item/resonance_dregs.png` | A | `tools/compose_pile.py`：vanilla `flint` + `amethyst_shard` 裁成碎片拼成一堆，套紫水晶色板 | 生成模型对"碎渣"无稳定先验（画成水晶簇或 3D 立方体），故改用拼贴 |
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
