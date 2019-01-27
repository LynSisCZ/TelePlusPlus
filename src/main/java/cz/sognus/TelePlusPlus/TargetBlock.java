package cz.sognus.TelePlusPlus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author toi Thanks to Raphfrk for optimization of this class.
 */
public class TargetBlock {
    private Location loc;
    private double viewHeight;
    private int maxDistance;
    private HashMap<String, Material> blockToIgnoreMap;
    private double checkDistance, curDistance;
    private double xRotation, yRotation;
    private Vector targetPos = new Vector();
    private Vector targetPosDouble = new Vector();
    private Vector prevPos = new Vector();
    private Vector offset = new Vector();

    /**
     * @deprecated since 3.0.1 due to performance issues with array lookup
     */
    @Deprecated
    private Material[] blockToIgnore;

    /**
     * @param player           What player to work with
     * @param maxDistance      How far it checks for blocks
     * @param checkDistance    How often to check for blocks, the smaller the more precise
     * @param blockToIgnoreMap Map of what block to ignore while checking for viable targets
     * @author Sognus
     * @since 3.0.1
     */
    public TargetBlock(Player player, int maxDistance, double checkDistance, HashMap<String, Material> blockToIgnoreMap) {
        this.setValues(player.getLocation(), maxDistance, 1.65, checkDistance, blockToIgnoreMap);
    }

    /**
     * @param player           What player to work with
     * @param maxDistance      How far it checks for blocks
     * @param checkDistance    How often to check for blocks, the smaller the more precise
     * @param blockToIgnoreMap Map of what block to ignore while checking for viable targets
     * @author Sognus
     * @since 3.0.1
     */
    public TargetBlock(Player player, int maxDistance, double checkDistance, double viewHeight, HashMap<String, Material> blockToIgnoreMap) {
        this.setValues(player.getLocation(), maxDistance, viewHeight, checkDistance, blockToIgnoreMap);
    }


    /**
     * @param player         What player to work with
     * @param maxDistance    How far it checks for blocks
     * @param checkDistance  How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore String ArrayList of what block ids to ignore while checking for viable targets
     * @author Sognus
     * @since 3.0.0
     * @deprecated since 3.0.1 due to performance issues with array lookup
     * <p>
     * <p>
     * Constructor requiring a player, max distance, checking distance and an array of blocks to ignore
     */
    @Deprecated
    public TargetBlock(Player player, int maxDistance, double checkDistance, ArrayList<String> blocksToIgnore) {
        Material[] bti = this.convertStringArraytoMaterialArray(blocksToIgnore);
        this.setValues(player.getLocation(), maxDistance, 1.65, checkDistance, bti);
    }

    /**
     * @param player Player to work with
     * @deprecated since 3.0.1 due to performance issues with array lookup
     * <p>
     * Constructor requiring a player, uses default values
     */
    @Deprecated
    public TargetBlock(Player player) {
        this.setValues(player.getLocation(), 300, 1.65, 0.2, (Material[]) null);
    }

    /**
     * @param loc Location to work with
     * @deprecated since 3.0.1 due to performance issues with array lookup
     * <p>
     * Constructor requiring a location, uses default values
     */
    @Deprecated
    public TargetBlock(Location loc) {
        this.setValues(loc, 300, 0, 0.2, (Material[]) null);
    }

    /**
     * @param player        Player to work with
     * @param maxDistance   How far it checks for blocks
     * @param checkDistance How often to check for blocks, the smaller the more precise
     * @deprecated since 3.0.1 due to performance issues with array lookup
     * <p>
     * Constructor requiring a player, max distance and a checking distance
     */
    @Deprecated
    public TargetBlock(Player player, int maxDistance, double checkDistance) {
        this.setValues(player.getLocation(), maxDistance, 1.65, checkDistance, (Material[]) null);
    }

