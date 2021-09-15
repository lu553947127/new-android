package com.ktw.fly.helper;

import com.ktw.fly.util.Base64;
import com.ktw.fly.util.secure.MAC;
import com.ktw.fly.util.secure.Parameter;

import java.util.Map;

public class MessageSecureHelper {
    public static void mac(String messageKey, Map<String, Object> message) {
        String mac = MAC.encodeBase64((Parameter.joinObjectValues(message)).getBytes(), Base64.decode(messageKey));
        message.put("mac", mac);
    }
}
