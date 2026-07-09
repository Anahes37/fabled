package studio.magemonkey.fabled.api.player;

import org.bukkit.entity.Player;

public interface SkillTreeHook {
    /**
     * Resolves the skill to cast based on the player's current combo state.
     * @param player the player performing the combo
     * @param comboString the combo string (e.g. "Q L R")
     * @return the name of the skill to cast,
     *         or "" (empty) to BLOCK the combo (tree manages it but no node unlocked),
     *         or null to fall through to native Fabled combo handling (tree does not manage this combo)
     */
    String resolveSkillByCombo(PlayerData player, String comboString);

    /**
     * Called right before a resolved skill is cast.
     * The hook can inject data (e.g. ENHANCE node markers) into CastData here.
     * @param player the player
     * @param comboString the combo that was just resolved
     */
    default void beforeCast(Player player, String comboString) {}
}
