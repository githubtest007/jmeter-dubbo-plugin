package com.test.dubbo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * dubbo调用工具
 * 
 * @author phwang
 * @date 2016/11/2
 */
@Service
public class DubboClient {

    private static final Logger    log      = LoggerFactory.getLogger(DubboTestInvoker.class);
    //    @Value("${dubbo.timeout}")
    private Integer                timeout;

    @Autowired
    private ApplicationContextUtil applicationContextUtil;

    private List<String>           baseType = Arrays.asList("int", "long", "double", "float",
        "byte", "boolean", "short", "char");;

    public ApiResponse invoke(String url, String classDefine, String classMethod,
                              List<String> params) {

        ApiResponse response = new ApiResponse();
        log.info("url:{}, class:{}, method:{}, param:{}", url, classDefine, classMethod, params);
        // find method by serviceMethodName
        Method method = findMethod(classDefine, classMethod, params);

        // convert to parameter
        List<Object> param = getParameterObject(method, params);

        response.setReturnType(method.getReturnType());
        Object service = getDubboService(url, classDefine);
        Object result = null;
        try {
            log.debug("invoke dubbo request:{}",
                ToStringBuilder.reflectionToString(param.toArray(), ToStringStyle.JSON_STYLE));
            result = method.invoke(service, param.toArray());
            if (!StringUtils.equals(method.getReturnType().getName(), "void")) {
                String desc = "null";
                if (result != null) {
                    desc = ToStringBuilder.reflectionToString(result, ToStringStyle.JSON_STYLE);
                }
                log.debug("invoke dubbo response:{}", desc);
            }
            response.setInvokeSuccess(true);
            response.setOriginalResponse(result);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            log.warn("invoke has InvocationTargetException:{}", cause.getMessage());
            Class<?>[] methodExceptionTypes = method.getExceptionTypes();
            for (Class<?> methodExceptionType : methodExceptionTypes) {
                // 如果抛的异常，等于方法定义的异常，说明网络调通了。 // 调通了
                if (StringUtils.equals(cause.getClass().getName(), methodExceptionType.getName())) {
                    response.setInvokeSuccess(true);
                    response.setRespText(cause.getMessage());
                    response.setResponseBody(cause.getMessage());
                    return response;
                }
            }
            response.setRespText(cause.getMessage());
            response.setInvokeSuccess(false);
            return response;
        } catch (Exception e) {
            log.warn("invoke has error:{}", e.getMessage());
            response.setRespText(e.getMessage());
            response.setInvokeSuccess(false);
            return response;
        }
        response.setInvokeSuccess(true);
        response.setResponseBody(result);
        if (StringUtils.equals(method.getReturnType().getName(), "void")) {
            response.setRespText(Constant.RETURN_VOID_TEXT);
        } else if (result == null) {
            response.setRespText(Constant.RETURN_NULL);
        } else {
            response
                .setRespText(ToStringBuilder.reflectionToString(result, ToStringStyle.JSON_STYLE));
        }

        return response;
    }

    private List<Object> getParameterObject(Method method, List<String> base) {
        List<Object> result = new ArrayList<>();
        Type[] types = method.getGenericParameterTypes();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        try {
            for (int i = 0; i < types.length; i++) {
                Type typ = types[i];
                String param = base.get(i);
                String typeName = typ.getTypeName();
                // java基本类型
                if (baseType.contains(typeName)) {
                    result.add(param);
                    continue;
                }

                // java引用类型
                if ("java.lang.Class".equals(typ.getClass().getName())) {
                    if (typeName.contains("[]")) {
                        typeName = StringUtils.substring(typeName, 0, typeName.indexOf("[]"));
                        // TODO 这里写死吧，赶时间
                        typeName = "[L" + typeName + ";";
                    }
                    Class<?> typeClazz = Class.forName(typeName);
                    // enum
                    if (typeClazz.isEnum()) {
                        Method valueOf = typeClazz.getMethod("valueOf", String.class);
                        Object value = valueOf.invoke(typeClazz, param);
                        result.add(value);
                    } else if ("java.lang.String".equals(typeClazz.getName())) {// string
                        result.add(param);
                    } else {
                        Object value = mapper.readValue(param, typeClazz);
                        result.add(value);
                    }
                } else if (typ instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) typ;
                    // 集合类型
                    String collectionType = type.getRawType().getTypeName();
                    Class<?> rawTypeClass = Class.forName(collectionType);
                    // collection系列
                    if ("java.util.List".equals(collectionType)
                        || "java.util.Set".equals(collectionType)) {
                        Object value = mapper.readValue(param,
                            mapper.getTypeFactory().constructCollectionType(
                                (Class<? extends Collection>) rawTypeClass,
                                // 集合里面的类型
                                Class.forName(type.getActualTypeArguments()[0].getTypeName())));
                        result.add(value);

                        // map 系列
                    } else if (collectionType.equals("java.util.Map")) {
                        // 集合里面的类型
                        Object value = mapper.readValue(param,
                            mapper.getTypeFactory().constructMapType(
                                (Class<? extends Map>) rawTypeClass,
                                // 集合里面的类型
                                Class.forName(type.getActualTypeArguments()[0].getTypeName()),
                                Class.forName(type.getActualTypeArguments()[1].getTypeName())));
                        result.add(value);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException | InvocationTargetException
                | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Method findMethod(String classDefine, String classMethod, List<String> param) {
        // 找方法
        Method[] declaredMethods = new Method[0];
        try {
            declaredMethods = Class.forName(classDefine).getDeclaredMethods();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("not found method of　" + classMethod);
        }

        // key = methodName+parameterSize, value = method reference
        Map<String, Method> methodMap = new HashMap<>();

        for (Method method : declaredMethods) {
            methodMap.put(method.getName() + "-" + method.getParameterTypes().length, method);
        }

        Method method = methodMap.get(classMethod + "-" + param.size());

        if (method == null) {
            throw new RuntimeException("not found method of　" + classMethod);
        }
        return method;
    }

    private Object getDubboService(String ip, String serviceName) {
        try {
            ReferenceConfig<Object> reference = new ReferenceConfig<>();
            ApplicationConfig bean = applicationContextUtil.getApplicationContext()
                .getBean(ApplicationConfig.class);
            reference.setApplication(bean);
            reference.setInterface(Class.forName(serviceName));

            reference.setUrl(ip + "/" + serviceName);
            if (timeout == null || timeout == 0) {
                timeout = 50000;
            }
            reference.setTimeout(timeout);
            return reference.get();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
