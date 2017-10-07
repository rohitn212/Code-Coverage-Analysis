package de.codesourcery.booleanalgebra.ast;

import java.util.Stack;

import de.codesourcery.booleanalgebra.IExpressionContext;
import de.codesourcery.booleanalgebra.exceptions.ParseException;
import de.codesourcery.booleanalgebra.lexer.ILexer;
import de.codesourcery.booleanalgebra.lexer.Token;
import de.codesourcery.booleanalgebra.lexer.TokenType;

public class TermNode extends ASTNode
{
    private boolean artificial = false;
    
    @Override
    protected int getMaxSupportedChildCount()
    {
        return 1;
    }
    
    public TermNode() {
    	super();
    }
    
    /**
     * INTERNAL USE ONLY.
     * 
     * @param artificial  <code>true</code> this term node was generated by the parser to group a parsed sub-expression
     * , <code>false</code> if the user passed an expression that contained parens
     */
    private TermNode(boolean artificial) {
        this.artificial=artificial;
    }    
    
    private void markAsArtifical() {
        this.artificial = true;
    }
    
    private boolean isArtificial() {
        return artificial;
    }
    
    public TermNode(ASTNode child) {
    	super( child );
    }
    
    protected class Operator 
    {
        private final OperatorType type;

        public Operator(OperatorType type)
        {
            if (type == null) {
                throw new IllegalArgumentException("type must not be NULL.");
            }
            this.type = type;
        }
        
        public OperatorType getType()
        {
            return type;
        }
        
        public boolean isNOT() {
            return type == OperatorType.NOT;
        }
        
        public boolean isAND() {
            return type == OperatorType.AND;
        }
        
        public boolean isOR() {
            return type == OperatorType.OR;
        }        
    }
    
    protected enum ValueType {
        TRUE,
        FALSE,
        IDENTIFIER,
        TERM;
    }
    
    public class Value {
        
        private final ValueType type;
        private final Object value;

        public Value(ValueType type,Object value)
        {
            if ( type == null ) {
                throw new IllegalArgumentException("type must not be NULL.");
            }
            if ( value == null ) {
                throw new IllegalArgumentException("value must not be NULL.");
            }
            this.type = type;
            this.value = value;
        }
        
        public ValueType getType()
        {
            return type;
        }
        
        public Object getValue()
        {
            return value;
        }
    }
    
    @Override    
    public ASTNode parse(ILexer lexer) throws ParseException
    {
        ASTNode result = internalParse(lexer);
        if ( result.hasChildren() ) {
            return result.child(0);
        }
        return result;
    }
    
    protected ASTNode internalParse(ILexer lexer) throws ParseException
    {
        /*
         * CHARACTERS,
         */

        /*
         *  -- terminals
         *  
         * TRUE := 'true'
         * FALSE := 'false'
         * IDENTIFIER := '_\\-0-9a-zA-Z'
         * EQUALS := '='
         * NOT := 'not'
         * OR := 'or'
         * AND := 'and'
         * 
         * --- 
         * NOT_TERM = NOT TERM
         * OR_TERM = EXPR OR TERM
         * AND_TERM = EXPR AND TERM
         * 
         *  TERM = IDENTIFIER | TRUE | FALSE | NOT_TERM | OR_TERM | AND_TERM | '(' TERM ')'
         *  
         *  EXPRESSION = TERM EQUALS TERM 
         */
        
        final int startOffset = lexer.currentParseOffset();
        ASTNode lastAddedNode = null;
        final Stack<OperatorNode> operatorStack = new Stack<OperatorNode>();
        final Stack<ASTNode> valueStack = new Stack<ASTNode>();   
        
        // System.out.println(" ---------- start: parse term ---------- ");
        do {
            final Token tok = lexer.peek();
            
            if ( tok.hasType( TokenType.IDENTIFIER ) ) 
            {
                lastAddedNode = pushToStack( operatorStack , valueStack , new IdentifierNode().parse(lexer ) , lastAddedNode  );
            } 
            else if ( tok.hasType( TokenType.TRUE ) ) 
            {
                lastAddedNode = pushToStack( operatorStack , valueStack , new TrueNode().parse(lexer ) , lastAddedNode  );
            } 
            else if ( tok.hasType( TokenType.FALSE ) ) 
            {
                lastAddedNode = pushToStack( operatorStack , valueStack , new FalseNode().parse(lexer ) , lastAddedNode );
            } 
            else if ( tok.hasType( TokenType.AND ) || tok.hasType( TokenType.NOT ) || tok.hasType( TokenType.OR)) 
            {
                lastAddedNode = pushToStack( operatorStack ,valueStack ,  new OperatorNode().parse(lexer ) , lastAddedNode );
            } 
            else if ( tok.hasType( TokenType.PARENS_OPEN ) ) 
            {
                lexer.read( TokenType.PARENS_OPEN );
                ASTNode parsed = new TermNode().internalParse(lexer);
                lastAddedNode = pushToStack( operatorStack ,valueStack , parsed , lastAddedNode  );
                lexer.read( TokenType.PARENS_CLOSE);                
            }
            else {
                break;
            }
            
        } while ( ! lexer.eof() );

        clearStacks( operatorStack , valueStack , lastAddedNode );

        if ( ! operatorStack.isEmpty() ) {
            throw new ParseException("Term stack not empty: "+operatorStack, startOffset );
        }
        // System.out.println(" ---------- finish: parse term : "+this+" ---------- ");
        
        assertNoEmptyTerms( startOffset );
        return this;
    }
    
