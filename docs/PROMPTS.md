# 贴图提示词模板（实测校准版）

> 原稿是通用经验；这一版每条都对着本机 vanilla 客户端的真实像素校准过。
> 校准脚本：`tools/inspect_ref.py` / `tools/sprite_check.py` / `tools/tile_sheet.py`。

---

## 零、先记住一句

> **提示词决定上限，后处理决定下限。**

扩散模型交给你的是 512×512、带渐变和抗锯齿的图；Minecraft 要的是 16×16、≤12 色、alpha 二值。
中间必须有一步确定性转换（`tools/img2mc.py`），否则提示词写得再准也是废图。

---

## 一、物品类

**原稿**：pixel art item icon of [描述], vanilla Minecraft style, 16x16 pixel grid look, strong readable silhouette, transparent background, limited colour palette, no anti-aliasing, no smooth shading, flat lighting, drawn diagonally from bottom-left to top-right

**实测校准**：

| 原稿要素 | 实测结论 |
|---|---|
| `drawn diagonally from bottom-left to top-right` | ✅ **对**。vanilla `rotten_flesh` / `beef` / `leather` 主轴全是左下→右上 |
| `strong readable silhouette` | ⚠️ 不够，AI 会理解成「占满画面」。必须给数字 |
| `transparent background` | ✅ 对，且必须配合后处理的 alpha 阈值（半透明边会出白边） |

**补丁（把这两句加上）**：

```
occupies about 45% of the canvas with generous transparent margin,
off-centre composition, deliberately asymmetrical, no outline stroke, no drop shadow
```

依据：我第一版手绘的失败数据 —— 不透明像素 174（vanilla 119）、对称度 85.9%（vanilla 40.6%）。
**「填满 + 对称」是 AI 画物品的默认行为，必须显式否定掉。**

---

## 二、方块类（这里原稿有一条是错的）

**原稿**：seamless tileable pixel art texture of [材质], ..., fills the whole square edge to edge, no border, no text

**❌ 错的那条**：「原版方块贴图必须边缘无缝」。

实测：vanilla `stone.png` 的左右边缘列差异，**大于它内部 100% 的相邻列差异**。
也就是说，vanilla 石头在像素层面根本不无缝 —— 它靠的是**高频噪声密度均匀**，让眼睛锁不住接缝。

所以提示词里写 `seamless` 会引导模型去**画边框式的对称图案**（反而更像错的东西）。

**改写成**：

```
uniform high-frequency detail with no large-scale structure,
no vignette, no border, no gradient across the tile,
fills the canvas edge to edge
```

**验收方式也必须改**：不要用数字判据（我试了三种，全部校准失败，见 `AI-LOG.md` E07），
用 `tools/tile_sheet.py` 铺 4×4 看一眼 —— 接缝在 4×4 里要么一眼看得见，要么不存在。

![平铺检查](C:\mcdev\mods\gleanings\tools\tiling_sheet.png)

左边 vanilla 石头、右边纯噪声都铺得开；中间那条渐变，任何人一眼就能看出四条竖带。
**这三个里有两个会被我的数字指标判 100 分** —— 所以这个尺度上，眼睛才是判据。

---

## 三、生物 / 实体类

**原稿**：strictly cubic, no curves, no smooth surfaces, hard angular edges only, body composed of perfect cubes, rectangular prism limbs

**❌ 自相矛盾**：`body composed of perfect cubes`（正方体）和 `rectangular prism limbs`（长方体）打架。
而且 vanilla 生物的身体**没有一个是正方体** —— 都是不同比例的长方体。

**改写成**：

```
blocky voxel model, body is a single boxy cuboid with roughly 1:1:1.5 proportions,
limbs are elongated rectangular prisms of square cross-section,
no curves, no smooth surfaces, hard angular edges only,
visible grid of square faces, no subdivision surface
```

---

## 四、负面提示词（原稿基础上补三条）

原稿：`smooth, realistic, detailed, photorealistic, blurry, noise, anti-aliasing, soft shading, gradient, organic curves`

补：

```
ambient occlusion, drop shadow, outline stroke, glow, reflection, vignette
```

理由：前三个是 AI 画「游戏图标」时最爱的装饰，而 vanilla 物品贴图**一个都没有**。
`noise` 那条要小心 —— 方块类恰恰需要噪声，所以**方块模板里不要加 `noise`**，物品模板里才加。

---

## 五、五步流水线

```
1. 提示词出图（512×512，越干净越好）
2. python tools/img2mc.py raw.png out.png --size 16 --colors 11
3. python tools/sprite_check.py out.png        # 物品：五项硬指标
   python tools/tile_sheet.py out.png          # 方块：4x4 平铺，用眼睛看
4. 进游戏看 1:1 —— 快捷栏里那一格才是终审
5. 不满意就回第 1 步改提示词，不要在第 2 步手动修像素
```

第 5 步很关键：**手修像素会让资产无法从提示词复现**，下次换模型就全丢。
