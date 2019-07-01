package com.gitlab.indicode.fabric.tooltipwrap;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.TextComponentUtil;
import net.minecraft.datafixers.fixes.ChunkPalettedStorageFix;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Indigo A.
 */
public class TooltipDataHolder { // Statics are fine cause you can only do one tooltip at a time, right?
    public static int initX, initY, x, y, screenWidth, screenHeight, txtY, newHeight = -1;
    public static List<String> strings;
    public static Map<String, List<String>> chopMap = new HashMap();
    public static TextRenderer font;
    public static int getNewHeight() {
        if (newHeight == -1) {
            int height = 0;
            for (String string: strings) {
                if (isOffender(string)) {
                    int lines = chop(string).size();
                    height += lines * 10;
                } else {
                    height += 10;
                }
            }
            newHeight = height;
        }
        return newHeight;
    }
    public static int longestStringWidth() {
        int width = 0;
        for (String string: strings) {
            int w = font.getStringWidth(string);
            if (w > width) width = w;
        }
        return width;
    }
    public static List<String> offenders() {
        List<String> offenders = new ArrayList();
        for (String string: strings) {
            int w = font.getStringWidth(string);
            if (w > screenWidth - 10) offenders.add(string);
        }
        return offenders;
    }
    public static boolean isOffender(String string) {
        return font.getStringWidth(string) > screenWidth - 10;
    }
    public static List<String> chop(String string) {
        if (chopMap.containsKey(string)) return chopMap.get(string);
        else {
            String[] split = string.split(" ");
            List<String> chops = new ArrayList();
            int lastChop = 0;
            int pieces = split.length;
            String oldAttempt = "";
            String attempt = "";
            for (int i = 0; i < pieces; i++) {
                attempt += split[i] + " ";
                if (font.getStringWidth(attempt) > screenWidth - 15) {
                    if (font.getStringWidth(split[i]) > screenWidth - 15) {
                        if (!chops.isEmpty()) oldAttempt = getEndForematting(chops.get(chops.size() - 1)) + oldAttempt;
                        chops.add(oldAttempt);
                        attempt = "";
                        //System.out.println(split[i]);
                        String joldAttempt = "";
                        String jattempt = "";
                        for (int j = 0; j < split[i].length(); j++) {
                            jattempt += split[i].charAt(j);
                            if (font.getStringWidth(jattempt) > screenWidth - 15) {
                                if (!chops.isEmpty()) joldAttempt = getEndForematting(chops.get(chops.size() - 1)) + joldAttempt;
                                chops.add(joldAttempt);
                                jattempt = String.valueOf(split[i].charAt(j));
                                //System.out.println(joldAttempt + " -> " + jattempt + " -> " + j + "/" + split[i].length() + " WID: " + font.getStringWidth(jattempt) + "/" + screenWidth);
                            }
                            joldAttempt = jattempt;
                        }
                        if (!jattempt.equals("")) {
                            if (!chops.isEmpty()) jattempt = getEndForematting(chops.get(chops.size() - 1)) + jattempt;
                            chops.add(jattempt);
                            //System.out.println(jattempt);
                        }
                        i++;
                    } else {
                        if (!chops.isEmpty()) oldAttempt = getEndForematting(chops.get(chops.size() - 1)) + oldAttempt;
                        chops.add(oldAttempt);
                        attempt = split[i] + " ";
                    }
                }
                if (attempt.length() > 0) attempt.substring(0, attempt.length() - 1);
                oldAttempt = attempt;
            }
            if (!attempt.equals("")) {
                if (!chops.isEmpty()) attempt = getEndForematting(chops.get(chops.size() - 1)) + attempt;
                chops.add(attempt);
            }
            chopMap.put(string, chops);
            return chops;
        }
    }
    public static String getEndForematting(String string) {
        String[] resets = string.split(Formatting.RESET.toString());
        String txt = resets[resets.length - 1];
        return Formatting.getFormatAtEnd(txt);
    }
}
