package cam72cam.immersiverailroading.gui.components;

import cam72cam.immersiverailroading.util.MathUtil;
import cam72cam.mod.entity.Player;
import cam72cam.mod.gui.screen.Button;
import cam72cam.mod.gui.screen.IScreenBuilder;
import cam72cam.mod.gui.screen.Slider;
import cam72cam.mod.gui.screen.TextField;

import java.util.function.Consumer;

public class NumberInputer {
    public TextField number;
    public Slider slider;
    public Button btn;

    private double content;
    private final double min;
    private final double max;
    private final boolean doublePrecision;
    private boolean flag;
    private final Consumer<Double> consumer;

    public NumberInputer(IScreenBuilder builder, int x, int y,
                         String prefix,
                         double min, double max, double start, boolean doublePrecision,
                         Consumer<Double> callback) {
        this(builder, x, y, 150, 20, prefix, "", min, max, start, doublePrecision, false, callback);
    }

    public NumberInputer(IScreenBuilder builder, int x, int y,
                         String prefix,
                         double min, double max, double start,
                         boolean doublePrecision, boolean useLogScale, Consumer<Double> callback) {
        this(builder, x, y, 150, 20, prefix, "", min, max, start, doublePrecision, useLogScale, callback);
    }

    public NumberInputer(IScreenBuilder builder, int x, int y, int width, int height,
                         String prefix,
                         double min, double max, double start,
                         boolean doublePrecision, Consumer<Double> callback) {
        this(builder, x, y, width, height, prefix, "",
             min, max, start, doublePrecision, false, callback);
    }

    public NumberInputer(IScreenBuilder builder, int x, int y, int width, int height,
                         String prefix, String suffix,
                         double min, double max, double start,
                         boolean doublePrecision, Consumer<Double> callback) {
        this(builder, x, y, width, height, prefix, suffix,
             min, max, start, doublePrecision, false, callback);
    }

    public NumberInputer(IScreenBuilder builder, int x, int y, int width, int height,
                         String prefix, String suffix,
                         double min, double max, double start,
                         boolean doublePrecision, boolean useLogScale, Consumer<Double> callback) {

        if (width <= 2 * height) {
            throw new IllegalStateException();
        }

        this.consumer = callback;
        this.min = min;
        this.max = max;
        this.doublePrecision = doublePrecision;

        start = MathUtil.clamp(start, min, max);
        content = start;

        number = new TextField(builder, x + 2, y + 2, width - height - 4, height - 4);
        number.setValidator(s -> {
            try {
                double d = Double.parseDouble(s);
                if (d < this.min || d > this.max) return false;

                content = this.doublePrecision ? d : Math.round(d);
                content = MathUtil.clamp(content, this.min, this.max);

                callback.accept(content);
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        double sliderMin = useLogScale ? Math.log10(min) : min;
        double sliderMax = useLogScale ? Math.log10(max) : max;
        double sliderStart = useLogScale ? Math.log10(start) : start;

        slider = new Slider(builder, x, y, width - height, height, prefix,
                            sliderMin, sliderMax, sliderStart,
                            doublePrecision, s -> {
            content = useLogScale ? Math.pow(10,
                                             Math.log10(min) + ((s.getValue() - sliderMin) / (sliderMax - sliderMin) * (sliderMax - sliderMin))
            ) : s.getValue();

            s.setText(prefix + String.format(doublePrecision ? "%.4f" : "%.0f", content) + suffix);
            callback.accept(doublePrecision ? content : Math.round(content));
        });

        btn = new Button(builder, x + width - height, y, height, height, "↺") {
            @Override
            public void onClick(Player.Hand hand) {
                toggleMode(useLogScale, prefix, suffix);
            }
        };

        flag = false;
        btn.setVisible(true);
        btn.onClick(Player.Hand.PRIMARY);
        number.setText(String.format(doublePrecision ? "%.4f" : "%.0f", content));
    }

    private void toggleMode(boolean useLogScale, String prefix, String suffix) {
        if (flag) {
            number.setVisible(false);
            slider.setVisible(true);

            if (useLogScale) {
                double v = Math.log10(max) - Math.log10(min);
                double value = (Math.log10(content) - Math.log10(min)) / v;
                slider.setValue(Math.log10(min) + value * v);
            } else {
                slider.setValue(content);
            }
            slider.onSlider();
        } else {
            number.setVisible(true);
            slider.setVisible(false);
            number.setText(String.format(doublePrecision ? "%.4f" : "%.0f", content));
        }
        flag = !flag;
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
        slider.setValue(content);
        number.setText(String.format(doublePrecision ? "%.4f" : "%.0f", content));
        consumer.accept(content);
    }
}