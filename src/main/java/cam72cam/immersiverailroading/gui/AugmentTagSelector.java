package cam72cam.immersiverailroading.gui;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.gui.components.ListSelector;
import cam72cam.immersiverailroading.items.ItemRollingStock;
import cam72cam.immersiverailroading.net.AugmentTagPacket;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.mod.MinecraftClient;
import cam72cam.mod.entity.Player;
import cam72cam.mod.gui.screen.IScreen;
import cam72cam.mod.gui.screen.IScreenBuilder;
import cam72cam.mod.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AugmentTagSelector implements IScreen {
    private TileRailBase tileRailBase;
    private String current;
    private List<String> candidates;
    private ListSelector<String> listSelector;

    public AugmentTagSelector(TileRailBase tileRailBase) {
        this.tileRailBase = tileRailBase;
    }

    @Override
    public void init(IScreenBuilder screen) {
        Player player = MinecraftClient.getPlayer();
        this.candidates = new ArrayList<>();
        if(player != null && player.getHeldItem(Player.Hand.PRIMARY).is(IRItems.ITEM_ROLLING_STOCK)) {
            ItemStack stack = player.getHeldItem(Player.Hand.PRIMARY);
            EntityRollingStockDefinition definition = new  ItemRollingStock.Data(stack).def;
            candidates.add(definition.name);
            if(!Objects.equals(definition.packName, "N/A")){
                candidates.add(definition.packName);
            }
            if(!Objects.equals(definition.modelerName, "N/A")){
                candidates.add(definition.modelerName);
            }
            candidates.addAll(definition.tags);
        }
        this.current = tileRailBase.getStockTag();
        HashMap<String, String> options = new HashMap<>();
        this.candidates.forEach(s -> options.put(s,s));
        this.listSelector = new ListSelector<String>(screen, 100, 120, 20, current, options) {
            @Override
            public void onClick(String option) {
                current = option;
            }
        };
        this.listSelector.setVisible(true);
    }

    @Override
    public void onEnterKey(IScreenBuilder builder) {
        builder.close();
    }

    @Override
    public void onClose() {
        if(current != null){
            new AugmentTagPacket(tileRailBase.getPos(), current).sendToServer();
        }
    }
}
