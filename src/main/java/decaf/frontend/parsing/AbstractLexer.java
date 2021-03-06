package decaf.frontend.parsing;

import decaf.driver.ErrorIssuer;
import decaf.driver.error.DecafError;
import decaf.driver.error.IntTooLargeError;
import decaf.frontend.tree.Pos;

import java.io.IOException;

/**
 * The abstract lexer specifies all methods that a concrete lexer (i.e. the one generated by jflex) should implement.
 * Also, a couple of helper methods are provided.
 * <p>
 * See {@code src/main/jflex/Decaf.jflex}.
 *
 * @param <P> type of parser
 */
abstract class AbstractLexer<P extends AbstractParser> {

    /**
     * Get position of the current token.
     */
    abstract Pos getPos();

    /**
     * Get the next token (if any). NOTE that every token is encoded as an integer, called the token's <em>code</em>.
     *
     * @throws IOException in case I/O error occurs
     */
    abstract int yylex() throws IOException;

    private P parser;
    private ErrorIssuer issuer;

    /**
     * When lexing, we need to interact with the parser to set semantic value.
     */
    void setup(P parser, ErrorIssuer issuer) {
        this.parser = parser;
        this.issuer = issuer;
    }

    /**
     * Helper method used by the concrete lexer: record a keyword by its code.
     *
     * @param code standard code of a Decaf keyword, specified in {@link Tokens}
     * @return parser-specified token
     */
    protected int keyword(int code) {
        var token = parser.tokenOf(code);
        parser.semValue = new SemValue(code, getPos());
        return token;
    }

    /**
     * Helper method used by the concrete lexer: record an operator.
     * <p>
     * Operators may contain multiple characters.
     * NOTE: ALL parsers MUST use ASCII code to encode a single-character token.
     *
     * @param code standard code of a Decaf keyword, specified in {@link Tokens}
     * @return parser-specified token
     */
    protected int operator(int code) {
        var token = parser.tokenOf(code);
        parser.semValue = new SemValue(code, getPos());
        return token;
    }

    /**
     * Helper method used by the concrete lexer: record a constant integer.
     *
     * @param value the text representation of the integer
     * @return parser-specified token
     */
    protected int intConst(String value) {
        var token = parser.tokenOf(Tokens.INT_LIT);
        parser.semValue = new SemValue(Tokens.INT_LIT, getPos());
        try {
            parser.semValue.intVal = Integer.decode(value);
        } catch (NumberFormatException e) {
            issueError(new IntTooLargeError(getPos(), value));
        }
        return token;
    }

    /**
     * Helper method used by the concrete lexer: record a constant bool.
     *
     * @param value the text representation of the bool, i.e. "true" or "false"
     * @return parser-specified token
     */
    protected int boolConst(boolean value) {
        var token = parser.tokenOf(Tokens.BOOL_LIT);
        parser.semValue = new SemValue(Tokens.BOOL_LIT, getPos());
        parser.semValue.boolVal = value;
        return token;
    }

    /**
     * Helper method used by the concrete lexer: record a constant string.
     *
     * @param value the _quoted_ string, i.e. the exact user input
     * @return parser-specified token
     */
    protected int stringConst(String value, Pos pos) {
        var token = parser.tokenOf(Tokens.STRING_LIT);
        parser.semValue = new SemValue(Tokens.STRING_LIT, pos);
        parser.semValue.strVal = value;
        return token;
    }

    /**
     * Helper method used by the concrete lexer: record an identifier.
     *
     * @param name the text representation (or name) of the identifier
     * @return parser-specified token
     */
    protected int identifier(String name) {
        var token = parser.tokenOf(Tokens.IDENTIFIER);
        parser.semValue = new SemValue(Tokens.IDENTIFIER, getPos());
        parser.semValue.strVal = name;
        return token;
    }

    /**
     * Helper method used by the concrete lexer: report error.
     *
     * @param error the error
     */
    protected void issueError(DecafError error) {
        issuer.issue(error);
    }
}
