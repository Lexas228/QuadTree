package ru.vsu;

import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuadTreeTest {
    @Getter
    public static class TestValues extends Point{
        private final int myVal;
        public TestValues(int x, int y, int myVal) {
            super(x, y);
            this.myVal = myVal;
        }
    }

    private QuadTree<TestValues> myTree;

    @BeforeEach
    public void setUp(){
        myTree = new QuadTree<>(new Square(new Point(0, 500),500, 500), 4);
    }

    @Test
    public void shouldAddAndFindSomeValues(){
        boolean r= myTree.add(new TestValues(1, 499, 10));
        boolean k =myTree.add(new TestValues(1, 498, 20));
        assertTrue(r);
        assertTrue(k);
        List<TestValues> val = myTree.getAllInRange(new Square(new Point(0, 500), 20, 20));
        assertFalse(val.isEmpty());
        TestValues t = val.get(0);
        assertTrue(t.myVal == 10 || t.myVal == 20);
    }

    @Test
    public void shouldRemoveSomeValues(){
        boolean r= myTree.add(new TestValues(1, 499, 10));
        boolean k =myTree.add(new TestValues(1, 498, 20));
        assertTrue(r);
        assertTrue(k);
        myTree.removeInArea(new Square(new Point(0, 500), 20, 20));
        List<TestValues> val = myTree.getAllInRange(new Square(new Point(0, 500), 500, 500));
        assertTrue(val.isEmpty());
    }

    @Test
    public void shouldRemoveOneValue(){
        TestValues testValues = new TestValues(1, 499, 10);
        boolean r= myTree.add(testValues);
        assertTrue(r);
        myTree.remove(testValues);
        assertTrue(myTree.getAllInRange(new Square(new Point(0, 500), 500, 500)).isEmpty());
    }


    @Test
    public void shouldNotRemoveOneValue(){
        TestValues testValues = new TestValues(1, 499, 10);
        boolean r= myTree.add(testValues);
        assertTrue(r);
        myTree.remove(new TestValues(2, 498, 30));
        assertFalse(myTree.getAllInRange(new Square(new Point(0, 500), 500, 500)).isEmpty());
    }

    @Test
    public void shouldReturnTrueOnContains(){
        TestValues testValues = new TestValues(1, 499, 10);
        boolean r= myTree.add(testValues);
        assertTrue(r);
        assertTrue(myTree.contains(testValues));
    }

    @Test
    public void shouldReturnFalseOnContains(){
        TestValues testValues = new TestValues(1, 499, 10);
        boolean r= myTree.add(testValues);
        assertTrue(r);
        assertFalse(myTree.contains(new TestValues(1, 3, 10)));
    }





}
