package com.gec.lightlink.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

/**
 * @author sig
 * @version 1.01
 *
 */
@SuppressWarnings("deprecation")
public class NetworkUtil {

	// 获取本机动态IP
	public static String getLocalAddress() throws SocketException {
		for (Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces(); eni.hasMoreElements();) {
			NetworkInterface ni = eni.nextElement();
			for (Enumeration<InetAddress> eia = ni.getInetAddresses(); eia.hasMoreElements();) {
				InetAddress ia = eia.nextElement();
				String hostAddr = ia.getHostAddress();
				if (!ia.isLoopbackAddress() && InetAddressUtils.isIPv4Address(hostAddr)) {
					return hostAddr;
				}
			}
		}
		return null;
	}
}
