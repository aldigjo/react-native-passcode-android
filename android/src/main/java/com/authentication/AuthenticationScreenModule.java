package com.authentication;

import com.facebook.react.bridge.Callback;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ActivityEventListener;

import android.content.Intent;
import android.content.Context;
import android.app.KeyguardManager;
import android.app.Activity;

interface ActivityResultInterface {
  void callback(int requestCode, int resultCode, Intent data);
}

public class AuthenticationScreenModule extends ReactContextBaseJavaModule {

  private Callback mCallback;

  private AuthenticationActivityEventListener mActivityEventListener;
  private final ReactApplicationContext mReactContext;

  Activity mActivity;


  private KeyguardManager mKeyguardManager;

  public AuthenticationScreenModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mKeyguardManager = (KeyguardManager) reactContext.getSystemService(Context.KEYGUARD_SERVICE);
    mReactContext = reactContext;
    mActivityEventListener = new AuthenticationActivityEventListener(reactContext, new ActivityResultInterface() {
      @Override
      public void callback(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
      }
    });
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


  @ReactMethod
  public void show(final Callback callback) {
    mActivity = getCurrentActivity();
    mCallback = callback;
    System.out.println("SUPPOSED TO BE HEREEEEEEEEEEE");

    Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, "Reset identity?");

    if (intent != null) {
      mActivity.startActivityForResult(intent, 1);
    }
  }

  // Required for RN 0.30+ modules than implement ActivityEventListener
  public void onNewIntent(Intent intent) { }



}