package com.nangosha.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nangosha.lang.TokenType.*;

// this is the scanner. it will be responsible for generating and returning the tokens
// when given a source file.
// make this class static, so it can be accessed to all Lang runtimes.
public class Scanner {
    private  final String source;
    private static final Map<String, TokenType> keywords;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0; // need to keep track of the current pos we are visiting.
    private int line = 1;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source){
        this.source = source;
    }

    public List<Token> scanTokens(){
        while (!isEOF()) {
            // we are at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken(){
        // we can begin by getting a character
        char c = advance();

        switch (c){
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(isNextCharMatch('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(isNextCharMatch('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(isNextCharMatch('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(isNextCharMatch('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (isNextCharMatch('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isEOF()) advance();
                } else if (isNextCharMatch('*')){
                    if (isNextCharMatch('/') || isEOF()){
                        Lang.error(line, "This is not a valid comment.");
                    } else{
                        while (peek() != '\n' && !isEOF() && peekBefore() != '/') advance();
                    }
                }
                else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lang.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char peekNext() {
        // this also looks at the char after current, but also does not consume it.
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char peekBefore(){
        if(current-1 <= 0) return '\0';
        return source.charAt(current-1);
    }

    private void string() {
        while (peek() != '"' && !isEOF()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isEOF()) {
            Lang.error(line, "Unterminated string.");
            return;
        }
        // The closing ".
        advance();
        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean isNextCharMatch(char expected) {
        if (isEOF()) return false;
        if (source.charAt(current) != expected) return false;
        current++; // we then skip the pos of that next character
        return true;
    }

    private char peek() {
        // this looks at the current char but doesn't consume it.
        if (isEOF()) return '\0';
        return source.charAt(current);
    }

    // we want to know whether we have reached the end of the source file
    private boolean isEOF(){
        return  current >= source.length();
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private  void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char advance(){
        // we increment our "current" counter
        current++;

        // in order to know whether we have reached the end of the file, we need to have a look at the next
        // character. The reason we do "current-1", is because we increment the counter
        // before we read the character at that position.
        return source.charAt(current - 1);
    }

    // add support for C-style /*  ... */ comment style


}
