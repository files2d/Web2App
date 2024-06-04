## android.yml
```
name: Build and Release Android APK

on:
  push:
    branches:
      - "main"
  pull_request:
    branches:
      - "main"

jobs:
  build:

    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
        cache: gradle


    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build Release APK
      run: ./gradlew assembleRelease

    - name: Create Release
      id: create_release
      uses: actions/create-release@v1
      env:
        GITHUB_TOKEN: ${{ secrets.TOKEN }}
      with:
        tag_name: v1.0-${{ github.run_number }}  # 每次创建新的发布时，使用唯一的标签名称
        release_name: Release-${{ github.run_number }}  # 你可以自定义发布名称
        body: |
          Changes in this release:
          - Added new features
          - Bug fixes
        draft: false
        prerelease: false
      if: success()

    - name: Get current date
      id: get_date
      run: echo "::set-output name=date::$(date '+%Y%m%d-%H%M%S')"

    - name: Check APK file exists
      run: ls -l app/build/outputs/apk/release/
      
    - name: Upload APK to Release
      id: upload-release-asset
      uses: actions/upload-release-asset@v1
      env:
        GITHUB_TOKEN: ${{ secrets.TOKEN }}
      with:
        upload_url: ${{ steps.create_release.outputs.upload_url }}
        asset_path: app/build/outputs/apk/release/app-release.apk  # 生成的 APK 文件路径
        asset_name: app-release-${{ github.run_number }}-${{ steps.get_date.outputs.date }}.apk  # 包含日期时间信息的 APK 文件名
        asset_content_type: application/vnd.android.package-archive
```

注意要生成`android.jks`文件才能构建已签名的APK,生成命令：
```
keytool -genkey -v -keystore android.jks -keyalg RSA -keysize 2048 -validity 10000 -alias android
```

可以直接用[文件](../android.jks) 密码：123456

然后配置`app/build.gradle`:

加入：
```
android {
    ...
    signingConfigs {
        release {
            keyAlias 'android'
            keyPassword '123456'
            storeFile file('../android.jks')  // 确保路径正确
            storePassword '123456'
            v1SigningEnabled true
            v2SigningEnabled true
        }
    }
    
    buildTypes {
        release {
            minifyEnabled true
            zipAlignEnabled true
            signingConfig signingConfigs.release
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    
        debug {
            signingConfig signingConfigs.release
        }
    }
}
```








