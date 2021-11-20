package com.ft.mapp;

import android.content.Context;

import com.ft.mapp.R;
import com.ft.mapp.home.models.BrandItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class BrandConstant {
    private static LinkedHashMap<String, ArrayList<BrandItem>> listLinkedHashMap = null;

    public static LinkedHashMap<String, ArrayList<BrandItem>> get(Context context) {
        if (listLinkedHashMap != null) {
            return listLinkedHashMap;
        }
        listLinkedHashMap = new LinkedHashMap<>();

        //iphone
        ArrayList<BrandItem> list = new ArrayList<>();
        String[] arr = context.getResources().getStringArray(R.array.iphone);
        for (String model : arr) {
            BrandItem item = new BrandItem("iphone", model);
            list.add(item);
        }
        listLinkedHashMap.put("iPhone", list);

        //huawei
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.huawei);
        for (String model : arr) {
            BrandItem item = new BrandItem("huawei", model);
            list.add(item);
        }
        listLinkedHashMap.put("华为", list);

        //samsung
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.samsung);
        for (String model : arr) {
            BrandItem item = new BrandItem("samsung", model);
            list.add(item);
        }
        listLinkedHashMap.put("三星", list);

        //honor
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.honor);
        for (String model : arr) {
            BrandItem item = new BrandItem("honor", model);
            list.add(item);
        }
        listLinkedHashMap.put("荣耀", list);

        //oppo
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.oppo);
        for (String model : arr) {
            BrandItem item = new BrandItem("oppo", model);
            list.add(item);
        }
        listLinkedHashMap.put("OPPO", list);

        //vivo
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.vivo);
        for (String model : arr) {
            BrandItem item = new BrandItem("vivo", model);
            list.add(item);
        }
        listLinkedHashMap.put("vivo", list);

        //xiaomi
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.xiaomi);
        for (String model : arr) {
            BrandItem item = new BrandItem("xiaomi", model);
            list.add(item);
        }
        listLinkedHashMap.put("小米", list);

        //redmi
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.redmi);
        for (String model : arr) {
            BrandItem item = new BrandItem("redmi", model);
            list.add(item);
        }
        listLinkedHashMap.put("红米", list);

        //meizu
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.meizu);
        for (String model : arr) {
            BrandItem item = new BrandItem("meizu", model);
            list.add(item);
        }
        listLinkedHashMap.put("魅族", list);

        //oneplus
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.oneplus);
        for (String model : arr) {
            BrandItem item = new BrandItem("oneplus", model);
            list.add(item);
        }
        listLinkedHashMap.put("一加", list);

        //nokia
        list = new ArrayList<>();
        arr = context.getResources().getStringArray(R.array.nokia);
        for (String model : arr) {
            BrandItem item = new BrandItem("nokia", model);
            list.add(item);
        }
        listLinkedHashMap.put("诺基亚", list);

        //custom
        list = new ArrayList<>();
        BrandItem item = new BrandItem("custom", "高级设置");
        list.add(item);
        listLinkedHashMap.put("自定义", list);

        return listLinkedHashMap;
    }
}
