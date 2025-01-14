package com.luiges90.renx;

import java.util.Random;

public class WordGenerator {

    private static final String[] ConsonantPrefix = new String[]
            {
                    "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z",
                    "bl", "br", "ch", "cl", "cr", "dr", "fl", "fr", "gh", "gl", "gr", "kn", "kr",
                    "ph", "sc", "sch", "scr", "sh", "sl", "sm", "sn", "sp", "sph", "st", "str", "sw", "th", "tr", "tw"
            };

    private static final String[] ConsonantSuffix = new String[]
            {
                    "b", "be", "c", "ce", "d", "de", "f", "de", "g", "ge", "h", "he", "k", "ke", "l", "le", "m", "me", "n",
                    "ne", "p", "pe", "r", "re", "s", "se", "t", "te", "v", "ve", "w", "we", "x", "z", "ze", "bt", "by", "ch", "ck",
                    "ct", "cy", "dy", "ff", "ft", "fy", "gh", "gy", "ld", "lf", "lk", "ll", "lp", "lt", "ly", "my", "ng", "nk", "nt",
                    "ny",
                    "pt", "py", "rb", "rd", "rf", "rk", "rl", "rt", "ry", "sh", "sk", "ss", "st", "sy", "th", "tt", "ty", "vy",
                    "wy"
            };

    private static final String[] Vowels = new String[]
            {
                    "a", "e", "i", "o", "u", "ae", "ai", "ao", "au", "ee", "ei", "eo", "eu", "ia", "ie", "io", "iu", "oa", "oe",
                    "oi", "oo", "ou", "ua", "ue", "ui", "uo"
            };

    public static String generateWord(Random random)
    {
        String prefix1, vowel1, suffix1;

        var noPrefix1 = random.nextFloat() < 0.2f;
        if (!noPrefix1)
        {
            prefix1 = Util.from(random, ConsonantPrefix);
        }
        else
        {
            prefix1 = "";
        }

        vowel1 = Util.from(random, Vowels);
        if (noPrefix1 || random.nextFloat() < 0.8f)
        {
            suffix1 = Util.from(random, ConsonantSuffix);
        }
        else
        {
            suffix1 = "";
        }

        String prefix2 = "", vowel2 = "", suffix2 = "";
        if (random.nextFloat() < 0.5f)
        {
            var noPrefix2 = random.nextFloat() < 0.2f;
            if (!noPrefix2)
            {
                prefix2 = Util.from(random, ConsonantPrefix);
            }
            else
            {
                prefix2 = "";
            }

            vowel2 = Util.from(random, Vowels);
            if (noPrefix2 || random.nextFloat() < 0.8f)
            {
                suffix2 = Util.from(random, ConsonantSuffix);
            }
            else
            {
                suffix2 = "";
            }
        }

        return String.format("%s%s%s%s%s%s", prefix1, vowel1, suffix1, prefix2, vowel2, suffix2);
    }
}