    /**
     * @param loc           What location to work with
     * @param maxDistance   How far it checks for blocks
     * @param checkDistance How often to check for blocks, the smaller the more precise
     * @deprecated since 3.0.1 due to performance issues with array lookup
     * <p>
     * Constructor requiring a location, max distance and a checking distance
     */
    @Deprecated
    public TargetBlock(Location loc, int maxDistance, double checkDistance) {
        this.setValues(loc, maxDistance, 0, checkDistance, (Material[]) null);
    }

    /**
     * @param player         What player to work with
     * @param maxDistance    How far it checks for blocks
     * @param checkDistance  How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore Material array of what block ids to ignore while checking for viable targets
     * @deprecated since 3.0.1 due to performance issues with array lookup
     * <p>
     * Constructor requiring a player, max distance, checking distance and an array of blocks to ignore
     */
    @Deprecated
    public TargetBlock(Player player, int maxDistance, double checkDistance, Material[] blocksToIgnore) {
        this.setValues(player.getLocation(), maxDistance, 1.65, checkDistance, blocksToIgnore);
    }

    /**
     * @param player         What player to work with
     * @param maxDistance    How far it checks for blocks
     * @param checkDistance  How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore List of Material of what block ids to ignore while checking for viable targets
     * @deprecated since 3.0.1 due to performance issues with array lookup
     */
    @Deprecated
    public TargetBlock(Player player, int maxDistance, double checkDistance, List<Material> blocksToIgnore) {
        Material[] arr = blocksToIgnore.toArray(new Material[blocksToIgnore.size()]);
        this.setValues(player.getLocation(), maxDistance, 1.65, checkDistance, arr);
    }

    /**
     * @param loc            What location to work with
     * @param maxDistance    How far it checks for blocks
     * @param checkDistance  How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore Array of what block ids to ignore while checking for viable targets
     * @deprecated since 3.0.1 due to performance issues with array lookup
     * <p>
     * Constructor requiring a location, max distance, checking distance and an array of blocks to ignore
     */
    @Deprecated
    public TargetBlock(Location loc, int maxDistance, double checkDistance, Material[] blocksToIgnore) {
        this.setValues(loc, maxDistance, 0, checkDistance, blocksToIgnore);
    }


    /**
     * @param loc            Location of the view
     * @param maxDistance    How far it checks for blocks
     * @param viewHeight     Where the view is positioned in y-axis
     * @param checkDistance  How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore Ids of blocks to ignore while checking for viable targets
     * @deprecated since 3.0.1 due to performance issues with array lookup
     * <p>
     * Set the values, all constructors uses this function
     */
    @Deprecated
    private void setValues(Location loc, int maxDistance, double viewHeight, double checkDistance, Material[] blocksToIgnore) {
        this.loc = loc;
        this.maxDistance = maxDistance;
        this.viewHeight = viewHeight;
        this.checkDistance = checkDistance;
        this.blockToIgnore = blocksToIgnore;
        this.curDistance = 0;
        xRotation = (loc.getYaw() + 90) % 360;
        yRotation = loc.getPitch() * -1;

        double h = (checkDistance * Math.cos(Math.toRadians(yRotation)));
        offset.setY((checkDistance * Math.sin(Math.toRadians(yRotation))));
        offset.setX((h * Math.cos(Math.toRadians(xRotation))));
        offset.setZ((h * Math.sin(Math.toRadians(xRotation))));

        targetPosDouble = new Vector(loc.getX(), loc.getY() + viewHeight, loc.getZ());
        targetPos = new Vector(targetPosDouble.getBlockX(), targetPosDouble.getBlockY(), targetPosDouble.getBlockZ());
        prevPos = targetPos.clone();
    }

