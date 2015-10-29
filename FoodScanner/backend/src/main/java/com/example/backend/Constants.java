package com.example.backend;

/**
 * Created by mlenarto on 9/21/15.
 */
public class Constants {
    public static final String WEB_CLIENT_ID = "419943060428-q8lotlqd42hdl3je6n8ca2oirgc9cnru.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID = "419943060428-ql5sh80b9f8mfj7mr9gmpkfs30tmp0b4.apps.googleusercontent.com";
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;
    public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    public enum MealType {
        BREAKFAST,
        LUNCH,
        DINNER,
        SNACK
    }
}
