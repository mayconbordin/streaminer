package org.streaminer.util;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class ArrayUtils {
    public static void swap(int[] arr, int a, int b) {
        int temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
    
    public static void swap(long[] arr, int a, int b) {
        long temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
    
    public static int medSelect(int k, int n, int[] arr) {
        int i, ir = n, j, mid, l = 1, a;

        for (;;) {
            if (ir <= l+1) {
                if (ir == l+1 && arr[ir] < arr[l]) {
                    swap(arr, l, ir);
                }
                return arr[k];
            } else {
                mid = (l+ir) >> 1;
                swap(arr, mid, (l+1));
                if (arr[l] > arr[ir]) {
                    swap(arr, l, ir);
                }
                if (arr[l+1] > arr[ir]) {
                    swap(arr, (l+1), ir);
                }
                if (arr[l] > arr[l+1]) {
                    swap(arr, l, (l+1));
                }
                i=l+1;
                j=ir;
                a=arr[l+1];
                for (;;) {
                    do i++; while (arr[i] < a);
                    do j--; while (arr[j] > a);
                    if (j < i) break;
                    swap(arr, i, j);
                }
                arr[l+1]=arr[j];
                arr[j]=a;
                if (j >= k) ir=j-1;
                if (j <= k) l=i;
            }
        }
    }
    
    public static long longMedSelect(int k, int n, long[] arr) {
        int i, ir = n, j, mid, l = 1;
        long a;

        for (;;) {
            if (ir <= l+1) {
                if (ir == l+1 && arr[ir] < arr[l]) {
                    swap(arr, l, ir);
                }
                return arr[k];
            } else {
                mid = (l+ir) >> 1;
                swap(arr, mid, (l+1));
                if (arr[l] > arr[ir]) {
                    swap(arr, l, ir);
                }
                if (arr[l+1] > arr[ir]) {
                    swap(arr, (l+1), ir);
                }
                if (arr[l] > arr[l+1]) {
                    swap(arr, l, (l+1));
                }
                i=l+1;
                j=ir;
                a=arr[l+1];
                for (;;) {
                    do i++; while (arr[i] < a);
                    do j--; while (arr[j] > a);
                    if (j < i) break;
                    swap(arr, i, j);
                }
                arr[l+1]=arr[j];
                arr[j]=a;
                if (j >= k) ir=j-1;
                if (j <= k) l=i;
            }
        }
    }
}
