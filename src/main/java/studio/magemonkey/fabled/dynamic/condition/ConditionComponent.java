package studio.magemonkey.fabled.dynamic.condition;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.dynamic.ComponentType;
import studio.magemonkey.fabled.dynamic.EffectComponent;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * © 2026 VoidEdge
 * studio.magemonkey.fabled.dynamic.condition.ConditionComponent
 */
public abstract class ConditionComponent extends EffectComponent {

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentType getType() {
        return ComponentType.CONDITION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(
            final LivingEntity caster, final int level, final List<LivingEntity> targets, boolean force) {
        final List<LivingEntity> filtered = targets.stream()
                .filter(t -> test(caster, level, t))
                .collect(Collectors.toList());

        if (!filtered.isEmpty()) {
            Fabled.inst().getLogger().info("[CondDebug] " + getKey() + " PASSED (filtered=" + filtered.size() + "), executing " + children.size() + " children");
            return executeChildren(caster, level, filtered, force);
        }
        // Condition failed — search children for an ElseCondition to handle the fallback
        passed = false;
        Fabled.inst().getLogger().info("[CondDebug] " + getKey() + " FAILED, searching " + children.size() + " children for ElseCondition");
        for (EffectComponent child : children) {
            Fabled.inst().getLogger().info("[CondDebug]   child: " + child.getKey() + " (" + child.getClass().getSimpleName() + ")");
            if (child instanceof ElseCondition) {
                Fabled.inst().getLogger().info("[CondDebug]   -> Found ElseCondition, executing it");
                return child.execute(caster, level, targets, force);
            }
        }
        Fabled.inst().getLogger().info("[CondDebug] " + getKey() + " no ElseCondition found, returning false");
        return false;
    }

    abstract boolean test(final LivingEntity caster, final int level, final LivingEntity target);

    /**
     * {@inheritDoc}
     */
    @Override
    public void playPreview(List<Runnable> onPreviewStop,
                            Player caster,
                            int level,
                            Supplier<List<LivingEntity>> targetSupplier) {
        super.playPreview(onPreviewStop, caster, level, () -> targetSupplier.get().stream()
                .filter(t -> test(caster, level, t))
                .collect(Collectors.toList()));
    }
}
