package com.github.gtache.fxml.compiler.impl;

import javafx.beans.NamedArg;

public class WholeConstructorArgs {
    public WholeConstructorArgs(@NamedArg("p1") final int p1, @NamedArg("p2") final Integer p2,
                                @NamedArg("p3") final char p3, @NamedArg("p4") final Character p4,
                                @NamedArg("p5") final boolean p5, @NamedArg("p6") final Boolean p6,
                                @NamedArg("p7") final byte p7, @NamedArg("p8") final Byte p8,
                                @NamedArg("p9") final short p9, @NamedArg("p10") final Short p10,
                                @NamedArg("p11") final long p11, @NamedArg("p12") final Long p12,
                                @NamedArg("p13") final float p13, @NamedArg("p14") final Float p14,
                                @NamedArg("p15") final double p15, @NamedArg("p16") final Double p16,
                                @NamedArg("p17") final String p17, @NamedArg("p18") final Object p18) {
    }

    public WholeConstructorArgs(@NamedArg(value = "p1", defaultValue = "1") final int p1,
                                @NamedArg(value = "p3", defaultValue = "a") final char p3,
                                @NamedArg(value = "p5", defaultValue = "true") final boolean p5,
                                @NamedArg(value = "p7", defaultValue = "2") final byte p7,
                                @NamedArg(value = "p9", defaultValue = "3") final short p9,
                                @NamedArg(value = "p11", defaultValue = "4") final long p11,
                                @NamedArg(value = "p13", defaultValue = "5.5") final float p13,
                                @NamedArg(value = "p15", defaultValue = "6.6") final double p15,
                                @NamedArg(value = "p17", defaultValue = "str") final String p17) {
    }

    public WholeConstructorArgs(final int p1, final char p3) {

    }

    public WholeConstructorArgs(final int p1, @NamedArg("p3") final char p3, @NamedArg("p5") final boolean p5) {

    }
}
