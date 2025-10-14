package cam72cam.immersiverailroading.gui;

import cam72cam.immersiverailroading.gui.components.ListSelector;
import cam72cam.immersiverailroading.registry.UnitDefinition;
import cam72cam.mod.MinecraftClient;
import cam72cam.mod.entity.Player;
import cam72cam.mod.gui.helpers.GUIHelpers;
import cam72cam.mod.gui.screen.Button;
import cam72cam.mod.gui.screen.IScreen;
import cam72cam.mod.gui.screen.IScreenBuilder;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.render.opengl.RenderState;

import java.util.ArrayDeque;
import java.util.Deque;


public class MultiUnitGui implements IScreen {
    private UnitDefinition current;

    //Main Panel
    private Button selection;
    private ListSelector<UnitDefinition> definitionsSelector;

    //Edit Panel
    private Deque<Button> num;
    private Deque<Button> stockDef;
    private Deque<Button> swapPrev;
    private Deque<Button> swapNext;

    private Deque<UnitDefinition.Stock> builder;
    public MultiUnitGui() {
        this(MinecraftClient.getPlayer().getHeldItem(Player.Hand.PRIMARY));
    }

    private MultiUnitGui(ItemStack stack) {
        stack = stack.copy();
    }

    @Override
    public void init(IScreenBuilder iScreenBuilder) {
        int xtop = -GUIHelpers.getScreenWidth() / 3;
        int ytop = -GUIHelpers.getScreenHeight() / 3;
        //Main Panel

        selection = new Button(iScreenBuilder, xtop, ytop, 200, 20, "Select Unit Def") {
            @Override
            public void onClick(Player.Hand hand) {
                definitionsSelector.setVisible(!definitionsSelector.isVisible());
            }
        };

//        definitionsSelector = new ListSelector<UnitDefinition>() {
//            @Override
//            public void onClick(UnitDefinition option) {
//
//            }
//        };

        //Edit Panel
        num = new ArrayDeque<>();
        stockDef = new ArrayDeque<>();
        swapPrev = new ArrayDeque<>();
        swapNext = new ArrayDeque<>();

        xtop = -GUIHelpers.getScreenWidth() / 3;
        ytop = -GUIHelpers.getScreenHeight() / 3;
        for (int i = 0; i < 8; i++) {
            num.add(new Button(iScreenBuilder, xtop, ytop, 20, 20, "No.1") {
                @Override
                public void onClick(Player.Hand hand) {

                }
            });
            num.getLast().setEnabled(false);
            stockDef.add(new Button(iScreenBuilder, xtop + 20, ytop, 200, 20, "stock1") {
                @Override
                public void onClick(Player.Hand hand) {

                }
            });
            swapPrev.add(new Button(iScreenBuilder, xtop + 220, ytop, 200, 20, "⇅") {
                @Override
                public void onClick(Player.Hand hand) {

                }
            });
            swapNext.add(new Button(iScreenBuilder, xtop + 240, ytop, 200, 20, "⇵") {
                @Override
                public void onClick(Player.Hand hand) {

                }
            });
            ytop += 20;
        }
    }

    @Override
    public void onEnterKey(IScreenBuilder iScreenBuilder) {

    }

    @Override
    public void onClose() {

    }

    @Override
    public void draw(IScreenBuilder builder, RenderState state) {
        IScreen.super.draw(builder, state);

        GUIHelpers.drawRect(0, 0, GUIHelpers.getScreenWidth(), GUIHelpers.getScreenHeight(), 0xCC000000);
    }
}
