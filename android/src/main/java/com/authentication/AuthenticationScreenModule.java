package com.authentication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.List;

interface ActivityResultInterface {
  void callback(int requestCode, int resultCode, Intent data);
}

public class AuthenticationScreenModule extends ReactContextBaseJavaModule {

  private Callback mCallback;

  private AuthenticationScreenActivityEventListener mActivityEventListener;
  private final ReactApplicationContext mReactContext;

  Activity mActivity;


  private KeyguardManager mKeyguardManager;

  public AuthenticationScreenModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mKeyguardManager = (KeyguardManager) reactContext.getSystemService(Context.KEYGUARD_SERVICE);
    mReactContext = reactContext;
    mActivityEventListener = new AuthenticationScreenActivityEventListener(new ActivityResultInterface() {
      @Override
      public void callback(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
      }
    });

    //todo: remove this listener when not needed
    mReactContext.addActivityEventListener(mActivityEventListener);

  }

  @Override
  public String getName() {
    return "AuthenticationScreenModule";
  }


  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1) {
      // Challenge completed, proceed with using cipher
      if (resultCode == Activity.RESULT_OK) {
        mCallback.invoke(null, resultCode);
      } else {
        mCallback.invoke("Wrong passcode", resultCode);
        // The user canceled or didn’t complete the lock screen
        // operation. Go to error/cancellation flow.
      }
    }
  }

  /**
   *
   * based on AOSP KeyguardManager.java for API 23+
   *
   * Get an intent to prompt the user to confirm credentials (pin, pattern or password)
   * for the current user of the device. The caller is expected to launch this activity using
   * {@link android.app.Activity#startActivityForResult(Intent, int)} and check for
   * {@link android.app.Activity#RESULT_OK} if the user successfully completes the challenge.
   *
   * @return the intent for launching the activity or null if no password is required.
   **/
  public Intent createConfirmDeviceCredentialIntent(CharSequence title, CharSequence description) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
      return mKeyguardManager.createConfirmDeviceCredentialIntent(title, description);
    } else {
      Intent intent = new Intent("android.app.action.CONFIRM_DEVICE_CREDENTIAL");

      String content;

      if (title != null) {
        content = title.toString();
        content += "\n\n";
        content += description;
      } else {
        content = description.toString();
      }

      intent.putExtra("android.app.extra.TITLE", content);

      // explicitly set the package for security
      intent.setPackage(getSettingsPackageForIntent(intent));
      return intent;
    }
  }


  /**
   * based on AOSP KeyguardManager.java for API 23+
   */
  private String getSettingsPackageForIntent(Intent intent) {
    @SuppressLint("InlinedApi") List<ResolveInfo> resolveInfos = mReactContext.getPackageManager()
            .queryIntentActivities(intent, PackageManager.MATCH_SYSTEM_ONLY);
    for (int i = 0; i < resolveInfos.size(); i++) {
      return resolveInfos.get(i).activityInfo.packageName;
    }

    return "com.android.settings";
  }



  @ReactMethod
  public void show(String message, final Callback callback) {
    mActivity = getCurrentActivity();
    mCallback = callback;

    Intent intent = createConfirmDeviceCredentialIntent(null, message);

    if (intent != null) {
      mActivity.startActivityForResult(intent, 1);
    }
    else {
      callback.invoke("No Passcode set", null);
    }
  }





}
