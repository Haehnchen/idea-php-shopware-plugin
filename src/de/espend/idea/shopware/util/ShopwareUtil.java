package de.espend.idea.shopware.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ShopwareUtil {

    public static void writeShopwareMagicFile(String outputString, String outputPath) {

        File file = new File(outputPath);

        // create .idea folder, should not occur
        File folder = new File(file.getParent());
        if(!folder.exists() && !folder.mkdir()) {
            return;
        }

        FileWriter fw;
        try {
            fw = new FileWriter(file);
            fw.write(outputString);
            fw.close();
        } catch (IOException ignored) {
        }

    }

}
