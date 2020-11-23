package net.kitcake.magic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;

public class Main extends JavaPlugin implements CommandExecutor, TabCompleter {

    public static Main INSTANCE;
    public static File blocksFile;
    public static YamlConfiguration blocksYaml;
    public static Map<String, Group> blocks = new HashMap<>();
    public static long start = System.currentTimeMillis();

    @Override
    public void onEnable() {
        INSTANCE = this;
        blocksFile = new File(getDataFolder() + File.separator + "blocks.yml");
        blocksYaml = YamlConfiguration.loadConfiguration(blocksFile);
        blocksYaml.addDefault("blocks", new ArrayList<>());
        blocksYaml.options().copyDefaults(true);
        saveBlocks();
        try {
            // Boucle qui parcourt les groupes de blocs
            for(String groupStr : blocksYaml.getConfigurationSection("blocks").getKeys(false)) {
                Material material1;
                Material material2;
                try {
                    material1 = Material.valueOf(blocksYaml.getString("blocks." + groupStr + ".options.material1"));
                    material2 = Material.valueOf(blocksYaml.getString("blocks." + groupStr + ".options.material2"));
                }catch(IllegalArgumentException e) {
                    material1 = null;
                    material2 = null;
                }
                Group group = new Group(groupStr, blocksYaml.getInt("blocks." + groupStr + ".options.timer"), material1, material2);
                blocks.put(groupStr, group);
                Set<String> bb = blocksYaml.getConfigurationSection("blocks." + groupStr).getKeys(false);
                bb.remove("options");
                for(String block : bb) {
                    group.getBlocks().add(Bukkit.getWorld(blocksYaml.getString("blocks." + groupStr + "." + block + ".world"))
                            .getBlockAt(
                                    blocksYaml.getInt("blocks." + groupStr + "." + block + ".x"),
                                    blocksYaml.getInt("blocks." + groupStr + "." + block + ".y"),
                                    blocksYaml.getInt("blocks." + groupStr + "." + block + ".z")
                            )
                    );
                }
            }
        }catch(NullPointerException ignored) {}
        getCommand("magic").setExecutor(this);
        getCommand("magic").setTabCompleter(this);



        new BukkitRunnable(){
            @Override
            public void run() {
                while(true) {
                    try {
                        // Synchronisation
                        long now = System.currentTimeMillis();
                        if(now > start + 10000) {
                            for(Map.Entry<String, Group> entry : blocks.entrySet()) {
                                entry.getValue().setLastChange(now);
                            }
                            start = System.currentTimeMillis();
                        }else {
                            for(Map.Entry<String, Group> entry : blocks.entrySet()) {
                                Group group = entry.getValue();
                                if(System.currentTimeMillis() > group.getLastChange() + group.getTimer()) {
                                    Material mat = group.isItsMaterial1() ? group.getMaterial2() : group.getMaterial1();
                                    group.setItsMaterial1(!group.isItsMaterial1());
                                    if(mat != null) {
                                        for(Block block : group.getBlocks()) {
                                            try {
                                                Bukkit.getScheduler().runTask(Main.INSTANCE, () -> {
                                                    try {
                                                        block.setType(mat);
                                                    }catch(IllegalArgumentException ignored) {}
                                                });
                                            }catch(IllegalPluginAccessException ignored) {}
                                        }
                                    }
                                    group.setLastChange(System.currentTimeMillis());
                                }
                            }
                        }
                    }catch(ConcurrentModificationException ignored) {}
                }
            }
        }.runTaskAsynchronously(this);

    }

