package com.mistdig.misttags;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Compatibility scheduler. Uses Folia/Paper region schedulers by reflection when present;
 * otherwise falls back to the classic Bukkit scheduler used by Spigot/Bukkit/Paper.
 */
public final class MistTagsScheduler {

    public interface Handle {
        void cancel();
    }

    private final MistTags plugin;
    private final boolean foliaLike;

    public MistTagsScheduler(MistTags plugin) {
        this.plugin = plugin;
        this.foliaLike = hasMethod(Bukkit.class, "getGlobalRegionScheduler");
    }

    public Handle runGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        if (foliaLike) {
            Object scheduled = invokeGlobal("runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, long.class},
                    plugin, consumer(task), Math.max(1, delayTicks), Math.max(1, periodTicks));
            return reflectHandle(scheduled);
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }

    public void runGlobalNow(Runnable task) {
        if (foliaLike) {
            invokeGlobal("execute", new Class<?>[]{Plugin.class, Runnable.class}, plugin, task);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void runGlobalDelayed(Runnable task, long delayTicks) {
        if (foliaLike) {
            invokeGlobal("runDelayed", new Class<?>[]{Plugin.class, Consumer.class, long.class},
                    plugin, consumer(task), Math.max(1, delayTicks));
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public Handle runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        if (foliaLike) {
            long delayMs = Math.max(1, delayTicks) * 50L;
            long periodMs = Math.max(1, periodTicks) * 50L;
            Object scheduled = invokeAsync("runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class},
                    plugin, consumer(task), delayMs, periodMs, TimeUnit.MILLISECONDS);
            return reflectHandle(scheduled);
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }

    public void runAsyncNow(Runnable task) {
        if (foliaLike) {
            invokeAsync("runNow", new Class<?>[]{Plugin.class, Consumer.class}, plugin, consumer(task));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public void runForPlayer(Player player, Runnable task) {
        if (foliaLike) {
            Object scheduler = invoke(player, "getScheduler", new Class<?>[]{});
            invoke(scheduler, "run", new Class<?>[]{Plugin.class, Consumer.class, Runnable.class},
                    plugin, consumer(task), null);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public Handle runForPlayerTimer(Player player, Runnable task, long delayTicks, long periodTicks) {
        if (foliaLike) {
            Object scheduler = invoke(player, "getScheduler", new Class<?>[]{});
            Object scheduled = invoke(scheduler, "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class, long.class, long.class},
                    plugin, consumer(task), null, Math.max(1, delayTicks), Math.max(1, periodTicks));
            return reflectHandle(scheduled);
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }

    public void runForSender(CommandSender sender, Runnable task) {
        if (sender instanceof Player player) {
            runForPlayer(player, task);
        } else {
            runGlobalNow(task);
        }
    }

    public void cancel(Handle task) {
        if (task == null) return;
        try {
            task.cancel();
        } catch (RuntimeException e) {
            plugin.getLogger().warning("Could not cancel scheduled task cleanly: " + e.getMessage());
        }
    }

    private Object invokeGlobal(String method, Class<?>[] types, Object... args) {
        Object scheduler = invokeStatic(Bukkit.class, "getGlobalRegionScheduler", new Class<?>[]{});
        return invoke(scheduler, method, types, args);
    }

    private Object invokeAsync(String method, Class<?>[] types, Object... args) {
        Object scheduler = invokeStatic(Bukkit.class, "getAsyncScheduler", new Class<?>[]{});
        return invoke(scheduler, method, types, args);
    }

    private Consumer<Object> consumer(Runnable task) {
        return ignored -> task.run();
    }

    private Handle reflectHandle(Object task) {
        return () -> {
            if (task != null) invoke(task, "cancel", new Class<?>[]{});
        };
    }

    private boolean hasMethod(Class<?> type, String name) {
        try {
            type.getMethod(name);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private Object invokeStatic(Class<?> type, String method, Class<?>[] types, Object... args) {
        try {
            Method m = type.getMethod(method, types);
            m.setAccessible(true);
            return m.invoke(null, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Scheduler reflection failed: " + method, e);
        }
    }

    private Object invoke(Object target, String method, Class<?>[] types, Object... args) {
        try {
            Method m = target.getClass().getMethod(method, types);
            m.setAccessible(true);
            return m.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Scheduler reflection failed: " + method, e);
        }
    }
}
