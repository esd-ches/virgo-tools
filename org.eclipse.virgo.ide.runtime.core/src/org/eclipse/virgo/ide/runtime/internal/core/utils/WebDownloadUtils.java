/*******************************************************************************
 * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.runtime.internal.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.DateUtil;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
import org.osgi.framework.ServiceReference;

/**
 * Utility to download files using Apache HTTPClient with using Eclipse's proxy service.
 *
 * @author Christian Dupuis
 * @since 1.0.0
 */
public class WebDownloadUtils {

    private static final int CONNNECT_TIMEOUT = 60000;

    private static final int HTTP_PORT = 80;

    private static final int SOCKET_TIMEOUT = 60000;

    private static Credentials getCredentials(IProxyData proxyData, String host) {
        int i = proxyData.getUserId().indexOf("\\");
        if (i > 0 && i < proxyData.getUserId().length() - 1) {
            return new NTCredentials(proxyData.getUserId().substring(i + 1), proxyData.getPassword(), host, proxyData.getUserId().substring(0, i));
        } else {
            return new UsernamePasswordCredentials(proxyData.getUserId(), proxyData.getPassword());
        }
    }

    private static String getHost(String repositoryUrl) {
        String result = repositoryUrl;
        int colonSlashSlash = repositoryUrl.indexOf("://");

        if (colonSlashSlash >= 0) {
            result = repositoryUrl.substring(colonSlashSlash + 3);
        }

        int colonPort = result.indexOf(':');
        int requestPath = result.indexOf('/');

        int substringEnd;

        // minimum positive, or string length
        if (colonPort > 0 && requestPath > 0) {
            substringEnd = Math.min(colonPort, requestPath);
        } else if (colonPort > 0) {
            substringEnd = colonPort;
        } else if (requestPath > 0) {
            substringEnd = requestPath;
        } else {
            substringEnd = result.length();
        }

        return result.substring(0, substringEnd);
    }

    public static Date getLastModifiedDate(String url, IProgressMonitor monitor) {
        GetMethod method = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        try {
            if (monitor.isCanceled()) {
                return null;
            }
            HttpClient client = new HttpClient();
            method = new GetMethod(url);
            method.setFollowRedirects(true);

            HostConfiguration hostConfiguration = new HostConfiguration();
            hostConfiguration.setHost(getHost(url), HTTP_PORT, "http");

            configureHttpClientProxy(url, client, hostConfiguration);

            client.getHttpConnectionManager().getParams().setSoTimeout(SOCKET_TIMEOUT);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(CONNNECT_TIMEOUT);

            // Execute the GET method
            int statusCode = client.executeMethod(hostConfiguration, method);
            if (statusCode == 200) {

                if (monitor.isCanceled()) {
                    return null;
                }

                Header lastModified = method.getResponseHeader("Last-Modified");
                if (lastModified != null) {
                    return DateUtil.parseDate(lastModified.getValue());
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                if (method != null) {
                    method.releaseConnection();
                }
            } catch (Exception e2) {
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e2) {
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e2) {
            }
        }
        return null;
    }

    public static File downloadFile(String url, File outputPath, IProgressMonitor monitor) {
        GetMethod method = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        File outputFile = null;
        try {
            if (monitor.isCanceled()) {
                return null;
            }
            HttpClient client = new HttpClient();
            method = new GetMethod(url);
            method.setFollowRedirects(true);

            HostConfiguration hostConfiguration = new HostConfiguration();
            hostConfiguration.setHost(getHost(url), HTTP_PORT, "http");

            configureHttpClientProxy(url, client, hostConfiguration);

            client.getHttpConnectionManager().getParams().setSoTimeout(SOCKET_TIMEOUT);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(CONNNECT_TIMEOUT);

            // Execute the GET method
            int statusCode = client.executeMethod(hostConfiguration, method);
            if (statusCode == 200) {

                int ix = method.getPath().lastIndexOf('/');
                String fileName = method.getPath().substring(ix + 1);
                monitor.subTask("Downloading file '" + fileName + "'");

                if (monitor.isCanceled()) {
                    return null;
                }

                Header[] header = method.getResponseHeaders("content-length");
                long totalBytes = Long.valueOf(header[0].getValue());

                InputStream is = method.getResponseBodyAsStream();
                bis = new BufferedInputStream(is);
                outputFile = new File(outputPath, fileName);
                fos = new FileOutputStream(outputFile);
                long bytesRead = 0;
                byte[] bytes = new byte[8192];
                int count = bis.read(bytes);
                while (count != -1 && count <= 8192) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    bytesRead = bytesRead + count;
                    float percent = Float.valueOf(bytesRead) / Float.valueOf(totalBytes) * 100;
                    monitor.subTask(
                        "Downloading file '" + fileName + "': " + bytesRead + "bytes/" + totalBytes + "bytes (" + Math.round(percent) + "%)");
                    fos.write(bytes, 0, count);
                    count = bis.read(bytes);
                }
                if (count != -1) {
                    fos.write(bytes, 0, count);
                }
            }

            return outputFile;
        } catch (Exception e) {
            ServerCorePlugin.getDefault().getLog().log(
                new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID, 1, "Error downloading bundle/library", e));
        } finally {
            try {
                if (method != null) {
                    method.releaseConnection();
                }
            } catch (Exception e2) {
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e2) {
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e2) {
            }
        }

        return null;
    }

    private static void configureHttpClientProxy(String url, HttpClient client, HostConfiguration hostConfiguration) {
        ServiceReference services = ServerCorePlugin.getDefault().getBundle().getBundleContext().getServiceReference(IProxyService.class.getName());
        if (services != null) {
            IProxyService proxyService = (IProxyService) ServerCorePlugin.getDefault().getBundle().getBundleContext().getService(services);
            IProxyData proxyData = proxyService.getProxyDataForHost(getHost(url), IProxyData.HTTP_PROXY_TYPE);
            if (proxyService.isProxiesEnabled() && proxyData != null) {
                hostConfiguration.setProxy(proxyData.getHost(), proxyData.getPort());
            }
            if (proxyData != null && proxyData.isRequiresAuthentication()) {
                client.getState().setProxyCredentials(AuthScope.ANY, getCredentials(proxyData, getHost(url)));
            }
        }
    }

}
