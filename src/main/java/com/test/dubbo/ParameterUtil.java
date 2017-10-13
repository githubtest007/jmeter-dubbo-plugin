package com.test.dubbo;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 *
 * @author wangpeihu
 * @date 2017/10/13 13:49
 *
 */
public class ParameterUtil {
    public static List<String> getRequestBodyList(String text) {
        List<String> list = new ArrayList<String>();
        Object obj = JSON.parse(text);
        if (obj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) obj;
            for (Object o : jsonArray) {
                list.add(o.toString());
            }
        } else if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            list.add(jsonObject.toString());
        }

        return list;
    }
}
