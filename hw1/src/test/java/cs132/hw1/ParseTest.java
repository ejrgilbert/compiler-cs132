package cs132.hw1;


import net.jcip.annotations.NotThreadSafe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TODO -- the tests in this class have to be manually executed
 *     one-by-one. There must be something going on with System.in/out
 *     getting botched. I tried forcing the testcases to not run in parallel,
 *     but apparently what I did isn't working :/
 */
@NotThreadSafe
public class ParseTest {

    ByteArrayInputStream testIn;
    PrintStream origOut;
    ByteArrayOutputStream outputStream;

    private void provideInput(String input) {
        testIn = new ByteArrayInputStream(input.getBytes());
        System.setIn(testIn);
    }

    private ByteArrayOutputStream setupOutput() {
        outputStream = new ByteArrayOutputStream();
        origOut = System.out;
        System.setOut(new PrintStream(outputStream));

        return outputStream;
    }

    private ByteArrayOutputStream setup(String input) {
        provideInput(input);
        return setupOutput();
    }

    @AfterEach
    void cleanup() throws IOException {
        testIn.close();
        outputStream.close();
    }

    @Test
    void basicTest() {
        ByteArrayOutputStream outputStream = setup("$1");

        // Run the parser
        Parse.main(new String[0]);

        String result = outputStream.toString();

        System.setOut(origOut);
        System.out.println(result);

        assertEquals("""
                1 $ _
                Expression parsed successfully
                """, result);
    }

    @Test
    void test0() {
        ByteArrayOutputStream outputStream = setup("$1$2");

        // Run the parser
        Parse.main(new String[0]);

        String result = outputStream.toString();

        System.setOut(origOut);
        System.out.println(result);

        assertEquals("""
                1 $ 2 $ _
                Expression parsed successfully
                """, result);
    }

    @Test
    void test1() {
        ByteArrayOutputStream outputStream = setup("""
                $1 +
                (1 - ++$2) $# (a confusing comment)
                3""");

        // Run the parser
        Parse.main(new String[0]);

        String result = outputStream.toString();

        System.setOut(origOut);
        System.out.println(result);

        assertEquals("""
                1 $ 1 2 $ ++_ - + 3 $ _
                Expression parsed successfully
                """, result);
    }

    @Test
    void test2() {
        ByteArrayOutputStream outputStream = setup("$$1++++$2");

        // Run the parser
        Parse.main(new String[0]);

        String result = outputStream.toString();

        System.setOut(origOut);
        System.out.println(result);

        assertEquals("""
                1 $ $ _++ _++ 2 $ _
                Expression parsed successfully
                """, result);
    }

    @Test
    void test3() {
        ByteArrayOutputStream outputStream = setup("$1++--$2");

        // Run the parser
        Parse.main(new String[0]);

        String result = outputStream.toString();

        System.setOut(origOut);
        System.out.println(result);

        assertEquals("""
                1 $ _++ _-- 2 $ _
                Expression parsed successfully
                """, result);
    }

    @Test
    void test4() {
        ByteArrayOutputStream outputStream = setup("++++$1");

        // Run the parser
        Parse.main(new String[0]);

        String result = outputStream.toString();

        System.setOut(origOut);
        System.out.println(result);

        assertEquals("""
                1 $ ++_ ++_ _
                Expression parsed successfully
                """, result);
    }
}
