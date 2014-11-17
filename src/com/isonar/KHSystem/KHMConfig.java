package com.isonar.KHSystem;

import android.os.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: gustaw
 * Date: 15.12.12
 * Time: 13:23
 * To change this template use File | Settings | File Templates.
 */
public class KHMConfig {
    public static File getKHMDir() {
        return new File(Environment.getExternalStorageDirectory(),"KHM");
    }

    public static boolean isStorageMounted()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static boolean existsKHMDir() {
        try {
            File file = getKHMDir();
            return file.isDirectory();
        } catch (Exception ex) {
            return false;
        }
    }

    public static int getDensityDPI() {
        try {
            File file = new File(getKHMDir(),"config.xml");
            if (!file.isFile()) {
                return -1;
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("KHMConfig");

            for (int s = 0; s < nodeLst.getLength(); s++) {
                Node fstNode = nodeLst.item(s);
                if (fstNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element elem = (Element) fstNode;
                NodeList dpiElmntLst = elem.getElementsByTagName("densityDPI");
                Element dpiElmnt = (Element) dpiElmntLst.item(0);
                NodeList dpi = dpiElmnt.getChildNodes();
                String dpiValue = ((Node) dpi.item(0)).getNodeValue();
                return Integer.parseInt(dpiValue);
            }
        } catch (Exception ex) {
        }
        return -1;
    }
}
