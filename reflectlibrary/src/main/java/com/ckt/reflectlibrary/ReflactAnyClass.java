package com.ckt.reflectlibrary;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by ckt on 16-12-13.
 */
public class ReflactAnyClass {
    private static final String TAG = "ReflactAnyClass";

    public static void showAllPublicFields(String classFullName){
        try {
            Class<?> mteleManager = Class.forName(classFullName);
            Field[] fields = mteleManager.getFields();
            for (int i = 0; i < fields.length; i++) {
                Class<?> cl = fields[i].getType();
                MyLogs.MyLogD(TAG, "fields:" + cl + "___" + fields[i].getName());
            }
        }catch (Exception e){
            MyLogs.MyLogD(TAG, "exception : "+ e.toString());
        }finally {

        }
    }

    public static void showAllDeclaredFields(String classFullName){
        try {
            Class<?> mteleManager = Class.forName(classFullName);
            Field[] fields = mteleManager.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Class<?> cl = fields[i].getType();
                MyLogs.MyLogD(TAG, "declaredFields:" + cl + "___" + fields[i].getName());
            }
        }catch (Exception e){
            MyLogs.MyLogD(TAG, "exception : "+ e.toString());
        }finally {

        }
    }

    public static void showAllPublicMethods(String classFullName){
        try {
            Class<?> mteleManager = Class.forName(classFullName);
            Method[] methods = mteleManager.getMethods();
            for (int i = 0; i < methods.length; i++) {
                MyLogs.MyLogD(TAG, "methods:" + methods[i].getName() + "____"
                        + methods[i].getReturnType().getName());
            }
        }catch (Exception e){
            MyLogs.MyLogD(TAG, "exception : "+ e.toString());
        }finally {

        }
    }

    public static void showAllDeclaredMethods(String classFullName){
        try {
            Class<?> mteleManager = Class.forName(classFullName);
            Method[] methods = mteleManager.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                MyLogs.MyLogD(TAG, "methods:" + methods[i].getName() + "____"
                        + methods[i].getReturnType().getName());
            }
        }catch (Exception e){
            MyLogs.MyLogD(TAG, "exception : "+ e.toString());
        }finally {

        }
    }
}
