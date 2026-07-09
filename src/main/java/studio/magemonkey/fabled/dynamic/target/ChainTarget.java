/**
 * Fabled
 * studio.magemonkey.fabled.dynamic.target.ChainTarget
 * <p>
 * The MIT License (MIT)
 * <p>
 * © 2026 VoidEdge
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software") to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * ...
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package studio.magemonkey.fabled.dynamic.target;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.particle.ParticleHelper;
import studio.magemonkey.fabled.api.particle.ParticleSettings;
import studio.magemonkey.fabled.api.util.Nearby;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Chain-bounces between nearby enemies, starting from the first target.
 * Supports per-bounce delay and particle lines between bounces using
 * Fabled's standard particle configuration (line-particle, line-amount, etc.).
 */
public class ChainTarget extends TargetComponent {
    private static final String RANGE   = "range";
    private static final String BOUNCES = "bounces";
    private static final String UNIQUE  = "unique";
    private static final String DELAY   = "delay";

    @Override
    public List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        final double  range   = parseValues(caster, RANGE, level, 5.0);
        final int     bounces = (int) parseValues(caster, BOUNCES, level, 3.0);
        final boolean unique  = settings.getBool(UNIQUE, true);
        final double  delay   = parseValues(caster, DELAY, level, 0.0);

        final boolean useLine  = settings.getBool("show-line", false);
        final boolean useDelay = delay > 0;

        if (useDelay || useLine) {
            scheduleChain(caster, level, targets, range, bounces, unique, delay,
                    useLine ? new ParticleSettings(settings, "line-") : null);
            return Collections.emptyList();
        }

        return collectTargets(caster, level, targets, range, bounces, unique);
    }

    private List<LivingEntity> collectTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets,
            final double range, final int bounces, final boolean unique) {

        final List<LivingEntity> result = new ArrayList<>();
        final Set<LivingEntity>  hit    = new HashSet<>();
        LivingEntity current = targets.isEmpty() ? caster : targets.get(0);
        hit.add(current);
        final DistanceComparator comparator = new DistanceComparator();

        for (int i = 0; i < bounces; i++) {
            current = findNext(caster, current, range, unique, hit, comparator);
            if (current == null) break;
            hit.add(current);
            result.add(current);
        }
        return result;
    }

    private void scheduleChain(
            final LivingEntity caster, final int level, final List<LivingEntity> targets,
            final double range, final int bounces, final boolean unique,
            final double delay, final ParticleSettings lineSettings) {

        final Set<LivingEntity>  hit        = new HashSet<>();
        final DistanceComparator comparator = new DistanceComparator();

        new BukkitRunnable() {
            LivingEntity prev    = targets.isEmpty() ? caster : targets.get(0);
            int          bounced = 0;

            @Override
            public void run() {
                if (bounced >= bounces) { cancel(); return; }

                final LivingEntity next = findNext(caster, prev, range, unique, hit, comparator);
                if (next == null) { cancel(); return; }

                // Draw particle line from previous target to new target
                if (lineSettings != null) {
                    drawLine(prev.getLocation().add(0, 1, 0),
                            next.getLocation().add(0, 1, 0),
                            lineSettings,
                            (int) parseValues(caster, "line-amount", level, 15.0));
                }

                hit.add(next);
                prev = next;
                bounced++;
                executeChildren(caster, level, List.of(next), false);
                if (bounced >= bounces) cancel();
            }
        }.runTaskTimer(Fabled.inst(), 0, Math.max(1, (long) (delay * 20)));
    }

    private LivingEntity findNext(
            final LivingEntity caster, final LivingEntity origin,
            final double range, final boolean unique,
            final Set<LivingEntity> hit, final DistanceComparator comparator) {
        comparator.setOrigin(origin.getLocation());
        return Nearby.getLivingNearby(origin, range, true).stream()
                .filter(e -> {
                    if (e.equals(origin)) return false;
                    if (unique && hit.contains(e)) return false;
                    if (e instanceof Player) {
                        GameMode gm = ((Player) e).getGameMode();
                        if (gm == GameMode.SPECTATOR || gm == GameMode.CREATIVE) return false;
                    }
                    return isValidTarget(caster, origin, e)
                            || (self.equals(IncludeCaster.IN_AREA) && caster.equals(e));
                })
                .min(comparator)
                .orElse(null);
    }

    /** Draw a line of particles between two locations using Fabled's ParticleHelper. */
    private void drawLine(final Location from, final Location to,
                          final ParticleSettings lineSettings, final int points) {
        if (from.getWorld() == null) return;
        final Vector dir = to.toVector().subtract(from.toVector());
        final double len = dir.length();
        if (len < 0.01) return;
        dir.normalize();
        final double step = len / Math.max(1, points);
        for (double d = 0; d <= len; d += step) {
            final Location loc = from.clone().add(dir.clone().multiply(d));
            ParticleHelper.play(loc, settings, null, "line-", null);
        }
    }

    @Override
    public String getKey() {
        return "chain";
    }

    private static class DistanceComparator implements Comparator<LivingEntity> {
        private Location origin;
        void setOrigin(final Location origin) { this.origin = origin; }
        @Override
        public int compare(final LivingEntity o1, final LivingEntity o2) {
            return Double.compare(
                    o1.getLocation().distanceSquared(origin),
                    o2.getLocation().distanceSquared(origin));
        }
    }
}
