package cam72cam.immersiverailroading.gui.components;

import cam72cam.immersiverailroading.util.MathUtil;
import cam72cam.mod.entity.Player;
import cam72cam.mod.gui.screen.Button;
import cam72cam.mod.gui.screen.IScreenBuilder;
import cam72cam.mod.gui.screen.Slider;
import cam72cam.mod.gui.screen.TextField;
import net.minecraft.util.math.MathHelper;

public class NumberInputer {
    public TextField number;
    public Slider slider;
    public Button btn;

    private double content;
    private final double min;
    private final double max;
    private final boolean doublePrecision;
    private boolean flag;

    public NumberInputer(IScreenBuilder builder, int x, int y, int width, int height, String text, double min, double max, double start, boolean doublePrecision) {
        if (width <= 2 * height) {
            throw new IllegalStateException();
        }

        number = new TextField(builder, x, y, width - height, height);
        number.setValidator(s -> {
            try {
                double d = Double.parseDouble(s);
                if (doublePrecision) {
                    content = MathHelper.clamp(d, min, max);
                } else {
                    content = MathHelper.clamp((int) d, (int) min, (int) max);
                }

                return true;
            } catch (Exception e) {
                return false;
            }
        });
        slider = new Slider(builder, x, y, /*width, height,*/ text, min, max, start, doublePrecision) {
            @Override
            public void onSlider() {
                content = slider.getValue();
                slider.setText(text + String.format(doublePrecision ? "%.4f" : "%d", content));
            }
        };
        btn = new Button(builder, x + width - height, y, height, height, "↺") {
            @Override
            public void onClick(Player.Hand hand) {
                if(flag) {
                    number.setVisible(false);
                    slider.setVisible(true);
//                    slider.setValue(content)
                } else {
                    number.setVisible(true);
                    slider.setVisible(false);
                    number.setText(String.format(doublePrecision ? "%.4f" : "%d", content));
                }
                flag = !flag;
            }
        };
        flag = false;
        btn.setVisible(true);
        btn.onClick(Player.Hand.PRIMARY);
        slider.onSlider();

        this.min = min;
        this.max = max;
        this.doublePrecision = doublePrecision;
    }

    public double getValue() {
        return content;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            flag = !flag;
            btn.setVisible(true);
            btn.onClick(Player.Hand.PRIMARY);
        } else {
            btn.setVisible(false);
            number.setVisible(false);
            slider.setVisible(false);
        }
    }

    public void setValue(double value) {
        content = MathUtil.clamp(value, min, max);
//                    slider.setValue(content)
        number.setText(String.format(doublePrecision ? "%.4f" : "%d", content));
    }
}
