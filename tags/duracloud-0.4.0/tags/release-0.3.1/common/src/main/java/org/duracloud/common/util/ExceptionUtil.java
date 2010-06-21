
package org.duracloud.common.util;

public class ExceptionUtil {

    public static String getStackTraceAsString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement elem : e.getStackTrace()) {
            sb.append(elem.toString() + "\n");
        }
        return sb.toString();
    }

}
