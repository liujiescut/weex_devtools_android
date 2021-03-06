package com.taobao.weex.devtools.inspector.protocol.module;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.common.IWXDebugProxy;
import com.taobao.weex.devtools.debug.DebugBridge;
import com.taobao.weex.devtools.inspector.jsonrpc.JsonRpcPeer;
import com.taobao.weex.devtools.inspector.protocol.ChromeDevtoolsDomain;
import com.taobao.weex.devtools.inspector.protocol.ChromeDevtoolsMethod;
import com.taobao.weex.devtools.json.ObjectMapper;
import com.taobao.weex.devtools.json.annotation.JsonProperty;
import com.taobao.weex.utils.LogLevel;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by budao on 16/6/24.
 */
public class WxDebug implements ChromeDevtoolsDomain {
    private static final String TAG = "WxDebug";
    private static final HashMap<String, LogLevel> sLevelMap = new HashMap<String, LogLevel>(6);

    static {
        sLevelMap.put("all", LogLevel.ALL);
        sLevelMap.put("verbose", LogLevel.VERBOSE);
        sLevelMap.put("info", LogLevel.INFO);
        sLevelMap.put("debug", LogLevel.DEBUG);
        sLevelMap.put("warn", LogLevel.WARN);
        sLevelMap.put("error", LogLevel.ERROR);
    }

    private final ObjectMapper mObjectMapper = new ObjectMapper();
    public WxDebug() {

    }

    @ChromeDevtoolsMethod
    public void enable(JsonRpcPeer peer, JSONObject params) {
        Context context = WXEnvironment.getApplication();
        if (context != null) {
            WXSDKEngine.reload(context, true);
            context.sendBroadcast(new Intent(IWXDebugProxy.ACTION_DEBUG_INSTANCE_REFRESH));
        }
    }

    @ChromeDevtoolsMethod
    public void disable(JsonRpcPeer peer, JSONObject params) {
        Context context = WXEnvironment.getApplication();
        if (context != null) {
            WXSDKEngine.reload(context, false);
            context.sendBroadcast(new Intent(IWXDebugProxy.ACTION_DEBUG_INSTANCE_REFRESH));
        }
    }

    @ChromeDevtoolsMethod
    public void setLogLevel(JsonRpcPeer peer, JSONObject params) {
        if (params != null) {
            LogLevel logLevel = sLevelMap.get(params.optString("logLevel"));
            if (logLevel != null) {
                WXEnvironment.sLogLevel = logLevel;
            }
        }
    }

    @ChromeDevtoolsMethod
    public void callNative(JsonRpcPeer peer, JSONObject params) {
        if (params != null) {
            DebugBridge.getInstance().callNative(
                    params.optString("instance"),
                    params.optString("tasks"),
                    params.optString("callback"));
        }
        // another way to handle call native
//        CallNative callNative = mObjectMapper.convertValue(params, CallNative.class);
//        if (callNative != null) {
//            try {
//                String tasks = mObjectMapper.convertListToJsonArray(callNative.tasks).toString();
//                Log.v(TAG, "WxDebug.callNative tasks " + tasks);
//                DebugBridge.getInstance().callNative(callNative.instance,
//                        tasks,
//                        callNative.callback);
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @ChromeDevtoolsMethod
    public void reload(JsonRpcPeer peer, JSONObject params) {
        WXSDKEngine.reload();
        Context context = WXEnvironment.getApplication();
        if (context != null) {
            context.sendBroadcast(new Intent(IWXDebugProxy.ACTION_DEBUG_INSTANCE_REFRESH));
        }
    }

    public static class CallNative {
        @JsonProperty(required = true)
        public String instance;
        @JsonProperty(required = true)
        public String callback;
        @JsonProperty(required = true)
        public List<Task> tasks;
    }

    public static class CallJS {
        @JsonProperty(required = true)
        public String method;
        @JsonProperty(required = true)
        public List<Object> args;
    }

    public static class Task {
        @JsonProperty(required = true)
        public String module;
        @JsonProperty(required = true)
        public String method;
        @JsonProperty(required = true)
        public List<String> args;
    }
}
