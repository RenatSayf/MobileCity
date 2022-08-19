package com.alfasreda.mobilecity.utils;

public class AppExt
{
    private long sign(long input)
    {
        if (input < 0x8000000) return input;
        return input |((~0x7ffffff));
    }

    private long ltob(byte input)
    {
        long r = input;
        if (r < 0) r=r+256;
        return r;
    }
}
