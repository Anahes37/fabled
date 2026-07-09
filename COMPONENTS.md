# Fabled 常用组件精确选项参考

基于你的实际技能文件 + 编辑器源码提取。每个组件的 `data` 字段都能用 `{key}-base` 和 `{key}-scale` 格式。

## Triggers (触发器)

### Cast
```yaml
# 节点名: Cast-a, Cast-0 等
data: {}  # 无额外选项
```
玩家用 `/cast 技能名` 或 combo 按键时触发。

### Kill
```yaml
data:
  type: 'Killer'              # Killer | Victim
  # Killer: 施法者为击杀者; Victim: 施法者为被击杀者
```
有人死亡时触发（可以是施法者击杀别人，也可以是施法者被杀）。

### Death
```yaml
data: {}  # 无额外选项
```
玩家死亡时触发。

### Left Click / Right Click
```yaml
data:
  type: 'Both'  # Both | Air | Block
  # 点击空气或方块
  crouching: 'false'  # 是否需要蹲下
```

### Physical Damage
```yaml
data:
  target: 'true'     # true=children target the caster; false=children target the damaged entity
  type: 'Both'       # Both | Melee | Projectile
  dmg-min-base: 0
  dmg-max-base: 999
```
玩家造成物理伤害（近战/弓箭）时触发。

### Took Physical Damage
```yaml
data:
  type: 'Both'       # Both | Melee | Projectile
  dmg-min-base: 0
  dmg-max-base: 999
```
玩家受到物理伤害时触发。

### Took Skill Damage
```yaml
data:
  type: 'Both'       # Both | Melee | Projectile
  dmg-min-base: 0
  dmg-max-base: 999
```
玩家受到技能伤害时触发。

### Crouch
```yaml
data:
  type: 'Both'       # Both | Crouch | Uncrouch
```
蹲下/起身时触发。

### Consume
```yaml
data:
  type: 'Both'       # Both | Eat | Drink
  item: 'Any'        # 具体物品类型
```
吃/喝物品时触发。

### Block Break
```yaml
data:
  type: 'Both'       # Both | Allow | Deny
  # Allow=允许破坏; Deny=阻止破坏
```
方块被破坏时触发。`Allow` 是破坏成功之后触发，`Deny` 是破坏被阻止前触发。

---

## Targets (目标选择器)

### Self
```yaml
data: {}  # 无额外选项
```
只选择施法者自己。

### Chain (NEW - 连锁弹跳)
```yaml
data:
  range-base: 5          # 每一跳的最大距离（格）
  range-scale: 0
  bounces-base: 3        # 最大弹跳次数（每个弹跳 = 1 个新目标）
  bounces-scale: 0
  unique: 'true'         # true = 不重复命中同一目标; false = 可以弹回之前的目标
  group: 'Enemy'         # Ally | Enemy | Both
  wall: 'false'          # 是否穿墙
  caster: 'False'        # True | False | In area
  invulnerable: 'false'  # 是否包含无敌目标
```
**连锁闪电专用目标选择器**。从第一个 target 出发，在 range 半径内找最近的未被命中过的敌人，命中后移动到该敌人位置继续找下一个，重复 bounces 次。返回所有弹跳到的目标 → 子组件（Damage 等）对每个生效。

**链锁闪电示例**：
```yaml
Cast-a:
  type: 'trigger'
  children:
    Single-b:
      type: 'target'       # 先选中初始目标
      data: { group: 'Enemy', range-base: 8 }
      children:
        Chain-c:
          type: 'target'   # 从初始目标弹跳 5 次
          data:
            range-base: 5
            bounces-base: 5
            group: 'Enemy'
            unique: 'true'
          children:
            Damage-d:      # 每个弹到的目标都受到伤害
              type: 'mechanic'
              data: { type: 'Damage', value-base: 15, value-scale: 3 }
            Particle Animation-e:
              type: 'mechanic'
              data:
                particle: 'Spell'
                arrangement: 'Single'
                particles-base: 5
                speed: '0'
```

