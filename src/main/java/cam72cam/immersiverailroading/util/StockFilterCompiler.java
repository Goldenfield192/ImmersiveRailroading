package cam72cam.immersiverailroading.util;

import cam72cam.immersiverailroading.entity.*;
import cam72cam.immersiverailroading.registry.DefinitionManager;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
        //A filter supports (/&&/||/) to add logic calculation
        if (expression == null || expression.trim().isEmpty()) {
            return s -> true;
        }

        expression = expression.trim();

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

        //Parse tokens
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            switch (c) {
                case '(':
                    flushBuffer.run();
                    parenDepth++;
                    tokens.add(START_PAREN);
                    break;
                case ')':
                    flushBuffer.run();
                    parenDepth--;
                    if (parenDepth < 0) {
                        throw new IllegalArgumentException("Unmatched closing parenthesis");
                    }
                    tokens.add(END_PAREN);
                    break;
                case '&':
                    if (i + 1 < expression.length() && expression.charAt(i + 1) == '&') {
                        flushBuffer.run();
                        tokens.add(AND);
                        i++;
                    } else {
                        buffer.append(c);
                    }
                    break;
                case '|':
                    if (i + 1 < expression.length() && expression.charAt(i + 1) == '|') {
                        flushBuffer.run();
                        tokens.add(OR);
                        i++;
                    } else {
                        buffer.append(c);
                    }
                    break;
                default:
                    //Default token
                    buffer.append(c);
            }
        }

        if (parenDepth != 0) {
            throw new IllegalArgumentException("Unmatched opening parenthesis");
        }

        flushBuffer.run();
        Node node = buildAST(tokens);
        return node.predicate.get();
    }

    private static Node buildAST(List<String> tokens) {
        Node root = new Node();
        Node current = root;
        Stack<Node> paren = new Stack<>();

        BiFunction<Boolean, Boolean, Boolean> and = (b1, b2) -> b1 && b2;
        BiFunction<Boolean, Boolean, Boolean> or = (b1, b2) -> b1 || b2;

        //I used a complex way to build it but it works...
        for (String token : tokens) {
            switch (token) {
                case START_PAREN:
                    Node child = new Node();
                    if (current.leftChild == null) {
                        current.leftChild = child;
                    } else {
                        current.rightChild = child;
                    }
                    child.parent = current;
                    current = child;
                    paren.push(current);
                    break;
                case END_PAREN:
                    current = paren.pop();
                    break;
                case AND:
                    if (current.parent != null) {
                        if (current.parent.function == null) {
                            current = current.parent;
                            current.function = and;
                        } else {
                            if (current.parent.function == and) {
                                Node insert = new Node();
                                if (current.parent.leftChild == current) {
                                    current.parent.leftChild = insert;
                                } else {
                                    current.parent.rightChild = insert;
                                }
                                current.parent = insert;
                                current = insert;
                                current.function = and;
                            } else {
                                Node parent = current.parent;
                                if (parent == root) {
                                    Node newRoot = new Node();
                                    root.parent = newRoot;
                                    root = newRoot;
                                    current = root;
                                    current.function = and;
                                } else {
                                    Node grand = parent.parent;
                                    Node insert = new Node();
                                    if (grand.leftChild == parent) {
                                        grand.leftChild = insert;
                                    } else {
                                        grand.rightChild = insert;
                                    }
                                    parent.parent = insert;
                                    current = insert;
                                    current.function = and;
                                }
                            }
                        }
                        Node inter = current;
                        current.predicate = () -> inter.leftChild.predicate.get().and(inter.rightChild.predicate.get());
                    } else {
                        Node newParent = new Node();
                        newParent.leftChild = current;
                        current.parent = newParent;
                        current = newParent;
                        current.function = and;
                        root = newParent;
                    }
                    break;
                case OR:
                    if (current.parent != null) {
                        if (current.parent.function == null) {
                            current = current.parent;
                            current.function = or;
                        } else {
                            Node insert = new Node();
                            if (current.parent.leftChild == current) {
                                current.parent.leftChild = insert;
                            } else {
                                current.parent.rightChild = insert;
                            }
                            current.parent = insert;
                            current = insert;
                            current.function = or;
                        }
                        Node inter = current;
                        current.predicate = () -> inter.leftChild.predicate.get().or(inter.rightChild.predicate.get());
                    } else {
                        Node newParent = new Node();
                        newParent.leftChild = current;
                        current.parent = newParent;
                        current = newParent;
                        current.function = or;
                        root = newParent;
                    }
                    break;
                default:
                    Node newNode = new Node();
                    newNode.predicate = () -> getPredicate(token);
                    newNode.parent = current;
                    if (current.leftChild == null) {
                        current.leftChild = newNode;
                    } else {
                        current.rightChild = newNode;
                    }
                    current = newNode;
            }
        }

        while (root.leftChild == null || root.rightChild == null) {
            if (root.leftChild == null && root.rightChild == null) {
                break;
            }
            root = root.leftChild == null ? root.rightChild : root.leftChild;
        }

        return root;
    }

    private static Predicate<EntityRollingStock> getPredicate(String token) {
        String[] strings = token.split(":");
        if (strings.length == 2) {
            return stock -> prefixes.get(strings[0]).apply(stock, strings[1]);
        } else {
            throw new IllegalArgumentException("Invalid token format: " + token);
        }
    }

    private static class Node {
        //If is leaf...
        Supplier<Predicate<EntityRollingStock>> predicate;
        //It is not leaf...
        BiFunction<Boolean, Boolean, Boolean> function;

        Node parent;
        Node leftChild;
        Node rightChild;
    }
}
