package de.codesourcery.booleanalgebra;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExpressionContextTest {

    @Test (expected = IllegalArgumentException.class)
    public void lookupNullTest() throws Exception {
        ExpressionContext testInstance = new ExpressionContext();
        testInstance.lookup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNullTest() {
        ExpressionContext testInstance = new ExpressionContext();
        testInstance.remove(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void setNullTest() {
        ExpressionContext testInstance = new ExpressionContext();
        testInstance.set(null, null);
    }

}