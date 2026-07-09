package studio.magemonkey.fabled.dynamic.condition;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.dynamic.DynamicSkill;

public class TreeNodeCondition extends ConditionComponent {
    private static final String NODE = "node";

    @Override
    public String getKey() {
        return "tree node";
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String node = settings.getString(NODE);
        CastData data = DynamicSkill.getCastData(caster);
        boolean result = data.contains("fst_node_" + node) && data.getDouble("fst_node_" + node) > 0;
        Fabled.inst().getLogger().info("[TreeCond] node=" + node + " key=fst_node_" + node + " contains=" + data.contains("fst_node_" + node) + " value=" + data.getDouble("fst_node_" + node) + " result=" + result);
        return result;
    }
}
