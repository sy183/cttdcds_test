package com.suy.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
}
