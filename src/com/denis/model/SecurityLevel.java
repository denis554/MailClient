package com.denis.model;

public class SecurityLevel implements Comparable<SecurityLevel>{

    public int level;
    public String levelName;
    public String levelColor;

    public SecurityLevel(int level, String name, String color) {
        this.level = level;
        this.levelName = name;
        this.levelColor = color;
    }

    @Override
    public String toString() {
        return this.levelName;
    }

    @Override
    public int compareTo(SecurityLevel o) {
        int ret = 0;
        if (level > o.level)
            ret = 1;
        else if (level < o.level)
            ret = -1;
        return ret;
    }
}