### Area
```yaml
data:
  radius-base: 5       # 半径（格）
  radius-scale: 0
  group: 'Enemy'       # Ally | Enemy | Both
  wall: 'false'        # 是否穿墙
  caster: 'false'      # 是否包含施法者
  invulnerable: 'false' # 是否包含无敌目标
  max-base: 99         # 最大目标数
  max-scale: 0
```
以当前目标为圆心，选择半径内所有实体。

### Linear
```yaml
data:
  range-base: 5            # 射程（格）
  tolerance-base: 2        # 宽度（格，选偏离中心线多远的目标）
  group: 'Enemy'           # Ally | Enemy | Both
  wall: 'true'             # 是否穿墙
  caster: 'false'
  invulnerable: 'false'
  max-base: 99
```
选择施法者面前一条直线上的目标。

### Offset
```yaml
data:
  forward-base: 0    # 前方偏移
  upward-base: 0     # 上方偏移
  right-base: 0      # 右方偏移
  group: 'Enemy'
  wall: 'false'
  caster: 'false'
  invulnerable: 'false'
```
选择一个目标位置偏移点（通常用于 Delay 后或在特定位置放粒子/爆炸）。

### Single
```yaml
data:
  range-base: 5       # 射程
  group: 'Enemy'
  wall: 'false'
  caster: 'false'
  invulnerable: 'false'
```
选择玩家视线方向指向的单个目标。

---

## Conditions (条件)

### Skill Level
```yaml
data:
  skill: '技能名'        # 要检查的技能名称
  min-level: '1'         # 最低等级（包含）
  max-level: '9'         # 最高等级（包含）
```
检查指定技能的当前等级是否在范围内。这是你实现"等级分段效果"的核心组件——不同等级段可以有不同的 children。

### Health
```yaml
data:
  type: 'Percent'               # Health | Percent | Difference | Difference Percent
  min-value-base: 0             # 最低血量/百分比
  max-value-base: 100           # 最高血量/百分比
```
检查目标血量。Percentage 类型时，min/max 是百分比（0-100）。

### Chance
```yaml
data:
  chance-base: 50       # 概率 (0-100)
  chance-scale: 0
```
随机概率。百分制（50 = 50%）。

### Distance
```yaml
data:
  min-base: 0        # 最小距离
  max-base: 999      # 最大距离
```
检查目标与施法者之间的距离。

### Mana
```yaml
data:
  type: 'Percent'      # Percent | Flat
  min-base: 0
  max-base: 100
```
检查施法者法力值。

### Class Level
```yaml
data:
  min-base: 1
  max-base: 100
```
检查玩家职业等级。

### Crouch
```yaml
data:
  type: 'Crouching'   # Crouching | Standing
```
检查目标是否在蹲下。

### Else
```yaml
data: {}  # 无选项
```
永远通过。放在其他 Condition 下面作为"ElseCondition"节点，在条件不满足时执行。这是实现 if/else 分支的核心。

### Flag
```yaml
data:
  flag: 'my_flag'
  type: 'Set'         # Set | Not Set
```
检查指定标记是否已设置。

---

## Mechanics (机制/效果)

### Damage
```yaml
data:
  counts: 'true'
  type: 'Damage'         # Damage | Multiplier | Percent | Percent Left | Percent Missing | True
  value-base: 10          # 伤害值（Damage模式=固定值; Multiplier=倍率; Percent=最大生命百分比）
  value-scale: 2
  true: 'false'           # 是否真实伤害（忽略护甲）
  classifier: 'default'   # 伤害分类
  knockback: 'false'      # 是否带击退
  ignore-divinity: 'false' # 是否忽略无敌
  cause: 'Custom'         # 伤害原因（Entity Attack, Custom 等）
```
**type 详解**:
- `Damage`: 固定伤害值。value = 伤害值
- `Multiplier`: 伤害倍率。value = 武器攻击力的倍率（如 2.5 = 250% 武器伤害）
- `Percent`: 目标最大生命的百分比。value = 百分比数
- `Percent Left`: 目标剩余生命的百分比
- `Percent Missing`: 目标已损失生命的百分比