    private void assertNoEmptyTerms(final int offset) {
        final INodeVisitor visitor = new INodeVisitor() {
            
            @Override
            public boolean visit(ASTNode node, int currentDepth)
            {
                if ( node instanceof TermNode && ! node.hasChildren() ) {
                    throw new ParseException("Term must not be empty",offset);                    
                }
                return true;
            }
        };
        visitInOrder( visitor );
    }

    private void clearStacks(Stack<OperatorNode> operatorStack, Stack<ASTNode> valueStack, ASTNode lastAddedNode)
    {
        if ( operatorStack.isEmpty() && valueStack.size() == 1 ) 
        {
            final ASTNode value = valueStack.pop();
            // System.out.println("VALUE: POP "+value);
            if ( lastAddedNode != null ) {
                lastAddedNode.addChild( value );
            } else {
                addChild( value );
            }
            return;
        }
        
        ASTNode previousNode = lastAddedNode;
        while( ! operatorStack.isEmpty() ) 
        {
            previousNode = popOperatorFromStack(operatorStack, valueStack, previousNode,true);
        }
        
        if ( ! valueStack.isEmpty() ) {
            throw new RuntimeException("Internal error, value stack not empty: "+valueStack);
        }
        
        // System.out.println(" ---------- finish: clearing stacks ----------");          
    }

    private ASTNode pushToStack(Stack<OperatorNode> operatorStack,Stack<ASTNode> valueStack,ASTNode newNode,ASTNode lastAddedNode) 
    {
        if ( ! ( newNode instanceof OperatorNode ) ) {
            valueStack.push( newNode );
            // System.out.println("VALUE: PUSH "+newNode);
            return lastAddedNode;
        }
        
        final OperatorNode x = (OperatorNode) newNode;
        
        /*
    For all the input tokens [S1]:

        - Read the next token [S2];
        - If token is an operator (x) [S3]:
            - While there is an operator (y) at the top of the operators stack and either (x) is
              left-associative and its precedence is less or equal to that of (y), or (x) is right-associative
              and its precedence is less than (y) [S4]:

         * Pop (y) from the stack [S5];
         * Add (y) output buffer [S6];
            - Push (x) on the stack [S7];

          Else If token is left parenthesis, then push it on the stack [S8];

          Else If token is a right parenthesis [S9]:
            Until the top token (from the stack) is left parenthesis, pop from the stack to the output buffer [S10];
            Also pop the left parenthesis but don’t include it in the output buffer [S11];

          Else add token to output buffer [S12].

        - While there are still operator tokens in the stack, pop them to output [S13]
         */
        // System.out.println("CURRENT OPERATOR: "+x);  
        if ( ! operatorStack.isEmpty()  ) {

            final OperatorNode y = operatorStack.peek();
            if ( ( isLeftAssociative(x) && getPrecedence( x ) <= getPrecedence( y ) ) ||
                   ( isRightAssociative(x) && getPrecedence( x ) < getPrecedence( y ) ) )
            {
            	if ( isLeftAssociative( x ) ) {
            		// System.out.println("Left-associative operator "+x+" has <= precedence than "+y);            		
            	} else {
            		// System.out.println("Right-associative operator "+x+" has < precedence than "+y);
            	}
                ASTNode result = popOperatorFromStack(operatorStack, valueStack,lastAddedNode,false);
                // System.out.println("OPERATOR: PUSH "+x);  
                operatorStack.push( x );
                return result;
            } 
            // System.out.println("OPERATOR: PUSH "+x);            
            operatorStack.push( x );                  
        } else {
            // System.out.println("OPERATOR: PUSH "+x);  
            operatorStack.push( x );            
        }
        return lastAddedNode;
    }

