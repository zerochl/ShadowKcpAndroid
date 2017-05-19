package cn.zero.shadowkcp;

/**
 * Created by hongli on 2017/5/9.
 */

public class NativeHelper {
    static {
        System.loadLibrary("ZeroVPN");   //defaultConfig.ndk.moduleName
    }
    public native String getCLanguageString();
    public native String startServer();
}
