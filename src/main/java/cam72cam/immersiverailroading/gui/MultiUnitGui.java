package cam72cam.immersiverailroading.gui;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.gui.components.ListSelector;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.registry.MultiUnitDefinitionManager;
import cam72cam.immersiverailroading.registry.UnitDefinition;
import cam72cam.mod.MinecraftClient;
import cam72cam.mod.entity.Player;
import cam72cam.mod.gui.helpers.GUIHelpers;
import cam72cam.mod.gui.screen.Button;
import cam72cam.mod.gui.screen.IScreen;
import cam72cam.mod.gui.screen.IScreenBuilder;
import cam72cam.mod.gui.screen.TextField;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.net.Packet;
import cam72cam.mod.render.opengl.RenderState;
import cam72cam.mod.serialization.TagField;

import java.util.*;
import java.util.stream.Collectors;


public class MultiUnitGui implements IScreen {
    private UnitDefinition current;

    //Main Panel
    private Button selection;
    private Button newMU;
    private ListSelector<UnitDefinition> definitionsSelector;

    //Edit Panel
    private List<Button> num;
    private List<Button> stockDef;
    private List<Button> swapPrev;
    private List<Button> swapNext;

    private Button prevPage;
    private Button nextPage;
    private Button addStock;
    private Button saveMU;
    private TextField name;

    //Stock Addition Panel
    private Button stock;
    private ListSelector<EntityRollingStockDefinition> stockSelector;
    private Button texture;
    private ListSelector<String> textureSelector;
    private Button direction;
    private Button finish;

    private int currentPage = 0;

    private List<UnitDefinition.Stock> stockListBuilder;
    private int currentEditing = 0;
    UnitDefinition.Stock building;
    public MultiUnitGui() {
        this(MinecraftClient.getPlayer().getHeldItem(Player.Hand.PRIMARY));
    }

    private MultiUnitGui(ItemStack stack) {
        stack = stack.copy();
        current = MultiUnitDefinitionManager.getUnitDef(stack.getTagCompound().getString("multi_unit"));
    }

