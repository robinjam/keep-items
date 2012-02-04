package net.robinjam.bukkit.keepitems;

import java.util.ArrayList;
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
    
    public Death(Location location, List<ItemStack> drops, int experience) {
        this.location = location;
        this.drops = new ArrayList<ItemStack>();
        for (ItemStack is : drops) {
            this.drops.add(is.clone());
        }
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
        (loc.getWorld().spawn(loc, ExperienceOrb.class)).setExperience(experience);
    }
    
}
