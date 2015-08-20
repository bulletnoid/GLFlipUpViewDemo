package com.bulletnoid.android.glflipupdemo.common;

public class Vector3 {
    public float x, y, z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(Vector3 v3) {
        this.x += v3.x;
        this.y += v3.y;
        this.z += v3.z;
    }

    public void scale(float scale) {
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
    }

    public void acc(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public static Vector3 add(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }

    public static void copy(Vector3 v1, Vector3 v2) {
        v1.x = v2.x;
        v1.y = v2.y;
        v1.z = v2.z;
    }

    public static boolean equals(Vector3 v1, Vector3 v2) {
        if (v1.x == v2.x && v1.y == v2.y && v1.z == v2.z) {
            return true;
        }
        return false;
    }

}