    @Override
    public void init(IScreenBuilder screen) {
        stockListBuilder = new ArrayList<>();
        int xtop = -GUIHelpers.getScreenWidth() / 2;
        int ytop = -GUIHelpers.getScreenHeight() / 4;
        //Main Panel

        selection = new Button(screen, xtop, ytop, 100, 20, "Select Unit Def") {
            @Override
            public void onClick(Player.Hand hand) {
                definitionsSelector.setVisible(!definitionsSelector.isVisible());
            }
        };
        ytop += 30;
        newMU = new Button(screen, xtop, ytop, 100, 20, "New stock") {
            @Override
            public void onClick(Player.Hand hand) {
                openEditPanel();
            }
        };

        definitionsSelector = new ListSelector<UnitDefinition>(screen, 100, 200, 20, current,
                                                               MultiUnitDefinitionManager.getUnits()) {
            @Override
            public void onClick(UnitDefinition option) {
                current = option;
            }
        };

        //Edit Panel
        num = new ArrayList<>();
        stockDef = new ArrayList<>();
        swapPrev = new ArrayList<>();
        swapNext = new ArrayList<>();

        xtop = -138;
        ytop = GUIHelpers.getScreenHeight() / 4 - 94;
        int currX = xtop;
        int spacing = 4;
        for (int i = 0; i < 8; i++) {
            Button button = new Button(screen, currX, ytop, 25, 20, "No." + i) {
                @Override
                public void onClick(Player.Hand hand) {}
            };
            button.setEnabled(false);
            num.add(button);
            currX += 25 + spacing;
            int finalI = i;
            stockDef.add(new Button(screen, currX, ytop, 200, 20, "") {
                @Override
                public void onClick(Player.Hand hand) {
                    openAdditionPanel(screen, currentPage*8 + finalI);
                }
            });
            currX += 200 + spacing;
            swapPrev.add(new Button(screen, currX, ytop, 20, 20, "↑") {
                @Override
                public void onClick(Player.Hand hand) {

                }
            });
            currX += 20 + spacing;
            swapNext.add(new Button(screen, currX, ytop, 20, 20, "↓") {
                @Override
                public void onClick(Player.Hand hand) {

                }
            });
            ytop += 20 + spacing;
            currX = xtop;
        }
        prevPage = new Button(screen, currX, ytop, 60, 20, "Prev Page") {
            @Override
            public void onClick(Player.Hand hand) {
                if(currentPage > 0) {
                    currentPage -= 1;
                    updatePage();
                }
            }
        };
        addStock = new Button(screen, currX + 88, ytop, 100, 20, "Add Stock") {
            @Override
            public void onClick(Player.Hand hand) {
                openAdditionPanel(screen);
            }
        };
        nextPage = new Button(screen, currX + 217, ytop, 50, 20, "Next Page") {
            @Override
            public void onClick(Player.Hand hand) {
                if(currentPage < stockListBuilder.size() / 8) {
                    currentPage += 1;
                    updatePage();
                }
            }
        };
        xtop = -GUIHelpers.getScreenWidth() / 2;
        ytop = -GUIHelpers.getScreenHeight() / 4;
        name = new TextField(screen, xtop, ytop, 199, 20);
        name.setText("Type your multiunit name here");
        ytop += 24;
        saveMU = new Button(screen, xtop, ytop, 100, 20, "Save") {
            @Override
            public void onClick(Player.Hand hand) {
                if(name.getText().isEmpty()) {
                    //Reject
                    return;
                }
                UnitDefinition.UnitDefBuilder builder1 = UnitDefinition.UnitDefBuilder.of(name.getText());
                for (UnitDefinition.Stock stock1 : stockListBuilder) {
                    builder1.appendStock(stock1);
                }
                UnitDefinition build = builder1.build();
                System.out.println(build);
                MultiUnitDefinitionManager.addUnit(build);
                closeEditPanel(screen);
            }
        };

        xtop = -GUIHelpers.getScreenWidth() / 2;
        ytop = -GUIHelpers.getScreenHeight() / 4;
        stock = new Button(screen, xtop, ytop, 200, 20, "Select Stock") {
            @Override
            public void onClick(Player.Hand hand) {
                if(textureSelector.isVisible()){
                    stockSelector.setVisible(true);
                    textureSelector.setVisible(false);
                }else{
                    stockSelector.setVisible(!stockSelector.isVisible());
                }
            }
        };
        Map<String, EntityRollingStockDefinition> definitionMap = DefinitionManager.getDefinitions().stream().collect(
                Collectors.toMap(EntityRollingStockDefinition::name, def -> def));
        stockSelector = new ListSelector<EntityRollingStockDefinition>(screen, 200, 200, 20, null, definitionMap) {
            @Override
            public void onClick(EntityRollingStockDefinition option) {
                if (building != null) {
                    building.definition = option;
                    stock.setText(option.name());
                    textureSelector = new ListSelector<String>(screen, 200, 200, 20, building.texture,
                                                               building.definition.textureNames.entrySet().stream()
                                                                                               .collect(Collectors.toMap(
                                                                                                       Map.Entry::getValue, Map.Entry::getKey,
                                                                                                       (u, v) -> u, LinkedHashMap::new))
                    ) {
                        @Override
                        public void onClick(String option) {
                            building.texture = option;
                            if(option.isEmpty()) {
                                texture.setText("Tex Variant:" + "default");
                            } else {
                                texture.setText("Tex Variant:" + option);
                            }
                        }
                    };
                    if(building.definition.textureNames.keySet().stream().findFirst().isPresent()){
                        building.texture = building.definition.textureNames.keySet().stream().findFirst().get();
                    } else {
                        building.texture = null;
                    }
                }
            }
        };
        ytop += 25;
        texture = new Button(screen, xtop, ytop, 200, 20, "Tex Variant:default") {
            @Override
            public void onClick(Player.Hand hand) {
                if(stockSelector.isVisible()) {
                    stockSelector.setVisible(false);
                    textureSelector.setVisible(true);
                } else {
                    textureSelector.setVisible(!textureSelector.isVisible());
                }
            }
        };
        ytop += 25;
        direction = new Button(screen, xtop, ytop, 200, 20, "") {
            @Override
            public void onClick(Player.Hand hand) {
                building.direction = ClickListHelper.next(building.direction, hand);
                this.setText("Direction:"+ building.direction);
            }
        };
        ytop += 25;
        finish = new Button(screen, xtop, ytop, 200, 20, "Save") {
            @Override
            public void onClick(Player.Hand hand) {
                if(building != null && building.definition != null){
                    closeAdditionPanel();
                }
            }
        };

        closeEditPanel(screen);
        updatePage();

        finish.setVisible(false);
        direction.setVisible(false);
        stock.setVisible(false);
        texture.setVisible(false);
        stockSelector.setVisible(false);
        definitionsSelector.setVisible(false);
    }