    public static void saveBlocks() {
        try {
            blocksYaml.save(blocksFile);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "add":
                    if(sender instanceof Player) {
                        if(args.length > 1) {
                            if(!blocks.containsKey(args[1])) {
                                if(checkName(args[1])) {
                                    blocks.put(args[1], new Group(args[1], 5000L, null, null));
                                    blocksYaml.set("blocks." + args[1] + ".options.timer", 5000);
                                    blocksYaml.set("blocks." + args[1] + ".options.material1", ".");
                                    blocksYaml.set("blocks." + args[1] + ".options.material2", ".");
                                    sender.sendMessage(blocks.get(args[1]).addBlock(((Player)sender).getTargetBlock(null, 10)) ? ChatColor.GREEN + "Bloc ajouté au groupe !" : ChatColor.RED + "Le bloc est déjà dans le groupe.");
                                }else {
                                    sender.sendMessage(ChatColor.RED + "Le nom ne doit contenir que des lettres ou des chiffres et avoir un maximum de 20 caractères.");
                                }
                            }
                        }else {
                            sender.sendMessage(ChatColor.RED + "/magic add <nom du groupe>");
                        }
                    }
                    break;
                case "remove":
                    if(sender instanceof Player) {
                        if(args.length > 1) {
                            if(blocks.containsKey(args[1])) {
                                sender.sendMessage(blocks.get(args[1]).removeBlock(((Player) sender).getTargetBlock(null, 10)) ? ChatColor.GREEN + "Bloc enlevé du groupe \"" + args[1] + "\" !" : ChatColor.RED + "Ce bloc n'est pas dans le groupe.");
                            }else {
                                sender.sendMessage(ChatColor.RED + "Ce groupe n'existe pas.");
                            }
                        }else {
                            sender.sendMessage(ChatColor.RED + "/magic remove <nom du groupe>");
                        }
                    }
                    break;
                case "number":
                    try {
                        Set<String> groups = blocksYaml.getConfigurationSection("blocks").getKeys(false);
                        int blocks = 0;
                        for(String str : groups) {
                            try {
                                blocks += blocksYaml.getConfigurationSection("blocks." + str).getKeys(false).size() - 1;
                            }catch(NullPointerException ignored) {}
                        }
                        sender.sendMessage(ChatColor.GOLD + "Groupes: " + ChatColor.RED + groups.size() + "\n" + ChatColor.GOLD + "Blocs: " + ChatColor.RED + blocks);
                    }catch(NullPointerException ignored) {}
                    break;
                case "checkerror":
                    for(Map.Entry<String, Group> entry : blocks.entrySet()) {
                        sender.sendMessage(ChatColor.YELLOW + entry.getKey() + ":");
                        for(Block b : entry.getValue().getBlocks()) {
                            sender.sendMessage(ChatColor.DARK_AQUA + b.getType().toString());
                        }
                    }
                    break;
                case "settimer":
                    if(args.length > 2) {
                        if(blocks.containsKey(args[1])) {
                            try {
                                long timer = Long.parseLong(args[2]);
                                if(timer >= 50) {
                                    blocks.get(args[1]).setTimer(timer);
                                    sender.sendMessage(ChatColor.YELLOW + "Timer de \"" + args[1] + "\" changé avec succès ! " + ChatColor.DARK_AQUA + "[" + args[2] + " ms]");
                                }else {
                                    sender.sendMessage(ChatColor.RED + "Le timer doit être compris entre 50 et " + Long.MAX_VALUE + ".");
                                }
                            }catch(NumberFormatException e) {
                                sender.sendMessage(ChatColor.RED + "Format du timer incorrect.");
                            }
                        }else {
                            sender.sendMessage(ChatColor.RED + "Ce groupe n'existe pas.");
                        }
                    }else {
                        sender.sendMessage(ChatColor.RED + "/magic settimer <nom du groupe> <temps en millisecondes>");
                    }
                    break;
                case "showtimer":
                    if(args.length > 1) {
                        if(blocks.containsKey(args[1])) {
                            sender.sendMessage(ChatColor.YELLOW + "" + blocks.get(args[1]).getTimer() + " ms.");
                        }else {
                            sender.sendMessage(ChatColor.RED + "Ce groupe n'existe pas.");
                        }
                    }else {
                        sender.sendMessage(ChatColor.RED + "/magic showtimer <nom du groupe>");
                    }
                    break;
                case "replace":
                    if(args.length > 3) {
                        if(blocks.containsKey(args[1])) {
                            try {
                                Material mat1 = Material.valueOf(args[2].toUpperCase());
                                Material mat2 = Material.valueOf(args[3].toUpperCase());
                                blocks.get(args[1]).setMaterials(mat1, mat2);
                                sender.sendMessage(ChatColor.YELLOW + "Matériaux remplacés ! " + ChatColor.DARK_AQUA + "[" + mat1 + " - " + mat2 + "]");
                            }catch(IllegalArgumentException e) {
                                sender.sendMessage(ChatColor.RED + "Matériel invalide.");
                            }
                        }else {
                            sender.sendMessage(ChatColor.RED + "Ce groupe n'existe pas.");
                        }
                    }else {
                        sender.sendMessage(ChatColor.RED + "/magic replace <nom du groupe> <matériel 1> <matériel 2>");
                    }
                    break;
                case "showmaterials":
                    if(args.length > 1) {
                        if(blocks.containsKey(args[1])) {
                            Group group = blocks.get(args[1]);
                            sender.sendMessage(ChatColor.DARK_AQUA + "[" + group.getMaterial1() + " - " + group.getMaterial2() + "]");
                        }else {
                            sender.sendMessage(ChatColor.RED + "Ce groupe n'existe pas.");
                        }
                    }else {
                        sender.sendMessage(ChatColor.RED + "/magic showmaterials <nom du groupe>");
                    }
                    break;
                case "reset":
                    if(args.length > 1 && args[1].equalsIgnoreCase("yes")) {
                        blocks.clear();
                        blocksYaml.set("blocks", new ArrayList<>());
                        saveBlocks();
                        sender.sendMessage(ChatColor.YELLOW + "Tous les groupes ont été supprimés.");
                    }else {
                        sender.sendMessage(ChatColor.RED + "/magic reset yes");
                    }
                    break;
                case "deletegroup":
                    if(args.length > 1) {
                        if(blocks.containsKey(args[1])) {
                            blocks.remove(args[1]);
                            blocksYaml.set("blocks." + args[1], null);
                            try {
                                Set<String> f = blocksYaml.getConfigurationSection("blocks").getKeys(false);
                            }catch(NullPointerException e) {
                                blocksYaml.set("blocks", new ArrayList<>());
                            }
                            saveBlocks();
                            sender.sendMessage(ChatColor.YELLOW + "Groupe supprimé avec succès ! " + ChatColor.DARK_AQUA + "[" + args[1] + "]");
                        }else {
                            sender.sendMessage(ChatColor.RED + "Ce groupe n'existe pas.");
                        }
                    }else {
                        sender.sendMessage(ChatColor.RED + "/magic deletegroup <nom du groupe>");
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Argument invalide.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if(sender instanceof Player) {
            switch(args.length) {
                case 1:
                    for(String str : new String[]{"add", "remove", "number", "checkerror", "settimer", "showtimer", "replace", "showmaterials", "reset", "deletegroup"}) {
                        if(str.startsWith(args[0].toLowerCase())) {
                            list.add(str);
                        }
                    }
                    break;
                case 2:
                    try {
                        for(String str : blocks.keySet()) {
                            if(str.startsWith(args[1])) {
                                list.add(str);
                            }
                        }
                    }catch(NullPointerException ignored) {}
                    break;
                case 3:
                case 4:
                    if(args[0].equalsIgnoreCase("replace")) {
                        for(Material material : Material.values()) {
                            String name = material.toString();
                            if(name.startsWith(args[args.length - 1].toUpperCase())) {
                                list.add(name);
                            }
                        }
                    }
                    break;
            }
        }
        Collections.sort(list);
        return list;
    }

    public static boolean checkName(String value) {
        if(value.length() > 20) {
            return false;
        }
        for(char c : value.toCharArray()) {
            if(!((c >= 65 && c <= 90) || (c >= 97 && c <= 122) || (c >= 48 && c <= 57))) {
                return false;
            }
        }
        return true;
    }
}

