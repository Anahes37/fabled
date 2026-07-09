/**
 * Fabled
 * studio.magemonkey.fabled.dynamic.mechanic.ModelEffectMechanic
 * <p>
 * The MIT License (MIT)
 * <p>
 * © 2026 VoidEdge
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package studio.magemonkey.fabled.dynamic.mechanic;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.DummyTracker;
import kr.toxicity.model.api.tracker.TrackerModifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.hook.PluginChecker;
import studio.magemonkey.fabled.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Plays a BetterModel 3D model effect at each target location.
 * The model is displayed for a configurable duration then automatically removed.
 */
public class ModelEffectMechanic extends MechanicComponent {
    private static final String MODEL      = "model";
    private static final String SECONDS    = "seconds";
    private static final String SIGHT_TRACE = "sight-trace";
    private static final String FORWARD    = "forward";
    private static final String UPWARD     = "upward";
    private static final String RIGHT      = "right";

    @Override
    public String getKey() {
        return "model effect";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets, boolean force) {
        if (!PluginChecker.isBetterModelActive()) {
            Logger.invalid("BetterModel is not active - cannot play model effect");
            return false;
        }

        String  modelId    = settings.getString(MODEL, "");
        double  seconds    = parseValues(caster, SECONDS, level, 2.0);
        boolean sightTrace = settings.getBool(SIGHT_TRACE, true);
        double  forward    = parseValues(caster, FORWARD, level, 0);
        double  upward     = parseValues(caster, UPWARD, level, 0);
        double  right      = parseValues(caster, RIGHT, level, 0);

        if (modelId.isEmpty()) {
            Logger.invalid("ModelEffect: model name is empty");
            return false;
        }

        ModelRenderer renderer = BetterModel.modelOrNull(modelId);
        if (renderer == null) {
            Logger.invalid("ModelEffect: BetterModel model not found: " + modelId);
            return false;
        }

        List<DummyTracker> trackers = new ArrayList<>();
        for (LivingEntity target : targets) {
            Location loc = target.getLocation().clone();
            loc.add(loc.getDirection().setY(0).normalize().multiply(forward));
            loc.add(0, upward, 0);
            loc.add(loc.getDirection().setY(0).normalize().crossProduct(new org.bukkit.util.Vector(0, 1, 0)).multiply(right));

            DummyTracker tracker = renderer.create(
                BukkitAdapter.adapt(loc),
                TrackerModifier.builder().sightTrace(sightTrace).build()
            );

            // DummyTracker does NOT auto-broadcast to players — must spawn manually
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(loc.getWorld())
                    && player.getLocation().distanceSquared(loc) < 4096) { // 64 blocks
                    tracker.spawn(BukkitAdapter.adapt(player));
                }
            }
            trackers.add(tracker);
        }

        int durationTicks = Math.max(1, (int) (seconds * 20));
        // Schedule cleanup after duration
        Fabled.inst().getServer().getScheduler().runTaskLater(Fabled.inst(), () -> {
            for (DummyTracker tracker : trackers) {
                tracker.close();
            }
        }, durationTicks);

        return !targets.isEmpty();
    }

    @Override
    public void playPreview(List<Runnable> onPreviewStop,
                            Player caster,
                            int level,
                            Supplier<List<LivingEntity>> targetSupplier) {
        // Preview: just show a simple particle marker since we can't render 3D models in preview
        super.playPreview(onPreviewStop, caster, level, targetSupplier);
    }
}
