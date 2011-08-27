package shootout.revcomp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings({"InfiniteLoopStatement"})
@RunWith(Parameterized.class)
public class RevCompTest {

    private static String expectedOutput;
    private Class<?> revcomp;

    @BeforeClass
    public static void beforeClass() throws Exception {
        expectedOutput = new String(readAllBytesFromResource("shootout/revcomp/revcomp-output.txt"));
    }

    public RevCompTest(Class<?> revcomp) {
        this.revcomp = revcomp;
    }

    @Parameterized.Parameters
    @SuppressWarnings({"unchecked"})
    public static Collection<Object[]> getRevcompClasses() {
        Collection parameters = new ArrayList<Object[]>();
        int i = 1;
        try {
            while (true) {
                Class<?> c = Class.forName("shootout.revcomp.revcomp" + i++);
                parameters.add(new Object[]{c});
            }
        } catch (ClassNotFoundException e) {
        }
        return parameters;
    }

    private static byte[] readAllBytesFromResource(String name) throws IOException {
        return Files.readAllBytes(new File(ClassLoader.getSystemResource(name).getFile()).toPath());
    }

    @Test
    public void testDefault() throws Exception {
        testRevComp(revcomp);
    }

    public void testRevComp(Class<?> revcomp) {
        InputStream inOld = System.in;
        PrintStream outOld = System.out;
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(110000);
        try (
                InputStream in = ClassLoader.getSystemResourceAsStream("shootout/revcomp/revcomp-input.txt");
                PrintStream out = new PrintStream(byteOutput)
        ) {
            System.setIn(in);
            System.setOut(out);
            Method main = revcomp.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[0]);
            Assert.assertEquals(expectedOutput, byteOutput.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            System.setIn(inOld);
            System.setOut(outOld);
        }

    }
}
