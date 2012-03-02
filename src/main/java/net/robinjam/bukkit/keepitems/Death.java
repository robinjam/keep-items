package net.robinjam.bukkit.keepitems;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a death event, recording its location and the items dropped.
 * 
 * @author robinjam
 */
public class Death {
    
    private Location location;
    private List<ItemStack> drops;
    private int experience;
    
    /**
     * @param location The location at which the player died
     * @param drops The items that would have been dropped
     * @param experience The amount of experience the player had when they died
     */
    public Death(Location location, List<ItemStack> drops, int experience) {
        this.location = location;
        this.drops = drops;
        this.experience = experience;
    }
    
    /**
     * Drops every item held by this instance, at the location where the player died.
     */
    public void drop() {
        drop(location);
    }
    
    /**
     * Drops every item held by this instance, at the given location.
     */
    public void drop(Location loc) {
        for (ItemStack is : drops)
            loc.getWorld().dropItem(loc, is);
        
        if (experience > 0)
            (loc.getWorld().spawn(loc, ExperienceOrb.class)).setExperience(experience);
    }
    
}