    private ASTNode popOperatorFromStack(Stack<OperatorNode> operatorStack, 
            Stack<ASTNode> valueStack, 
            ASTNode lastAddedNode,
            boolean clearStack)
    {
        final OperatorNode op = operatorStack.pop();
        // System.out.println("OPERATOR: POP "+op);
        
        ASTNode newNode;
        if ( isRightAssociative( op ) ) // NOT <something>
        {
            if ( valueStack.isEmpty() ) {
                throw new ParseException("Missing operand",-1);
            }
            final ASTNode rightValue = valueStack.pop();
            // System.out.println("VALUE: POP "+rightValue);
            if ( op.isNOT() ) {
                newNode = OperatorNode.not( rightValue);
            } else {
                throw new RuntimeException("Internal error"); 
            }
        } 
        else if ( isLeftAssociative( op ) ) // <something> AND <something>, <something> OR <something>
        {
            if ( valueStack.isEmpty() ) {
                throw new ParseException("Missing operand",-1);
            }            
            final ASTNode rightValue = valueStack.pop();
            // System.out.println("VALUE: POP "+rightValue);
            if ( valueStack.isEmpty() ) {
                throw new ParseException("Missing operand",-1);
            }            
            final ASTNode leftValue = valueStack.pop();
            // System.out.println("VALUE: POP "+leftValue);
            if ( op.isAND() ) {
                newNode = OperatorNode.and( leftValue, rightValue);
            } else if ( op.isOR() ) {
                newNode = OperatorNode.or( leftValue, rightValue);
            } else {
                throw new RuntimeException("Internal error");                        
            }
        } 
        else {
            throw new RuntimeException("Internal error , unhandled operator "+op);
        }
        
        if ( ! operatorStack.isEmpty() || ! clearStack  ) {
            // System.out.println("COMBINED VALUE: PUSH "+newNode);
            valueStack.push( newNode );
            return lastAddedNode;
        } 
        
        if ( lastAddedNode != null ) {
            lastAddedNode.addChild( newNode );
        } else {
            addChild( newNode );
        }
        return newNode;
    }

    private boolean isLeftAssociative(OperatorNode node) {
        return node.isAND() || node.isOR();
    }

    private boolean isRightAssociative(OperatorNode node) {
        return node.isNOT();
    }

    /**
     * 
     * @param node
     * @return precedence value, the higher the returned value, the higher the node's precedence
     */
    protected int getPrecedence(OperatorNode node) {
        return node.getType().getPrecedence();
    }
    
    @Override
    public String toString(boolean prettyPrint)
    {
        if ( prettyPrint) {
            if ( ! hasParent() || getParent() instanceof BooleanExpression) { 
                return childToString(0 , prettyPrint );
            }
        }
      	return "("+childToString(0 , prettyPrint )+")";
    }
    
    @Override
    public boolean hasLiteralValue(IExpressionContext context) {
    	return child(0).hasLiteralValue( context );
    }
    
	@Override
	protected TermNode copyThisNode() 
	{
		return new TermNode();
	}     

    @Override
	public ASTNode evaluate(IExpressionContext context)
    {
        return child(0).evaluate( context );
    }

	@Override
	public boolean isEquals(ASTNode other) {
		if ( other instanceof TermNode ) {
			final int len = getChildCount();
			if ( len != other.getChildCount() ) {
				return false;
			}
			for ( int i = 0 ; i < len ; i++ ) {
				if ( ! child(i).isEquals( other.child(i) ) ) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

    @Override
    protected int thisHashCode()
    {
        return 0x8610231f;
    }
}