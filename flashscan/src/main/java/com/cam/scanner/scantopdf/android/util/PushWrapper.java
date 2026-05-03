package com.cam.scanner.scantopdf.android.util;

import java.util.Map;

public class PushWrapper {
    public String body, type, sid, action, title, img_url, offer_url, plan_id;


    public PushWrapper(Map<String, String> message) {
        body = message.get("body");
        title = message.get("title");
        sid = message.get("sid");
        action = message.get("action");
        img_url = message.get("picture_url");
        offer_url = message.get("offer_url");
        plan_id = message.get("plan_id");
    }
}
