package net.evilblock.twofactorauth.util;

import com.google.common.base.Preconditions;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

/**
 * Utility for interacting with the server's {@link CommandMap} instance.
 */
public final class CommandMapUtil {

    private static final Constructor<PluginCommand> COMMAND_CONSTRUCTOR;
    private static final Field COMMAND_MAP_FIELD;
    private static final Field KNOWN_COMMANDS_FIELD;

    static {
        try {
            COMMAND_CONSTRUCTOR = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            COMMAND_CONSTRUCTOR.setAccessible(true);
            COMMAND_MAP_FIELD = SimplePluginManager.class.getDeclaredField("commandMap");
            COMMAND_MAP_FIELD.setAccessible(true);
            KNOWN_COMMANDS_FIELD = SimpleCommandMap.class.getDeclaredField("knownCommands");
            KNOWN_COMMANDS_FIELD.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static CommandMap getCommandMap() {
        try {
            return (CommandMap) COMMAND_MAP_FIELD.get(Bukkit.getServer().getPluginManager());
        } catch (Exception e) {
            throw new RuntimeException("Could not get CommandMap", e);
        }
    }

    private static Map<String, Command> getKnownCommandMap() {
        try {
            //noinspection unchecked
            return (Map<String, Command>) KNOWN_COMMANDS_FIELD.get(getCommandMap());
        } catch (Exception e) {
            throw new RuntimeException("Could not get known commands map", e);
        }
    }

    /**
     * Registers a CommandExecutor with the server
     *
     * @param plugin the plugin instance
     * @param command the command instance
     * @param aliases the command aliases
     * @param <T> the command executor class state
     * @return the command executor
     */
    public static <T extends CommandExecutor> T registerCommand(Plugin plugin, T command, String... aliases) {
        Preconditions.checkArgument(aliases.length != 0, "No aliases");
        for (String alias : aliases) {
            try {
                PluginCommand cmd = COMMAND_CONSTRUCTOR.newInstance(alias, plugin);

                getCommandMap().register(plugin.getDescription().getName(), cmd);
                getKnownCommandMap().put(plugin.getDescription().getName().toLowerCase() + ":" + alias.toLowerCase(), cmd);
                getKnownCommandMap().put(alias.toLowerCase(), cmd);
                cmd.setLabel(alias.toLowerCase());

                cmd.setExecutor(command);
                if (command instanceof TabCompleter) {
                    cmd.setTabCompleter((TabCompleter) command);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return command;
    }

    /**
     * Unregisters a CommandExecutor with the server
     *
     * @param command the command instance
     * @param <T> the command executor class state
     * @return the command executor
     */
    public static <T extends CommandExecutor> T unregisterCommand(T command) {
        CommandMap map = getCommandMap();
        try {
            //noinspection unchecked
            Map<String, Command> knownCommands = (Map<String, Command>) KNOWN_COMMANDS_FIELD.get(map);

            Iterator<Command> iterator = knownCommands.values().iterator();
            while (iterator.hasNext()) {
                Command cmd = iterator.next();
                if (cmd instanceof PluginCommand) {
                    CommandExecutor executor = ((PluginCommand) cmd).getExecutor();
                    if (command == executor) {
                        cmd.unregister(map);
                        iterator.remove();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not unregister command", e);
        }

        return command;
    }

    private CommandMapUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}