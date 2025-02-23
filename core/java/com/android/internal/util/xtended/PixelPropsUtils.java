/*
 * Copyright (C) 2020 The Pixel Experience Project
 *               2021-2022 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.internal.util.xtended;

import android.app.Application;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PixelPropsUtils {

    private static final String PACKAGE_ARCORE = "com.google.ar.core";
    private static final String PACKAGE_RESTORE = "com.google.android.apps.restore";
    private static final String PACKAGE_GPHOTOS = "com.google.android.apps.photos";
    private static final String PACKAGE_GMS = "com.google.android.gms";
    private static final String PROCESS_GMS_UNSTABLE = PACKAGE_GMS + ".unstable";
    private static final String PACKAGE_PS = "com.android.vending";
    public static final String PACKAGE_SETTINGS_SERVICES = "com.google.android.settings.intelligence";

    private static final String sCertifiedFp =
            Resources.getSystem().getString(R.string.config_certifiedFingerprint);

    private static final String sStockFp =
            Resources.getSystem().getString(R.string.config_stockFingerprint);

    private static final String DEVICE = "ro.product.device";
    private static final String TAG = PixelPropsUtils.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final Map<String, Object> propsToChangePixel7Pro;
    private static final Map<String, Object> propsToChangePixelXL;
    private static final Map<String, ArrayList<String>> propsToKeep;

    private static final String[] PackagesToChange = {
            "com.google.android.apps.googleassistant",
            "com.google.android.apps.nbu.files",
            "com.google.android.apps.podcasts",
            "com.google.android.apps.privacy.wildlife",
            "com.google.android.apps.subscriptions.red",
            "com.google.android.apps.tachyon",
            "com.google.android.apps.wallpaper",
            "com.google.android.contacts",
            "com.google.android.deskclock",
            "com.google.android.inputmethod.latin",
            "com.google.android.apps.turbo",
            "com.google.android.googlequicksearchbox",
            "com.android.chrome",
            "com.breel.wallpapers20",
            PACKAGE_PS
    };

    private static final String[] packagesToKeep = {
            "com.google.android.GoogleCamera.Cameight",
            "com.google.android.GoogleCamera.Go",
            "com.google.android.GoogleCamera.Urnyx",
            "com.google.android.GoogleCameraAsp",
            "com.google.android.GoogleCameraCVM",
            "com.google.android.GoogleCameraEng",
            "com.google.android.GoogleCameraEng2",
            "com.google.android.GoogleCameraGood",
            "com.google.android.MTCL83",
            "com.google.android.UltraCVM",
            "com.google.android.apps.cameralite",
            "com.google.android.apps.recorder",
            "com.google.android.apps.wearables.maestro.companion",
            "com.google.android.apps.youtube.kids",
            "com.google.android.apps.youtube.music",
            "com.google.android.dialer",
            "com.google.android.euicc",
            "com.google.android.youtube",
            "com.google.ar.core",
            "com.google.android.apps.restore"
    };

    // Codenames for currently supported Pixels by Google
    private static final String[] pixelCodenames = {
            "cheetah",
            "panther",
            "bluejay",
            "oriole",
            "raven",
            "redfin",
            "barbet",
            "bramble",
            "sunfish",
            "coral",
            "flame"
    };

    private static volatile boolean sIsGms = false;
    private static volatile boolean sIsFinsky = false;

    static {
        propsToKeep = new HashMap<>();
        propsToKeep.put(PACKAGE_SETTINGS_SERVICES, new ArrayList<>(Collections.singletonList("FINGERPRINT")));
        propsToChangePixel7Pro = new HashMap<>();
        propsToChangePixel7Pro.put("BRAND", "google");
        propsToChangePixel7Pro.put("MANUFACTURER", "Google");
        propsToChangePixel7Pro.put("DEVICE", "cheetah");
        propsToChangePixel7Pro.put("PRODUCT", "cheetah");
        propsToChangePixel7Pro.put("MODEL", "Pixel 7 Pro");
        propsToChangePixel7Pro.put(
                "FINGERPRINT", "BUILD_FINGERPRINT := google/cheetah/cheetah:13/TQ2A.230505.002/9891397:user/release-keys");
        propsToChangePixelXL = new HashMap<>();
        propsToChangePixelXL.put("BRAND", "google");
        propsToChangePixelXL.put("MANUFACTURER", "Google");
        propsToChangePixelXL.put("DEVICE", "marlin");
        propsToChangePixelXL.put("PRODUCT", "marlin");
        propsToChangePixelXL.put("MODEL", "Pixel XL");
        propsToChangePixelXL.put(
                "FINGERPRINT", "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys");
    }

    public static void setProps(String packageName) {
        Map<String, Object> propsToChange = new HashMap<>();
        boolean isPixelDevice = Arrays.asList(pixelCodenames).contains(SystemProperties.get(DEVICE));
        if (packageName == null || (Arrays.asList(packagesToKeep).contains(packageName))) {
            return;
        }
        if (packageName.equals(PACKAGE_PS)) {
            sIsFinsky = true;
            return;
        }
        if (packageName.startsWith("com.google.")
                || Arrays.asList(PackagesToChange).contains(packageName)) {
            if (packageName.equals(PACKAGE_GPHOTOS)) {
                if (SystemProperties.getBoolean("persist.sys.pixelprops.gphotos", false)) {
                    propsToChange.putAll(propsToChangePixelXL);
                } else {
                    if (isPixelDevice) return;
                    propsToChange.putAll(propsToChangePixel7Pro);
                }
            } else {
                propsToChange.putAll(propsToChangePixel7Pro);
            }
            dlog("Defining props for: " + packageName);
            if (!isPixelDevice) {
                for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)) {
                        dlog("Not defining " + key + " prop for: " + packageName);
                        continue;
                    }
                    dlog("Defining " + key + " prop for: " + packageName);
                    setPropValue(key, value);
                }
            }
            if (packageName.equals(PACKAGE_GMS)) {
                final String processName = Application.getProcessName();
                if (processName.equals(PROCESS_GMS_UNSTABLE)) {
                    sIsGms = true;
                }
            }
            boolean useFallbackFp = false;
            if (!sCertifiedFp.isEmpty() && (sIsGms || sIsFinsky)) {
                dlog("Setting certified fingerprint for: " + packageName);
                setPropValue("FINGERPRINT", sCertifiedFp);
            } else if (!sStockFp.isEmpty() && packageName.equals(PACKAGE_ARCORE)) {
                dlog("Setting stock fingerprint for: " + packageName);
                setPropValue("FINGERPRINT", sStockFp);
            } else if (sIsGms) {
                dlog("Setting Pixel XL fingerprint for: " + packageName);
                setPropValue("FINGERPRINT", "google/marlin/marlin:7.1.2/NJH47F/4146041:user/release-keys");
                setPropValue("PRODUCT", "marlin");
                setPropValue("DEVICE", "marlin");
                setPropValue("MODEL", "Pixel XL");
                setVersionField("DEVICE_INITIAL_SDK_INT", Build.VERSION_CODES.N_MR1);
                setPropValue("TYPE", "userdebug");
                useFallbackFp = true;
            }
            if (!useFallbackFp && sIsGms && Build.VERSION.DEVICE_INITIAL_SDK_INT > Build.VERSION_CODES.S) {
                dlog("Setting sdk to 32");
                setVersionField("DEVICE_INITIAL_SDK_INT", Build.VERSION_CODES.S);
            }
            // Set proper indexing fingerprint
            if (packageName.equals(PACKAGE_SETTINGS_SERVICES)) {
                setPropValue("FINGERPRINT", Build.VERSION.INCREMENTAL);
            }
        }
    }

    private static void setPropValue(String key, Object value) {
        try {
            dlog("Defining prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void setVersionField(String key, Integer value) {
        try {
            // Unlock
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);

            // Edit
            field.set(null, value);

            // Lock
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to spoof Build." + key, e);
        }
    }

    private static boolean isCallerSafetyNet() {
        return sIsGms && Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("DroidGuard"));
    }

    public static void onEngineGetCertificateChain() {
        // Check stack for SafetyNet or Play Integrity
        if (isCallerSafetyNet() || sIsFinsky) {
            Log.i(TAG, "Blocked key attestation sIsGms=" + sIsGms + " sIsFinsky=" + sIsFinsky);
            throw new UnsupportedOperationException();
        }
    }

    public static void dlog(String msg) {
      if (DEBUG) Log.d(TAG, msg);
    }
}
