# Fabled Skill Writing Guide

This document is an AI-friendly reference for writing and designing skills for the Fabled Minecraft plugin.
Use it to generate valid YAML skill configurations.

## Skill YAML Structure

Each skill file contains one or more skills, each as a top-level map entry:

```yaml
SkillName:
  name: 'Display Name'
  type: ''                       # Usually empty or 'Dynamic'
  max-level: 5                   # Max skill level (integer)
  skill-req: ''                  # Required skill name, or empty
  skill-req-lvl: 0               # Required skill level (0 = any)
  needs-permission: false         # Requires permission node
  cooldown-message: true          # Show cooldown message
  msg: '&6{player} &2has cast &6{skill}'
  combo: ''                      # Left/Right click combo pattern
  icon: 'Paper'                  # Item material for skill icon
  icon-data: 0                   # Custom model data
  icon-lore:                     # Icon lore lines
    - '&d{name} &7({level}/{max})'
    - '&2Type: &6{type}'
    - '{req:level}Level: {attr:level}'
    - '{req:cost}Cost: {attr:cost}'
    - '&2Mana: {attr:mana}'
    - '&2Cooldown: {attr:cooldown}'
  attributes:                    # Level-scalable attributes
    level-base: 1
    level-scale: 0
    cost-base: 1
    cost-scale: 0
    cooldown-base: 0
    cooldown-scale: 0
    mana-base: 0
    mana-scale: 0
    points-spent-req-base: 0
    points-spent-req-scale: 0
  components:                    # The skill tree - REQUIRED
    NodeName:
      type: 'trigger'            # trigger / target / mechanic / condition
      data: { ... }
      children:
        ChildNode:
          type: 'mechanic'
          data: { ... }
```

## Attribute Scaling

All numeric values use `{key}-base` and `{key}-scale`:
```
effective_value = base + (skill_level - 1) * scale
```

## Component Tree Architecture

Each skill is a tree of components. Execution flows top-to-bottom through children:

```
Trigger (entry point)
  └── Target (selects entities)
       └── Condition (filters targets)
            ├── Mechanic (actions on passing targets)
            └── ElseCondition (actions on failing targets)
```

### Special Component Names

| Name | Purpose |
|------|---------|
| `Cast-a`, `Cast-b`, etc. | Runs when player casts the skill (`/cast SkillName`) |
| `Initialize-a` | Runs when skill is first unlocked |
| `Cleanup-a` | Runs when skill is removed |
| Any other name | Becomes an event-listening trigger |
| `ElseCondition` | Child of a Condition; executes when condition fails |

---

## All Triggers (50 types)

Trigger name goes after a dash: `Kill-a`, `Death-b`, `Left Click-c`

| Component Name | Fires When |
|---|---|
| `Air` | Player is in the air |
| `Armor Equip` | Player equips armor |
| `Attribute Change` | A dynamic attribute changes |
| `Block Break` | Player breaks a block |
| `Block Place` | Player places a block |
| `Chat` | Player sends chat message |
| `Left Click` | Player left-clicks |
| `Right Click` | Player right-clicks |
| `Consume` | Player consumes an item |
| `Crouch` | Player crouches |
| `Death` | Player dies |
| `Drop Item` | Player drops an item |
| `Entity Resurrect` | Entity resurrects |
| `Entity Target` | Entity targets something |
| `Environment Damage` | Player takes environmental damage |
| `Experience` | Player gains experience |
| `Fishing Bite` | Fish bites on fishing rod |
| `Fishing Fail` | Fishing fails |
| `Fishing` | Fish is caught |
| `Fishing Grab` | Fishing grab event |
| `Fishing Ground` | Fishing ground event |
| `Fishing Reel` | Player reels in |
| `Flag` | A flag is set on the player |
| `Flag Expire` | A flag expires |
| `Flight Toggle` | Player toggles flight |
| `Glide` | Player glides with elytra |
| `Harvest` | Player harvests crops |
| `Heal` | Player is healed |
| `Item Swap` | Player swaps items |
| `Jump` | Player jumps |
| `Kill` | Player kills an entity |
| `Land` | Player lands from fall |
| `Launch` | Player is launched |
| `Move` | Player moves |
| `Physical Damage` | Player deals physical damage |
| `Took Physical Damage` | Player takes physical damage |
| `Projectile Hit` | Player's projectile hits |
| `Projectile Tick` | Projectile ticks |
| `Riptide` | Player uses Riptide |
| `Shear` | Player shears |
| `Shield` | Player blocks with shield |
| `Signal` | Signal received |
| `Skill Cast` | Another skill is cast |
| `Skill Damage` | Player deals skill damage |
| `Skill Downgrade` | Skill level decreases |
| `Skill Upgrade` | Skill level increases |
| `Sprint` | Player sprints |
| `Strip Log` | Player strips a log |
| `Took Skill Damage` | Player takes skill damage |
| `World Change` | Player changes world |

