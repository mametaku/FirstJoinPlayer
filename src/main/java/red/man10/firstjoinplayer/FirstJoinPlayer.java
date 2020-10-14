package red.man10.firstjoinplayer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class FirstJoinPlayer extends JavaPlugin implements Listener {

    MySQLManager data = new MySQLManager(this,"FirstJoinPlayer");

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
        if (args[0].equalsIgnoreCase("set")) {
            if (args.length == 1){
                if (p.hasPermission("fjp.use")) {
                    try {
                        saveInventory(p,this);
                    } catch (IOException e) {
                        e.printStackTrace();
                        p.sendMessage("§2§l[FJP]§fインベントリのセットができませんでした");
                    }
                    p.sendMessage("§2§l[FJP]§fインベントリのセットができました");
                    reloadConfig();
                    return true;
                }
            }
        }

        if (args[0].equalsIgnoreCase("get")) {
            if (args.length == 1){
                if (p.hasPermission("fjp.use")) {
                    try {
                        restoreInventory(p,this);
                    } catch (IOException e) {
                        e.printStackTrace();
                        p.sendMessage("§2§l[FJP]§fインベントリの取得ができませんでした");
                    }
                    p.sendMessage("§2§l[FJP]§fインベントリの取得ができました");
                    reloadConfig();
                    return true;
                }
            }
        }
        return false;
    }

    public void getData(PlayerEvent event){
        Player p = event.getPlayer();
        String uuid = p.getUniqueId().toString();
        String player =  p.getName();
        ResultSet rs = data.query("select * from logged_in_user where uuid='" + uuid + "';");
        try {
            if (!rs.next()) {
                restoreInventory(p,this);
                data.execute("insert into logged_in_user (uuid,player) value ('" + uuid +"','"+ player +"');");
            } else {

            }
        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
        }
    }

    @EventHandler
    public void LoginEvent(PlayerLoginEvent event) throws IOException {
        FileConfiguration config = getConfig();
        if (!config.getBoolean("mode")) return;
        Bukkit.getScheduler().runTaskAsynchronously(this, (  ) -> {
            try {
                getData((PlayerEvent) event);
            } catch (Exception e) {
                Bukkit.getLogger().info(e.getMessage());
                System.out.println(e.getMessage());
            }
        });
    }

    public Inventory saveInventory(Player p, Plugin plugin) throws IOException {
        File f = new File(plugin.getDataFolder().getAbsolutePath(), "kit.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        c.set("inventory.armor", p.getInventory().getArmorContents());
        c.set("inventory.content", p.getInventory().getContents());
        c.save(f);
        return null;
    }
    @SuppressWarnings("unchecked")
    public void restoreInventory(Player p, Plugin plugin) throws IOException {
        File f = new File(plugin.getDataFolder().getAbsolutePath(),  "kit.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        ItemStack[] content = ((List<ItemStack>) c.get("inventory.armor")).toArray(new ItemStack[0]);
        p.getInventory().setArmorContents(content);
        content = ((List<ItemStack>) c.get("inventory.content")).toArray(new ItemStack[0]);
        p.getInventory().setContents(content);
    }
}
