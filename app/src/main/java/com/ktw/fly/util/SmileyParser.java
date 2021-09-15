/* * Copyright (C) 2009 The Android Open Source Project * * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at * *      http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */package com.ktw.fly.util;import android.content.Context;import android.text.Spannable;import android.text.SpannableStringBuilder;import com.ktw.fly.R;import com.ktw.fly.bean.Emoji;import com.ktw.fly.db.InternationalizationHelper;import java.util.ArrayList;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.regex.Matcher;import java.util.regex.Pattern;/** * A class for annotating a CharSequence with spans to convert textual emoticons to graphical ones. */public class SmileyParser {    private static SmileyParser sInstance;    private static List<Emoji> emojis;    private final Context mContext;    private final Pattern mHtmlPattern;    private Pattern mPattern;    private SmileyParser(Context context) {        emojis = InternationalizationHelper.getEmojiList();        mContext = context;        mPattern = buildPattern();        mHtmlPattern = buildHtmlPattern();        SmileyParser.Smilies.loadMapData();    }    public static SmileyParser getInstance(Context context) {        if (sInstance == null) {            synchronized (SmileyParser.class) {                if (sInstance == null) {                    sInstance = new SmileyParser(context);                }            }        }        return sInstance;    }    private Pattern buildHtmlPattern() {        // Set the StringBuilder capacity with the assumption that the average        // smiley is 3 characters long.        // StringBuilder patternString = new StringBuilder();        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies        // properly so they will be interpreted literally by the regex matcher.        // patternString.append('(');        // patternString.append(Pattern.quote("(<a)(\\w)+(?=</a>)"));        // // Replace the extra '|' with a ')'        // patternString.replace(patternString.length() - 1,        // patternString.length(), ")");        return Pattern.compile("(http://(\\S+?)(\\s))|(www.(\\S+?)(\\s))");    }    /**     * Builds the regular expression we use to find smileys in {@link #addSmileySpans}.     */    private Pattern buildPattern() {        StringBuilder patternString = new StringBuilder();        patternString.append('(');        for (int i = 0; i < 23; i++) {            Smilies.TEXTS1[i] = "[" + emojis.get(i).getEnglish() + "]";            Smilies.IDS1[i] = getDrawableByReflect(emojis.get(i).getFilename().toLowerCase().replace("-", "_"));        }        for (int i = 0; i < 23; i++) {            Smilies.TEXTS2[i] = "[" + emojis.get(i + 23).getEnglish() + "]";            Smilies.IDS2[i] = getDrawableByReflect(emojis.get(i + 23).getFilename().toLowerCase().replace("-", "_"));        }        for (int i = 0; i < 23; i++) {            Smilies.TEXTS3[i] = "[" + emojis.get(i + 46).getEnglish() + "]";            Smilies.IDS3[i] = getDrawableByReflect(emojis.get(i + 46).getFilename().toLowerCase().replace("-", "_"));        }        Smilies.TEXTS1[23] = "[" + "del" + "]";        Smilies.IDS1[23] = getDrawableByReflect("e_del");        Smilies.TEXTS2[23] = "[" + "del" + "]";        Smilies.IDS2[23] = getDrawableByReflect("e_del");        Smilies.TEXTS3[23] = "[" + "del" + "]";        Smilies.IDS3[23] = getDrawableByReflect("e_del");        // Log.e("zx", "buildPattern: " + Smilies.TEXTS.length + " " + Smilies.TEXTS[0].length);        for (int i = 0; i < Smilies.TEXTS.length; i++) {            for (int j = 0; j < Smilies.TEXTS[i].length; j++) {                patternString.append(Pattern.quote(Smilies.TEXTS[i][j]));                patternString.append('|');            }        }        // Replace the extra '|' with a ')'        patternString.replace(patternString.length() - 1, patternString.length(), ")");        return Pattern.compile(patternString.toString());    }    // 通过数据库拿到的图片名字把本地的id反射出来    private int getDrawableByReflect(String imgname) {        try {            int resId = R.drawable.class.getDeclaredField(imgname).getInt(R.drawable.class);            return resId;        } catch (IllegalArgumentException e) {            e.printStackTrace();        } catch (IllegalAccessException e) {            e.printStackTrace();        } catch (NoSuchFieldException e) {            e.printStackTrace();        }        return 0;    }    public void notifyUpdate() {        emojis.clear();        emojis = InternationalizationHelper.getEmojiList();        mPattern = buildPattern();        SmileyParser.Smilies.loadMapData();    }    /**     * Adds ImageSpans to a CharSequence that replace textual emoticons such as :-) with a graphical version.     *     * @param text A CharSequence possibly containing emoticons     * @return A CharSequence annotated with ImageSpans covering any recognized emoticons.     */    public CharSequence addSmileySpans(CharSequence text, boolean canClick) {        SpannableStringBuilder builder = new SpannableStringBuilder(text);        Matcher matcher = mPattern.matcher(text);        while (matcher.find()) {            int resId = Smilies.textMapId(matcher.group());            if (resId != -1) {                builder.setSpan(new MyImageSpan(mContext, resId), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);            }        }        return builder;    }    public List<String> findEmojiName(CharSequence text) {        List<String> names = new ArrayList<>();        Matcher matcher = mPattern.matcher(text);        while (matcher.find()) {            names.add(matcher.group().substring(1, matcher.group().length() - 1));        }        return names;    }    public static class Smilies {        private static final int[] IDS1 = new int[24];        private static final int[] IDS2 = new int[24];        private static final int[] IDS3 = new int[24];        private static final int[][] IDS = {IDS1, IDS2, IDS3};        // 表情文本        private static final String[] TEXTS1 = new String[24];        private static final String[] TEXTS2 = new String[24];        private static final String[] TEXTS3 = new String[24];        private static final String[][] TEXTS = {TEXTS1, TEXTS2, TEXTS3};        private static final Map<String, Integer> MAPS = new HashMap<String, Integer>();        private static final Map<Integer, String> MAPSNAME = new HashMap<Integer, String>();        private static void loadMapData() {            MAPS.clear();            MAPSNAME.clear();            // 取最小的长度，防止长度不一致出错            int length = IDS.length > TEXTS.length ? TEXTS.length : IDS.length;            for (int i = 0; i < length; i++) {                int[] subIds = IDS[i];                String[] subTexts = TEXTS[i];                if (subIds == null || subTexts == null) {                    continue;                }                int subLength = subIds.length > subTexts.length ? subTexts.length : subIds.length;                for (int j = 0; j < subLength; j++) {                    MAPS.put(TEXTS[i][j], IDS[i][j]);                    MAPSNAME.put(IDS[i][j], TEXTS[i][j]);                }            }        }        public static int[][] getIds() {            return IDS;        }        public static String[][] getTexts() {            return TEXTS;        }        public static int textMapId(String text) {            if (MAPS.containsKey(text)) {                return MAPS.get(text);            } else {                return -1;            }        }    }    public static class Gifs {        private static final int[][] IDS = {                {                        R.drawable.gif_eight, R.drawable.gif_eighteen, R.drawable.gif_eleven, R.drawable.gif_fifity,                        R.drawable.gif_fifity_four, R.drawable.gif_fifity_one, R.drawable.gif_fifity_three, R.drawable.gif_fifity_two                        , R.drawable.gif_fifteen, R.drawable.gif_five                },                {                        R.drawable.gif_forty, R.drawable.gif_forty_eight,                        R.drawable.gif_forty_five, R.drawable.gif_forty_four, R.drawable.gif_forty_nine, R.drawable.gif_forty_one                        , R.drawable.gif_forty_seven, R.drawable.gif_forty_three, R.drawable.gif_forty_two, R.drawable.gif_fourteen                },                {                        R.drawable.gif_nine, R.drawable.gif_nineteen, R.drawable.gif_one, R.drawable.gif_seven,                        R.drawable.gif_seventeen, R.drawable.gif_sixteen, R.drawable.gif_ten, R.drawable.gif_thirteen,                        R.drawable.gif_thirty, R.drawable.gif_thirty_eight                },                {                        R.drawable.gif_thirty_five, R.drawable.gif_thirty_four, R.drawable.gif_thirty_nine, R.drawable.gif_thirty_seven,                        R.drawable.gif_thirty_six, R.drawable.gif_thirty_three, R.drawable.gif_thirty_two, R.drawable.gif_thirty_one,                        R.drawable.gif_three, R.drawable.gif_twelve                },                {                        R.drawable.gif_twenty, R.drawable.gif_twenty_eight, R.drawable.gif_twenty_five, R.drawable.gif_twenty_four,                        R.drawable.gif_twenty_nine, R.drawable.gif_twenty_one, R.drawable.gif_twenty_seven, R.drawable.gif_twenty_six                        , R.drawable.gif_twenty_three, R.drawable.gif_twenty_two                }        };        private static final String[][] TEXTS = {                {                        "eight.gif", "eighteen.gif", "eleven.gif", "fifity.gif",                        "fifity_four.gif", "fifity_one.gif", "fifity_three.gif", "fifity_two.gif"                        , "fifteen.gif", "five.gif"                },                {                        "forty.gif", "forty_eight.gif",                        "forty_five.gif", "forty_four.gif", "forty_nine.gif", "forty_one.gif",                        "forty_seven.gif", "forty_three.gif", "forty_two.gif", "fourteen.gif"                },                {                        "nine.gif", "nineteen.gif", "one.gif", "seven.gif",                        "seventeen.gif", "sixteen.gif", "ten.gif", "thirteen.gif",                        "thirty.gif", "thirty_eight.gif",                },                {                        "thirty_five.gif", "thirty_four.gif",                        "thirty_nine.gif", "thirty_seven.gif", "thirty_six.gif", "thirty_three.gif",                        "thirty_two.gif", "thirty-one.gif", "three.gif", "twelve.gif"                },                {                        "twenty.gif", "twenty_eight.gif", "twenty_five.gif", "twenty_four.gif",                        "twenty_nine.gif", "twenty_one.gif", "twenty_seven.gif", "twenty_six.gif"                        , "twenty_three.gif", "twenty_two.gif"                }        };        /**         * gif         */        private static final int[][] PNGID = {                {                        R.drawable.gif_eight_png, R.drawable.gif_eighteen_png, R.drawable.gif_eleven_png, R.drawable.gif_fifity_png,                        R.drawable.gif_fifity_four_png, R.drawable.gif_fifity_one_png, R.drawable.gif_fifity_three_png, R.drawable.gif_fifity_two_png                        , R.drawable.gif_fifteen_png, R.drawable.gif_five_png                },                {                        R.drawable.gif_forty_png, R.drawable.gif_forty_eight_png,                        R.drawable.gif_forty_five_png, R.drawable.gif_forty_four_png, R.drawable.gif_forty_nine_png, R.drawable.gif_forty_one_png                        , R.drawable.gif_forty_seven_png, R.drawable.gif_forty_three_png, R.drawable.gif_forty_two_png, R.drawable.gif_fourteen_png                },                {                        R.drawable.gif_nine_png, R.drawable.gif_nineteen_png, R.drawable.gif_one_png, R.drawable.gif_seven_png,                        R.drawable.gif_seventeen_png, R.drawable.gif_sixteen_png, R.drawable.gif_ten_png, R.drawable.gif_thirteen_png,                        R.drawable.gif_thirty_png, R.drawable.gif_thirty_eight_png                },                {                        R.drawable.gif_thirty_five_png, R.drawable.gif_thirty_four_png, R.drawable.gif_thirty_nine_png, R.drawable.gif_thirty_seven_png                        , R.drawable.gif_thirty_six_png, R.drawable.gif_thirty_three_png, R.drawable.gif_thirty_two_png, R.drawable.gif_thirty_one_png                        , R.drawable.gif_three_png, R.drawable.gif_twelve_png                },                {                        R.drawable.gif_twenty_png, R.drawable.gif_twenty_eight_png, R.drawable.gif_twenty_five_png, R.drawable.gif_twenty_four_png,                        R.drawable.gif_twenty_nine_png, R.drawable.gif_twenty_one_png, R.drawable.gif_twenty_seven_png, R.drawable.gif_twenty_six_png                        , R.drawable.gif_twenty_three_png, R.drawable.gif_twenty_two_png                }        };        private static final Map<String, Integer> MAPS = new HashMap<String, Integer>();        static {            // 取最小的长度，防止长度不一致出错            int length = IDS.length > TEXTS.length ? TEXTS.length : IDS.length;            for (int i = 0; i < length; i++) {                int[] subIds = IDS[i];                String[] subTexts = TEXTS[i];                if (subIds == null || subTexts == null) {                    continue;                }                int subLength = subIds.length > subTexts.length ? subTexts.length : subIds.length;                for (int j = 0; j < subLength; j++) {                    MAPS.put(TEXTS[i][j], IDS[i][j]);                }            }        }        public static int[][] getIds() {            return IDS;        }        public static String[][] getTexts() {            return TEXTS;        }        public static int textMapId(String text) {            if (MAPS.containsKey(text)) {                return MAPS.get(text);            } else {                return -1;            }        }        public static int[][] getPngIds() {            return PNGID;        }    }}