---

## All Targets (10 types)

| Component Name | Description |
|---|---|
| `Self` | Targets the caster |
| `Single` | Targets the entity the player is looking at |
| `Area` | Targets all entities in a radius |
| `Cone` | Targets entities in a cone in front of player |
| `Linear` | Targets entities along a line |
| `Nearest` | Targets the nearest entity |
| `Location` | Targets a stored location |
| `Offset` | Targets at an offset |
| `Remember` | Targets stored in memory |
| `World` | Targets all entities in the world |

### Target Data Options

| Key | Values | Description |
|-----|--------|-------------|
| `group` | `Ally`, `Enemy`, `Both` | Who to target |
| `wall` | `true`/`false` | Target through walls |
| `caster` | `true`/`false` | Include the caster |
| `max` | number | Max targets |
| `range` | number | Range in blocks |
| `radius` | number | Radius for Area/Cone |
| `angle` | number | Angle for Cone |
| `tolerance` | number | Tolerance |

---

## All Conditions (56 types)

Conditions filter targets. Only targets that pass the condition proceed to children.
Use `ElseCondition` as a child to handle failing targets.

### Player State Conditions
| Name | Checks |
|------|-------|
| `Air` | Player's remaining air |
| `Altitude` | Player's Y coordinate |
| `Armor` | Player's armor piece |
| `Attack Indicator` | Attack indicator value |
| `Attribute` | Dynamic attribute value |
| `Blocking` | If player is blocking |
| `Burning` | If player is on fire |
| `Cast Level` | Current skill cast level |
| `Ceiling` | If block above player |
| `Class` | Player's class name |
| `Class Level` | Player's class level |
| `Color` | Team color |
| `Combat` | If player is in combat |
| `Crouch` | If player is crouching |
| `Direction` | Player's facing direction |
| `Distance` | Distance between entities |
| `Elevation` | Elevation difference |
| `Fire` | Fire ticks remaining |
| `Flag` | If a flag is set |
| `Food` | Food level |
| `Glide` | If player is gliding |
| `Ground` | If player is on ground |
| `Health` | Health amount |
| `Light` | Light level |
| `Mana` | Mana amount |
| `Money` | Money (Vault) |
| `Moon` | Moon phase |
| `Mounted` | If riding something |
| `Mounting` | If being ridden |
| `Offhand` | Offhand item check |
| `Permission` | Has permission |
| `Potion` | Potion effect active |
| `Skill Level` | A skill's level |
| `Sprint` | If player is sprinting |
| `Status` | Status effect |
| `Time` | World time |
| `Water` | If in water |
| `Weather` | Current weather |
| `World` | World name |
| `Yaw` | Player's yaw angle |

### Target/Entity Conditions
| Name | Checks |
|------|-------|
| `Entity Type` | Entity type match |
| `Name` | Entity name match |
| `Lore` | Item lore match |
| `Mythicmob Type` | MythicMobs type |

### Logic/Utility Conditions
| Name | Checks |
|------|-------|
| `Chance` | Random percentage |
| `Else` | Always passes (used as ElseCondition fallback) |
| `Tree Node` | TreeNode value |
| `Value` | Dynamic value comparison |
| `Value Text` | String value comparison |

### World/Block Conditions
| Name | Checks |
|------|-------|
| `Biome` | Current biome |
| `Block` | Block type at location |

### Item/Inventory Conditions
| Name | Checks |
|------|-------|
| `Inventory` | Inventory contents |
| `Item` | Held item check |
| `Action Bar` | Action bar text |
| `Slot` | Inventory slot |
| `Tool` | Tool type in hand |

---

## All Mechanics (84 types)

Mechanics perform actions. They are the "effect" part of a skill.

### Combat Mechanics
| Name | Description |
|------|-------------|
| `Damage` | Deal damage to targets |
| `Damage Buff` | Buff damage output |
| `Damage Lore` | Damage scaling from lore |
| `Defense Buff` | Buff damage resistance |
| `Heal` | Heal targets |
| `Health Set` | Set health to specific value |
| `Immunity` | Grant damage immunity |
| `Invisibility` | Make player invisible |
| `Shield` | Create absorption shield |
| `Taunt` | Taunt nearby enemies |
| `Wolf` | Spawn wolf minion |

