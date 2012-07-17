package com.pi.gl.graphics.objects;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class MeshLoader {
	public static float[][] loadHeightMap(File img,
			float minHeight, float maxHeight) throws IOException {
		BufferedImage hMap = ImageIO.read(img);
		final float[][] mapping =
				new float[hMap.getWidth()][hMap.getHeight()];
		for (int x = 0; x < hMap.getWidth(); x++) {
			for (int y = 0; y < hMap.getHeight(); y++) {
				Color col = new Color(hMap.getRGB(x, y));
				mapping[x][y] =
						minHeight
								+ ((col.getRed() + col.getBlue() + col
										.getGreen()) / 765f)
								* (maxHeight - minHeight);
			}
		}
		return mapping;
	}
}
