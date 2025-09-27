package cam72cam.immersiverailroading.util;

import cam72cam.immersiverailroading.entity.*;
import cam72cam.immersiverailroading.registry.DefinitionManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class StockFilterCompiler {
    private static final Map<String, BiFunction<EntityRollingStock, String, Boolean>> prefixes = new HashMap<>();
    private static final String AND = "&&";
    private static final String OR = "||";
    private static final String START_PAREN = "(";
    private static final String END_PAREN = ")";

    static {
        prefixes.put("type", (stock, content) -> {
            switch (content) {
                case "locomotive":
                    return stock instanceof LocomotiveDiesel
                           || stock instanceof LocomotiveSteam
                           || stock instanceof HandCar;
                case "diesel":
                    return stock instanceof LocomotiveDiesel;
                case "steam":
                    return stock instanceof LocomotiveSteam;
                case "handcar":
                    return stock instanceof HandCar;
                case "passenger":
                    return stock instanceof CarPassenger;
                case "tender":
                    return stock instanceof Tender;
                case "tank":
                    return stock instanceof CarTank;
                case "freight":
                    return stock instanceof CarFreight;
                default:
                    return false;
            }
        });
        prefixes.put("tag", (stock, content) -> DefinitionManager.isTaggedWith(stock.getDefinition(), content));
        prefixes.put("stock", (stock, content) -> {
            String definitionFileName = stock.getDefinitionID().split("/")[2];
            return definitionFileName.substring(0, definitionFileName.length() - 5).equals(content);
        });
        prefixes.put("works", (stock, content) -> stock instanceof Locomotive && ((Locomotive) stock).getDefinition().works.equals(content));
        prefixes.put("author", (stock, content) -> stock.getDefinition().modelerName.equals(content));
        prefixes.put("pack", (stock, content) -> stock.getDefinition().packName.equals(content));
        prefixes.put("nametag", (stock, content) -> stock.tag.equals(content));
    }

    public static Predicate<EntityRollingStock> compile(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return s -> true;
        }

        expression = expression.trim();

        Predicate<EntityRollingStock> parse = s -> true;
        List<String> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        int parenDepth = 0;

        Runnable flushBuffer = () -> {
            if (buffer.length() > 0) {
                String token = buffer.toString().trim();
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
                buffer.setLength(0);
            }
        };

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            switch (c) {
                case '(':
                    if (parenDepth == 0) {
                        flushBuffer.run();
                    }
                    parenDepth++;
                    if (parenDepth == 1) {
                        tokens.add(START_PAREN);
                    } else {
                        buffer.append(c);
                    }
                    break;
                case ')':
                    parenDepth--;
                    if (parenDepth < 0) {
                        throw new IllegalArgumentException("Unmatched closing parenthesis");
                    }
                    if (parenDepth == 0) {
                        flushBuffer.run();
                        tokens.add(END_PAREN);
                    } else {
                        buffer.append(c);
                    }
                    break;
                case '&':
                    if (parenDepth == 0 && i + 1 < expression.length() && expression.charAt(i + 1) == '&') {
                        flushBuffer.run();
                        tokens.add(AND);
                        i++;
                    } else {
                        buffer.append(c);
                    }
                    break;
                case '|':
                    if (parenDepth == 0 && i + 1 < expression.length() && expression.charAt(i + 1) == '|') {
                        flushBuffer.run();
                        tokens.add(OR);
                        i++;
                    } else {
                        buffer.append(c);
                    }
                    break;

                default:
                    buffer.append(c);
            }
        }
        if (parenDepth != 0) {
            throw new IllegalArgumentException("Unmatched opening parenthesis");
        }

        flushBuffer.run();

        return buildAST(tokens)::test;
    }
    private static Node buildAST(List<String> tokens) {
        return parseExpression(tokens, new int[]{0});
    }

    private static Node parseExpression(List<String> tokens, int[] index) {
        return parseLogicalOr(tokens, index);
    }

    private static Node parseLogicalOr(List<String> tokens, int[] index) {
        Node left = parseLogicalAnd(tokens, index);

        while (index[0] < tokens.size() && OR.equals(tokens.get(index[0]))) {
            index[0]++; // 跳过 OR
            Node right = parseLogicalAnd(tokens, index);
            left = new Node(left, right, (a, b) -> a || b);
        }

        return left;
    }

    private static Node parseLogicalAnd(List<String> tokens, int[] index) {
        Node left = parsePrimary(tokens, index);

        while (index[0] < tokens.size() && AND.equals(tokens.get(index[0]))) {
            index[0]++; // 跳过 AND
            Node right = parsePrimary(tokens, index);
            left = new Node(left, right, (a, b) -> a && b);
        }

        return left;
    }

    private static Node parsePrimary(List<String> tokens, int[] index) {
        if (index[0] >= tokens.size()) {
            throw new IllegalArgumentException("Unexpected end of expression");
        }

        String token = tokens.get(index[0]);

        if (START_PAREN.equals(token)) {
            index[0]++; // 跳过 '('
            Node expr = parseExpression(tokens, index);

            if (index[0] >= tokens.size() || !END_PAREN.equals(tokens.get(index[0]))) {
                throw new IllegalArgumentException("Expected closing parenthesis");
            }
            index[0]++; // 跳过 ')'

            return expr;
        } else if (END_PAREN.equals(token)) {
            throw new IllegalArgumentException("Unexpected closing parenthesis");
        } else {
            // 处理条件token
            index[0]++;
            return createConditionNode(token);
        }
    }

    private static Node createConditionNode(String conditionToken) {
        String[] parts = conditionToken.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid condition format: " + conditionToken);
        }

        String prefix = parts[0].trim();
        String content = parts[1].trim();

        BiFunction<EntityRollingStock, String, Boolean> conditionFunc = prefixes.get(prefix);
        if (conditionFunc == null) {
            throw new IllegalArgumentException("Unknown condition prefix: " + prefix);
        }

        return new Node(stock -> conditionFunc.apply(stock, content));
    }

    private static class Node {
        boolean isLeaf;
        //If is leaf...
        Predicate<EntityRollingStock> predicate;
        //It is not leaf...
        BiFunction<Boolean, Boolean, Boolean> function;

        Node parent;
        Node leftChild;
        Node rightChild;

        public Node(@Nonnull Node leftChild, @Nonnull Node rightChild, @Nonnull BiFunction<Boolean, Boolean, Boolean> function) {
            this.isLeaf = false;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.function = function;
        }

        public Node(@Nonnull Predicate<EntityRollingStock> predicate) {
            this.isLeaf = true;
            this.predicate = predicate;
        }

        public Node() {
        }

        public boolean test(EntityRollingStock stock) {
            return isLeaf ? predicate.test(stock) : function.apply(leftChild.test(stock), rightChild.test(stock));
        }
    }
}
