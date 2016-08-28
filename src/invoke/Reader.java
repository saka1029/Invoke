package invoke;

import static invoke.Global.*;

import java.io.IOException;

public class Reader {

    public static final Object EOF_OBJECT = new Object(){};
    static final int EOF = -1;
    static final Object DOT = new Object(){};

    private final java.io.Reader reader;
    int ch;
    
    public Reader(java.io.Reader reader) {
        this.reader = reader;
        get();
    }
    
    int get() {
        if (ch == EOF)
            return EOF;
        try {
            return ch = reader.read();
        } catch (IOException e) {
            throw new InvokeException(e);
        }
    }
   
    static boolean isSpace(int ch) {
        return Character.isWhitespace(ch);
    }
    
    static boolean isDigit(int ch) {
        return Character.isDigit(ch);
    }
    
    static boolean isDelimiter(int ch) {
        switch (ch) {
        case '\'': case '(': case ')':
        case ',':
        case '"':
            return true;
        }
        return false;
    }
    
    static boolean isSymbol(int ch) {
        return ch != EOF && !isSpace(ch) && !isDelimiter(ch);
    }

    Object readList() {
        Pair.Builder builder = new List.Builder();
        while (true) {
            skipSpaces();
            switch (ch) {
            case ')':
                get();
                return builder.build();
            case EOF:
                throw new InvokeException("')' expected");
            default:
                Object r = readObject();
                if (r == DOT) {
                    Object last = read();
                    skipSpaces();
                    if (ch != ')')
                        throw new InvokeException("')' expected");
                    get();
                    builder.last(last);
                    return builder.build();
                }
                builder.tail(r);
                break;
            }
        }
    }
    
    Object readSymbol(StringBuilder sb) {
        while (isSymbol(ch)) {
            sb.append((char)ch);
            get();
        }
        String s = sb.toString();
        switch (s) {
        case "true": return TRUE;
        case "false": return FALSE;
        default: return symbol(sb.toString());
        }
    }
    
    Integer readNumber(StringBuilder sb) {
        while (isDigit(ch)) {
            sb.append((char)ch);
            get();
        }
        return Integer.parseInt(sb.toString());
    }
    
    String readString(StringBuilder sb) {
        sb.deleteCharAt(0);
        while (ch != EOF && ch != '\n' && ch != '\r' && ch != '"') {
            switch (ch) {
            case '\\':
                switch (get()) {
                case 'r': sb.append("\r"); break;
                case 'n': sb.append("\n"); break;
                case 't': sb.append("\t"); break;
                default: sb.append(ch); break;
                }
                break;
            default:
                sb.append((char)ch);
            }
            get();
        }
        if (ch != '"')
            throw new InvokeException("Unterminated string: %s", sb);
        get();
        return sb.toString();
    }

    Object readAtom() {
        int first = ch;
        StringBuilder sb = new StringBuilder();
        sb.append((char)first);
        get();
        switch (first) {
        case '+': case '-':
            return isDigit(ch) ? readNumber(sb) : readSymbol(sb);
        case '.':
            return isSymbol(ch) ? readSymbol(sb) : DOT;
        case '"':
            return readString(sb);
        default:
            return isDigit(first) ? readNumber(sb) : readSymbol(sb);
        }
    }

    void skipSpaces() {
        while (isSpace(ch))
            get();
    }

    Object readObject() {
        skipSpaces();
        switch (ch) {
        case EOF:
            return EOF_OBJECT;
        case '(':
            get();
            return readList();
        case '\'':
            get();
            return list(QUOTE, read());
        default:
            return readAtom();
        }
    }

    public Object read() {
        Object r = readObject();
        if (r == DOT)
            throw new InvokeException("unexpected dot");
        return r;
    }
}
