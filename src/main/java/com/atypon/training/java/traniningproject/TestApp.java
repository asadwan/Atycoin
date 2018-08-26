package com.atypon.training.java.traniningproject;

import com.atypon.training.java.traniningproject.internodecommunication.Node;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TestApp {
    public static void main(String[] args) {

        Node node = new Node(8764);
        Map<String, Node> myNode = new HashMap<>();
        Type stringNodeType = new TypeToken<Map<String, Node>>() {
        }.getType();
        myNode.put("node", node);
        Gson gson = new Gson();
        String json = gson.toJson(myNode);
        //System.out.println(json);

        myNode = gson.fromJson(json, Map.class);
        System.out.println(myNode);
        System.out.println(myNode.keySet().contains("node"));

        myNode = gson.fromJson(json, stringNodeType);
        System.out.println(myNode);

    }
}
