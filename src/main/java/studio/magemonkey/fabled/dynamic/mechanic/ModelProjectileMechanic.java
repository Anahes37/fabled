/**
 * Fabled
 * studio.magemonkey.fabled.dynamic.mechanic.ModelProjectileMechanic
 * <p>
 * The MIT License (MIT)
 * <p>
 * © 2026 VoidEdge
 */
package studio.magemonkey.fabled.dynamic.mechanic;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.entity.BaseEntity;
import kr.toxicity.model.api.tracker.TrackerModifier;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.target.TargetHelper;
import studio.magemonkey.fabled.api.util.Nearby;
import studio.magemonkey.fabled.hook.PluginChecker;
import studio.magemonkey.fabled.log.Logger;
import studio.magemonkey.fabled.task.RemoveEntitiesTask;
import studio.magemonkey.fabled.util.VectorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Launches a BetterModel 3D model as a projectile.
 * An invisible armor stand carries the model via EntityTracker.
 */
public class ModelProjectileMechanic extends MechanicComponent {
    private static final String MODEL       = "model";
    private static final String VELOCITY    = "velocity";
    private static final String RANGE       = "range";
    private static final String SPREAD      = "spread";
    private static final String AMOUNT      = "amount";
    private static final String LIFESPAN    = "lifespan";
    private static final String HIT_RADIUS  = "hit-radius";
    private static final String SIGHT_TRACE = "sight-trace";
    private static final String FORWARD     = "forward";
    private static final String UPWARD      = "upward";
    private static final String RIGHT       = "right";

    @Override
    public String getKey() {
        return "model projectile";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets, boolean force) {
        if (!PluginChecker.isBetterModelActive()) {
            Logger.invalid("BetterModel is not active - cannot launch model projectile");
            return false;
        }

        String  modelId    = settings.getString(MODEL, "");
        double  speed      = parseValues(caster, VELOCITY, level, 2.0);
        double  rangeVal   = parseValues(caster, RANGE, level, 50.0);
        String  spread     = settings.getString(SPREAD, "cone").toLowerCase();
        int     amount     = (int) parseValues(caster, AMOUNT, level, 1.0);
        int     lifespanTicks = (int) (parseValues(caster, LIFESPAN, level, 10.0) * 20);
        double  hitRadius  = parseValues(caster, HIT_RADIUS, level, 1.0);
        boolean sightTrace = settings.getBool(SIGHT_TRACE, true);
        double  forward    = parseValues(caster, FORWARD, level, 0);
        double  upward     = parseValues(caster, UPWARD, level, 0);
        double  right      = parseValues(caster, RIGHT, level, 0);

        if (modelId.isEmpty()) {
            Logger.invalid("ModelProjectile: model name is empty");
            return false;
        }

        ModelRenderer renderer = BetterModel.modelOrNull(modelId);
        if (renderer == null) {
            Logger.invalid("ModelProjectile: BetterModel model not found: " + modelId);
            return false;
        }

        List<ArmorStand> projectiles = new CopyOnWriteArrayList<>();
        for (LivingEntity target : targets) {
            Location location = VectorUtil.getOffsetLocation(target, forward, right, upward);

            for (int i = 0; i < amount; i++) {
                Location spawnLoc = location.clone();
                Vector   dir      = location.getDirection().clone();

                // Apply spread
                if (spread.equals("cone")) {
                    double spreadAngle = Math.toRadians(15);
                    double yaw   = Math.atan2(dir.getZ(), dir.getX()) + (Math.random() - 0.5) * spreadAngle;
                    double pitch = Math.asin(dir.getY()) + (Math.random() - 0.5) * spreadAngle;
                    dir = new Vector(-Math.cos(pitch) * Math.sin(yaw), -Math.sin(pitch), Math.cos(pitch) * Math.cos(yaw));
                    dir.normalize();
                } else if (spread.equals("horizontal cone")) {
                    dir.setY(0);
                    dir.normalize();
                    double spreadAngle = Math.toRadians(15);
                    double yaw = Math.atan2(dir.getZ(), dir.getX()) + (Math.random() - 0.5) * spreadAngle;
                    dir = new Vector(-Math.sin(yaw), 0, Math.cos(yaw));
                    dir.normalize();
                }

                ArmorStand as = spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class, s -> {
                    s.setInvisible(true);
                    s.setMarker(true);
                    s.setInvulnerable(true);
                    s.setGravity(false);
                    s.setSilent(true);
                    s.setPersistent(false);
                });

                // Attach BetterModel to armor stand via EntityTracker
                try {
                    renderer.create(
                        BaseEntity.of(BukkitAdapter.adapt(as)),
                        TrackerModifier.builder()
                            .sightTrace(sightTrace)
                            .damageAnimation(false)
                            .damageTint(false)
                            .build()
                    );
                } catch (Exception e) {
                    Logger.bug("ModelProjectile: Failed to attach BetterModel to armor stand: " + e.getMessage());
                    as.remove();
                    continue;
                }

                projectiles.add(as);

                final Vector velocity = dir.clone().multiply(speed);
                final Location startLoc = spawnLoc.clone();
                final int[] ticks = {0};

                // Tick loop for movement + collision
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!as.isValid() || as.isDead()) {
                            this.cancel();
                            projectiles.remove(as);
                            return;
                        }

                        ticks[0]++;

                        // Lifespan
                        if (ticks[0] > lifespanTicks) {
                            as.remove();
                            projectiles.remove(as);
                            this.cancel();
                            return;
                        }

                        // Move
                        Location newLoc = as.getLocation().add(velocity);
                        as.teleport(newLoc);

                        // Range
                        if (newLoc.distanceSquared(startLoc) > rangeVal * rangeVal) {
                            as.remove();
                            projectiles.remove(as);
                            this.cancel();
                            return;
                        }

                        // Collision
                        List<LivingEntity> nearby = Nearby.getLivingNearby(newLoc, hitRadius);
                        for (LivingEntity hit : nearby) {
                            if (hit == caster) continue;
                            if (!Fabled.getSettings().isValidTarget(hit)) continue;
                            if (TargetHelper.isObstructed(newLoc, hit.getEyeLocation())) continue;

                            ArrayList<LivingEntity> hitTargets = new ArrayList<>();
                            hitTargets.add(hit);
                            executeChildren(caster, level, hitTargets, force);
                            as.remove();
                            projectiles.remove(as);
                            this.cancel();
                            return;
                        }
                    }
                }.runTaskTimer(Fabled.inst(), 0L, 1L);
            }
        }

        if (projectiles.isEmpty()) return false;

        // Safety cleanup
        new RemoveEntitiesTask(projectiles, lifespanTicks + 10);

        return true;
    }

    @Override
    public void playPreview(List<Runnable> onPreviewStop,
                            Player caster,
                            int level,
                            Supplier<List<LivingEntity>> targetSupplier) {
        super.playPreview(onPreviewStop, caster, level, targetSupplier);
    }
}
