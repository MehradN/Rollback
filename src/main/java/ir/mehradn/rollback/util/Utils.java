package ir.mehradn.rollback.util;

import org.apache.commons.io.FileUtils;

public interface Utils {
    static String fileSizeToString(long size) {
        if (size < 0)
            return "???";
        return FileUtils.byteCountToDisplaySize(size);
    }
}
