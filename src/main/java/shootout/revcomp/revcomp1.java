package shootout.revcomp;

/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 * contributed by Jon Edvardsson
 * added fork-join to the original
 * program by Anthony Donnefort and Enotus.
 */

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public final class revcomp1 {

    static final ForkJoinPool fjPool = new ForkJoinPool();

    static final byte[] map = new byte[128];

    static {
        String[] mm = {"ACBDGHK\nMNSRUTWVYacbdghkmnsrutwvy",
                       "TGVHCDM\nKNSYAAWBRTGVHCDMKNSYAAWBR"};
        for (int i = 0; i < mm[0].length(); i++)
            map[mm[0].charAt(i)] = (byte) mm[1].charAt(i);
    }

    static void reverse(byte[] buf, int begin, int end) {
        while (true) {
            byte bb = buf[begin];
            if (bb == '\n')
                bb = buf[++begin];
            byte be = buf[end];
            if (be == '\n')
                be = buf[--end];
            if (begin > end)
                break;
            buf[begin++] = map[be];
            buf[end--] = map[bb];
        }
    }

    private static class ReverseComplement extends RecursiveAction {
        private static int SEQUENTIAL_THRESHOLD = 1024 * 1024 * 16;
        private byte[] buf;
        private int begin;
        private int end;

        public ReverseComplement(byte[] buf, int begin, int end) {
            this.buf = buf;
            this.begin = begin;
            this.end = end;
        }

        protected void compute() {
            byte[] buf = this.buf;
            int begin = this.begin;
            int end = this.end;

            if (begin - end <= SEQUENTIAL_THRESHOLD) {
                reverse(buf, begin, end);
            } else {
                int mid = begin + (end - begin) / 2;
                ReverseComplement left = new ReverseComplement(buf, begin, mid);
                ReverseComplement right = new ReverseComplement(buf, mid + 1, end);
                invokeAll(left, right);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final byte[] buf = new byte[System.in.available()];
        System.in.read(buf);
        List<ReverseComplement> tasks = new LinkedList<ReverseComplement>();

        for (int i = 0; i < buf.length; ) {
            while (buf[i++] != '\n') ;
            int data = i;
            while (i < buf.length && buf[i++] != '>') ;
            ReverseComplement task = new ReverseComplement(buf, data, i - 2);
            fjPool.execute(task);
            tasks.add(task);
        }
        for (ReverseComplement task : tasks) {
            task.join();
        }

        System.out.write(buf);
    }
}

