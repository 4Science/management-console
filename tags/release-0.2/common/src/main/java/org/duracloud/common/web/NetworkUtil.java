package org.duracloud.common.web;

import org.apache.log4j.Logger;
import org.duracloud.common.error.DuraCloudCheckedException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class NetworkUtil {

    protected static final Logger log = Logger.getLogger(NetworkUtil.class);

    /**
     * <pre>
     * This method provides the current environment's IP address,
     * taking into account the Internet connection to any of the available
     * machine's Network interfaces.
     * <p/>
     * The outputs can be in octatos or in IPV6 format.
     * </pre>
     */
    public static String getCurrentEnvironmentNetworkIp() {
        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.error(e);
        }

        while (netInterfaces.hasMoreElements()) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress addr = address.nextElement();
                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress() &&
                    !(addr.getHostAddress().indexOf(":") > -1)) {
                    return addr.getHostAddress();
                }
            }
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn(e);
            return "127.0.0.1";
        }
    }

    public static void waitForStartup(String url)
        throws DuraCloudCheckedException {
        isRunning(url, true);
    }

    public static void waitForShutdown(String url)
        throws DuraCloudCheckedException {
        isRunning(url, false);
    }

    private static void isRunning(String url, boolean state)
        throws DuraCloudCheckedException {
        int tries = 0;
        int maxTries = 20;
        while (isRunning(url) != state && tries++ < maxTries) {
            sleep(500);
        }

        if (isRunning(url) != state) {
            String err = state ? "Not running" : "Still running";
            throw new DuraCloudCheckedException(err + ": " + url);
        }
    }

    private static boolean isRunning(String url) {
        boolean running = false;

        RestHttpHelper httpHelper = new RestHttpHelper();
        RestHttpHelper.HttpResponse response = null;
        try {
            response = httpHelper.get(url);
        } catch (Exception e) {
            // do nothing.
        }

        if (response != null && response.getStatusCode() == 200) {
            running = true;
        }

        return running;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing.
        }
    }

}