{
  description = "Devshell for light-gpslogger Android app";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true;
          config.android_sdk.accept_license = true;
        };
        androidSdk = pkgs.androidenv.composeAndroidPackages {
          buildToolsVersions = [ "34.0.0" "35.0.0" ];
          platformVersions = [ "34" "35" ];
          includeEmulator = false;
          includeSystemImages = false;
          includeSources = false;
          includeNDK = false;
        };
        sdk = androidSdk.androidsdk;
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk17
            android-tools
            sdk
          ];

          GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdk}/libexec/android-sdk/build-tools/35.0.0/aapt2";

          shellHook = ''
            export JAVA_HOME="${pkgs.jdk17}"
            export ANDROID_HOME="${sdk}/libexec/android-sdk"
            export ANDROID_SDK_ROOT="$ANDROID_HOME"
            export PATH="$ANDROID_HOME/platform-tools:$PATH"
            echo "Dev shell for light-gpslogger"
            echo "  adb devices               - list connected devices"
            echo "  ./gradlew installDebug    - build and install debug APK"
            echo "  ./gradlew assembleDebug   - build debug APK only"
          '';
        };
      });
}
