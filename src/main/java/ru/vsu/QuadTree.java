package ru.vsu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;


public class QuadTree<T extends Point> {

    @Getter
    @Setter
    public static class QuadTreeNode<T>{
        private final Square square;
        private QuadTreeNode<T> parent;
        private final Map<Direction, QuadTreeNode<T>> nodes;
        private List<T> values;

        QuadTreeNode(Square square, int capacity, QuadTreeNode<T> parent){
            this.square = square;
            this.nodes = new HashMap<>();
            this.values = new ArrayList<>(capacity);
            this.parent = parent;
        }

        public int countDepth(){
            if(parent == null) return 0;
            return parent.countDepth() + 1;
        }

    }

    private QuadTreeNode<T> root;
    private long size = 0;
    private final int capacity;

    public QuadTree (Square square, int capacity){
        this.capacity = capacity;
        root = new QuadTreeNode<>(square, capacity, null);
        split(root);
    }

    public QuadTree(Square square){
        this(square, 4);
    }



    public boolean add(T value) {
        int x = value.getX();
        int y = value.getY();
        if (!isInside(x, y, root.getSquare())) {
            return false;
        }
        return insert(root, value);
    }

    public void removeInArea(Square square){
        getAllInRange(square).forEach(this::remove);
    }

    public boolean contains(T value){
        if(isInside(value.getX(), value.getY(), root.getSquare())){
           QuadTreeNode<T> node = findNodeFor(new Point(value.getX(), value.getY()), root);
           if(node == null) return false;
           return node.getValues().stream().anyMatch(val -> val.getX() == value.getX() && val.getY() == value.getY());
        }
        return false;
    }

    public boolean remove(T value){
        Point p = new Point(value.getX(), value.getY());
        if(!isInside(p.getX(), p.getY(), root.getSquare()))return false;
        QuadTreeNode<T> need = findNodeFor(p, root);
        if(need == null) return false;
        T forDelete = need.values.stream().filter(val -> val.getY() == p.getY() && val.getX() == p.getX()).findAny().orElse(null);
        if(forDelete == null)return false;
        need.getValues().remove(forDelete);
        tryToGather(need);
        return true;
    }


    private void tryToGather(QuadTreeNode<T> node){
        if(!node.getNodes().isEmpty() || node.parent == null)return;
        QuadTreeNode<T> parent = node.parent;
        long c = parent.nodes.values().stream()
                .filter(nod -> nod.values.isEmpty())
                .count();
        if(c >= parent.getNodes().size()){
            parent.getNodes().clear();
        }
    }


    private boolean insert(QuadTreeNode<T> node, T value) {
        if(node.nodes.isEmpty()){
            if(node.getValues().size() >= capacity){
                split(node);
                return insert(node, value);
            }
            T old = node.getValues().stream().filter(n -> n.getY() == value.getY() && n.getX() == value.getX()).findAny().orElse(null);
            if(old == null) {
                node.getValues().add(value);
                return true;
            }
            return false;
        }else{
            QuadTreeNode<T> need = node.nodes.values().stream()
                    .filter(nod -> isInside(value.getX(), value.getY(), nod.getSquare()))
                    .findAny().orElse(null);
            if(need == null) return false;
            return insert(need, value);
        }
    }

    private QuadTreeNode<T> findNodeFor(Point p, QuadTreeNode<T> curr){
        if(curr.getNodes().isEmpty()){
            return curr;
        }else{
            QuadTreeNode<T> need = curr.nodes.values().stream()
                    .filter(node -> isInside(p.getX(), p.getY(), node.getSquare())).findAny().orElse(null);
            if(need != null){
                return findNodeFor(p, need);
            }
        }
        return null;
    }

    public List<T> getAllInRange(Square square){
        return getAllInRange(root, square);
    }

    private List<T> getAllInRange(QuadTreeNode<T> node, Square square){
        List<T> answer = new ArrayList<>();
        findAll(square, node, answer::add);
        return answer;
    }

    private void findAll(Square square, QuadTreeNode<T> node, Consumer<T> valueConsumer){
        if(!isIntersects(node.square, square)) return;
        node.getValues().stream()
                .filter(value -> isInside(value.getX(), value.getY(), square))
                .forEach(valueConsumer);
        node.nodes.forEach((direction, tQuadTreeNode) -> {
           findAll(square, tQuadTreeNode, valueConsumer);
        });
    }





    private void split(QuadTreeNode<T> node){
        List<T> values = node.getValues();
        node.setValues(new ArrayList<>());
        Map<Direction, Square> sq = separate(node.getSquare());
        sq.forEach((direction, square1) -> root.getNodes().put(direction, new QuadTreeNode<>(square1, capacity, node)));
        values.forEach(this::add);
    }

    private boolean isInside(int x, int y, Square square){
        int minX = square.getPoint().getX();
        int maxY = square.getPoint().getY();
        int maxX = square.getPoint().getX() + square.getWidth();
        int minY = square.getPoint().getY() - square.getHeight();
        return x > minX && x < maxX && y > minY && y < maxY;
    }

    private boolean isIntersects(Square one, Square two){
        int minX1 = one.getPoint().getX();
        int minY1 = one.getPoint().getY();
        int maxX1 = one.getPoint().getX() + one.getWidth();
        int maxY1 = one.getPoint().getY() + one.getHeight();
        int minX2 = two.getPoint().getX();
        int minY2 = two.getPoint().getY();
        int maxX2 = two.getPoint().getX() + two.getWidth();
        int maxY2 = two.getPoint().getY() + two.getHeight();
        return minX2 < maxX1 && minY2 < maxY1 && maxX2 > minX1 && maxY2 > minY1;
    }


    private Map<Direction, Square> separate(Square square){
        Point p = square.getPoint();
        int initialX = p.getX();
        int initialY = p.getY();
        int halfOfWidth = square.getWidth()/2;
        int halfOfHeight = square.getHeight()/2;
        Square leftUp = new Square(p, halfOfWidth, halfOfHeight);
        Square rightUp = new Square(new Point(initialX + halfOfWidth, initialY), halfOfWidth, halfOfHeight);
        Square leftDown = new Square(new Point(initialX, initialY - halfOfHeight), halfOfWidth, halfOfHeight);
        Square rightDown = new Square(new Point(initialX + halfOfWidth, initialY - halfOfHeight), halfOfWidth, halfOfHeight);
        return Map.of(
                Direction.LeftUp, leftUp,
                Direction.LeftDown, leftDown,
                Direction.RightUp, rightUp,
                Direction.RightDown, rightDown);
    }




    private QuadTreeNode<T> getRoot() {
        return root;
    }

    private void setRoot(QuadTreeNode<T> root) {
        this.root = root;
    }

    private long getSize() {
        return size;
    }

    private void setSize(long size) {
        this.size = size;
    }
}
