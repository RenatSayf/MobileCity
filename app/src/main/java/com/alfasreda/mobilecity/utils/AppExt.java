package com.alfasreda.mobilecity.utils;

import kotlin.UByteArray;

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

    public void convertBytesToCoordinate(UByteArray bytes){



        //преобразование в long со знаком. n-широта, e-долгота:
        long n = (N0_E0 >> 4) & 0xf;
        long e = (N0_E0) & 0xf;

        n = n * 0x1000000 + ltob(N1) * 0x10000 + ltob(N2) * 0x100 + ltob(N3);
        e = e * 0x1000000 + ltob(E1) * 0x10000 + ltob(E2) * 0x100 + ltob(E3);
        n = sign(n);
        e = sign(e);

        //преобразование из фиксированной запятой в плавающую:
        double dn = n;
        dn = dn / 0x8000000;
        dn = dn * 180;
        double de = e;
        de = de / 0x8000000;
        de = de * 180;
//выходные данные:
//dn - широта, в градусах
//de - долгота, в градусах
    }
}
