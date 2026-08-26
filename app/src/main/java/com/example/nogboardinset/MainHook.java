package com.example.nogboardinset;

import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * NoGboardBottomInset v2 - Hook sul FRAMEWORK ANDROID (InputMethodService),
 * NON sulle classi offuscate di Gboard. Questo approccio è stabile tra le
 * build di Gboard perché InputMethodService è una classe pubblica del
 * framework Android che non viene offuscata.
 */
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "NoGboardBottomInset";
    private static final String GBOARD_PACKAGE = "com.google.android.inputmethod.latin";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!GBOARD_PACKAGE.equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log(TAG + ": === GBOARD CARICATO === package=" + lpparam.packageName
                + " process=" + lpparam.processName + " sdk=" + Build.VERSION.SDK_INT);

        hookCanImeRenderGesturalNavButtons(lpparam);
        hookOnComputeInsets(lpparam);
        hookOnEvaluateFullscreenMode(lpparam);
        hookImeSwitcherArrowGeneric(lpparam);
        hookGenericPaddingSafetyNet(lpparam);
        hookOnCreateInputView(lpparam);
    }

    private void hookCanImeRenderGesturalNavButtons(final LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    InputMethodService.class,
                    "canImeRenderGesturalNavButtons",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            XposedBridge.log(TAG + ": canImeRenderGesturalNavButtons() forzato a FALSE");
                            return false;
                        }
                    });
            XposedBridge.log(TAG + ": hook su InputMethodService.canImeRenderGesturalNavButtons() installato");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": errore hookCanImeRenderGesturalNavButtons: " + t);
        }
    }

    private void hookOnComputeInsets(final LoadPackageParam lpparam) {
        try {
            Class<?> insetsClass = XposedHelpers.findClass(
                    "android.inputmethodservice.InputMethodService$Insets", lpparam.classLoader);

            XposedHelpers.findAndHookMethod(
                    InputMethodService.class,
                    "onComputeInsets",
                    insetsClass,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                Object outInsets = param.args[0];
                                Field visibleTopInsets = insetsClass.getField("visibleTopInsets");
                                Field contentTopInsets = insetsClass.getField("contentTopInsets");
                                int visible = visibleTopInsets.getInt(outInsets);
                                int content = contentTopInsets.getInt(outInsets);
                                XposedBridge.log(TAG + ": onComputeInsets -> visibleTopInsets=" + visible
                                        + " contentTopInsets=" + content);
                            } catch (Throwable t) {
                                XposedBridge.log(TAG + ": errore lettura Insets: " + t);
                            }
                        }
                    });
            XposedBridge.log(TAG + ": hook su InputMethodService.onComputeInsets() installato");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": errore hookOnComputeInsets: " + t);
        }
    }

    private void hookOnEvaluateFullscreenMode(final LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    InputMethodService.class,
                    "onEvaluateFullscreenMode",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            XposedBridge.log(TAG + ": onEvaluateFullscreenMode chiamato, result="
                                    + param.getResult());
                        }
                    });
            XposedBridge.log(TAG + ": hook su InputMethodService.onEvaluateFullscreenMode() installato");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": errore hookOnEvaluateFullscreenMode: " + t);
        }
    }

    private void hookImeSwitcherArrowGeneric(final LoadPackageParam lpparam) {
        try {
            Method m = findMethodBooleanArgOnClass(InputMethodService.class,
                    "onCustomImeSwitcherButtonRequestedVisible");
            if (m != null) {
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.args[0] = false;
                        XposedBridge.log(TAG + ": [InputMethodService] onCustomImeSwitcherButtonRequestedVisible forzato a false");
                    }
                });
                XposedBridge.log(TAG + ": hook IME switcher trovato direttamente su InputMethodService");
                return;
            }

            XposedBridge.log(TAG + ": onCustomImeSwitcherButtonRequestedVisible non trovato su InputMethodService (normale, log-only)");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": errore hookImeSwitcherArrowGeneric: " + t);
        }
    }

    private void hookGenericPaddingSafetyNet(final LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    View.class,
                    "setPadding",
                    int.class, int.class, int.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            int bottom = (int) param.args[3];
                            if (bottom > 0) {
                                XposedBridge.log(TAG + ": setPadding bottom=" + bottom + " -> azzerato su "
                                        + param.thisObject.getClass().getSimpleName());
                                param.args[3] = 0;
                            }
                        }
                    });
            XposedBridge.log(TAG + ": safety-net su View.setPadding installato");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": errore hookGenericPaddingSafetyNet: " + t);
        }
    }

    private void hookOnCreateInputView(final LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    InputMethodService.class,
                    "onCreateInputView",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object result = param.getResult();
                            if (result instanceof View) {
                                View v = (View) result;
                                XposedBridge.log(TAG + ": onCreateInputView -> " + v.getClass().getName()
                                        + " paddingBottom=" + v.getPaddingBottom());
                            }
                        }
                    });
            XposedBridge.log(TAG + ": hook su InputMethodService.onCreateInputView() installato");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": errore hookOnCreateInputView: " + t);
        }
    }

    private Method findMethodBooleanArgOnClass(Class<?> clazz, String name) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.getName().equals(name)) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length == 1 && (params[0] == boolean.class || params[0] == Boolean.class)) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }
}
