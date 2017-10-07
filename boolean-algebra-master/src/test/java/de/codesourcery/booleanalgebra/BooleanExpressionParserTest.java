package de.codesourcery.booleanalgebra;

import java.io.PrintWriter;
import java.util.Iterator;

import junit.framework.TestCase;
import de.codesourcery.booleanalgebra.ast.ASTNode;
import de.codesourcery.booleanalgebra.ast.IdentifierNode;
import de.codesourcery.booleanalgebra.ast.OperatorNode;
import de.codesourcery.booleanalgebra.ast.OperatorType;
import de.codesourcery.booleanalgebra.ast.TermNode;
import de.codesourcery.booleanalgebra.exceptions.ParseException;

public class BooleanExpressionParserTest extends TestCase
{
    private BooleanExpressionParser parser;
    
    @Override
    protected void setUp() throws Exception
    {
        parser = new BooleanExpressionParser();
    }
    
    public void testParseEmptyExpression() {
        
        final String expr = "()";
        try {
            parser.parse( expr , true );
            fail("Should've failed");
        } catch(ParseException e) {
            // ok
        }
    }
    
    public void testIncompleteBlankExpression1() {
        
        final String expr = "not";
        try {
            parser.parse( expr , true );
            fail("Should've failed");
        } catch(ParseException e) {
            // ok
        }
    }   
    
    public void testIncompleteBlankExpression2() {
        
        final String expr = "a and";
        try {
            parser.parse( expr , true );
            fail("Should've failed");
        } catch(ParseException e) {
            // ok
        }
    }  
    
    public void testIncompleteBlankExpression3() {
        
        final String expr = "and a";
        try {
            parser.parse( expr , true );
            fail("Should've failed");
        } catch(ParseException e) {
            // ok
        }
    }  
    
    private void print(ASTNode node) {
        node.print( new PrintWriter(System.out,true) );
    }
    
    public void testParseExpressionWithParens() {
        
        final String expr = "(a and b)";
        ASTNode term = parser.parse( expr , true );
        
        Iterator<ASTNode> it = term.createPreOrderIterator();
        
        ASTNode node = it.next();
        assertTrue( node instanceof TermNode );
        
        node = it.next();
        assertTrue( "was: "+node.getClass() , node instanceof OperatorNode );
        assertEquals( OperatorType.AND , ((OperatorNode) node).getType() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("a" , ((IdentifierNode) node).getIdentifier().getValue() );
        
        node = it.next();
        assertTrue( node instanceof IdentifierNode );
        assertEquals("b" , ((IdentifierNode) node).getIdentifier().getValue() );
    }    
    

}