### Heal
```yaml
data:
  counts: 'true'
  type: 'Health'         # Health | Percent
  value-base: 10          # 治疗量
  value-scale: 2
```
治疗目标。Percent 模式时 value 是最大血量的百分比。

### Particle Animation
```yaml
data:
  counts: 'true'
  particle: 'Dust'               # 粒子类型（Flame, Dust, Lava, Spell, Heart, Portal 等）
  arrangement: 'Sphere'          # Sphere | Circle | Single | Line 等
  dust-color: '#FF0033'          # Dust 粒子的颜色（仅 Dust 类型）
  dust-size: '1'                 # Dust 粒子大小
  steps: '1'                     # 动画步数
  frequency: '0.05'              # 每步的时长（秒）
  angle: '0'                     # 起始角度
  start: '0'                     # 结束角度
  duration-base: 0.2            # 总持续时间
  h-translation-base: 0         # 水平平移
  v-translation-base: 0         # 垂直平移
  h-cycles: '1'                  # 水平循环次数
  v-cycles: '1'                  # 垂直循环次数
  radius-base: 1.5              # 半径
  particles-base: 20            # 粒子数
  visible-radius: '25'           # 可见距离
  dx: '0'                        # X偏移
  dy: '0'                        # Y偏移
  dz: '0'                        # Z偏移
  forward: '0'                   # 前方偏移
  upward: '1.2'                  # 上方偏移
  right: '0'                     # 右方偏移
  -with-rotation: 'false'        # 是否随目标旋转
  direction: 'XY'                # 方向（Circle 时用）
  amount: '1'                    # 粒子数量乘数
  speed: '0.1'                   # 粒子速度
```

### Delay
```yaml
data:
  counts: 'true'
  delay-base: 0.5             # 延迟秒数
  cleanup: 'true'             # 登出/中断时是否清除
  single-instance: 'false'    # 是否只允许一个实例（true=新的会取消旧的）
```
延迟执行子组件。`single-instance: 'true'` 可防止技能刷多段伤害。

### Sound
```yaml
data:
  counts: 'true'
  sound: 'Entity wither break block'    # 音效名
  custom: ''                            # 自定义资源包音效（sound 选 Custom 才生效）
  volume-base: 100           # 音量 (100=最大)
  pitch-base: 1              # 音调 (0.5-2)
```
播放音效。`sound: 'Custom'` 时可通过 `custom` 字段写自定义 key。

### Potion
```yaml
data:
  counts: 'true'
  potion: 'Slow'             # 药水类型（Slow, Blindness, Poison, Weakness, Regeneration 等）
  ambient: 'true'            # 是否显示环境粒子
  tier-base: 1               # 等级（0=I级, 1=II级, 2=III级...）
  seconds-base: 5            # 持续秒数
```
给药水效果。

### Launch
```yaml
data:
  counts: 'true'
  relative: 'Target'         # Target | Caster | Between
  reset-y: 'false'           # 是否重置Y轴速度
  forward-base: 0            # 前方速度
  upward-base: 2             # 上方速度
  right-base: 0              # 右方速度
```
发射/击飞目标。

### Push
```yaml
data:
  counts: 'true'
  type: 'Fixed'              # Fixed | Inverse | Scaled
  speed-base: 3              # 速度（正=推，负=拉）
  source: 'none'             # Remember Targets 的 key，或 'none'=施法者
```
推/拉目标。

### Message
```yaml
data:
  counts: 'true'
  message: '&cHello!'        # 消息内容
  targets: 'Target'          # Target | Caster | Everyone | World
```
发送聊天栏消息。支持颜色代码（&a, &c, &6 等）。

### Explosion
```yaml
data:
  counts: 'true'
  power-base: 4              # 爆炸威力
  fire: 'false'              # 是否产生火焰
  block-damage: 'false'      # 是否破坏方块
```
创建爆炸。

