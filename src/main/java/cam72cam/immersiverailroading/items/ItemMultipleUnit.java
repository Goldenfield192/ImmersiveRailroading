package cam72cam.immersiverailroading.items;

import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.mod.entity.Player;
import cam72cam.mod.item.ClickResult;
import cam72cam.mod.item.CreativeTab;
import cam72cam.mod.item.CustomItem;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.util.Facing;
import cam72cam.mod.world.World;

import java.util.*;

public class ItemMultipleUnit extends CustomItem {

    public ItemMultipleUnit(String modID, String name) {
        super(modID, name);
    }

//    @Override
//    public List<String> getTooltip(ItemStack stack) {
//        //todo
//
//        return tooltip;
//    }

    @Override
    public List<CreativeTab> getCreativeTabs() {
        return Collections.singletonList(ItemTabs.MAIN_TAB);
    }

    @Override
    public ClickResult onClickBlock(Player player, World world, Vec3i pos, Player.Hand hand, Facing facing, Vec3d hit) {
//        return tryPlaceStock(player, world, pos, hand, null);
        return ClickResult.ACCEPTED;
    }

    @Override
    public void onClickAir(Player player, World world, Player.Hand hand) {
        super.onClickAir(player, world, hand);
        if (world.isClient) {
            GuiTypes.MULTI_UNIT.open(player);
        }
    }
}
