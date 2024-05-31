import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Main {
    private static final int SEED = 42;
    public static void main(String[] args) {
        Random rand = new Random(SEED);
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the array size N: ");
        int arraySize = scanner.nextInt();
        // int arraySize = 8388608; 
        System.out.print("Enter the thread count: ");
        int threadCount = scanner.nextInt();
        scanner.close();

        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= arraySize; i++) {
            list.add(i);
        }
        Collections.shuffle(list, rand); // Shuffles the list using the seeded random

        int[] array = list.stream().mapToInt(i -> i).toArray();
        int[] arrayCopy = array.clone();  // Make a copy for concurrent sorting

        // Single-threaded merge sort
        System.out.println("Starting single-threaded merge sort...");
        long startTime = System.currentTimeMillis();
        List<Interval> intervals = generate_intervals(0, array.length - 1);
        for (Interval interval : intervals) {
            merge(array, interval.getStart(), interval.getEnd());
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Single-threaded Execution Time: " + elapsedTime + " ms");

        // Concurrent merge sort
        System.out.println("Starting concurrent merge sort...");
        ForkJoinPool pool = new ForkJoinPool(threadCount);
        MergeSortTask task = new MergeSortTask(arrayCopy, 0, arrayCopy.length - 1);
        startTime = System.currentTimeMillis();
        pool.invoke(task);
        elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Concurrent Execution Time: " + elapsedTime + " ms");

        // Output the sorted array for single-threaded
        System.out.println("Single-threaded sorted array (first 100 elements):");
        printArray(array);

        // Output the sorted array for concurrent
        System.out.println("Concurrent sorted array (first 100 elements):");
        printArray(arrayCopy);

        // Verify if arrays are sorted
        System.out.println("Single-threaded array is sorted: " + isSorted(array));
        System.out.println("Concurrent array is sorted: " + isSorted(arrayCopy));
    }

    // Print first 100 elements of the array for brevity
    private static void printArray(int[] array) {
        for (int i = 0; i < 100 && i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }

    // Generate intervals 
    public static List<Interval> generate_intervals(int start, int end) {
        List<Interval> intervals = new ArrayList<>();
        if (start == end) {
            intervals.add(new Interval(start, end));
            return intervals;
        }

        int mid = start + (end - start) / 2;
        intervals.addAll(generate_intervals(start, mid));
        intervals.addAll(generate_intervals(mid + 1, end));
        intervals.add(new Interval(start, end));
        return intervals;
    }

    // Merge function as provided in the template
    public static void merge(int[] array, int s, int e) {
        if (s < e) {
            int m = s + (e - s) / 2;
            int[] left = new int[m - s + 1];
            int[] right = new int[e - m];
            int l_ptr = 0, r_ptr = 0;
            for(int i = s; i <= e; i++) {
                if(i <= m) {
                    left[l_ptr++] = array[i];
                } else {
                    right[r_ptr++] = array[i];
                }
            }
            l_ptr = r_ptr = 0;

            for(int i = s; i <= e; i++) {
                if(l_ptr < left.length && (r_ptr == right.length || left[l_ptr] <= right[r_ptr])) {
                    array[i] = left[l_ptr++];
                } else {
                    array[i] = right[r_ptr++];
                }
            }
        }
    }

    // Utility method to check if the array is sorted
    private static boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }

    static class Interval {
        private int start;
        private int end;

        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

    static class MergeSortTask extends RecursiveAction {
        private final int[] array;
        private final int start;
        private final int end;

        public MergeSortTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (start < end) {
                int mid = start + (end - start) / 2;
                MergeSortTask leftTask = new MergeSortTask(array, start, mid);
                MergeSortTask rightTask = new MergeSortTask(array, mid + 1, end);
                invokeAll(leftTask, rightTask);
                merge(array, start, end);
            }
        }
    }
}