    /**
     * @param loc               Location of the view
     * @param maxDistance       How far it checks for blocks
     * @param viewHeight        Where the view is positioned in y-axis
     * @param checkDistance     How often to check for blocks, the smaller the more precise
     * @param blocksToIgnoreMap Ids of blocks to ignore while checking for viable targets
     * @author Sognus
     * @since 3.0.1
     * <p>
     * Set the values, all constructors uses this function
     */
    private void setValues(Location loc, int maxDistance, double viewHeight, double checkDistance, HashMap<String, Material> blocksToIgnoreMap) {
        this.loc = loc;
        this.maxDistance = maxDistance;
        this.viewHeight = viewHeight;
        this.checkDistance = checkDistance;
        this.blockToIgnoreMap = blocksToIgnoreMap;
        this.curDistance = 0;
        xRotation = (loc.getYaw() + 90) % 360;
        yRotation = loc.getPitch() * -1;

        double h = (checkDistance * Math.cos(Math.toRadians(yRotation)));
        offset.setY((checkDistance * Math.sin(Math.toRadians(yRotation))));
        offset.setX((h * Math.cos(Math.toRadians(xRotation))));
        offset.setZ((h * Math.sin(Math.toRadians(xRotation))));

        targetPosDouble = new Vector(loc.getX(), loc.getY() + viewHeight, loc.getZ());
        targetPos = new Vector(targetPosDouble.getBlockX(), targetPosDouble.getBlockY(), targetPosDouble.getBlockZ());
        prevPos = targetPos.clone();
    }

    /**
     * Call this to reset checking position to allow you to check for a new target with the same TargetBlock instance.
     */
    public void reset() {
        targetPosDouble = new Vector(loc.getX(), loc.getY() + viewHeight, loc.getZ());
        targetPos = new Vector(targetPosDouble.getBlockX(), targetPosDouble.getBlockY(), targetPosDouble.getBlockZ());
        prevPos = targetPos.clone();
        this.curDistance = 0;
    }

