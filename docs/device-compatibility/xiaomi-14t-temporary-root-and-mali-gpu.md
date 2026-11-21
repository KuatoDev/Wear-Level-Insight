# Xiaomi 14T: temporary-root shell handling and Mali GPU detection

## Summary

This report documents two compatibility changes tested with Wear-Level-Insight 4.0.1 on a Xiaomi 14T. The changes were made locally in the decompiled Smali representation because this public repository currently contains the Android resources and build configuration but no Kotlin/Java application source files.

**This PR is a compatibility report and proof-of-concept description, not a claim that the app's source implementation has been changed.** The maintainers should port the behaviour to the original source code when it is available.

## Test device

- Device: Xiaomi 14T
- SoC/platform: MediaTek MT6897
- GPU: ARM Mali-G615 MC6
- GPU API reported by SurfaceFlinger: OpenGL ES 3.2
- Android: Android 16 / HyperOS 3 (reported device build: OS3.0.304.0.WNERUXM)
- WLI version: 4.0.1

## 1. Temporary-root shell lifecycle

### Scope

This is **not a KernelSU compatibility fix** and does not modify KernelSU. It is an app-side shell-cache adjustment that was needed for the temporary-root (Jailbreak Root) flow tested on the Xiaomi 14T.

### Observed behaviour

The app creates a `su` process, checks that the shell responds, and uses the result of `id` to determine whether the shell has UID 0. The shell is cached and can be reused later.

With the temporary-root setup on this Xiaomi 14T, reusing a cached shell whose status was not a successful/active status led the app to fall back to ordinary `sh`; the root checker then did not recognize root.

### Tested app-side adjustment

The local Smali proof of concept changes the cache rule:

- Reuse the cached shell only when its status is exactly `1` (the successful/active state observed by the app).
- If the cached shell has any other status, close it and clear the cached reference before attempting to obtain a new shell.
- Keep the existing root verification: the app must still see `uid=0` from `id` before granting root access.

This made WLI recognize root in the tested temporary-root/Jailbreak Root environment.

### Control test: normal KernelSU Next

The unmodified WLI 4.0.1 was also tested on a Redmi 10C with KernelSU Next providing normal in-kernel root. It worked there without this patch. This is why the change should not be described as a generic KernelSU fix; the observed need was specific to the temporary-root flow tested on the Xiaomi 14T.

## 2. MediaTek Mali GPU identification

### Observed behaviour

The current GPU lookup reads:

```sh
cat /sys/class/kgsl/kgsl-3d0/gpu_model
```

That is a Qualcomm KGSL-specific sysfs path. It is not present on the tested MediaTek/Mali device, so the app returns `Unknown`.

### Tested proof of concept

SurfaceFlinger exposes the GLES renderer on this device:

```sh
dumpsys SurfaceFlinger | grep '^GLES:'
```

Output observed:

```text
GLES: ARM, Mali-G615 MC6, OpenGL ES 3.2 v1.r44p1-01eac0...
```

The local proof of concept extracts the renderer name:

```sh
dumpsys SurfaceFlinger | grep '^GLES:' | sed -n 's/^GLES: [^,]*, \([^,]*\),.*/\1/p'
```

This returns:

```text
Mali-G615 MC6
```

After changing the GPU lookup in the local build, WLI displayed `Mali-G615 MC6` instead of `Unknown`.

### Suggested source-level implementation

The SurfaceFlinger shell command is a tested proof of concept. For the upstream source implementation, it would be preferable to query the active OpenGL ES renderer through Android's EGL/GLES APIs (for example, `GLES20.glGetString(GLES20.GL_RENDERER)` while a valid GL context is current), with a safe fallback when no GL context can be created. This avoids depending on a Qualcomm-only sysfs path and avoids requiring a shell command solely to identify the GPU.

## Validation

Both local changes were tested together on the Xiaomi 14T:

- WLI launched and recognized root through the tested temporary-root flow.
- The GPU field displayed `Mali-G615 MC6`.
- The updated APK installed over the existing app with `adb install -r` and launched successfully.
- The APK was built by overlaying the rebuilt DEX onto the original APK. This was necessary because a normal Apktool rebuild on Windows dropped case-only duplicate resource paths. The overlay kept all 878 original non-DEX, non-signature archive entries byte-for-byte identical; the resulting APK passed zipalign and signature verification.

## Maintainer follow-up

The public repository currently exposes the Android manifest, resources, and Gradle configuration, but no Kotlin/Java application sources. Please advise where the application source is maintained or provide the preferred contribution route. The two behaviours above can then be ported as a proper source-level change rather than maintaining a Smali-only patch.
