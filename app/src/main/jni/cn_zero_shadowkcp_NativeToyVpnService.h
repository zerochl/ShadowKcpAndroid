/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class cc_aznc_android_nativetoyvpn_NativeToyVpnService */

#ifndef _Included_cn_zero_shadowkcp_NativeToyVpnService
#define _Included_cn_zero_shadowkcp_NativeToyVpnService
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     cc_aznc_android_nativetoyvpn_NativeToyVpnService
 * Method:    getTunnelSock
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cn_zero_shadowkcp_NativeToyVpnService_getTunnelSock
  (JNIEnv *, jobject);

/*
 * Class:     cc_aznc_android_nativetoyvpn_NativeToyVpnService
 * Method:    startTunnel
 * Signature: (Ljava/lang/String;I[B)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_cn_zero_shadowkcp_NativeToyVpnService_startTunnel
  (JNIEnv *, jobject, jstring, jint, jbyteArray);

/*
 * Class:     cc_aznc_android_nativetoyvpn_NativeToyVpnService
 * Method:    tunnelLoop
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cn_zero_shadowkcp_NativeToyVpnService_tunnelLoop
  (JNIEnv *, jobject, jint);

/*
 * Class:     cc_aznc_android_nativetoyvpn_NativeToyVpnService
 * Method:    tunnelStop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_zero_shadowkcp_NativeToyVpnService_tunnelStop
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