    private void updatePage() {
        for (int i = 1; i <= 8; i++) {
            if(currentPage*8 + i <= stockListBuilder.size()){
                this.num.get(i-1).setVisible(true);
                this.stockDef.get(i-1).setVisible(true);
                this.stockDef.get(i-1).setText(stockListBuilder.get(currentPage*8 + i - 1).definition.name());
                this.num.get(i-1).setText("No."+(currentPage*8 + i));
                this.swapPrev.get(i-1).setVisible(true);
                this.swapNext.get(i-1).setVisible(true);
            } else {
                this.num.get(i-1).setVisible(false);
                this.stockDef.get(i-1).setVisible(false);
                this.swapPrev.get(i-1).setVisible(false);
                this.swapNext.get(i-1).setVisible(false);
            }
            if(currentPage*8 + i == 1) {
                this.swapPrev.get(i-1).setVisible(false);
            }
            if(currentPage*8 + i == stockListBuilder.size()) {
                this.swapNext.get(i-1).setVisible(false);
            }
        }
    }

    private void openEditPanel() {
        this.selection.setVisible(false);
        this.newMU.setVisible(false);
        this.definitionsSelector.setVisible(false);

        this.num.forEach(b -> b.setVisible(true));
        this.stockDef.forEach(b -> b.setVisible(true));
        this.swapPrev.forEach(b -> b.setVisible(true));
        this.swapNext.forEach(b -> b.setVisible(true));
        this.prevPage.setVisible(true);
        this.nextPage.setVisible(true);
        this.addStock.setVisible(true);
        this.name.setVisible(true);
        this.saveMU.setVisible(true);

        stockListBuilder.clear();
        updatePage();
    }

    private void closeEditPanel(IScreenBuilder screen) {
        this.selection.setVisible(true);
        this.newMU.setVisible(true);

        this.num.forEach(b -> b.setVisible(false));
        this.stockDef.forEach(b -> b.setVisible(false));
        this.swapPrev.forEach(b -> b.setVisible(false));
        this.swapNext.forEach(b -> b.setVisible(false));
        this.prevPage.setVisible(false);
        this.nextPage.setVisible(false);
        this.addStock.setVisible(false);
        this.name.setVisible(false);
        this.name.setText("");
        this.saveMU.setVisible(false);

        definitionsSelector = new ListSelector<UnitDefinition>(screen, 100, 200, 20, current,
                                                               MultiUnitDefinitionManager.getUnits()) {
            @Override
            public void onClick(UnitDefinition option) {
                current = option;
            }
        };
    }

    private void openAdditionPanel(IScreenBuilder builder) {
        this.num.forEach(b -> b.setVisible(false));
        this.stockDef.forEach(b -> b.setVisible(false));
        this.swapPrev.forEach(b -> b.setVisible(false));
        this.swapNext.forEach(b -> b.setVisible(false));
        this.prevPage.setVisible(false);
        this.nextPage.setVisible(false);
        this.addStock.setVisible(false);
        this.name.setVisible(false);
        this.saveMU.setVisible(false);

        finish.setVisible(true);
        direction.setVisible(true);
        stock.setVisible(true);
        texture.setVisible(true);
        building = new UnitDefinition.Stock();
        building.direction = UnitDefinition.Direction.FORWARD;
        building.definition = DefinitionManager.getDefinitions().stream().findFirst().get();
        stock.setText(building.definition.name());
        this.currentEditing = -1;

        Map<String, EntityRollingStockDefinition> definitionMap = DefinitionManager.getDefinitions().stream().collect(
                Collectors.toMap(EntityRollingStockDefinition::name, def -> def));
        stockSelector = new ListSelector<EntityRollingStockDefinition>(builder, 200, 200, 20, null, definitionMap) {
            @Override
            public void onClick(EntityRollingStockDefinition option) {
                if (building != null) {
                    building.definition = option;
                    stock.setText(option.name());
                    textureSelector = new ListSelector<String>(builder, 200, 200, 20, building.texture,
                                                               building.definition.textureNames.entrySet().stream()
                                                                                               .collect(Collectors.toMap(
                                                                                                       Map.Entry::getKey, Map.Entry::getKey,
                                                                                                       (u, v) -> u, LinkedHashMap::new))
                    ) {
                        @Override
                        public void onClick(String option) {
                            building.texture = option;
                            if(option.isEmpty()) {
                                texture.setText("Tex Variant:" + "default");
                            } else {
                                texture.setText("Tex Variant:" + option);
                            }
                        }
                    };
                    if(building.definition.textureNames.keySet().stream().findFirst().isPresent()){
                        building.texture = building.definition.textureNames.keySet().stream().findFirst().get();
                    } else {
                        building.texture = null;
                    }
                }
            }
        };
        textureSelector = new ListSelector<String>(builder, 200, 200, 20, building.texture,
                                                   building.definition.textureNames.entrySet().stream()
                                                                                   .collect(Collectors.toMap(
                                                                                           Map.Entry::getValue, Map.Entry::getKey,
                                                                                           (u, v) -> u, LinkedHashMap::new))
        ) {
            @Override
            public void onClick(String option) {
                building.texture = option;
                if(option.isEmpty()) {
                    texture.setText("Tex Variant:" + "default");
                } else {
                    texture.setText("Tex Variant:" + option);
                }
            }
        };
        direction.setText("Direction:"+ building.direction);
        texture.setText("Tex Variant:" + (building.texture == null ? "default" : building.texture));
    }

