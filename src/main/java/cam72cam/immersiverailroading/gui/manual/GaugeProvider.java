package cam72cam.immersiverailroading.gui.manual;

import cam72cam.immersiverailroading.gui.markdown.MarkdownDocument;
import cam72cam.immersiverailroading.gui.markdown.element.MarkdownStyledText;
import cam72cam.immersiverailroading.library.Gauge;

import java.util.List;
import java.util.stream.Collectors;

public class GaugeProvider {
    public static final String SYNTAX = "[gauge_provider]";

    public static List<MarkdownDocument.MarkdownLine> parse(String input, MarkdownDocument context) {
        return Gauge.values().stream()
                .map(gauge -> MarkdownDocument.MarkdownLine.create(new MarkdownStyledText(gauge.toString() + " (" + gauge.value() + "m)"))
                        .isUnorderedList(true))
                .collect(Collectors.toList());
    }
}
