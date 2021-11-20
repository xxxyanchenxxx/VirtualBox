package com.fun.vbox.server.pm.parser;

import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.lang.reflect.Method;


public class PackageLite {
    public String packageName;
    public int versionCode;
    public String versionName;

    public static PackageLite parse(File file) {
        XmlResourceParser openXmlResourceParser = null;
        try {
            AssetManager assetManager = AssetManager.class
                    .newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            int intValue = (Integer) addAssetPath.invoke(assetManager, file.getAbsolutePath());

            if (intValue != 0) {
                openXmlResourceParser = assetManager.openXmlResourceParser(
                        intValue, "AndroidManifest.xml");
            } else {
                openXmlResourceParser = assetManager.openXmlResourceParser(
                        intValue, "AndroidManifest.xml");
            }
            PackageLite parse = parse(openXmlResourceParser);
            if (parse == null) {
                parse = new PackageLite();
            }
            openXmlResourceParser.close();
            return parse;
        } catch (Exception e) {
            //
        } finally {
            if (openXmlResourceParser != null) {
                openXmlResourceParser.close();
            }
        }
        return null;
    }

    protected static PackageLite parse(XmlResourceParser xmlResourceParser) throws Exception {
        int currentTag = xmlResourceParser.next();
        PackageLite mPackageLite = new PackageLite();
        while (currentTag != XmlPullParser.END_DOCUMENT) {
            if (currentTag == XmlPullParser.START_TAG) {
                if (xmlResourceParser.getName().equals("manifest")) {
                    parserManifestAttribute(xmlResourceParser, mPackageLite);
                    break;
                }
            }
            currentTag = xmlResourceParser.next();
        }
        return mPackageLite;

    }

    private static void parserManifestAttribute(XmlResourceParser xmlResourceParser, PackageLite mPackageLite) {
        for (int i = 0; i < xmlResourceParser.getAttributeCount(); i++) {
            String value = xmlResourceParser.getAttributeName(i);

            if (value.equalsIgnoreCase("package")) {
                mPackageLite.packageName = xmlResourceParser
                        .getAttributeValue(i);
            }
            if (value.equals("versionCode")) {
                mPackageLite.versionCode = xmlResourceParser
                        .getAttributeIntValue(i, 0);
            } else if (value.equals("versionName")) {
                mPackageLite.versionName = xmlResourceParser
                        .getAttributeValue(i);
            }
        }
    }
}