    private void openAdditionPanel(IScreenBuilder builder, int num) {
        building = stockListBuilder.get(num);
        this.num.forEach(b -> b.setVisible(false));
        this.stockDef.forEach(b -> b.setVisible(false));
        this.swapPrev.forEach(b -> b.setVisible(false));
        this.swapNext.forEach(b -> b.setVisible(false));
        this.prevPage.setVisible(false);
        this.nextPage.setVisible(false);
        this.addStock.setVisible(false);
        this.name.setVisible(false);
        this.saveMU.setVisible(false);

        finish.setVisible(true);
        direction.setVisible(true);
        stock.setVisible(true);
        stock.setText(building.definition.name());
        texture.setVisible(true);

        this.currentEditing = num;

        Map<String, EntityRollingStockDefinition> definitionMap = DefinitionManager.getDefinitions().stream().collect(
                Collectors.toMap(EntityRollingStockDefinition::name, def -> def));
        stockSelector = new ListSelector<EntityRollingStockDefinition>(builder, 200, 200, 20, building.definition, definitionMap) {
            @Override
            public void onClick(EntityRollingStockDefinition option) {
                if (building != null) {
                    building.definition = option;
                    stock.setText(option.name());
                    textureSelector = new ListSelector<String>(builder, 200, 200, 20, building.texture,
                                                               building.definition.textureNames.entrySet().stream()
                                                                                               .collect(Collectors.toMap(
                                                                                                       Map.Entry::getValue, Map.Entry::getKey,
                                                                                                       (u, v) -> u, LinkedHashMap::new))
                    ) {
                        @Override
                        public void onClick(String option) {
                            building.texture = option;
                            if(option.isEmpty()) {
                                texture.setText("Tex Variant:" + "default");
                            } else {
                                texture.setText("Tex Variant:" + option);
                            }
                        }
                    };
                    if(building.definition.textureNames.keySet().stream().findFirst().isPresent()){
                        building.texture = building.definition.textureNames.keySet().stream().findFirst().get();
                    } else {
                        building.texture = null;
                    }
                }
            }
        };
        textureSelector = new ListSelector<String>(builder, 200, 200, 20, building.texture,
                                                   building.definition.textureNames.entrySet().stream()
                                                                                   .collect(Collectors.toMap(
                                                                                           Map.Entry::getValue, Map.Entry::getKey,
                                                                                           (u, v) -> u, LinkedHashMap::new))
        ) {
            @Override
            public void onClick(String option) {
                building.texture = option;
                if(option.isEmpty()) {
                    texture.setText("Tex Variant:" + "default");
                } else {
                    texture.setText("Tex Variant:" + option);
                }
            }
        };
        direction.setText("Direction:"+ building.direction);
        texture.setText("Tex Variant:" + building.texture);
    }

    private void closeAdditionPanel() {
        finish.setVisible(false);
        direction.setVisible(false);
        stock.setVisible(false);
        texture.setVisible(false);
        stockSelector.setVisible(false);
        textureSelector.setVisible(false);
        this.addStock.setVisible(true);
        this.name.setVisible(true);
        this.saveMU.setVisible(true);

        if (currentEditing == -1) {
            stockListBuilder.add(building);
        } else {
            stockListBuilder.set(currentEditing, building);
            currentEditing = -1;
        }

        building = null;
        updatePage();
    }

    @Override
    public void onEnterKey(IScreenBuilder iScreenBuilder) {
        iScreenBuilder.close();
    }

    @Override
    public void onClose() {
        if (current != null){
            new MultiUnitChangePacket(current.getName()).sendToServer();
        }
    }

    @Override
    public void draw(IScreenBuilder builder, RenderState state) {
        IScreen.super.draw(builder, state);

        GUIHelpers.drawRect(0, 0, GUIHelpers.getScreenWidth(), GUIHelpers.getScreenHeight(), 0xCC000000);
    }

    public static class MultiUnitChangePacket extends Packet {
        @TagField
        private String def;

        public MultiUnitChangePacket() {
        }

        public MultiUnitChangePacket(String def) {
            this.def = def;
        }

        @Override
        protected void handle() {
            Player player = this.getPlayer();
            ItemStack stack = player.getHeldItem(Player.Hand.PRIMARY);
            if(stack.is(IRItems.ITEM_MULTIPLE_UNIT)) {
                stack.setTagCompound(stack.getTagCompound().setString("multi_unit", def));
            }
            player.setHeldItem(Player.Hand.PRIMARY, stack);
        }
    }
}
