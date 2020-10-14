package red.man10.firstjoinplayer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class FirstJoinPlayer extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("FirstJoinPlayer is run.");
        getServer().getPluginManager().registerEvents(this, this);
        // config.ymlが存在しない場合はファイルに出力します。
        saveDefaultConfig();
        // config.ymlを読み込みます。
        FileConfiguration config = getConfig();
        reloadConfig();
        getCommand("fjp").setExecutor(this);
        if (!config.getBoolean("mode")) {
            getLogger().info("FirstJoinPlayer is not run.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("fjp.use")) {
            p.sendMessage("Unknown command. Type \"/help\" for help.");
            return true;
        }
        if (args.length == 0){
            p.sendMessage("§b§l ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
            p.sendMessage("§b§l                 [FirstPlayerJoin]                   ");
            p.sendMessage("§b§l /fpj set 現在のインベントリを初期装備としてセットします。");
            p.sendMessage("§b§l /fpj get 現在のインベントリ初期装備をゲットします。");
            p.sendMessage("§b§l ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
            return true;
        }
        if (p.hasPermission("fpj.set")) {
            if (args[0].equalsIgnoreCase("set")) {
                try {
                    saveInventory(p,this);
                } catch (IOException e) {
                    e.printStackTrace();
                    p.sendMessage("インベントリのセットができませんでした");
                }
                p.sendMessage("インベントリのセットができました");
                reloadConfig();
                return true;
            }
            return true;
        }
        if (p.hasPermission("fpj.get")) {
            if (args[0].equalsIgnoreCase("get")) {
                try {
                    restoreInventory(p,this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                p.sendMessage("インベントリの取得ができました");
                reloadConfig();
                return true;
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void LoginEvent(PlayerLoginEvent e) throws IOException {
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()) return;
        restoreInventory(p,this);
    }

    public Inventory saveInventory(Player p, Plugin plugin) throws IOException {
        File f = new File(plugin.getDataFolder().getAbsolutePath(), "kit.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        c.set("inventory.armor", p.getInventory().getArmorContents());
        c.set("inventory.content", p.getInventory().getContents());
        c.save(f);
        return null;
    }

    public Inventory restoreInventory(Player p, Plugin plugin) throws IOException {
        File f = new File(plugin.getDataFolder().getAbsolutePath(),  "kit.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        ItemStack[] content = ((List<ItemStack>) c.get("inventory.armor")).toArray(new ItemStack[0]);
        p.getInventory().setArmorContents(content);
        content = ((List<ItemStack>) c.get("inventory.content")).toArray(new ItemStack[0]);
        p.getInventory().setContents(content);
        return null;
    }
}
