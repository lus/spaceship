package pm.lus.spaceship.util.collection;

import java.lang.reflect.Array;

/**
 * Provides some utility methods for arrays
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * Merges two arrays
     *
     * @param a   The first array
     * @param b   The second array which gets appended to the first array
     * @param <T> The type of the array contents
     * @return The merged array
     */
    public static <T> T[] merge(final T[] a, final T[] b) {
        final T[] result = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}
