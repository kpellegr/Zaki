package be.bluebanana.zaki;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private static final Operator[] OPERATORS = {Operator.ADD, Operator.SUB, Operator.MUL, Operator.DIV};
    private final Operator operator;
    private final float value;
    private Node leftTree;
    private Node rightTree;

    private Node parent;

    // create a leaf node
    public Node(int value_) {
        leftTree = null;
        rightTree = null;
        parent = null;
        operator = Operator.ADD;
        value = value_;
    }

    // create a node
    public Node(Node left, Node right, Operator operator) {
        leftTree = left;
        rightTree = right;
        if (left != null) left.setParent(this);
        if (right != null) right.setParent(this);

        this.operator = operator;
        this.value = 0;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }


    public static List<Node> getAllTrees(List<Integer> numbers) {
        ArrayList<Node> list = new ArrayList<>();
        int size = numbers.size();

        if (size == 1) {
            list.add(new Node(numbers.get(0))); // only one element left, create a leaf node
        } else {
            int i;
            List<Node> leftList, rightList;
            for (i = 1; i < size; i++) {
                //Log.d("Node", String.format("Generating (%d, %d)", i, size - i));
                leftList = getAllTrees(numbers.subList(0, i));
                rightList = getAllTrees(numbers.subList(i, size));

                //Log.d("Node", String.format("Concatenating %d + %d lists", leftList.size(), rightList.size()));
                for (int j = 0; j < leftList.size(); j++) {
                    for (int k = 0; k < rightList.size(); k++) {
                        for (Operator op : Node.OPERATORS) {
                            list.add(new Node(leftList.get(j), rightList.get(k), op));
                        }
                    }
                }
            }
        }
        return list;
    }

    public Node getLeftTree() {
        return leftTree;
    }

    private void setLeftTree(Node leftTree) {
        this.leftTree = leftTree;
    }

    public Node getRightTree() {
        return rightTree;
    }

    private void setRightTree(Node rightTree) {
        this.rightTree = rightTree;
    }

    public Operator getOperator() {
        return operator;
    }

    public char getOperatorAsChar() {
        switch (getOperator()) {
            case ADD:
                return '+';
            case SUB:
                return '-';
            case MUL:
                return '*';
            case DIV:
                return '/';
            default:
                return 0x00;
        }
    }

    public float getValue() {
        return value;
    }

    public boolean isLeaf() {
        return (this.getLeftTree() == null) || (this.getRightTree() == null);
    }

    public boolean isRoot() {
        return (parent == null);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.isLeaf()) {
            sb.append(String.format("%.0f", getValue()));
        } else {

            boolean needsNoParenthesis =
                    (getOperator() == Operator.MUL)
                    && (getOperator() != Operator.DIV)
                    || isRoot()
                    || (getParent().getOperator() == Operator.ADD)
                    || (getParent().getOperator() == Operator.SUB);

            if  (!needsNoParenthesis) sb.append("(");
            sb.append(this.getLeftTree().toString());
            sb.append(this.getOperatorAsChar());
            sb.append(this.getRightTree().toString());
            if  (!needsNoParenthesis) sb.append(")");
        }

        return sb.toString();
    }

    public float evaluate() {
        float result = 0.0f;

        if (!isLeaf()) {
            float a = getLeftTree().evaluate();
            float b = getRightTree().evaluate();
            switch (getOperator()) {
                case ADD:
                    return a + b;
                case SUB:
                    return a - b;
                case MUL:
                    return a * b;
                case DIV:
                    return a / b;
                default:
                    return 0;
            }
        }
        return getValue();
    }

    private enum Operator {ADD, SUB, MUL, DIV}


}
