package com.sct.picturebackend.utils;

import java.awt.*;

/**
 * 工具类：计算颜色相似度
 */
public class ColorSimilarUtils {
    private ColorSimilarUtils() {
        //工具类不需要实例化
    }

    public static double calculateSimilarity(Color color1, Color color2) {
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();

        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();

        //计算欧式距离
        double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
        //计算相似度，值越大，颜色越相似（原本是越小越相似）
        return 1.0 - distance / Math.sqrt(3 * Math.pow(255, 2));
    }
}
