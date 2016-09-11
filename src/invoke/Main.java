package invoke;

import static invoke.Global.*;

import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        boolean redirect = System.console() == null;
        String prompt = "> ";
        Reader reader = null;
        while (true) {
            if (!redirect) {
                System.out.print(prompt);
                System.out.flush();
            }
            if (reader == null)
                reader = new Reader(new InputStreamReader(System.in));
            try {
                Object input = reader.read();
                if (input == Reader.EOF_OBJECT ||
                    input == Symbol.of("exit") ||
                    input == Symbol.of("quit"))
                    break;
                Object evaled = eval(input);
                if (evaled != UNDEF)
                    System.out.println(evaled);
            } catch (Exception e) {
                System.err.println("! " + e.getClass().getSimpleName() + ": "+ e.getMessage());
            }
        }
    }

}