### Movement Mechanics
| Name | Description |
|------|-------------|
| `Launch` | Launch target into air |
| `Push` | Push target away |
| `Throw` | Throw target |
| `Fly` | Toggle flight |
| `Warp` | Teleport (all warp variants below) |
| `Warp Location` | Warp to specific location |
| `Warp Random` | Warp to random location |
| `Warp Swap` | Swap positions |
| `Warp Target` | Warp to target |
| `Warp Value` | Warp based on value |

### Value Mechanics (for calculations)
| Name | Description |
|------|-------------|
| `Value Set` | Set a variable |
| `Value Add` | Add to a variable |
| `Value Copy` | Copy a variable |
| `Value Divide` | Divide a variable |
| `Value Multiply` | Multiply a variable |
| `Value Math` | Math formula evaluation |
| `Value Random` | Random number |
| `Value Round` | Round a number |
| `Value Attribute` | Get attribute value |
| `Value Distance` | Get distance value |
| `Value Health` | Get health value |
| `Value Load` | Load value from config |
| `Value Location` | Location to value |
| `Value Lore` | Lore value |
| `Value Lore Slot` | Lore slot value |
| `Value Mana` | Mana value |
| `Value Placeholder` | PlaceholderAPI value |
| `Value Rotation` | Rotation value |

### Status/Potion Mechanics
| Name | Description |
|------|-------------|
| `Potion` | Apply potion effect |
| `Potion Projectile` | Launch potion projectile |
| `Status` | Apply status effect |
| `Buff` | Apply buff |
| `Cleanse` | Remove negative effects |
| `Purge` | Remove positive effects |

### Projectile Mechanics
| Name | Description |
|------|-------------|
| `Projectile` | Launch a projectile |
| `Item Projectile` | Launch item as projectile |
| `Particle Projectile` | Launch particle projectile |

### Particle/Visual Mechanics
| Name | Description |
|------|-------------|
| `Particle` | Spawn particles |
| `Particle Animation` | Play particle animation |
| `Particle Effect` | Play particle effect |
| `Particle Image` | Display particle image |

### World Mechanics
| Name | Description |
|------|-------------|
| `Block` | Place/remove blocks |
| `Explosion` | Create explosion |
| `Fire` | Set target on fire |
| `Lightning` | Strike lightning |
| `Mine` | Break blocks |
| `Sound` | Play sound |

### Item Mechanics
| Name | Description |
|------|-------------|
| `Item` | Give items |
| `Item Drop` | Drop items |
| `Item Remove` | Remove items |
| `Held Item` | Modify held item |
| `Durability` | Modify item durability |
| `Armor` | Equip armor |
| `Armor Stand` | Spawn armor stand |
| `Armor Stand Pose` | Pose armor stand |
| `Armor Stand Remove` | Remove armor stand |

### Skill/System Mechanics
| Name | Description |
|------|-------------|
| `Skill Cast` | Cast another skill |
| `Command` | Execute console command |
| `Message` | Send message to player |
| `Delay` | Delay execution of children |
| `Repeat` | Repeat children N times |
| `Channel` | Channel skill effects |
| `Cooldown` | Modify cooldowns |
| `Cancel` | Cancel skill execution |
| `Cancel Effect` | Cancel effects |
| `Interrupt` | Interrupt target |
| `Abort Skill` | Abort skill cast |
| `Passive` | Enable passive mode |
| `Trigger` | Trigger another trigger |
| `Signal Emit` | Emit a signal |
| `Flag` | Set a flag |
| `Flag Clear` | Clear a flag |
| `Flag Toggle` | Toggle a flag |
| `Forget Targets` | Clear target memory |
| `Remember Targets` | Store targets in memory |
| `Permission` | Give/take permissions |
| `Experience` | Give experience |
| `Disguise` | Disguise entity |
| `Money` | Give/take money (Vault) |
| `Mount` | Mount an entity |
| `Summon` | Summon an entity |
| `Mythicmob Skill` | Cast MythicMob skill |
| `Air Modify` | Modify air bubbles |
| `Air Set` | Set air bubbles |
| `Food` | Set food level |
| `Stat` | Modify player stats |
| `Mana` | Modify mana |
| `Attribute` | Modify dynamic attributes |

---

## Example: Simple Damage Skill

