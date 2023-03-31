package net.okocraft.scoreboard.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class LengthChecker {

    private static final int MAX_LIMIT = 64;
    private static int lengthLimit = MAX_LIMIT;

    private LengthChecker() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static String check(@NotNull String str) {
        if (lengthLimit < ChatColor.stripColor(str).length()) {

            boolean bool = false;
            int colors = 0;
            int length = 0;

            for (char c : str.toCharArray()) {
                if (bool) {
                    if (-1 < "0123456789abcdefklmnor".indexOf(c)) {
                        colors += 2;
                    }
                    bool = false;
                    continue;
                }

                if (c == ChatColor.COLOR_CHAR) {
                    bool = true;
                }

                length++;

                if (lengthLimit < length) {
                    break;
                }
            }

            return str.substring(0, length + colors - 1);
        } else {
            return str;
        }
    }

    public static void setLimit(int limit) {
        if (limit < MAX_LIMIT) {
            lengthLimit = limit;
        }
    }
}
