package net.kitcake.magic;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Group {

    private List<Block> blocks;
    private final String groupName;
    private long timer;      // Temps en millisecondes
    private long lastChange = Main.start;
    private Material material1;
    private Material material2;
    private boolean itsMaterial1 = true;

    public Group(String groupName, long timer, Material material1, Material material2) {
        this.blocks = new ArrayList<>();
        this.groupName = groupName;
        this.timer = timer;
        this.material1 = material1;
        this.material2 = material2;
    }

    public void setTimer(long timer) {
        this.timer = timer;
        Main.blocksYaml.set("blocks." + groupName + ".options.timer", timer);
        Main.saveBlocks();
    }

    public void setMaterial1(Material material1) {
        this.material1 = material1;
        Main.blocksYaml.set("blocks." + groupName + ".options.material1", material1.toString());
        Main.saveBlocks();
    }

    public void setMaterial2(Material material2) {
        this.material2 = material2;
        Main.blocksYaml.set("blocks." + groupName + ".options.material2", material2.toString());
        Main.saveBlocks();
    }

    public void setMaterials(Material material1, Material material2) {
        this.material1 = material1;
        Main.blocksYaml.set("blocks." + groupName + ".options.material1", material1.toString());
        this.material2 = material2;
        Main.blocksYaml.set("blocks." + groupName + ".options.material2", material2.toString());
        Main.saveBlocks();
    }

    public void setLastChange(long value) {
        lastChange = value;
    }

    public void setItsMaterial1(boolean itsMaterial1) {
        this.itsMaterial1 = itsMaterial1;
    }

    public boolean addBlock(Block block) {
        if(!blocks.contains(block)) {
            blocks.add(block);
            String path = "blocks." + groupName + "." + block.getX() + block.getY() + block.getZ() + ".";
            Main.blocksYaml.set(path + "world", block.getWorld().getName());
            Main.blocksYaml.set(path + "x", block.getX());
            Main.blocksYaml.set(path + "y", block.getY());
            Main.blocksYaml.set(path + "z", block.getZ());
            Main.blocksYaml.set(path + "date-added", System.currentTimeMillis());
            Main.saveBlocks();
            return true;
        }
        return false;
    }

    public boolean removeBlock(Block block) {
        if(blocks.remove(block)) {
            Main.blocksYaml.set("blocks." + groupName + "." + block.getX() + block.getY() + block.getZ(), null);
            try {
                Set<String> ET = Main.blocksYaml.getConfigurationSection("blocks." + groupName).getKeys(false);
                if(ET.size() == 1) {
                    Main.blocksYaml.set("blocks." + groupName, null);
                }
            }catch(NullPointerException e) {
                Main.blocksYaml.set("blocks." + groupName, null);
            }
            Main.saveBlocks();
            return true;
        }
        return false;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public long getTimer() {
        return timer;
    }

    public Material getMaterial1() {
        return material1;
    }

    public Material getMaterial2() {
        return material2;
    }

    public long getLastChange() {
        return lastChange;
    }

    public boolean isItsMaterial1() {
        return itsMaterial1;
    }
}
