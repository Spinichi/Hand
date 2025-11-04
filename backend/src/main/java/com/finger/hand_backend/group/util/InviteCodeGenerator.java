package com.finger.hand_backend.group.util;

import java.security.SecureRandom;

public final class InviteCodeGenerator {
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RND = new SecureRandom();
    private InviteCodeGenerator() {}
    public static String generate6() {
        StringBuilder sb = new StringBuilder(6);
        for (int i=0;i<6;i++) sb.append(ALPHANUM.charAt(RND.nextInt(ALPHANUM.length())));
        return sb.toString();
    }
}