### Fire
```yaml
data:
  counts: 'true'
  ticks-base: 60             # 着火tick数（20tick=1秒）
```
让目标着火。

### Flag
```yaml
data:
  flag: 'my_flag'            # 标记名
  seconds-base: 10           # 持续秒数（可选）
```
设置标记。标记可被 Flag Condition 检查，或被 Flag Clear 清除。

### Value Set
```yaml
data:
  counts: 'true'
  key: 'my_value'            # 变量名
  value-base: 10             # 值
  type: 'Set'                # Set | Add | Multiply | Divide
  target: 'Caster'           # Caster | Target | Global
  min-base: 0                # 最小值
  max-base: 100              # 最大值
```
设置/修改数字变量。其他变体：`Value Add`, `Value Multiply`, `Value Divide`, `Value Random`, `Value Attribute`, `Value Health`, `Value Mana`, `Value Distance`, `Value Copy`。

### Mana
```yaml
data:
  counts: 'true'
  type: 'Flat'               # Flat | Percent | Percent Left | Missing Percent
  value-base: 10              # 法力值
```
修改施法者法力值。

### Cleanse
```yaml
data:
  counts: 'true'
  type: 'All Negative'       # All Positive | All Negative | All | Specific
```
移除药水效果。

### Command
```yaml
data:
  counts: 'true'
  command: 'say hello'       # 控制台命令
  type: 'Console'            # Console | Player
```
执行命令（通常用 Console 模式）。`{player}` 可替换为施法者名，`{target}` 为目标名。

### Status
```yaml
data:
  counts: 'true'
  type: 'Stun'               # Stun | Silence | Invincible | Disarm | Root 等
  seconds-base: 3            # 持续秒数
```
施加控制效果。常见类型：Stun=眩晕, Silence=沉默, Invincible=无敌, Root=定身。

### Repeat
```yaml
data:
  counts: 'true'
  times-base: 3              # 重复次数
  delay-base: 0.5            # 每次间隔（秒）
```
重复执行子组件。

### Remember Targets
```yaml
data:
  key: 'my_targets'          # 记住的名字
  type: 'Add'                # Add | Replace | Remove
  forget-after: 'false'      # 执行完子组件后是否忘记
```
记住当前目标。后续用 `Push(remember-targets: 'my_targets')` 或 `Forget Targets` 处理。

### Forget Targets
```yaml
data:
  key: 'my_targets'          # 要忘记的目标组名
```

### Trigger
```yaml
data:
  trigger: 'my_trigger'      # 要触发的触发器名
```
触发技能树中另一个触发器节点。

### Cancel
```yaml
data:
  type: 'Cast'               # Cast | Channel | All
```
取消技能施放/引导。

### Projectile
```yaml
data:
  counts: 'true'
  type: 'Arrow'              # Arrow | Snowball | Egg | Ender Pearl 等
  speed-base: 2              # 速度
  gravity: 'true'            # 是否有重力
  damage-base: 0             # 伤害
```
发射弹射物。

### Wolf
```yaml
data:
  counts: 'true'
  type: 'Spawn'              # Spawn | Remove
  name: '&cWolf'             # 狼的名字
  health-base: 20            # 生命值
  damage-base: 5             # 伤害
```
生成/移除狼。

### Armor
```yaml
data:
  counts: 'true'
  slot: 'Helmet'             # Helmet | Chestplate | Leggings | Boots
  material: 'Iron Helmet'    # 装备材料
  duration-base: 10          # 持续秒数（0=永久直到脱下）
```
临时装备。

### Cooldown
```yaml
data:
  counts: 'true'
  seconds-base: 5            # 冷却秒数
  type: 'Add'                # Add | Set | Remove
  skill: ''                  # 技能名（空=当前技能）
```

---

## 通用 data 字段

所有 mechanic 都有这两个通用字段：
```yaml
counts: 'true'        # 是否计入命中次数（影响 mana per hit 等属性计算）
icon-key: ''          # 编辑器用，游戏内忽略
```
