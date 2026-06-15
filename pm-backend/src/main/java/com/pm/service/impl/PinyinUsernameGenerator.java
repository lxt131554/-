package com.pm.service.impl;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.util.function.Predicate;

final class PinyinUsernameGenerator {

    private PinyinUsernameGenerator() {
    }

    static String generate(String realName, Predicate<String> usernameExists) {
        String base = toUsernameBase(realName);
        String candidate = base;
        int suffix = 2;
        while (usernameExists.test(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private static String toUsernameBase(String realName) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        StringBuilder builder = new StringBuilder();
        if (realName != null) {
            for (char c : realName.trim().toCharArray()) {
                appendUsernamePart(builder, c, format);
            }
        }
        return builder.length() > 0 ? builder.toString() : "user";
    }

    private static void appendUsernamePart(StringBuilder builder, char c, HanyuPinyinOutputFormat format) {
        if (c >= 'a' && c <= 'z') {
            builder.append(c);
            return;
        }
        if (c >= 'A' && c <= 'Z') {
            builder.append(Character.toLowerCase(c));
            return;
        }
        if (c >= '0' && c <= '9') {
            builder.append(c);
            return;
        }

        try {
            String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, format);
            if (pinyins != null && pinyins.length > 0) {
                builder.append(pinyins[0].replaceAll("[^a-z0-9]", ""));
            }
        } catch (Exception ignored) {
            // Unsupported punctuation and symbols are skipped.
        }
    }
}