```yaml
Fireball:
  name: 'Fireball'
  max-level: 10
  skill-req: ''
  skill-req-lvl: 0
  needs-permission: false
  cooldown-message: true
  msg: '&6{player} &2casts &6{skill}!'
  combo: ''
  icon: 'Fire Charge'
  icon-data: 0
  icon-lore:
    - '&d{name} &7({level}/{max})'
    - '&2Type: &6{type}'
    - '&2Cooldown: {attr:cooldown}s'
    - '&2Mana: {attr:mana}'
  attributes:
    level-base: 1
    level-scale: 0
    cost-base: 2
    cost-scale: 1
    cooldown-base: 8
    cooldown-scale: -0.5
    mana-base: 15
    mana-scale: 5
  components:
    Cast-a:
      type: 'trigger'
      children:
        Area-b:
          type: 'target'
          data:
            radius-base: 5
            radius-scale: 0
            group: 'Enemy'
            wall: 'False'
            caster: 'False'
            max-base: 10
            max-scale: 0
          children:
            Damage-c:
              type: 'mechanic'
              data:
                damage-base: 20
                damage-scale: 5
                type: 'MAGIC'
            Particle-d:
              type: 'mechanic'
              data:
                effect: 'flame'
                amount-base: 30
                amount-scale: 0
```

## Example: Kill-Trigger Skill (Passive)

```yaml
Bloodlust:
  name: 'Bloodlust'
  max-level: 5
  skill-req: ''
  skill-req-lvl: 0
  needs-permission: false
  cooldown-message: true
  msg: ''
  combo: ''
  icon: 'Redstone'
  icon-data: 0
  icon-lore:
    - '&d{name} &7({level}/{max})'
    - '&2Heal on kill'
  attributes:
    level-base: 1
    level-scale: 0
    cost-base: 3
    cost-scale: 1
    cooldown-base: 0
    cooldown-scale: 0
    mana-base: 0
    mana-scale: 0
  components:
    Kill-a:
      type: 'trigger'
      children:
        Self-b:
          type: 'target'
          data:
            group: 'Ally'
            caster: 'True'
          children:
            Heal-c:
              type: 'mechanic'
              data:
                amount-base: 4
                amount-scale: 2
            Particle Effect-d:
              type: 'mechanic'
              data:
                effect: 'heart'
                amount-base: 5
                amount-scale: 0
```

## Example: Conditional Skill

```yaml
Execute:
  name: 'Execute'
  max-level: 10
  skill-req: ''
  skill-req-lvl: 0
  needs-permission: false
  cooldown-message: true
  msg: '&c{player} &4executes &c{target}!'
  combo: ''
  icon: 'Iron Axe'
  icon-data: 0
  icon-lore:
    - '&d{name} &7({level}/{max})'
    - '&2Deals bonus damage to low HP targets'
  attributes:
    level-base: 1
    level-scale: 0
    cost-base: 2
    cost-scale: 1
    cooldown-base: 12
    cooldown-scale: -0.8
    mana-base: 20
    mana-scale: 3
  components:
    Cast-a:
      type: 'trigger'
      children:
        Single-b:
          type: 'target'
          data:
            group: 'Enemy'
            range-base: 5
            range-scale: 0
          children:
            Health-c:
              type: 'condition'
              data:
                min-health-base: 0
                min-health-scale: 0
                max-health-base: 30
                max-health-scale: 0
                percentage: 'True'
              children:
                Damage-d:
                  type: 'mechanic'
                  data:
                    damage-base: 50
                    damage-scale: 10
                    type: 'PHYSICAL'
                Message-e:
                  type: 'mechanic'
                  data:
                    message: '&4Executed!'
                    targets: 'Caster'
                ElseCondition:
                  type: 'condition'
                  data: {}
                  children:
                    Damage-f:
                      type: 'mechanic'
                      data:
                        damage-base: 15
                        damage-scale: 3
                        type: 'PHYSICAL'
```

## Key Design Tips

1. **Node naming**: Component names in YAML are arbitrary keys (only used as map keys). The actual component type is determined by the `type` field. Convention: `SkillName`, `TriggerName-a`, `TargetName-b`, `MechanicName-c`, `ConditionName-d`.

2. **Cast trigger**: Most active skills use `Cast-a` as the entry point. Passive/event-based skills use event triggers like `Kill-a`, `Death-b`, `Left Click-c`.

3. **Execution order**: Components execute in the order they appear in the YAML children map (insertion order).

4. **Target filtering chain**: `Trigger → Target → [Condition] → Mechanic`. Conditions between Target and Mechanic filter which targets receive the mechanic.

5. **`counts: True`**: Setting `counts: True` on a mechanic makes it count as a "hit" for attributes that track hits (like mana per hit, etc.).

6. **Immediate execution**: Add `immediate: 'True'` to mechanics that should execute instantly rather than being batched.

7. **Value mechanics**: Use Value mechanics (Value Set, Value Add, Value Attribute, etc.) to store and manipulate variables that can be referenced by other components via `{values:key}` placeholders.

8. **Group targeting**: Use `group: 'Ally'` for support skills, `group: 'Enemy'` for offensive skills, `group: 'Both'` for self + others.
