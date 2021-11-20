package com.ft.mapp.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * 存放一些在内存中保存的数据
 */
public class GlobalData {

    private static Map<String, Long> sLaunchedAppMap;

    public static void setLaunchedApp(String pkg) {
        if (sLaunchedAppMap == null) {
            sLaunchedAppMap = new HashMap<>();
        }
        sLaunchedAppMap.put(pkg, System.currentTimeMillis());
    }

    public static boolean isAppLaunched(String pkg) {
        if (sLaunchedAppMap == null) {
            return false;
        }
        Long time = sLaunchedAppMap.get(pkg);
        if (time != null) {
            return time > 0;
        }
        return false;
    }


    public static String getRateConfig() {
        return "{\"scene\":{\"main\":{\"enable\":true,\"condition\":\"main\"}},\"conditions\":{\"main\":{\"template\":6,\"new_show_times\":1,\"rate_cycle\":0,\"show_times_after_cycle\":1}},\"common\":{\"start_day\":1,\"start_count\":1,\"go_market_min_rate\":4,\"low_star_rate_country\":[\"US\"],\"rate_delay_low_star\":3,\"back_seconds_limit\":10,\"times_after_low_star\":10,\"new_max_rate_count\":10,\"max_rate_count\":10,\"rate_interval_mins\":5,\"need_feedback_page\":false,\"show_market_hint\":false,\"rate_title\":\"给个好评呗\",\"rate_desc\":\"亲，如果方便的话，请移步应用市场给我们五星好评！\",\"cancel_text\":\"日后再议\",\"rate_text\":\"立即好评\",\"fb_title\":\"您的反馈对我们很重要\",\"fb_desc\":\"有意见您尽管提，我们会不断改进。\",\"fb_text\":\"反馈\"}}";
    }

}