    /**
     * Gets the distance to a block. Measures from the block underneath the player to the targetblock Should only be used when passing player as an constructor parameter
     *
     * @return double
     */
    public double getDistanceToBlock() {
        Vector blockUnderPlayer = new Vector((int) Math.floor(loc.getX() + 0.5), (int) Math.floor(loc.getY() - 0.5), (int) Math.floor(loc.getZ() + 0.5));

        Block blk = getTargetBlock();
        double x = blk.getX() - blockUnderPlayer.getBlockX();
        double y = blk.getY() - blockUnderPlayer.getBlockY();
        double z = blk.getZ() - blockUnderPlayer.getBlockZ();

        return Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)));
    }

    /**
     * Gets the rounded distance to a block. Measures from the block underneath the player to the targetblock Should only be used when passing player as an constructor parameter
     *
     * @return int
     */
    public int getDistanceToBlockRounded() {
        Vector blockUnderPlayer = new Vector((int) Math.floor(loc.getX() + 0.5), (int) Math.floor(loc.getY() - 0.5), (int) Math.floor(loc.getZ() + 0.5));

        Block blk = getTargetBlock();
        double x = blk.getX() - blockUnderPlayer.getBlockX();
        double y = blk.getY() - blockUnderPlayer.getBlockY();
        double z = blk.getZ() - blockUnderPlayer.getBlockZ();

        return (int) Math.round((Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)))));
    }

    /**
     * Gets the floored x distance to a block.
     *
     * @return int
     */
    public int getXDistanceToBlock() {
        this.reset();
        return (int) Math.floor(getTargetBlock().getX() - loc.getBlockX() + 0.5);
    }

    /**
     * Gets the floored y distance to a block
     *
     * @return int
     */
    public int getYDistanceToBlock() {
        this.reset();
        return (int) Math.floor(getTargetBlock().getY() - loc.getBlockY() + viewHeight);
    }

    /**
     * Gets the floored z distance to a block
     *
     * @return int
     */
    public int getZDistanceToBlock() {
        this.reset();
        return (int) Math.floor(getTargetBlock().getZ() - loc.getBlockZ() + 0.5);
    }

    /**
     * Returns the block at the sight. Returns null if out of range or if no viable target was found
     *
     * @return Block
     */
    public Block getTargetBlock() {
        this.reset();
        while ((getNextBlock() != null) && ((getCurrentBlock().getType() == Material.AIR) || this.isBlockToIgnore(getCurrentBlock().getType()))) {
        }
        return getCurrentBlock();
    }

    /**
     * Sets the type of the block at the sight. Returns false if the block wasn't set.
     *
     * @param type Material to set the block to
     * @return boolean
     */
    public boolean setTargetBlock(Material type) {
        this.reset();
        while ((getNextBlock() != null) && ((getCurrentBlock().getType() == Material.AIR) || this.isBlockToIgnore(getCurrentBlock().getType()))) {
        }
        if (getCurrentBlock() != null) {
            Block blk = loc.getWorld().getBlockAt(targetPos.getBlockX(), targetPos.getBlockY(), targetPos.getBlockZ());
            blk.setType(type);
            return true;
        }
        return false;
    }

    /**
     * Sets the type of the block at the sight. Returns false if the block wasn't set. Observe! At the moment this function is using the built-in enumerator function .valueOf(String) but would preferably be changed to smarter function, when implemented
     *
     * @param type Name of type to set the block to
     * @return boolean
     */
    public boolean setTargetBlock(String type) {
        Material mat = Material.valueOf(type);
        if (mat != null) {
            this.reset();
            while ((getNextBlock() != null) && ((getCurrentBlock().getType() == Material.AIR) || this.isBlockToIgnore(getCurrentBlock().getType()))) {
            }
            if (getCurrentBlock() != null) {
                Block blk = loc.getWorld().getBlockAt(targetPos.getBlockX(), targetPos.getBlockY(), targetPos.getBlockZ());
                blk.setType(mat);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the type of the block attached to the face at the sight. Returns false if the block wasn't set.
     *
     * @param type
     * @return boolean
     */
    public boolean setFaceBlock(Material type) {
        if (getCurrentBlock() != null) {
            Block blk = loc.getWorld().getBlockAt(prevPos.getBlockX(), prevPos.getBlockY(), prevPos.getBlockZ());
            blk.setType(type);
            return true;
        }
        return false;
    }

    /**
     * Sets the type of the block attached to the face at the sight. Returns false if the block wasn't set. Observe! At the moment this function is using the built-in enumerator function .valueOf(String) but would preferably be changed to smarter function, when implemented
     *
     * @param type
     * @return boolean
     */
    public boolean setFaceBlock(String type) {
        Material mat = Material.valueOf(type);
        if (mat != null) {
            if (getCurrentBlock() != null) {
                Block blk = loc.getWorld().getBlockAt(prevPos.getBlockX(), prevPos.getBlockY(), prevPos.getBlockZ());
                blk.setType(mat);
                return true;
            }
        }
        return false;
    }

    /**
     * Get next block
     *
     * @return Block
     */
    public Block getNextBlock() {
        prevPos = targetPos.clone();
        do {
            curDistance += checkDistance;

            targetPosDouble.setX(offset.getX() + targetPosDouble.getX());
            targetPosDouble.setY(offset.getY() + targetPosDouble.getY());
            targetPosDouble.setZ(offset.getZ() + targetPosDouble.getZ());
            targetPos = new Vector(targetPosDouble.getBlockX(), targetPosDouble.getBlockY(), targetPosDouble.getBlockZ());
        } while (curDistance <= maxDistance && targetPos.getBlockX() == prevPos.getBlockX() && targetPos.getBlockY() == prevPos.getBlockY() && targetPos.getBlockZ() == prevPos.getBlockZ());
        if (curDistance > maxDistance) {
            return null;
        }

        return this.loc.getWorld().getBlockAt(this.targetPos.getBlockX(), this.targetPos.getBlockY(), this.targetPos.getBlockZ());
    }

    /**
     * Returns the current block along the line of vision
     *
     * @return Block
     */
    public Block getCurrentBlock() {
        if (curDistance > maxDistance) {
            return null;
        } else {
            return this.loc.getWorld().getBlockAt(this.targetPos.getBlockX(), this.targetPos.getBlockY(), this.targetPos.getBlockZ());
        }
    }

    /**
     * Sets current block type. Returns false if the block wasn't set.
     *
     * @param type
     */
    public boolean setCurrentBlock(Material type) {
        Block blk = getCurrentBlock();
        if (blk != null) {
            blk.setType(type);
            return true;
        }
        return false;
    }

    /**
     * Sets current block type. Returns false if the block wasn't set. Observe! At the moment this function is using the built-in enumerator function .valueOf(String) but would preferably be changed to smarter function, when implemented
     *
     * @param type
     */
    public boolean setCurrentBlock(String type) {
        Material mat = Material.valueOf(type);
        if (mat != null) {
            Block blk = getCurrentBlock();
            if (blk != null) {
                blk.setType(mat);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the previous block in the aimed path
     *
     * @return Block
     */
    public Block getPreviousBlock() {
        return this.loc.getWorld().getBlockAt(prevPos.getBlockX(), prevPos.getBlockY(), prevPos.getBlockZ());
    }

    /**
     * Sets previous block type id. Returns false if the block wasn't set.
     *
     * @param type
     */
    public boolean setPreviousBlock(Material type) {
        Block blk = getPreviousBlock();
        if (blk != null) {
            blk.setType(type);
            return true;
        }
        return false;
    }

    /**
     * Sets previous block type id. Returns false if the block wasn't set. Observe! At the moment this function is using the built-in enumerator function .valueOf(String) but would preferably be changed to smarter function, when implemented
     *
     * @param type
     */
    public boolean setPreviousBlock(String type) {
        Material mat = Material.valueOf(type);
        if (mat != null) {
            Block blk = getPreviousBlock();
            if (blk != null) {
                blk.setType(mat);
                return true;
            }
        }
        return false;
    }

    /**
     * @deprecated since 3.0.1
     */
    private Material[] convertStringArraytoMaterialArray(ArrayList<String> array) {
        if (array != null) {
            Material[] matarray = new Material[array.size()];
            for (int i = 0; i < array.size(); i++) {
                try {
                    matarray[i] = Material.getMaterial(array.get(i));
                } catch (NumberFormatException nfe) {
                    matarray[i] = null;
                }
            }
            return matarray;
        }
        return null;
    }

    /**
     * @deprecated since 3.0.1
     */
    @Deprecated
    private int[] convertIntListtoIntArray(List<Integer> array) {
        if (array != null) {
            int[] intarray = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                try {
                    intarray[i] = array.get(i);
                } catch (NumberFormatException nfe) {
                    intarray[i] = 0;
                }
            }
            return intarray;
        }
        return null;
    }


    public Block getFaceBlock() {
        while ((getNextBlock() != null) && ((getCurrentBlock().getType() == Material.AIR) || this.isBlockToIgnore(getCurrentBlock().getType()))) {
        }
        if (getCurrentBlock() != null) {
            return getPreviousBlock();
        } else {
            return null;
        }
    }

    /**
     * @param value Material lookup
     * @return array contains value
     * @deprecated since 3.0.1 due to performance issues
     */
    @Deprecated
    private boolean blockToIgnoreHasValue(Material value) {
        if (this.blockToIgnore != null) {
            if (this.blockToIgnore.length > 0) {
                for (Material i : this.blockToIgnore) {
                    if (i == value) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param lookup material to check
     * @return whether map contains given material
     * @author Sognus
     * @since 3.0.1
     */
    private boolean isBlockToIgnore(Material lookup) {
        return (this.blockToIgnoreMap.get(lookup.name()) != null);
    }
}
