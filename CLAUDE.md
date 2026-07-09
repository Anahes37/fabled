# Fabled Plugin Development Guidelines

- Always respond in 简体中文
- When asked to write or design a skill, read `COMPONENTS.md` first for precise data options of each component, then `SKILLS.md` for the full list of available components and YAML structure
- Skills are YAML files consumed by the Fabled Minecraft plugin
- Use the web-based skill editor at `d:\cppwork\MCcodes\fablededitor` to visually create and edit skills, then copy the YAML output
- Skill attribute values use `{key}-base` and `{key}-scale` format: `value = base + (level - 1) * scale`
- Every skill must have a `components` tree with at least one trigger
- Active skills use `Cast-a` trigger; passive/event skills use event triggers like `Kill-a`, `Death-b`, `Left Click-c`
- The component execution chain is: `Trigger → Target → [Condition] → Mechanic`
- Node names in YAML are arbitrary; the `type` field determines the actual component

## Common Patterns

**Active AOE skill**: `Cast-a(trigger) → Area(target, group=Enemy) → Damage(mechanic)`
**Passive on-kill**: `Kill-a(trigger) → Self(target) → Heal(mechanic)`
**Conditional skill**: `Cast-a → Target → Condition → [Mechanic on pass] → ElseCondition → [Mechanic on fail]`
**Self-buff**: `Cast-a → Self(target) → Buff/Potion(mechanic)`
